/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.tool.layout;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxICell;
import edu.ie3.datamodel.graph.DistanceWeightedEdge;
import edu.ie3.datamodel.graph.DistanceWeightedGraph;
import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.input.container.JointGridContainer;
import edu.ie3.datamodel.models.input.container.RawGridElements;
import edu.ie3.datamodel.utils.ContainerNodeUpdateUtil;
import edu.ie3.datamodel.utils.ContainerUtils;
import edu.ie3.util.geo.GeoUtils;
import java.awt.Point;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.morbz.osmonaut.osm.LatLon;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.ext.JGraphXAdapter;

import static java.awt.geom.Point2D.distance;

/**
 * The class lays out nodes in a {@link edu.ie3.datamodel.models.input.container.GridContainer}
 * using a hierarchical layout algorithm.
 */
public class GridLayouter {

  private static final double EARTH_RADIUS = 6371000d; // metres
  private static final LatLon REFERENCE_POINT_GEO = new LatLon(51.420620, 7.360411);

  private final JGraphXAdapter<NodeInput, DistanceWeightedEdge> graphAdapter;
  private final HierarchicalLayout hierarchicalLayout;

  private final JointGridContainer jointGridContainer;
  private final DistanceWeightedGraph graph;

  public GridLayouter(JointGridContainer jointGridContainer) {
    this.jointGridContainer = jointGridContainer;
    this.graph =
        ContainerUtils.getDistanceTopologyGraph(jointGridContainer)
            .orElseThrow(() -> new RuntimeException("TOASd")); // todo JH
    graph.edgeSet().forEach(edge -> graph.setEdgeWeight(edge, 100));
    graphAdapter = new JGraphXAdapter<>(graph);
    hierarchicalLayout = new HierarchicalLayout(graphAdapter);
  }

  public JointGridContainer execute() {
    // Execute the layout algorithm.
    hierarchicalLayout.execute(graphAdapter.getDefaultParent());

    JGraphXAdapter<NodeInput, DistanceWeightedEdge> layoutGraph =
        (JGraphXAdapter<NodeInput, DistanceWeightedEdge>) hierarchicalLayout.getGraph();

    // Create a mapping of Points (from the layout algorithm result) to NodeInputModels.
    Map<Point, NodeInput> pointsToNodes = createNodeMappings(layoutGraph);

    // Calculate a scale factor and scale the coordinates, so that the resulting "area" of nodes is
    // square.
    double scaleFactor = calcScaleFactor(pointsToNodes.keySet());
    Map<Point, NodeInput> scaledPointsToNodes = scalePoints(pointsToNodes, scaleFactor);

    // Set lowest point (smallest y-coordinate) as the reference point.
    Point referencePoint = this.getLowestPoint(scaledPointsToNodes.keySet());

    // Calculate the scale factor for the distances to reproduce the real distance proportions.
    double scaleFactorDistance =
        calcScaleFactorDistance(
            scaledPointsToNodes, referencePoint, this.graph, this.jointGridContainer.getRawGrid());

    // Set geo coordinates for reference point.
    NodeInput referenceNode = scaledPointsToNodes.get(referencePoint);
    NodeInput updatedReferencedNodeInput =
        referenceNode.copy().geoPosition(GeoUtils.latlonToPoint(REFERENCE_POINT_GEO)).build();
    scaledPointsToNodes.put(referencePoint, updatedReferencedNodeInput);

    // Calculate the new geo coordinates and set the calculated values in the point to node map.
    List<NodeInput> updatedScaledPointsToNodes =
        scaledPointsToNodes.entrySet().stream()
            .map(
                entry -> {
                  Point point = entry.getKey();

                  // Calculate the distance and the bearing between point and reference point and
                  // calculate the geo position.
                  double distance =
                      distance(
                              point.getX(),
                              point.getY(),
                              referencePoint.getX(),
                              referencePoint.getY())
                          / scaleFactorDistance;
                  double bearing = calcBearing(point, referencePoint);
                  LatLon geoPosition = calcGeoPosition(distance, bearing);

                  // Set the new geo position in the corresponding NodeInputModel.
                  NodeInput oldNode = entry.getValue();
                  NodeInput updatedNode =
                      oldNode.copy().geoPosition(GeoUtils.latlonToPoint(geoPosition)).build();
                  return updatedNode;
                  //
                  // entry.getValue().setGeoPosition(Utils.latlonToPoint(geoPosition));
                })
            .collect(Collectors.toList());

    // Update geo information in NodeInputModels from GridInputModel.
    //        gridInputModel.getNodes().forEach(node -> {
    //            com.vividsolutions.jts.geom.Point geoPosition =
    //                            scaledPointsToNodes.values().stream().filter(n ->
    // n.getTID().equals(node.getTID()))
    //                                            .findAny().orElseThrow().getGeoPosition();
    //            node.setGeoPosition(geoPosition);
    //        });

    Map<NodeInput, NodeInput> oldToNewNodes =
        updatedScaledPointsToNodes.stream()
            .map(
                newNode -> {
                  NodeInput oldNode =
                      jointGridContainer.getRawGrid().getNodes().stream()
                          .filter(node -> node.getUuid().equals(newNode.getUuid()))
                          .findFirst()
                          .orElseThrow();
                  return new AbstractMap.SimpleEntry<>(oldNode, newNode);
                })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    //    NodeInput oldNode =
    //        jointGridContainer.getRawGrid().getNodes().stream()
    //            .filter(node -> node.getUuid().equals(newNode.getUuid()))
    //            .findFirst()
    //            .orElseThrow();

    return ContainerNodeUpdateUtil.updateGridWithNodes(
        jointGridContainer, oldToNewNodes); // todo JH

    //    return null;

    // Update geo information in LineInputModels from GridInputModel.
    //        gridInputModel.getLines().forEach(line ->
    // line.setGeoPosition(Utils.generateLineString(line)));
  }

