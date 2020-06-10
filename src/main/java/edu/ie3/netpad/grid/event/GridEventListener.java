/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.event;

import javafx.beans.value.ChangeListener;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 25.05.20
 */
public interface GridEventListener {

  ChangeListener<GridEvent> gridEventListener();

  void handleGridEvent(GridEvent gridEvent);
}
