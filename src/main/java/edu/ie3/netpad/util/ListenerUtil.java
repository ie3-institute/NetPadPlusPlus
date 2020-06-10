/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.util;

import edu.ie3.netpad.grid.event.GridEvent;
import edu.ie3.netpad.grid.event.GridEventListener;
import javafx.beans.value.ChangeListener;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 25.05.20
 */
public class ListenerUtil {

  // todo JH javadocs 1) should only called once during init, 2) should contain all actions that are
  // performed by entity update on this layer
  public static ChangeListener<GridEvent> createGridEventListener(
      GridEventListener gridEventListener) {
    return (observable, oldValue, gridEvent) -> gridEventListener.handleGridEvent(gridEvent);
  }
}
