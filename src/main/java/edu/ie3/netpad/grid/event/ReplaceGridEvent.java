/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.event;

import edu.ie3.datamodel.models.input.container.SubGridContainer;
import java.util.Map;
import java.util.UUID;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 20.05.20
 */
public class ReplaceGridEvent implements GridEvent {

  private final String gridName;
  // todo order layer from evh -> lv and add them in the order that the last layer added is the ehv
  // layer
  private final Map<UUID, SubGridContainer> subGrids;

  public ReplaceGridEvent(String gridName, Map<UUID, SubGridContainer> subGrids) {
    this.gridName = gridName;
    this.subGrids = subGrids;
  }

  public Map<UUID, SubGridContainer> getSubGrids() {
    return subGrids;
  }

  public String getGridName() {
    return gridName;
  }
}
