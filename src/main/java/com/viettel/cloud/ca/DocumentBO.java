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
public class DocumentBO {
    
    private int document_id;
    private String document_name;

    public DocumentBO(int document_id, String document_name) {
        this.document_id = document_id;
        this.document_name = document_name;
    }

    public int getDocument_id() {
        return document_id;
    }

    public void setDocument_id(int document_id) {
        this.document_id = document_id;
    }

    public String getDocument_name() {
        return document_name;
    }

    public void setDocument_name(String document_name) {
        this.document_name = document_name;
    }
    
}
