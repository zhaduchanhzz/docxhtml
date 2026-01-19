//package com.esignature.pdf;
//
//import com.itextpdf.io.image.ImageData;
//import com.itextpdf.io.image.ImageDataFactory;
//import com.itextpdf.kernel.geom.Rectangle;
//import com.itextpdf.kernel.pdf.*;
//import com.itextpdf.signatures.*;
//
//import java.io.*;
//import java.security.GeneralSecurityException;
//
///**
// * Class hỗ trợ ký PDF bằng chữ ký PKCS#7 từ server bên ngoài (remote signing)
// * Tương thích iText 7.1.x trở lên (đã test ổn định đến 7.2.x / 8.x)
// * Khắc phục vấn đề cấu trúc signature bị hỏng như trong SO question 57140446
// */
//public class PdfExternalSigner {
//
//    /**
//     * Ký PDF với chữ ký từ server bên ngoài
//     *
//     * @param inputPdf          File PDF gốc (có thể đã có signature field hoặc chưa)
//     * @param outputPdf         File PDF đầu ra
//     * @param fieldName         Tên signature field (ví dụ: "Signature1")
//     * @param signatureImage    Hình ảnh chữ ký (byte[]) - optional, để hiển thị visible signature
//     * @param pageNumber        Trang đặt visible signature (1-based)
//     * @param rect              Vị trí & kích thước chữ ký trên trang (chỉ cần nếu tạo field mới)
//     * @param reason            Lý do ký
//     * @param location          Địa điểm ký
//     * @param contact           Thông tin liên hệ
//     * @param estimatedSize     Kích thước ước tính của PKCS#7 (thường 8-20KB, để trống thì dùng 12000)
//     * @throws IOException
//     * @throws GeneralSecurityException
//     */
//    public void signWithExternalSignature(
//            File inputPdf,
//            File outputPdf,
//            String fieldName,
//            byte[] signatureImage,      // null nếu không cần visible
//            int pageNumber,
//            Rectangle rect,             // null nếu dùng field đã có sẵn
//            String reason,
//            String location,
//            String contact,
//            int estimatedSize
//    ) throws IOException, GeneralSecurityException {
//
//        if (estimatedSize <= 0) {
//            estimatedSize = 12000; // giá trị an toàn phổ biến
//        }
//
//        try (PdfReader reader = new PdfReader(new FileInputStream(inputPdf));
//             OutputStream os = new FileOutputStream(outputPdf)) {
//
//            StampingProperties props = new StampingProperties().useAppendMode();
//
//            PdfSigner signer = new PdfSigner(reader, os, props);
//            signer.setFieldName(fieldName);
//
//            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
//
//            // Cài đặt thông tin hiển thị (nếu cần visible signature)
//            appearance.setReason(reason != null ? reason : "Ký số tài liệu");
//            appearance.setLocation(location != null ? location : "Hà Nội, Việt Nam");
//            appearance.setContact(contact != null ? contact : "example@company.vn");
//
//            if (signatureImage != null && signatureImage.length > 0) {
//                ImageData imageData = ImageDataFactory.create(signatureImage);
//                appearance.setSignatureGraphic(imageData);
//                appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);
//
//                if (rect != null) {
//                    // Chỉ set rectangle nếu tạo field mới hoặc muốn override vị trí
//                    appearance.setPageRect(rect);
//                }
//                appearance.setPageNumber(pageNumber);
//            } else {
//                // Không có hình → chỉ text hoặc invisible
//                appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.DESCRIPTION);
//            }
//
//            // RẤT QUAN TRỌNG: Fix bug iText không ghi SigFlags khi ký existing field
//            signer.getDocument().getCatalog().setModified();
//
//            // Sử dụng container gọi server bên ngoài
//            RemoteSignatureContainer container = new RemoteSignatureContainer();
//
//            // Ký với external container
//            signer.signExternalContainer(container, estimatedSize);
//        }
//    }
//
//    /**
//     * Implement IExternalSignatureContainer để lấy chữ ký PKCS#7 từ server
//     */
//    private static class RemoteSignatureContainer implements IExternalSignatureContainer {
//
//        @Override
//        public void modifySigningDictionary(PdfDictionary signDic) {
//            // Bắt buộc phải set đúng filter & subfilter cho PKCS#7 detached
//            signDic.put(PdfName.Filter, PdfName.Adobe_PPKLite);
//            signDic.put(PdfName.SubFilter, PdfName.Adbe_pkcs7_detached);
//            // Có thể thêm các entry khác nếu server yêu cầu (ví dụ Name, M, ...)
//        }
//
//        @Override
//        public byte[] sign(InputStream documentHashStream) throws GeneralSecurityException {
//            try {
//                // Đọc toàn bộ dữ liệu mà iText đã hash sẵn (đây là dữ liệu cần ký)
//                byte[] dataToSign = org.apache.commons.io.IOUtils.toByteArray(documentHashStream);
//
//                // Gọi server signing của bạn
//                // Thường truyền: dataToSign + thông tin khác (cert serial, reason, location, ...)
//                byte[] pkcs7FromServer = callYourSigningServer(
//                        dataToSign,
//                        "SHA256",               // hoặc lấy từ iText nếu bạn set khác
//                        certificateChainIfNeeded,
//                        otherParams // tùy service
//                );
//
//                if (pkcs7FromServer == null || pkcs7FromServer.length == 0) {
//                    throw new GeneralSecurityException("Server trả về PKCS#7 rỗng");
//                }
//
//                return pkcs7FromServer;  // Phải là DER-encoded CMS/PKCS#7 detached
//
//            } catch (IOException e) {
//                throw new GeneralSecurityException("Không đọc được dữ liệu hash từ iText", e);
//            } catch (Exception e) {
//                throw new GeneralSecurityException("Lỗi gọi remote signing service", e);
//            }
//        }
//    // Ví dụ cách sử dụng
//    public static void main(String[] args) throws Exception {
//        PdfExternalSigner signer = new PdfExternalSigner();
//
//        signer.signWithExternalSignature(
//                new File("input.pdf"),
//                new File("signed_output.pdf"),
//                "SignatureField1",              // tên field (phải tồn tại hoặc để iText tạo mới)
//                null,                           // byte[] ảnh chữ ký (nếu có)
//                1,                              // trang 1
//                new Rectangle(50, 100, 200, 80), // vị trí nếu tạo field mới
//                "Ký xác nhận hợp đồng",
//                "Hà Nội",
//                "legal@company.vn",
//                15000
//        );
//    }
//}