package com.esignature.pdf;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.signatures.IExternalSignatureContainer;

import java.io.InputStream;
import java.security.GeneralSecurityException;

public class ExternalBlankSignatureContainer implements IExternalSignatureContainer {
    private byte[] signedBytes;

    public ExternalBlankSignatureContainer(byte[] signedBytes)
    {
        this.signedBytes = signedBytes;
    }

    @Override
    public byte[] sign(InputStream data) throws GeneralSecurityException {
        return this.signedBytes;
    }

    @Override
    public void modifySigningDictionary(PdfDictionary signDic) {

    }
}
