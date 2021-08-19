package test.base;

import java.nio.Buffer;
import java.nio.IntBuffer;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class BufferTest {

    /**
     * 
     */
    public void run() {
        int capacity = 8;
        IntBuffer intBuffer = IntBuffer.allocate(capacity);
        logBufferInfo(intBuffer);
        for (int i = 0; i < capacity / 2; i++) {
            intBuffer.put(i * 2);
            logBufferInfo(intBuffer);
        }
        intBuffer.flip();
        logBufferInfo(intBuffer);
        while (intBuffer.hasRemaining()) {
            intBuffer.get();
            logBufferInfo(intBuffer);
        }
        intBuffer.clear();
        logBufferInfo(intBuffer);
    }

    private void logBufferInfo(Buffer buffer) {
        log.info("position = {}, limit = {}, capacity = {}", buffer.position(), buffer.limit(), buffer.capacity());
    }

    public static void main(String[] args) {
        new BufferTest().run();
    }
}
