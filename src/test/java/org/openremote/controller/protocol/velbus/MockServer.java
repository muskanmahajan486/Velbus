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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MockServer extends Thread {
    protected ServerSocket socket;
    protected Socket connection;
    protected DataOutputStream os;
    protected DataInputStream is;
    protected int port;
    protected boolean shutdown;

    public  MockServer(int port) throws IOException {
        super("MockServer");
        this.port = port;
    }

    @Override
    public void run() {
        synchronized (this) {
            this.notify();
        }
        try {
            while (!this.isInterrupted() && !shutdown) {
                if (connection == null || !connection.isConnected()) {
                    socket = new ServerSocket(port);
                    connection = socket.accept();
                    is = new DataInputStream(connection.getInputStream());
                    os = new DataOutputStream(connection.getOutputStream());
                }
                sleep(100);
            }
        } catch (Exception e) {
        }
    }


    public void writeBytes(byte[] bytes) {
        if (isConnected()) {
            try {
                os.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] readBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        try {
            while ((b = is.read()) != -1) {
                baos.write(b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public void shutdown() throws IOException {
        if (connection != null) {
            is.close();
            os.close();
            connection.close();
        }

        if (socket != null) {
            socket.close();
        }

        shutdown = true;
    }

    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }
}
