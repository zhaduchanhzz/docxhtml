package com.viettel.signature.pdf;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.signatures.*;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;

import java.io.*;
import java.security.cert.Certificate;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SignPdfAsynchronous7 {

    public static final String CRYPT_ALGORITHM_RSA = "RSA";
    public static final String HASH_ALGORITHM_SHA256 = "SHA256";
    private void prepareEmptySignature(
            PdfSigner signer,
            String fieldName,
            DisplayConfig displayConfig
    ) {

        PdfSignatureAppearance appearance = signer.getSignatureAppearance()
                .setReason(displayConfig.getReason())
                .setLocation(displayConfig.getLocation())
                .setContact(displayConfig.getContact())
                .setPageNumber(displayConfig.getNumberPageSign());

        if (displayConfig.isIsDisplaySignature()) {
            Rectangle rect = new Rectangle(
                    displayConfig.getMarginLeftOfRectangle(),
                    displayConfig.getMarginBottomOfRectangle(),
                    displayConfig.getWidthRectangle(),
                    displayConfig.getHeightRectangle()
            );
            appearance.setPageRect(rect);
        }

        signer.setFieldName(fieldName);
    }
    private void drawSignatureTable(
            PdfSigner signer,
            DisplayConfig displayConfig
    ) {

        float[] widths = displayConfig.getWidthsPercen();
        Table table = new Table(UnitValue.createPercentArray(widths))
                .setWidth(UnitValue.createPercentValue(100));

        for (int i = 0; i < displayConfig.getTextArray().length; i++) {
            Cell cell = new Cell()
                    .add(new Paragraph(displayConfig.getTextArray()[i]))
                    .setTextAlignment(displayConfig.getAlignmentArray()[i]);
            table.addCell(cell);
        }

        PdfSignatureAppearance appearance = signer.getSignatureAppearance();
        Canvas canvas = new Canvas(
                appearance.getLayer2(),
                signer.getDocument()
        );
        canvas.add(table);
        canvas.close();
    }


}
