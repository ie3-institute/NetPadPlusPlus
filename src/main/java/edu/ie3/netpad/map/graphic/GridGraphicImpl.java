/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.map.graphic;

import static java.util.stream.Collectors.*;

import com.gluonhq.maps.MapPoint;
import edu.ie3.datamodel.models.UniqueEntity;
import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.input.system.SystemParticipantInput;
import edu.ie3.netpad.map.GridPaintLayer;
import edu.ie3.netpad.map.MapGridElementAttribute;
import edu.ie3.netpad.map.event.NodeGeoPositionUpdateEvent;
import edu.ie3.util.geo.GeoUtils;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Shape;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 20.05.20
 */
public abstract class GridGraphicImpl<T extends Shape> implements GridGraphic {

  private static final double DRAG_OPACITY = 0.75;

  protected static final Color SLACK_COLOR = MapGridElementAttribute.SLACK.getColor();
  protected static final double SLACK_STROKE_WIDTH = 2.0;

  private final ObjectProperty<NodeGeoPositionUpdateEvent> nodeGeoPositionUpdateEvent =
      new SimpleObjectProperty<>();
  protected final GridPaintLayer gridPaintLayer;

  protected final Set<SystemParticipantInput> systemParticipants;

  protected final T shape;

  public GridGraphicImpl(
      GridPaintLayer gridPaintLayer,
      T shape,
      NodeInput draggableNode,
      Set<SystemParticipantInput> systemParticipants,
      List<ChangeListener<NodeGeoPositionUpdateEvent>> nodeGeoPositionUpdateEventListener,
      String tooltipTxt) {

    this.systemParticipants = systemParticipants;
    this.shape = shape;
    this.gridPaintLayer = gridPaintLayer;

    nodeGeoPositionUpdateEventListener.forEach(nodeGeoPositionUpdateEvent::addListener);

    draggableNode(draggableNode);

    setShapeColor(gridPaintLayer.getLayerColor(), systemParticipants, draggableNode);

    installTooltip(shape, tooltipTxt);
  }

  public GridGraphicImpl(
      GridPaintLayer gridPaintLayer,
      T shape,
      List<ChangeListener<NodeGeoPositionUpdateEvent>> nodeGeoPositionUpdateEventListener,
      String tooltipTxt) {

    this.systemParticipants = Collections.emptySet();
    this.shape = shape;
    this.gridPaintLayer = gridPaintLayer;

    nodeGeoPositionUpdateEventListener.forEach(nodeGeoPositionUpdateEvent::addListener);

    installTooltip(shape, tooltipTxt);
  }

  @Override
  public GridPaintLayer getGridPaintLayer() {
    return this.gridPaintLayer;
  }

  @Override
  public Set<SystemParticipantInput> getSystemParticipants() {
    return this.systemParticipants;
  }

  protected void notifyNodeGeoPosListener(
      UUID subGridUuid, NodeInput oldNodeInput, Point updatedGeoPosition) {
    nodeGeoPositionUpdateEvent.set(
        new NodeGeoPositionUpdateEvent(subGridUuid, oldNodeInput, updatedGeoPosition));
  }

  protected void installTooltip(Shape shape, String txt) {
    Tooltip.install(shape, new Tooltip(txt));
  }

  private void setShapeColor(
      Color layerColor, Set<SystemParticipantInput> systemParticipants, NodeInput nodeToBeUpdated) {

    Map<? extends Class<? extends UniqueEntity>, List<MapGridElementAttribute>> gridEntityColor =
        Arrays.stream(MapGridElementAttribute.values())
            .collect(groupingBy(MapGridElementAttribute::getClz, toList()));

    Set<Stop> stops = new LinkedHashSet<>();
    Set<Color> assetColors =
        systemParticipants.stream()
            .map(
                sysPart ->
                    Optional.ofNullable(gridEntityColor.get(sysPart.getClass()))
                        .orElseThrow(
                            () ->
                                new RuntimeException(
                                    "No map grid attributes defined for element of class '"
                                        + sysPart.getClass().getSimpleName()
                                        + "'."))
                        .get(0)
                        .getColor())
            .collect(Collectors.toSet());

    int counter = 0;
    Iterator<Color> it = assetColors.iterator();
    while (it.hasNext()) {
      Color c = it.next();
      Stop s1 = new Stop((1d / assetColors.size() * counter), c);
      Stop s2 = new Stop((1d / assetColors.size() * (counter + 1)), c);
      stops.add(s1);
      stops.add(s2);
      counter++;
    }

    LinearGradient shapeFill =
        new LinearGradient(
            0,
            0,
            1,
            0,
            true,
            CycleMethod.NO_CYCLE,
            stops.isEmpty()
                ? Collections.singletonList(new Stop(0, layerColor))
                : new ArrayList<>(stops));

    shape.setFill(shapeFill);

    //    slack node also gets a stroke
    if (nodeToBeUpdated.isSlack()) {
      shape.setStrokeWidth(SLACK_STROKE_WIDTH);
      shape.setStroke(SLACK_COLOR);
    }
  }

