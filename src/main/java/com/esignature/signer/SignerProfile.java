package com.esignature.signer;

import java.io.Serializable;
import java.util.Calendar;

public class SignerProfile implements Serializable {
    private String docType;
    private String docId;
    private String hashAlgorithm;
    private byte[] tempData;
    private byte[] secondHash;
    private byte[] dataHashFile;
    private byte[] signerInfo;
    private int estimatedSize;
    private Calendar signingTime;
    private String fieldName;

    private String tsaUrl;

    private String tsaUsername;

    private String tsaPassword;

    private String certBase64;

    public String getCertBase64() {
        return certBase64;
    }

    public void setCertBase64(String certBase64) {
        this.certBase64 = certBase64;
    }

    public String getTsaUrl() {
        return tsaUrl;
    }

    public void setTsaUrl(String tsaUrl) {
        this.tsaUrl = tsaUrl;
    }

    public String getTsaUsername() {
        return tsaUsername;
    }

    public void setTsaUsername(String tsaUsername) {
        this.tsaUsername = tsaUsername;
    }

    public String getTsaPassword() {
        return tsaPassword;
    }

    public void setTsaPassword(String tsaPassword) {
        this.tsaPassword = tsaPassword;
    }

    public byte[] getTempData() {
        return tempData;
    }

    public void setTempData(byte[] tempData) {
        this.tempData = tempData;
    }

    public byte[] getSecondHash() {
        return secondHash;
    }

    public void setSecondHash(byte[] secondHash) {
        this.secondHash = secondHash;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public int getEstimatedSize() {
        return estimatedSize;
    }

    public void setEstimatedSize(int estimatedSize) {
        this.estimatedSize = estimatedSize;
    }

    public Calendar getSigningTime() {
        return signingTime;
    }

    public void setSigningTime(Calendar signingTime) {
        this.signingTime = signingTime;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public byte[] getDataHashFile() {
        return dataHashFile;
    }

    public void setDataHashFile(byte[] dataHashFile) {
        this.dataHashFile = dataHashFile;
    }

    public byte[] getSignerInfo() {
        return signerInfo;
    }

    public void setSignerInfo(byte[] signerInfo) {
        this.signerInfo = signerInfo;
    }
}
