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

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import com.johnstok.http.Request;
import com.johnstok.http.RequestFactory;
import com.johnstok.http.Response;
import com.johnstok.http.Version;


/**
 * Netty handler for dispatching requests.
 *
 * @author Keith Webster Johnston.
 */
class AsyncHttpUpstreamHandler
    extends
        SimpleChannelUpstreamHandler{

    private final RequestFactory _requestFactory;


    /**
     * Constructor.
     *
     * @param requestFactory The factory for request objects.
     */
    AsyncHttpUpstreamHandler(final RequestFactory requestFactory) {
        _requestFactory = requestFactory;
    }


    /** {@inheritDoc} */
    @Override
    public void messageReceived(final ChannelHandlerContext ctx,
                                final MessageEvent me) throws Exception {
        try {
            final HttpRequest request = (HttpRequest) me.getMessage();
            final HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
            final Channel channel = me.getChannel();

            final Request req   = _requestFactory.newInstance();
            final Response resp = new NettyResponse(response, channel);

            req.onBegin(resp);
            req.onRequestLine(
                request.getMethod().toString(),
                request.getUri(),
                new Version(
                    request.getProtocolVersion().getMajorVersion(),
                    request.getProtocolVersion().getMinorVersion()));
            req.onHeaders(headersAsMap(request));
            req.onBody(ByteBuffer.wrap(request.getContent().array()));
            req.onEnd(null);

        } catch (final RuntimeException e) {
            // TODO Auto-generated catch block.
            e.printStackTrace();
            throw e;
        } finally {
            ctx.getChannel().close();
        }
    }


    private Map<String, List<String>> headersAsMap(final HttpRequest request) {
        final Map<String, List<String>> headers = new HashMap<String, List<String>>();
        for (final String name : request.getHeaderNames()) {
            headers.put(name, request.getHeaders(name));
        }
        return headers;
    }
}
