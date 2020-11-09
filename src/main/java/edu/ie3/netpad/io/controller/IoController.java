/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.io.controller;

import edu.ie3.datamodel.exceptions.FileException;
import edu.ie3.datamodel.exceptions.ParsingException;
import edu.ie3.datamodel.io.TarballUtils;
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
import edu.ie3.util.io.FileIOUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.apache.commons.io.FilenameUtils;
import org.locationtech.jts.io.ParseException;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 15.05.20
 */
public class IoController {

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

  public boolean loadGridFromArchive(
      File absoluteArchivePath,
      String csvSeparator,
      IoDialogs.CsvIoData.DirectoryHierarchy hierarchy) {
    /* Create a temp directory */
    Path tmpDirectory;
    try {
      tmpDirectory = Files.createTempDirectory("");
    } catch (IOException e) {
      logger.error("Cannot read from archive, as temp directory creation failed.", e);
      return false;
    }

    /* Extract the content of the tarball */
    Path folderPath;
    try {
      folderPath = TarballUtils.extract(absoluteArchivePath.toPath(), tmpDirectory, false);
    } catch (FileException e) {
      logger.error("Cannot read from archive, as extraction failed.", e);
      return false;
    }

    /* Get the grid from the extracted folder */
    boolean result = loadGridFromDirectory(folderPath.toFile(), csvSeparator, hierarchy);

    /* Clean up the temp directory */
    try {
      FileIOUtils.deleteRecursively(folderPath);
    } catch (IOException e) {
      logger.warn("Cleaning up of temp directory '{}' failed.", tmpDirectory, e);
    }

    return result;
  }

  /**
   * Load the grid model from a directory, utilizing the given hierarchy information.
   *
   * @param absoluteFilePath Absolute path to the base directory of the data set
   * @param csvSeparator Csv column separator to use
   * @param hierarchy Information about the underlying directory hierarchy
   * @return true, if the grid has been read successfully, false otherwise
   */
  public boolean loadGridFromDirectory(
      File absoluteFilePath,
      String csvSeparator,
      IoDialogs.CsvIoData.DirectoryHierarchy hierarchy) {
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
        /* Remove the last part from the path and possibly the last File separator */
        String regex = File.separator.equals("\\") ? "\\\\[\\w\\d-]+\\\\?$" : "/[\\w\\d-]+/?$";
        baseDirectory = absoluteFilePath.toString().replaceAll(regex, "");
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

  /**
   * Saves the given grid container to csv files. Either compressed or not compressed.
   *
   * @param directoryPath Target path, where to put
   * @param csvSeparator csv column separator to use
   * @param hierarchy Information about the hierarchy of the directories to use
   * @param compress true, if the output should be compressed or not
   */
  public void saveGrid(
      File directoryPath,
      String csvSeparator,
      IoDialogs.CsvIoData.DirectoryHierarchy hierarchy,
      boolean compress) {
    // issue an event that we want to save,
    // the listener provided is a one-shot instance which fires when the
    // gridController returns
    notifyListener(
        new SaveGridEvent(
            (observable, oldValue, gridContainer) -> {
              if (compress)
                saveGridCompressed(
                    directoryPath.getAbsolutePath(), gridContainer, hierarchy, csvSeparator);
              else
                saveGridToCsv(
                    directoryPath.getAbsolutePath(), gridContainer, hierarchy, csvSeparator);
            }));
  }

  /**
   * Saves the grid to csv files by utilizing {@link #saveGridToCsv(String, GridContainer,
   * IoDialogs.CsvIoData.DirectoryHierarchy, String)} and later compresses the output.
   *
   * @param targetPath Target directory
   * @param gridContainer Grid container to save
   * @param hierarchy Information about the hierarchy of the directories to use
   * @param csvSeparator csv column separator to use
   */
  private void saveGridCompressed(
      String targetPath,
      GridContainer gridContainer,
      IoDialogs.CsvIoData.DirectoryHierarchy hierarchy,
      String csvSeparator) {
    String gridName = gridContainer.getGridName();
    try {
      /* Determine the target directory path. If the output is meant to be compressed, write the raw files to
       * temp folder */
      String tmpDirectory = Files.createTempDirectory("save_grid").toAbsolutePath().toString();
      String targetDirectory =
          FilenameUtils.concat(
              tmpDirectory,
              ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSS")));
      saveGridToCsv(targetDirectory, gridContainer, hierarchy, csvSeparator);
      Path targetFile = Paths.get(FilenameUtils.concat(targetPath, gridName + ".tar.gz"));
      TarballUtils.compress(Paths.get(targetDirectory), targetFile);
      FileIOUtils.deleteRecursively(tmpDirectory);
    } catch (IOException | FileException e) {
      throw new IoControllerException("Cannot save '" + gridName + "'.", e);
    }
  }

  /**
   * Saves the given grid container to target directory without any compression.
   *
   * @param targetDirectory Target directory
   * @param gridContainer Grid container to save
   * @param hierarchy Information about the hierarchy of the directories to use
   * @param csvSeparator csv column separator to use
   */
  private void saveGridToCsv(
      String targetDirectory,
      GridContainer gridContainer,
      IoDialogs.CsvIoData.DirectoryHierarchy hierarchy,
      String csvSeparator) {
    /* Persist the raw csv data */
    FileNamingStrategy fileNamingStrategy =
        hierarchy == IoDialogs.CsvIoData.DirectoryHierarchy.FLAT
            ? new FileNamingStrategy()
            : new HierarchicFileNamingStrategy(
                new DefaultDirectoryHierarchy(targetDirectory, gridContainer.getGridName()));
    CsvFileSink csvFileSink =
        new CsvFileSink(
            targetDirectory, new ProcessorProvider(), fileNamingStrategy, false, csvSeparator);

    if (gridContainer instanceof JointGridContainer) {
      csvFileSink.persistJointGrid((JointGridContainer) gridContainer);
    } else if (gridContainer instanceof SubGridContainer) {
      csvFileSink.persistAll(gridContainer.allEntitiesAsList());
    } else {
      throw new IoControllerException("Cannot persist unknown grid container: " + gridContainer);
    }

    /* Properly shut down the sink */
    csvFileSink.shutdown();
  }

  private void notifyListener(IOEvent ioEvent) {
    ioEventProperty.setValue(ioEvent);
  }
}
