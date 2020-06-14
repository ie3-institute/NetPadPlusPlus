/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.context.dialog;

import edu.ie3.datamodel.io.processor.input.InputEntityProcessor;
import edu.ie3.datamodel.models.input.AssetInput;
import java.util.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
          ((Label) nodes.get(i)).getText(), ((TextField) nodes.get(i + 1)).getText());
    }
    return updatedFieldsToValuesAfterClick;
  }

  protected static Optional<LinkedHashMap<String, String>> getFieldsToAttributes(
      AssetInput inputEntity) {
    InputEntityProcessor inputEntityProcessor = new InputEntityProcessor(inputEntity.getClass());
    return inputEntityProcessor.handleEntity(inputEntity);
  }

  protected static GridPane getAssetInputEditGridPane(Map<String, String> fieldsToAttributes) {

    List<String> enabledFields = Collections.singletonList("id");

    // Create the grid pane, that holds all contents
    GridPane gridPane = new GridPane();
    gridPane.setHgap(10);
    gridPane.setVgap(10);
    gridPane.setPadding(new Insets(20, 150, 10, 10));
    final int[] rowIdx = {0};
    fieldsToAttributes.forEach(
        (key, value) -> {
          Label lbl = new Label(key);
          TextField gridNameTextField = new TextField(value);

          if (!containsCaseInsensitive(lbl.getText(), enabledFields)) {
            gridNameTextField.setDisable(true);
          }

          gridPane.addRow(rowIdx[0], lbl, gridNameTextField);
          rowIdx[0] = rowIdx[0] + 1;
        });

    return gridPane;
  }

  private static boolean containsCaseInsensitive(String s, List<String> l) {
    return l.stream().anyMatch(x -> x.equalsIgnoreCase(s));
  }
}
