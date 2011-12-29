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
import org.junit.Test;
import com.johnstok.http.netty.test.SingletonRequestFactory;


/**
 * Tests for the {@link NettyServer}.
 *
 * @author Keith Webster Johnston.
 */
public class NettyServerTest
    extends
        AbstractServerTest<NettyServer> {


    /** Test. */
    @Test
    public void listenOnNonListeningServerStartsListening() {

        // ARRANGE

        // ACT
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new SingletonRequestFactory(null));

        // ASSERT
        assertTrue(_server.isListening());
    }


    /** Test. */
    @Test
    public void closeOnListeningServerStopsListening() {

        // ARRANGE
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new SingletonRequestFactory(null));

        // ACT
        _server.close();

        // ASSERT
        assertFalse(_server.isListening());
    }


    /** Test. */
    @Test
    public void closeOnNonListeningServerThrowsException() {

        // ARRANGE

        // ACT
        try {
            _server.close();
            fail();

            // ASSERT
        } catch (final IllegalStateException ex) {
            // TODO: Test message?
        }
    }


    /** Test. */
    @Test
    public void listenOnListeningServerThrowsException() {

        // ARRANGE
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new SingletonRequestFactory(null));

        // ACT
        try {
            _server.listen(
                new InetSocketAddress(LOCALHOST, 4444),
                new SingletonRequestFactory(null));
            fail();

        // ASSERT
        } catch (final IllegalStateException ex) {
            // TODO: Test message?
        }
    }


    /** {@inheritDoc} */
    @Override
    protected NettyServer createServer() { return new NettyServer(); }
}
