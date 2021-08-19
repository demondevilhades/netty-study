package test.ss;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class Test {

    private final Pattern pattern = Pattern.compile("^([01234567][\\dabcdef],)*[01234567][\\dabcdef]$");

    public void run() {
        byte[] bs = new byte[1024];
        int total = -1;
        try (BufferedInputStream bis = new BufferedInputStream(System.in)) {
            do {
                total = bis.read(bs);
                for (int i = 0; i < total; i++) {
                    String hexStr = Integer.toHexString(bs[i] & 0xFF);
                    String str = hexStr.length() == 1 ? "0x0" + hexStr : "0x" + hexStr;
                    System.out.print(str);
                    System.out.print("\t");
                }
                System.out.println();
            } while (total != -1);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    public void sendByte() {
        String line = null;
        try (InputStreamReader isr = new InputStreamReader(System.in); BufferedReader br = new BufferedReader(isr);) {
            do {
                line = br.readLine();
                boolean matches = pattern.matcher(line).matches();
                if (matches) {
                    String[] split = line.split(",");
                    for (int i = 0; i < split.length; i++) {
                        String str = "0x" + split[i];
                        byte b = (byte)Integer.parseInt(split[i], 16);
                        System.out.print(str);
                        System.out.print("/");
                        System.out.print(b);
                        System.out.print("\t");
                    }
                    System.out.println();
                }
            } while (line != null);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    public static void main(String[] args) {
        new Test().sendByte();
    }
}
