/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.webdav;

import com.itextpdf.layout.properties.TextAlignment;
import com.viettel.signature.pdf.DisplayConfig;
import com.viettel.signature.pdf.SignPdfAsynchronous7;
import com.viettel.signature.plugin.SignPdfFileIText7;
import com.viettel.signature.utils.CertUtils;

import java.io.File;
import java.security.cert.X509Certificate;

public class HashFilePDF {

    public static String getHashTypeNoDisplay_ExistedSignatureField(SignPdfFileIText7 pdfSig, String filePath, X509Certificate[] certChain, String fontPath, String fieldName) {

        String contact = CertUtils.getCN(certChain[0]);
        String reason = "Ký số";
        String location = "Hà Nội";
        DisplayConfig displayConfig = DisplayConfig.generateDisplayConfigNoDisplay_ExistSignatureField(contact, reason, location, fieldName);
//        String digestAlg = SignPdfAsynchronous.HASH_ALGORITHM_SHA1;
        String digestAlg = SignPdfAsynchronous7.HASH_ALGORITHM_SHA256;
        String cryptAlg = SignPdfAsynchronous7.CRYPT_ALGORITHM_RSA;
        String base64Hash = pdfSig.createHash(filePath, certChain, digestAlg, cryptAlg, displayConfig);
        return base64Hash;
    }

    public static String getHashTypeNoDisplay(SignPdfFileIText7 pdfSig, String filePath, X509Certificate[] certChain, String fontPath) {
        String contact = CertUtils.getCN(certChain[0]);
        String reason = "Ký số";
        String location = "Hà Nội";
        DisplayConfig displayConfig = DisplayConfig.generateDisplayConfigNoDisplay(contact, reason, location);
//        String digestAlg = SignPdfAsynchronous.HASH_ALGORITHM_SHA1;
        String digestAlg = SignPdfAsynchronous7.HASH_ALGORITHM_SHA256;
        String cryptAlg = SignPdfAsynchronous7.CRYPT_ALGORITHM_RSA;
        String base64Hash = pdfSig.createHash(filePath, certChain, digestAlg, cryptAlg, displayConfig);
        return base64Hash;
    }

