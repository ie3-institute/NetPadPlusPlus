/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.info;

import java.util.UUID;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 24.05.20
 */
public class GridInfoEvent {

  private final UUID subGridUuid;
  private final boolean isSelected;

  public GridInfoEvent(UUID subGridUuid, boolean isSelected) {
    this.subGridUuid = subGridUuid;
    this.isSelected = isSelected;
  }

  public UUID getSubGridUuid() {
    return subGridUuid;
  }

  public boolean isSelected() {
    return isSelected;
  }
}
