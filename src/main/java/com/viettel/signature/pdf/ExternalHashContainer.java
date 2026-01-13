package com.viettel.signature.pdf;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.signatures.*;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Calendar;

@Slf4j
public class ExternalHashContainer implements IExternalSignatureContainer {

    private byte[] hash;                  // Hash của PDF hoặc tự tính
    private final String hashAlgorithm;   // SHA256, SHA1,...
    private final String provider;        // "BC"
    private final int estimatedSize;      // Kích thước reserve
    private final byte[] extSignature;    // Chữ ký ngoài (HSM, offline)
    private final Certificate[] chain;    // Certificate chain
    private final String cryptAlgorithm;  // RSA, DSA
    private final java.util.Date signDate;        // Ngày ký
    private final TimestampConfig timestampConfig; // TSA nếu có

    public ExternalHashContainer(String hashAlgorithm,
                                 String provider,
                                 int estimatedSize,
                                 byte[] extSignature,
                                 Certificate[] chain,
                                 String cryptAlgorithm,
                                 java.util.Date signDate,
                                 TimestampConfig timestampConfig) {
        this.hashAlgorithm = hashAlgorithm;
        this.provider = provider;
        this.estimatedSize = estimatedSize;
        this.extSignature = extSignature;
        this.chain = chain;
        this.cryptAlgorithm = cryptAlgorithm;
        this.signDate = signDate;
        this.timestampConfig = timestampConfig;
    }

    @Override
    public byte[] sign(InputStream data) throws GeneralSecurityException {
        try {
            // 1️⃣ Đảm bảo provider BC đã add
            if (Security.getProvider(provider) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }

            // 2️⃣ Tính hash nếu chưa có
            if (hash == null) {
                MessageDigest md = DigestAlgorithms.getMessageDigest(hashAlgorithm, provider);
                hash = DigestAlgorithms.digest(data, md);
            }

            // 3️⃣ Ký hash nếu extSignature null (offline/HSM)
            byte[] sig = extSignature != null ? extSignature : hsmSign(hash, cryptAlgorithm, timestampConfig);

            // 4️⃣ Build PKCS#7
            Calendar cal = Calendar.getInstance();
            cal.setTime(signDate);

            PdfPKCS7 pkcs7 = new PdfPKCS7(
                    null,          // privateKey không cần khi dùng extSignature
                    chain,
                    hashAlgorithm,
                    null,
                    new BouncyCastleDigest(),
                    false
            );

            pkcs7.setExternalDigest(sig, null, cryptAlgorithm);

            return pkcs7.getEncodedPKCS7(
                    hash, // secondDigest
                    com.itextpdf.signatures.PdfSigner.CryptoStandard.CMS, // sigtype
                    (timestampConfig != null && timestampConfig.isUseTimestamp())
                            ? new TSAClientBouncyCastle(
                            timestampConfig.getTsa_url(),
                            timestampConfig.getTsa_acc(),
                            timestampConfig.getTsa_pass())
                            : null, // tsaClient
                    null, // ocsp
                    null  // crlBytes
            );

        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        } catch (Exception e) {
            throw new GeneralSecurityException(e);
        }
    }

    @Override
    public void modifySigningDictionary(PdfDictionary dic) {
        dic.put(PdfName.Filter, PdfName.Adobe_PPKLite);
        dic.put(PdfName.SubFilter, PdfName.Adbe_pkcs7_detached);
    }

    public byte[] getHash() {
        return hash;
    }

    public int getEstimatedSize() {
        return estimatedSize;
    }

    /**
     * Hàm ký offline/HSM tùy dự án
     */
    private byte[] hsmSign(byte[] pdfHash, String algorithm, TimestampConfig timestampConfig) throws GeneralSecurityException {
        // TODO: Thực hiện ký hash với HSM hoặc private key offline
        // Đây chỉ là placeholder, cần implement theo HSM/PKCS#11
        throw new UnsupportedOperationException("HSM signing not implemented yet");
    }
}
