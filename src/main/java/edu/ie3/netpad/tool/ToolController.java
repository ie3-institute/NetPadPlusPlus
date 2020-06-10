/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.tool;

import edu.ie3.datamodel.models.input.container.JointGridContainer;
import edu.ie3.netpad.exception.GridControllerListenerException;
import edu.ie3.netpad.tool.event.LayoutGridRequestEvent;
import edu.ie3.netpad.tool.event.LayoutGridResponse;
import edu.ie3.netpad.tool.event.ToolEvent;
import edu.ie3.netpad.tool.layout.GridLayouter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 04.06.20
 */
public class ToolController {

  private static final ObjectProperty<ToolEvent> toolEventProperty = new SimpleObjectProperty<>();
  private boolean initialized;

  public void layoutGrid() {

    // issue an event that we want to layout the grid
    // the listener provided is a one-shot instance which fires when the
    // gridController returns
    notifyListener(
        new LayoutGridRequestEvent(
            (observable, oldValue, jointGridContainer) -> {

              // layout the grid
              GridLayouter l = new GridLayouter(jointGridContainer);
              JointGridContainer layoutedGrid = l.execute();

              // inform the grid controller about the change

              notifyListener(new LayoutGridResponse(layoutedGrid));
            }));
  }

  private static final class InstanceHolder {
    static final ToolController INSTANCE = new ToolController();
  }

  private ToolController() {}

  public static ToolController getInstance() {
    return ToolController.InstanceHolder.INSTANCE;
  }

  public void registerGridControllerListener(ChangeListener<ToolEvent> listener) {
    if (initialized)
      throw new GridControllerListenerException(
          "ToolController should contain only one listener from a GridController instance. "
              + "There is already a listener registered. Cannot register a second listener!");
    this.initialized = true;
    toolEventProperty.addListener(listener);
  }

  private void notifyListener(ToolEvent toolEvent) {
    toolEventProperty.setValue(toolEvent);
  }
}
