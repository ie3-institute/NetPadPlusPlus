/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.context.event;

import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.input.container.GridContainer;
import edu.ie3.datamodel.utils.ContainerNodeUpdateUtil;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 25.05.20
 */
public class NodeUpdatedGridContextEvent extends GridContextEventImpl<NodeInput> {

  private final NodeInput updatedNode;

  public NodeUpdatedGridContextEvent(NodeInput oldNode, NodeInput updatedNode, UUID subGridUuid) {
    super(subGridUuid, oldNode);
    this.updatedNode = updatedNode;
  }

  @Override
  public Optional<GridContainer> updatedGridContainer(GridContainer grid) {
    // sanity check if the provided subGridContainer can be updated
    if (grid.getRawGrid().getNodes().contains(oldAssetEntity)) {
      return Optional.of(
          ContainerNodeUpdateUtil.updateGridWithNodes(
              grid, Collections.singletonMap(oldAssetEntity, updatedNode)));
    } else {
      return Optional.empty();
    }
  }
}