    public static String getHashTypeRectangleText(SignPdfFileIText7 pdfSig, String filePath, X509Certificate[] certChain, String fontPath) {
        int numberPageSign = 2;
        float widthRectangle = DisplayConfig.WIDTH_RECTANGLE_DEFAULT;
        float heightRectangle = DisplayConfig.HEIGHT_RECTANGLE_DEFAULT;
        int locateSign = DisplayConfig.LOCATE_SIGN_DEFAULT;
        float marginTopOfRectangle = DisplayConfig.MARGIN_TOP_OF_RECTANGLE_DEFAULT;
        float marginBottomOfRectangle = 320.0F;
        float marginRightOfRectangle = 10.0F;
        float marginLeftOfRectangle = 320.0F;
        String displayText = DisplayConfig.SIGN_TEXT_FORMAT_1;
        String formatRectangleText = DisplayConfig.FORMAT_RECTANGLE_TEXT_DEFAULT;
        String contact = CertUtils.getCN(certChain[0]);
        
        String reason = "Ký số";
        String location = "Hà Nội";
        String dateFormatString = DisplayConfig.DATE_FORMAT_STRING_DEFAULT;
        int sizeFont = DisplayConfig.SIZE_FONT_DEFAULT;
        String ou = DisplayConfig.ORGANIZATION_UNIT_DEFAULT_EMPTY;
        String o = DisplayConfig.ORGANIZATION_DEFAULT_EMPTY;
        DisplayConfig displayConfig = DisplayConfig.generateDisplayConfigRectangleText(
                numberPageSign, widthRectangle, heightRectangle, locateSign,
                marginTopOfRectangle, marginBottomOfRectangle,
                marginRightOfRectangle, marginLeftOfRectangle,
                displayText, formatRectangleText,
                contact, reason, location,
                dateFormatString, fontPath, sizeFont,
                ou, o);
//        displayConfig.setSignDate();
        displayConfig.getSignDate();
//        String digestAlg = SignPdfAsynchronous.HASH_ALGORITHM_SHA1;
        String digestAlg = SignPdfAsynchronous7.HASH_ALGORITHM_SHA256;
        String cryptAlg = SignPdfAsynchronous7.CRYPT_ALGORITHM_RSA;
        return pdfSig.createHash(filePath, certChain, digestAlg, cryptAlg, displayConfig);
    }

//    public static String getHashTypeRectangleText_ExistedSignatureField(SignPdfFileIText7 pdfSig, String filePath, X509Certificate[] certChain, String fontPath, String displayText, String fieldName) {
////        String displayText = DisplayConfig.DISPLAY_TEXT_DEFAULT_EMPTY;
//        String formatRectangleText = DisplayConfig.FORMAT_RECTANGLE_TEXT_DEFAULT;
//        String contact = CertUtils.getCN(certChain[0]);
//        String reason = "Ký số";
//        String location = "Hà Nội";
//        String dateFormatString = DisplayConfig.DATE_FORMAT_STRING_DEFAULT;
//        int sizeFont = DisplayConfig.SIZE_FONT_DEFAULT;
//
//        String ou = DisplayConfig.ORGANIZATION_UNIT_DEFAULT_EMPTY;
//        String o = DisplayConfig.ORGANIZATION_DEFAULT_EMPTY;
//        DisplayConfig displayConfig = DisplayConfig.generateDisplayConfigRectangleText_ExistedSignatureField(
//                displayText, formatRectangleText,
//                contact, reason, location,
//                dateFormatString, fontPath, sizeFont, fieldName,
//                ou, o);
////        String digestAlg = SignPdfAsynchronous.HASH_ALGORITHM_SHA1;
//        String digestAlg = SignPdfAsynchronous.HASH_ALGORITHM_SHA256;
//        String cryptAlg = SignPdfAsynchronous.CRYPT_ALGORITHM_RSA;
//        String base64Hash = pdfSig.createHash(filePath, certChain, digestAlg, cryptAlg, displayConfig);
//        return base64Hash;
//    }
//
    public static String getHashTypeImageTextKBNN(SignPdfFileIText7 pdfSig, String filePath,
            X509Certificate[] certChain, String fontPath, String pathImage, String displayText) {
        int numberPageSign = DisplayConfig.NUMBER_PAGE_SIGN_DEFAULT;
        float widthRectangle = DisplayConfig.WIDTH_RECTANGLE_DEFAULT;
        float heightRectangle = DisplayConfig.HEIGHT_RECTANGLE_DEFAULT;
        int locateSign = 3;

        float marginTopOfRectangle = 10.0F;
        //float marginBottomOfRectangle = DisplayConfig.MARGIN_BOTTOM_OF_RECTANGLE_DEFAULT;
        //float marginRightOfRectangle = DisplayConfig.MARGIN_RIGHT_OF_RECTANGLE_DEFAULT;
        //float marginLeftOfRectangle = DisplayConfig.MARGIN_LEFT_OF_RECTANGLE_DEFAULT;

        float marginBottomOfRectangle = 320.0F;
        float marginRightOfRectangle = 10.0F;
        float marginLeftOfRectangle = 320.0F;
        
        String displayText1 = DisplayConfig.SIGN_TEXT_FORMAT_4;
       
        String formatRectangleText = DisplayConfig.FORMAT_RECTANGLE_TEXT_DEFAULT;
        String contact = CertUtils.getCN(certChain[0]);
        String reason = "Ký số";
        String location = "Hà Nội";
        String dateFormatString = DisplayConfig.DATE_FORMAT_STRING_DEFAULT;
        int sizeFont = DisplayConfig.SIZE_FONT_DEFAULT;
        String ou = DisplayConfig.ORGANIZATION_UNIT_DEFAULT_EMPTY;
        String o = DisplayConfig.ORGANIZATION_DEFAULT_EMPTY;
        DisplayConfig displayConfig = DisplayConfig.generateDisplayConfigImageText(
                numberPageSign, widthRectangle, heightRectangle, locateSign,
                marginTopOfRectangle, marginBottomOfRectangle,
                marginRightOfRectangle, marginLeftOfRectangle,
                displayText1, formatRectangleText,
                contact, reason, location,
                dateFormatString, fontPath, sizeFont,
                ou, o,
                pathImage);
//        String digestAlg = SignPdfAsynchronous.HASH_ALGORITHM_SHA1;
        String digestAlg = SignPdfAsynchronous7.HASH_ALGORITHM_SHA256;
        String cryptAlg = SignPdfAsynchronous7.CRYPT_ALGORITHM_RSA;
        String base64Hash = pdfSig.createHash(filePath, certChain, digestAlg, cryptAlg, displayConfig);
        return base64Hash;
    }

