//package com.esignature.pdf;
//
//import com.itextpdf.kernel.pdf.PdfDictionary;
//import com.itextpdf.kernel.pdf.PdfName;
//import com.itextpdf.signatures.IExternalSignatureContainer;
//
//import java.io.InputStream;
//import java.security.MessageDigest;
//
//public class PreSignContainer implements IExternalSignatureContainer {
//
//    private final String digestAlgorithm;
//    private byte[] documentHash;
//
//    public PreSignContainer(String digestAlgorithm) {
//        this.digestAlgorithm = digestAlgorithm;
//    }
//
//    @Override
//    public byte[] sign(InputStream data) {
//        try {
//            MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
//            this.documentHash = DigestAlgorithms.digest(data, md);
//            // KHÔNG trả chữ ký thật
//            return new byte[0];
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void modifySigningDictionary(PdfDictionary dic) {
//        dic.put(PdfName.Filter, PdfName.Adobe_PPKLite);
//        dic.put(PdfName.SubFilter, PdfName.Adbe_pkcs7_detached);
//    }
//
//    public byte[] getDocumentHash() {
//        return documentHash;
//    }
//}
