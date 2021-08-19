package test.base.nio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class ClientTest {

    /**
     * 
     */
    public void run() {
        try (SocketChannel socketChannel = SocketChannel.open();) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(ServerTest.port);
            socketChannel.configureBlocking(false);
            if (!socketChannel.connect(inetSocketAddress)) {
                do {
                    log.info("connecting...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } while (!socketChannel.finishConnect());
            }
            log.info("connected");
            try (InputStreamReader isr = new InputStreamReader(System.in); BufferedReader br = new BufferedReader(isr);) {
                do {
                    String line = br.readLine();
                    ByteBuffer byteBuffer = ByteBuffer.wrap(line.getBytes());
                    socketChannel.write(byteBuffer);
                    log.info("send : {}", line);
                    if ("stop".equals(line)) {
                        log.info("stop");
                        break;
                    }
                } while (socketChannel.isConnected());
            }
        } catch (IOException e) {
            log.info("", e);
        }
    }

    public static void main(String[] args) {
        new ClientTest().run();
    }
}
