/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.map;

import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import edu.ie3.netpad.grid.event.GridEvent;
import edu.ie3.netpad.grid.event.GridEventListener;
import edu.ie3.netpad.grid.event.ReplaceGridEvent;
import edu.ie3.netpad.grid.event.UpdateGridEvent;
import edu.ie3.netpad.grid.info.GridInfoEvent;
import edu.ie3.netpad.map.event.MapEvent;
import edu.ie3.netpad.util.ListenerUtil;
import edu.ie3.netpad.util.RandomSingleton;
import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 19.05.20
 */
public class MapController implements GridEventListener {

  private static final Logger logger = LoggerFactory.getLogger(MapController.class);

  // Default zoom value and location
  private static final int ZOOM_DEFAULT = 6;
  private static final MapPoint FUERWEILER = new MapPoint(49.3765, 6.59384);

  private final ObjectProperty<MapEvent> papUpdateEventProperty = new SimpleObjectProperty<>();
  private final Map<UUID, GridPaintLayer> subGridGraphicLayer = new HashMap<>();

  private final ChangeListener<GridEvent> gridEventListener =
      ListenerUtil.createGridEventListener(this);

  @FXML private MapView map;

  @Override
  public ChangeListener<GridEvent> gridEventListener() {
    return gridEventListener;
  }

  @FXML
  public void initialize() {

    map.setCenter(FUERWEILER);
    map.setZoom(ZOOM_DEFAULT);

    // add information caption to the mapAnchorPane
    map.addLayer(new CaptionLayer());
  }

  @Override
  public void handleGridEvent(GridEvent gridEvent) {
    if (gridEvent instanceof UpdateGridEvent) {
      handleUpdateGridEvent((UpdateGridEvent) gridEvent);
    } else if (gridEvent instanceof ReplaceGridEvent) {
      handleReplaceGridEvent((ReplaceGridEvent) gridEvent);
    } else {
      throw new RuntimeException(
          "The provided GridEvent "
              + gridEvent.getClass().getSimpleName()
              + " is not supported by the MapController!");
    }
  }

  private void handleReplaceGridEvent(ReplaceGridEvent gridEvent) {

    // clear base map
    subGridGraphicLayer.values().forEach(map::removeLayer);

    // clear subGridGraphicLayer map
    subGridGraphicLayer.clear();

    // todo preserve grid layer order

    // create layer for each subGrid and add them to the subGridGraphicLayer map
    gridEvent
        .getSubGrids()
        .forEach(
            (subGridUuid, subGrid) -> {
              Set<Color> existingColors =
                  subGridGraphicLayer.values().stream()
                      .map(GridPaintLayer::getLayerColor)
                      .collect(Collectors.toSet());
              Color uniqueColor = getUniqueLayerColor(existingColors);

              GridPaintLayer gridPaintLayer =
                  new GridPaintLayer(subGridUuid, layerUpdateListener(), uniqueColor);

              // needs to be called before the layer can draw its grid because this makes the
              // baseMap accessible to the layer
              map.addLayer(gridPaintLayer);

              // add the grid to the layer
              gridPaintLayer.initGridGraphics(subGrid);

              subGridGraphicLayer.put(gridPaintLayer.getSubGridUuid(), gridPaintLayer);
            });

    // adapt map center and zoom for better user experience
    subGridGraphicLayer.keySet().stream()
        .findAny()
        .flatMap(
            randomUuid ->
                gridEvent.getSubGrids().get(randomUuid).getRawGrid().getNodes().stream().findAny())
        .ifPresent(
            randomNodeInGrid ->
                map.setCenter(
                    new MapPoint(
                        randomNodeInGrid.getGeoPosition().getY(),
                        randomNodeInGrid.getGeoPosition().getX())));
    map.setZoom(11);
  }

  private Color getUniqueLayerColor(Set<Color> existingColors) {
    Color uniqueLayerColor = existingColors.stream().findAny().orElseGet(this::randomColor);
    List<Color> reservedColors =
        Arrays.stream(MapGridElementAttribute.values())
            .map(MapGridElementAttribute::getColor)
            .collect(Collectors.toList());
    if (!existingColors.isEmpty()) {
      // uniqueness by checking if the color exist and if the color is not black, as black is
      // reserved for slack nodes
      while (existingColors.contains(uniqueLayerColor)
          || reservedColors.contains(uniqueLayerColor)) {
        uniqueLayerColor = randomColor();
      }
    }
    return uniqueLayerColor;
  }

  private void handleUpdateGridEvent(UpdateGridEvent updateGridEvent) {

    subGridGraphicLayer.get(updateGridEvent.getSubGridUuid()).updateGraphicEntity(updateGridEvent);

    logger.debug("Received GridEvent: {}", updateGridEvent);
  }

  private ChangeListener<MapEvent> layerUpdateListener() {
    return (observable, oldValue, newValue) -> notifyListener(newValue);
  }

  private void notifyListener(MapEvent newValue) {
    papUpdateEventProperty.set(newValue);
  }

  public ObjectProperty<MapEvent> mapUpdateEvents() {
    return papUpdateEventProperty;
  }

  protected Color randomColor() {

    double r = ((RandomSingleton.nextFloat() * RandomSingleton.nextDouble()) / 2f + 0.25f);
    double g = ((RandomSingleton.nextFloat() * RandomSingleton.nextDouble()) / 2f + 0.25f);
    double b = RandomSingleton.nextDouble() / 2d + 0.25d;

    return new Color(r, g, b, 1);
  }

  public ChangeListener<GridInfoEvent> gridInfoEventListener() {
    return (observable, oldValue, newValue) -> handleGridInfoEvent(newValue);
  }

  private void handleGridInfoEvent(GridInfoEvent gridInfoEvent) {

    GridPaintLayer layer = subGridGraphicLayer.get(gridInfoEvent.getSubGridUuid());
    if (gridInfoEvent.isSelected()) {
      map.addLayer(layer);
    } else {
      map.removeLayer(layer);
    }

    // refresh view
    layer.layoutLayer();
  }
}
