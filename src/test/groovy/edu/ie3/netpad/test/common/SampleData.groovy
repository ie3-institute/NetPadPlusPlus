/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.netpad.test.common

import edu.ie3.datamodel.models.OperationTime
import edu.ie3.datamodel.models.StandardUnits
import edu.ie3.datamodel.models.input.NodeInput
import edu.ie3.datamodel.models.input.OperatorInput
import edu.ie3.datamodel.models.input.connector.LineInput
import edu.ie3.datamodel.models.input.connector.type.LineTypeInput
import edu.ie3.datamodel.models.input.system.characteristic.OlmCharacteristicInput
import edu.ie3.datamodel.models.voltagelevels.GermanVoltageLevelUtils
import edu.ie3.datamodel.utils.GridAndGeoUtils
import edu.ie3.util.geo.GeoUtils
import edu.ie3.util.quantities.PowerSystemUnits
import org.locationtech.jts.geom.Coordinate
import tech.units.indriya.quantity.Quantities

trait SampleData {

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

	LineInput testLine = new LineInput(
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
