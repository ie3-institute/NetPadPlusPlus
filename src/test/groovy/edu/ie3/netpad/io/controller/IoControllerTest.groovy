package edu.ie3.netpad.io.controller

import edu.ie3.datamodel.models.input.container.GridContainer
import edu.ie3.netpad.io.event.IOEvent
import edu.ie3.netpad.io.event.ReadGridEvent
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import org.apache.commons.io.FilenameUtils
import spock.lang.Shared
import spock.lang.Specification

class IoControllerTest extends Specification {
    @Shared
    String testFileFolder

    @Shared
    IoEventListener ioListener

    def setupSpec() {
        testFileFolder = this.getClass().getResource("/testFiles").toString().replaceAll("^file:/", "")
        ioListener = new IoEventListener()
        IoController.instance.registerGridControllerListener(ioListener)
    }

    def cleanup() {
        println("blubb")
        ioListener.grid = null
    }

    def "The I/O controller is able to read a grid from flat hierarchy directory"() {
        given:
        def flatDirectory = new File(FilenameUtils.concat(testFileFolder, "flat"))

        when:
        def actual = IoController.instance.loadGridFromCsv(flatDirectory, ";", IoDialogs.CsvImportData.DirectoryHierarchy.FLAT)

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
        def actual = IoController.instance.loadGridFromCsv(flatDirectory, ";", IoDialogs.CsvImportData.DirectoryHierarchy.HIERARCHIC)

        then:
        actual == false
        Objects.isNull(ioListener.grid)
    }

    def "The I/O controller is able to read a grid from nested hierarchy directory"() {
        given:
        def flatDirectory = new File(FilenameUtils.concat(testFileFolder, "hierarchic"))

        when:
        def actual = IoController.instance.loadGridFromCsv(flatDirectory, ";", IoDialogs.CsvImportData.DirectoryHierarchy.HIERARCHIC)

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
        def actual = IoController.instance.loadGridFromArchive(archive, ";", IoDialogs.CsvImportData.DirectoryHierarchy.FLAT)

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
        def actual = IoController.instance.loadGridFromArchive(archive, ";", IoDialogs.CsvImportData.DirectoryHierarchy.HIERARCHIC)

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
