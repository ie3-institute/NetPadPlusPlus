/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.io.controller;

import edu.ie3.datamodel.exceptions.FileException;
import edu.ie3.datamodel.exceptions.ParsingException;
import edu.ie3.datamodel.io.csv.DefaultDirectoryHierarchy;
import edu.ie3.datamodel.io.csv.FileNamingStrategy;
import edu.ie3.datamodel.io.csv.HierarchicFileNamingStrategy;
import edu.ie3.datamodel.io.processor.ProcessorProvider;
import edu.ie3.datamodel.io.sink.CsvFileSink;
import edu.ie3.datamodel.io.source.csv.*;
import edu.ie3.datamodel.models.input.container.*;
import edu.ie3.netpad.exception.GridControllerListenerException;
import edu.ie3.netpad.exception.IoControllerException;
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

  public boolean loadGridFromCsv(
      File absoluteFilePath,
      String csvSeparator,
      IoDialogs.CsvImportData.DirectoryHierarchy hierarchy) {
    /* Collect the information needed to obtain the grid structure */
    String gridName = extractGridName(absoluteFilePath);
    FileNamingStrategy fileNamingStrategy;
    String baseDirectory;
    switch (hierarchy) {
      case FLAT:
        baseDirectory = absoluteFilePath.toString();
        fileNamingStrategy = new FileNamingStrategy();
        break;
      case HIERARCHIC:
        baseDirectory = absoluteFilePath.toString().replaceAll(File.separator + gridName + "$", "");
        try {
          DefaultDirectoryHierarchy directoryHierarchy =
              new DefaultDirectoryHierarchy(baseDirectory, gridName);
          directoryHierarchy.validate();
          fileNamingStrategy = new HierarchicFileNamingStrategy(directoryHierarchy);
        } catch (FileException e) {
          logger.error(
              "Cannot read grid '{}', as the directory hierarchy does not comply with the specifications.",
              gridName,
              e);
          return false;
        }
        break;
      default:
        logger.error("Unsupported hierarchy '{}'.", hierarchy);
        return false;
    }

    /* Build the sources */
    CsvTypeSource typeSource = new CsvTypeSource(csvSeparator, baseDirectory, fileNamingStrategy);
    CsvRawGridSource rawGridSource =
        new CsvRawGridSource(csvSeparator, baseDirectory, fileNamingStrategy, typeSource);
    CsvThermalSource thermalSource =
        new CsvThermalSource(csvSeparator, baseDirectory, fileNamingStrategy, typeSource);
    CsvSystemParticipantSource participantSource =
        new CsvSystemParticipantSource(
            csvSeparator,
            baseDirectory,
            fileNamingStrategy,
            typeSource,
            thermalSource,
            rawGridSource);
    CsvGraphicSource graphicSource =
        new CsvGraphicSource(
            csvSeparator, baseDirectory, fileNamingStrategy, typeSource, rawGridSource);

    /* Actually get the grid */
    RawGridElements rawGrid =
        new RawGridElements(
            rawGridSource.getNodes(),
            rawGridSource.getLines(),
            rawGridSource.get2WTransformers(),
            rawGridSource.get3WTransformers(),
            rawGridSource.getSwitches(),
            rawGridSource.getMeasurementUnits());
    SystemParticipants systemParticipants =
        new SystemParticipants(
            participantSource.getBmPlants(),
            participantSource.getChpPlants(),
            participantSource.getEvCS(),
            participantSource.getEvs(),
            participantSource.getFixedFeedIns(),
            participantSource.getHeatPumps(),
            participantSource.getLoads(),
            participantSource.getPvPlants(),
            participantSource.getStorages(),
            participantSource.getWecPlants());
    GraphicElements graphicElements =
        new GraphicElements(
            graphicSource.getNodeGraphicInput(), graphicSource.getLineGraphicInput());

    /* Return the grid and inform the interested listeners about the result */
    JointGridContainer grid =
        new JointGridContainer(gridName, rawGrid, systemParticipants, graphicElements);
    notifyListener(new ReadGridEvent(grid));
    return true;
  }

  /**
   * Extracts the grid name as the last part of the directory path
   *
   * @param path Directory path
   * @return The grid's name
   */
  private String extractGridName(File path) {
    return path.getAbsolutePath()
        .split(File.separatorChar == '\\' ? "\\\\" : File.separator)[
        path.getAbsolutePath().split(File.separatorChar == '\\' ? "\\\\" : File.separator).length
            - 1];
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
