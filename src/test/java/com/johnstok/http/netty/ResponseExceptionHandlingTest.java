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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.junit.Test;
import com.johnstok.http.Response;
import com.johnstok.http.Version;
import com.johnstok.http.netty.test.FakeChannel;


/**
 * Tests for the {@link NettyServer}.
 *
 * @author Keith Webster Johnston.
 */
public class ResponseExceptionHandlingTest
    extends
        AbstractServerTest<NettyServer> {


    /** Test. */
    @Test
    public void nullVersionRejected() {

        // ARRANGE
        final Response response = createResponse();

        // ACT
        try {
            response.writeStatusLine(null, 200, "OK");
            fail();

        // ASSERT
        } catch(final IllegalArgumentException e) {

        }
    }


    /** Test. */
    @Test
    public void zeroStatusCodeRejected() {

        // ARRANGE
        final Response response = createResponse();

        // ACT
        try {
            response.writeStatusLine(new Version(1,1), 0, "OK");
            fail();

        // ASSERT
        } catch(final IllegalArgumentException e) {

        }
    }


    /** Test. */
    @Test
    public void negativeStatusCodeRejected() {

        // ARRANGE
        final Response response = createResponse();

        // ACT
        try {
            response.writeStatusLine(new Version(1,1), -1, "OK");
            fail();

        // ASSERT
        } catch(final IllegalArgumentException e) {

        }
    }


    /** Test. */
    @Test
    public void zeroLengthReasonPhraseRejected() {

        // ARRANGE
        final Response response = createResponse();

        // ACT
        try {
            response.writeStatusLine(new Version(1,1), 200, "");
            fail();

        // ASSERT
        } catch(final IllegalArgumentException e) {

        }
    }


    /** Test. */
    @Test
    public void whitespaceReasonPhraseRejected() {

        // ARRANGE
        final Response response = createResponse();

        // ACT
        try {
            response.writeStatusLine(new Version(1,1), 200, " \t\r\n");
            fail();

        // ASSERT
        } catch(final IllegalArgumentException e) {

        }
    }


    /** Test. */
    @Test
    public void nullReasonPhraseRejected() {

        // ARRANGE
        final Response response = createResponse();

        // ACT
        try {
            response.writeStatusLine(new Version(1,1), 200, null);
            fail();

        // ASSERT
        } catch(final IllegalArgumentException e) {

        }
    }


    /** Test. */
    @Test
    public void nullHeaderMapRejected() {

        // ARRANGE
        final Response response = createResponse();
        response.writeStatusLine(new Version(1,1), 200, "OK");

        // ACT
        try {
            response.writeHeaders(null);
            fail();

        // ASSERT
        } catch(final IllegalArgumentException e) {

        }
    }


    /** Test. */
    @Test
    public void nullHeaderKeyRejected() {

        // ARRANGE
        final Response response = createResponse();
        response.writeStatusLine(new Version(1,1), 200, "OK");

        // ACT
        try {
            response.writeHeaders(Collections.singletonMap((String) null, new ArrayList<String>()));
            fail();

        // ASSERT
        } catch(final IllegalArgumentException e) {

        }
    }


    /** Test. */
    @Test
    public void nullHeaderListRejected() {

        // ARRANGE
        final Response response = createResponse();
        response.writeStatusLine(new Version(1,1), 200, "OK");

        // ACT
        try {
            response.writeHeaders(Collections.singletonMap("Key", (List<String>) null));
            fail();

        // ASSERT
        } catch(final IllegalArgumentException e) {

        }
    }


    /** Test. */
    @Test
    public void nullHeaderValueRejected() {

        // ARRANGE
        final Response response = createResponse();
        response.writeStatusLine(new Version(1,1), 200, "OK");

        // ACT
        try {
            response.writeHeaders(Collections.singletonMap("key", new ArrayList<String>(){{add((String) null);}}));
            fail();

        // ASSERT
        } catch(final IllegalArgumentException e) {

        }
    }


    /** Test. */
    @Test
    public void nullBodyDataRejected() {

        // ARRANGE
        final Response response = createResponse();
        response.writeStatusLine(new Version(1,1), 200, "OK");
        response.writeHeaders(Collections.singletonMap("Key", new ArrayList<String>()));

        // ACT
        try {
            response.writeBody(null);
            fail();

        // ASSERT
        } catch(final IllegalArgumentException e) {

        }
    }


    /** Test. */
    @Test
    public void nullTrailersIgnored() {

        // ARRANGE
        final Response response = createResponse();

        // ACT
        response.writeEnd(null);

        // ASSERT
    }


    /** Test. */
    @Test
    public void repeatedCallToWriteStatusLineRejected() {

        // ARRANGE
        final Response response = createResponse();
        response.writeStatusLine(new Version(1,1), 200, "OK");

        // ACT
        try {
            response.writeStatusLine(new Version(1,1), 200, "OK");
            fail();

        // ASSERT
        } catch(final IllegalStateException e) {

        }
    }


    /** Test. */
    @Test
    public void earlyCallToWriteHeadersRejected() {

        // ARRANGE
        final Response response = createResponse();

        // ACT
        try {
            response.writeHeaders(Collections.singletonMap("Key", new ArrayList<String>()));
            fail();

        // ASSERT
        } catch(final IllegalStateException e) {

        }
    }


    /** Test. */
    @Test
    public void earlyCallToWriteBodyRejected() {

        // ARRANGE
        final Response response = createResponse();

        // ACT
        try {
            response.writeBody(ByteBuffer.wrap(new byte[]{0}));
            fail();

            // ASSERT
        } catch(final IllegalStateException e) {

        }
    }


    protected NettyResponse createResponse() {
        return new NettyResponse(
            new DefaultHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK),
            new FakeChannel()); }


    /** {@inheritDoc} */
    @Override
    protected NettyServer createServer() { return new NettyServer(); }
}
