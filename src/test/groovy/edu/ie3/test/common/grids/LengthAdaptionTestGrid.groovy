/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.test.common.grids

import edu.ie3.datamodel.models.OperationTime
import edu.ie3.datamodel.models.input.NodeInput
import edu.ie3.datamodel.models.input.OperatorInput
import edu.ie3.datamodel.models.input.connector.LineInput
import edu.ie3.datamodel.models.input.connector.Transformer2WInput
import edu.ie3.datamodel.models.input.connector.type.LineTypeInput
import edu.ie3.datamodel.models.input.connector.type.Transformer2WTypeInput
import edu.ie3.datamodel.models.input.container.GraphicElements
import edu.ie3.datamodel.models.input.container.JointGridContainer
import edu.ie3.datamodel.models.input.container.RawGridElements
import edu.ie3.datamodel.models.input.container.SystemParticipants
import edu.ie3.datamodel.models.input.system.characteristic.OlmCharacteristicInput
import edu.ie3.datamodel.models.voltagelevels.GermanVoltageLevelUtils
import edu.ie3.util.quantities.PowerSystemUnits
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.locationtech.jts.io.geojson.GeoJsonReader
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

import javax.measure.MetricPrefix

import static tech.units.indriya.unit.Units.SIEMENS

/**
 * This is a star-topology grid, that is at the first hand used to test the adaption of geo positions in order to meet
 * the electrical line length
 */
trait LengthAdaptionTestGrid {
	private static final GeoJsonReader geoJsonReader = new GeoJsonReader()

	/* === Nodes === */
	NodeInput nodeA = new NodeInput(
	UUID.fromString("36514e92-e6d9-4a7e-85b1-175ec6e27216"),
	"node_a",
	OperatorInput.NO_OPERATOR_ASSIGNED,
	OperationTime.notLimited(),
	Quantities.getQuantity(1d, PowerSystemUnits.PU),
	true,
	geoJsonReader.read("{\"type\":\"Point\",\"coordinates\":[7.411931,51.493045],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}") as Point,
	GermanVoltageLevelUtils.MV_20KV,
	1
	)
	NodeInput nodeB = new NodeInput(
	UUID.fromString("78beb137-45e2-43e3-8baa-1c25f0c91616"),
	"node_b",
	OperatorInput.NO_OPERATOR_ASSIGNED,
	OperationTime.notLimited(),
	Quantities.getQuantity(1d, PowerSystemUnits.PU),
	false,
	geoJsonReader.read("{\"type\":\"Point\",\"coordinates\":[7.411931,51.493045],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}") as Point,
	GermanVoltageLevelUtils.LV,
	2
	)
	NodeInput nodeC = new NodeInput(
	UUID.fromString("83586a39-8a55-4b10-a034-7ccfe6a451cb"),
	"node_c",
	OperatorInput.NO_OPERATOR_ASSIGNED,
	OperationTime.notLimited(),
	Quantities.getQuantity(1d, PowerSystemUnits.PU),
	false,
	geoJsonReader.read("{\"type\":\"Point\",\"coordinates\":[7.411728,51.493918],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}") as Point,
	GermanVoltageLevelUtils.LV,
	2
	)
	NodeInput nodeD = new NodeInput(
	UUID.fromString("a0746324-3c74-4a03-9e45-41ae2880ad8d"),
	"node_d",
	OperatorInput.NO_OPERATOR_ASSIGNED,
	OperationTime.notLimited(),
	Quantities.getQuantity(1d, PowerSystemUnits.PU),
	false,
	geoJsonReader.read("{\"type\":\"Point\",\"coordinates\":[7.411497,51.494553],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}") as Point,
	GermanVoltageLevelUtils.LV,
	2
	)
	NodeInput nodeE = new NodeInput(
	UUID.fromString("1e755e89-7bfb-4987-9bee-358ac1892313"),
	"node_e",
	OperatorInput.NO_OPERATOR_ASSIGNED,
	OperationTime.notLimited(),
	Quantities.getQuantity(1d, PowerSystemUnits.PU),
	false,
	geoJsonReader.read("{\"type\":\"Point\",\"coordinates\":[7.412838,51.494136],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}") as Point,
	GermanVoltageLevelUtils.LV,
	2
	)
	NodeInput nodeF = new NodeInput(
	UUID.fromString("7dc3d14b-3536-43b8-96f4-0f43b26854f8"),
	"node_f",
	OperatorInput.NO_OPERATOR_ASSIGNED,
	OperationTime.notLimited(),
	Quantities.getQuantity(1d, PowerSystemUnits.PU),
	false,
	geoJsonReader.read("{\"type\":\"Point\",\"coordinates\":[7.413761,51.494250],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}") as Point,
	GermanVoltageLevelUtils.LV,
	2
	)
	NodeInput nodeG = new NodeInput(
	UUID.fromString("292ea9b1-79f8-4615-bad5-c0d33b17527d"),
	"node_g",
	OperatorInput.NO_OPERATOR_ASSIGNED,
	OperationTime.notLimited(),
	Quantities.getQuantity(1d, PowerSystemUnits.PU),
	false,
	geoJsonReader.read("{\"type\":\"Point\",\"coordinates\":[7.413123,51.494791],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}") as Point,
	GermanVoltageLevelUtils.LV,
	2
	)

