/*
 * Copyright 2016, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller.protocol.velbus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Test connection to an IP interface
 */
public class MockServerBaseTest extends BaseTest {
    private MockServer server;
    private VelbusCommandBuilder builder;

    @Override
    VelbusCommandBuilder getCommandBuilder() {
        if (builder == null) {
            builder = new VelbusCommandBuilder(new String[] {"127.0.0.1"}, new String[] {"40000"});
        }

        return builder;
    }


    @Before
    public void setup() throws IOException {
        server = new MockServer(40000);
    }

    @After
    public void destroy() throws IOException {
        server.shutdown();
        server = null;
    }

    protected MockServer getServer() {
        return server;
    }

    protected void startServerAndAssertConnected(int waitSeconds) throws InterruptedException {
        getServer().start();
        Assert.assertEquals(1, getCommandBuilder().getNetworks().size());
        VelbusNetwork network = getCommandBuilder().getNetworks().get(0);

        // Initialise the network so it connects to the mock server
        network.initialise();

        // Give connection manager up to 1s to establish connection
        waitForConnection(network, waitSeconds);

        // Check we are connected
        Assert.assertTrue(getServer().isConnected());
        Assert.assertTrue(network.isInitialised());
        Assert.assertEquals(ConnectionStatus.CONNECTED, network.getConnection().getStatus());
    }

    protected void waitForConnection(VelbusNetwork network, int waitSeconds) throws InterruptedException {
        int counter = 0;
        while (counter < (waitSeconds*10) && !server.isConnected()) {
            counter++;
            Thread.sleep(100);
        }
    }
}
