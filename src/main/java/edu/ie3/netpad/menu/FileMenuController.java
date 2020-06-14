/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.menu;

import edu.ie3.netpad.io.controller.IoController;
import edu.ie3.netpad.io.controller.IoDialogs;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

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
  @FXML private MenuItem loadGridInputModelCsvItem;
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
    loadGridInputModelCsvItem.setOnAction(
        event ->
            IoController.getInstance()
                .loadGridFromCsv(menuBar.getScene())
                .ifPresent(ignored -> activateSaveButton()));
    createSampleGridInputModelItem.setOnAction(
        event ->
            IoController.getInstance()
                .createSampleGrid()
                .ifPresent(ignored -> activateSaveButton()));
    saveGridCsvItem.setOnAction(
        event ->
            IoDialogs.chooseDir("Save grid in folder", menuBar.getScene())
                .ifPresent(IoController.getInstance()::saveGridAsCsv));
    exitItem.setOnAction(event -> System.exit(0));
  }

  private void activateSaveButton() {
    saveGrid.setDisable(false);
    saveGridCsvItem.setDisable(false);
  }
}