	/* === Transformers === */
	def transformerType = new Transformer2WTypeInput(
	UUID.fromString("08559390-d7c0-4427-a2dc-97ba312ae0ac"),
	"MS-NS_1",
	Quantities.getQuantity(10.078, Units.OHM),
	Quantities.getQuantity(23.312, Units.OHM),
	Quantities.getQuantity(630.0, PowerSystemUnits.KILOVOLTAMPERE),
	Quantities.getQuantity(20.0, PowerSystemUnits.KILOVOLT),
	Quantities.getQuantity(0.4, PowerSystemUnits.KILOVOLT),
	Quantities.getQuantity(0.0, MetricPrefix.NANO(SIEMENS)),
	Quantities.getQuantity(0.0, MetricPrefix.NANO(SIEMENS)),
	Quantities.getQuantity(0.5, Units.PERCENT),
	Quantities.getQuantity(0.0, PowerSystemUnits.DEGREE_GEOM),
	false,
	0,
	-10,
	10
	)
	Transformer2WInput transformer = new Transformer2WInput(
	UUID.fromString("e691ebc3-fcd6-420d-8fc6-ecf45e88c86c"),
	"transformer",
	OperatorInput.NO_OPERATOR_ASSIGNED,
	OperationTime.notLimited(),
	nodeA,
	nodeB,
	1,
	transformerType,
	0,
	false
	)

	/* === Lines === */
	def lineType = new LineTypeInput(
	UUID.fromString("3bed3eb3-9790-4874-89b5-a5434d408088"),
	"lineType",
	Quantities.getQuantity(0.00322, PowerSystemUnits.MICRO_SIEMENS_PER_KILOMETRE),
	Quantities.getQuantity(0d, PowerSystemUnits.MICRO_SIEMENS_PER_KILOMETRE),
	Quantities.getQuantity(0.437, PowerSystemUnits.OHM_PER_KILOMETRE),
	Quantities.getQuantity(0.356, PowerSystemUnits.OHM_PER_KILOMETRE),
	Quantities.getQuantity(300d, Units.AMPERE),
	Quantities.getQuantity(0.4, PowerSystemUnits.KILOVOLT)
	)
	LineInput lineBC = new LineInput(
	UUID.fromString("57be16a6-e55a-4177-915c-d44b5dc7c78a"),
	"b_c",
	OperatorInput.NO_OPERATOR_ASSIGNED,
	OperationTime.notLimited(),
	nodeB,
	nodeC,
	1,
	lineType,
	Quantities.getQuantity(2.5, PowerSystemUnits.KILOMETRE),
	geoJsonReader.read("{\"type\":\"LineString\",\"coordinates\":[[7.411931,51.493045],[7.411728,51.493918]],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}") as LineString,
	OlmCharacteristicInput.CONSTANT_CHARACTERISTIC
	)
	LineInput lineCD = new LineInput(
	UUID.fromString("f7c1d0f8-4464-4243-ab0b-bfb048e8b1a9"),
	"c_d",
	OperatorInput.NO_OPERATOR_ASSIGNED,
	OperationTime.notLimited(),
	nodeC,
	nodeD,
	1,
	lineType,
	Quantities.getQuantity(2.5, PowerSystemUnits.KILOMETRE),
	geoJsonReader.read("{\"type\":\"LineString\",\"coordinates\":[[7.411728,51.493918],[7.411497,51.494553]],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}") as LineString,
	OlmCharacteristicInput.CONSTANT_CHARACTERISTIC
	)
	LineInput lineCE = new LineInput(
	UUID.fromString("959d901a-8403-4b24-a99f-ea07a7c924a4"),
	"c_e",
	OperatorInput.NO_OPERATOR_ASSIGNED,
	OperationTime.notLimited(),
	nodeC,
	nodeE,
	1,
	lineType,
	Quantities.getQuantity(2.5, PowerSystemUnits.KILOMETRE),
	geoJsonReader.read("{\"type\":\"LineString\",\"coordinates\":[[7.411728,51.493918],[7.412838,51.494136]],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}") as LineString,
	OlmCharacteristicInput.CONSTANT_CHARACTERISTIC
	)
	LineInput lineEF = new LineInput(
	UUID.fromString("c709c1da-cab2-4ad3-8211-1fa765ec7c45"),
	"e_f",
	OperatorInput.NO_OPERATOR_ASSIGNED,
	OperationTime.notLimited(),
	nodeE,
	nodeF,
	1,
	lineType,
	Quantities.getQuantity(2.5, PowerSystemUnits.KILOMETRE),
	geoJsonReader.read("{\"type\":\"LineString\",\"coordinates\":[[7.412838,51.494136],[7.413761,51.494250]],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}") as LineString,
	OlmCharacteristicInput.CONSTANT_CHARACTERISTIC
	)
	LineInput lineEG = new LineInput(
	UUID.fromString("2fefea62-53c7-4923-97a0-f4e23b14143f"),
	"e_g",
	OperatorInput.NO_OPERATOR_ASSIGNED,
	OperationTime.notLimited(),
	nodeE,
	nodeG,
	1,
	lineType,
	Quantities.getQuantity(2.5, PowerSystemUnits.KILOMETRE),
	geoJsonReader.read("{\"type\":\"LineString\",\"coordinates\":[[7.412838,51.494136],[7.413123,51.494791]],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}") as LineString,
	OlmCharacteristicInput.CONSTANT_CHARACTERISTIC
	)

	/* Put everything together */
	JointGridContainer testGrid = new JointGridContainer(
	"lengthAdaptionTestGrid",
	new RawGridElements(
	[
		nodeA,
		nodeB,
		nodeC,
		nodeD,
		nodeE,
		nodeF,
		nodeG] as Set,
	[
		lineBC,
		lineCD,
		lineCE,
		lineEF,
		lineEG] as Set,
	[transformer] as Set,
	[] as Set,
	[] as Set,
	[] as Set
	),
	new SystemParticipants(
	[] as Set
	),
	new GraphicElements(
	[] as Set,
	[] as Set,
	)
	)
}