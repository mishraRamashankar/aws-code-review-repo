package com.wtc.pureit.data.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class InstallationStatusResponse {

    private String statusCode;
    private String errorMessage;
    private String errorType;
    private String stackTrace;
    private String message;
}
