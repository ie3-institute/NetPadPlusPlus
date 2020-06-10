/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.map.graphic;

import edu.ie3.datamodel.models.input.system.SystemParticipantInput;
import edu.ie3.netpad.map.GridPaintLayer;
import java.util.Set;
import javafx.scene.shape.Shape;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 26.05.20
 */
public interface GridGraphic {

  Shape getGraphicShape();

  void update(GridPaintLayer gridPaintLayer);

  GridPaintLayer getGridPaintLayer();

  Set<SystemParticipantInput> getSystemParticipants();
}
