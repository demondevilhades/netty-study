package test.netty.base.zerocopy;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class Client {

    private final String fileName = "";
    private final int maxLen = 1024 * 1024 * 8;

    /**
     * 
     */
    public void run0() {
        try (SocketChannel socketChannel = SocketChannel.open();) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(Server.PORT);
            if (!socketChannel.connect(inetSocketAddress)) {
                do {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } while (!socketChannel.finishConnect());
            }

            File file = new File(fileName);
            int len = (int) file.length();
//            len = maxLen;
            byte[] bs = new byte[len];
            long time = System.currentTimeMillis();
            try (FileInputStream fis = new FileInputStream(fileName);
                    Socket socket = socketChannel.socket();
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                fis.read(bs);
                dos.write(bs);
            }
            log.info("len = {}, time = {}", len, (System.currentTimeMillis() - time));
        } catch (IOException e) {
            log.info("", e);
        }
    }

    /**
     * 
     */
    public void run1() {
        try (SocketChannel socketChannel = SocketChannel.open();) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(Server.PORT);
            socketChannel.configureBlocking(false);
            if (!socketChannel.connect(inetSocketAddress)) {
                do {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } while (!socketChannel.finishConnect());
            }

            File file = new File(fileName);
            int len = (int) file.length();
//            len = maxLen;
            byte[] bs = new byte[len];
            long time = System.currentTimeMillis();
            try (FileInputStream fis = new FileInputStream(fileName);) {
                fis.read(bs);
                socketChannel.write(ByteBuffer.wrap(bs));
            }
            socketChannel.finishConnect();
            log.info("len = {}, time = {}", len, (System.currentTimeMillis() - time));
        } catch (IOException e) {
            log.info("", e);
        }
    }

    /**
     * 
     */
    public void run2() {
        try (SocketChannel socketChannel = SocketChannel.open();) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(Server.PORT);
            socketChannel.configureBlocking(false);
            if (!socketChannel.connect(inetSocketAddress)) {
                do {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } while (!socketChannel.finishConnect());
            }

            long len;
            long time = System.currentTimeMillis();
            try (FileInputStream fis = new FileInputStream(fileName);
                    FileChannel fileChannel = fis.getChannel();) {
                len = fileChannel.transferTo(0, fileChannel.size(), socketChannel);
            }
            log.info("len = {}, time = {}", len, (System.currentTimeMillis() - time));
        } catch (IOException e) {
            log.info("", e);
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        for (int i = 0; i < 5; i++) {
            client.run0();
            client.run1();
            client.run2();
        }
    }
}
