/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.cloud.ca;

import java.util.List;

/**
 *
 * @author HoTro
 */
public class SignHashRequestBO {
    private String client_id;
    private String client_secret;
    private String credentialID;
    private List<DocumentBO> documents;
    private Integer numSignatures;
    private List<String> hash;
    private String hashAlgo;
    private String signAlgo;
    private Integer async;

    public String getCredentialID() {
        return credentialID;
    }

    public void setCredentialID(String credentialID) {
        this.credentialID = credentialID;
    }
    
    public List<DocumentBO> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentBO> documents) {
        this.documents = documents;
    }

    public List<String> getHash() {
        return hash;
    }

    public void setHash(List<String> hash) {
        this.hash = hash;
    }

    public String getHashAlgo() {
        return hashAlgo;
    }

    public void setHashAlgo(String hashAlgo) {
        this.hashAlgo = hashAlgo;
    }

    public String getSignAlgo() {
        return signAlgo;
    }

    public void setSignAlgo(String signAlgo) {
        this.signAlgo = signAlgo;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }  

    public Integer getNumSignatures() {
        return numSignatures;
    }

    public void setNumSignatures(Integer numSignatures) {
        this.numSignatures = numSignatures;
    }

    public Integer getAsync() {
        return async;
    }

    public void setAsync(Integer async) {
        this.async = async;
    }
    
}
