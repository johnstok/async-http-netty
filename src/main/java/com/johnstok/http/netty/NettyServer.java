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
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import com.johnstok.http.async.Connection;
import com.johnstok.http.async.RequestFactory;
import com.johnstok.http.async.Server;


/**
 * The Netty server implementation.
 *
 * @author Keith Webster Johnston.
 */
public class NettyServer
    implements
        Server {
    public static Logger logger =
        Logger.getLogger(NettyServer.class.getName());

    private final DefaultChannelGroup _connections = new  DefaultChannelGroup();
    private Channel _channel;
    private ServerBootstrap _bootstrap;
    private Connection _connection;


    /** {@inheritDoc} */
    @Override
    public void listen(final InetSocketAddress address,
                       final RequestFactory requestFactory,
                       final Connection connection) {
        logger.info("Starting");
        if (isListening()) {
            throw new IllegalStateException("Server is already listening.");
        }

        _bootstrap = new ServerBootstrap(
            new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));
        _bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override  public ChannelPipeline getPipeline() {
                final ChannelPipeline pipeline = new DefaultChannelPipeline();
                pipeline.addLast("connection-handler", new SimpleChannelUpstreamHandler() {
                    @Override
                    public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent e) {
                        logger.info("New connection");
                        _connections.add(ctx.getChannel());
                        if (null!=_connection) { _connection.onOpen(); }
                    }
                });
                pipeline.addLast(
                    "decoder",                                     //$NON-NLS-1$
                    new HttpRequestDecoder());
                pipeline.addLast(
                    "encoder",                                     //$NON-NLS-1$
                    new HttpResponseEncoder());
                pipeline.addLast(
                    "handler",                                     //$NON-NLS-1$
                    new AsyncHttpUpstreamHandler(requestFactory));
                return pipeline;
            }
        });

        _connection = connection;
        _channel = _bootstrap.bind(address);
        _connections.add(_channel);

        logger.info("Started");
    }


    /** {@inheritDoc} */
    @Override
    public void listen(final InetSocketAddress address,
                       final RequestFactory requestFactory) {
        listen(address, requestFactory, null);
    }


    /** {@inheritDoc} */
    @Override
    public void close() {
        logger.info("Closing");
        if (!isListening()) {
            throw new IllegalStateException("Server is not listening.");
        }

        /*
         * To shut down a service gracefully, you should do the following:
         *   1. unbind all channels created by the factory,
         *   2. close all child channels accepted by the unbound channels, and (these two steps so far is usually done using ChannelGroup.close())
         *   3. call releaseExternalResources().
         *
         *   See http://docs.jboss.org/netty/3.2/api/org/jboss/netty/channel/socket/nio/NioServerSocketChannelFactory.html
         */
        _connections.close().awaitUninterruptibly();
        _bootstrap.releaseExternalResources();
        _channel = null;
        _bootstrap = null;
        _connection = null;
        logger.info("Closed");
    }


    /** {@inheritDoc} */
    @Override
    public boolean isListening() {
        return null!=_channel;
    }


    public int getConnectionCount() {
        return (isListening()) ? _connections.size()-1 : 0;
    }
}
