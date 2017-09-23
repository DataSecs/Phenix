package de.datasec.phenix.server;

import de.datasec.phenix.server.listener.PhenixServerPacketListener;
import de.datasec.phenix.shared.Protocol;
import de.datasec.phenix.shared.initializer.PhenixChannelInitializer;
import de.datasec.phenix.shared.packetsystem.packets.GetPacket;
import de.datasec.phenix.shared.packetsystem.packets.PutPacket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by DataSec on 23.09.2017.
 */
public class Server {

    private int port;

    private Protocol protocol;

    private PhenixServerPacketListener packetListener;

    public Server(int port) {
        this.port = port;

        protocol = new Protocol();
        protocol.registerPacket((byte) 1, GetPacket.class);
        protocol.registerPacket((byte) 2, PutPacket.class);

        packetListener = new PhenixServerPacketListener();
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // Set up a server
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new PhenixChannelInitializer(true, protocol, packetListener))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            // Wait until the server socket is closed
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}