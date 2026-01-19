package com.example.webdav;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.signatures.IExternalSignatureContainer;

import java.io.InputStream;

public class PostSignContainer implements IExternalSignatureContainer {

    private final byte[] pkcs7Signature;

    public PostSignContainer(byte[] pkcs7Signature) {
        this.pkcs7Signature = pkcs7Signature;
    }

    @Override
    public byte[] sign(InputStream data) {
        // data đã được hash ở bước pre-sign
        return pkcs7Signature;
    }

    @Override
    public void modifySigningDictionary(PdfDictionary signDic) {
        signDic.put(PdfName.Filter, PdfName.Adobe_PPKLite);
        signDic.put(PdfName.SubFilter, PdfName.Adbe_pkcs7_detached);
    }
}
