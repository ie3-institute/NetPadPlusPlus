/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.tool.controller;

import static edu.ie3.netpad.tool.controller.ToolDialogs.ResolutionMode.ELECTRICAL;
import static edu.ie3.netpad.tool.controller.ToolDialogs.ResolutionMode.GEOGRAPHICAL;

import edu.ie3.datamodel.models.input.container.SubGridContainer;
import edu.ie3.datamodel.models.voltagelevels.VoltageLevel;
import edu.ie3.netpad.grid.controller.GridController;
import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.controlsfx.control.CheckTreeView;

public class ToolDialogs {
  protected ToolDialogs() {
    throw new IllegalStateException("Don't instantiate a class with only static methods");
  }

  public static Dialog<String> fixLineLengthDialog() {
    GridPane gridPane = new GridPane();
    gridPane.setHgap(10);
    gridPane.setVgap(10);
    gridPane.setPadding(new Insets(20, 150, 10, 10));

    /* Toggle between modes */
    Label modeLbl = new Label("Resolution mode: ");
    ToggleGroup tglGrp = new ToggleGroup();
    ToggleButton electricalBtn = new RadioButton("Electrical → Geographical");
    electricalBtn.setUserData(ELECTRICAL);
    electricalBtn.setToggleGroup(tglGrp);
    electricalBtn.setDisable(true);
    ToggleButton geographicalBtn = new RadioButton("Geographical → Electrical");
    geographicalBtn.setUserData(GEOGRAPHICAL);
    geographicalBtn.setToggleGroup(tglGrp);
    tglGrp.selectToggle(geographicalBtn);
    gridPane.addRow(0, modeLbl, electricalBtn);
    gridPane.add(geographicalBtn, 1, 1);

    /* Select a subnet first */
    Label subnetLbl = new Label("Subnets: ");
    if (GridController.getInstance().isGridLoaded()) {
      /* There is no grid loaded. Only display a hint on what to do. */
      Text hintTxt = new Text("Load a grid first.");
      gridPane.addRow(2, subnetLbl, hintTxt);
    } else {
      /* List all available sub grids */
      CheckBoxTreeItem<String> root = new CheckBoxTreeItem<>("Subnets");
      root.setExpanded(false);
      root.setSelected(true);

      addToRootTreeItem(
          root,
          GridController.getInstance().getSubGrids().entrySet().parallelStream()
              .collect(
                  Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getSubGridContainer())));

      CheckTreeView<String> treeView = new CheckTreeView<>(root);
      ScrollPane scrollPane = new ScrollPane(treeView);
      gridPane.addRow(2, subnetLbl, scrollPane);
    }

    /* Setting up the dialog pane */
    DialogPane dialogPane = new DialogPane();
    dialogPane.setContent(gridPane);
    dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    /* Disable the ok button, if there is no grid loaded */
    dialogPane
        .lookupButton(ButtonType.OK)
        .disableProperty()
        .bind(Bindings.createBooleanBinding(() -> GridController.getInstance().isGridLoaded()));

    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("Resolve line length discrepancy");
    dialog.setDialogPane(dialogPane);

    return dialog;
  }

  /**
   * Adds {@link CheckBoxTreeItem}s for each voltage level
   *
   * @param root Root entry of the check box tree
   * @param subGridContainerMap Mapping from uuid to sub grid container
   */
  public static void addToRootTreeItem(
      CheckBoxTreeItem<String> root, Map<UUID, SubGridContainer> subGridContainerMap) {
    Collection<CheckBoxTreeItem<String>> treeItems = buildTreeItems(subGridContainerMap);
    treeItems.forEach(voltageLvlChkBox -> root.getChildren().add(voltageLvlChkBox));
  }

  /**
   * Builds all the {@link CheckBoxTreeItem}s for each voltage level
   *
   * @param subGridContainerMap Mapping from uuid to sub grid container
   * @return A collection of nested tree items
   */
  private static Collection<CheckBoxTreeItem<String>> buildTreeItems(
      Map<UUID, SubGridContainer> subGridContainerMap) {
    Map<VoltageLevel, CheckBoxTreeItem<String>> voltLvlToTreeItem = new HashMap<>();

    subGridContainerMap.forEach(
        (uuid, subGrid) -> {
          CheckBoxTreeItem<String> voltageLvlChkBox =
              Optional.ofNullable(voltLvlToTreeItem.get(subGrid.getPredominantVoltageLevel()))
                  .orElseGet(
                      () -> new CheckBoxTreeItem<>(subGrid.getPredominantVoltageLevel().getId()));

          voltageLvlChkBox.setSelected(true);

          CheckBoxTreeItem<String> checkBoxTreeItem =
              new CheckBoxTreeItem<>(Integer.toString(subGrid.getSubnet()));

          checkBoxTreeItem.setSelected(true);

          voltageLvlChkBox.getChildren().add(checkBoxTreeItem);

          voltLvlToTreeItem.put(subGrid.getPredominantVoltageLevel(), voltageLvlChkBox);
        });
    return voltLvlToTreeItem.values();
  }

  public enum ResolutionMode {
    GEOGRAPHICAL,
    ELECTRICAL
  }
}
