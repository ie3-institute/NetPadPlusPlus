/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.controller;

import edu.ie3.datamodel.graph.SubGridGate;
import edu.ie3.datamodel.graph.SubGridTopologyGraph;
import edu.ie3.datamodel.models.UniqueEntity;
import edu.ie3.datamodel.models.input.InputEntity;
import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.input.connector.LineInput;
import edu.ie3.datamodel.models.input.connector.Transformer2WInput;
import edu.ie3.datamodel.models.input.container.*;
import edu.ie3.datamodel.models.input.system.LoadInput;
import edu.ie3.datamodel.models.input.system.PvInput;
import edu.ie3.datamodel.models.input.system.StorageInput;
import edu.ie3.datamodel.models.input.system.SystemParticipantInput;
import edu.ie3.datamodel.utils.ContainerUtils;
import edu.ie3.datamodel.utils.GridAndGeoUtils;
import edu.ie3.netpad.grid.GridModel;
import edu.ie3.netpad.grid.GridModification;
import edu.ie3.netpad.grid.ModifiedSubGridData;
import edu.ie3.netpad.grid.event.*;
import edu.ie3.netpad.io.controller.IoController;
import edu.ie3.netpad.io.event.IOEvent;
import edu.ie3.netpad.io.event.ReadGridEvent;
import edu.ie3.netpad.io.event.SaveGridEvent;
import edu.ie3.netpad.map.event.MapEvent;
import edu.ie3.netpad.tool.LineLengthResolutionMode;
import edu.ie3.netpad.tool.controller.ToolController;
import edu.ie3.netpad.tool.controller.ToolDialogs;
import edu.ie3.netpad.tool.event.FixLineLengthRequestEvent;
import edu.ie3.netpad.tool.event.LayoutGridRequestEvent;
import edu.ie3.netpad.tool.event.LayoutGridResponse;
import edu.ie3.netpad.tool.event.ToolEvent;
import edu.ie3.util.geo.GeoUtils;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javax.measure.quantity.Length;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.units.indriya.ComparableQuantity;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 15.05.20
 */
public class GridController {

  private static final Logger log = LoggerFactory.getLogger(GridController.class);

  private static final class InstanceHolder {
    static final GridController INSTANCE = new GridController();
  }

  public static GridController getInstance() {
    return GridController.InstanceHolder.INSTANCE;
  }

  private final Map<UUID, GridModel> subGrids = new LinkedHashMap<>();

  private final ObjectProperty<GridEvent> gridUpdateEventProperty = new SimpleObjectProperty<>();

  private GridController() {

    // register for updates from iOController
    IoController.getInstance().registerGridControllerListener(this.ioEventListener());

    // register for updates from toolController
    ToolController.getInstance().registerGridControllerListener(this.toolEventListener());

    // register for grid context controller updates
    EditGridContextController.getInstance()
        .registerGridControllerListener(
            (observable, oldValue, gridContextEvent) -> handleGridModifications(gridContextEvent));
  }

  public boolean isGridLoaded() {
    return subGrids.isEmpty();
  }

  public Map<UUID, GridModel> getSubGrids() {
    return subGrids;
  }

  private void handleReadGridEvent(ReadGridEvent newValue) {

    // clear subGrids
    subGrids.clear();

    // each change by the I/O controller triggers an update of the whole
    // grid model of this class
    GridContainer receivedGrid = newValue.getGrid();
    this.subGrids.putAll(gridContainerToGridModel(receivedGrid, Collections.emptyMap()));

    // furthermore, all listeners that wanna hear about grid updates are notified
    notifyGridUpdateListener(
        new ReplaceGridEvent(
            receivedGrid.getGridName(),
            subGrids.keySet().stream()
                .collect(
                    Collectors.toMap(
                        uuid -> uuid, uuid -> subGrids.get(uuid).getSubGridContainer()))));

    log.debug("Received changed grid from I/O controller!");
  }

