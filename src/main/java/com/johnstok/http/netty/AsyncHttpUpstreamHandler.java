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

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import com.johnstok.http.Version;
import com.johnstok.http.async.Request;
import com.johnstok.http.async.RequestFactory;
import com.johnstok.http.async.Response;


/**
 * Netty handler for dispatching requests.
 *
 * @author Keith Webster Johnston.
 */
class AsyncHttpUpstreamHandler
    extends
        SimpleChannelUpstreamHandler {

    public static Logger logger =
        Logger.getLogger(AsyncHttpUpstreamHandler.class.getName());

    private final RequestFactory _requestFactory;
    private Request _req;


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
                                final MessageEvent me) {

        final Object o = me.getMessage();
        logger.info(o.toString());

        if (!ctx.getChannel().isOpen()) { discard(o); return; }

        try {
            if (o instanceof HttpRequest) {
                final HttpRequest request = (HttpRequest) o;
                final HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
                final Channel channel = me.getChannel();

                _req = _requestFactory.newInstance();
                final Response resp = new NettyResponse(response, channel);
                final String method = request.getMethod().toString();
                final String uri = request.getUri();
                final Version version =
                    new Version(
                        request.getProtocolVersion().getMajorVersion(),
                        request.getProtocolVersion().getMinorVersion());
                try {
                    _req.onBegin(resp);
                    _req.onRequestLine(method, uri, version);
                    _req.onHeaders(headersAsMap(request));
                } catch (final Exception e) {
                    ctx.getChannel().close();
                    logger.log(Level.WARNING, "Request threw exception", e);
                    return;
                }

                if (!request.isChunked()) { // No additional chunks to come
                    try {
                        _req.onBody(ByteBuffer.wrap(request.getContent().array()));
                        _req.onEnd(null);
                    } catch (final Exception e) {
                        logger.log(Level.WARNING, "Request threw exception", e);
                    } finally {
                        ctx.getChannel().close();
                        logger.info("Channel closed");
                    }
                }

            } else if (o instanceof HttpChunk) {
                final HttpChunk chunk = (HttpChunk) o;
                if (chunk.isLast()) {
                    try {
                        if (chunk instanceof HttpChunkTrailer) {
                            final HttpChunkTrailer trailer = (HttpChunkTrailer) chunk;
                            _req.onEnd(headersAsMap(trailer));
                        } else {
                            _req.onEnd(new HashMap<String, List<String>>());
                        }
                    } finally {
                        ctx.getChannel().close();
                        logger.info("Channel closed");
                    }
                } else {
                    try {
                        _req.onBody(ByteBuffer.wrap(chunk.getContent().array()));
                    } catch (final Exception e) {
                        ctx.getChannel().close();
                        logger.log(Level.WARNING, "Request threw exception", e);
                        return;
                    }
                }
            }
        } catch (final RuntimeException e) {
            logger.log(Level.WARNING, "Error processing request", e);
            throw e;
        }
    }


    /**
     * TODO: Add a description for this method.
     *
     * @param o
     */
    private void discard(final Object o) {
        logger.warning("Discarded message on closed channel.");
    }


    /** {@inheritDoc} */
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx,
                                final ExceptionEvent e) {
        // Catching 'java.io.IOException: Broken pipe' here indicates the client disconnected early.
        logger.log(Level.WARNING, "Error processing request", e.getCause());
    }


    private Map<String, List<String>> headersAsMap(final HttpRequest request) {
        final Map<String, List<String>> headers = new HashMap<String, List<String>>();
        for (final String name : request.getHeaderNames()) {
            headers.put(name, request.getHeaders(name));
        }
        return headers;
    }

    private Map<String, List<String>> headersAsMap(final HttpChunkTrailer request) {
        final Map<String, List<String>> headers = new HashMap<String, List<String>>();
        for (final String name : request.getHeaderNames()) {
            headers.put(name, request.getHeaders(name));
        }
        return headers;
    }
}
