/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid;

import edu.ie3.datamodel.models.input.container.SubGridContainer;
import java.util.Set;
import java.util.UUID;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 23.05.20
 */
public class GridModel {

  private final UUID uuid;
  private final SubGridContainer subGridContainer;
  private final Set<UUID> inferiorGrids;
  private final Set<UUID> superiorGrids;

  public GridModel(
      UUID uuid,
      SubGridContainer subGridContainer,
      Set<UUID> superiorGrids,
      Set<UUID> inferiorGrids) {
    this.uuid = uuid;
    this.subGridContainer = subGridContainer;
    this.inferiorGrids = inferiorGrids;
    this.superiorGrids = superiorGrids;
  }

  public SubGridContainer getSubGridContainer() {
    return subGridContainer;
  }

  public UUID getUuid() {
    return uuid;
  }

  public Set<UUID> getInferiorGrids() {
    return inferiorGrids;
  }

  public Set<UUID> getSuperiorGrids() {
    return superiorGrids;
  }
}
