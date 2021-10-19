/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.netpad.grid.controller

import edu.ie3.netpad.test.common.SampleData
import edu.ie3.netpad.util.SampleGridFactory
import spock.lang.Specification
import spock.lang.Unroll
import tech.units.indriya.quantity.Quantities

import javax.measure.Quantity
import javax.measure.quantity.Length
import java.util.stream.Collectors

class GridControllerTest extends Specification implements LengthAdaptionTestGrid {

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

		subGridUuid.present
		subGridUuid == Optional.of(gridUuid)
	}

	def "A GridController is able to calculate the correct total length of a LineString"() {
		given:
		def coordinates = [
			new Coordinate(7.41197, 51.49292),
			new Coordinate(7.41183, 51.49333),
			new Coordinate(7.41189, 51.49341),
			new Coordinate(7.41172, 51.49391),
			new Coordinate(7.41279, 51.49404)
		] as Coordinate[]

		def lineString = GeoUtils.DEFAULT_GEOMETRY_FACTORY.createLineString(coordinates)
		def expectedLength = Quantities.getQuantity(0.2372622519686716860237276, PowerSystemUnits.KILOMETRE)

		when:
		def actualLength = GridController.lengthOfLineString(lineString)

		then:
		actualLength.present
		QuantityUtil.isEquivalentAbs(actualLength.get(), expectedLength)
	}

	/* Remark: The emtpy Optional being returned when handing in a LineString with only one node cannot be tested, as
	 * a LineString cannot be created with one coordinate only */

	def "A GridController is able to adjust the electrical line length to it's line string's total length"() {
		given:
		def coordinates = [
			new Coordinate(7.41197, 51.49292),
			new Coordinate(7.41183, 51.49333),
			new Coordinate(7.41189, 51.49341),
			new Coordinate(7.41172, 51.49391),
			new Coordinate(7.41279, 51.49404)
		] as Coordinate[]
		def lineString = GeoUtils.DEFAULT_GEOMETRY_FACTORY.createLineString(coordinates)
		def line = testLine.copy().geoPosition(lineString).build()
		def expectedLength = Quantities.getQuantity(0.2372622519686716860237276, PowerSystemUnits.KILOMETRE)

		when:
		def updateLine = GridController.setLineLengthToGeographicDistance(line)

		then:
		QuantityUtil.isEquivalentAbs(updateLine.length, expectedLength)
	}

	def "A GridController is capable of adjusting the electrical line length to actual length within a selected subnet"() {
		given:
		/* Load sample grid and announce it to the grid controller */
		def sampleGrid = SampleGridFactory.sampleJointGrid()
		IoController.instance.notifyListener(new ReadGridEvent(sampleGrid))

		def expectedLineLengths = [
			/* subnet 1 */
			(UUID.fromString("92ec3bcf-1777-4d38-af67-0bf7c9fa73c7")): Quantities.getQuantity(0.13570403123164909653797, PowerSystemUnits.KILOMETRE),
			(UUID.fromString("4dd1bde7-0ec9-4540-ac9e-008bc5f883ba")): Quantities.getQuantity(0.065091844094861731826615, PowerSystemUnits.KILOMETRE),
			(UUID.fromString("d0f36763-c11e-46a4-bf6b-e57fb06fd8d8")): Quantities.getQuantity(0.11430643088441233981695, PowerSystemUnits.KILOMETRE),

			/* subnet 2 (will be unchanged) */
			(UUID.fromString("b83b93ed-7468-47c2-aed9-48e554c428c7")): Quantities.getQuantity(1.11308358844200193288058, PowerSystemUnits.KILOMETRE),
			(UUID.fromString("571e8b88-dd9d-4542-89ed-b7f37916d775")): Quantities.getQuantity(2.65621973769665467422535, PowerSystemUnits.KILOMETRE),
			(UUID.fromString("7197e24f-97cd-4764-ae22-40cdc2f26dd2")): Quantities.getQuantity(1.82710747893781441715269, PowerSystemUnits.KILOMETRE),
		]

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

	@Unroll
	def "A GridController is able to determine the bearing to #bearing degree between #latLon1 and #latLon2 correctly"() {
		when:
		def actual = GridController.getBearing(latLon1, latLon2)

		then:
		actual.unit == PowerSystemUnits.DEGREE_GEOM
		Math.abs(bearing - actual.value.doubleValue()) < 1E-2

		where:
		latLon1                           | latLon2                                           || bearing
		new LatLon(51.4843281, 7.4116482) | new LatLon(51.493311252841195, 7.4116482)         || 0.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(51.490679705804766, 7.421849967564311) || 45.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(51.4843281, 7.426073668191069)         || 90.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(51.47797560937315, 7.421847125806461)  || 135.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(51.4753449471588, 7.4116482)           || 180.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(51.47797560937315, 7.401449274193539)  || 225.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(51.4843281, 7.397222731808931)         || 270.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(51.490679705804766, 7.401446432435688) || 315.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(53.60884374694267, 7.4116482)          || 0.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(52.9608245636982, 9.90581664831644)    || 45.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(51.4348704962085, 10.820806952802956)  || 90.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(49.958281440399226, 9.746834735372158) || 135.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(49.35981245305733, 7.4116482)          || 180.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(49.958281440399226, 5.076461664627842) || 225.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(51.4348704962085, 4.0024894471970445)  || 270.0
		new LatLon(51.4843281, 7.4116482) | new LatLon(52.9608245636982, 4.91747975168356)    || 315.0
	}

	@Unroll
	def "A GridController is able to determine a second position #distance km away from start with a bearing of #bearing degree."() {
		when:
		def actual = GridController.secondCoordinateWithDistanceAndBearing(
				start,
				Quantities.getQuantity(distance, PowerSystemUnits.KILOMETRE),
				Quantities.getQuantity(bearing, PowerSystemUnits.DEGREE_GEOM)
				)

		then:
		Math.abs(expectedPosition.lat - actual.lat) < 1E-6
		Math.abs(expectedPosition.lon - actual.lon) < 1E-6

		/* The difference between targeted and actual distance is in the order of E-13 in this test. As the above
		 * mentioned snippet does not really point out, what the bearing is, an actual validation is quite hard. */

		where:
		start                             | bearing | distance || expectedPosition
		new LatLon(51.4843281, 7.4116482) | 0.0     | 1.0      || new LatLon(51.493311252841195, 7.4116482)
		new LatLon(51.4843281, 7.4116482) | 45.0    | 1.0      || new LatLon(51.490679705804766, 7.421849967564311)
		new LatLon(51.4843281, 7.4116482) | 90.0    | 1.0      || new LatLon(51.4843281, 7.426073668191069)
		new LatLon(51.4843281, 7.4116482) | 135.0   | 1.0      || new LatLon(51.47797560937315, 7.421847125806461)
		new LatLon(51.4843281, 7.4116482) | 180.0   | 1.0      || new LatLon(51.4753449471588, 7.4116482)
		new LatLon(51.4843281, 7.4116482) | 225.0   | 1.0      || new LatLon(51.47797560937315, 7.401449274193539)
		new LatLon(51.4843281, 7.4116482) | 270.0   | 1.0      || new LatLon(51.4843281, 7.397222731808931)
		new LatLon(51.4843281, 7.4116482) | 315.0   | 1.0      || new LatLon(51.490679705804766, 7.401446432435688)
		new LatLon(51.4843281, 7.4116482) | 0.0     | 236.5    || new LatLon(53.60884374694267, 7.4116482)
		new LatLon(51.4843281, 7.4116482) | 45.0    | 236.5    || new LatLon(52.9608245636982, 9.90581664831644)
		new LatLon(51.4843281, 7.4116482) | 90.0    | 236.5    || new LatLon(51.4348704962085, 10.820806952802956)
		new LatLon(51.4843281, 7.4116482) | 135.0   | 236.5    || new LatLon(49.958281440399226, 9.746834735372158)
		new LatLon(51.4843281, 7.4116482) | 180.0   | 236.5    || new LatLon(49.35981245305733, 7.4116482)
		new LatLon(51.4843281, 7.4116482) | 225.0   | 236.5    || new LatLon(49.958281440399226, 5.076461664627842)
		new LatLon(51.4843281, 7.4116482) | 270.0   | 236.5    || new LatLon(51.4348704962085, 4.0024894471970445)
		new LatLon(51.4843281, 7.4116482) | 315.0   | 236.5    || new LatLon(52.9608245636982, 4.91747975168356)
	}

	def "A GridController is able to update nodes in a grid container"() {
		given:
		IoController.instance.notifyListener(new ReadGridEvent(testGrid))

		def updatedNodeA = nodeA.copy().subnet(3).build()
		def nodeMapping = new HashMap<UUID, NodeInput>()
		nodeMapping.put(updatedNodeA.uuid, updatedNodeA)

		def expectedNodes = [
			(nodeA.uuid): updatedNodeA,
			(nodeB.uuid): nodeB,
			(nodeC.uuid): nodeC,
			(nodeD.uuid): nodeD,
			(nodeE.uuid): nodeE,
			(nodeF.uuid): nodeF,
			(nodeG.uuid): nodeG
		]

		when:
		def actual = GridController.instance.update(testGrid as GridContainer, nodeMapping, [] as Set<LineInput>)
		def uuidToNode = actual
				.rawGrid
				.nodes
				.stream()
				.collect(
				Collectors.toMap(
				{ node -> ((NodeInput) node).uuid }, { node ->
					((NodeInput) node)
				}
				)
				)

		then:
		uuidToNode.size() == expectedNodes.size()
		uuidToNode.forEach { uuid, node ->
			assert expectedNodes.get(uuid) == node
		}
	}

	def "A GridController is able to update lines in a grid container"() {
		given:
		def updatedLineBC = lineBC.copy().length(Quantities.getQuantity(3d, PowerSystemUnits.KILOMETRE)).build()
		def updatedLineCD = lineCD.copy().length(Quantities.getQuantity(3d, PowerSystemUnits.KILOMETRE)).build()
		def updatedLines = new HashSet<LineInput>()
		updatedLines.add(updatedLineBC)
		updatedLines.add(updatedLineCD)

		def expectedLines = new HashMap<UUID, LineInput>()
		expectedLines.put(lineBC.uuid, updatedLineBC)
		expectedLines.put(lineCD.uuid, updatedLineCD)
		expectedLines.put(lineCE.uuid, lineCE)
		expectedLines.put(lineEF.uuid, lineEF)
		expectedLines.put(lineEG.uuid, lineEG)

		when:
		def actual = GridController.instance.update(testGrid as GridContainer, [:] as Map<UUID, NodeInput>, updatedLines)
		def uuidToLine = actual
				.rawGrid
				.lines
				.stream()
				.collect(
				Collectors.toMap(
				{ line -> ((LineInput) line).uuid }, { line ->
					((LineInput) line)
				}
				)
				)

		then:
		uuidToLine.size() == expectedLines.size()
		uuidToLine.forEach { uuid, node ->
			assert expectedLines.get(uuid) == node
		}
	}

	def "A GridController is able to traverse a sub grid and adjust the line length"() {
		given:
		def subGrid = testGrid.subGridTopologyGraph
				.vertexSet()
				.stream()
				.filter({ subgrid -> subgrid.subnet == 2 })
				.findFirst()
				.orElseThrow({ -> new RuntimeException("Someone has stolen subnet 2...") })

		def expectedNodeLocations = [
			(UUID.fromString("36514e92-e6d9-4a7e-85b1-175ec6e27216")): GeoUtils.DEFAULT_GEOMETRY_FACTORY.createPoint(new Coordinate(7.411931, 51.493045)),
			(UUID.fromString("78beb137-45e2-43e3-8baa-1c25f0c91616")): GeoUtils.DEFAULT_GEOMETRY_FACTORY.createPoint(new Coordinate(7.411931, 51.493045)),
			(UUID.fromString("83586a39-8a55-4b10-a034-7ccfe6a451cb")): GeoUtils.DEFAULT_GEOMETRY_FACTORY.createPoint(new Coordinate(7.406760294855038, 51.51527105298355)),
			(UUID.fromString("a0746324-3c74-4a03-9e45-41ae2880ad8d")): GeoUtils.DEFAULT_GEOMETRY_FACTORY.createPoint(new Coordinate(7.398784924455003, 51.53717392365705)),
			(UUID.fromString("1e755e89-7bfb-4987-9bee-358ac1892313")): GeoUtils.DEFAULT_GEOMETRY_FACTORY.createPoint(new Coordinate(7.441181742043899, 51.5220222908142)),
			(UUID.fromString("7dc3d14b-3536-43b8-96f4-0f43b26854f8")): GeoUtils.DEFAULT_GEOMETRY_FACTORY.createPoint(new Coordinate(7.476588680897974, 51.52638713859204)),
			(UUID.fromString("292ea9b1-79f8-4615-bad5-c0d33b17527d")): GeoUtils.DEFAULT_GEOMETRY_FACTORY.createPoint(new Coordinate(7.45062367268736, 51.54369850948029))
		]

		when:
		def actual = GridController.instance.setGeographicalToElectricalLineLength(subGrid)

		then:
		"all line length differ less than 1mm and line string as well as nodes do match"
		actual.rawGrid.lines.forEach {
			def pointA = it.nodeA.geoPosition
			def pointB = it.nodeB.geoPosition
			def geographicalDistance = GeoUtils.calcHaversine(pointA.y, pointA.x, pointB.y, pointB.x)

			assert QuantityUtil.isEquivalentAbs(geographicalDistance, it.length, 10E-6)

			def lineStringStart = it.geoPosition.startPoint
			def lineStringEnd = it.geoPosition.endPoint

			assert (
			lineStringStart.equalsExact(pointA, 1E-6)
			&& lineStringEnd.equalsExact(pointB, 1E-6)
			) || (
			lineStringStart.equalsExact(pointB, 1E-6)
			&& lineStringEnd.equalsExact(pointA, 1E-6)
			)
		}

		"all nodes are at it's foreseen place"
		actual.rawGrid.nodes.forEach {
			def expectedPosition = expectedNodeLocations.get(it.uuid)
			if (Objects.isNull(expectedPosition))
				throw new RuntimeException("Somebody has stolen the expected position of '" + it + "'")

			assert expectedPosition.equalsExact(it.geoPosition, 1E-6)
		}
	}
}
