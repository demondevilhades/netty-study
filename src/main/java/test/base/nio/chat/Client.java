package test.base.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class Client {

    private Selector selector;
    private SocketChannel socketChannel;

    /**
     * 
     * @throws IOException
     */
    public Client() throws IOException {
        selector = Selector.open();
        socketChannel = SocketChannel.open(new InetSocketAddress(Server.PORT));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        log.info("client connected : {}", socketChannel.getLocalAddress());
    }

    /**
     * 
     */
    public void run() {
        Thread scannerThread = new Thread() {

            @Override
            public void run() {
                try (Scanner scanner = new Scanner(System.in)) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        try {
                            send(line);
                        } catch (IOException e) {
                            log.info("", e);
                        }
                    }
                }
            }

        };
        scannerThread.start();
        
        new Thread() {

            @Override
            public void run() {
                while (true) {
                    try {
                        recieve();
                        Thread.sleep(5000);
                    } catch (IOException e) {
                        log.info("stop", e);
                        break;
                    } catch (InterruptedException e) {
                        log.info("", e);
                    }
                }
            }

        }.start();
    }

    /**
     * 
     * @param str
     * @throws IOException
     */
    public void send(String str) throws IOException {
        log.info("send : {}", str);
        socketChannel.write(ByteBuffer.wrap(str.getBytes()));
    }

    /**
     * 
     * @throws IOException
     */
    public void recieve() throws IOException {
        if (selector.select() > 0) {
            for (SelectionKey selectionKey : selector.selectedKeys()) {
                if (selectionKey.isReadable()) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//                    ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    int read = socketChannel.read(byteBuffer);
                    String str = new String(byteBuffer.array(), 0, read);
                    byteBuffer.clear();
                    log.info("recieve : from = {}, msg = {}", socketChannel.getRemoteAddress(), str);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.run();
    }
}
