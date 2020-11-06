/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.io.controller;

import java.io.File;
import java.util.Objects;
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

  public static Dialog<CsvImportData> csvImportDialog() {
    GridPane gridPane = new GridPane();
    gridPane.setHgap(10);
    gridPane.setVgap(10);
    gridPane.setPadding(new Insets(20, 150, 10, 10));

    /* Ask for the csv separator */
    Label separatorLbl = new Label(".csv file separator: ");
    final ComboBox<String> separatorCb =
        new ComboBox<>(FXCollections.observableArrayList(";", ","));
    separatorCb.getSelectionModel().selectFirst();
    gridPane.addRow(0, separatorLbl, separatorCb);

    /* Select the directory hierarchy */
    Label hierarchyLbl = new Label("Directory hierarchy: ");
    ToggleGroup tglGrp = new ToggleGroup();
    ToggleButton flatBtn = new RadioButton("flat");
    flatBtn.setUserData(CsvImportData.DirectoryHierarchy.FLAT);
    flatBtn.setToggleGroup(tglGrp);
    ToggleButton hierarchicBtn = new RadioButton("hierarchic");
    hierarchicBtn.setUserData(CsvImportData.DirectoryHierarchy.HIERARCHIC);
    hierarchicBtn.setToggleGroup(tglGrp);
    tglGrp.selectToggle(flatBtn);
    gridPane.addRow(1, hierarchyLbl, flatBtn, hierarchicBtn);

    DialogPane dialogPane = new DialogPane();
    dialogPane.setContent(gridPane);

    ButtonType directoryButtonType = new ButtonType("From directory");
    ButtonType archiveButtonType = new ButtonType("From archive");

    dialogPane.getButtonTypes().addAll(directoryButtonType, archiveButtonType, ButtonType.CANCEL);

    Dialog<CsvImportData> csvImportDialog = new Dialog<>();
    csvImportDialog.setTitle("Import from csv files");
    csvImportDialog.setDialogPane(dialogPane);

    csvImportDialog.setResultConverter(
        buttonType -> {
          String csvSeparator = separatorCb.getSelectionModel().getSelectedItem();
          CsvImportData.DirectoryHierarchy hierarchy =
              ((CsvImportData.DirectoryHierarchy) tglGrp.getSelectedToggle().getUserData());
          if (buttonType.equals(directoryButtonType)) {
            return new CsvImportData(csvSeparator, hierarchy, CsvImportData.SourceType.DIRECTORY);
          } else if (buttonType.equals(archiveButtonType)) {
            return new CsvImportData(csvSeparator, hierarchy, CsvImportData.SourceType.ARCHIVE);
          } else {
            return null;
          }
        });

    return csvImportDialog;
  }

  public static class CsvImportData {
    private final String csvSeparator;
    private final DirectoryHierarchy hierarchy;
    private final SourceType source;

    public CsvImportData(String csvSeparator, DirectoryHierarchy hierarchy, SourceType source) {
      this.csvSeparator = csvSeparator;
      this.hierarchy = hierarchy;
      this.source = source;
    }

    public String getCsvSeparator() {
      return csvSeparator;
    }

    public DirectoryHierarchy getHierarchy() {
      return hierarchy;
    }

    public SourceType getSource() {
      return source;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      CsvImportData that = (CsvImportData) o;
      return csvSeparator.equals(that.csvSeparator)
          && hierarchy == that.hierarchy
          && source == that.source;
    }

    @Override
    public String toString() {
      return "CsvImportData{"
          + "csvSeparator='"
          + csvSeparator
          + '\''
          + ", hierarchy="
          + hierarchy
          + ", source="
          + source
          + '}';
    }

    @Override
    public int hashCode() {
      return Objects.hash(csvSeparator, hierarchy, source);
    }

    public enum DirectoryHierarchy {
      FLAT,
      HIERARCHIC;
    }

    public enum SourceType {
      DIRECTORY,
      ARCHIVE;
    }
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
