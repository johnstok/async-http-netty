/*-----------------------------------------------------------------------------
 * Copyright © 2011 Keith Webster Johnston.
 * All rights reserved.
 *
 * This file is part of async-http-netty.
 *
 * async-http-netty is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * async-http-netty is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with async-http-netty. If not, see <http://www.gnu.org/licenses/>.
 *---------------------------------------------------------------------------*/
package com.johnstok.http.netty;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import org.junit.Test;
import com.johnstok.http.Server;


/**
 * Non-functional tests for the Netty async-http implementation.
 *
 * @author Keith Webster Johnston.
 */
public class NonFunctionalTest extends AbstractServerTest {

    /** Test. */
    @Test
    public void TenKTest() throws Exception {

        // ARRANGE
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new HelloWorldRequestFactory());

        // ACT
        final Socket[] connections = new Socket[3];
        for (int i=0; i<connections.length; i++) {
            connections[i] = connectAndWrite();
        }

        // ASSERT
        for (final Socket connection : connections) {
            assertTrue(connection.isConnected());
            connection.close();
        }
    }


    private Socket connectAndWrite()
        throws UnknownHostException,
               IOException,
               UnsupportedEncodingException {
        final Socket s = new Socket(LOCALHOST, 4444);
        final OutputStream os = s.getOutputStream();
        os.write("GET / HTTP/1.1\r\n\r\n".getBytes("ASCII"));           //$NON-NLS-1$ //$NON-NLS-2$
        os.flush();
        return s;
    }


    /** {@inheritDoc} */
    @Override
    protected Server createServer() { return new NettyServer(); }
}
