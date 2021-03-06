/*
 * Copyright (c) 2015 "JackWhite20"
 *
 * This file is part of Comix.
 *
 * Comix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.jackwhite20.comix.handler;

import de.jackwhite20.comix.Comix;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.net.InetSocketAddress;
import java.util.logging.Level;

/**
 * Created by JackWhite20 on 18.07.2015.
 */
public class ComixChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Comix comix;

    public ComixChannelInitializer() {
        this.comix = Comix.getInstance();
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        InetSocketAddress remoteAddress = ch.remoteAddress();

        // Simple IP-Blacklist
        if (comix.isIpBanned(remoteAddress.getAddress().getHostAddress())) {
            ch.close();
            return;
        }

        // Simple IP-Range-Blacklist
        if(comix.isIpRangeBanned(remoteAddress)) {
            ch.close();
            return;
        }

        HandshakeHandler handshakeHandler = new HandshakeHandler();
        p.addFirst(handshakeHandler);

        UpstreamHandler upstreamHandler = new UpstreamHandler(Comix.getInstance().getBalancingStrategy());
        p.addLast(upstreamHandler);

        handshakeHandler.setUpstreamHandler(upstreamHandler);

        Comix.getLogger().log(Level.INFO, "Comix", "[" + ch.remoteAddress().getAddress().getHostAddress() + "] -> InitialHandler has connected");
    }

}
