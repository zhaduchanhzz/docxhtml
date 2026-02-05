package com.itextpdf.signatures;

import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.pdf.*;
import org.bouncycastle.asn1.esf.SignaturePolicyIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

public class OWSigner extends PdfSigner {
    public OWSigner(PdfReader reader, OutputStream outputStream, StampingProperties properties) throws IOException {
        super(reader, outputStream, properties);
    }

    public OWSigner(PdfReader reader, OutputStream outputStream, String path, StampingProperties properties) throws IOException {
        super(reader, outputStream, path, properties);
    }



    public void signDetached(IExternalDigest externalDigest, IExternalSignature externalSignature, Certificate[] chain, Collection<ICrlClient> crlList, IOcspClient ocspClient, ITSAClient tsaClient, int estimatedSize, CryptoStandard sigtype, SignaturePolicyIdentifier signaturePolicy) throws IOException, GeneralSecurityException {
        if (this.closed) {
            throw new PdfException("This instance of PdfSigner has been already closed.");
        } else if (this.certificationLevel > 0 && this.isDocumentPdf2() && this.documentContainsCertificationOrApprovalSignatures()) {
            throw new PdfException("Certification signature creation failed. Document shall not contain any certification or approval signatures before signing with certification signature.");
        } else {
            Collection<byte[]> crlBytes = null;

            for(int i = 0; crlBytes == null && i < chain.length; crlBytes = this.processCrl(chain[i++], crlList)) {
            }

            if (estimatedSize == 0) {
                estimatedSize = 8192;
                if (crlBytes != null) {
                    for(byte[] element : crlBytes) {
                        estimatedSize += element.length + 10;
                    }
                }

                if (ocspClient != null) {
                    estimatedSize += 4192;
                }

                if (tsaClient != null) {
                    estimatedSize += 4192;
                }
            }

            PdfSignatureAppearance appearance = this.getSignatureAppearance();
            appearance.setCertificate(chain[0]);
            if (sigtype == PdfSigner.CryptoStandard.CADES && !this.isDocumentPdf2()) {
                this.addDeveloperExtension(PdfDeveloperExtension.ESIC_1_7_EXTENSIONLEVEL2);
            }

            String hashAlgorithm = externalSignature.getHashAlgorithm();
            PdfSignature dic = new PdfSignature(PdfName.Adobe_PPKLite, sigtype == PdfSigner.CryptoStandard.CADES ? PdfName.ETSI_CAdES_DETACHED : PdfName.Adbe_pkcs7_detached);
            dic.setReason(appearance.getReason());
            dic.setLocation(appearance.getLocation());
            dic.setSignatureCreator(appearance.getSignatureCreator());
            dic.setContact(appearance.getContact());
            dic.setDate(new PdfDate(this.getSignDate()));
            this.cryptoDictionary = dic;
            Map<PdfName, Integer> exc = new HashMap();
            exc.put(PdfName.Contents, estimatedSize * 2 + 2);
            this.preClose(exc);
            PdfPKCS7 sgn = new PdfPKCS7((PrivateKey)null, chain, hashAlgorithm, (String)null, externalDigest, false);
            if (signaturePolicy != null) {
                sgn.setSignaturePolicy(signaturePolicy);
            }

            InputStream data = this.getRangeStream();
            byte[] hash = DigestAlgorithms.digest(data, SignUtils.getMessageDigest(hashAlgorithm, externalDigest));
            List<byte[]> ocspList = new ArrayList();
            if (chain.length > 1 && ocspClient != null) {
                for(int j = 0; j < chain.length - 1; ++j) {
                    byte[] ocsp = ocspClient.getEncoded((X509Certificate)chain[j], (X509Certificate)chain[j + 1], (String)null);
                    if (ocsp != null) {
                        ocspList.add(ocsp);
                    }
                }
            }

            byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, sigtype, ocspList, crlBytes);
            byte[] extSignature = externalSignature.sign(sh);
            sgn.setExternalDigest(extSignature, (byte[])null, externalSignature.getEncryptionAlgorithm());
            byte[] encodedSig = sgn.getEncodedPKCS7(hash, sigtype, tsaClient, ocspList, crlBytes);
            if (estimatedSize < encodedSig.length) {
                throw new IOException("Not enough space");
            } else {
                byte[] paddedSig = new byte[estimatedSize];
                System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);
                PdfDictionary dic2 = new PdfDictionary();
                dic2.put(PdfName.Contents, (new PdfString(paddedSig)).setHexWriting(true));
                this.close(dic2);
                this.closed = true;
            }
        }
    }
    private boolean isDocumentPdf2() {
        return this.document.getPdfVersion().compareTo(PdfVersion.PDF_2_0) >= 0;
    }
    public InputStream getHashStream() throws IOException {
        return this.getRangeStream();
    }
}
