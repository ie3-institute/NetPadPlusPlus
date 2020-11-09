/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.netpad.io.controller

import edu.ie3.datamodel.models.input.container.GridContainer
import edu.ie3.datamodel.models.input.container.JointGridContainer
import edu.ie3.netpad.io.event.IOEvent
import edu.ie3.netpad.io.event.ReadGridEvent
import edu.ie3.netpad.util.SampleGridFactory
import edu.ie3.util.io.FileIOUtils
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.io.FilenameUtils
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.zip.GZIPInputStream

class IoControllerTest extends Specification {
	@Shared
	String testFileFolder

	@Shared
	IoEventListener ioListener

	@Shared
	JointGridContainer sampleGrid

	@Shared
	IoController ioController

	def setupSpec() {
		testFileFolder = this.getClass().getResource("/testFiles").toString().replaceAll("^file:/", "")
		ioListener = new IoEventListener()
		ioController = new IoController()
		ioController.registerGridControllerListener(ioListener)

		sampleGrid = SampleGridFactory.sampleJointGrid()
	}

	def cleanup() {
		ioListener.grid = null
	}

	def "The I/O controller is able to read a grid from flat hierarchy directory"() {
		given:
		def flatDirectory = new File(FilenameUtils.concat(testFileFolder, "flat"))

		when:
		def actual = ioController.loadGridFromDirectory(flatDirectory, ";", IoDialogs.CsvIoData.DirectoryHierarchy.FLAT)

		then:
		actual == true
		Objects.nonNull(ioListener.grid)
		ioListener.grid.with {
			assert it.rawGrid.nodes.size() == 3
			assert it.rawGrid.lines.size() == 1
			assert it.rawGrid.transformer2Ws.size() == 1

			assert it.systemParticipants.chpPlants.size() == 1
		}
	}

	def "The I/O controller is not able to read a grid from corrupt nested hierarchy directory"() {
		given:
		def flatDirectory = new File(FilenameUtils.concat(testFileFolder, "hierarchicCorrupt"))

		when:
		def actual = ioController.loadGridFromDirectory(flatDirectory, ";", IoDialogs.CsvIoData.DirectoryHierarchy.HIERARCHIC)

		then:
		actual == false
		Objects.isNull(ioListener.grid)
	}

	def "The I/O controller is able to read a grid from nested hierarchy directory"() {
		given:
		def flatDirectory = new File(FilenameUtils.concat(testFileFolder, "hierarchic"))

		when:
		def actual = ioController.loadGridFromDirectory(flatDirectory, ";", IoDialogs.CsvIoData.DirectoryHierarchy.HIERARCHIC)

		then:
		actual == true
		Objects.nonNull(ioListener.grid)
		ioListener.grid.with {
			assert it.rawGrid.nodes.size() == 3
			assert it.rawGrid.lines.size() == 1
			assert it.rawGrid.transformer2Ws.size() == 1

			assert it.systemParticipants.chpPlants.size() == 1
		}
	}

	def "The I/O controller is able to read a grid from flat hierarchy archive"() {
		given:
		def archive = new File(FilenameUtils.concat(testFileFolder, "flat.tar.gz"))

		when:
		def actual = ioController.loadGridFromArchive(archive, ";", IoDialogs.CsvIoData.DirectoryHierarchy.FLAT)

		then:
		actual == true
		Objects.nonNull(ioListener.grid)
		ioListener.grid.with {
			assert it.rawGrid.nodes.size() == 3
			assert it.rawGrid.lines.size() == 1
			assert it.rawGrid.transformer2Ws.size() == 1

			assert it.systemParticipants.chpPlants.size() == 1
		}
	}

	def "The I/O controller is able to read a grid from nested hierarchy archive"() {
		given:
		def archive = new File(FilenameUtils.concat(testFileFolder, "hierarchic.tar.gz"))

		when:
		def actual = ioController.loadGridFromArchive(archive, ";", IoDialogs.CsvIoData.DirectoryHierarchy.HIERARCHIC)

		then:
		actual == true
		Objects.nonNull(ioListener.grid)
		ioListener.grid.with {
			assert it.rawGrid.nodes.size() == 3
			assert it.rawGrid.lines.size() == 1
			assert it.rawGrid.transformer2Ws.size() == 1

			assert it.systemParticipants.chpPlants.size() == 1
		}
	}

