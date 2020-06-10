/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.io.event;

import edu.ie3.datamodel.models.input.container.GridContainer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 25.05.20
 */
public class SaveGridEvent implements IOEvent {

  private final ObjectProperty<GridContainer> readGridEventProperty = new SimpleObjectProperty<>();

  public SaveGridEvent(ChangeListener<GridContainer> jointGridContainerChangeListener) {
    readGridEventProperty.addListener(jointGridContainerChangeListener);
  }

  public ObjectProperty<GridContainer> readGridEventPropertyProperty() {
    return readGridEventProperty;
  }
}
