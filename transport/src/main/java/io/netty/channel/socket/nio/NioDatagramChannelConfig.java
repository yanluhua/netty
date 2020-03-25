/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.socket.nio;

import io.netty.channel.ChannelException;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.DatagramChannelConfig;
import io.netty.channel.socket.DefaultDatagramChannelConfig;
import io.netty.util.internal.SocketUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.util.Enumeration;
import java.util.Map;

/**
 * The default {@link NioDatagramChannelConfig} implementation.
 */
class NioDatagramChannelConfig extends DefaultDatagramChannelConfig {

    private final DatagramChannel javaChannel;

    NioDatagramChannelConfig(NioDatagramChannel channel, DatagramChannel javaChannel) {
        super(channel, javaChannel.socket());
        this.javaChannel = javaChannel;
    }

    @Override
    public int getTimeToLive() {
        try {
            return javaChannel.getOption(StandardSocketOptions.IP_MULTICAST_TTL);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public DatagramChannelConfig setTimeToLive(int ttl) {
        try {
            javaChannel.setOption(StandardSocketOptions.IP_MULTICAST_TTL, ttl);
            return this;
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public InetAddress getInterface() {
        NetworkInterface inf = getNetworkInterface();
        if (inf != null) {
            Enumeration<InetAddress> addresses = SocketUtils.addressesFromNetworkInterface(inf);
            if (addresses.hasMoreElements()) {
                return addresses.nextElement();
            }
        }
        return null;
    }

    @Override
    public DatagramChannelConfig setInterface(InetAddress interfaceAddress) {
        try {
            setNetworkInterface(NetworkInterface.getByInetAddress(interfaceAddress));
        } catch (SocketException e) {
            throw new ChannelException(e);
        }
        return this;
    }

    @Override
    public NetworkInterface getNetworkInterface() {
        try {
            return javaChannel.getOption(StandardSocketOptions.IP_MULTICAST_IF);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public DatagramChannelConfig setNetworkInterface(NetworkInterface networkInterface) {
        try {
            javaChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
            return this;
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public boolean isLoopbackModeDisabled() {
        try {
            return javaChannel.getOption(StandardSocketOptions.IP_MULTICAST_LOOP);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public DatagramChannelConfig setLoopbackModeDisabled(boolean loopbackModeDisabled) {
        try {
            javaChannel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, loopbackModeDisabled);
            return this;
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public DatagramChannelConfig setAutoRead(boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }

    @Override
    protected void autoReadCleared() {
        ((NioDatagramChannel) channel).clearReadPending0();
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        if (option instanceof NioChannelOption) {
            return NioChannelOption.setOption(javaChannel, (NioChannelOption<T>) option, value);
        }
        return super.setOption(option, value);
    }

    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option instanceof NioChannelOption) {
            return NioChannelOption.getOption(javaChannel, (NioChannelOption<T>) option);
        }
        return super.getOption(option);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(super.getOptions(), NioChannelOption.getOptions(javaChannel));
    }
}
