package test.netty.base;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class SocketChannelTest {
    private final int port = 9527;

    /**
     * 
     */
    public void run() {
        final AtomicBoolean running = new AtomicBoolean(true);
        int capacity = 1024;
        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();) {
            log.info("ServerSocketChannel.open");
            log(serverSocketChannel);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
            serverSocketChannel.socket().bind(inetSocketAddress);
            log.info("ServerSocketChannel.bind");
            log(serverSocketChannel);

            while (running.get()) {
                try (SocketChannel socketChannel = serverSocketChannel.accept();) {
                    log.info("ServerSocketChannel.accept");
                    log(socketChannel);
                    log(socketChannel.socket());
                    StringBuilder sb = new StringBuilder();
                    while (true) {// TODO
                        byteBuffer.clear();
                        int read = socketChannel.read(byteBuffer);
                        if (read != -1) {
                            byteBuffer.flip();
                            sb.append(StandardCharsets.UTF_8.decode(byteBuffer), 0, read);
                            if (read == capacity) {
                                continue;
                            }
                            String str = sb.toString();
                            log.info("msg = {}", sb.toString());
                            if ("stop".equalsIgnoreCase(str)) {
                                running.set(false);
                                break;
                            }
                            sb.setLength(0);
                            continue;
                        } else if(sb.length() > 0) {
                            String str = sb.toString();
                            log.info("msg = {}", sb.toString());
                            if ("stop".equalsIgnoreCase(str)) {
                                running.set(false);
                                break;
                            }
                            sb.setLength(0);
                            continue;
                        }
                        break;
                    }
                    log.info("ServerSocketChannel.end");
                    log(socketChannel);
                    log(socketChannel.socket());
                } catch (IOException e) {
                    log.info("", e);
                }
            }
            log(serverSocketChannel);
        } catch (IOException e) {
            log.info("", e);
        }
    }

    private void log(SocketChannel socketChannel) {
        log.info(
                "socketChannel.isBlocking = {}, socketChannel.isConnected = {}, socketChannel.isConnectionPending = {}, socketChannel.isOpen = {}, socketChannel.isRegistered = {}",
                socketChannel.isBlocking(), socketChannel.isConnected(), socketChannel.isConnectionPending(),
                socketChannel.isOpen(), socketChannel.isRegistered());
    }

    private void log(ServerSocketChannel serverSocketChannel) {
        log.info(
                "serverSocketChannel.isBlocking = {}, serverSocketChannel.isOpen = {}, serverSocketChannel.isRegistered = {}",
                serverSocketChannel.isBlocking(), serverSocketChannel.isOpen(), serverSocketChannel.isRegistered());
    }

    private void log(Socket socket) {
        log.info("socket.isBound = {}, socket.isClosed = {}, socket.isConnected = {}", socket.isBound(),
                socket.isClosed(), socket.isConnected());
    }

    public static void main(String[] args) {
        new SocketChannelTest().run();
    }
}
