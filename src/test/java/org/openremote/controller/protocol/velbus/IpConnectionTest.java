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

import org.junit.*;

/**
 * Test connection to an IP interface
 */
public class IpConnectionTest extends MockServerBaseTest {
    @Test
    public void severAvailableAtStartup() throws Exception {
        startServerAndAssertConnected(1);
    }

    @Test
    public void serverUnavailableAtStartup() throws InterruptedException {
        Assert.assertEquals(1, getCommandBuilder().getNetworks().size());
        VelbusNetwork network = getCommandBuilder().getNetworks().get(0);

        // Initialise the network so it tries to connect to the mock server
        network.initialise();

        // Give connection manager up to 5s to establish connection
        waitForConnection(network, 5);

        // Check we aren't connected
        Assert.assertFalse(getServer().isConnected());
        Assert.assertTrue(network.isInitialised());
        Assert.assertEquals(ConnectionStatus.DISCONNECTED, network.getConnection().getStatus());
        // Start the server and ensure connection is re-established
        startServerAndAssertConnected(VelbusConnectionManager.RECONNECTION_DELAY/1000);
    }
}
