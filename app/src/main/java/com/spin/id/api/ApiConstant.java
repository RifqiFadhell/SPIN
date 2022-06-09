package com.spin.id.api;

public interface ApiConstant {

    String API_VERSION = "2.0";

    long TIMEOUT = 120;

    long UPLOAD_TIMEOUT = 60;

    int HTTP_INTERNAL_SERVER_ERROR = 500;

    int HTTP_GATEWAY_TIMEOUT = 504;

    int BAD_GATEWAY = 502;

    int SERVICE_UNAVAILABLE = 503;

    String API_CODE = "status";

    String TOKEN = "token";

    String MSG_CODE = "Message";

    String DATA_CODE = "data";

    String ERRORS = "errors";

    String MAP_RESULT = "results";

    String PLACE_RESULT = "result";

    String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    String BIRTH_DATE_PATTERN = "yyyy-MM-dd";

    String VALUE = "value";

    String CONTENT_TYPE_JSON = "application/json";

    String CONTENT_TYPE_FORM_ENCODED = "application/x-www-form-urlencoded";
}
