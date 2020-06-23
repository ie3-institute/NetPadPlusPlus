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

    Optional<GridContainer> sampleGridOpt;
    try {
      sampleGridOpt = Optional.of(SampleGridFactory.sampleJointGrid());
    } catch (ParseException | ParsingException e) {
      throw new IoControllerException("Cannot create sample grid: ", e);
    }

    // if grid is present notify listener, otherwise do nothing
    sampleGridOpt.ifPresent(sampleGrid -> notifyListener(new ReadGridEvent(sampleGrid)));

    return sampleGridOpt;
  }

  public Optional<JointGridContainer> loadGridFromCsv(File absoluteFilePath, String csvSeparator) {

    // get the CsvGridSource
    String gridName =
        absoluteFilePath.getAbsolutePath()
            .split(File.separatorChar == '\\' ? "\\\\" : File.separator)[
            absoluteFilePath
                    .getAbsolutePath()
                    .split(File.separatorChar == '\\' ? "\\\\" : File.separator)
                    .length
                - 1];

    // get grid and inform listeners if present
    return new CsvGridSource(absoluteFilePath.getAbsolutePath(), gridName, csvSeparator)
        .getGrid()
        .map(
            grid -> {
              notifyListener(new ReadGridEvent(grid));
              return grid;
            });
  }

  public void saveGridAsCsv(File saveFolderPath, String csvSeparator) {
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
            }));
  }

  private void notifyListener(IOEvent ioEvent) {
    ioEventProperty.setValue(ioEvent);
  }
}
