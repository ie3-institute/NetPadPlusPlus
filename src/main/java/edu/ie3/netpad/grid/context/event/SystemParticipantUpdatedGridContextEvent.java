/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.context.event;

import edu.ie3.datamodel.models.input.container.GridContainer;
import edu.ie3.datamodel.models.input.system.SystemParticipantInput;
import edu.ie3.netpad.util.ContainerUpdateUtil;
import java.util.Optional;
import java.util.UUID;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 25.05.20
 */
public class SystemParticipantUpdatedGridContextEvent
    extends GridContextEventImpl<SystemParticipantInput> {

  private final SystemParticipantInput updatedSystemParticipant;

  public SystemParticipantUpdatedGridContextEvent(
      SystemParticipantInput oldSystemParticipant,
      SystemParticipantInput updatedSystemParticipant,
      UUID subGridUuid) {
    super(subGridUuid, oldSystemParticipant);
    this.updatedSystemParticipant = updatedSystemParticipant;
  }

  @Override
  public Optional<GridContainer> updatedGridContainer(GridContainer grid) {
    if (grid.getSystemParticipants().allEntitiesAsList().contains(oldAssetEntity)) {
      return Optional.of(
          ContainerUpdateUtil.updateGridWithSystemParticipant(
              grid, oldAssetEntity, updatedSystemParticipant));
    } else {
      return Optional.empty();
    }
  }
}