  /**
   * Creates a mapping {@link Point} to corresponding {@link NodeInput} from the result layout. The
   * points contain the coordinates determined by the layout algorithm. Each point maps to its
   * corresponding NodeInputModel.
   *
   * @param layoutGraph The {@link mxHierarchicalLayout} containing the layout algorithm results.
   * @return The created mapping.
   */
  private static Map<Point, NodeInput> createNodeMappings(
      JGraphXAdapter<NodeInput, DistanceWeightedEdge> layoutGraph) {
    Map<Point, NodeInput> pointsToNodes = new HashMap<>();

    for (Entry<NodeInput, mxICell> entry : layoutGraph.getVertexToCellMap().entrySet()) {
      Point point =
          new Point(
              (int) entry.getValue().getGeometry().getX(),
              (int) entry.getValue().getGeometry().getY());

      //            NodeInput nodeInput = DeepCopy.copy(entry.getKey());

      pointsToNodes.put(point, entry.getKey());
    }

    return pointsToNodes;
  }

  /**
   * Calculates the scale factor for the y-axis. All points y-coordinates have to be multiplied by
   * the calculated value later to get a square point set.
   *
   * @param pointCollection The point set that needs to be scaled.
   * @return The scale factor.
   */
  private static double calcScaleFactor(Collection<Point> pointCollection) {
    // Copy points from Map to separate list
    LinkedList<Point> points = new LinkedList<>(pointCollection);

    // Sort list by points x-values and get the highest x-value
    points.sort(Comparator.comparingDouble(Point::getX));
    double edgeLengthX = points.getLast().getX();

    // Sort list by points y-values and get the highest y-value
    points.sort(Comparator.comparingDouble(Point::getY));
    double edgeLengthY = points.getLast().getY();

    // Determine the scale factor
    return edgeLengthX / edgeLengthY;
  }

  /**
   * Scales all points by scaling the y-axis of each point. When using the scale factor from the
   * method calcScaleFactor, the resulting set of points is square.
   */
  private static Map<Point, NodeInput> scalePoints(
      Map<Point, NodeInput> pointsToNodes, double scaleFactor) {
    Map<Point, NodeInput> scaledPoints = new HashMap<>(pointsToNodes);

    // Scale all points y-coordinates using the calculated scaling factor
    for (Point point : scaledPoints.keySet()) {
      point.y *= scaleFactor;
    }
    return scaledPoints;
  }

