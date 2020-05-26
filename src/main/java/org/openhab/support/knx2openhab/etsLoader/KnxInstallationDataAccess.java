package org.openhab.support.knx2openhab.etsLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.knx.KnxApplicationProgramT;
import org.knx.KnxComObjectInstanceRefT;
import org.knx.KnxComObjectRefT;
import org.knx.KnxComObjectT;
import org.knx.KnxDeviceInstanceT;
import org.knx.KnxDeviceInstanceT.KnxComObjectInstanceRefs;
import org.knx.KnxGroupAddressExt;
import org.knx.KnxGroupAddressT;
import org.knx.KnxGroupAddressesT.KnxGroupRanges;
import org.knx.KnxGroupRangeT;
import org.knx.KnxProjectT.KnxInstallations.KnxInstallation;

public class KnxInstallationDataAccess
{

    private final KnxInstallation installation;
    private Map<String, KnxGroupAddressExt> groupAddresses;
    private final KnxMasterDataAccess masterData;
    private final KnxManufacturerDataAccess manufacturerAccess;

    public KnxInstallationDataAccess(final KnxMasterDataAccess masterData,
            final KnxManufacturerDataAccess manufacturers, final KnxInstallation installation)
    {
        this.masterData = masterData;
        this.manufacturerAccess = manufacturers;
        this.installation = installation;

    }

    public KnxGroupAddressExt getGroupAddress(final String groupAddress)
    {
        if (groupAddresses == null)
        {
            groupAddresses = getGroupAddresses(installation).stream()
                    .collect(Collectors.toMap(KnxGroupAddressExt::getId, g -> g));
        }
        return groupAddresses.get(groupAddress);
    }

    public Map<String, KnxGroupAddressExt> getGroupAddresses()
    {
        if (groupAddresses == null)
        {
            groupAddresses = getGroupAddresses(installation).stream()
                    .collect(Collectors.toMap(KnxGroupAddressExt::getId, g -> g));
        }
        return groupAddresses;
    }

    private List<KnxGroupAddressExt> getGroupAddresses(final KnxInstallation knxInstallation)
    {
        KnxGroupRanges groupRanges = knxInstallation.getGroupAddresses().getGroupRanges();

        List<KnxGroupAddressExt> groupAddresses = groupRanges.getGroupRange().stream()
                .flatMap(r -> getGroupAddresses(r.getGroupRange()).stream()).collect(Collectors.toList());

        return groupAddresses;
    }

    private List<KnxGroupAddressExt> getGroupAddresses(final List<KnxGroupRangeT> groupRange)
    {
        if (groupRange == null)
            return Collections.emptyList();

        List<KnxGroupAddressExt> groupAddresses = groupRange.stream()
                .flatMap(r -> r.getGroupAddress() != null ? r.getGroupAddress().stream().map(this::getGroupAddress)
                        : Stream.empty())
                .collect(Collectors.toList());

        groupAddresses.addAll(groupRange.stream().flatMap(r -> getGroupAddresses(r.getGroupRange()).stream())
                .collect(Collectors.toList()));

        return groupAddresses;
    }

    private KnxGroupAddressExt getGroupAddress(final KnxGroupAddressT g)
    {
        return new KnxGroupAddressExt(g, masterData.resolveDatapointType(g.getDatapointType()));
    }

    public List<KnxComObjectInstanceRefT> getLinkedComObjects(final String groupAddressId)
    {
        String shortId = groupAddressId.substring(groupAddressId.lastIndexOf('_') + 1, groupAddressId.length());

        List<KnxComObjectInstanceRefT> comObjectRefs = installation.getTopology().getArea().stream()
                .flatMap(a -> a.getLine().stream()).flatMap(l -> l.getDeviceInstance().stream())
                .flatMap(d -> getComObjectRefs(d, shortId)).collect(Collectors.toList());

        return comObjectRefs;
    }

    private Stream<KnxComObjectInstanceRefT> getComObjectRefs(final KnxDeviceInstanceT device, final String shortId)
    {
        KnxComObjectInstanceRefs comObjectInstanceRefs = device.getComObjectInstanceRefs();
        if (comObjectInstanceRefs == null)
            return Stream.empty();
        return comObjectInstanceRefs.getComObjectInstanceRef().stream()
                .filter(c -> c.getLinks().stream().filter(s -> s.equals(shortId)).findAny().isPresent())
                .map(c -> fillInDeviceData(device, c));
    }

    private KnxComObjectInstanceRefT fillInDeviceData(final KnxDeviceInstanceT device,
            final KnxComObjectInstanceRefT comObjectInstanceRef)
    {
        KnxApplicationProgramT applicationProgram = manufacturerAccess
                .getDeviceByHardware2ProgramId(device.getHardware2ProgramRefId());
        String refId = applicationProgram.getId() + "_" + comObjectInstanceRef.getRefId();
        Optional<KnxComObjectRefT> comObjectRef = applicationProgram.getStatic().getComObjectRefs().getComObjectRef()
                .stream().filter(r -> r.getId().equals(refId)).findFirst();

        KnxComObjectT comObject = applicationProgram.getStatic().getComObjectTable().getComObject().stream()
                .filter(c -> c.getId().equals(comObjectRef.get().getRefId())).findFirst().get();

        if (comObjectInstanceRef.getReadFlag() == null)
        {
            comObjectInstanceRef.setReadFlag(comObject.getReadFlag());
        }

        if (comObjectInstanceRef.getWriteFlag() == null)
        {
            comObjectInstanceRef.setWriteFlag(comObject.getWriteFlag());
        }

        if (comObjectInstanceRef.getReadOnInitFlag() == null)
        {
            comObjectInstanceRef.setReadOnInitFlag(comObject.getReadOnInitFlag());
        }

        if (comObjectInstanceRef.getUpdateFlag() == null)
        {
            comObjectInstanceRef.setUpdateFlag(comObject.getUpdateFlag());
        }

        return comObjectInstanceRef;
    }

}
