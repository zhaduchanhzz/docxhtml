/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.cloud.ca;

/**
 *
 * @author HoTro
 */
public class CredentialsInfoResponseBO extends ResponseBO {

    private String description;
    private KeyBO key;
    private CertBO cert;
    private PINBO PIN;
    private OTPBO OTP;
    private String authMode;
    private String SCAL;
    private int multisign;
    private String lang;
    private String credential_id;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public KeyBO getKey() {
        return key;
    }

    public void setKey(KeyBO key) {
        this.key = key;
    }

    public CertBO getCert() {
        return cert;
    }

    public void setCert(CertBO cert) {
        this.cert = cert;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    public PINBO getPIN() {
        return PIN;
    }

    public void setPIN(PINBO PIN) {
        this.PIN = PIN;
    }

    public OTPBO getOTP() {
        return OTP;
    }

    public void setOTP(OTPBO OTP) {
        this.OTP = OTP;
    }

    public String getSCAL() {
        return SCAL;
    }

    public void setSCAL(String SCAL) {
        this.SCAL = SCAL;
    }

    public int getMultisign() {
        return multisign;
    }

    public void setMultisign(int multisign) {
        this.multisign = multisign;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getCredential_id() {
        return credential_id;
    }

    public void setCredential_id(String credential_id) {
        this.credential_id = credential_id;
    }
}
