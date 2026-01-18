package com.esignature.service.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaseResponse {
    public Integer responseCode;
    public String responseMessage;
}
