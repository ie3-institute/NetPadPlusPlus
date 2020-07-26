/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.context.dialog;

import edu.ie3.datamodel.io.processor.input.InputEntityProcessor;
import edu.ie3.datamodel.models.input.InputEntity;
import java.lang.reflect.Method;
import java.util.SortedMap;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 26.07.20
 */
public class DialogInputEntityProcessor extends InputEntityProcessor {

  public DialogInputEntityProcessor(Class<? extends InputEntity> registeredClass) {
    super(registeredClass);
  }

  public SortedMap<String, Method> mapFieldNameToGetter() {
    return super.mapFieldNameToGetter(registeredClass);
  }

  @Override
  public String processMethodResult(Object methodReturnObject, Method method, String fieldName) {
    return super.processMethodResult(methodReturnObject, method, fieldName);
  }
}
