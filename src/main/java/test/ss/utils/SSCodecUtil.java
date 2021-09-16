package test.ss.utils;

import static io.netty.handler.codec.http.HttpConstants.CR;
import static io.netty.handler.codec.http.HttpConstants.LF;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;

/**
 * 
 * @author awesome
 */
public class SSCodecUtil {
    static final short CRLF_SHORT = (CR << 8) | LF;

    public static void encode(ByteBuf buf, String str1, String str2) {

        int str1Len = str1.length();
        int str2Len = str2.length();
        final int entryLen = str1Len + str2Len + 4;
        buf.ensureWritable(entryLen);
        int offset = buf.writerIndex();
        {
            writeAscii(buf, offset, str1);
            offset += str1Len;
        }
        {
            ByteBufUtil.setShortBE(buf, offset, CRLF_SHORT);
            offset += 2;
        }
        {
            writeAscii(buf, offset, str1);
            offset += str1Len;
        }
        {
            ByteBufUtil.setShortBE(buf, offset, CRLF_SHORT);
            offset += 2;
        }
        buf.writerIndex(offset);
    }

    private static void writeAscii(ByteBuf buf, int offset, CharSequence value) {
        if (value instanceof AsciiString) {
            ByteBufUtil.copy((AsciiString) value, 0, buf, offset, value.length());
        } else {
            buf.setCharSequence(offset, value, CharsetUtil.US_ASCII);
        }
    }
}
