/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.netpad.grid.controller

import edu.ie3.netpad.util.SampleGridFactory
import spock.lang.Specification


class GridControllerTest extends Specification {

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
		gridController.findSubGridUuid(validMapping, sampleGrid) == Optional.of(gridUuid)
	}
}
