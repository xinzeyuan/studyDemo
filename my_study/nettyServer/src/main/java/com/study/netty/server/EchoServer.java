package com.study.netty.server;

import com.alibaba.fastjson.JSON;
import com.study.netty.hand.EchoServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {

    private  int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {

        System.err.println("args:" + JSON.toJSONString(args));

        if(args.length != 1){
            System.err.println("Usage:" + EchoServer.class.getSimpleName() + "<port>");
        }

        int port = Integer.parseInt(args[0]);
        new EchoServer(port).start();
    }

    public void start() throws  Exception{
        EchoServerHandler echoServerHandler = new EchoServerHandler();
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventExecutors)
                           .channel(NioServerSocketChannel.class)
                           .localAddress(new InetSocketAddress(port))
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline().addLast(echoServerHandler);
                                }
                            });

            ChannelFuture future = serverBootstrap.bind().sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            eventExecutors.shutdownGracefully().sync();
        }
    }
}
