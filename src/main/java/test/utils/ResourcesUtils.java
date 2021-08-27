package test.utils;

import java.net.URL;

/**
 * 
 * @author awesome
 */
public class ResourcesUtils {

    /**
     * 
     * @param str
     * @return
     */
    public static String getResourceFile(String str) {
        return ResourcesUtils.class.getClassLoader().getResource(str).getFile();
    }

    /**
     * 
     * @param str
     * @return
     */
    public static URL getResource(String str) {
        return ResourcesUtils.class.getClassLoader().getResource(str);
    }
}
