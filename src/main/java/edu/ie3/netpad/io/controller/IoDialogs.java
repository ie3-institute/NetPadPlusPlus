/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.io.controller;

import java.io.File;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 26.05.20
 */
public class IoDialogs {

  public IoDialogs() {
    throw new IllegalStateException("Utility class");
  }

  public static Dialog<String> csvFileSeparatorDialog() {

    GridPane gridPane = new GridPane();
    gridPane.setHgap(10);
    gridPane.setVgap(10);
    gridPane.setPadding(new Insets(20, 150, 10, 10));

    Label lbl = new Label(".csv file separator: ");

    final ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList(";", ","));
    comboBox.getSelectionModel().selectFirst();

    gridPane.addRow(0, lbl, comboBox);

    DialogPane dialogPane = new DialogPane();
    dialogPane.setContent(gridPane);

    dialogPane.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

    Dialog<String> csvFileSeparatorDialog = new Dialog<>();
    csvFileSeparatorDialog.setTitle("Select .csv-file separator");
    csvFileSeparatorDialog.setDialogPane(dialogPane);

    csvFileSeparatorDialog.setResultConverter(
        buttonType -> {
          if (buttonType.equals(ButtonType.APPLY)) {
            return comboBox.getSelectionModel().getSelectedItem();
          } else {
            return null;
          }
        });

    return csvFileSeparatorDialog;
  }

  public static Optional<File> chooseDir(String title, Scene scene) {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle(title);

    File selectedFolderFile = directoryChooser.showDialog(scene.getWindow());
    if (selectedFolderFile == null) {
      return Optional.empty();
    } else {
      return Optional.of(selectedFolderFile);
    }
  }
}
