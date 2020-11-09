/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.menu;

import static edu.ie3.netpad.io.controller.IoDialogs.CsvIoData.SourceType.ARCHIVE;

import edu.ie3.netpad.exception.NetPadPlusPlusException;
import edu.ie3.netpad.io.controller.IoController;
import edu.ie3.netpad.io.controller.IoDialogs;
import java.io.File;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 15.05.20
 */
public class FileMenuController {

  @FXML protected Menu fileMenu;
  @FXML private MenuItem createEmptyGridInputModelItem;
  @FXML private MenuItem createSampleGridInputModelItem;
  @FXML private MenuItem fromCsvItem;
  @FXML private MenuItem loadEfRuhrGridModelItem;
  @FXML private Menu saveGrid;
  @FXML private MenuItem saveGridCsvItem;
  @FXML private MenuItem saveEfRuhrGridModelItem;
  @FXML private MenuItem exitItem;

  private MenuBar menuBar;

  public void setMenuBar(MenuBar menuBar) {
    this.menuBar = menuBar;
  }

  public void initMenuActions() {

    if (menuBar == null)
      throw new RuntimeException(
          "Cannot initialize FileMenu actions without an a reference to the holding menuBar!"
              + "Please use 'setMenuBar(...)' first!");

    // initialize all currently activated menu items
    fromCsvItem.setOnAction(
        event ->
            IoDialogs.csvIoDialog("Import from csv files", "From directory", "From archive")
                .showAndWait()
                .flatMap(
                    csvIoData -> {
                      Optional<Boolean> maybeSuccess;
                      switch (csvIoData.getShape()) {
                        case ARCHIVE:
                          maybeSuccess =
                              getPathFromFileChooser(menuBar.getScene())
                                  .map(
                                      absoluteDirectoryPath ->
                                          IoController.getInstance()
                                              .loadGridFromArchive(
                                                  absoluteDirectoryPath,
                                                  csvIoData.getCsvSeparator(),
                                                  csvIoData.getHierarchy()));
                          break;
                        case DIRECTORY:
                          maybeSuccess =
                              getPathFromDirChooser(menuBar.getScene())
                                  .map(
                                      absoluteDirectoryPath ->
                                          IoController.getInstance()
                                              .loadGridFromDirectory(
                                                  absoluteDirectoryPath,
                                                  csvIoData.getCsvSeparator(),
                                                  csvIoData.getHierarchy()));
                          break;
                        default:
                          throw new NetPadPlusPlusException(
                              "Unable to handle csv shape '" + csvIoData.getShape() + "'");
                      }
                      return maybeSuccess;
                    })
                .ifPresent(
                    success -> {
                      if (success) activateSaveButton();
                      else {
                        Alert alert =
                            new Alert(
                                Alert.AlertType.ERROR,
                                "Unable to read grid. Check your import settings.",
                                ButtonType.OK);
                        alert.show();
                      }
                    }));
    createSampleGridInputModelItem.setOnAction(
        event ->
            IoController.getInstance()
                .createSampleGrid()
                .ifPresent(ignored -> activateSaveButton()));
    saveGridCsvItem.setOnAction(
        event ->
            IoDialogs.csvIoDialog("Export to csv files", "To directory", "To archive")
                .showAndWait()
                .ifPresent(
                    csvIoData ->
                        getPathFromDirChooser(menuBar.getScene())
                            .ifPresent(
                                absoluteDirectoryPath ->
                                    IoController.getInstance()
                                        .saveGrid(
                                            absoluteDirectoryPath,
                                            csvIoData.getCsvSeparator(),
                                            csvIoData.getHierarchy(),
                                            csvIoData.getShape() == ARCHIVE))));

    exitItem.setOnAction(event -> System.exit(0));
  }

  private void activateSaveButton() {
    saveGrid.setDisable(false);
    saveGridCsvItem.setDisable(false);
  }

  private Optional<File> getPathFromDirChooser(Scene scene) {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("Load GridInputModel from CSV");

    return Optional.ofNullable(directoryChooser.showDialog(scene.getWindow()));
  }

  private Optional<File> getPathFromFileChooser(Scene scene) {
    FileChooser fileChooser = new FileChooser();
    FileChooser.ExtensionFilter extensionFilter =
        new FileChooser.ExtensionFilter("Archive files (*.tar.gz)", "*.tar.gz");
    fileChooser.getExtensionFilters().add(extensionFilter);
    fileChooser.setTitle("Load GridInputModel from CSV");

    return Optional.ofNullable(fileChooser.showOpenDialog(scene.getWindow()));
  }
}
