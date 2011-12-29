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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.RequestClientConnControl;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.johnstok.http.Request;
import com.johnstok.http.RequestFactory;
import com.johnstok.http.netty.test.HelloWorldRequestFactory;
import com.johnstok.http.netty.test.SingletonRequestFactory;


/**
 * Tests for the {@link NettyServer}.
 *
 * @author Keith Webster Johnston.
 */
public class SimpleRequestTest
    extends
        AbstractServerTest<NettyServer> {

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
    public void echoFixedLengthContent() throws Exception {

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
        final HttpPost httpPost =
            new HttpPost("http://localhost:4444/");
        httpPost.setEntity(new StringEntity(uuid.toString(), "UTF-8"));

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


    /** Test. */
    @Test
    public void simpleGet() {

        // ARRANGE
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new HelloWorldRequestFactory());

        // ACT
        final String responseBody = get("/");

        // ASSERT
        assertEquals("Hello World!", responseBody);
    }


    private String get(final String uri) {
        try {
            final StringBuilder responseBody = new StringBuilder();
            final URL oracle = new URL("http://localhost:4444"+uri);
            final URLConnection yc = oracle.openConnection();
            final BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    yc.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                responseBody.append(inputLine);
            }
            in.close();
            return responseBody.toString();
        } catch (final MalformedURLException e) {
            throw new RuntimeException("Error GETting '"+uri+"'", e);
        } catch (final IOException e) {
            throw new RuntimeException("Error GETting '"+uri+"'", e);
        }
    }

    /** Test. */
    @Test
    public void listenOnNonListeningServerStartsListening() {

        // ARRANGE

        // ACT
        _server.listen(
            new InetSocketAddress(LOCALHOST, 4444),
            new SingletonRequestFactory(null));

        // ASSERT
        assertTrue(_server.isListening());
    }


    /** {@inheritDoc} */
    @Override
    protected NettyServer createServer() { return new NettyServer(); }
}
