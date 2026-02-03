package com.esignature.pdf;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.signatures.IExternalSignatureContainer;

import java.io.InputStream;
import java.security.GeneralSecurityException;

public class ExternalBlankSignatureContainer implements IExternalSignatureContainer {
    private PdfDictionary sigDic;

    public ExternalBlankSignatureContainer(PdfDictionary sigDic) {
        this.sigDic = sigDic;
    }

    public ExternalBlankSignatureContainer(PdfName filter, PdfName subFilter) {
        this.sigDic = new PdfDictionary();
        this.sigDic.put(PdfName.Filter, filter);
        this.sigDic.put(PdfName.SubFilter, subFilter);
    }

    @Override
    public byte[] sign(InputStream data) throws GeneralSecurityException {
        return new byte[0];
    }

    @Override
    public void modifySigningDictionary(PdfDictionary signDic) {
        signDic.put(PdfName.Filter, PdfName.Adobe_PPKLite);
        signDic.put(PdfName.SubFilter, PdfName.Adbe_pkcs7_detached);
    }
}
