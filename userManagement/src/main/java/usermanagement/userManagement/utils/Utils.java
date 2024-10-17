package usermanagement.userManagement.utils;

import usermanagement.userManagement.model.Response;

public class Utils {

    public static <T> Response generateResponse(String message, T body, int status){
        return Response.builder()
                .statusCode(status)
                .data(body)
                .message(message)
                .build();
    }
}
