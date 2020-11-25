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
import edu.ie3.netpad.util.SampleGridFactory
import edu.ie3.util.geo.GeoUtils
import edu.ie3.util.quantities.PowerSystemUnits
import edu.ie3.util.quantities.QuantityUtil
import org.locationtech.jts.geom.Coordinate
import spock.lang.Shared
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities


class GridControllerTest extends Specification {

	@Shared
	LineInput testLine

	def setupSpec() {
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
		def gridController = new GridController()

		def validEntityList = [
			UUID.fromString("bd837a25-58f3-44ac-aa90-c6b6e3cd91b2"),
			UUID.fromString("60ec3bcf-1777-4d38-af67-0bf7c9fa73c7"),
			UUID.fromString("4ca90220-74c2-4369-9afa-a18bf068840d"),
			UUID.fromString("47d29df0-ba2d-4d23-8e75-c82229c5c758"),
			UUID.fromString("94ec3bcf-1777-4d38-af67-0bf7c9fa73c7"),
			UUID.fromString("93ec3bcf-1777-4d38-af67-0bf7c9fa73c7"),
			UUID.fromString("06b58276-8350-40fb-86c0-2414aa4a0452"),
			UUID.fromString("58247de7-e297-4d9b-a5e4-b662c058c655"),
			UUID.fromString("58257de7-f297-4d9b-a5e4-b662c058c655"),
			UUID.fromString("99ec3bcf-1777-4d38-af67-0bf7c9fa73c7"),
			UUID.fromString("11aec636-791b-45aa-b981-b14edf171c4c"),
			UUID.fromString("09aec636-791b-45aa-b981-b14edf171c4c"),
			UUID.fromString("99fc3bcf-1777-4d38-af67-0bf7c9fa73c7"),
			UUID.fromString("d56f15b7-8293-4b98-b5bd-58f6273ce229"),
			UUID.fromString("92ec3bcf-1777-4d38-af67-0bf7c9fa73c7"),
			UUID.fromString("eaf77f7e-9001-479f-94ca-7fb657766f5f"),
			UUID.fromString("eaf77f7e-9001-479f-94ca-7fb657766f6f"),
			UUID.fromString("12aec637-791b-45aa-b981-b14edf171c4c"),
			UUID.fromString("10aec636-791b-45aa-b981-b14edf171c4c"),
			UUID.fromString("d56f15b7-8293-4b98-b5bd-58f6273ce239")] as Set

		def gridUuid = UUID.randomUUID()

		def validMapping = [(gridUuid) : validEntityList]

		def sampleGrid = SampleGridFactory.sampleJointGrid()

		expect:
		sampleGrid.allEntitiesAsList().size() == validEntityList.size()

		def subGridUuid = gridController.findSubGridUuid(validMapping, sampleGrid)

		subGridUuid.isPresent()
		subGridUuid == Optional.of(gridUuid)
	}

	def "A GridController is able to calculate the correct total length of a LineString"() {
		given:
		def coordinates =  [
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

	def "A GridController is able to adjust the electrical line length to it's line string's total length" () {
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
		/* TODO: This test will fail, until PowerSystemDataModel#251 is resolved, as the order of coordinates will be
		 *   mixed up */
		QuantityUtil.isEquivalentAbs(updateLine.getLength(), expectedLength)
	}
}
