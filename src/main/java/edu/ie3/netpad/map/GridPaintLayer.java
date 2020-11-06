/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import edu.ie3.datamodel.models.input.AssetInput;
import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.input.connector.LineInput;
import edu.ie3.datamodel.models.input.connector.Transformer2WInput;
import edu.ie3.datamodel.models.input.container.RawGridElements;
import edu.ie3.datamodel.models.input.container.SubGridContainer;
import edu.ie3.datamodel.models.input.container.SystemParticipants;
import edu.ie3.datamodel.models.input.system.SystemParticipantInput;
import edu.ie3.netpad.exception.GridPaintLayerException;
import edu.ie3.netpad.grid.event.UpdateGridEvent;
import edu.ie3.netpad.map.event.MapEvent;
import edu.ie3.netpad.map.event.NodeGeoPositionUpdateEvent;
import edu.ie3.netpad.map.graphic.*;
import java.util.*;
import java.util.stream.Stream;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 15.05.20
 */
public class GridPaintLayer extends MapLayer {

  private static final Logger log = LoggerFactory.getLogger(GridPaintLayer.class);

  private final ObjectProperty<MapEvent> gridMapUpdateEventProperty = new SimpleObjectProperty<>();

  private final Map<UUID, GridGraphic> paintedElements = new HashMap<>();
  private final Map<UUID, UUID> entityUuidToPaintedUuid = new HashMap<>();
  private final Set<UUID> unpaintedNodes = new HashSet<>();

  private final UUID subGridUuid;
  private final Color layerColor;
  private final ChangeListener<NodeGeoPositionUpdateEvent> mapEntityListener;

  public GridPaintLayer(
      UUID subGridUuid,
      ChangeListener<MapEvent> updateGridMapEventChangeListener,
      Color layerColor) {
    super();
    this.subGridUuid = subGridUuid;

    // one subGrid per layer, one color per layer/subGrid
    this.layerColor = layerColor;

    // create the listener that receives updates from the entities painted on this layer (e.g.
    // lines, nodes, ...)
    mapEntityListener = createMapEventListener();

    // register the provided listener to receive updates from this layer
    this.gridMapUpdateEventProperty.addListener(updateGridMapEventChangeListener);

    // important: we don't want this layer to be managed by the parent
    this.setManaged(false);
  }

  // todo JH javadoc this is needed due to restrictions on the baseMap which is null at the
  // beginning and
  //  becomes set after the fully initialization of the layer
  protected void initGridGraphics(SubGridContainer grid) {

    RawGridElements rawGrid = grid.getRawGrid();
    SystemParticipants sysParts = grid.getSystemParticipants();

    Map<NodeInput, Set<SystemParticipantInput>> nodeToSysPart =
        sysParts.allEntitiesAsList().stream()
            .collect(groupingBy(SystemParticipantInput::getNode, toSet()));

    // we do not paint transformer nodes as they are represented by transformer graphics itself
    rawGrid.getTransformer2Ws().stream()
        .flatMap(trafo -> Stream.of(trafo.getNodeA(), trafo.getNodeB()))
        .forEach(node -> unpaintedNodes.add(node.getUuid()));

    rawGrid.getNodes().stream()
        .filter(node -> !unpaintedNodes.contains(node.getUuid()))
        .forEach(
            node -> this.addNode(node, nodeToSysPart.getOrDefault(node, Collections.emptySet())));

    rawGrid.getLines().forEach(this::addLine);

    rawGrid
        .getTransformer2Ws()
        .forEach(
            trafo2w ->
                this.addTransformer2W(
                    trafo2w,
                    nodeToSysPart.getOrDefault(trafo2w.getNodeB(), Collections.emptySet())));
  }

  @Override
  protected void layoutLayer() {
    for (GridGraphic gridGraphic : paintedElements.values()) {
      gridGraphic.update(this);
    }
  }

  // todo JH javadocs 1) should only called once during init, 2) should contain all actions that are
  // performed by entity update on this layer
  private ChangeListener<NodeGeoPositionUpdateEvent> createMapEventListener() {
    return (observable, oldValue, mapEvent) -> {

      // if an entity on this layer is altered, we inform all listener that listen to updates from
      // this layer
      notifyListener(mapEvent);

      log.debug(
          "GridPaintLayer element received node position update from painted element: {}",
          mapEvent);
    };
  }

  private void notifyListener(MapEvent newValue) {
    gridMapUpdateEventProperty.set(newValue);
  }

