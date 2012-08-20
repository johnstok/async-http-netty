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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.johnstok.http.async.Connection;
import com.johnstok.http.netty.test.SingletonRequestFactory;


/**
 * Non-functional tests for the Netty async-http implementation.
 *
 * @author Keith Webster Johnston.
 */
public class NonFunctionalTest extends AbstractServerTest<NettyServer> {

    /** Test. */
    @Test
    public void OneThousandConnections() throws Exception {

        // ARRANGE
        final int connCount = 1000;
        final CountDownLatch connLatch = new CountDownLatch(connCount);
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new SingletonRequestFactory(null),
            new Connection() {
                @Override
                public void onOpen() { connLatch.countDown(); }
            });

        // ACT
        final Socket[] connections = new Socket[connCount];
        for (int i=0; i<connections.length; i++) {
            connections[i] = new Socket(LOCALHOST, 4444);
        }

        // ASSERT
        assertTrue(
            "Failed to open simultaneous connections.",
            connLatch.await(1, TimeUnit.SECONDS));
        assertEquals(connCount, _server.getConnectionCount());
    }


    /** {@inheritDoc} */
    @Override
    protected NettyServer createServer() { return new NettyServer(); }
}
