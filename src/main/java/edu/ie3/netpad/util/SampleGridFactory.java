/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.netpad.util;

import static edu.ie3.util.quantities.PowerSystemUnits.*;
// import static tec.uom.se.unit.Units.AMPERE;

import edu.ie3.datamodel.exceptions.ParsingException;
import edu.ie3.datamodel.models.BdewLoadProfile;
import edu.ie3.datamodel.models.OperationTime;
import edu.ie3.datamodel.models.StandardLoadProfile;
import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.input.OperatorInput;
import edu.ie3.datamodel.models.input.connector.LineInput;
import edu.ie3.datamodel.models.input.connector.Transformer2WInput;
import edu.ie3.datamodel.models.input.connector.type.LineTypeInput;
import edu.ie3.datamodel.models.input.connector.type.Transformer2WTypeInput;
import edu.ie3.datamodel.models.input.container.GraphicElements;
import edu.ie3.datamodel.models.input.container.JointGridContainer;
import edu.ie3.datamodel.models.input.container.RawGridElements;
import edu.ie3.datamodel.models.input.container.SystemParticipants;
import edu.ie3.datamodel.models.input.system.EvcsInput;
import edu.ie3.datamodel.models.input.system.LoadInput;
import edu.ie3.datamodel.models.input.system.PvInput;
import edu.ie3.datamodel.models.input.system.StorageInput;
import edu.ie3.datamodel.models.input.system.characteristic.CosPhiFixed;
import edu.ie3.datamodel.models.input.system.characteristic.OlmCharacteristicInput;
import edu.ie3.datamodel.models.input.system.type.StorageTypeInput;
import edu.ie3.datamodel.models.input.system.type.chargingpoint.ChargingPointTypeUtils;
import edu.ie3.datamodel.models.voltagelevels.GermanVoltageLevelUtils;
import edu.ie3.datamodel.utils.GridAndGeoUtils;
import edu.ie3.util.TimeUtil;
import edu.ie3.util.quantities.PowerSystemUnits;
import edu.ie3.util.quantities.interfaces.Currency;
import edu.ie3.util.quantities.interfaces.DimensionlessRate;
import edu.ie3.util.quantities.interfaces.EnergyPrice;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.measure.MetricPrefix;
import javax.measure.quantity.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import tech.units.indriya.ComparableQuantity;
import tech.units.indriya.quantity.Quantities;

/**
 * //ToDo: Class Description
 *
 * @version 0.1
 * @since 23.05.20
 */
public class SampleGridFactory {

  public static JointGridContainer sampleJointGrid() throws ParseException, ParsingException {

    RawGridElements rawGridElements = jointSampleRawGridElements();

    return new JointGridContainer(
        "sampleGrid",
        rawGridElements,
        systemParticipants(rawGridElements),
        new GraphicElements(Collections.emptySet()));
  }