  /**
   * Calculates the scale factor, to scale the distances from the points calculated by the layout
   * algorithm to the real distances represented by the {@link
   * edu.ie3.datamodel.models.input.connector.LineInput}s lengths.
   *
   * @param pointsToNodes The mapping that contains the points from the layout algorithm mapped to
   *     the corresponding {@link NodeInput}s.
   * @param referencePoint A reference point, should be the upper left point from the point set.
   * @return The calculated scale factor.
   */
  private double calcScaleFactorDistance(
      Map<Point, NodeInput> pointsToNodes,
      Point referencePoint,
      Graph graph,
      RawGridElements rawGridElements) {

    // Get the NodeInputModel mapped to the reference point -> reference NodeInputModel
    NodeInput referenceNodeInput = pointsToNodes.get(referencePoint);

    // Get the neighbor list from referenceNodeInput
    List<NodeInput> neighborList = Graphs.neighborListOf(graph, referenceNodeInput);

    if (!neighborList.isEmpty()) {
      NodeInput secondaryNodeInput = neighborList.get(0);

      // Get the corresponding Point object from the algorithm result
      Point secondaryPoint =
          pointsToNodes.entrySet().stream()
              .filter(entry -> entry.getValue().equals(secondaryNodeInput))
              .findAny()
              .orElseThrow()
              .getKey();

      // Calculate distance between secondaryPoint and referencePoint
      double distance =
          distance(
              secondaryPoint.getX(),
              secondaryPoint.getY(),
              referencePoint.getX(),
              referencePoint.getY());

      // Get real distance between the NodeInputModels from the corresponding LineInputModel, if any
      // and       calculate and return the scale factor.
      return rawGridElements.getLines().stream()
          .filter(
              line ->
                  line.getNodeA().equals(referenceNodeInput)
                          && line.getNodeB().equals(secondaryNodeInput)
                      || line.getNodeB().equals(referenceNodeInput)
                          && line.getNodeA().equals(secondaryNodeInput))
          .findAny()
          .map(line -> distance / line.getLength().toSystemUnit().getValue().doubleValue())
          .orElse(1d);
    } else {
      // If no neighbor node for reference node was found, return 1 as scale factor.
      return 1d;
    }
  }

  /**
   * Calculates the bearing to north between referencePoint and point using trigonometry. All angles
   * are in radians. If point is to referencePoint's left, bearing is equal to pi (180°) subtracted
   * by the result of the atan method. If point is to referencePoint's right, pi/2 (90°) has to be
   * added to the atan method, since bearing is orientated to north. If both points x-coordinates
   * are equal, bearing is pi (180°). The calculation assumes that the reference points y-coordinate
   * is smaller than the points y-coordinate. The formula used is copied from
   * http://www.movable-type.co.uk/scripts/latlong.html (Topic Bearing).
   */
  private double calcBearing(Point referencePoint, Point point) {
    double bearing = Double.NaN;

    // Catch standard cases (90° or 180°)
    if (referencePoint.getY() == point.getY()) {
      bearing = Math.PI / 2;
    } else if (referencePoint.getX() == point.getX()) {
      bearing = Math.PI;
    }

    // Calculate bearing
    double deltaY = Math.abs(point.getY() - referencePoint.getY());
    double deltaX = Math.abs(point.getX() - referencePoint.getX());
    if (referencePoint.getX() > point.getX()) {
      bearing = Math.PI - Math.atan(deltaY / deltaX);
    } else if (referencePoint.getX() < point.getX()) {
      bearing = Math.atan(deltaY / deltaX) + Math.PI / 2;
    }

    return bearing;
  }

  /**
   * Calculates a geo position using distance and bearing. The formula used is copied from
   * http://www.movable-type.co.uk/scripts/latlong.html (Topic Destination point).
   *
   * @return The calculated {@link LatLon} geo position.
   */
  private LatLon calcGeoPosition(double distance, double bearing) {
    // Convert latitude and longitude to radians
    double lat1 = Math.toRadians(REFERENCE_POINT_GEO.getLat());
    double lon1 = Math.toRadians(REFERENCE_POINT_GEO.getLon());

    // Calculate angular distance
    double angularDistance = distance / EARTH_RADIUS;

    // Calculate and return new latitude and longitude values
    double lat2 =
        Math.asin(
            (Math.sin(lat1) * Math.cos(angularDistance))
                + (Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(bearing)));
    double lon2 =
        lon1
            + Math.atan2(
                Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(lat1),
                Math.cos(angularDistance) - (Math.sin(lat1) * Math.sin(lat2)));

    return new LatLon(Math.toDegrees(lat2), Math.toDegrees(lon2));
  }

  /**
   * Returns the point with the lowest y coordinate. In case that more than one such point exists,
   * the one with the lowest x coordinate is returned.
   *
   * @param points The collection of points to return the lowest point from.
   * @return The point with the lowest y coordinate. In case more than one such point exists, the
   *     one with the lowest x coordinate is returned.
   */
  // todo JH move somewhere else
  public java.awt.Point getLowestPoint(Collection<java.awt.Point> points) {
    java.awt.Point lowest = new java.awt.Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

    for (java.awt.Point point : points) {
      if (point.y < lowest.y || (point.y == lowest.y && point.x < lowest.x)) {
        lowest = point;
      }
    }
    return lowest;
  }

  public JointGridContainer getJointGridContainer() {
    return jointGridContainer;
  }
}
