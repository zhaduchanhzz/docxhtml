package com.example.webdav;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.signatures.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Calendar;

public class PreSignContainer implements IExternalSignatureContainer {

    private final String hashAlgorithm;
    private final Certificate[] certChain;
    private final Calendar signingTime;

    private byte[] documentHash;
    private byte[] authenticatedAttributes;
    private byte[] secondHash;

    public PreSignContainer(String hashAlgorithm,
                            Certificate[] certChain,
                            Calendar signingTime) {
        this.hashAlgorithm = hashAlgorithm;
        this.certChain = certChain;
        this.signingTime = signingTime;
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
        PdfPKCS7 pkcs7 = new PdfPKCS7(
                null,
                certChain,
                hashAlgorithm,
                "BC",
                digest,
                false
        );
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

        ITSAClient tsaClient = new TSAClientBouncyCastle("http://tsa.ca.gov.vn/", null, null);
        byte[] encodedPKCS7 = pkcs7.getEncodedPKCS7(documentHash, PdfSigner.CryptoStandard.CMS, tsaClient, null, null);


        // 3. Hash lần 2 (đưa cho HSM / remote signer)
        MessageDigest md = DigestAlgorithms.getMessageDigest(hashAlgorithm, "BC");
        this.secondHash = md.digest(authenticatedAttributes);

        // ⚠️ QUAN TRỌNG:
        // Chưa có chữ ký thật → trả mảng rỗng để giữ chỗ
        return new byte[0];
    }


    @Override
    public void modifySigningDictionary(PdfDictionary signDic) {
        signDic.put(PdfName.Filter, PdfName.Adobe_PPKLite);
        signDic.put(PdfName.SubFilter, PdfName.Adbe_pkcs7_detached);
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
