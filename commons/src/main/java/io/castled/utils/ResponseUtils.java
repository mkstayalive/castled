package io.castled.utils;

import javax.ws.rs.core.Response;

public class ResponseUtils {

    public static boolean is2xx(Response response) {
        return response.getStatus() >= 200 && response.getStatus() < 300;
    }
}
