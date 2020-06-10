/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.map.event;

import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.input.container.GridContainer;
import edu.ie3.datamodel.utils.ContainerNodeUpdateUtil;
import java.util.*;
import org.locationtech.jts.geom.Point;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 19.05.20
 */
public class NodeGeoPositionUpdateEvent implements MapEvent {

  private final NodeInput oldNode;
  private final UUID subGridUuid;

  private final NodeInput updatedNode;

  public NodeGeoPositionUpdateEvent(UUID subGridUuid, NodeInput oldNode, Point updatedGeoPosition) {
    this.oldNode = oldNode;
    this.subGridUuid = subGridUuid;

    this.updatedNode = oldNode.copy().geoPosition(updatedGeoPosition).build();
  }

  @Override
  public UUID getSubGridUuid() {
    return subGridUuid;
  }

  @Override
  public NodeInput getOldValue() {
    return oldNode;
  }

  @Override
  public Optional<GridContainer> updatedGridContainer(GridContainer grid) {
    // sanity check if the provided subGridContainer can be updated
    if (grid.getRawGrid().getNodes().contains(oldNode)) {
      return Optional.of(
          ContainerNodeUpdateUtil.updateGridWithNodes(
              grid, Collections.singletonMap(oldNode, updatedNode)));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public String toString() {
    return "MapUpdateNodeEvent{"
        + "oldNodeInput="
        + oldNode
        + ", newNodeInput="
        + updatedNode
        + ", graphicLayerUuid="
        + subGridUuid
        + '}';
  }
}
