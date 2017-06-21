package org.teamspace.client.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by shpilb on 22/03/2017.
 */
@Slf4j
public class AuthHelperUtil {
    public static final String protocol = "http";
    public static final Integer port = 80;
    public static final String path = "/rest";

    public static String getHostBasePath(String host) {
        URL url = null;
        try {
            url = new URL(protocol, host, port, path);
        } catch (MalformedURLException e) {
            log.error("Unable to create URL", e);
            throw new RuntimeException("Unable to create URL");
        }
        return  url.toString();
    }

}