  private static SystemParticipants systemParticipants(RawGridElements rawGridElements)
      throws ParsingException {

    NodeInput participantNode =
        rawGridElements.getNodes().stream()
            . // todo JH make this nice
            filter(node -> node.getId().equalsIgnoreCase("nodeA"))
            .collect(Collectors.toList())
            .get(0);

    // general participant data
    final OperationTime operationTime =
        OperationTime.builder()
            .withStart(TimeUtil.withDefaults.toZonedDateTime("2020-03-24 15:11:31"))
            .withEnd(TimeUtil.withDefaults.toZonedDateTime("2020-03-25 15:11:31"))
            .build();
    final OperatorInput operator =
        new OperatorInput(UUID.fromString("8f9682df-0744-4b58-a122-f0dc730f6510"), "TestOperator");

    // general type data
    final CosPhiFixed cosPhiFixed;

    cosPhiFixed = new CosPhiFixed("cosPhiFixed:{(0.0,0.95)}");

    final ComparableQuantity<Power> sRated = Quantities.getQuantity(25d, KILOVOLTAMPERE);
    final double cosPhiRated = 0.95;
    final UUID typeUuid = UUID.fromString("5ebd8f7e-dedb-4017-bb86-6373c4b68eb8");
    final ComparableQuantity<Currency> capex = Quantities.getQuantity(100d, EURO);
    final ComparableQuantity<EnergyPrice> opex = Quantities.getQuantity(50d, EURO_PER_MEGAWATTHOUR);
    final ComparableQuantity<Dimensionless> etaConv = Quantities.getQuantity(98d, PERCENT);

    final double albedo = 0.20000000298023224;
    final ComparableQuantity<Angle> azimuth =
        Quantities.getQuantity(-8.926613807678223, DEGREE_GEOM);
    final ComparableQuantity<Angle> height = Quantities.getQuantity(41.01871871948242, DEGREE_GEOM);

    double kT = 1;
    double kG = 0.8999999761581421;
    final PvInput pvInput =
        new PvInput(
            UUID.fromString("d56f15b7-8293-4b98-b5bd-58f6273ce229"),
            "test_pvInput",
            operator,
            operationTime,
            participantNode,
            cosPhiFixed,
            albedo,
            azimuth,
            etaConv,
            height,
            kG,
            kT,
            false,
            sRated,
            cosPhiRated);

    // Load
    final ComparableQuantity<Energy> eConsAnnual = Quantities.getQuantity(4000, KILOWATTHOUR);
    final StandardLoadProfile standardLoadProfile = BdewLoadProfile.H0;
    final LoadInput loadInput =
        new LoadInput(
            UUID.fromString("eaf77f7e-9001-479f-94ca-7fb657766f5f"),
            "test_loadInput",
            operator,
            operationTime,
            participantNode,
            cosPhiFixed,
            standardLoadProfile,
            false,
            eConsAnnual,
            sRated,
            cosPhiRated);

    // Evcs
    final EvcsInput evcsInput =
        new EvcsInput(
            UUID.fromString("d56f15b7-8293-4b98-b5bd-58f6273ce239"),
            "test_pvInput",
            operator,
            operationTime,
            participantNode,
            cosPhiFixed,
            ChargingPointTypeUtils.HouseholdSocket,
            2,
            cosPhiRated);

    final LoadInput loadInput1 =
        new LoadInput(
            UUID.fromString("eaf77f7e-9001-479f-94ca-7fb657766f6f"),
            "test_loadInput1",
            operator,
            operationTime,
            participantNode,
            cosPhiFixed,
            standardLoadProfile,
            false,
            eConsAnnual,
            sRated,
            cosPhiRated);

    // Storage
    final ComparableQuantity<Energy> eStorage = Quantities.getQuantity(100, KILOWATTHOUR);
    final ComparableQuantity<Power> pMax = Quantities.getQuantity(15, KILOWATT);
    final ComparableQuantity<Dimensionless> eta = Quantities.getQuantity(95, PERCENT);
    final ComparableQuantity<Dimensionless> dod = Quantities.getQuantity(10, PERCENT);
    final ComparableQuantity<DimensionlessRate> cpRate = Quantities.getQuantity(1, PU_PER_HOUR);
    final ComparableQuantity<Time> lifeTime = Quantities.getQuantity(20, YEAR);
    final int lifeCycle = 100;
    final StorageTypeInput storageTypeInput =
        new StorageTypeInput(
            typeUuid,
            "test_storageTypeInput",
            capex,
            opex,
            eStorage,
            sRated,
            cosPhiRated,
            pMax,
            cpRate,
            eta,
            dod,
            lifeTime,
            lifeCycle);
    final StorageInput storageInput =
        new StorageInput(
            UUID.fromString("06b58276-8350-40fb-86c0-2414aa4a0452"),
            "test_storageInput",
            operator,
            operationTime,
            participantNode,
            cosPhiFixed,
            storageTypeInput);

    return new SystemParticipants(
        Collections.emptySet(),
        Collections.emptySet(),
        Collections.singleton(evcsInput),
        Collections.emptySet(),
        Collections.emptySet(),
        Collections.emptySet(),
        new HashSet<>(Arrays.asList(loadInput, loadInput1)),
        Collections.singleton(pvInput),
        Collections.singleton(storageInput),
        Collections.emptySet());
  }

