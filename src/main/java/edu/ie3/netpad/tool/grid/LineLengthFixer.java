/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.tool.grid;

import edu.ie3.datamodel.models.input.connector.LineInput;
import edu.ie3.datamodel.models.input.container.JointGridContainer;
import edu.ie3.datamodel.models.input.container.RawGridElements;
import edu.ie3.datamodel.models.input.container.SubGridContainer;
import edu.ie3.datamodel.utils.ContainerUtils;
import edu.ie3.datamodel.utils.GridAndGeoUtils;
import edu.ie3.netpad.grid.GridModel;
import edu.ie3.netpad.io.event.ReadGridEvent;
import edu.ie3.netpad.tool.controller.ToolDialogs;
import edu.ie3.util.geo.GeoUtils;
import java.util.*;
import java.util.stream.Collectors;
import javax.measure.quantity.Length;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.units.indriya.ComparableQuantity;

public class LineLengthFixer {

  private static final Logger log = LoggerFactory.getLogger(LineLengthFixer.class);

  private LineLengthFixer() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Fix the line length discrepancy based on the user given {@link ToolDialogs.FixLineLengthData}
   *
   * @param resolutionMode Selected resolution mode
   * @param selectedSubnets Subnets to apply adjustments to
   */
  public static Optional<ReadGridEvent> execute(
      LineLengthResolutionMode resolutionMode,
      Set<Integer> selectedSubnets,
      Map<UUID, GridModel> allSubGrids) {
    JointGridContainer updatedGrid;

    /* Act depending on the chosen resolution mode */
    switch (resolutionMode) {
      case GEOGRAPHICAL:
        updatedGrid = setElectricalToGeographicalLineLength(allSubGrids, selectedSubnets);
        break;
      case ELECTRICAL:
      /* TODO CK: Figure out, what to do here */
      default:
        log.error("Unknown resolution mode '{}'", resolutionMode);
        return Optional.empty();
    }

    /* Build a new event and inform the listeners about the "new" / adapted grid model */
    return Optional.of(new ReadGridEvent(updatedGrid));
  }

  /**
   * Sets the electrical length of all lines within the selected sub nets to the length of their
   * geographical line string if apparent. If not, it is set to the geographical distance between
   * start and end node.
   *
   * @param selectedSubnets Subnets to apply adjustments to
   * @return A {@link JointGridContainer} with updated line models
   */
  private static JointGridContainer setElectricalToGeographicalLineLength(
      Map<UUID, GridModel> subGrids, Set<Integer> selectedSubnets) {
    /* Adjust the electrical line length to be the same as the geographical distance */
    List<SubGridContainer> subGridContainers =
        subGrids.values().parallelStream()
            .map(GridModel::getSubGridContainer)
            .map(
                subGridContainer -> {
                  if (!selectedSubnets.contains(subGridContainer.getSubnet())) {
                    /* If this grid isn't selected, hand it back, as it is */
                    return subGridContainer;
                  } else {
                    /* Update all lines */
                    Set<LineInput> lines =
                        subGridContainer.getRawGrid().getLines().parallelStream()
                            .map(LineLengthFixer::setLineLengthToGeographicDistance)
                            .collect(Collectors.toSet());

                    /* Put together, what has been there before */
                    RawGridElements rawGrid =
                        new RawGridElements(
                            subGridContainer.getRawGrid().getNodes(),
                            lines,
                            subGridContainer.getRawGrid().getTransformer2Ws(),
                            subGridContainer.getRawGrid().getTransformer3Ws(),
                            subGridContainer.getRawGrid().getSwitches(),
                            subGridContainer.getRawGrid().getMeasurementUnits());
                    return new SubGridContainer(
                        subGridContainer.getGridName(),
                        subGridContainer.getSubnet(),
                        rawGrid,
                        subGridContainer.getSystemParticipants(),
                        subGridContainer.getGraphics());
                  }
                })
            .collect(Collectors.toList());

    /* Assemble all sub grids to one container */
    return ContainerUtils.combineToJointGrid(subGridContainers);
  }

  /**
   * Adjusts the line length to the length of their geographical line string if apparent. If not, it
   * is set to the geographical distance between start and end node.
   *
   * @param line line model to adjust
   * @return The adjusted line model
   * @deprecated This method should be transferred to PowerSystemDataModel
   */
  @Deprecated
  private static LineInput setLineLengthToGeographicDistance(LineInput line) {
    ComparableQuantity<Length> lineLength;
    lineLength =
        lengthOfLineString(line.getGeoPosition())
            .orElseGet(
                () -> {
                  log.warn(
                      "Cannot determine the length of the line string of line '{}' as it only contains one coordinate."
                          + " Take distance between it's nodes instead.",
                      line);
                  return GridAndGeoUtils.distanceBetweenNodes(line.getNodeA(), line.getNodeB());
                });
    return line.copy().length(lineLength).build();
  }

  /**
   * Calculate the length of a line string
   *
   * @param lineString The line string to calculate the length of
   * @return An option to the length, if it can be determined
   * @deprecated This method should be transferred to PowerSystemUtils
   */
  @Deprecated
  private static Optional<ComparableQuantity<Length>> lengthOfLineString(LineString lineString) {
    Coordinate[] coordinates = lineString.getCoordinates();

    if (coordinates.length == 1) {
      return Optional.empty();
    }

    /* Go over the line piecewise and sum up the distance */
    Coordinate a = coordinates[0];
    Coordinate b = coordinates[1];
    ComparableQuantity<Length> length = GeoUtils.calcHaversine(a.x, a.y, b.x, b.y);
    for (int coordIndex = 2; coordIndex < coordinates.length; coordIndex++) {
      a = b;
      b = coordinates[coordIndex];
      length = length.add(GeoUtils.calcHaversine(a.x, a.y, b.x, b.y));
    }

    return Optional.of(length);
  }
}
