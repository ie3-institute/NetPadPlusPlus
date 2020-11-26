/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.netpad.grid.controller

import edu.ie3.datamodel.models.OperationTime
import edu.ie3.datamodel.models.StandardUnits
import edu.ie3.datamodel.models.input.NodeInput
import edu.ie3.datamodel.models.input.OperatorInput
import edu.ie3.datamodel.models.input.connector.LineInput
import edu.ie3.datamodel.models.input.connector.type.LineTypeInput
import edu.ie3.datamodel.models.input.system.characteristic.OlmCharacteristicInput
import edu.ie3.datamodel.models.voltagelevels.GermanVoltageLevelUtils
import edu.ie3.datamodel.utils.GridAndGeoUtils
import edu.ie3.netpad.io.controller.IoController
import edu.ie3.netpad.io.event.ReadGridEvent
import edu.ie3.netpad.util.SampleGridFactory
import edu.ie3.util.geo.GeoUtils
import edu.ie3.util.quantities.PowerSystemUnits
import edu.ie3.util.quantities.QuantityUtil
import org.locationtech.jts.geom.Coordinate
import spock.lang.Shared
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities

import javax.measure.Quantity
import javax.measure.quantity.Length
import java.util.stream.Collectors

class GridControllerTest extends Specification {

	@Shared
	LineInput testLine

	def setupSpec() {
		/* Build the test line */

		def nodeA = new NodeInput(
				UUID.randomUUID(),
				"nodeA",
				OperatorInput.NO_OPERATOR_ASSIGNED,
				OperationTime.notLimited(),
				Quantities.getQuantity(1d, PowerSystemUnits.PU),
				false,
				GeoUtils.DEFAULT_GEOMETRY_FACTORY.createPoint(new Coordinate(51.49292, 7.41197)),
				GermanVoltageLevelUtils.LV,
				1
				)

		def nodeB = new NodeInput(
				UUID.randomUUID(),
				"nodeB",
				OperatorInput.NO_OPERATOR_ASSIGNED,
				OperationTime.notLimited(),
				Quantities.getQuantity(1d, PowerSystemUnits.PU),
				false,
				GeoUtils.DEFAULT_GEOMETRY_FACTORY.createPoint(new Coordinate(51.49404, 7.41279)),
				GermanVoltageLevelUtils.LV,
				1
				)

		testLine = new LineInput(
				UUID.randomUUID(),
				"testLine",
				OperatorInput.NO_OPERATOR_ASSIGNED,
				OperationTime.notLimited(),
				nodeA,
				nodeB,
				1,
				new LineTypeInput(
				UUID.randomUUID(),
				"testType",
				Quantities.getQuantity(0d, StandardUnits.ADMITTANCE_PER_LENGTH),
				Quantities.getQuantity(0d, StandardUnits.ADMITTANCE_PER_LENGTH),
				Quantities.getQuantity(0d, StandardUnits.IMPEDANCE_PER_LENGTH),
				Quantities.getQuantity(0d, StandardUnits.IMPEDANCE_PER_LENGTH),
				Quantities.getQuantity(0d, StandardUnits.ELECTRIC_CURRENT_MAGNITUDE),
				Quantities.getQuantity(0.4, StandardUnits.RATED_VOLTAGE_MAGNITUDE)
				),
				Quantities.getQuantity(0d, PowerSystemUnits.KILOMETRE),
				GridAndGeoUtils.buildSafeLineStringBetweenNodes(nodeA, nodeB),
				OlmCharacteristicInput.CONSTANT_CHARACTERISTIC
				)
	}

	def "A GridController should find the correct grid based from a valid mapping of grid id and a set of entity ids"() {
		given:
		def validEntityList = [
			UUID.fromString("d56f15b7-8293-4b98-b5bd-58f6273ce229"),
			UUID.fromString("eaf77f7e-9001-479f-94ca-7fb657766f5f"),
			UUID.fromString("d56f15b7-8293-4b98-b5bd-58f6273ce239"),
			UUID.fromString("eaf77f7e-9001-479f-94ca-7fb657766f6f"),
			UUID.fromString("06b58276-8350-40fb-86c0-2414aa4a0452"),
			UUID.fromString("4ca90220-74c2-4369-9afa-a18bf068840d"),
			UUID.fromString("47d29df0-ba2d-4d23-8e75-c82229c5c758"),
			UUID.fromString("bd837a25-58f3-44ac-aa90-c6b6e3cd91b2"),
			UUID.fromString("92ec3bcf-1777-4d38-af67-0bf7c9fa73c7"),
			UUID.fromString("d0f36763-c11e-46a4-bf6b-e57fb06fd8d8"),
			UUID.fromString("4dd1bde7-0ec9-4540-ac9e-008bc5f883ba"),
			UUID.fromString("09aec636-791b-45aa-b981-b14edf171c4c"),
			UUID.fromString("35dc7348-2602-4c4a-99fb-1d1bbc76ec6a"),
			UUID.fromString("211ccf5b-58ee-4c3c-8165-852b0c9255ef"),
			UUID.fromString("30c48ca2-9cfe-423b-bf8c-3adbc6ab496b"),
			UUID.fromString("571e8b88-dd9d-4542-89ed-b7f37916d775"),
			UUID.fromString("b83b93ed-7468-47c2-aed9-48e554c428c7"),
			UUID.fromString("7197e24f-97cd-4764-ae22-40cdc2f26dd2"),
			UUID.fromString("58247de7-e297-4d9b-a5e4-b662c058c655"),
			UUID.fromString("d75b93d0-5d8d-43e5-81a1-8cef01aec56d")
		] as Set

		def gridUuid = UUID.randomUUID()

		def validMapping = [(gridUuid): validEntityList]

		def sampleGrid = SampleGridFactory.sampleJointGrid()

		expect:
		sampleGrid.allEntitiesAsList().size() == validEntityList.size()

		def subGridUuid = GridController.instance.findSubGridUuid(validMapping, sampleGrid)

		subGridUuid.isPresent()
		subGridUuid == Optional.of(gridUuid)
	}

	def "A GridController is able to calculate the correct total length of a LineString"() {
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
		def actualLength = GridController.lengthOfLineString(lineString)

		then:
		actualLength.isPresent()
		QuantityUtil.isEquivalentAbs(actualLength.get(), expectedLength)
	}

	/* Remark: The emtpy Optional being returned when handing in a LineString with only one node cannot be tested, as
	 * a LineString cannot be created with one coordinate only */

	def "A GridController is able to adjust the electrical line length to it's line string's total length"() {
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
		def updateLine = GridController.setLineLengthToGeographicDistance(line)

		then:
		QuantityUtil.isEquivalentAbs(updateLine.getLength(), expectedLength)
	}

	def "A GridController is capable of adjusting the electrical line length to actual length within a selected subnet"() {
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

		when:
		def actual = GridController.instance.setElectricalToGeographicalLineLength(selectedSubnets)
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
