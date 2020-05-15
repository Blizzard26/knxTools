package org.openhab.support.knx2openhab.etsLoader;

import java.util.Map;
import java.util.stream.Collectors;

import org.knx.KnxDatapointTypeT;
import org.knx.KnxDatapointTypeT.KnxDatapointSubtypes.KnxDatapointSubtype;
import org.knx.KnxFunctionTypeT;
import org.knx.KnxMasterDataT;
import org.openhab.support.knx2openhab.Tupel;

public class KnxMasterDataAccess {

	private KnxMasterDataT masterData;
	private Map<String, KnxFunctionTypeT> functionTypes;
	private Map<String, Tupel<KnxDatapointTypeT, KnxDatapointSubtype>> dataPointTypes;
	

	public KnxMasterDataAccess(KnxMasterDataT masterData) {
		this.masterData = masterData;
	}

	public String resolveFunctionType(String type) {

		if (functionTypes == null) {
			functionTypes = masterData.getFunctionTypes().getFunctionType().stream()
					.collect(Collectors.toMap(f -> f.getId(), f -> f));
		}

		return functionTypes.get(type).getText();
//		
//		Optional<KnxFunctionTypeT> functionType = masterData.getFunctionTypes().getFunctionType().stream()
//				.filter(ft -> ft.getId().equals(type)).findAny();
//		return functionType.get().getText();
	}

	public String resolveDatapointType(String dataPointType) {
		if (dataPointType == null)
			return null;

		if (dataPointTypes == null) {
			dataPointTypes = masterData.getDatapointTypes().getDatapointType().stream()
					.collect(Collectors.toMap(d -> d.getId(), d -> new Tupel<>(d, null)));
			dataPointTypes.putAll(masterData.getDatapointTypes().getDatapointType().stream()
					.flatMap(d -> d.getDatapointSubtypes().getDatapointSubtype().stream().map(ds -> new Tupel<>(d, ds)))
					.collect(Collectors.toMap(ds -> ds.getSecond().getId(), ds -> ds)));
		}

//		Optional<KnxDatapointTypeT> dtp = masterData.getDatapointTypes().getDatapointType().stream()
//				.filter(dt -> dataPointType.equals(dt.getId())).findAny();
		

//		Optional<Tupel<KnxDatapointTypeT, KnxDatapointSubtype>> dataPointSubType = masterData.getDatapointTypes()
//				.getDatapointType().stream()
//				.flatMap(
//						dt -> dt.getDatapointSubtypes().getDatapointSubtype().stream().map(dts -> new Tupel<>(dt, dts)))
//				.filter(t -> t.getSecond().getId().equals(dataPointType)).findAny();

		Tupel<KnxDatapointTypeT,KnxDatapointSubtype> dataPointSubType = dataPointTypes.get(dataPointType);
		if (dataPointSubType != null) {
			return String.format("%1$d.%2$03d", dataPointSubType.getFirst().getNumber(),
					dataPointSubType.getSecond() != null ? dataPointSubType.getSecond().getNumber() : 0);
		}

		return null;
	}
}
