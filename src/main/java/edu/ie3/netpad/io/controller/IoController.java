/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.io.controller;

import edu.ie3.datamodel.exceptions.ParsingException;
import edu.ie3.datamodel.io.FileNamingStrategy;
import edu.ie3.datamodel.io.processor.ProcessorProvider;
import edu.ie3.datamodel.io.sink.CsvFileSink;
import edu.ie3.datamodel.models.input.container.GridContainer;
import edu.ie3.datamodel.models.input.container.JointGridContainer;
import edu.ie3.datamodel.models.input.container.SubGridContainer;
import edu.ie3.netpad.exception.GridControllerListenerException;
import edu.ie3.netpad.exception.IoControllerException;
import edu.ie3.netpad.io.CsvGridSource;
import edu.ie3.netpad.io.event.IOEvent;
import edu.ie3.netpad.io.event.ReadGridEvent;
import edu.ie3.netpad.io.event.SaveGridEvent;
import edu.ie3.netpad.util.SampleGridFactory;
import java.io.File;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.util.Pair;
import org.locationtech.jts.io.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 15.05.20
 */
public class IoController {

  private static final Logger logger = LoggerFactory.getLogger(IoController.class);

  private static final ObjectProperty<IOEvent> ioEventProperty = new SimpleObjectProperty<>();
  private boolean initialized;

  private static final class InstanceHolder {
    static final IoController INSTANCE = new IoController();
  }

  private IoController() {}

  public static IoController getInstance() {
    return IoController.InstanceHolder.INSTANCE;
  }

  public void registerGridControllerListener(ChangeListener<IOEvent> listener) {
    if (initialized)
      throw new GridControllerListenerException(
          "IoController should contain only one listener from a GridController instance. "
              + "There is already a listener registered. Cannot register a second listener!");
    this.initialized = true;
    ioEventProperty.addListener(listener);
  }

  public Optional<GridContainer> createSampleGrid() {

    Optional<GridContainer> sampleGridOpt = Optional.empty();
    try {
      sampleGridOpt = Optional.of(SampleGridFactory.sampleJointGrid());
    } catch (ParseException | ParsingException e) {
      throw new IoControllerException("Cannot create sample grid: ", e);
    }

    // if grid is present notify listener, otherwise do nothing
    sampleGridOpt.ifPresent(sampleGrid -> notifyListener(new ReadGridEvent(sampleGrid)));

    return sampleGridOpt;
  }

  public Optional<JointGridContainer> loadGridFromCsv(Scene scene) {

    Optional<JointGridContainer> res;

    // get the path and the gridName (derived from the the folder name)
    Pair<String, String> path =
        buildCsvSourceFolderPath(scene)
            .orElseThrow(
                () ->
                    new RuntimeException("Cannot open folder!")); // todo error handling with dialog

    // get the CsvGridSource
    String gridName =
        path.getKey()
            .split(File.separator)[
            path.getKey().split(File.separatorChar == '\\' ? "\\\\" : File.separator).length - 1];
    CsvGridSource csvGridSource = new CsvGridSource(path.getKey(), gridName);

    // get the grid container
    res = Optional.of(csvGridSource.getGrid());

    // if grid is present notify listener, otherwise do nothing
    res.ifPresent(grid -> notifyListener(new ReadGridEvent(grid)));

    return res;
  }

  // todo refactor -> not checked just copied
  // todo on refactor -> remove scene as this controller should not do anything considering scenes
  // or stages but only IO ops
  private Optional<Pair<String, String>> buildCsvSourceFolderPath(Scene scene) {

    // Create and open a directory chooser
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("Load GridInputModel from CSV");

    File file = directoryChooser.showDialog(scene.getWindow());

    if (file != null) {
      String path = file.getPath();
      String prefix = "";

      File inputPath = new File(path.concat(System.getProperty("file.separator")).concat("input"));
      if (inputPath.exists() && inputPath.isDirectory()) {
        File[] availableInputFolders = inputPath.listFiles(File::isDirectory);

        if (availableInputFolders != null) {
          if (availableInputFolders.length > 1) {
            Dialog<String> selectPrefixDialog = new Dialog<>();
            selectPrefixDialog.setTitle("Select Prefix");

            selectPrefixDialog.getDialogPane().getButtonTypes().add(ButtonType.APPLY);

            ListView<String> prefixListView = new ListView<>();
            prefixListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            for (File availableInputFolder : availableInputFolders) {
              prefixListView.getItems().add(availableInputFolder.getName());
            }
            // Make the list view only as big as it needs to be (does not work this way because no
            // cell factory is set)
            //                        prefixListView.setFixedCellSize(30);
            //
            // prefixListView.prefHeightProperty().bind(Bindings.size(prefixListView.getItems())
            //
            // .multiply(prefixListView.fixedCellSizeProperty().add(1)));

            selectPrefixDialog.getDialogPane().setContent(prefixListView);

            selectPrefixDialog.setResultConverter(
                dialogButton -> prefixListView.getSelectionModel().getSelectedItem());

            Optional<String> result = selectPrefixDialog.showAndWait();
            if (result.isPresent()) {
              prefix = result.get();
            }
          } else if (availableInputFolders.length == 1) {
            prefix = availableInputFolders[0].getName();
          }
        }
      } else {
        logger.error("Input folder not found!");
      }

      return Optional.of(new Pair<>(path, prefix));

    } else {
      return Optional.empty();
    }
  }

  public void saveGridAsCsv(File saveFolderPath) {
    IoDialogs.csvFileSeparatorDialog()
        .showAndWait()
        .ifPresent(
            csvSeparator ->
                // issue an event that we want to save,
                // the listener provided is a one-shot instance which fires when the
                // gridController returns
                notifyListener(
                    new SaveGridEvent(
                        (observable, oldValue, gridContainer) -> {

                          // create a new csv file sink
                          CsvFileSink csvFileSink =
                              new CsvFileSink(
                                  saveFolderPath.getAbsolutePath(),
                                  new ProcessorProvider(),
                                  new FileNamingStrategy(),
                                  false,
                                  csvSeparator);

                          if (gridContainer instanceof JointGridContainer) {
                            csvFileSink.persistJointGrid((JointGridContainer) gridContainer);
                          } else if (gridContainer instanceof SubGridContainer) {
                            csvFileSink.persistAll(gridContainer.allEntitiesAsList());
                          } else {
                            throw new IoControllerException(
                                "Cannot persist unknown grid container: " + gridContainer);
                          }
                        })));
  }

  private void notifyListener(IOEvent ioEvent) {
    ioEventProperty.setValue(ioEvent);
  }
}
