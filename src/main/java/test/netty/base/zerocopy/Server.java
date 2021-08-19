package test.netty.base.zerocopy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class Server {
    static final int PORT = 9527;

    /**
     * 
     * @throws IOException
     */
    public void run() throws IOException {
        try(ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                Selector selector = Selector.open();){
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                if (selector.select(5000) > 0) {
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey selectionKey = keyIterator.next();
                        if (selectionKey.isAcceptable()) {
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024 * 10));
                            log.info("accept channel : {}", socketChannel.getRemoteAddress());
                        } else if (selectionKey.isConnectable()) {
                        } else if (selectionKey.isReadable()) {
                            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                            ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();

                            int sum = 0;
//                            int count = 0;
                            int read = 0;
                            do {
                                sum += read;
                                read = socketChannel.read(byteBuffer);
//                                System.out.println(read + "\t" + (++count));
                                byteBuffer.rewind();
                            } while (read != -1);

                            log.info("read : from = {}, sum = {}", socketChannel.getRemoteAddress(), sum);
                            selectionKey.cancel();
                            socketChannel.close();
                        } else if (selectionKey.isWritable()) {
                        }
                        keyIterator.remove();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new Server().run();
    }
}
