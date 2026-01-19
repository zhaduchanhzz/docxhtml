//package com.esignature;
//
//import com.esignature.pdf.PdfHashSigner;
//import com.esignature.pdf.PdfHashSigner.FontName;
//import com.esignature.pdf.PdfHashSigner.FontStyle;
//import com.esignature.pdf.PdfHashSigner7;
//import com.esignature.pdf.PdfSignatureView;
//import com.esignature.service.WorkerConnector;
//import com.esignature.service.response.CertResponse;
//import com.esignature.service.response.SignatureResponse;
//import com.esignature.signer.SignatureParameter;
//import com.esignature.signer.SignerProfile;
//import com.esignature.utils.MessageDigestAlgorithm;
//import com.itextpdf.signatures.PdfSignatureAppearance;
//import com.itextpdf.text.pdf.PdfSignatureAppearance;
//import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.io.FileUtils;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//public class Main {
//    private static final String baseUrl = "http://104.156.255.132:8666/api/esignature/";
//    private static final String workerName = "minhlq.pa";
//    private static final String workerId = null;
//
//    public static void main(String[] args) throws Exception {
//        signPdf();
//        System.out.println("done");
//    }
//
//    private static void signPdf() throws Exception {
//
//        WorkerConnector workerConnector = new WorkerConnector(baseUrl);
//
//        // ===== CERT =====
//        CertResponse certResponse =
//                workerConnector.getCert(workerName, workerId);
//
//        String certBase64 =
//                certResponse.getData().getCert();
//
//        // ===== PDF =====
//        byte[] pdfBytes =
//                FileUtils.readFileToByteArray(
//                        new File("DEMO VIEW PDF.pdf")
//                );
//
//        String sigText =
//                "Ký bởi: Trần Quang Khánh\n" +
//                        "Ngày ký: 13/01/2026\n" +
//                        "Tổ chức xác thực: SecureMetric";
//
//        PdfHashSigner7 signer = new PdfHashSigner7(
//                pdfBytes,
//                certBase64,
//                1,
//                "0,0,250,100",
//                sigText,
//                PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION,
//                Base64.decodeBase64("ORK5CYII="),
//                "http://tsa.ca.gov.vn/"
//        );
//
//        // ===== PRE-SIGN =====
//        signer.preSign();
//
//        String hashBase64 =
//                Base64.encodeBase64String(
//                        signer.getDocumentHash()
//                );
//
//        // ===== SIGN HASH =====
//        SignatureResponse signResponse =
//                workerConnector.sign(hashBase64, workerName, workerId);
//
//        byte[] pkcs7 =
//                Base64.decodeBase64(signResponse.getSignedData());
//
//        // ===== POST-SIGN =====
//        byte[] signedPdf =
//                signer.postSign(pkcs7);
//
//        FileUtils.writeByteArrayToFile(
//                new File("test_signed.pdf"),
//                signedPdf
//        );
//    }
//
//
//    private static void writeToFile(byte[] input, String pathname) {
//        FileOutputStream outStream = null;
//        try {
//            outStream = new FileOutputStream(new File(pathname));
//            outStream.write(input);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (outStream != null) {
//                try {
//                    outStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//}