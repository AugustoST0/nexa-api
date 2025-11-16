package org.senai.dtos;

import java.util.List;

public class ErrorResponse {
    public String message;
    public String code;
    public List<String> details;

    public ErrorResponse(String message, String code) {
        this.message = message;
        this.code = code;
    }

    public ErrorResponse(String message, String code, List<String> details) {
        this.message = message;
        this.code = code;
        this.details = details;
    }
}