    public static String getHashTypeImageText(SignPdfFileIText7 pdfSig, String filePath,
            X509Certificate[] certChain, String fontPath, String pathImage) {
        int numberPageSign = DisplayConfig.NUMBER_PAGE_SIGN_DEFAULT;
        float widthRectangle = DisplayConfig.WIDTH_RECTANGLE_DEFAULT;
        float heightRectangle = DisplayConfig.HEIGHT_RECTANGLE_DEFAULT;
        int locateSign = DisplayConfig.LOCATE_SIGN_DEFAULT;

        float marginTopOfRectangle = DisplayConfig.MARGIN_TOP_OF_RECTANGLE_DEFAULT;
        float marginBottomOfRectangle = DisplayConfig.MARGIN_BOTTOM_OF_RECTANGLE_DEFAULT;
        float marginRightOfRectangle = DisplayConfig.MARGIN_RIGHT_OF_RECTANGLE_DEFAULT;
        float marginLeftOfRectangle = DisplayConfig.MARGIN_LEFT_OF_RECTANGLE_DEFAULT;

        String displayText = DisplayConfig.DISPLAY_TEXT_DEFAULT_EMPTY;
        String formatRectangleText = DisplayConfig.FORMAT_RECTANGLE_TEXT_DEFAULT;
        String contact = CertUtils.getCN(certChain[0]);
        String reason = "Ký số";
        String location = "Hà Nội";
        String dateFormatString = DisplayConfig.DATE_FORMAT_STRING_DEFAULT;
        int sizeFont = DisplayConfig.SIZE_FONT_DEFAULT;
        String ou = DisplayConfig.ORGANIZATION_UNIT_DEFAULT_EMPTY;
        String o = DisplayConfig.ORGANIZATION_DEFAULT_EMPTY;
        DisplayConfig displayConfig = DisplayConfig.generateDisplayConfigImageText(
                numberPageSign, widthRectangle, heightRectangle, locateSign,
                marginTopOfRectangle, marginBottomOfRectangle,
                marginRightOfRectangle, marginLeftOfRectangle,
                displayText, formatRectangleText,
                contact, reason, location,
                dateFormatString, fontPath, sizeFont,
                ou, o,
                pathImage);
//        String digestAlg = SignPdfAsynchronous.HASH_ALGORITHM_SHA1;
        String digestAlg = SignPdfAsynchronous7.HASH_ALGORITHM_SHA256;
        String cryptAlg = SignPdfAsynchronous7.CRYPT_ALGORITHM_RSA;
        String base64Hash = pdfSig.createHash(filePath, certChain, digestAlg, cryptAlg, displayConfig);
        return base64Hash;
    }

