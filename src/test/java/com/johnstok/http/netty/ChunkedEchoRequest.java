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

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.johnstok.http.Version;
import com.johnstok.http.async.Request;
import com.johnstok.http.async.Response;


/**
 * A request that echoes the incoming body to the response.
 *
 * @author Keith Webster Johnston.
 */
public class ChunkedEchoRequest
    implements
        Request {

    Response _response;
    boolean _complete = false;

    /** {@inheritDoc} */
    @Override
    public void onBegin(final Response response) {
        _response = response;
    }


    /** {@inheritDoc} */
    @Override
    public void onRequestLine(final String method,
                              final String uri,
                              final Version version) {
        _response.writeStatusLine(version, 200, "OK");
    }


    /** {@inheritDoc} */
    @Override
    public void onHeaders(final Map<String, List<String>> headers) {
        final Map<String, List<String>> responseHeaders =
            new HashMap<String, List<String>>();
        if (isChunked(headers)) {
            responseHeaders.put("Content-Length", headers.get("Content-Length"));
        }
        responseHeaders.put("Content-Type", headers.get("Content-Type"));
        responseHeaders.put("Transfer-Encoding", Collections.singletonList("chunked"));
        _response.writeHeaders(responseHeaders);
    }


    private boolean isChunked(final Map<String, List<String>> headers) {
        return headers.containsKey("Chunked") && headers.get("Transfer-Encoding").contains("chunked"); // FIXME: Move to TransferEncoding class; fix test for comma-separated header values.
    }


    /** {@inheritDoc} */
    @Override
    public void onBody(final ByteBuffer bytes) {
        for (final byte b : bytes.array()) {
            _response.writeBody(ByteBuffer.wrap(new byte[] {b}));
        }
    }


    /** {@inheritDoc} */
    @Override
    public void onEnd(final Map<String, List<String>> trailers) {
        _response.writeEnd(null);
        _complete = true;
    }


    public boolean isComplete() { return _complete; }
}
