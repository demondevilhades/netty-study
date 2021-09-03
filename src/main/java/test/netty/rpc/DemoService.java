package test.netty.rpc;

/**
 * 
 * @author awesome
 */
public interface DemoService {
    
    public static final String PROVIDER_NAME = "#DemoService#test#";

    public String test(String msg);
}
