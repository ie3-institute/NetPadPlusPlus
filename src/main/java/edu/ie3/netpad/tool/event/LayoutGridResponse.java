/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.tool.event;

import edu.ie3.datamodel.models.input.container.JointGridContainer;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 04.06.20
 */
public class LayoutGridResponse implements ToolEvent {

  private final JointGridContainer grid;

  public LayoutGridResponse(JointGridContainer grid) {
    this.grid = grid;
  }

  public JointGridContainer getGrid() {
    return grid;
  }
}
