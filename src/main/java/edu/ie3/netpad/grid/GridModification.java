/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid;

import edu.ie3.datamodel.models.UniqueEntity;
import edu.ie3.datamodel.models.input.container.GridContainer;
import java.util.Optional;
import java.util.UUID;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 20.05.20
 */
public interface GridModification {

  UUID getSubGridUuid();

  UniqueEntity getOldValue();

  Optional<GridContainer> updatedGridContainer(GridContainer grid);
}
