package com.viettel.signature.utils;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xml.security.utils.Base64;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.X509Principal;

public class CertUtils {
    public CertUtils() {
    }

    public static String getCNFromDN(String dn) {
        String[] array = dn.split(",");

        for(int i = 0; i < array.length; ++i) {
            String a = array[i];
            String[] b = a.split("=");
            if (b.length >= 2 && "CN".toLowerCase().equals(b[0].toLowerCase())) {
                if (b.length <= 2) {
                    return b[1];
                }

                String[] c = new String[b.length - 1];

                for(int j = 1; j < b.length; ++j) {
                    c[j - 1] = b[j];
                }

                return join(c, "=");
            }
        }

        return null;
    }

    public static String join(String[] list, String delim) {
        StringBuilder sb = new StringBuilder();
        String loopDelim = "";

        for(String s : list) {
            sb.append(loopDelim);
            sb.append(s);
            loopDelim = delim;
        }

        return sb.toString();
    }

    public static X509Certificate getX509Cert(String CertStr) {
        CertificateFactory cf = null;

        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        if (CertStr != null && !CertStr.isEmpty()) {
            try {
                return (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(Base64.decode(CertStr.getBytes())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static String convertTVKhongDau(String str) {
        String[] a = new String[]{"à", "á", "ạ", "ả", "ã", "â", "ầ", "ấ", "ậ", "ẩ", "ẫ", "ă", "ằ", "ắ", "ặ", "ẳ", "ẵ"};
        String[] aUpper = new String[]{"À", "Á", "Ạ", "Ả", "Ã", "Â", "Ầ", "Ấ", "Ầ", "Ẩ", "Ẫ", "Ă", "Ằ", "Ắ", "Ằ", "Ẳ", "Ẵ"};
        String[] e = new String[]{"è", "é", "ẹ", "ẻ", "ẽ", "ê", "ề", "ế", "ệ", "ể", "ễ"};
        String[] eUpper = new String[]{"È", "É", "Ẹ", "Ẻ", "Ẽ", "Ê", "Ề", "Ế", "Ề", "Ể", "Ễ"};
        String[] i = new String[]{"ì", "í", "ị", "ỉ", "ĩ"};
        String[] iUpper = new String[]{"Ì", "Í", "Ị", "Ỉ", "Ĩ"};
        String[] o = new String[]{"ò", "ó", "ọ", "ỏ", "õ", "ô", "ồ", "ố", "ộ", "ổ", "ỗ", "ơ", "ờ", "ớ", "ợ", "ở", "ỡ"};
        String[] oUpper = new String[]{"Ò", "Ó", "Ọ", "Ỏ", "Õ", "Ô", "Ồ", "Ố", "Ộ", "Ổ", "Ỗ", "Ơ", "Ờ", "Ớ", "Ợ", "Ở", "Ỡ"};
        String[] u = new String[]{"ù", "ú", "ụ", "ủ", "ũ", "ư", "ừ", "ứ", "ự", "ử", "ữ"};
        String[] uUpper = new String[]{"Ù", "Ú", "Ụ", "Ủ", "Ũ", "Ư", "Ừ", "Ứ", "Ự", "Ử", "Ữ"};
        String[] y = new String[]{"ỳ", "ý", "ỵ", "ỷ", "ỹ"};
        String[] yUpper = new String[]{"Ỳ", "Ý", "Y", "Ỷ", "Ỹ"};
        str = str.replaceAll("đ", "d");
        str = str.replaceAll("Đ", "D");

        for(String a1 : a) {
            str = str.replaceAll(a1, "a");
        }

        for(String a1 : aUpper) {
            str = str.replaceAll(a1, "A");
        }

        for(String e1 : e) {
            str = str.replaceAll(e1, "e");
        }

        for(String e1 : eUpper) {
            str = str.replaceAll(e1, "E");
        }

        for(String i1 : i) {
            str = str.replaceAll(i1, "i");
        }

        for(String i1 : iUpper) {
            str = str.replaceAll(i1, "I");
        }

        for(String o1 : o) {
            str = str.replaceAll(o1, "o");
        }

        for(String o1 : oUpper) {
            str = str.replaceAll(o1, "O");
        }

        for(String u1 : u) {
            str = str.replaceAll(u1, "u");
        }

        for(String u1 : uUpper) {
            str = str.replaceAll(u1, "U");
        }

        for(String y1 : y) {
            str = str.replaceAll(y1, "y");
        }

        for(String y1 : yUpper) {
            str = str.replaceAll(y1, "Y");
        }

        return str;
    }

    public static String getCN(X509Certificate cert) {
        try {
            X509Principal principal = PrincipalUtil.getSubjectX509Principal(cert);
            return principal.getValues(X509Principal.CN).firstElement().toString();
        } catch (CertificateEncodingException ex) {
            Logger.getLogger(CertUtils.class.getName()).log(Level.SEVERE, (String)null, ex);
            return null;
        }
    }

    public static String getSubject(X509Certificate certificate) {
        try {
            X509Principal principal = PrincipalUtil.getSubjectX509Principal(certificate);
            Vector vector = principal.getValues(X509Principal.CN);
            return vector.size() != 1 ? "" : vector.firstElement().toString();
        } catch (CertificateEncodingException var3) {
            return "";
        }
    }

    public static String getOrganization(X509Certificate certificate) {
        try {
            X509Principal principal = PrincipalUtil.getSubjectX509Principal(certificate);
            Vector vector = principal.getValues(X509Principal.O);
            return vector.size() != 1 ? "" : vector.firstElement().toString();
        } catch (CertificateEncodingException var3) {
            return "";
        }
    }

    public static String getOrganizationUnit(X509Certificate certificate) {
        try {
            X509Principal principal = PrincipalUtil.getSubjectX509Principal(certificate);
            Vector vector = principal.getValues(X509Principal.OU);
            return vector.size() != 1 ? "" : vector.firstElement().toString();
        } catch (CertificateEncodingException var3) {
            return "";
        }
    }

    public static String getLocation(X509Certificate certificate) {
        try {
            X509Principal principal = PrincipalUtil.getSubjectX509Principal(certificate);
            Vector vector = principal.getValues(X509Principal.L);
            return vector.size() != 1 ? "" : vector.firstElement().toString();
        } catch (CertificateEncodingException var3) {
            return "";
        }
    }

    public static String getIssuerName(X509Certificate certificate) {
        try {
            X509Principal principal = PrincipalUtil.getIssuerX509Principal(certificate);
            Vector vector = principal.getValues(X509Principal.CN);
            return vector.size() != 1 ? "" : vector.firstElement().toString();
        } catch (CertificateEncodingException var3) {
            return "";
        }
    }
}
