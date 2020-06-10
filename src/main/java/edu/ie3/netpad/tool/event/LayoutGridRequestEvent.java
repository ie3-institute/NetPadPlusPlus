/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.tool.event;

import edu.ie3.datamodel.models.input.container.JointGridContainer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 04.06.20
 */
public class LayoutGridRequestEvent implements ToolEvent {

  private final ObjectProperty<JointGridContainer> readGridEventProperty =
      new SimpleObjectProperty<>();

  public LayoutGridRequestEvent(
      ChangeListener<JointGridContainer> jointGridContainerChangeListener) {
    readGridEventProperty.addListener(jointGridContainerChangeListener);
  }

  public ObjectProperty<JointGridContainer> readGridEventPropertyProperty() {
    return readGridEventProperty;
  }
}
