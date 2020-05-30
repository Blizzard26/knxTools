package org.knx.xml;

import java.util.Map;
import java.util.stream.Collectors;

import org.knx.xml.KnxDatapointTypeT.KnxDatapointSubtypes.KnxDatapointSubtype;
import org.openhab.support.knx2openhab.Tupel;

public class KnxMasterDataAccess {

	private KnxMasterDataT masterData;
	private Map<String, KnxFunctionTypeT> functionTypes;
	private Map<String, Tupel<KnxDatapointTypeT, KnxDatapointSubtype>> dataPointTypes;
	

	public KnxMasterDataAccess(KnxMasterDataT masterData) {
		this.masterData = masterData;
	}

	public String resolveFunctionType(String type) {
		if (type == null)
			return null;
		return getFunctionType(type).getText();
	}

	public KnxFunctionTypeT getFunctionType(String type) {
		if (type == null)
			return null;
		return getFunctionTypes().get(type);
	}

	public String resolveDatapointType(String dataPointType) {
		Tupel<KnxDatapointTypeT, KnxDatapointSubtype> dataPointSubType = getDataPointType(dataPointType);
		if (dataPointSubType != null) {
			return String.format("%1$d.%2$03d", dataPointSubType.getFirst().getNumber(),
					dataPointSubType.getSecond() != null ? dataPointSubType.getSecond().getNumber() : 0);
		}

		return null;
	}

	public Tupel<KnxDatapointTypeT, KnxDatapointSubtype> getDataPointType(String dataPointType) {
		if (dataPointType == null)
			return null;
		return getDataPointTypes().get(dataPointType);
	}
	
	private Map<String, KnxFunctionTypeT> getFunctionTypes() {
		if (functionTypes == null) {
			functionTypes = masterData.getFunctionTypes().getFunctionType().stream()
					.collect(Collectors.toMap(f -> f.getId(), f -> f));
		}
		return functionTypes;
	}

	private Map<String, Tupel<KnxDatapointTypeT, KnxDatapointSubtype>> getDataPointTypes() {
		if (dataPointTypes == null) {
			dataPointTypes = masterData.getDatapointTypes().getDatapointType().stream()
					.collect(Collectors.toMap(d -> d.getId(), d -> new Tupel<>(d, null)));
			dataPointTypes.putAll(masterData.getDatapointTypes().getDatapointType().stream()
					.flatMap(d -> d.getDatapointSubtypes().getDatapointSubtype().stream().map(ds -> new Tupel<>(d, ds)))
					.collect(Collectors.toMap(ds -> ds.getSecond().getId(), ds -> ds)));
		}
		return dataPointTypes;
	}
}
