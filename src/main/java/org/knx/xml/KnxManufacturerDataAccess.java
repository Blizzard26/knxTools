package org.knx.xml;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.knx.xml.KnxManufacturerDataT.KnxManufacturer;

public class KnxManufacturerDataAccess {

	private KnxManufacturerDataT manufacturerData;

	public KnxManufacturerDataAccess(KnxManufacturerDataT manufacturerData) {
		this.manufacturerData = manufacturerData;
	}

	public KnxApplicationProgramT getDeviceByHardware2ProgramId(String hardware2ProgramRefId) {

		for (KnxManufacturer manufacturer : manufacturerData.getManufacturer()) {
			Optional<KnxHardware2ProgramT> hardware = manufacturer.getHardware().getHardware().stream()
					.flatMap(h -> h.getHardware2Programs() != null ? h.getHardware2Programs().getHardware2Program().stream() : Stream.empty())
					.filter(h -> h.getId().equals(hardware2ProgramRefId)).findAny();
			
			if (!hardware.isPresent())
				continue;

			List<KnxApplicationProgramRefT> applicationProgramRef = hardware.get().getApplicationProgramRef();

			return getApplicationProgram(manufacturer, applicationProgramRef.get(0).getRefId());
		}

		return null;

	}

	private KnxApplicationProgramT getApplicationProgram(KnxManufacturer manufacturer, String refId) {
		return manufacturer.getApplicationPrograms().getApplicationProgram().stream()
				.filter(a -> a.getId().equals(refId)).findFirst().get();
	}

}
