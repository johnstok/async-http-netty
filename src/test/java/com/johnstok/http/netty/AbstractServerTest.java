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

import org.junit.After;
import org.junit.Before;
import com.johnstok.http.async.Server;


/**
 * Common methods for server tests.
 *
 * @param <T> The type of server used.
 *
 * @author Keith Webster Johnston.
 */
public abstract class AbstractServerTest<T extends Server> {

    protected static final String LOCALHOST = "localhost";         //$NON-NLS-1$
    protected T _server = createServer();


    /** Set up. */
    @Before
    public void setUp() {
        _server = createServer();
    }


    /** Tear down. */
    @After
    public void tearDown() {
        if (_server.isListening()) {
            _server.close();
        }
    }


    /**
     * Create a default server object.
     *
     * @return The async-http server.
     */
    protected abstract T createServer();
}
