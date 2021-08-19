package test.netty.base.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class ServerTest {
    static final int port = 9527;

    /**
     * 
     */
    public void run() {
        final AtomicBoolean running = new AtomicBoolean(true);
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); Selector selector = Selector.open();) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
            serverSocketChannel.socket().bind(inetSocketAddress);
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            log.info("start");

            while (running.get()) {
                log.info("checking...");
                if (selector.select(5000) > 0) {
                    log.info("accept key");
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey selectionKey = keyIterator.next();
                        log.info("key : hashCode = {}", selectionKey.hashCode());
                        if (selectionKey.isAcceptable()) {
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                            log.info("accept channel : hashCode = {}", socketChannel.hashCode());
                        } else if (selectionKey.isConnectable()) {
                        } else if (selectionKey.isReadable()) {
                            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                            ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                            int read = socketChannel.read(byteBuffer);
                            log.info("position = {}, limit = {}, capacity = {}", byteBuffer.position(), byteBuffer.limit(), byteBuffer.capacity());
                            String str = new String(byteBuffer.array(), 0, read);
                            byteBuffer.clear();
                            log.info("read[{}] = {}", socketChannel.hashCode(), str);
                            if ("stop".equals(str)) {
                                log.info("stop");
                                running.set(false);
                            }
                        } else if (selectionKey.isWritable()) {
                        }
                        keyIterator.remove();
                    }
                }
            }
        } catch (IOException e) {
            log.info("", e);
        }
    }

    public static void main(String[] args) {
        new ServerTest().run();
    }
}
