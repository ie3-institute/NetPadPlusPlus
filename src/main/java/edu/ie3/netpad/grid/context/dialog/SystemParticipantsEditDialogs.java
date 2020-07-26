/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.context.dialog;

import edu.ie3.datamodel.io.factory.input.NodeAssetInputEntityData;
import edu.ie3.datamodel.io.factory.input.participant.LoadInputFactory;
import edu.ie3.datamodel.io.factory.input.participant.PvInputFactory;
import edu.ie3.datamodel.models.input.system.LoadInput;
import edu.ie3.datamodel.models.input.system.PvInput;
import edu.ie3.datamodel.models.input.system.SystemParticipantInput;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 03.06.20
 */
public class SystemParticipantsEditDialogs extends DialogProvider {
  private SystemParticipantsEditDialogs() {}

  public static Optional<Dialog<SystemParticipantInput>> editSysPartInputDialog(
      SystemParticipantInput systemParticipantInput) {

    Map<String, Control> fieldsToValues = getFieldsToAttributes(systemParticipantInput);

    GridPane gridPane = getAssetInputEditGridPane(fieldsToValues);

    if (systemParticipantInput instanceof PvInput) {
      return Optional.of(editPvInputDialog((PvInput) systemParticipantInput, gridPane));
    } else if (systemParticipantInput instanceof LoadInput) {
      return Optional.of(editLoadInputDialog((LoadInput) systemParticipantInput, gridPane));
    } else {
      return Optional.empty();
    }
  }

  public static Dialog<SystemParticipantInput> editLoadInputDialog(
      LoadInput loadInput, GridPane gridPane) {
    AtomicReference<LoadInput> loadInputAtomicReference = new AtomicReference<>();

    return getDialog(
        "Edit Load",
        gridPane,
        actionEvent -> {
          Map<String, String> updatedFieldValues = updateFieldValuesFromPane(gridPane);
          updatedFieldValues.remove("operator");
          updatedFieldValues.remove("node");
          // actionEvent::consume prevents closing dialog on error
          new LoadInputFactory()
              .getEntity(
                  new NodeAssetInputEntityData(
                      updatedFieldValues,
                      LoadInput.class,
                      loadInput.getOperator(),
                      loadInput.getNode()))
              .ifPresentOrElse(loadInputAtomicReference::set, actionEvent::consume);
        },
        buttonType -> {
          if (buttonType.equals(ButtonType.APPLY)) {
            return loadInputAtomicReference.get();
          } else {
            return null;
          }
        });
  }

  public static Dialog<SystemParticipantInput> editPvInputDialog(
      PvInput pvInput, GridPane gridPane) {

    AtomicReference<PvInput> pvInputAtomicReference = new AtomicReference<>();

    return getDialog(
        "Edit Photovoltaic",
        gridPane,
        actionEvent -> {
          Map<String, String> updatedFieldValues = updateFieldValuesFromPane(gridPane);
          updatedFieldValues.remove("operator");
          updatedFieldValues.remove("node");
          // actionEvent::consume prevents closing dialog on error
          new PvInputFactory()
              .getEntity(
                  new NodeAssetInputEntityData(
                      updatedFieldValues, PvInput.class, pvInput.getOperator(), pvInput.getNode()))
              .ifPresentOrElse(pvInputAtomicReference::set, actionEvent::consume);
        },
        buttonType -> {
          if (buttonType.equals(ButtonType.APPLY)) {
            return pvInputAtomicReference.get();
          } else {
            return null;
          }
        });
  }
}
