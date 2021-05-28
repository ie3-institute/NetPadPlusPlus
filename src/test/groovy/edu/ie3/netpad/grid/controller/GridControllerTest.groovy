/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.netpad.grid.controller

import edu.ie3.netpad.test.common.SampleData
import edu.ie3.netpad.util.SampleGridFactory
import spock.lang.Specification

class GridControllerTest extends Specification implements SampleData {

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
}
