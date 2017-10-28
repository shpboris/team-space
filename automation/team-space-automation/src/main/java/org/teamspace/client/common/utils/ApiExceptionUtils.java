package org.teamspace.client.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.teamspace.client.common.domain.ApiResponseBody;

import java.io.IOException;

/**
 * Created by shpilb on 07/04/2017.
 */
@Slf4j
public class ApiExceptionUtils {

    public static ApiResponseBody fromJson(String s) throws IOException {
        return new ObjectMapper().readValue(s, ApiResponseBody.class);
    }
}
