/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.event;

import edu.ie3.datamodel.models.input.connector.Transformer2WInput;
import edu.ie3.netpad.map.event.NodeGeoPositionUpdateEvent;
import edu.ie3.netpad.map.graphic.GridGraphic;
import edu.ie3.netpad.map.graphic.GridGraphicUpdate;
import edu.ie3.netpad.map.graphic.GridTransformer2WGraphic;
import java.util.List;
import java.util.UUID;
import javafx.beans.value.ChangeListener;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 23.05.20
 */
public class GridUpdateTransformer2WEvent implements UpdateGridEvent, GridGraphicUpdate {

  private final Transformer2WInput transformer2WInput;
  private final UUID subGridUuid;

  public GridUpdateTransformer2WEvent(Transformer2WInput transformer2WInput, UUID subGridUuid) {
    this.transformer2WInput = transformer2WInput;
    this.subGridUuid = subGridUuid;
  }

  @Override
  public UUID getGridEntityUuid() {
    return transformer2WInput.getUuid();
  }

  @Override
  public UUID getSubGridUuid() {
    return subGridUuid;
  }

  @Override
  public GridGraphic updateGraphicEntity(
      GridGraphic oldGridGraphicEntity, List<ChangeListener<NodeGeoPositionUpdateEvent>> listener) {
    return new GridTransformer2WGraphic(
        this.transformer2WInput,
        oldGridGraphicEntity.getSystemParticipants(),
        oldGridGraphicEntity.getGridPaintLayer(),
        listener);
  }

  @Override
  public String toString() {
    return "GridUpdateTransformer2WEvent{"
        + "transformer2WInput="
        + transformer2WInput
        + ", subGridUuid="
        + subGridUuid
        + ", gridEntityUuid="
        + getGridEntityUuid()
        + ", graphicLayerUUID="
        + getSubGridUuid()
        + '}';
  }
}
