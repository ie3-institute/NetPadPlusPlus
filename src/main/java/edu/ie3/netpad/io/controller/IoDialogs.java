/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.io.controller;

import edu.ie3.netpad.exception.NetPadPlusPlusException;
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

  /**
   * Creates a dialog, that is used to ask the user for details on how the csv data is meant to look
   * like in detail.
   *
   * @param title Window title
   * @param directoryButtonText Button text for the directory button
   * @param archiveButtonText Button text for the archive button
   * @return Detailed information about the csv details as {@link CsvIoData}
   */
  public static Dialog<CsvIoData> csvIoDialog(
      String title, String directoryButtonText, String archiveButtonText) {
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
    flatBtn.setUserData(CsvIoData.DirectoryHierarchy.FLAT);
    flatBtn.setToggleGroup(tglGrp);
    ToggleButton hierarchicBtn = new RadioButton("hierarchic");
    hierarchicBtn.setUserData(CsvIoData.DirectoryHierarchy.HIERARCHIC);
    hierarchicBtn.setToggleGroup(tglGrp);
    tglGrp.selectToggle(flatBtn);
    gridPane.addRow(1, hierarchyLbl, flatBtn, hierarchicBtn);

    DialogPane dialogPane = new DialogPane();
    dialogPane.setContent(gridPane);

    ButtonType directoryButtonType = new ButtonType(directoryButtonText);
    ButtonType archiveButtonType = new ButtonType(archiveButtonText);

    dialogPane.getButtonTypes().addAll(directoryButtonType, archiveButtonType, ButtonType.CANCEL);

    Dialog<CsvIoData> csvImportDialog = new Dialog<>();
    csvImportDialog.setTitle(title);
    csvImportDialog.setDialogPane(dialogPane);

    csvImportDialog.setResultConverter(
        buttonType -> {
          String csvSeparator = separatorCb.getSelectionModel().getSelectedItem();
          CsvIoData.DirectoryHierarchy hierarchy =
              (CsvIoData.DirectoryHierarchy) tglGrp.getSelectedToggle().getUserData();
          if (buttonType.equals(directoryButtonType)) {
            return new CsvIoData(csvSeparator, hierarchy, CsvIoData.SourceType.DIRECTORY);
          } else if (buttonType.equals(archiveButtonType)) {
            return new CsvIoData(csvSeparator, hierarchy, CsvIoData.SourceType.ARCHIVE);
          } else if (buttonType.equals(ButtonType.CANCEL)) {
            return null;
          } else {
            throw new NetPadPlusPlusException(
                "Invalid button type " + buttonType + " in csv I/O dialog.");
          }
        });

    return csvImportDialog;
  }

  /**
   * Container class to gather detailed information about the shape of csv files to import / export
   */
  public static class CsvIoData {
    private final String csvSeparator;
    private final DirectoryHierarchy hierarchy;
    private final SourceType shape;

    public CsvIoData(String csvSeparator, DirectoryHierarchy hierarchy, SourceType shape) {
      this.csvSeparator = csvSeparator;
      this.hierarchy = hierarchy;
      this.shape = shape;
    }

    public String getCsvSeparator() {
      return csvSeparator;
    }

    public DirectoryHierarchy getHierarchy() {
      return hierarchy;
    }

    public SourceType getShape() {
      return shape;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      CsvIoData that = (CsvIoData) o;
      return csvSeparator.equals(that.csvSeparator)
          && hierarchy == that.hierarchy
          && shape == that.shape;
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
          + shape
          + '}';
    }

    @Override
    public int hashCode() {
      return Objects.hash(csvSeparator, hierarchy, shape);
    }

    public enum DirectoryHierarchy {
      FLAT,
      HIERARCHIC
    }

    public enum SourceType {
      DIRECTORY,
      ARCHIVE
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
