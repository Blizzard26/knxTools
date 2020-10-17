package org.knx;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.knx.xml.KNX;
import org.knx.xml.KnxDatapointTypeT.KnxDatapointSubtypes.KnxDatapointSubtype;
import org.knx.xml.KnxFunctionT;
import org.knx.xml.KnxGroupAddressT;
import org.knx.xml.KnxGroupAddressesT;
import org.knx.xml.KnxGroupAddressesT.KnxGroupRanges;
import org.knx.xml.KnxGroupRangeT;
import org.knx.xml.KnxLocationsT;
import org.knx.xml.KnxProjectT;
import org.knx.xml.KnxProjectT.KnxInstallations;
import org.knx.xml.KnxProjectT.KnxInstallations.KnxInstallation;
import org.knx.xml.KnxSpaceT;

class ETSLoaderTest
{

    @Test
    void testUnencrypted()
    {
        // GIVEN
        File projectFile = new File("example/ExampleProject.knxproj");
        assertThat(projectFile, is(FunctionalMatcher.isTrue(File::exists, "existing file")));

        ETSLoader loader = new ETSLoader();

        // WHEN
        KNX knx = loader.load(projectFile, Optional.empty());

        // THEN
        assertThat(knx, is(notNullValue()));
        assertThat(knx.getMasterData(), is(notNullValue()));
        assertThat(knx.getManufacturerData(), is(notNullValue()));
        assertThat(knx.getProject(), is(not(empty())));
        assertThat(knx.getProject(), hasSize(1));

        KnxProjectT project = knx.getProject().get(0);
        assertProject(knx, project);

    }

    private void assertProject(final KNX knx, final KnxProjectT project)
    {
        assertThat(project.getParent(), sameInstance(knx));

        assertThat(project.getId(), equalTo("P-045C"));
        assertThat(project.getProjectInformation(), is(notNullValue()));

        KnxInstallations installations = project.getInstallations();
        assertThat(installations, is(notNullValue()));
        assertThat(installations.getParent(), sameInstance(project));
        assertThat(installations.getInstallation(), hasSize(1));

        KnxInstallation installation = installations.getInstallation().get(0);
        assertInstallation(installations, installation);
    }

    private void assertInstallation(final KnxInstallations installations, final KnxInstallation installation)
    {
        assertThat(installation.getParent(), sameInstance(installations));

        assertThat(installation.getInstallationId(), is(equalTo(0)));
        assertThat(installation.getName(), is(equalTo("")));
        assertThat(installation.getGroupAddresses(), is(notNullValue()));
        assertThat(installation.getLocations(), is(notNullValue()));
        assertThat(installation.getTopology(), is(notNullValue()));

        assertGroupAddresses(installation);
        assertLocations(installation.getLocations());
    }

    private void assertLocations(final KnxLocationsT locations)
    {
        List<KnxSpaceT> spaces = locations.getSpace();
        assertThat(spaces, hasSize(1));
        KnxSpaceT space = spaces.get(0);
        assertThat(space.getName(), is(equalTo("DemoProject")));
        assertThat(space.getFunction(), hasSize(0));

        spaces = space.getSpace();
        assertThat(spaces, hasSize(1));
        space = spaces.get(0);
        assertThat(space.getName(), is(equalTo("Building")));
        assertThat(space.getFunction(), hasSize(0));

        spaces = space.getSpace();
        assertThat(spaces, hasSize(1));
        space = spaces.get(0);
        assertThat(space.getName(), is(equalTo("First Floor")));
        assertThat(space.getFunction(), hasSize(0));

        spaces = space.getSpace();
        assertThat(spaces, hasSize(2));
        space = spaces.get(0);
        assertThat(space.getName(), is(equalTo("Living room")));
        assertThat(space.getFunction(), hasSize(5));
        assertFunctions(space);

        space = spaces.get(1);
        assertThat(space.getName(), is(equalTo("Utility room")));
        assertThat(space.getFunction(), hasSize(0));

    }

