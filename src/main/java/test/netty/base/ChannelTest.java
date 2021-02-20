package test.netty.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class ChannelTest {

    public void runFileChannel() {
        int capacity = 1024;
        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);
        try (FileInputStream fis = new FileInputStream("./src/main/java/test/netty/base/ChannelTest.java");
                FileChannel fileChannel = fis.getChannel();) {
            int read = -1;
            while (true) {
                byteBuffer.clear();
                read = fileChannel.read(byteBuffer);
                log.info("read = {}", read);
                if (read == -1) {
                    break;
                }
                byteBuffer.flip();
                log.info("\r\n{}", new String(byteBuffer.array(), 0, read));
            }
        } catch (IOException e) {
            log.error("", e);
        }
    }

    public void runFileChannel0() {
        try (RandomAccessFile fis = new RandomAccessFile("./src/main/java/test/netty/base/ChannelTest.java", "rw");
                FileChannel fileChannel = fis.getChannel();) {
            MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, 0, fileChannel.size());
            // read
            int index = 334;
            for (int i = 0; i < 7; i++) {
                log.info("{}", (char)mappedByteBuffer.get(index + i));
            }
            // write
//            mappedByteBuffer.put(index, (byte) 'A');
        } catch (IOException e) {
            log.error("", e);
        }
    }

    public static void main(String[] args) {
        ChannelTest channelTest = new ChannelTest();
//        channelTest.runFileChannel();
        channelTest.runFileChannel0();
    }
}
