/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid;

import edu.ie3.datamodel.models.input.container.SubGridContainer;
import edu.ie3.netpad.grid.event.UpdateGridEvent;
import java.util.Set;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 21.05.20
 */
public class ModifiedSubGridData {

  private final SubGridContainer modifiedSubGrid;
  private final Set<UpdateGridEvent> changeSet;

  public ModifiedSubGridData(SubGridContainer modifiedSubGrid, Set<UpdateGridEvent> changeSet) {
    this.modifiedSubGrid = modifiedSubGrid;
    this.changeSet = changeSet;
  }

  public SubGridContainer getModifiedSubGrid() {
    return modifiedSubGrid;
  }

  public Set<UpdateGridEvent> getChangeSet() {
    return changeSet;
  }
}
