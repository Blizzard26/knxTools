package org.knx;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.knx.xml.KNX;
import org.knx.xml.KnxGroupAddressT;
import org.knx.xml.KnxGroupAddressesT;
import org.knx.xml.KnxGroupAddressesT.KnxGroupRanges;
import org.knx.xml.KnxGroupRangeT;
import org.knx.xml.KnxProjectT;
import org.knx.xml.KnxProjectT.KnxInstallations;
import org.knx.xml.KnxProjectT.KnxInstallations.KnxInstallation;

class ETSLoaderTest
{

    @BeforeEach
    void setUp() throws Exception
    {
    }

    @AfterEach
    void tearDown() throws Exception
    {
    }

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
        // assertThat(knx.getCreatedBy(), is(equalTo("Test")));
        // assertThat(knx.getToolVersion(), is(equalTo("5")));

        assertThat(knx.getProject(), hasSize(1));

        KnxProjectT project = knx.getProject().get(0);
        assertProject(knx, project);

    }

    private void assertProject(final KNX knx, final KnxProjectT project)
    {
        assertThat(project.getParent(), equalTo(knx));

        assertThat(project.getId(), equalTo("P-045C"));
        assertThat(project.getProjectInformation(), is(notNullValue()));

        KnxInstallations installations = project.getInstallations();
        assertThat(installations, is(notNullValue()));
        assertThat(installations.getParent(), equalTo(project));
        assertThat(installations.getInstallation(), hasSize(1));

        KnxInstallation installation = installations.getInstallation().get(0);
        assertInstallation(installations, installation);
    }

    private void assertInstallation(final KnxInstallations installations, final KnxInstallation installation)
    {
        assertThat(installation.getParent(), equalTo(installations));

        assertThat(installation.getInstallationId(), is(equalTo(0)));
        assertThat(installation.getName(), is(equalTo("")));
        assertThat(installation.getGroupAddresses(), is(notNullValue()));
        assertThat(installation.getLocations(), is(notNullValue()));
        assertThat(installation.getTopology(), is(notNullValue()));

        KnxGroupAddressesT groupAddresses = installation.getGroupAddresses();
        assertThat(groupAddresses.getParent(), is(equalTo(installation)));
        assertThat(groupAddresses.getGroupRanges(), is(notNullValue()));

        KnxGroupRanges groupRanges = groupAddresses.getGroupRanges();
        assertThat(groupRanges.getParent(), is(equalTo(groupAddresses)));
        assertThat(groupRanges.getGroupRange(), hasSize(1));

        KnxGroupRangeT knxGroupRangeT = groupRanges.getGroupRange().get(0);
        assertThat(knxGroupRangeT.getParent(), is(equalTo(groupRanges)));
        assertThat(knxGroupRangeT.getName(), is(equalTo("New main group")));
        assertThat(knxGroupRangeT.getRangeStart(), is(equalTo(1)));
        assertThat(knxGroupRangeT.getRangeEnd(), is(equalTo(2047)));
        assertThat(knxGroupRangeT.getGroupAddress(), is(empty()));
        assertThat(knxGroupRangeT.getGroupRange(), is(notNullValue()));
        assertThat(knxGroupRangeT.getGroupRange(), is(hasSize(1)));

        KnxGroupRangeT knxGroupRangeT2 = knxGroupRangeT.getGroupRange().get(0);
        assertThat(knxGroupRangeT2.getParent(), is(equalTo(knxGroupRangeT)));
        assertThat(knxGroupRangeT2.getName(), is(equalTo("New middle group")));
        assertThat(knxGroupRangeT2.getRangeStart(), is(equalTo(1)));
        assertThat(knxGroupRangeT2.getRangeEnd(), is(equalTo(255)));
        assertThat(knxGroupRangeT2.getGroupAddress(), is(notNullValue()));
        assertThat(knxGroupRangeT2.getGroupRange(), is(empty()));
        assertThat(knxGroupRangeT2.getGroupAddress(), is(hasSize(19)));

        List<KnxGroupAddressT> groupAddressList = knxGroupRangeT2.getGroupAddress();
        KnxGroupAddressT groupAddress = groupAddressList.get(0);
        // assertThat(groupAddress.getName(), is(equalTo("L LR Switching")));
        assertThat(groupAddress.getAddress(), is(equalTo(1L)));
        // FIXME Mapping
        assertThat(groupAddress.getDatapointType(), is(notNullValue()));
        assertThat(groupAddress.getDescription(), is(equalTo("Living room Light")));
        // assertThat(groupAddress.getComment(), is(equalTo("Comment")));
        assertThat(groupAddress.getContext(), is(nullValue()));
    }

}
