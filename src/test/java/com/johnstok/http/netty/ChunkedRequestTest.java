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
import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.RequestClientConnControl;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.johnstok.http.Version;
import com.johnstok.http.async.Request;
import com.johnstok.http.async.RequestFactory;
import com.johnstok.http.async.Response;


/**
 * Tests for the {@link NettyServer}.
 *
 * @author Keith Webster Johnston.
 */
public class ChunkedRequestTest
    extends
        AbstractServerTest<NettyServer> {

    /*
     * Test fixed length, chunked request/response.
     */

    private DefaultHttpClient _httpClient;


    /**
     * Set up ready for a test.
     *
     *  @throws Exception If setup fails.
     */
    @Override
    @Before
    public void setUp() {
        super.setUp();
        _httpClient = new DefaultHttpClient();
        // Disable automatic redirect following
        _httpClient.getParams().setParameter(
            ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        // Disable 100-Continue
        _httpClient.getParams().setParameter(
            CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
        // Disable Keep-Alive
        _httpClient.removeRequestInterceptorByClass(RequestClientConnControl.class);
        // Disable retry
        _httpClient.setHttpRequestRetryHandler(
            new DefaultHttpRequestRetryHandler(0, false));
    }


    /** Clean up after a test. */
    @Override
    @After
    public void tearDown() {
        _httpClient.getConnectionManager().shutdown();
        _httpClient = null;
        super.tearDown();
    }


    /**
     * Test.
     *
     * @throws Exception If the test fails.
     */
    @Test
    public void onBodyCalledForEachRequestChunk() throws Exception {

        // ARRANGE
        final CountDownLatch latch = new CountDownLatch(1);
        final List<ByteBuffer> chunks = new ArrayList<ByteBuffer>();
        final Request request = new Request() {

            @Override
            public void onBegin(final Response response) { /* No Op */ }

            @Override
            public void onRequestLine(final String method, final String uri, final Version version) {
                /* No Op */
            }

            @Override
            public void onHeaders(final Map<String, List<String>> headers) {
                /* No Op */
            }

            @Override
            public void onBody(final ByteBuffer bytes) {
                chunks.add(bytes);
            }

            @Override
            public void onEnd(final Map<String, List<String>> trailers) {
                latch.countDown();
            }};
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new RequestFactory() {
                @Override
                public Request newInstance() {
                    return request;
                }
            });


        // ACT
        post("http://localhost:4444/", "abc", 1);
        latch.await();

        // ASSERT
        assertEquals(3, chunks.size());
        assertEquals(1, chunks.get(0).array().length);
        assertEquals((byte) 'a', chunks.get(0).array()[0]);
        assertEquals(1, chunks.get(1).array().length);
        assertEquals((byte) 'b', chunks.get(1).array()[0]);
        assertEquals(1, chunks.get(2).array().length);
        assertEquals((byte) 'c', chunks.get(2).array()[0]);
    }


    /**
     * Test.
     *
     * @throws Exception If the test fails.
     */
    @Test
    public void echoChunkedRequestUnchunkedResponse() throws Exception {

        // ARRANGE
        final EchoRequest request = new EchoRequest();
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new RequestFactory() {
                @Override
                public Request newInstance() {
                    return request;
                }
            });

        final UUID uuid = UUID.randomUUID();
        final InputStreamEntity entity =
            new InputStreamEntity(
                new ByteArrayInputStream(uuid.toString().getBytes("UTF-8")),
                -1);
        entity.setChunked(true);
        entity.setContentType("text/plain");

        final HttpPost httpPost =
            new HttpPost("http://localhost:4444/");
        httpPost.setEntity(entity);

        // ACT
        final HttpResponse response = _httpClient.execute(httpPost);

        // ASSERT
        assertEquals(
            200,
            response.getStatusLine().getStatusCode());
        assertEquals(
            "OK",
            response.getStatusLine().getReasonPhrase());
        assertEquals(uuid.toString(), EntityUtils.toString(response.getEntity()));
        assertTrue(request.isComplete());
    }


    /**
     * Test.
     *
     * @throws Exception If the test fails.
     */
    @Test
    public void echoChunkedRequestChunkedResponse() throws Exception {

        // ARRANGE
        final ChunkedEchoRequest request = new ChunkedEchoRequest();
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new RequestFactory() {
                @Override
                public Request newInstance() {
                    return request;
                }
            });

        final UUID uuid = UUID.randomUUID();
        final InputStreamEntity entity =
            new InputStreamEntity(
                new ByteArrayInputStream(uuid.toString().getBytes("UTF-8")),
                -1);
        entity.setChunked(true);
        entity.setContentType("text/plain");

        final HttpPost httpPost =
            new HttpPost("http://localhost:4444/");
        httpPost.setEntity(entity);

        // ACT
        final HttpResponse response = _httpClient.execute(httpPost);

        // ASSERT
        assertEquals(
            200,
            response.getStatusLine().getStatusCode());
        assertEquals(
            "OK",
            response.getStatusLine().getReasonPhrase());
        assertEquals(uuid.toString(), EntityUtils.toString(response.getEntity()));
        assertTrue(request.isComplete());
    }


    /**
     * Test.
     *
     * @throws Exception If the test fails.
     */
    @Test
    public void echoUnchunkedRequestChunkedResponse() throws Exception {

        // ARRANGE
        final ChunkedEchoRequest request = new ChunkedEchoRequest();
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new RequestFactory() {
                @Override
                public Request newInstance() {
                    return request;
                }
            });

        final UUID uuid = UUID.randomUUID();
        final StringEntity entity =
            new StringEntity(uuid.toString(), "UTF-8");
        entity.setChunked(false);

        final HttpPost httpPost =
            new HttpPost("http://localhost:4444/");
        httpPost.setEntity(entity);

        // ACT
        final HttpResponse response = _httpClient.execute(httpPost);

        // ASSERT
        assertEquals(
            200,
            response.getStatusLine().getStatusCode());
        assertEquals(
            "OK",
            response.getStatusLine().getReasonPhrase());
        assertEquals(uuid.toString(), EntityUtils.toString(response.getEntity()));
        assertTrue(request.isComplete());
    }


    /** {@inheritDoc} */
    @Override
    protected NettyServer createServer() { return new NettyServer(); }
}
