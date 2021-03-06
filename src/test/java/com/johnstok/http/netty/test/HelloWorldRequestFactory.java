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
package com.johnstok.http.netty.test;

import com.johnstok.http.async.Request;
import com.johnstok.http.async.RequestFactory;

/**
 * Factory for {@link HelloWorldRequest} objects.
 *
 * @author Keith Webster Johnston.
 */
public final class HelloWorldRequestFactory
    implements
        RequestFactory {


    /** {@inheritDoc} */
    @Override
    public Request newInstance() {
        return new HelloWorldRequest();
    }
}
