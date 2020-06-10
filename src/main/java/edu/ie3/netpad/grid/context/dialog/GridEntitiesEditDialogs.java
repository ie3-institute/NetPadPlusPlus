/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.context.dialog;

import edu.ie3.datamodel.io.factory.input.AssetInputEntityData;
import edu.ie3.datamodel.io.factory.input.NodeInputFactory;
import edu.ie3.datamodel.models.input.NodeInput;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 03.06.20
 */
public class GridEntitiesEditDialogs extends DialogProvider {

  private GridEntitiesEditDialogs() {}

  public static Dialog<NodeInput> editNodeInputDialog(NodeInput node) {

    Map<String, String> fieldsToValues =
        getFieldsToAttributes(node)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Cannot build fields to values for '"
                            + node.getClass().getSimpleName()
                            + "' entity."));

    GridPane gridPane = getAssetInputEditGridPane(fieldsToValues);

    AtomicReference<NodeInput> nodeInputAtomicReference = new AtomicReference<>();

    return getDialog(
        "Edit Node",
        gridPane,
        actionEvent -> {
          Map<String, String> updatedFieldValues = updateFieldValuesFromPane(gridPane);
          updatedFieldValues.remove("operator");
          // actionEvent::consume prevents closing dialog on error
          new NodeInputFactory()
              .getEntity(
                  new AssetInputEntityData(updatedFieldValues, NodeInput.class, node.getOperator()))
              .ifPresentOrElse(nodeInputAtomicReference::set, actionEvent::consume);
        },
        buttonType -> {
          if (buttonType.equals(ButtonType.APPLY)) {
            return nodeInputAtomicReference.get();
          } else {
            return null;
          }
        });
  }
}
