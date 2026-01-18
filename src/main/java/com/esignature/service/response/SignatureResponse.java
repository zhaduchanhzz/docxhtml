package com.esignature.service.response;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class SignatureResponse extends BaseResponse {
    private String signedData;
}
