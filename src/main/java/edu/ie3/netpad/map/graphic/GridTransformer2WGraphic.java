/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.map.graphic;

import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.input.connector.Transformer2WInput;
import edu.ie3.datamodel.models.input.system.SystemParticipantInput;
import edu.ie3.netpad.grid.controller.EditGridContextController;
import edu.ie3.netpad.map.GridPaintLayer;
import edu.ie3.netpad.map.event.NodeGeoPositionUpdateEvent;
import java.util.List;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 23.05.20
 */
public class GridTransformer2WGraphic extends GridGraphicImpl<Rectangle> {

  // graphic properties
  private static final double TRAFO_RECT_WIDTH = 12.0;
  private static final double TRAFO_RECT_HEIGHT = 12.0;

  private final Transformer2WInput transformer2WInput;

  public GridTransformer2WGraphic(
      Transformer2WInput transformer2WInput,
      Set<SystemParticipantInput> systemParticipants,
      GridPaintLayer gridPaintLayer,
      List<ChangeListener<NodeGeoPositionUpdateEvent>> nodeGeoPositionUpdateEventListener) {
    super(
        gridPaintLayer,
        new Rectangle(),
        transformer2WInput.getNodeA(),
        systemParticipants,
        nodeGeoPositionUpdateEventListener,
        transformer2WInput.getId());
    this.transformer2WInput = transformer2WInput;

    // setup properties
    setProperties(gridPaintLayer);

    // set features
    setFeatures();
  }

  private void setProperties(GridPaintLayer gridPaintLayer) {
    shape.setWidth(TRAFO_RECT_WIDTH);
    shape.setHeight(TRAFO_RECT_HEIGHT);

    shape.setRotate(45);

    shape.setCursor(Cursor.HAND);

    shape.setVisible(true);

    this.setCoords(gridPaintLayer, transformer2WInput);
  }

  private void setFeatures() {
    shape.addEventHandler(
        MouseEvent.MOUSE_PRESSED,
        mousePressedEvent -> {
          if (mousePressedEvent.getButton().equals(MouseButton.SECONDARY)) {
            EditGridContextController.getInstance()
                .showTransformerContextMenu(
                    shape, transformer2WInput, systemParticipants, gridPaintLayer.getSubGridUuid());
          }
        });
  }

  private void setCoords(GridPaintLayer gridPaintLayer, Transformer2WInput transformer2WInput) {
    NodeInput nodeA = transformer2WInput.getNodeA();
    Point2D mapPoint =
        gridPaintLayer.getGridLayerPoint(
            nodeA.getGeoPosition().getY(), nodeA.getGeoPosition().getX());

    shape.setTranslateX(mapPoint.getX() - TRAFO_RECT_WIDTH / 2);
    shape.setTranslateY(mapPoint.getY() - TRAFO_RECT_WIDTH / 2);
  }

  public Transformer2WInput getTransformer2WInput() {
    return transformer2WInput;
  }

  @Override
  public Rectangle getGraphicShape() {
    return this.shape;
  }

  @Override
  public void update(GridPaintLayer gridPaintLayer) {

    this.setCoords(gridPaintLayer, this.transformer2WInput);

    shape.setVisible(true);
    shape.setManaged(false);
    shape.toFront();
  }
}
