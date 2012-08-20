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

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.johnstok.http.Version;
import com.johnstok.http.async.Request;
import com.johnstok.http.async.RequestFactory;
import com.johnstok.http.async.Response;
import com.johnstok.http.netty.NettyServer;


/**
 * TODO: Describe this classes responsibility.
 *
 * TODO: Add a description for this type.
 *
 * @author Keith Webster Johnston.
 */
public class RNG
    implements
        Request {

    private Response _response;


    /*
var http = require("http");
var url = require("url");
http.createServer(function(request, response) {

     response.writeHead(200, {"Content-Type": "text/plain"});
     var numInput = new Number((require('url').parse(request.url, true).query).number);
     var numOutput = new Number(Math.random() * numInput).toFixed(0);
     // in real world, for a user operation ~1s
     setTimeout(
     function(){
     response.write(numOutput);
     response.end();}
     ,
     1000);
}).listen(4444);
console.log("server starting...");
     */

    /** {@inheritDoc} */
    @Override
    public void onBegin(final Response response) {
        _response = response;
    }


    /** {@inheritDoc} */
    @Override
    public void onRequestLine(final String method, final String uri, final Version version) {}


    /** {@inheritDoc} */
    @Override
    public void onHeaders(final Map<String, List<String>> headers) {
        _response.writeStatusLine(new Version(1, 1), 200, "OK");
        final HashMap<String, List<String>> respHeaders = new HashMap<String, List<String>>();
        respHeaders.put("Content-Type", Collections.singletonList("text/plain"));
        _response.writeHeaders(respHeaders);
        try {
            Thread.sleep(1000);
            final byte[] body =
                String.valueOf(Math.round(Math.random()*Integer.valueOf("1")))
                      .getBytes("UTF-8");
            _response.writeBody(ByteBuffer.wrap(body));
            _response.writeEnd(null);
        } catch (final NumberFormatException e) {
            throw new RuntimeException(e);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (final InterruptedException e) {

            throw new RuntimeException(e);
        }
    }


    /** {@inheritDoc} */
    @Override
    public void onBody(final ByteBuffer bytes) {}


    /** {@inheritDoc} */
    @Override
    public void onEnd(final Map<String, List<String>> trailers) {}


    public static void main(final String[] args) {
        new NettyServer().listen(
            new InetSocketAddress("localhost", 4444),
            new RequestFactory() {
                @Override
                public Request newInstance() {
                    return new RNG();
                }
            });
    }
}
