/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.util;

import edu.ie3.datamodel.models.input.container.GridContainer;
import edu.ie3.datamodel.models.input.container.JointGridContainer;
import edu.ie3.datamodel.models.input.container.SubGridContainer;
import edu.ie3.datamodel.models.input.container.SystemParticipants;
import edu.ie3.datamodel.models.input.system.SystemParticipantInput;
import java.util.List;
import java.util.stream.Collectors;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 08.06.20
 */
public class ContainerUpdateUtil {

  public ContainerUpdateUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static GridContainer updateGridWithSystemParticipant(
      GridContainer grid,
      SystemParticipantInput oldAssetEntity,
      SystemParticipantInput updatedSystemParticipant) {

    List<SystemParticipantInput> updatedSystemParticipantEntities =
        grid.getSystemParticipants().allEntitiesAsList().stream()
            .filter(entity -> !entity.equals(oldAssetEntity))
            .collect(Collectors.toList());

    updatedSystemParticipantEntities.add(updatedSystemParticipant);

    if (grid instanceof JointGridContainer) {
      return new JointGridContainer(
          grid.getGridName(),
          grid.getRawGrid(),
          new SystemParticipants(updatedSystemParticipantEntities),
          grid.getGraphics());
    } else if (grid instanceof SubGridContainer) {
      return new SubGridContainer(
          grid.getGridName(),
          ((SubGridContainer) grid).getSubnet(),
          grid.getRawGrid(),
          new SystemParticipants(updatedSystemParticipantEntities),
          grid.getGraphics());
    } else {
      throw new RuntimeException("INvalid GridContainer"); // todo JH
    }
  }
}
