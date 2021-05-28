/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.netpad.tool.grid

import edu.ie3.datamodel.models.input.connector.LineInput
import edu.ie3.netpad.grid.GridModel
import edu.ie3.netpad.grid.controller.GridController
import edu.ie3.netpad.io.controller.IoController
import edu.ie3.netpad.io.event.ReadGridEvent
import edu.ie3.netpad.test.common.SampleData
import edu.ie3.netpad.util.SampleGridFactory
import edu.ie3.util.geo.GeoUtils
import edu.ie3.util.quantities.PowerSystemUnits
import edu.ie3.util.quantities.QuantityUtil
import org.locationtech.jts.geom.Coordinate
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities

import javax.measure.Quantity
import javax.measure.quantity.Length
import java.util.stream.Collectors

class LineLengthFixerTest extends Specification implements SampleData{

	def "A LineLengthFixer is able to calculate the correct total length of a LineString"() {
		given:
		def coordinates = [
			new Coordinate(51.49292, 7.41197),
			new Coordinate(51.49333, 7.41183),
			new Coordinate(51.49341, 7.41189),
			new Coordinate(51.49391, 7.41172),
			new Coordinate(51.49404, 7.41279)
		] as Coordinate[]

		def lineString = GeoUtils.DEFAULT_GEOMETRY_FACTORY.createLineString(coordinates)
		def expectedLength = Quantities.getQuantity(0.188940297821461040910483, PowerSystemUnits.KILOMETRE)

		when:
		def actualLength = LineLengthFixer.lengthOfLineString(lineString)

		then:
		actualLength.isPresent()
		QuantityUtil.isEquivalentAbs(actualLength.get(), expectedLength)
	}

	/* Remark: The emtpy Optional being returned when handing in a LineString with only one node cannot be tested, as
	 * a LineString cannot be created with one coordinate only */

	def "A LineLengthFixer is able to adjust the electrical line length to it's line string's total length"() {
		given:
		def coordinates = [
			new Coordinate(51.49292, 7.41197),
			new Coordinate(51.49333, 7.41183),
			new Coordinate(51.49341, 7.41189),
			new Coordinate(51.49391, 7.41172),
			new Coordinate(51.49404, 7.41279)
		] as Coordinate[]
		def lineString = GeoUtils.DEFAULT_GEOMETRY_FACTORY.createLineString(coordinates)
		def line = testLine.copy().geoPosition(lineString).build()
		def expectedLength = Quantities.getQuantity(0.188940297821461040910483, PowerSystemUnits.KILOMETRE)

		when:
		def updateLine = LineLengthFixer.setLineLengthToGeographicDistance(line)

		then:
		QuantityUtil.isEquivalentAbs(updateLine.getLength(), expectedLength)
	}

	def "A LineLengthFixer is capable of adjusting the electrical line length to actual length within a selected subnet"() {
		given:
		/* Load sample grid and announce it to the grid controller */
		def sampleGrid = SampleGridFactory.sampleJointGrid()
		IoController.instance.notifyListener(new ReadGridEvent(sampleGrid))

		def expectedLineLengths = new HashMap()
		/* subnet 1 */
		expectedLineLengths.put(UUID.fromString("92ec3bcf-1777-4d38-af67-0bf7c9fa73c7"), Quantities.getQuantity(0.13570403123164909653797, PowerSystemUnits.KILOMETRE))
		expectedLineLengths.put(UUID.fromString("4dd1bde7-0ec9-4540-ac9e-008bc5f883ba"), Quantities.getQuantity(0.065091844094861731826615, PowerSystemUnits.KILOMETRE))
		expectedLineLengths.put(UUID.fromString("d0f36763-c11e-46a4-bf6b-e57fb06fd8d8"), Quantities.getQuantity(0.11430643088441233981695, PowerSystemUnits.KILOMETRE))

		/* subnet 2 (will be unchanged) */
		expectedLineLengths.put(UUID.fromString("b83b93ed-7468-47c2-aed9-48e554c428c7"), Quantities.getQuantity(1.11308358844200193288058, PowerSystemUnits.KILOMETRE))
		expectedLineLengths.put(UUID.fromString("571e8b88-dd9d-4542-89ed-b7f37916d775"), Quantities.getQuantity(2.65621973769665467422535, PowerSystemUnits.KILOMETRE))
		expectedLineLengths.put(UUID.fromString("7197e24f-97cd-4764-ae22-40cdc2f26dd2"), Quantities.getQuantity(1.82710747893781441715269, PowerSystemUnits.KILOMETRE))

		def selectedSubnets = [1] as Set
		def allGrids = GridController.getInstance().gridContainerToGridModel(sampleGrid, Collections.emptyMap());

		when:
		def actual = LineLengthFixer.setElectricalToGeographicalLineLength(allGrids, selectedSubnets)
		def actualLineLengths = actual.rawGrid.lines.stream().collect(Collectors.toMap(
				{ k -> ((LineInput) k).uuid },
				{ v -> ((LineInput) v).length }))

		then:
		/* Check each line's length */
		expectedLineLengths.forEach { UUID uuid, Quantity<Length> expectedLength ->
			def actualLength = actualLineLengths.get(uuid)

			assert Objects.nonNull(actualLength)
			assert QuantityUtil.isEquivalentAbs(expectedLength, actualLength)
		}
	}
}
