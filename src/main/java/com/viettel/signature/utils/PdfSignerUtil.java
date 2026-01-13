//package com.viettel.signature.utils;
//
//
//import com.itextpdf.kernel.pdf.*;
//import com.itextpdf.signatures.*;
//import com.viettel.signature.pdf.TimestampConfig;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//
//import java.io.*;
//import java.security.*;
//import java.security.cert.Certificate;
//import java.util.*;
//
//public class PdfSignerUtil {
//
//    static {
//        Security.addProvider(new BouncyCastleProvider());
//    }
//
//    /**
//     * Chèn chữ ký vào PDF với iText 7.
//     *
//     * @param src            PDF nguồn
//     * @param dest           PDF ký xong
//     * @param fieldName      Tên field chữ ký
//     * @param digestAlgorithm SHA256, SHA512...
//     * @param cryptAlgorithm  RSA, DSA
//     * @param hash            Hash đã tính sẵn
//     * @param extSignature    Chữ ký từ HSM hoặc offline
//     * @param chain           Chain certificate
//     * @param signDate        Ngày ký
//     * @param timestampConfig cấu hình timestamp
//     * @return true nếu ký thành công
//     */
//    public static boolean insertSignatureItext7(
//            String src,
//            String dest,
//            String fieldName,
//            String digestAlgorithm,
//            String cryptAlgorithm,
//            byte[] hash,
//            byte[] extSignature,
//            Certificate[] chain,
//            Date signDate,
//            TimestampConfig timestampConfig
//    ) {
//        try (PdfReader reader = new PdfReader(src);
//             FileOutputStream os = new FileOutputStream(dest)) {
//
//            Security.addProvider(new BouncyCastleProvider());
//
//            // ===== Tạo PdfSigner =====
//            PdfSigner signer = new PdfSigner(reader, os, new StampingProperties().useAppendMode());
//            signer.setFieldName(fieldName);
//
//            // ===== Kiểm tra chữ ký cuối =====
//            SignatureUtil signUtil = new SignatureUtil(signer.getDocument());
//            if (!signUtil.signatureCoversWholeDocument(fieldName)) {
//                System.err.println("Not the last signature");
//                return false;
//            }
//
//            // ===== ExternalSignatureContainer để ký =====
//            IExternalSignatureContainer container = new IExternalSignatureContainer() {
//
//                @Override
//                public byte[] sign(InputStream data) throws GeneralSecurityException {
//                    try {
//                        // 1️⃣ Tính hash PDF nếu chưa có
//                        byte[] pdfHashToSign = hash;
//                        if (pdfHashToSign == null) {
//                            MessageDigest md = DigestAlgorithms.getMessageDigest(digestAlgorithm, "BC");
//                            pdfHashToSign = DigestAlgorithms.digest(data, md);
//                        }
//
//                        // 2️⃣ Ký hash bằng chữ ký ngoài/HSM nếu chưa có
//                        byte[] sig = extSignature != null ? extSignature
//                                : hsmSign(pdfHashToSign, cryptAlgorithm, timestampConfig);
//
//                        // 3️⃣ Tạo PdfPKCS7
//                        PdfPKCS7 pkcs7 = new PdfPKCS7(
//                                null,       // private key không cần khi ký ngoài
//                                chain,
//                                digestAlgorithm,
//                                null,
//                                new BouncyCastleDigest(),
//                                false
//                        );
//
//                        pkcs7.setExternalDigest(sig, null, cryptAlgorithm);
//
//                        // 4️⃣ TSA nếu có
//                        ITSAClient tsaClient = null;
//                        if (timestampConfig != null && timestampConfig.isUseTimestamp()) {
//                            tsaClient = new TSAClientBouncyCastle(
//                                    timestampConfig.getTsa_url(),
//                                    timestampConfig.getTsa_acc(),
//                                    timestampConfig.getTsa_pass()
//                            );
//                        }
//
//                        // 5️⃣ Build PKCS#7 chuẩn iText 7
//                        return pkcs7.getEncodedPKCS7(
//                                pdfHashToSign,
//                                PdfSigner.CryptoStandard.CMS,
//                                tsaClient,
//                                null,
//                                null
//                        );
//
//                    } catch (IOException e) {
//                        throw new GeneralSecurityException(e);
//                    }
//                }
//
//                @Override
//                public void modifySigningDictionary(PdfDictionary dic) {
//                    dic.put(PdfName.Filter, PdfName.Adobe_PPKLite);
//                    dic.put(PdfName.SubFilter, PdfName.Adbe_pkcs7_detached);
//                }
//            };
//
//            // ===== Ký vào PDF =====
//            signer.signExternalContainer(container, 8192);
//            return true;
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return false;
//        }
//    }
//
//
//    /**
//     * Hàm ví dụ ký hash bằng HSM hoặc offline.
//     * Thay bằng logic ký thực tế.
//     */
//    private static byte[] hsmSign(byte[] hash, String cryptAlgorithm, TimestampConfig timestampConfig) {
//        // TODO: gửi hash đi HSM hoặc ký offline
//        // Ở đây tạm return hash để test
//        return hash;
//    }
//}
