//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.viettel.signature.pdf;

public class TimestampConfig {
    private boolean useTimestamp = false;
    private String tsa_url = "http://tsa.viettel-ca.vn/";
    private String tsa_acc;
    private String tsa_pass;

    public TimestampConfig() {
    }

    public boolean isUseTimestamp() {
        return this.useTimestamp;
    }

    public void setUseTimestamp(boolean useTimestamp) {
        this.useTimestamp = useTimestamp;
    }

    public String getTsa_url() {
        return this.tsa_url;
    }

    public void setTsa_url(String tsa_url) {
        this.tsa_url = tsa_url;
    }

    public String getTsa_acc() {
        return this.tsa_acc;
    }

    public void setTsa_acc(String tsa_acc) {
        this.tsa_acc = tsa_acc;
    }

    public String getTsa_pass() {
        return this.tsa_pass;
    }

    public void setTsa_pass(String tsa_pass) {
        this.tsa_pass = tsa_pass;
    }
}
