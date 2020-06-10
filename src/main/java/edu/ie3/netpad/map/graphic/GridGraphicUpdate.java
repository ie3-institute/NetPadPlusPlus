/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.map.graphic;

import edu.ie3.netpad.map.event.NodeGeoPositionUpdateEvent;
import java.util.List;
import javafx.beans.value.ChangeListener;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 20.05.20
 */
public interface GridGraphicUpdate {

  GridGraphic updateGraphicEntity(
      GridGraphic oldGridGraphicEntity, List<ChangeListener<NodeGeoPositionUpdateEvent>> listener);
}