	def "The I/O controller is able to write a grid to uncompressed, flat directory hierarchy"() {
		given:
		def tmpDirectory = Files.createTempDirectory("flat_uncompressed").toAbsolutePath().toString()
		def expectedDirectories = [
			FilenameUtils.concat(tmpDirectory, "operator_input.csv"),
			FilenameUtils.concat(tmpDirectory, "node_input.csv"),
			FilenameUtils.concat(tmpDirectory, "line_input.csv"),
			FilenameUtils.concat(tmpDirectory, "line_type_input.csv"),
			FilenameUtils.concat(tmpDirectory, "transformer_2_w_input.csv"),
			FilenameUtils.concat(tmpDirectory, "transformer_2_w_type_input.csv"),
			FilenameUtils.concat(tmpDirectory, "pv_input.csv"),
			FilenameUtils.concat(tmpDirectory, "load_input.csv"),
			FilenameUtils.concat(tmpDirectory, "evcs_input.csv"),
			FilenameUtils.concat(tmpDirectory, "storage_input.csv"),
			FilenameUtils.concat(tmpDirectory, "storage_type_input.csv")
		]

		when:
		try {
			ioController.saveGridToCsv(tmpDirectory, sampleGrid, IoDialogs.CsvIoData.DirectoryHierarchy.FLAT, ";")
		} catch (Exception e) {
			FileIOUtils.deleteRecursively(tmpDirectory)
			throw e
		}

		then:
		noExceptionThrown()
		expectedDirectories.each { filePath ->
			def file = new File(filePath)
			assert file.exists()
			assert file.isFile()
		}

		cleanup:
		FileIOUtils.deleteRecursively(tmpDirectory)
	}

	def "The I/O controller is able to write a grid to uncompressed, nested directory hierarchy"() {
		given:
		def tmpDirectory = Files.createTempDirectory("hierarchic_uncompressed").toAbsolutePath().toString()
		def expectedDirectories = [
			Stream.of(tmpDirectory,"sampleGrid", "input", "global", "operator_input.csv").collect(Collectors.joining(File.separator)),
			Stream.of(tmpDirectory, "sampleGrid", "input", "grid", "node_input.csv").collect(Collectors.joining(File.separator)),
			Stream.of(tmpDirectory, "sampleGrid", "input", "grid", "line_input.csv").collect(Collectors.joining(File.separator)),
			Stream.of(tmpDirectory, "sampleGrid", "input", "global", "line_type_input.csv").collect(Collectors.joining(File.separator)),
			Stream.of(tmpDirectory, "sampleGrid", "input", "grid", "transformer_2_w_input.csv").collect(Collectors.joining(File.separator)),
			Stream.of(tmpDirectory, "sampleGrid", "input", "global", "transformer_2_w_type_input.csv").collect(Collectors.joining(File.separator)),
			Stream.of(tmpDirectory, "sampleGrid", "input", "participants", "pv_input.csv").collect(Collectors.joining(File.separator)),
			Stream.of(tmpDirectory, "sampleGrid", "input", "participants", "load_input.csv").collect(Collectors.joining(File.separator)),
			Stream.of(tmpDirectory, "sampleGrid", "input", "participants", "evcs_input.csv").collect(Collectors.joining(File.separator)),
			Stream.of(tmpDirectory, "sampleGrid", "input", "participants", "storage_input.csv").collect(Collectors.joining(File.separator)),
			Stream.of(tmpDirectory, "sampleGrid", "input", "global", "storage_type_input.csv").collect(Collectors.joining(File.separator))
		]

		when:
		try {
			ioController.saveGridToCsv(tmpDirectory, sampleGrid, IoDialogs.CsvIoData.DirectoryHierarchy.HIERARCHIC, ";")
		} catch (Exception e) {
			FileIOUtils.deleteRecursively(tmpDirectory)
			throw e
		}

		then:
		noExceptionThrown()
		expectedDirectories.each { filePath ->
			def file = new File(filePath)
			assert file.exists()
			assert file.isFile()
		}

		cleanup:
		FileIOUtils.deleteRecursively(tmpDirectory)
	}