    private void assertFunctions(final KnxSpaceT space)
    {
        List<KnxFunctionT> functions = space.getFunction();
        KnxFunctionT function = functions.get(0);
        assertThat(function.getParent(), is(sameInstance(space)));
        assertThat(function.getName(), is(equalTo("Light")));
        assertThat(function.getNumber(), is(equalTo("L Living Room Lights 2")));
        assertThat(function.getType(), is(equalTo("FT-1")));

        function = functions.get(1);
        assertThat(function.getParent(), is(sameInstance(space)));
        assertThat(function.getName(), is(equalTo("Dimmable light")));
        assertThat(function.getNumber(), is(equalTo("LD Living Room Lights 1")));
        assertThat(function.getType(), is(equalTo("FT-6")));

        function = functions.get(2);
        assertThat(function.getParent(), is(sameInstance(space)));
        assertThat(function.getName(), is(equalTo("Rollershutter")));
        assertThat(function.getNumber(), is(equalTo("R Rollershutter")));
        assertThat(function.getType(), is(equalTo("FT-7")));
    }

    private void assertGroupAddresses(final KnxInstallation installation)
    {
        KnxGroupAddressesT groupAddresses = installation.getGroupAddresses();
        assertThat(groupAddresses.getParent(), is(sameInstance(installation)));
        assertThat(groupAddresses.getGroupRanges(), is(notNullValue()));

        KnxGroupRanges groupRanges = groupAddresses.getGroupRanges();
        assertThat(groupRanges.getParent(), is(sameInstance(groupAddresses)));
        assertThat(groupRanges.getGroupRange(), hasSize(1));

        KnxGroupRangeT knxGroupRangeT = groupRanges.getGroupRange().get(0);
        assertThat(knxGroupRangeT.getParent(), is(sameInstance(groupRanges)));
        assertThat(knxGroupRangeT.getName(), is(equalTo("New main group")));
        assertThat(knxGroupRangeT.getRangeStart(), is(equalTo(1)));
        assertThat(knxGroupRangeT.getRangeEnd(), is(equalTo(2047)));
        assertThat(knxGroupRangeT.getGroupAddress(), is(empty()));
        assertThat(knxGroupRangeT.getGroupRange(), is(notNullValue()));
        assertThat(knxGroupRangeT.getGroupRange(), is(hasSize(1)));

        KnxGroupRangeT knxGroupRangeT2 = knxGroupRangeT.getGroupRange().get(0);
        assertThat(knxGroupRangeT2.getParent(), is(sameInstance(knxGroupRangeT)));
        assertThat(knxGroupRangeT2.getName(), is(equalTo("New middle group")));
        assertThat(knxGroupRangeT2.getRangeStart(), is(equalTo(1)));
        assertThat(knxGroupRangeT2.getRangeEnd(), is(equalTo(255)));
        assertThat(knxGroupRangeT2.getGroupAddress(), is(notNullValue()));
        assertThat(knxGroupRangeT2.getGroupRange(), is(empty()));
        assertThat(knxGroupRangeT2.getGroupAddress(), is(hasSize(19)));

        List<KnxGroupAddressT> groupAddressList = knxGroupRangeT2.getGroupAddress();
        KnxGroupAddressT groupAddress = groupAddressList.get(0);
        assertThat(groupAddress.getName(), is(equalTo("L LR Switching")));
        assertThat(groupAddress.getAddress(), is(equalTo(1L)));
        assertThat(groupAddress.getDatapointType(), is(Matchers.instanceOf(KnxDatapointSubtype.class)));
        assertThat(groupAddress.getDescription(), is(equalTo("Living room Light")));
        assertThat(groupAddress.getComment(), is(notNullValue())); // RTF
        assertThat(groupAddress.getContext(), is(nullValue()));

        groupAddress = groupAddressList.get(1);
        assertThat(groupAddress.getName(), is(equalTo("L LR Status")));
        assertThat(groupAddress.getAddress(), is(equalTo(2L)));
        assertThat(groupAddress.getDatapointType(), is(Matchers.instanceOf(KnxDatapointSubtype.class)));
        assertThat(groupAddress.getDescription(), is(equalTo("Living room Light")));
        assertThat(groupAddress.getComment(), is(nullValue()));
        assertThat(groupAddress.getContext(), is(nullValue()));

        groupAddress = groupAddressList.get(2);
        assertThat(groupAddress.getName(), is(equalTo("LD LR Switching")));
        assertThat(groupAddress.getAddress(), is(equalTo(3L)));
        assertThat(groupAddress.getDatapointType(), is(Matchers.instanceOf(KnxDatapointSubtype.class)));
        assertThat(groupAddress.getDescription(), is(equalTo("Living room Dimmable light")));
        assertThat(groupAddress.getComment(), is(notNullValue())); // RTF
        assertThat(groupAddress.getContext(), is(nullValue()));
    }

}