  private Map<UUID, GridModel> gridContainerToGridModel(
      GridContainer gridContainer, Map<UUID, Set<UUID>> gridModelsToEntities) {

    Map<UUID, GridModel> subGridMapping = new LinkedHashMap<>();
    Stream.of(gridContainer)
        .flatMap(
            grid -> {
              if (grid instanceof SubGridContainer) {

                UUID gridModelUuid =
                    findSubGridUuid(gridModelsToEntities, grid).orElseGet(UUID::randomUUID);

                return Stream.of(
                    new GridModel(
                        gridModelUuid, (SubGridContainer) grid, new HashSet<>(), new HashSet<>()));
              } else if (grid instanceof JointGridContainer) {
                SubGridTopologyGraph topologyGraph =
                    ((JointGridContainer) grid).getSubGridTopologyGraph();

                Map<Integer, UUID> subGridUuids = new HashMap<>();
                for (SubGridContainer subGrid : topologyGraph.vertexSet()) {

                  UUID gridModelUuid =
                      findSubGridUuid(gridModelsToEntities, subGrid).orElseGet(UUID::randomUUID);

                  subGridUuids.put(subGrid.getSubnet(), gridModelUuid);
                }

                return topologyGraph.vertexSet().stream()
                    .map(
                        subGrid -> {
                          Set<SubGridGate> subGridGates = topologyGraph.edgesOf(subGrid);

                          UUID thisSubGridUuid = subGridUuids.get(subGrid.getSubnet());
                          Set<UUID> superiorGrids =
                              subGridGates.stream()
                                  .map(
                                      subGridGate ->
                                          subGridUuids.get(subGridGate.getSuperiorSubGrid()))
                                  .filter(uuid -> !uuid.equals(thisSubGridUuid))
                                  .collect(Collectors.toSet());

                          Set<UUID> inferiorGrids =
                              subGridGates.stream()
                                  .map(
                                      subGridGate ->
                                          subGridUuids.get(subGridGate.getInferiorSubGrid()))
                                  .filter(uuid -> !uuid.equals(thisSubGridUuid))
                                  .collect(Collectors.toSet());

                          return new GridModel(
                              thisSubGridUuid, subGrid, superiorGrids, inferiorGrids);
                        });
              } else {
                throw new RuntimeException("Invalid GridContainer instance provided:" + grid);
              }
            })
        .sorted(
            Comparator.comparingDouble(
                x ->
                    x.getSubGridContainer()
                        .getPredominantVoltageLevel()
                        .getNominalVoltage()
                        .getValue()
                        .doubleValue()))
        .forEach(gridModel -> subGridMapping.put(gridModel.getUuid(), gridModel));

    return subGridMapping;
  }

  private Optional<UUID> findSubGridUuid(
      Map<UUID, Set<UUID>> gridModelsToEntities, GridContainer grid) {
    return gridModelsToEntities.entrySet().stream()
        .filter(
            entityUuids ->
                entityUuids
                    .getValue()
                    .equals(
                        grid.allEntitiesAsList().stream()
                            .map(UniqueEntity::getUuid)
                            .collect(Collectors.toSet())))
        .map(Map.Entry::getKey)
        .findAny();
  }

  // todo JH clean this method
  private ChangeListener<ToolEvent> toolEventListener() {
    return (observable, oldValue, newValue) -> {
      if (newValue instanceof LayoutGridRequestEvent) {
        JointGridContainer currentFullGrid =
            ContainerUtils.combineToJointGrid(
                subGrids.values().stream()
                    .map(GridModel::getSubGridContainer)
                    .collect(Collectors.toList()));
        ((LayoutGridRequestEvent) newValue).readGridEventPropertyProperty().set(currentFullGrid);
        log.debug("Received Tool request event");
      } else if (newValue instanceof LayoutGridResponse) {
        handleReadGridEvent(new ReadGridEvent(((LayoutGridResponse) newValue).getGrid()));
        log.debug("Received Tool response event");
      } else if (newValue instanceof FixLineLengthRequestEvent) {
        FixLineLengthRequestEvent event = (FixLineLengthRequestEvent) newValue;
        LineLengthResolutionMode resolutionMode = event.getResolutionMode();
        Set<Integer> selectedSubnets = event.getSelectedSubnets();
        fixLineLength(resolutionMode, selectedSubnets);
      } else {
        throw new RuntimeException("Invalid GridContainer provided!");
      }

      // todo JH

    };
  }

