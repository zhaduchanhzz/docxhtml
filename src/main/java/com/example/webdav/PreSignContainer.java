package com.example.webdav;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.signatures.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;

public class PreSignContainer implements IExternalSignatureContainer {

    private final String hashAlgorithm;
    private final Certificate[] certChain;

    private byte[] documentHash;
    private byte[] authenticatedAttributes;
    private byte[] secondHash;

    public PreSignContainer(String hashAlgorithm,
                            Certificate[] certChain) {
        this.hashAlgorithm = hashAlgorithm;
        this.certChain = certChain;
    }

    @Override
    public byte[] sign(InputStream data) throws GeneralSecurityException, GeneralSecurityException {

        BouncyCastleDigest digest = new BouncyCastleDigest();

        // 1. Hash ByteRange
        try {
            this.documentHash = DigestAlgorithms.digest(
                    data,
                    digest.getMessageDigest(hashAlgorithm)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 2. Tạo PKCS7 để build authenticated attributes
        ITSAClient tsaClient = new TSAClientBouncyCastle("http://tsa.ca.gov.vn/", null, null);

        PdfPKCS7 pkcs7 = new PdfPKCS7(
                null,
                certChain,
                hashAlgorithm,
                "BC",
                digest,
                false
        );
//        pkcs7.get
//
//        try {
//            byte[] encodedPKCS7 = pkcs7.getEncodedPKCS7(data.readAllBytes(), PdfSigner.CryptoStandard.CMS, tsaClient, null, null);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        try {
            pkcs7.setExternalDigest(data.readAllBytes(), null, "RSA");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.authenticatedAttributes =
                pkcs7.getAuthenticatedAttributeBytes(
                        documentHash,
                        PdfSigner.CryptoStandard.CMS,
                        null,
                        null
                );


        // 3. Hash lần 2 (đưa cho HSM / remote signer)
        MessageDigest md = DigestAlgorithms.getMessageDigest(hashAlgorithm, "BC");
        this.secondHash = md.digest(authenticatedAttributes);
//        try {
//            secondHash =
//                    DigestAlgorithms.digest(
//                            new ByteArrayInputStream(this.authenticatedAttributes),
//                           MessageDigest.getInstance(hashAlgorithm)
//                    );
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        // ⚠️ QUAN TRỌNG:
        // Chưa có chữ ký thật → trả mảng rỗng để giữ chỗ
        return secondHash;
    }


    @Override
    public void modifySigningDictionary(PdfDictionary signDic) {
        signDic.put(PdfName.Filter, PdfName.Adobe_PPKLite);
        signDic.put(PdfName.SubFilter, PdfName.Adbe_pkcs7_detached);
    }

    public  boolean checkHashSignature(byte[] signerProfile, String alg, String certBase64, String signedHashBase64) {
        Signature verify = null;
        try {
            verify = Signature.getInstance(alg + "withRSA");
            CertificateFactory fac = CertificateFactory.getInstance("x509");
            Certificate cert = fac.generateCertificate(new ByteArrayInputStream(org.bouncycastle.util.encoders.Base64.decode(certBase64)));
            if (!(cert instanceof X509Certificate)) {
                return false;
            }
            X509Certificate signerFromProfile = (X509Certificate) cert;
            verify.initVerify(signerFromProfile.getPublicKey());
            verify.update(signerProfile, 0, signerProfile.length);
            Boolean r = verify.verify(org.bouncycastle.util.encoders.Base64.decode(signedHashBase64));
            return r;
        } catch (NoSuchAlgorithmException | CertificateException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===== Getter =====
    public byte[] getDocumentHash() {
        return documentHash;
    }

    public byte[] getAuthenticatedAttributes() {
        return authenticatedAttributes;
    }

    public byte[] getSecondHash() {
        return secondHash;
    }


}
