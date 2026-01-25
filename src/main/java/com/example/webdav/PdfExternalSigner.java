package com.example.webdav;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.ITSAClient;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.TSAClientBouncyCastle;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class PdfExternalSigner {

    public static void main(String[] args) throws Exception {
        String src = "input.pdf";
        String dest = "signed_output.pdf";
        String sigImage = "images/logo.jpg";

        PdfReader reader = new PdfReader(src);
        FileOutputStream os = new FileOutputStream(dest);

        // 1) Khởi tạo signer với append mode
        PdfSigner signer = new PdfSigner(
                reader,
                os,
                new StampingProperties().useAppendMode()
        );

        signer.setFieldName("Signature1");

        // 2) Tùy chỉnh giao diện chữ ký
        ImageData image = ImageDataFactory.create(sigImage);
        PdfSignatureAppearance appearance = signer.getSignatureAppearance();
        appearance
                .setReason("Document approved")
                .setLocation("Ha Noi")
                .setPageNumber(1)
                .setPageRect(new Rectangle(100, 500, image.getWidth() / 2, image.getHeight() / 2))
                .setSignatureGraphic(image)
                .setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);

        // 3) Thực hiện ký bằng container
        ITSAClient tsaClient = new TSAClientBouncyCastle("http://tsa.ca.gov.vn/", null, null);
//        signer.timestamp(tsaClient,"Signature1");
        int estimatedSize = 12000;
        ExternalServiceSignatureContainer container = new ExternalServiceSignatureContainer();
//        // estimatedSize thường ~12000 — thay đổi theo server

        signer.signExternalContainer(container, estimatedSize);
    }
}
