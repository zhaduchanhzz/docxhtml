package com.viettel.signature.pdf;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalSignatureContainer;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

public class ExternalHashContainer implements IExternalSignatureContainer {

    private byte[] hash;
    private final String hashAlgorithm;
    private final String provider;

    public ExternalHashContainer(String hashAlgorithm) {
        this(hashAlgorithm, "BC");
    }

    public ExternalHashContainer(String hashAlgorithm, String provider) {
        this.hashAlgorithm = hashAlgorithm;
        this.provider = provider;
    }

    @Override
    public byte[] sign(InputStream data) throws GeneralSecurityException {
        try {
            MessageDigest md =
                    DigestAlgorithms.getMessageDigest(hashAlgorithm, provider);
            this.hash = DigestAlgorithms.digest(data, md);
            return new byte[0]; // reserve only
        } catch (IOException e) {
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
}
