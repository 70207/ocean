import com.tt.ocean.net.Handler;
import com.tt.ocean.node.Node;
import com.tt.ocean.node.route.RouteForNode;
import com.tt.ocean.proto.OceanProto;
import com.tt.ocean.route.Route;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.ConfigurationException;

public class client extends Node {

    private static final Logger log = LogManager.getLogger(client.class.getName());

    public client() throws ConfigurationException{
        super();

    }


    public static void main(String[] args){

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();


        try {

            new client().init(workerGroup);
            Route route = new RouteForNode(new client());
            Handler handler = new Handler();
            handler.setRoute(route);

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>(){

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();


                            p.addLast(new ProtobufVarint32FrameDecoder());
                            p.addLast(new ProtobufDecoder(OceanProto.OceanMessage.getDefaultInstance()));

                            p.addLast(new ProtobufVarint32LengthFieldPrepender());
                            p.addLast(new ProtobufEncoder());

                            p.addLast(handler);
                        }
                    });

            b.bind(1214).sync().channel().closeFuture().sync();

        }
        catch (ConfigurationException ex){
            log.error("client create failed, ex:" + ex.getMessage());

        }
        catch (InterruptedException ex){
            log.error("interupption ex:" + ex.getMessage());
        }
        finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
