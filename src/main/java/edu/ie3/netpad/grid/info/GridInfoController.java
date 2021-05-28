/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.info;

import static javafx.scene.control.CheckBoxTreeItem.checkBoxSelectionChangedEvent;

import edu.ie3.datamodel.models.input.container.SubGridContainer;
import edu.ie3.datamodel.models.voltagelevels.VoltageLevel;
import edu.ie3.netpad.grid.event.GridEvent;
import edu.ie3.netpad.grid.event.GridEventListener;
import edu.ie3.netpad.grid.event.ReplaceGridEvent;
import edu.ie3.netpad.util.ListenerUtil;
import java.util.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckTreeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 24.05.20
 */
public class GridInfoController implements GridEventListener {

  private static final Logger log = LoggerFactory.getLogger(GridInfoController.class);

  @FXML private VBox upperVBox;

  @FXML private CheckTreeView<String> selectedGridCheckTreeView;

  private final ChangeListener<GridEvent> gridEventListener =
      ListenerUtil.createGridEventListener(this);

  private final ObjectProperty<GridInfoEvent> gridInfoEventProperty = new SimpleObjectProperty<>();

  @Override
  public ChangeListener<GridEvent> gridEventListener() {
    return gridEventListener;
  }

  @Override
  public void handleGridEvent(GridEvent gridEvent) {

    if (gridEvent instanceof ReplaceGridEvent) {
      handleReplaceGridEvent((ReplaceGridEvent) gridEvent);
    } else {
      log.warn(
          "The provided GridEvent {} is not supported by the GridInfoController!",
          gridEvent.getClass().getSimpleName());
    }
  }

  private void handleReplaceGridEvent(ReplaceGridEvent gridEvent) {

    CheckBoxTreeItem<String> root = new CheckBoxTreeItem<>(gridEvent.getGridName());
    root.setExpanded(false);
    root.setSelected(true);

    selectedGridCheckTreeView.setRoot(root);

    addToRootTreeItem(root, gridEvent.getSubGrids());
  }

  /**
   * Adds {@link CheckBoxTreeItem}s for each voltage level
   *
   * @param root Root entry of the check box tree
   * @param subGridContainerMap Mapping from uuid to sub grid container
   */
  public void addToRootTreeItem(
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
  private Collection<CheckBoxTreeItem<String>> buildTreeItems(
      Map<UUID, SubGridContainer> subGridContainerMap) {
    Map<VoltageLevel, CheckBoxTreeItem<String>> voltLvlToTreeItem = new HashMap<>();
    HashMap<CheckBoxTreeItem<String>, UUID> subGridCheckBoxes = new HashMap<>();

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
          checkBoxTreeItem.addEventHandler(
              checkBoxSelectionChangedEvent(),
              (EventHandler<CheckBoxTreeItem.TreeModificationEvent<String>>)
                  event -> {
                    CheckBoxTreeItem<String> chk = event.getTreeItem();
                    UUID subGridUUID = subGridCheckBoxes.get(chk);
                    notifyListener(new GridInfoEvent(subGridUUID, chk.isSelected()));
                  });

          voltageLvlChkBox.getChildren().add(checkBoxTreeItem);

          voltLvlToTreeItem.put(subGrid.getPredominantVoltageLevel(), voltageLvlChkBox);
          subGridCheckBoxes.put(checkBoxTreeItem, uuid);
        });
    return voltLvlToTreeItem.values();
  }

  public ObjectProperty<GridInfoEvent> gridInfoEvents() {
    return gridInfoEventProperty;
  }

  private void notifyListener(GridInfoEvent gridInfoEvent) {
    gridInfoEventProperty.set(gridInfoEvent);
  }
}
