/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.map.graphic;

import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.input.system.SystemParticipantInput;
import edu.ie3.netpad.grid.controller.EditGridContextController;
import edu.ie3.netpad.map.GridPaintLayer;
import edu.ie3.netpad.map.event.NodeGeoPositionUpdateEvent;
import java.util.*;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 19.05.20
 */
public class GridNodeGraphic extends GridGraphicImpl<Circle> {

  // graphic properties
  private static final double NODE_RADIUS = 6.0;
  private static final double HIGHLIGHTED_NODE_RADIUS = 7.5;

  private final NodeInput nodeInput;

  public GridNodeGraphic(
      NodeInput nodeInput,
      Set<SystemParticipantInput> systemParticipants,
      GridPaintLayer gridPaintLayer,
      List<ChangeListener<NodeGeoPositionUpdateEvent>> nodeGeoPositionUpdateEventListener) {
    super(
        gridPaintLayer,
        new Circle(),
        nodeInput,
        systemParticipants,
        nodeGeoPositionUpdateEventListener,
        nodeInput.getId());
    this.nodeInput = nodeInput;

    // setup properties
    setProperties();

    // setup features
    setFeatures();
  }

  private void setProperties() {

    setNodeRadius();

    shape.setCursor(Cursor.HAND);

    shape.setVisible(true);

    setNodeToBeUpdatedCoords(this.nodeInput);
  }

  private void setFeatures() {

    shape.addEventHandler(
        MouseEvent.MOUSE_PRESSED,
        mousePressedEvent -> {
          if (mousePressedEvent.getButton().equals(MouseButton.SECONDARY)) {
            EditGridContextController.getInstance()
                .showNodeContextMenu(
                    shape, nodeInput, systemParticipants, gridPaintLayer.getSubGridUuid());
          }
        });

    // highlight graphic when mouse enters
    shape.setOnMouseEntered(
        event -> {
          if (nodeInput.isSlack()) {
            shape.setRadius(HIGHLIGHTED_NODE_RADIUS - SLACK_STROKE_WIDTH / 2);
          } else {
            shape.setRadius(HIGHLIGHTED_NODE_RADIUS);
          }
        });
    shape.setOnMouseExited(event -> setNodeRadius());
  }

  private void setNodeRadius() {
    if (nodeInput.isSlack()) {
      shape.setRadius(NODE_RADIUS - SLACK_STROKE_WIDTH / 2);
    } else {
      shape.setRadius(NODE_RADIUS);
    }
  }

  public NodeInput getNodeInput() {
    return nodeInput;
  }

  @Override
  public Circle getGraphicShape() {
    return this.shape;
  }

  @Override
  public void update(GridPaintLayer gridPaintLayer) {

    setNodeToBeUpdatedCoords(this.nodeInput);

    shape.setVisible(true);
    shape.setManaged(false);
    shape.toFront();
  }
}
