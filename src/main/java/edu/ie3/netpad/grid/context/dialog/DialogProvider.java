/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.context.dialog;

import edu.ie3.datamodel.models.input.AssetInput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

/**
 * Provides methods commonly used by Dialogs that are shown to edit grid elements
 *
 * @version 0.1
 * @since 03.06.20
 */
abstract class DialogProvider {

  protected DialogProvider() {}

  protected static <T extends AssetInput> Dialog<T> getDialog(
      String dialogTitle,
      GridPane gridPane,
      final EventHandler<ActionEvent> eventFilter,
      final Callback<ButtonType, T> dialogResultCallback) {

    Dialog<T> gridDialog = new Dialog<>();
    gridDialog.setTitle(dialogTitle);
    gridDialog.getDialogPane().setContent(gridPane);
    gridDialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

    gridDialog
        .getDialogPane()
        .lookupButton(ButtonType.APPLY)
        .addEventFilter(ActionEvent.ACTION, eventFilter);

    gridDialog.setResultConverter(dialogResultCallback);

    return gridDialog;
  }

  protected static Map<String, String> updateFieldValuesFromPane(GridPane gridPane) {
    Map<String, String> updatedFieldsToValuesAfterClick = new HashMap<>();
    List<Node> nodes = gridPane.getChildren();
    for (int i = 0; i + 1 < nodes.size(); i = i + 2) {
      updatedFieldsToValuesAfterClick.put(
          ((Label) nodes.get(i)).getText(), getControlContent(nodes.get(i + 1)));
    }
    return updatedFieldsToValuesAfterClick;
  }

  protected static String getControlContent(Node n) {
    String res =
        ""; // todo better return optional and throw exception, this hides error and causes an
    // update with invalid data
    if (n instanceof TextField) res = ((TextField) n).getText();
    if (n instanceof Spinner) res = ((Spinner) n).getValue().toString();

    return res;
  }

  // todo restrict min/max boundaries and increment/decrement step size based on field name
  protected static Map<String, Control> getFieldsToAttributes(AssetInput inputEntity) {
    final DialogInputEntityProcessor inputEntityProcessor =
        new DialogInputEntityProcessor(inputEntity.getClass());
    return inputEntityProcessor.mapFieldNameToGetter().entrySet().stream()
        .map(
            fieldNameToGetter -> {
              Control res;
              try {
                Method getter = fieldNameToGetter.getValue();
                res =
                    Optional.ofNullable(getter.invoke(inputEntity))
                        .map(
                            methodReturnObj -> {
                              switch (getter.getReturnType().getSimpleName()) {
                                case "int":
                                  return new Spinner<>(
                                      Integer.MIN_VALUE,
                                      Integer.MAX_VALUE,
                                      (Integer) methodReturnObj);
                                case "double":
                                  return new Spinner<>(
                                      Double.MIN_VALUE, Double.MAX_VALUE, (Double) methodReturnObj);
                                case "String":
                                default:
                                  return new TextField(
                                      inputEntityProcessor.processMethodResult(
                                          methodReturnObj, getter, fieldNameToGetter.getKey()));
                              }
                            })
                        .orElseGet(() -> new TextField(""));
              } catch (IllegalAccessException | InvocationTargetException e) {
                res = new TextField("");
              }
              return new AbstractMap.SimpleEntry<>(fieldNameToGetter.getKey(), res);
            })
        .collect(
            Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
  }

  protected static GridPane getAssetInputEditGridPane(Map<String, Control> fieldsToAttributes) {

    List<String> enabledFields = Arrays.asList("id", "cosphirated");

    // Create the grid pane, that holds all contents
    GridPane gridPane = new GridPane();
    gridPane.setHgap(10);
    gridPane.setVgap(10);
    gridPane.setPadding(new Insets(20, 150, 10, 10));
    final int[] rowIdx = {0};
    fieldsToAttributes.forEach(
        (key, control) -> {
          Label lbl = new Label(key);

          if (!containsCaseInsensitive(lbl.getText(), enabledFields)) {
            control.setDisable(true);
          }

          gridPane.addRow(rowIdx[0], lbl, control);
          rowIdx[0] = rowIdx[0] + 1;
        });

    return gridPane;
  }

  private static boolean containsCaseInsensitive(String s, List<String> l) {
    return l.stream().anyMatch(x -> x.equalsIgnoreCase(s));
  }
}
