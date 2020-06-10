/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.grid.controller;

import static edu.ie3.netpad.grid.context.dialog.GridEntitiesEditDialogs.editNodeInputDialog;
import static edu.ie3.netpad.grid.context.dialog.SystemParticipantsEditDialogs.*;

import edu.ie3.datamodel.models.input.AssetInput;
import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.input.connector.Transformer2WInput;
import edu.ie3.datamodel.models.input.system.SystemParticipantInput;
import edu.ie3.netpad.exception.GridControllerListenerException;
import edu.ie3.netpad.grid.context.event.GridContextEvent;
import edu.ie3.netpad.grid.context.event.NodeUpdatedGridContextEvent;
import edu.ie3.netpad.grid.context.event.SystemParticipantUpdatedGridContextEvent;
import edu.ie3.netpad.util.FxUtil;
import edu.ie3.netpad.util.log.FactoryExceptionAppender;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 25.05.20
 */
public class EditGridContextController {

  public static final Logger logger = LogManager.getLogger(EditGridContextController.class);

  private final ObjectProperty<GridContextEvent> gridUpdateEventProperty =
      new SimpleObjectProperty<>();
  private boolean initialized;

  private static final class InstanceHolder {
    static final EditGridContextController INSTANCE = new EditGridContextController();
  }

  private EditGridContextController() {
    setupFactoryExceptionLogErrors();
  }

  public static EditGridContextController getInstance() {
    return InstanceHolder.INSTANCE;
  }

  private void setupFactoryExceptionLogErrors() {
    FactoryExceptionAppender ap = new FactoryExceptionAppender("FactoryExceptionAppender", null);
    ap.start();
    ap.factoryExceptionObjectProperty()
        .addListener(
            (observable, oldValue, factoryException) ->
                Platform.runLater(() -> FxUtil.alert(factoryException.getMessage())));
    ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(ap);
  }

  protected void registerGridControllerListener(ChangeListener<GridContextEvent> listener) {
    if (initialized)
      throw new GridControllerListenerException(
          "EditGridContextController should contain only one listener from a GridController instance. "
              + "There is already a listener registered. Cannot register a second listener!");
    this.initialized = true;
    gridUpdateEventProperty.addListener(listener);
  }

  public void showTransformerContextMenu(
      Node shape,
      Transformer2WInput transformer2WInput,
      Set<SystemParticipantInput> systemParticipants,
      UUID subGridUuid) {
    ContextMenu trafoContextMenu = new ContextMenu();

    /* add node context menu items  */
    MenuItem editNodeAItem =
        editNodeMenuItem("Edit NodeA", transformer2WInput.getNodeA(), subGridUuid);
    MenuItem editNodeBItem =
        editNodeMenuItem("Edit NodeB", transformer2WInput.getNodeB(), subGridUuid);

    /* add transformer2w context menu items  */
    MenuItem editTransformer = new MenuItem("Edit Transformer");
    editTransformer.setDisable(true);
    MenuItem editTransformerType = new MenuItem("Edit Transformer Type");
    editTransformerType.setDisable(true);

    /* add system participant context menu items */
    editSystemParticipantContextMenu(systemParticipants, trafoContextMenu, subGridUuid);

    Arrays.asList(editNodeAItem, editNodeBItem, editTransformer, editTransformerType)
        .forEach(menuItem -> trafoContextMenu.getItems().add(menuItem));

    trafoContextMenu.show(shape, Side.RIGHT, 5, 5);
  }

  public void showNodeContextMenu(
      Node shape,
      NodeInput node,
      Set<SystemParticipantInput> systemParticipants,
      UUID subGridUuid) {

    /* add node context menu items  */
    ContextMenu nodeContextMenu = new ContextMenu();
    MenuItem editGridNameItem = editNodeMenuItem("Edit Node", node, subGridUuid);
    MenuItem deleteNode = new MenuItem("Delete Node");
    deleteNode.setDisable(true);

    nodeContextMenu.getItems().add(editGridNameItem);
    nodeContextMenu.getItems().add(deleteNode);

    /* add system participant context menu items */
    editSystemParticipantContextMenu(systemParticipants, nodeContextMenu, subGridUuid);

    nodeContextMenu.show(shape, Side.RIGHT, 5, 5);
  }

  private MenuItem editNodeMenuItem(String menuItemLbl, NodeInput nodeInput, UUID subGridUuid) {
    MenuItem menuItem = new MenuItem(menuItemLbl);
    menuItem.setOnAction(
        event ->
            editNodeInputDialog(nodeInput)
                .showAndWait()
                .ifPresent(
                    updatedNode ->
                        gridUpdateEventProperty.setValue(
                            new NodeUpdatedGridContextEvent(nodeInput, updatedNode, subGridUuid))));
    return menuItem;
  }

  private void editSystemParticipantContextMenu(
      Set<SystemParticipantInput> systemParticipants, ContextMenu contextMenu, UUID subGridUuid) {
    /* add system participant context menu items */
    // system participants
    Map<Class<? extends AssetInput>, Set<SystemParticipantInput>> sysPartsPerClass =
        systemParticipants.stream()
            .collect(Collectors.groupingBy(SystemParticipantInput::getClass, Collectors.toSet()));

    sysPartsPerClass
        .keySet()
        .forEach(
            sysPartClass -> {
              Menu parentMenu =
                  new Menu("Edit " + sysPartClass.getSimpleName().replace("Input", ""));
              sysPartsPerClass
                  .get(sysPartClass)
                  .forEach(
                      systemParticipant ->
                          parentMenu
                              .getItems()
                              .add(
                                  editSystemParticipantMenuItem(
                                      systemParticipant.getId()
                                          + " ("
                                          + systemParticipant.getUuid()
                                          + ")",
                                      systemParticipant,
                                      subGridUuid)));
              contextMenu.getItems().add(parentMenu);
            });
  }

  private MenuItem editSystemParticipantMenuItem(
      String menuItemLbl, SystemParticipantInput systemParticipant, UUID subGridUuid) {
    MenuItem menuItem = new MenuItem(menuItemLbl);

    editSysPartInputDialog(systemParticipant)
        .ifPresentOrElse(
            dialog ->
                menuItem.setOnAction(
                    event ->
                        dialog
                            .showAndWait()
                            .ifPresent(
                                updatedSystemPart ->
                                    gridUpdateEventProperty.setValue(
                                        new SystemParticipantUpdatedGridContextEvent(
                                            systemParticipant, updatedSystemPart, subGridUuid)))),
            () -> menuItem.setDisable(true));

    return menuItem;
  }
}
