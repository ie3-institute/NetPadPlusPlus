/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.event;

import edu.ie3.datamodel.models.input.system.SystemParticipantInput;
import edu.ie3.netpad.map.event.NodeGeoPositionUpdateEvent;
import edu.ie3.netpad.map.graphic.GridGraphic;
import edu.ie3.netpad.map.graphic.GridNodeGraphic;
import edu.ie3.netpad.map.graphic.GridTransformer2WGraphic;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 03.06.20
 */
public class GridUpdateSystemParticipantEvent implements UpdateGridEvent {

  private static final Logger log = LoggerFactory.getLogger(GridUpdateSystemParticipantEvent.class);

  private final SystemParticipantInput systemParticipantInput;
  private final UUID subGridUuid;

  public GridUpdateSystemParticipantEvent(
      SystemParticipantInput systemParticipantInput, UUID subGridUuid) {
    this.systemParticipantInput = systemParticipantInput;
    this.subGridUuid = subGridUuid;
  }

  @Override
  public UUID getSubGridUuid() {
    return subGridUuid;
  }

  @Override
  public UUID getGridEntityUuid() {
    return systemParticipantInput.getUuid();
  }

  @Override
  public GridGraphic updateGraphicEntity(
      GridGraphic oldGridGraphicEntity, List<ChangeListener<NodeGeoPositionUpdateEvent>> listener) {
    if (oldGridGraphicEntity instanceof GridNodeGraphic) {

      return new GridNodeGraphic(
          ((GridNodeGraphic) oldGridGraphicEntity).getNodeInput(),
          updateSystemParticipants(oldGridGraphicEntity.getSystemParticipants()),
          oldGridGraphicEntity.getGridPaintLayer(),
          listener);
    } else if (oldGridGraphicEntity instanceof GridTransformer2WGraphic) {

      return new GridTransformer2WGraphic(
          ((GridTransformer2WGraphic) oldGridGraphicEntity).getTransformer2WInput(),
          updateSystemParticipants(oldGridGraphicEntity.getSystemParticipants()),
          oldGridGraphicEntity.getGridPaintLayer(),
          listener);

    } else {
      log.error(
          "Invalid GridGraphic entity provided '{}' into GridUpdateSystemParticipantEvent!",
          oldGridGraphicEntity.getClass().getSimpleName());
      return oldGridGraphicEntity;
    }
  }

  private Set<SystemParticipantInput> updateSystemParticipants(
      Set<SystemParticipantInput> oldSysParts) {
    Set<SystemParticipantInput> updatedSysParts =
        oldSysParts.stream()
            .filter(oldSysPart -> !oldSysPart.getUuid().equals(systemParticipantInput.getUuid()))
            .collect(Collectors.toSet());

    updatedSysParts.add(systemParticipantInput);
    return updatedSysParts;
  }
}
