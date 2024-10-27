package com.rishiraj.bitbybit.apiResponses;


import lombok.Builder;
import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
