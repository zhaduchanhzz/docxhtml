package com.esignature.service;

import com.esignature.service.request.SignRequest;
import com.esignature.service.response.CertResponse;
import com.esignature.service.response.SignatureResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;

public class WorkerConnector {
    public WorkerConnector(String baseUrl) {
        this.BASE_URL = baseUrl;
    }

    private static final Gson gson = (new GsonBuilder()).disableHtmlEscaping().create();
    private static String GET_CERT = "certificate";
    private static String SIGN = "sign";

    private final String BASE_URL;

    public CertResponse getCert(String workerName, String workerId) {
        try {
            SignRequest signRequest = new SignRequest();
            signRequest.setWorkerName(workerName);
            signRequest.setWorkerId(workerId);
            String result = this.execute(GET_CERT, new StringEntity(gson.toJson(signRequest)));
            return gson.fromJson(result, CertResponse.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public SignatureResponse sign(String data, String workerName, String workerId) {
        try {
            SignRequest signRequest = new SignRequest();
            signRequest.setWorkerName(workerName);
            signRequest.setWorkerId(workerId);
            signRequest.setType("hash");
            signRequest.setData(data);

            StringEntity postingString = new StringEntity(gson.toJson(signRequest), StandardCharsets.UTF_8);
            String result = this.execute(SIGN, postingString);
            return gson.fromJson(result, SignatureResponse.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private String execute(String functionName, StringEntity postingString) {
        String result = null;

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(this.BASE_URL + functionName);
            request.setHeader("Content-Type", "application/json;charset=UTF-8");
            request.setEntity(postingString);

            try {
                CloseableHttpResponse response = client.execute(request);
                Throwable var8 = null;

                try {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        result = EntityUtils.toString(entity);
                    }
                } catch (Throwable var35) {
                    var8 = var35;
                    throw var35;
                } finally {
                    if (response != null) {
                        if (var8 != null) {
                            try {
                                response.close();
                            } catch (Throwable var34) {
                                var8.addSuppressed(var34);
                            }
                        } else {
                            response.close();
                        }
                    }

                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return result;
    }
}
