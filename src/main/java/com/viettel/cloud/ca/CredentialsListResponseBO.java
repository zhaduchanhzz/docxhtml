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
public class CredentialsListResponseBO extends ResponseBO {

    private List<CredentialsInfoResponseBO> credentialIDs;

    public List<CredentialsInfoResponseBO> getCredentialIDs() {
        return credentialIDs;
    }

    public void setCredentialIDs(List<CredentialsInfoResponseBO> credentialIDs) {
        this.credentialIDs = credentialIDs;
    }

}
