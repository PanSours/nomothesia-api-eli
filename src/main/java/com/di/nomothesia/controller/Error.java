package com.di.nomothesia.controller;

import java.util.Map;

/**
 * Created by psour on 5/12/2016.
 *
 */
public class Error {

    private String error;
    private String message;
    private String timestamp;
    private Integer status;

    public Error(int status, Map<String, Object> errorAttributes) {
        this.error = (String) errorAttributes.get("error");
        this.message = (String) errorAttributes.get("message");
        this.timestamp = errorAttributes.get("timestamp").toString();
        this.status = (Integer) errorAttributes.get("status");
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Integer getStatus() {
        return status;
    }
}