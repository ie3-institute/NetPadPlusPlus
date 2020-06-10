/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.map.graphic;

import edu.ie3.datamodel.models.input.connector.LineInput;
import edu.ie3.netpad.map.GridPaintLayer;
import edu.ie3.netpad.map.event.NodeGeoPositionUpdateEvent;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polyline;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 20.05.20
 */
public class GridLineGraphic extends GridGraphicImpl<Polyline> {

  // graphic properties
  private static final double LINE_STROKE_WIDTH = 2.0;
  private static final double HIGHLIGHTED_LINE_STROKE_WIDTH = 2.0;

  private final LineInput lineInput;

  public GridLineGraphic(
      LineInput lineInput,
      GridPaintLayer gridPaintLayer,
      List<ChangeListener<NodeGeoPositionUpdateEvent>> nodeGeoPositionUpdateEventListener) {
    super(gridPaintLayer, new Polyline(), nodeGeoPositionUpdateEventListener, lineInput.getId());

    this.lineInput = lineInput;

    // setup properties
    setProperties(gridPaintLayer);

    // setup features
    setFeatures();
  }

  private void setProperties(GridPaintLayer gridPaintLayer) {
    shape.setStrokeWidth(LINE_STROKE_WIDTH);
    shape.setStroke(gridPaintLayer.getLayerColor());

    shape.setVisible(true);

    setLinePoints(gridPaintLayer, this.lineInput);
  }

  private void setFeatures() {

    // highlight graphic when mouse enters
    shape.setOnMouseEntered(event -> shape.setStrokeWidth(HIGHLIGHTED_LINE_STROKE_WIDTH));
    shape.setOnMouseExited(event -> shape.setStrokeWidth(LINE_STROKE_WIDTH));
  }

  private void setLinePoints(GridPaintLayer gridPaintLayer, LineInput lineInput) {
    Arrays.stream(lineInput.getGeoPosition().getCoordinates())
        .forEach(
            coordinate -> {
              Point2D mapPoint = gridPaintLayer.getGridLayerPoint(coordinate.y, coordinate.x);
              shape.getPoints().add(mapPoint.getX());
              shape.getPoints().add(mapPoint.getY());
            });
  }

  @Override
  public Polyline getGraphicShape() {
    return this.shape;
  }

  @Override
  public void update(GridPaintLayer gridPaintLayer) {
    // clear points
    shape.getPoints().clear();

    // update relative map position
    this.setLinePoints(gridPaintLayer, this.lineInput);

    // set visible
    shape.setVisible(true);
    shape.toBack();
  }
}
