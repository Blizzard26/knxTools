package org.openhab.support.knx2openhab.etsLoader;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.knx.KnxDatapointTypeT;
import org.knx.KnxDatapointTypeT.KnxDatapointSubtypes.KnxDatapointSubtype;
import org.knx.KnxFunctionExt;
import org.knx.KnxFunctionTypeT;
import org.knx.KnxGroupAddressExt;
import org.knx.KnxGroupAddressT;
import org.knx.KnxGroupAddressesT.KnxGroupRanges;
import org.knx.KnxGroupRangeT;
import org.knx.KnxLocationsT;
import org.knx.KnxMasterDataT;
import org.knx.KnxProjectT.KnxInstallations.KnxInstallation;
import org.knx.KnxSpaceT;
import org.openhab.support.knx2openhab.Tupel;

public class ETSUtils {
	public static String resolveFunctionType(KnxMasterDataT masterData, String type) {
		Optional<KnxFunctionTypeT> functionType = masterData.getFunctionTypes().getFunctionType().stream()
				.filter(ft -> ft.getId().equals(type)).findAny();
		return functionType.get().getText();
	}

	public static String resolveDatapointType(KnxMasterDataT masterData, String dataPointType) {
		if (dataPointType == null)
			return null;
		
		Optional<KnxDatapointTypeT> dtp = masterData.getDatapointTypes().getDatapointType().stream()
				.filter(dt -> dataPointType.equals(dt.getId())).findAny();
		if (dtp.isPresent()) {
			return dtp.get().getNumber() + ".000";
		}

		Optional<Tupel<KnxDatapointTypeT, KnxDatapointSubtype>> dataPointSubType = masterData.getDatapointTypes()
				.getDatapointType().stream()
				.flatMap(
						dt -> dt.getDatapointSubtypes().getDatapointSubtype().stream().map(dts -> new Tupel<>(dt, dts)))
				.filter(t -> t.getSecond().getId().equals(dataPointType)).findAny();

		if (dataPointSubType.isPresent()) {
			return String.format("%1$d.%2$03d", dataPointSubType.get().getFirst().getNumber(),
					dataPointSubType.get().getSecond().getNumber());
		}
		
		return null;
	}

	public static List<KnxGroupAddressExt> getGroupAddresses(KnxMasterDataT masterData,
			KnxInstallation knxInstallation) {
		KnxGroupRanges groupRanges = knxInstallation.getGroupAddresses().getGroupRanges();

		List<KnxGroupAddressExt> groupAddresses = groupRanges.getGroupRange().stream()
				.flatMap(r -> getGroupAddresses(masterData, r.getGroupRange()).stream()).collect(Collectors.toList());

		return groupAddresses;
	}

	private static List<KnxGroupAddressExt> getGroupAddresses(KnxMasterDataT masterData,
			List<KnxGroupRangeT> groupRange) {
		if (groupRange == null)
			return Collections.emptyList();

		List<KnxGroupAddressExt> groupAddresses = groupRange.stream()
				.flatMap(r -> r.getGroupAddress() != null
						? r.getGroupAddress().stream().map(g -> getGroupAddress(masterData, g))
						: Stream.empty())
				.collect(Collectors.toList());

		groupAddresses.addAll(groupRange.stream()
				.flatMap(r -> getGroupAddresses(masterData, r.getGroupRange()).stream()).collect(Collectors.toList()));

		return groupAddresses;
	}

	private static KnxGroupAddressExt getGroupAddress(KnxMasterDataT masterData, KnxGroupAddressT g) {
		return new KnxGroupAddressExt(g, resolveDatapointType(masterData, g.getDatapointType()));
	}

	public static List<KnxFunctionExt> getFunctions(KnxInstallation knxInstallation) {
		KnxLocationsT locations = knxInstallation.getLocations();
		return locations.getSpace().stream().flatMap(s -> getFunctions(s).stream()).collect(Collectors.toList());
	}

	private static List<KnxFunctionExt> getFunctions(KnxSpaceT space) {
		List<KnxFunctionExt> functions = space.getFunction().stream().map(f -> new KnxFunctionExt(space, f))
				.collect(Collectors.toList());
		functions.addAll(space.getSpace().stream().flatMap(s -> getFunctions(s).stream()).collect(Collectors.toList()));
		return functions;
	}
}