  protected void draggableNode(NodeInput nodeToBeUpdated) {

    AtomicReference<Double> orgSceneX = new AtomicReference<>(0d);
    AtomicReference<Double> orgSceneY = new AtomicReference<>(0d);
    AtomicReference<Double> translateXClickedDelta = new AtomicReference<>(0d);

    shape.addEventHandler(
        MouseEvent.MOUSE_PRESSED,
        mousePressedEvent -> {
          if (mousePressedEvent.getButton().equals(MouseButton.PRIMARY)) {
            orgSceneX.set(mousePressedEvent.getSceneX());
            orgSceneY.set(mousePressedEvent.getSceneY());

            translateXClickedDelta.set(shape.getTranslateX() - orgSceneX.get());
          }
        });

    shape.addEventHandler(
        MouseEvent.MOUSE_DRAGGED,
        mouseDraggedEvent -> {
          if (mouseDraggedEvent.getButton().equals(MouseButton.PRIMARY)) {

            shape.setOpacity(DRAG_OPACITY);
            shape.toFront();

            // correction of drag event offset to keep node directly under the cursor
            double deltaX =
                mouseDraggedEvent.getSceneX() - orgSceneX.get() + translateXClickedDelta.get();
            double deltaY = mouseDraggedEvent.getSceneY() - orgSceneY.get() - 55;

            shape.setTranslateX(mouseDraggedEvent.getSceneX() + deltaX);
            shape.setTranslateY(mouseDraggedEvent.getSceneY() + deltaY);

            orgSceneX.set(mouseDraggedEvent.getSceneX());
            orgSceneY.set(mouseDraggedEvent.getSceneY());

            mouseDraggedEvent.consume();
          }
        });

    shape.addEventHandler(
        MouseEvent.MOUSE_RELEASED,
        mouseReleasedEvent -> {
          if (mouseReleasedEvent.getButton().equals(MouseButton.PRIMARY)) {

            // refresh corresponding map point and inform listener
            MapPoint updatedPoint =
                gridPaintLayer.getMapPosition(shape.getTranslateX(), shape.getTranslateY());

            // we don't want this node to stay on the new place, but wait for the grid update event
            // --> reset to starting position
            setNodeToBeUpdatedCoords(nodeToBeUpdated);

            // reset opacity after dragging
            shape.setOpacity(1.0);

            notifyNodeGeoPosListener(
                gridPaintLayer.getSubGridUuid(), nodeToBeUpdated, updateNodeGeoPos(updatedPoint));
            mouseReleasedEvent.consume();
          }
        });
  }

  protected void setNodeToBeUpdatedCoords(NodeInput nodeToBeUpdated) {
    Point2D mapPoint =
        gridPaintLayer.getGridLayerPoint(
            nodeToBeUpdated.getGeoPosition().getY(), nodeToBeUpdated.getGeoPosition().getX());

    shape.setTranslateX(mapPoint.getX());
    shape.setTranslateY(mapPoint.getY());
  }

  private Point updateNodeGeoPos(MapPoint updatedPoint) {
    Coordinate[] coord =
        new Coordinate[] {new Coordinate(updatedPoint.getLongitude(), updatedPoint.getLatitude())};
    CoordinateArraySequence coordSeq = new CoordinateArraySequence(coord);

    return new Point(coordSeq, GeoUtils.DEFAULT_GEOMETRY_FACTORY);
  }
}
