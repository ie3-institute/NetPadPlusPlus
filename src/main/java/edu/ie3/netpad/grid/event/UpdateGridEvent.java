/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.event;

import edu.ie3.netpad.map.graphic.GridGraphicUpdate;
import java.util.UUID;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 20.05.20
 */
public interface UpdateGridEvent extends GridEvent, GridGraphicUpdate {

  UUID getSubGridUuid();

  UUID getGridEntityUuid();
}
