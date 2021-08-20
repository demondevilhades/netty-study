package test.base.nio.chat;

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

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    /**
     * 
     * @throws IOException
     */
    public Server() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        log.info("server registed");
    }

    /**
     * 
     * @throws IOException
     */
    public void run() throws IOException {
        while (true) {
            if (selector.select(2000) > 0) {
                log.info("accept key");
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey selectionKey = keyIterator.next();
                    if (selectionKey.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        log.info("online : {}", socketChannel.getRemoteAddress());
                    } else if (selectionKey.isConnectable()) {
                    } else if (selectionKey.isReadable()) {
                        recieveAndSend(selectionKey);
                    }
                    keyIterator.remove();
                }
            }
        }
    }
    
    private void recieveAndSend(SelectionKey selectionKey) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        int read = socketChannel.read(byteBuffer);
        byte[] bs = byteBuffer.array();
        String str = new String(bs, 0, read);
        byteBuffer.clear();
        log.info("send : from = {}, msg = {}", socketChannel.getRemoteAddress(), str);

        for (SelectionKey sk : selector.keys()) {
            SocketChannel channel = null;
            try {
                if(sk.channel() instanceof SocketChannel) {
                    channel = (SocketChannel) sk.channel();
                    if (channel == socketChannel) {
                        continue;
                    }
                    channel.write(ByteBuffer.wrap(bs));
                }
            } catch (IOException e) {
                log.info("send error", e);
                sk.cancel();
                if(channel != null) {
                    channel.close();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.run();
    }
}