  /**
   * Fix the line length discrepancy based on the user given {@link ToolDialogs.FixLineLengthData}
   *
   * @param resolutionMode Selected resolution mode
   * @param selectedSubnets Subnets to apply adjustments to
   */
  public void fixLineLength(LineLengthResolutionMode resolutionMode, Set<Integer> selectedSubnets) {
    JointGridContainer updatedGrid;

    /* Act depending on the chosen resolution mode */
    switch (resolutionMode) {
      case GEOGRAPHICAL:
        updatedGrid = setElectricalToGeographicalLineLength(selectedSubnets);
        break;
      case ELECTRICAL:
        /* TODO CK: Figure out, what to do here */
      default:
        log.error("Unknown resolution mode '{}'", resolutionMode);
        return;
    }

    /* Build a new event and inform the listeners about the "new" / adapted grid model */
    handleReadGridEvent(new ReadGridEvent(updatedGrid));
  }

  /**
   * Sets the electrical length of all lines within the selected sub nets to the length of their
   * geographical line string if apparent. If not, it is set to the geographical distance between
   * start and end node.
   *
   * @param selectedSubnets Subnets to apply adjustments to
   * @return A {@link JointGridContainer} with updated line models
   */
  private JointGridContainer setElectricalToGeographicalLineLength(Set<Integer> selectedSubnets) {
    /* Adjust the electrical line length to be the same as the geographical distance */
    List<SubGridContainer> subGridContainers =
        GridController.getInstance().getSubGrids().values().parallelStream()
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
                            .map(GridController::setLineLengthToGeographicDistance)
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
                      "Cannot determine the length of the line string of line '{}' as it only contains one coordinate." +
                              " Take distance between it's nodes instead.",
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

  private ChangeListener<IOEvent> ioEventListener() {
    return (observable, oldValue, newValue) -> {
      if (newValue instanceof ReadGridEvent) {
        handleReadGridEvent((ReadGridEvent) newValue);
      } else if (newValue instanceof SaveGridEvent) {
        // the io controller wants to save, hence we need to return the current state of the grid
        // if subGrids consists only of a single subgrid, we can save it directly,
        // otherwise we need to reassemble the joint grid
        if (subGrids.size() == 1) {
          ((SaveGridEvent) newValue)
              .readGridEventPropertyProperty()
              .set(subGrids.values().iterator().next().getSubGridContainer());
        } else {
          JointGridContainer currentFullGrid =
              ContainerUtils.combineToJointGrid(
                  subGrids.values().stream()
                      .map(GridModel::getSubGridContainer)
                      .collect(Collectors.toList()));
          ((SaveGridEvent) newValue).readGridEventPropertyProperty().set(currentFullGrid);
        }
      } else {
        log.warn(
            "GridController cannot handle instance of {}.", newValue.getClass().getSimpleName());
      }
    };
  }

  public ObjectProperty<GridEvent> gridUpdateEvents() {
    return gridUpdateEventProperty;
  }

  public ChangeListener<MapEvent> gridMapEventListener() {
    return (observable, oldValue, newValue) -> handleGridModifications(newValue);
  }

  private void handleGridModifications(GridModification gridModification) {
    // each change on grid map events triggers a rebuild of the whole grid model
    // with the provided element replaced

    /* update the grid container */
    // assemble to joint grid if possible
    GridContainer currentFullGrid =
        subGrids.size() == 1
            ? subGrids.values().iterator().next().getSubGridContainer()
            : ContainerUtils.combineToJointGrid(
                subGrids.values().stream()
                    .map(GridModel::getSubGridContainer)
                    .collect(Collectors.toList()));

    // map entity uuids to their gridModel uuids
    Map<UUID, Set<UUID>> gridModelsToEntities = new HashMap<>();
    subGrids
        .values()
        .forEach(
            gridModel ->
                gridModelsToEntities.put(
                    gridModel.getUuid(),
                    gridModel.getSubGridContainer().allEntitiesAsList().stream()
                        .map(UniqueEntity::getUuid)
                        .collect(Collectors.toSet())));

    // update the joint grid container
    Set<UpdateGridEvent> updateGridEvents = new HashSet<>();
    gridModification
        .updatedGridContainer(currentFullGrid)
        .ifPresent(
            updatedGridContainer -> {
              Map<UUID, GridModel> updatedSubGrids =
                  gridContainerToGridModel(updatedGridContainer, gridModelsToEntities);

              if (updatedSubGrids.isEmpty()) {
                log.warn(
                    "Received GridModificationEvent but no grid update has been performed! Event: {}",
                    gridModification);
              }

              updatedSubGrids.forEach(
                  (key, value) -> {
                    SubGridContainer oldSubGrid = this.subGrids.get(key).getSubGridContainer();
                    SubGridContainer updatedSubGrid = value.getSubGridContainer();
                    ModifiedSubGridData updatedGridData =
                        buildModifiedSubGridData(oldSubGrid, updatedSubGrid, key);
                    updateGridEvents.addAll(updatedGridData.getChangeSet());
                  });

              // map grid update event to their subgrid uuid
              Map<UUID, Set<UpdateGridEvent>> updatedGridElementsMapping =
                  updateGridEvents.stream()
                      .collect(
                          Collectors.groupingBy(
                              UpdateGridEvent::getSubGridUuid, Collectors.toSet()));

              // sanity check to prevent model inconsistency in listening models -> do not update
              // the grid model if no update grid events are issued for this grid
              updatedSubGrids.forEach(
                  (uuid, updatedGrid) -> {
                    if (updatedGridElementsMapping.containsKey(updatedGrid.getUuid())) {
                      this.subGrids.put(updatedGrid.getUuid(), updatedGrid);
                      log.debug(
                          "Updating element {} in subGrid {} (uuid: {}).",
                          gridModification.getOldValue().getUuid(),
                          updatedGrid.getSubGridContainer().getSubnet(),
                          updatedGrid.getUuid());
                    } else {
                      log.warn(
                          "Missing GridUpdateEvents for updated entities in subGrid {}. NO update of the grid model performed!",
                          updatedGrid.getUuid());
                    }
                  });
            });

    // notify listener
    updateGridEvents.forEach(this::notifyGridUpdateListener);
  }

  private void notifyGridUpdateListener(GridEvent gridUpdateEvent) {
    gridUpdateEventProperty.setValue(gridUpdateEvent);
  }

  private ModifiedSubGridData buildModifiedSubGridData(
      SubGridContainer oldSubGrid, SubGridContainer updatedSubGrid, UUID subGridUuid) {

    Set<UpdateGridEvent> updatedEntities =
        getGridUpdateEvents(updatedSubGrid, oldSubGrid.allEntitiesAsList(), subGridUuid);
    return new ModifiedSubGridData(updatedSubGrid, updatedEntities);
  }

  // todo JH javadoc an empty set is returned if at least one update event cannot be created to
  // avoid grid inconsitency later on
  private Set<UpdateGridEvent> getGridUpdateEvents(
      GridContainer updatedGrid, List<InputEntity> oldEntities, UUID subGridUuid) {
    return Optional.of(
            updatedGrid.allEntitiesAsList().stream()
                .filter(entity -> !oldEntities.contains(entity))
                .map(
                    entity -> {
                      if (entity instanceof NodeInput) {
                        return new GridUpdateNodeEvent((NodeInput) entity, subGridUuid);
                      } else if (entity instanceof LineInput) {
                        return new GridUpdateLineEvent((LineInput) entity, subGridUuid);
                      } else if (entity instanceof Transformer2WInput) {
                        return new GridUpdateTransformer2WEvent(
                            (Transformer2WInput) entity, subGridUuid);
                      } else if (entity instanceof PvInput
                          || entity instanceof LoadInput
                          || entity instanceof StorageInput) {
                        return new GridUpdateSystemParticipantEvent(
                            (SystemParticipantInput) entity, subGridUuid);
                      } else {
                        log.warn(
                            "Issuing grid update events for entity of type {} is currently NOT supported. No update events created!",
                            entity.getClass().getSimpleName());
                        return null;
                      }
                    })
                .collect(Collectors.toSet()))
        .map(resultingSet -> resultingSet.contains(null) ? null : resultingSet)
        .orElseGet(Collections::emptySet);
  }
}
