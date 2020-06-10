/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.util.log;

import edu.ie3.datamodel.exceptions.FactoryException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "MapAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class FactoryExceptionAppender extends AbstractAppender {

  private ObjectProperty<FactoryException> factoryExceptionObjectProperty =
      new SimpleObjectProperty<>();

  public FactoryExceptionAppender(String name, Filter filter) {
    super(name, filter, null);
  }

  @PluginFactory
  public static FactoryExceptionAppender createAppender(
      @PluginAttribute("name") String name, @PluginElement("Filter") Filter filter) {
    return new FactoryExceptionAppender(name, filter);
  }

  @Override
  public void append(LogEvent event) {
    if (event.getThrown() instanceof FactoryException) {
      factoryExceptionObjectProperty.setValue((FactoryException) event.getThrown());
    }
  }

  public ObjectProperty<FactoryException> factoryExceptionObjectProperty() {
    return factoryExceptionObjectProperty;
  }
}