	def "The I/O controller is able to write a grid to compressed, flat directory hierarchy"() {
		given:
		def tmpDirectory = Files.createTempDirectory("flat_compressed").toAbsolutePath().toString()
		def expectedArchiveFile = new File(FilenameUtils.concat(tmpDirectory, "sampleGrid.tar.gz"))
		def expectedDirectories = [
			"operator_input.csv",
			"node_input.csv",
			"line_input.csv",
			"line_type_input.csv",
			"transformer_2_w_input.csv",
			"transformer_2_w_type_input.csv",
			"pv_input.csv",
			"load_input.csv",
			"evcs_input.csv",
			"storage_input.csv",
			"storage_type_input.csv"
		]

		when: "saving the archive"
		try {
			ioController.saveGridCompressed(tmpDirectory, sampleGrid, IoDialogs.CsvIoData.DirectoryHierarchy.FLAT, ";")
		} catch (Exception e) {
			FileIOUtils.deleteRecursively(tmpDirectory)
			throw e
		}

		then: "the archive file is apparent"
		noExceptionThrown()
		expectedArchiveFile.exists()
		expectedArchiveFile.isFile()

		and: "the contains the correct entries"
		def actualEntries = []
		def tarInputStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(expectedArchiveFile)))

		ArchiveEntry archiveEntry
		while ((archiveEntry = tarInputStream.getNextEntry()) != null) {
			actualEntries.add(archiveEntry.getName())
		}

		actualEntries.size() == expectedDirectories.size()
		actualEntries.containsAll(expectedDirectories)

		cleanup:
		tarInputStream.close()

		FileIOUtils.deleteRecursively(tmpDirectory)
	}

	def "The I/O controller is able to write a grid to compressed, hierarchic directory hierarchy"() {
		given:
		def tmpDirectory = Files.createTempDirectory("hierarchic_compressed").toAbsolutePath().toString()
		def expectedArchiveFile = new File(FilenameUtils.concat(tmpDirectory, "sampleGrid.tar.gz"))
		def expectedDirectories = [
			Stream.of("sampleGrid", "input", "global", "operator_input.csv").collect(Collectors.joining("/")),
			Stream.of("sampleGrid", "input", "grid", "node_input.csv").collect(Collectors.joining("/")),
			Stream.of("sampleGrid", "input", "grid", "line_input.csv").collect(Collectors.joining("/")),
			Stream.of("sampleGrid", "input", "global", "line_type_input.csv").collect(Collectors.joining("/")),
			Stream.of("sampleGrid", "input", "grid", "transformer_2_w_input.csv").collect(Collectors.joining("/")),
			Stream.of("sampleGrid", "input", "global", "transformer_2_w_type_input.csv").collect(Collectors.joining("/")),
			Stream.of("sampleGrid", "input", "participants", "pv_input.csv").collect(Collectors.joining("/")),
			Stream.of("sampleGrid", "input", "participants", "load_input.csv").collect(Collectors.joining("/")),
			Stream.of("sampleGrid", "input", "participants", "evcs_input.csv").collect(Collectors.joining("/")),
			Stream.of("sampleGrid", "input", "participants", "storage_input.csv").collect(Collectors.joining("/")),
			Stream.of("sampleGrid", "input", "global", "storage_type_input.csv").collect(Collectors.joining("/"))
		]

		when: "saving the archive"
		try {
			ioController.saveGridCompressed(tmpDirectory, sampleGrid, IoDialogs.CsvIoData.DirectoryHierarchy.HIERARCHIC, ";")
		} catch (Exception e) {
			FileIOUtils.deleteRecursively(tmpDirectory)
			throw e
		}

		then: "the archive file is apparent"
		noExceptionThrown()
		expectedArchiveFile.exists()
		expectedArchiveFile.isFile()

		and: "the contains the correct entries"
		def actualEntries = []
		def tarInputStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(expectedArchiveFile)))

		ArchiveEntry archiveEntry
		while ((archiveEntry = tarInputStream.getNextEntry()) != null) {
			actualEntries.add(archiveEntry.getName())
		}

		actualEntries.size() == expectedDirectories.size()
		actualEntries.containsAll(expectedDirectories)

		cleanup:
		tarInputStream.close()

		FileIOUtils.deleteRecursively(tmpDirectory)
	}

	class IoEventListener implements ChangeListener<IOEvent> {
		GridContainer grid

		@Override
		void changed(ObservableValue<? extends IOEvent> observable, IOEvent oldValue, IOEvent newValue) {
			if (newValue instanceof ReadGridEvent) {
				grid = newValue.getGrid()
			}
		}
	}
}
