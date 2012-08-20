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

import static com.johnstok.http.netty.HTTPConstants.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.DefaultHttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import com.johnstok.http.Version;
import com.johnstok.http.async.Response;


/**
 * Netty implementation of the {@link Response} interface.
 *
 * @author Keith Webster Johnston.
 */
class NettyResponse
    implements
        Response {

    private static enum State { NEW, STATUS_LINE_WRITTEN, HEADERS_WRITTEN }

    public static Logger logger =
        Logger.getLogger(NettyResponse.class.getName());

    private static final String EMPTY_MAP = "{}";                  //$NON-NLS-1$


    // TODO: The following methods should only be executed once:
    // writeResponseLine, writeResponseHeaders, writeResponseEnd
    // FIXME: All writes should have a listener attached that correctly handles
    // errors. E.g. a NULL header value.

    private final HttpResponse _response;
    private final Channel _channel;
    private State _state = State.NEW;


    /**
     * Constructor.
     *
     * @param response The Netty response that backs this object.
     * @param channel  The Netty channel that will write bytes to the socket.
     */
    NettyResponse(final HttpResponse response,
                  final Channel channel) {
        _response = response;
        _channel = channel;
    }


    /** {@inheritDoc} */
    @Override
    public void writeStatusLine(final Version version,
                                final int statusCode,
                                final String reasonPhrase) {
        requireState(_state, State.NEW);
        requireNotNull(version);
        requireGreaterThan(statusCode, 0);
        requireNotNull(reasonPhrase);
        requireNotEmpty(reasonPhrase);
        // FIXME: Add reasonPhrase validation, per HTTP spec?
        logger.info(version+SP+statusCode+SP+reasonPhrase);
        _response.setProtocolVersion(
            new HttpVersion(
                "HTTP",                                            //$NON-NLS-1$
                version.getMajor(),
                version.getMinor(),
                true));
        _response.setStatus(
            new HttpResponseStatus(statusCode, reasonPhrase));
        _state=State.STATUS_LINE_WRITTEN;
        // TODO: Send status line to channel now?
    }


    private void requireState(final State actual, final State expected) {
        if (actual!=expected) { throw new IllegalStateException("Current state is: "+_state); }
    }


    private void requireNotEmpty(final String string) {
        if (string.trim().isEmpty()) { throw new IllegalArgumentException(); }
    }


    private void requireGreaterThan(final int integer, final int i) {
        if (integer<=i) { throw new IllegalArgumentException(); }
    }


    private void requireNotNull(final Object o) {
        if (null==o) { throw new IllegalArgumentException(); }
    }


    /** {@inheritDoc} */
    @Override
    public void writeHeaders(final Map<String, ? extends List<String>> headers) {
        requireState(_state, State.STATUS_LINE_WRITTEN);
        requireNotNull(headers);
        for (final Map.Entry<String, ? extends List<String>> h : headers.entrySet()) {
            requireNotNull(h.getKey());
            requireNotNull(h.getValue());
            for (final String value : h.getValue()) {
                requireNotNull(value);
            }
        }

        logger.info((null==headers) ? EMPTY_MAP : headers.toString());

        for (final Map.Entry<String, ? extends List<String>> h : headers.entrySet()) {
            _response.setHeader(h.getKey(), h.getValue());
        }

        _channel.write(_response); // Chunked encoding will be enabled if req'd.
        _state=State.HEADERS_WRITTEN;
    }


    /** {@inheritDoc} */
    @Override
    public void writeBody(final ByteBuffer bytes) {
        requireState(_state, State.HEADERS_WRITTEN);
        requireNotNull(bytes);
        logger.info("byte["+bytes.remaining()+"]"); // mark, limit
        final HttpChunk chunk =
            new DefaultHttpChunk(ChannelBuffers.wrappedBuffer(bytes));
        _channel.write(chunk);              // Chunk will be unwrapped if req'd.
    }


    /** {@inheritDoc} */
    @Override
    public void writeEnd(final Map<String, ? extends List<String>> trailers) {
        logger.info((null==trailers) ? EMPTY_MAP : trailers.toString());
        if (null==trailers) { return; }
        final DefaultHttpChunkTrailer trailerChunk =
            new DefaultHttpChunkTrailer();
        for (final Map.Entry<String, ? extends List<String>> t : trailers.entrySet()) {
            trailerChunk.setHeader(t.getKey(), t.getValue());
        }
        _channel.write(trailerChunk);         // Chunk will be ignored if req'd.
    }
}
