/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.menu;

import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 04.06.20
 */
public class MainMenuBarController {

  @FXML private MenuBar mainMenuBar;

  @FXML private FileMenuController fileMenuController;

  @FXML private ToolMenuController toolMenuController;

  @FXML
  public void initialize() {

    // file menu controller needs iOController instance
    this.fileMenuController.setMenuBar(mainMenuBar);
    this.fileMenuController.initMenuActions();
  }

  public FileMenuController getFileMenuController() {
    return fileMenuController;
  }

  public ToolMenuController getToolMenuController() {
    return toolMenuController;
  }

  public MenuBar getMainMenuBar() {
    return mainMenuBar;
  }
}
