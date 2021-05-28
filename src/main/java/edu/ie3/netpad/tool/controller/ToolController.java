/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.tool.controller;

import edu.ie3.datamodel.models.input.container.JointGridContainer;
import edu.ie3.netpad.exception.GridControllerListenerException;
import edu.ie3.netpad.grid.controller.GridController;
import edu.ie3.netpad.tool.event.FixLineLengthRequestEvent;
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

  private static final class InstanceHolder {
    static final ToolController INSTANCE = new ToolController();
  }

  public static ToolController getInstance() {
    return ToolController.InstanceHolder.INSTANCE;
  }

  private boolean initialized;

  private ToolController() {}

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

  /**
   * Ask the {@link GridController} to fix discrepancy between electrical and geographical line
   * length
   *
   * @param data user preferences for the given operation
   */
  public void fixLineLength(ToolDialogs.FixLineLengthData data) {
    notifyListener(
        new FixLineLengthRequestEvent(data.getResolutionMode(), data.getAffectedSubnets()));
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
