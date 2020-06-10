/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.menu;

import edu.ie3.netpad.grid.event.GridEvent;
import edu.ie3.netpad.grid.event.GridEventListener;
import edu.ie3.netpad.grid.event.ReplaceGridEvent;
import edu.ie3.netpad.tool.ToolController;
import edu.ie3.netpad.util.ListenerUtil;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 04.06.20
 */
public class ToolMenuController implements GridEventListener {

  private static final Logger log = LoggerFactory.getLogger(ToolMenuController.class);

  private final ChangeListener<GridEvent> gridEventListener =
      ListenerUtil.createGridEventListener(this);

  @FXML public MenuItem layoutGridItem;

  @FXML
  public void initialize() {
    layoutGridItem.setOnAction(event -> ToolController.getInstance().layoutGrid());
  }

  @Override
  public ChangeListener<GridEvent> gridEventListener() {
    return gridEventListener;
  }

  @Override
  public void handleGridEvent(GridEvent gridEvent) {
    if (gridEvent instanceof ReplaceGridEvent) {
      handleReplaceGridEvent((ReplaceGridEvent) gridEvent);
    } else {
      log.warn(
          "The provided GridEvent {} is not supported by the GridInfoController!",
          gridEvent.getClass().getSimpleName());
    }
  }

  private void handleReplaceGridEvent(ReplaceGridEvent gridEvent) {

    //    layoutGridItem.setDisable(false);
  }
}
