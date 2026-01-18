package com.esignature.service.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertModel {
    private String serialNumber;
    private String subjectDN;
    private String issuerDN;
    private Long validFrom;
    private Long validTo;
    private String cert;
    private String chain;
}
