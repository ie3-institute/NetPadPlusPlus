/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.io;

import edu.ie3.datamodel.io.FileNamingStrategy;
import edu.ie3.datamodel.io.source.csv.*;
import edu.ie3.datamodel.models.input.container.GraphicElements;
import edu.ie3.datamodel.models.input.container.JointGridContainer;
import edu.ie3.datamodel.models.input.container.RawGridElements;
import edu.ie3.datamodel.models.input.container.SystemParticipants;
import java.util.Optional;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 15.05.20
 */
public class CsvGridSource {

  private static final FileNamingStrategy csvFileNamingStrategy = new FileNamingStrategy();
  private final CsvRawGridSource csvRawGridSource;
  private final CsvSystemParticipantSource csvSystemParticipantSource;
  private final CsvGraphicSource csvGraphicSource;

  private final String gridName;

  public CsvGridSource(String baseFolder, String gridName, String csvSep) {
    this.gridName = gridName;
    CsvTypeSource csvTypeSource = new CsvTypeSource(csvSep, baseFolder, csvFileNamingStrategy);
    this.csvRawGridSource =
        new CsvRawGridSource(csvSep, baseFolder, csvFileNamingStrategy, csvTypeSource);
    CsvThermalSource csvThermalSource =
        new CsvThermalSource(csvSep, baseFolder, csvFileNamingStrategy, csvTypeSource);
    this.csvSystemParticipantSource =
        new CsvSystemParticipantSource(
            csvSep,
            baseFolder,
            csvFileNamingStrategy,
            csvTypeSource,
            csvThermalSource,
            csvRawGridSource);
    this.csvGraphicSource =
        new CsvGraphicSource(
            csvSep, baseFolder, csvFileNamingStrategy, csvTypeSource, csvRawGridSource);
  }

  public Optional<JointGridContainer> getGrid() {
    final Optional<RawGridElements> rawGridElements = csvRawGridSource.getGridData();
    final Optional<SystemParticipants> systemParticipants =
        csvSystemParticipantSource.getSystemParticipants();
    final Optional<GraphicElements> graphicElements = csvGraphicSource.getGraphicElements();

    if (rawGridElements.isPresent()
        && systemParticipants.isPresent()
        && graphicElements.isPresent())
      return Optional.of(
          new JointGridContainer(
              gridName, rawGridElements.get(), systemParticipants.get(), graphicElements.get()));

    return Optional.empty();
  }
}
