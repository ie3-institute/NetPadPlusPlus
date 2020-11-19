/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.main.controller;

import edu.ie3.netpad.grid.controller.GridController;
import edu.ie3.netpad.grid.event.GridEvent;
import edu.ie3.netpad.grid.event.GridEventListener;
import edu.ie3.netpad.grid.event.ReplaceGridEvent;
import edu.ie3.netpad.grid.info.GridInfoController;
import edu.ie3.netpad.map.MapController;
import edu.ie3.netpad.menu.MainMenuBarController;
import edu.ie3.netpad.util.ListenerUtil;
import java.util.Arrays;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController implements GridEventListener {

  private static final Logger log = LoggerFactory.getLogger(MainController.class);

  @FXML private Button gridInfoButton; // Shows/hides the GridInfoView
  @FXML private SplitPane splitPane; // Contains the GridInfoView (if active) and the GridMapView
  @FXML private ScrollPane gridInfo; // Contains the GridInfoView

  @FXML private MapController mapController; // The instance of the GridMapController

  @FXML private GridInfoController gridInfoController;

  @FXML private MainMenuBarController mainMenuBarController;

  private final ChangeListener<GridEvent> gridEventListener;

  private boolean gridInfoActive = false;

  // Indicates whether the GridInfoView is currently active.
  private final DoubleProperty gridInfoDividerPosition = new SimpleDoubleProperty(0.25);

  public MainController() {
    this.gridEventListener = ListenerUtil.createGridEventListener(this);
  }

  @Override
  public ChangeListener<GridEvent> gridEventListener() {
    return this.gridEventListener;
  }

  @Override
  public void handleGridEvent(GridEvent gridEvent) {

    if (gridEvent instanceof ReplaceGridEvent) {
      gridInfoButton.setDisable(false);
    } else {
      log.warn(
          "The provided GridEvent {} is not supported by the GridInfoController!",
          gridEvent.getClass().getSimpleName());
    }
  }

  /**
   * Will be called once when the contents of its associated document (= @FXML annotated members)
   * have been completely loaded. this allows to perform any necessary !!! post-processing !!! on
   * the annotated members e.g. get/set data or bindings from the fields visible on the view and
   * should NOT be used for anything else! (NO service registration or something else)
   */
  @FXML
  public void initialize() {

    // disable gridInfo on startup
    splitPane.getItems().remove(gridInfo);

    // initialize the bindings on the control elements (buttons, etc.)
    gridInfoButton.setOnAction(
        event -> {
          if (!gridInfoActive) {
            // Add the GridInfoView to the split pane at the first position and set and bind the
            // divider position
            splitPane.getItems().add(0, gridInfo);
            splitPane.setDividerPosition(0, gridInfoDividerPosition.doubleValue());
            gridInfoDividerPosition.bind(splitPane.getDividers().get(0).positionProperty());
            gridInfoActive = true;
          } else {
            // Unbind the divider position and remove the GridInfoView from the split pane
            splitPane.getDividers().get(0).positionProperty().unbind();
            splitPane.getItems().remove(gridInfo);
            gridInfoActive = false;
          }
        });
  }

  /** Called after initialization is finished. */
  public void postInitialization() {

    /* register listener that receive gridUpdates from gridController */
    Arrays.asList(
            mapController.gridEventListener(),
            gridInfoController.gridEventListener(),
            mainMenuBarController.getToolMenuController().gridEventListener(),
            this.gridEventListener())
        .forEach(GridController.getInstance().gridUpdateEvents()::addListener);

    /* register listener that receive updates from map controller (e.g. dragged nodes) */
    mapController
        .mapUpdateEvents()
        .addListener(GridController.getInstance().gridMapEventListener());

    /* register listener that receive updates from gridInfoController*/
    gridInfoController.gridInfoEvents().addListener(mapController.gridInfoEventListener());
  }
}
