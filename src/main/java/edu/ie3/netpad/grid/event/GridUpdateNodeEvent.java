/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.event;

import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.netpad.map.event.NodeGeoPositionUpdateEvent;
import edu.ie3.netpad.map.graphic.GridGraphic;
import edu.ie3.netpad.map.graphic.GridNodeGraphic;
import java.util.List;
import java.util.UUID;
import javafx.beans.value.ChangeListener;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 20.05.20
 */
public class GridUpdateNodeEvent implements UpdateGridEvent {

  private final NodeInput nodeInput;
  private final UUID subGridUuid;

  public GridUpdateNodeEvent(NodeInput nodeInput, UUID subGridUuid) {
    this.nodeInput = nodeInput;
    this.subGridUuid = subGridUuid;
  }

  @Override
  public UUID getGridEntityUuid() {
    return nodeInput.getUuid();
  }

  @Override
  public UUID getSubGridUuid() {
    return subGridUuid;
  }

  @Override
  public GridGraphic updateGraphicEntity(
      GridGraphic oldGridGraphicEntity, List<ChangeListener<NodeGeoPositionUpdateEvent>> listener) {
    return new GridNodeGraphic(
        this.nodeInput,
        oldGridGraphicEntity.getSystemParticipants(),
        oldGridGraphicEntity.getGridPaintLayer(),
        listener);
  }

  @Override
  public String toString() {
    return "GridUpdateNodeEvent{"
        + "nodeInput="
        + nodeInput
        + ", subGridUuid="
        + subGridUuid
        + ", gridEntityUuid="
        + getGridEntityUuid()
        + '}';
  }
}
