package com.esignature.pdf;


import com.esignature.service.WorkerConnector;
import com.esignature.service.response.CertResponse;
import com.esignature.service.response.SignatureResponse;
import com.esignature.signer.SignatureParameter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    private static final String baseUrl = "http://192.168.100.36:8666/api/esignature/";
    private static final String workerName = "test";
    private static final String workerId = null;

    public static void main(String[] args) throws Exception {

        String inputPdf = "input.pdf";
        String tmpPdf = "tmp.pdf";
        String signedPdf = "signed.pdf";
        String fieldName = "Signature1";

        // 1️⃣ Init signer
        IText7ExternalSigner signer =
                new IText7ExternalSigner();
        WorkerConnector workerConnector = new WorkerConnector(baseUrl);
        CertResponse certResponse = workerConnector.getCert(workerName, workerId);
        String certBase64 = certResponse.getData().getCert();
        File file = new File("input.pdf");
        byte[] bytes = FileUtils.readFileToByteArray(file);
        SignatureParameter param = new SignatureParameter(bytes, certBase64);
        signer.init(param);
        // 2️⃣ Phase 1: tạo placeholder + lấy second hash
        signer.prepareSignature(
                inputPdf,
                tmpPdf,
                fieldName
        );
//        signer.calculateSignatureIText7( inputPdf,
//                tmpPdf,
//                fieldName);
        byte[] hashToSign = signer.getSecondHash();
        System.out.println(
                "Second hash (Base64): " +
                        Base64.encodeBase64String(hashToSign)
        );
        String hashValue = Base64.encodeBase64String(signer.getSecondHash());
        SignatureResponse signatureResponse = workerConnector.sign(hashValue, workerName, workerId);
        String signature = signatureResponse.getSignedData();
        byte[] signedHash = Base64.decodeBase64(signature);


        // 4️⃣ Phase 2: embed chữ ký vào PDF
        signer.setSignedHash(signedHash);
        signer.completeSignature(
                tmpPdf,
                signedPdf,
                fieldName
        );

        System.out.println("✅ Signed PDF created: " + signedPdf);
    }

    private static void signPdf() throws Exception {
        WorkerConnector workerConnector = new WorkerConnector(baseUrl);
        CertResponse certResponse = workerConnector.getCert(workerName, workerId);
        String certBase64 = certResponse.getData().getCert();
        File file = new File("input.pdf");
        byte[] bytes = FileUtils.readFileToByteArray(file);
        SignatureParameter param = new SignatureParameter(bytes, certBase64);
        try {
            IText7ExternalSigner pdfSigner = new IText7ExternalSigner();
            pdfSigner.init(param);
//            pdfSigner.setTsaClient("http://tsa.ca.gov.vn/", null, null);
            try {
                pdfSigner.prepareSignature("input.pdf", "temp.pdf", "hehehehe1");
            } catch (Exception e) {
                e.printStackTrace();
            }
            String hashValue = Base64.encodeBase64String(pdfSigner.getSecondHash());
            SignatureResponse signatureResponse = workerConnector.sign(hashValue, workerName, workerId);
            String signature = signatureResponse.getSignedData();
//            byte[] signedData = pdfSigner.completeSigning(Base64.decodeBase64(signature));
            pdfSigner.completeSignature(
                    "tmp.pdf",
                    "signed.pdf",
                    "Signature1"
            );

//            writeToFile(signedData, "test_signed.pdf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeToFile(byte[] input, String pathname) {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(new File(pathname));
            outStream.write(input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}