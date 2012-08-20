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

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.RequestClientConnControl;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.CoreProtocolPNames;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.johnstok.http.Version;
import com.johnstok.http.async.Request;
import com.johnstok.http.async.RequestFactory;
import com.johnstok.http.async.Response;
import com.johnstok.http.netty.test.FakeChannel;
import com.johnstok.http.netty.test.HelloWorldRequest;


/**
 * Tests for the {@link NettyServer}.
 *
 * @author Keith Webster Johnston.
 */
public class RequestExceptionHandlingTest
    extends
        AbstractServerTest<NettyServer> {

    /*
     * TODO:
     * Test for both chunked and un-chunked requests (onBody, onEnd).
     * Should we give the client an opportunity to handle the exception? Perhaps via Connection#onError?
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


    /** Test. */
    @Test
    public void exceptionOnBeginCaught() throws Exception {

        // ARRANGE
        final EchoRequest request = new EchoRequest() {
            /** {@inheritDoc} */
            @Override
            public void onBegin(final Response response) {
                throw new RuntimeException("Internal Error.");
            }
        };
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new RequestFactory() {
                @Override
                public Request newInstance() {
                    return request;
                }
            });

        // ACT
        try {
            _httpClient.execute(new HttpGet("http://localhost:4444/"));

            // ASSERT
        } catch(final NoHttpResponseException e) {
        }
    }


    /** Test. */
    @Test
    public void exceptionOnRequestLineCaught() throws Exception {

        // ARRANGE
        final EchoRequest request = new EchoRequest() {
            /** {@inheritDoc} */
            @Override
            public void onRequestLine(final String method, final String uri, final Version version) {
                throw new RuntimeException("Internal Error.");
            }
        };
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new RequestFactory() {
                @Override
                public Request newInstance() {
                    return request;
                }
            });

        // ACT
        try {
            _httpClient.execute(new HttpGet("http://localhost:4444/"));

        // ASSERT
        } catch(final NoHttpResponseException e) {
        }
    }


    /** Test. */
    @Test
    public void exceptionOnHeadersCaught() throws Exception {

        // ARRANGE
        final EchoRequest request = new EchoRequest() {
            /** {@inheritDoc} */
            @Override
            public void onHeaders(final Map<String, List<String>> headers) {
                throw new RuntimeException("Internal Error.");
            }
        };
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new RequestFactory() {
                @Override
                public Request newInstance() {
                    return request;
                }
            });

        // ACT
        try {
            _httpClient.execute(new HttpGet("http://localhost:4444/"));

            // ASSERT
        } catch(final NoHttpResponseException e) {
        }
    }


    /** Test. */
    @Test
    public void exceptionOnBodyCaught() throws Exception {

        // ARRANGE
        final EchoRequest request = new EchoRequest() {
            /** {@inheritDoc} */
            @Override
            public void onBody(final ByteBuffer bytes) {
                throw new RuntimeException("Internal Error.");
            }
        };
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new RequestFactory() {
                @Override
                public Request newInstance() {
                    return request;
                }
            });

        // ACT
        try {
            _httpClient.execute(new HttpGet("http://localhost:4444/"));

            // ASSERT
        } catch(final NoHttpResponseException e) {
        }
    }


    /** Test. */
    @Test
    public void exceptionOnEndCaught() throws Exception {

        // ARRANGE
        final EchoRequest request = new EchoRequest() {
            /** {@inheritDoc} */
            @Override
            public void onEnd(final Map<String, List<String>> trailers) {
                throw new RuntimeException("Internal Error.");
            }
        };
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new RequestFactory() {
                @Override
                public Request newInstance() {
                    return request;
                }
            });

        // ACT
        try {
            _httpClient.execute(new HttpGet("http://localhost:4444/"));

            // ASSERT
        } catch(final NoHttpResponseException e) {
        }
    }


    /** Test. */
    @Test
    public void exceptionOnChunkedBodyCaught() throws Exception {

        // ARRANGE
        final HelloWorldRequest request = new HelloWorldRequest() {
            /** {@inheritDoc} */
            @Override
            public void onBody(final ByteBuffer bytes) {
                throw new RuntimeException("Internal Error.");
            }
        };
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new RequestFactory() {
                @Override
                public Request newInstance() {
                    return request;
                }
            });

        // ACT
        try {
            final StringEntity entity = new StringEntity("foo");
            entity.setChunked(true);

            final HttpPost httpPost =
                new HttpPost("http://localhost:4444/");
            httpPost.setEntity(entity);
            final HttpResponse response = _httpClient.execute(httpPost);
            response.getEntity().consumeContent();

            // ASSERT
        } catch(final NoHttpResponseException e) {
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