    public static String getHashTypeImageText_ExistedSignatureField(SignPdfFileIText7 pdfSig,
            String filePath, X509Certificate[] certChain, String fontPath,
            String displayText, String fieldName, String pathImage) {
//        String displayText = DisplayConfig.DISPLAY_TEXT_DEFAULT_EMPTY;
        String formatRectangleText = DisplayConfig.FORMAT_RECTANGLE_TEXT_DEFAULT;
        String contact = CertUtils.getCN(certChain[0]);
        String reason = "Ký số";
        String location = "Hà Nội";
        String dateFormatString = DisplayConfig.DATE_FORMAT_STRING_DEFAULT;
        int sizeFont = DisplayConfig.SIZE_FONT_DEFAULT;

        String ou = DisplayConfig.ORGANIZATION_UNIT_DEFAULT_EMPTY;
        String o = DisplayConfig.ORGANIZATION_DEFAULT_EMPTY;
        DisplayConfig displayConfig = DisplayConfig.generateDisplayConfigImageText_ExistedSignatureField(
                displayText, formatRectangleText,
                contact, reason, location,
                dateFormatString, fontPath, sizeFont, fieldName,
                ou, o,
                pathImage);
//        String digestAlg = SignPdfAsynchronous.HASH_ALGORITHM_SHA1;
        String digestAlg = SignPdfAsynchronous7.HASH_ALGORITHM_SHA256;
        String cryptAlg = SignPdfAsynchronous7.CRYPT_ALGORITHM_RSA;
        String base64Hash = pdfSig.createHash(filePath, certChain, digestAlg, cryptAlg, displayConfig);
        return base64Hash;
    }

    public static String getHashTypeImage(SignPdfFileIText7 pdfSig, String filePath, X509Certificate[] certChain, String pathImage) {
        int numberPageSign = DisplayConfig.NUMBER_PAGE_SIGN_DEFAULT;
        float widthRectangle = DisplayConfig.WIDTH_RECTANGLE_DEFAULT;
        float heightRectangle = DisplayConfig.HEIGHT_RECTANGLE_DEFAULT;
        int locateSign = DisplayConfig.LOCATE_SIGN_DEFAULT;
        float marginTopOfRectangle = DisplayConfig.MARGIN_TOP_OF_RECTANGLE_DEFAULT;
        float marginBottomOfRectangle = DisplayConfig.MARGIN_BOTTOM_OF_RECTANGLE_DEFAULT;
        float marginRightOfRectangle = DisplayConfig.MARGIN_RIGHT_OF_RECTANGLE_DEFAULT;
        float marginLeftOfRectangle = DisplayConfig.MARGIN_LEFT_OF_RECTANGLE_DEFAULT;
        String contact = CertUtils.getCN(certChain[0]);
        String reason = "Ký số";
        String location = "Hà Nội";
        DisplayConfig displayConfig = DisplayConfig.generateDisplayConfigImage(
                numberPageSign, widthRectangle, heightRectangle, locateSign,
                0, 0,
                0, 0,
                contact, reason, location,
                pathImage
        );
        System.out.println( displayConfig.getSignDate());
//        String digestAlg = SignPdfAsynchronous.HASH_ALGORITHM_SHA1;
        String digestAlg = SignPdfAsynchronous7.HASH_ALGORITHM_SHA256;
        String cryptAlg = SignPdfAsynchronous7.CRYPT_ALGORITHM_RSA;
        String base64Hash = pdfSig.createHash(filePath, certChain, digestAlg, cryptAlg, displayConfig);
        return base64Hash;
    }

