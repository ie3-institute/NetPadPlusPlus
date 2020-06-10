/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.io.event;

import edu.ie3.datamodel.models.input.container.GridContainer;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 19.05.20
 */
public class ReadGridEvent implements IOEvent {

  private final GridContainer gridContainer;

  public ReadGridEvent(GridContainer gridContainer) {
    this.gridContainer = gridContainer;
  }

  public GridContainer getGrid() {
    return gridContainer;
  }
}
