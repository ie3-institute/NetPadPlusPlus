/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.event;

import edu.ie3.datamodel.models.input.connector.LineInput;
import edu.ie3.netpad.map.event.NodeGeoPositionUpdateEvent;
import edu.ie3.netpad.map.graphic.GridGraphic;
import edu.ie3.netpad.map.graphic.GridLineGraphic;
import java.util.List;
import java.util.UUID;
import javafx.beans.value.ChangeListener;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 20.05.20
 */
public class GridUpdateLineEvent implements UpdateGridEvent {

  private final LineInput lineInput;
  private final UUID subGridUuid;

  public GridUpdateLineEvent(LineInput lineInput, UUID subGridUuid) {
    this.lineInput = lineInput;
    this.subGridUuid = subGridUuid;
  }

  @Override
  public UUID getGridEntityUuid() {
    return lineInput.getUuid();
  }

  @Override
  public UUID getSubGridUuid() {
    return subGridUuid;
  }

  @Override
  public GridGraphic updateGraphicEntity(
      GridGraphic oldGridGraphicEntity, List<ChangeListener<NodeGeoPositionUpdateEvent>> listener) {
    return new GridLineGraphic(this.lineInput, oldGridGraphicEntity.getGridPaintLayer(), listener);
  }

  @Override
  public String toString() {
    return "GridUpdateLineEvent{"
        + "lineInput="
        + lineInput
        + ", subGridUuid="
        + subGridUuid
        + ", gridEntityUuid="
        + getGridEntityUuid()
        + '}';
  }
}
