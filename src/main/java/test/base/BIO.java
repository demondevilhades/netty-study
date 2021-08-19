package test.base;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

/**
 * test : <br>
 * 1. telnet 127.0.0.1 9527 <br>
 * 2. Ctrl + ] <br>
 * 3. send msg <br>
 * 
 * @author awesome
 */
@Slf4j
public class BIO {
    private final int serverPort = 9527;

    /**
     * 
     */
    public void run() {
        log.info("run start");
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final AtomicBoolean running = new AtomicBoolean(true);
        try (ServerSocket serverSocket = new ServerSocket(serverPort);) {
            while (running.get()) {
                try {
                    final Socket socket = serverSocket.accept();
                    log.info("serverSocket.accept");
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try (InputStream is = socket.getInputStream();) {
                                byte[] bs = new byte[1024];
                                while (true) {
                                    int read = is.read(bs);
                                    if (read != -1) {
                                        String str = new String(bs, 0, read);
                                        log.info("msg = {}", str);
                                        if ("stop".equalsIgnoreCase(str)) {
                                            running.set(false);
                                            serverSocket.close();
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            } catch (IOException e) {
                                log.error("", e);
                            } finally {
                                log.info("thread end");
                            }
                        }
                    });
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        } catch (IOException e) {
            log.error("", e);
        } finally {
            executorService.shutdown();
            log.info("run end");
        }
    }

    public static void main(String[] args) {
        new BIO().run();
    }
}
