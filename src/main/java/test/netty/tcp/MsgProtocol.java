package test.netty.tcp;

import lombok.Setter;
import lombok.Builder;
import lombok.Getter;

/**
 * 
 * @author awesome
 */
@Builder
@Getter
@Setter
public class MsgProtocol {

    private int len;
    private byte[] bs;
}
