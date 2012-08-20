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

import java.util.logging.Logger;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;


/**
 * Channel handler that logs {@link ChannelStateEvent}s.
 *
 * @author Keith Webster Johnston.
 */
public class EventLoggingHandler
    implements
        ChannelUpstreamHandler,
        ChannelDownstreamHandler {
    public static Logger logger =
        Logger.getLogger(EventLoggingHandler.class.getName());

    /** {@inheritDoc} */
    @Override
    public void handleUpstream(final ChannelHandlerContext ctx,
                               final ChannelEvent e) {
        if (e instanceof ChannelStateEvent) { logger.info(e.toString()); }
        ctx.sendUpstream(e);
    }

    /** {@inheritDoc} */
    @Override
    public void handleDownstream(final ChannelHandlerContext ctx,
                                 final ChannelEvent e) {
        if (e instanceof ChannelStateEvent) { logger.info(e.toString()); }
        ctx.sendDownstream(e);
    }
}