  // todo JH javadoc -> sysparts @this node NO sanity check!
  private void addNode(NodeInput node, Set<SystemParticipantInput> systemParticipants) {
    if (node.getGeoPosition() != null) {
      GridNodeGraphic gridNodeGraphic =
          new GridNodeGraphic(
              node, systemParticipants, this, Collections.singletonList(mapEntityListener));

      paintElement(node.getUuid(), gridNodeGraphic);

      log.trace("Added node {}", node.getId());
    } else {
      throw gridPaintLayerException(node);
    }
  }

  private void addLine(LineInput line) {

    if (line.getGeoPosition() != null) {
      GridLineGraphic lineGraphic =
          new GridLineGraphic(line, this, Collections.singletonList(mapEntityListener));

      paintElement(line.getUuid(), lineGraphic);

      log.trace("Added line {}", line.getId());
    } else {
      throw gridPaintLayerException(line);
    }
  }

  private void addTransformer2W(
      Transformer2WInput transformer2WInput, Set<SystemParticipantInput> systemParticipants) {
    if (transformer2WInput.getNodeA().getGeoPosition() != null
        && transformer2WInput.getNodeB().getGeoPosition() != null) {

      GridTransformer2WGraphic trafo2WGraphic =
          new GridTransformer2WGraphic(
              transformer2WInput,
              systemParticipants,
              this,
              Collections.singletonList(mapEntityListener));

      paintElement(transformer2WInput.getUuid(), trafo2WGraphic);

      log.trace("Added transformer {}", transformer2WInput.getUuid());

    } else {
      throw gridPaintLayerException(transformer2WInput);
    }
  }

  private GridPaintLayerException gridPaintLayerException(AssetInput assetInput) {
    return new GridPaintLayerException(
        "Cannot paint graphic for "
            + assetInput.getClass().getSimpleName()
            + "(id: "
            + assetInput.getId()
            + ", uuid: "
            + assetInput.getUuid()
            + " with missing geo information in one ore more nodes!");
  }

  public void updateGraphicEntity(UpdateGridEvent updateGridEvent) {

    // we do not update nodes that are hold by transformers, as they
    // are represented by their specific shape
    if (!unpaintedNodes.contains(updateGridEvent.getGridEntityUuid())) {

      Optional.ofNullable(entityUuidToPaintedUuid.get(updateGridEvent.getGridEntityUuid()))
          .ifPresentOrElse(
              paintedElementUuid -> {
                GridGraphic paintedGridGraphic = paintedElements.get(paintedElementUuid);

                // remove element from painted elements
                removeElement(paintedElementUuid);

                // add an updated version of the previously removed element
                paintElement(
                    paintedElementUuid,
                    updateGridEvent.updateGraphicEntity(
                        paintedGridGraphic, Collections.singletonList(mapEntityListener)));
              },
              () ->
                  log.warn(
                      "Received an update for an element that is not inside painted elements: {}",
                      updateGridEvent));
    } else {
      log.warn("Received update for a node that is not drawn: {}", updateGridEvent);
    }
  }

  private void paintElement(UUID uuid, GridGraphic graphicEntity) {

    paintedElements.put(uuid, graphicEntity);

    // update the entityUuidToPaintedUuid map
    entityUuidToPaintedUuid.put(uuid, uuid); // each graphic shape is mapped to itself
    // each sys part is mapped to its graphic entity uuid
    graphicEntity
        .getSystemParticipants()
        .forEach(sysPart -> entityUuidToPaintedUuid.put(sysPart.getUuid(), uuid));

    // important: we don't want the shapes to be managed by it's parent
    graphicEntity.getGraphicShape().setManaged(false);
    graphicEntity.getGraphicShape().setVisible(true);

    this.getChildren().add(graphicEntity.getGraphicShape());

    this.markDirty();
  }

  private void removeElement(UUID uuid) {

    GridGraphic gridGraphic = paintedElements.get(uuid);
    Shape shapeEntity = gridGraphic.getGraphicShape();

    // remove actual graphic entity from uuid to painted mapping
    entityUuidToPaintedUuid.remove(uuid);

    // remove system participants if any
    gridGraphic
        .getSystemParticipants()
        .forEach(sysPart -> entityUuidToPaintedUuid.remove(sysPart.getUuid()));

    paintedElements.remove(uuid);

    this.getChildren().remove(shapeEntity);
    this.markDirty();
  }

  public Point2D getGridLayerPoint(double lat, double lon) {
    return baseMap.getMapPoint(lat, lon);
  }

  public MapPoint getMapPosition(double sceneX, double sceneY) {
    return baseMap.getMapPosition(sceneX, sceneY);
  }

  public UUID getSubGridUuid() {
    return subGridUuid;
  }

  public Color getLayerColor() {
    return layerColor;
  }
}
