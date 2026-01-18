package com.esignature.service.request;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class SignRequest {
    private String type;
    private String data;
    private String workerName;
    private String workerId;
    private String fileName;
}