  private static RawGridElements jointSampleRawGridElements() throws ParseException {

    GeoJsonReader geoJsonReader = new GeoJsonReader();

    final String NodePosition =
        "{ \"type\": \"Point\", \"coordinates\": [6.592276813887139, 49.37770599548332] }";

    // LV
    NodeInput nodeA =
        new NodeInput(
            UUID.fromString("4ca90220-74c2-4369-9afa-a18bf068840d"),
            "nodeA",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            Quantities.getQuantity(1, PowerSystemUnits.PU),
            false,
            (Point) geoJsonReader.read(NodePosition),
            GermanVoltageLevelUtils.LV,
            1);

    NodeInput nodeB =
        new NodeInput(
            UUID.fromString("47d29df0-ba2d-4d23-8e75-c82229c5c758"),
            "nodeB",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            Quantities.getQuantity(1, PowerSystemUnits.PU),
            false,
            (Point)
                geoJsonReader.read(
                    "{ \"type\": \"Point\", \"coordinates\": [6.593358228545043, 49.377139554965595] }"),
            GermanVoltageLevelUtils.LV,
            1);

    NodeInput nodeC =
        new NodeInput(
            UUID.fromString("bd837a25-58f3-44ac-aa90-c6b6e3cd91b2"),
            "nodeC",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            Quantities.getQuantity(1, PowerSystemUnits.PU),
            false,
            (Point)
                geoJsonReader.read(
                    "{ \"type\": \"Point\", \"coordinates\": [6.592850044965246, 49.37684839141148] }"),
            GermanVoltageLevelUtils.LV,
            1);

    LineTypeInput lv_lineType =
        new LineTypeInput(
            UUID.fromString("3bed3eb3-9790-4874-89b5-a5434d408088"),
            "lineType_AtoB",
            Quantities.getQuantity(191.636993408203, PowerSystemUnits.SIEMENS_PER_KILOMETRE),
            Quantities.getQuantity(0, PowerSystemUnits.SIEMENS_PER_KILOMETRE),
            Quantities.getQuantity(0.253899991512299, PowerSystemUnits.OHM_PER_KILOMETRE),
            Quantities.getQuantity(0.0691149979829788, PowerSystemUnits.OHM_PER_KILOMETRE),
            Quantities.getQuantity(265, AMPERE),
            Quantities.getQuantity(0.4, KILOVOLT));

    LineInput lineAB =
        new LineInput(
            UUID.fromString("92ec3bcf-1777-4d38-af67-0bf7c9fa73c7"),
            "lineAtoB",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            nodeA,
            nodeB,
            1,
            lv_lineType,
            GridAndGeoUtils.distanceBetweenNodes(nodeA, nodeB),
            GridAndGeoUtils.buildSafeLineStringBetweenNodes(nodeA, nodeB),
            OlmCharacteristicInput.CONSTANT_CHARACTERISTIC);

    LineInput lineAC =
        new LineInput(
            UUID.fromString("93ec3bcf-1777-4d38-af67-0bf7c9fa73c7"),
            "lineAtoC",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            nodeA,
            nodeC,
            1,
            lv_lineType,
            GridAndGeoUtils.distanceBetweenNodes(nodeA, nodeC),
            GridAndGeoUtils.buildSafeLineStringBetweenNodes(nodeA, nodeC),
            OlmCharacteristicInput.CONSTANT_CHARACTERISTIC);

    LineInput lineBC =
        new LineInput(
            UUID.fromString("94ec3bcf-1777-4d38-af67-0bf7c9fa73c7"),
            "lineBtoC",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            nodeB,
            nodeC,
            1,
            lv_lineType,
            GridAndGeoUtils.distanceBetweenNodes(nodeB, nodeC),
            GridAndGeoUtils.buildSafeLineStringBetweenNodes(nodeB, nodeC),
            OlmCharacteristicInput.CONSTANT_CHARACTERISTIC);

    // MV
    NodeInput nodeD =
        new NodeInput(
            UUID.fromString("09aec636-791b-45aa-b981-b14edf171c4c"),
            "nodeD",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            Quantities.getQuantity(1, PowerSystemUnits.PU),
            false,
            (Point) geoJsonReader.read(NodePosition),
            GermanVoltageLevelUtils.MV_10KV,
            2);

    NodeInput nodeE =
        new NodeInput(
            UUID.fromString("10aec636-791b-45aa-b981-b14edf171c4c"),
            "nodeE",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            Quantities.getQuantity(1, PowerSystemUnits.PU),
            false,
            (Point)
                geoJsonReader.read(
                    "{ \"type\": \"Point\", \"coordinates\": [6.572286813887139, 49.39770699548332] }"),
            GermanVoltageLevelUtils.MV_10KV,
            2);

    NodeInput nodeF =
        new NodeInput(
            UUID.fromString("11aec636-791b-45aa-b981-b14edf171c4c"),
            "nodeF",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            Quantities.getQuantity(1, PowerSystemUnits.PU),
            false,
            (Point)
                geoJsonReader.read(
                    "{ \"type\": \"Point\", \"coordinates\": [6.572286813887139, 49.38770799548332] }"),
            GermanVoltageLevelUtils.MV_10KV,
            2);

    NodeInput nodeG =
        new NodeInput(
            UUID.fromString("12aec637-791b-45aa-b981-b14edf171c4c"),
            "nodeG",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            Quantities.getQuantity(1, PowerSystemUnits.PU),
            false,
            (Point) geoJsonReader.read(NodePosition),
            GermanVoltageLevelUtils.EHV_220KV,
            4);

    LineTypeInput mv_lineType =
        new LineTypeInput(
            UUID.fromString("4bed3eb3-9790-4874-89b5-a5434d408088"),
            "lineType_AtoB",
            Quantities.getQuantity(191.636993408203, PowerSystemUnits.SIEMENS_PER_KILOMETRE),
            Quantities.getQuantity(0, PowerSystemUnits.SIEMENS_PER_KILOMETRE),
            Quantities.getQuantity(0.207000002264977, PowerSystemUnits.OHM_PER_KILOMETRE),
            Quantities.getQuantity(0.0691149979829788, PowerSystemUnits.OHM_PER_KILOMETRE),
            Quantities.getQuantity(300, AMPERE),
            Quantities.getQuantity(10, KILOVOLT));

    LineInput lineDE =
        new LineInput(
            UUID.fromString("99ec3bcf-1777-4d38-af67-0bf7c9fa73c7"),
            "lineDtoE",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            nodeD,
            nodeE,
            1,
            mv_lineType,
            GridAndGeoUtils.distanceBetweenNodes(nodeD, nodeE),
            GridAndGeoUtils.buildSafeLineStringBetweenNodes(nodeD, nodeE),
            OlmCharacteristicInput.CONSTANT_CHARACTERISTIC);

    LineInput lineEF =
        new LineInput(
            UUID.fromString("99fc3bcf-1777-4d38-af67-0bf7c9fa73c7"),
            "lineEtoF",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            nodeE,
            nodeF,
            1,
            mv_lineType,
            GridAndGeoUtils.distanceBetweenNodes(nodeE, nodeF),
            GridAndGeoUtils.buildSafeLineStringBetweenNodes(nodeE, nodeF),
            OlmCharacteristicInput.CONSTANT_CHARACTERISTIC);

    LineInput lineDF =
        new LineInput(
            UUID.fromString("60ec3bcf-1777-4d38-af67-0bf7c9fa73c7"),
            "lineDtoF",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            nodeD,
            nodeF,
            1,
            mv_lineType,
            GridAndGeoUtils.distanceBetweenNodes(nodeD, nodeF),
            GridAndGeoUtils.buildSafeLineStringBetweenNodes(nodeD, nodeF),
            OlmCharacteristicInput.CONSTANT_CHARACTERISTIC);

    // transformers

    Transformer2WTypeInput transformerType_LV_MV_10KV =
        new Transformer2WTypeInput(
            UUID.fromString("08559390-d7c0-4427-a2dc-97ba312ae0ac"),
            "MS-NS_1",
            Quantities.getQuantity(10.078, OHM),
            Quantities.getQuantity(23.312, OHM),
            Quantities.getQuantity(630d, KILOVOLTAMPERE),
            Quantities.getQuantity(20d, KILOVOLT),
            Quantities.getQuantity(0.4, KILOVOLT),
            Quantities.getQuantity(0d, MetricPrefix.MICRO(SIEMENS)),
            Quantities.getQuantity(0d, MetricPrefix.MICRO(SIEMENS)),
            Quantities.getQuantity(0.5, PERCENT),
            Quantities.getQuantity(0d, DEGREE_GEOM),
            false,
            0,
            -10,
            10);

    Transformer2WInput transformerDtoA =
        new Transformer2WInput(
            UUID.fromString("58247de7-e297-4d9b-a5e4-b662c058c655"),
            "transformerAtoD",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            nodeD,
            nodeA,
            1,
            transformerType_LV_MV_10KV,
            0,
            false);

    Transformer2WInput transformerGtoD =
        new Transformer2WInput(
            UUID.fromString("58257de7-f297-4d9b-a5e4-b662c058c655"),
            "transformerAtoD",
            OperatorInput.NO_OPERATOR_ASSIGNED,
            OperationTime.notLimited(),
            nodeG,
            nodeD,
            1,
            transformerType_LV_MV_10KV,
            0,
            false);

    return new RawGridElements(
        new HashSet<>(Arrays.asList(nodeA, nodeB, nodeC, nodeD, nodeE, nodeF, nodeG)),
        new HashSet<>(Arrays.asList(lineAB, lineAC, lineBC, lineDE, lineDF, lineEF)),
        new HashSet<>(Arrays.asList(transformerDtoA, transformerGtoD)),
        Collections.emptySet(),
        Collections.emptySet(),
        Collections.emptySet());
  }
}