    public static String getHashTypeImage_ExistedSignatureField(SignPdfFileIText7 pdfSig, String filePath, X509Certificate[] certChain, String pathImage, String fieldName) {
        String contact = CertUtils.getCN(certChain[0]);
        String reason = "Ký số";
        String location = "Hà Nội";
        DisplayConfig displayConfig = DisplayConfig.generateDisplayConfigImage_ExistedSignatureField(
                contact, reason, location,
                pathImage, fieldName
        );
//        String digestAlg = SignPdfAsynchronous.HASH_ALGORITHM_SHA1;
        String digestAlg = SignPdfAsynchronous7.HASH_ALGORITHM_SHA256;
        String cryptAlg = SignPdfAsynchronous7.CRYPT_ALGORITHM_RSA;
        String base64Hash = pdfSig.createHash(filePath, certChain, digestAlg, cryptAlg, displayConfig);
        return base64Hash;
    }

//    public static String getHashTypeTableDefault(SignPdfFileIText7 pdfSig, String filePath, X509Certificate[] certChain, String fontPath) {
//
//        String contact = CertUtils.getCN(certChain[0]);
//        String textArray[] = {"1", contact, "unit", "dateString", "opinion"};
//        DisplayConfig displayConfig = DisplayConfig.generateDisplayConfigTableDefault(2, textArray, fontPath);
//
//        int timeSign = 1;
//        String base64Hash;
////        String digestAlg = SignPdfAsynchronous.HASH_ALGORITHM_SHA1;
//        String digestAlg = SignPdfAsynchronous7.HASH_ALGORITHM_SHA256;
//        String cryptAlg = SignPdfAsynchronous7.CRYPT_ALGORITHM_RSA;
//        if (timeSign == 1) {
//            pdfSig.addPageEmpty(filePath, filePath + ".temp", displayConfig);
//            base64Hash = pdfSig.createHash(filePath + ".temp", certChain, digestAlg, cryptAlg, displayConfig);
//
//            File fileTemp = new File(filePath + ".temp");
//            if (fileTemp.exists()) {
//                fileTemp.delete();
//            }
//        } else {
//            base64Hash = pdfSig.createHash(filePath, certChain, digestAlg, cryptAlg, displayConfig);
//        }
//        return base64Hash;
//    }

    /**
     * Hien thi chu ky dang bang
     */
    public static String getHashTypeTable(SignPdfFileIText7 pdfSig, String filePath, X509Certificate[] certChain, String fontPath) {

        String[] titles = new String[5];

        titles[0] = "STT";
        titles[1] = "Người Ký";
        titles[2] = "Đơn vị";
        titles[3] = "Thời gian ký";
        titles[4] = "Ý kiến";

        float[] widthsPercen = new float[5];
        widthsPercen[0] = 0.06f;
        widthsPercen[1] = 0.18f;
        widthsPercen[2] = 0.2f;
        widthsPercen[3] = 0.14f;
        widthsPercen[4] = 0.42f;


        TextAlignment[] alignmentArray = new TextAlignment[5];
        alignmentArray[0] = TextAlignment.CENTER;
        alignmentArray[1] = TextAlignment.LEFT;
        alignmentArray[2] = TextAlignment.JUSTIFIED;
        alignmentArray[3] = TextAlignment.LEFT;
        alignmentArray[4] = TextAlignment.JUSTIFIED;

        String contact = CertUtils.getCN(certChain[0]);
        String textArray[] = {"1", contact, "unit", "dateString", "opinion"};
        DisplayConfig displayConfig = DisplayConfig.generateDisplayConfigTable(2, titles, widthsPercen, alignmentArray, textArray, fontPath);

        int timeSign = 1;
        String base64Hash;
//        String digestAlg = SignPdfAsynchronous.HASH_ALGORITHM_SHA1;
        String digestAlg = SignPdfAsynchronous7.HASH_ALGORITHM_SHA256;
        String cryptAlg = SignPdfAsynchronous7.CRYPT_ALGORITHM_RSA;
        if (timeSign == 1) {
//            pdfSig.addPageEmpty(filePath, filePath + ".temp", displayConfig);
            base64Hash = pdfSig.createHash(filePath + ".temp", certChain, digestAlg, cryptAlg, displayConfig);
            File fileTemp = new File(filePath + ".temp");
            if (fileTemp.exists()) {
                fileTemp.delete();
            }
        } else {
            base64Hash = pdfSig.createHash(filePath, certChain, digestAlg, cryptAlg, displayConfig);
        }
        return base64Hash;
    }
}
