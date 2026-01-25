//package com.esignature.pdf;
//
//import com.esignature.signer.BaseHashSigner;
//import com.esignature.signer.HashSignerException;
//import com.esignature.signer.SignatureParameter;
//import com.esignature.signer.SignerProfile;
//import com.esignature.utils.MessageDigestAlgorithm;
//import com.esignature.utils.X509CertificateInfo;
//import org.bouncycastle.util.encoders.Base64;
//
//import javax.naming.InvalidNameException;
//import javax.naming.ldap.LdapName;
//import javax.naming.ldap.Rdn;
//import java.io.*;
//import java.net.MalformedURLException;
//import java.security.*;
//import java.security.cert.*;
//import java.security.cert.Certificate;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//import java.util.UUID;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * @author TuanBS (tuanbs@vnpt.vn)
// *
// */
//public class PdfHashSigner implements Serializable {
//	protected SignatureParameter param;
//	protected String HASH_ALGORITHM = "SHA1";
//	protected static final String ENCRYPT_ALGORITHM = "RSA";
//	protected MessageDigestAlgorithm _hashAlgorithm = MessageDigestAlgorithm.SHA256;
//	private PdfReader reader = null;
//	private PdfStamper stamper = null;
//	private PdfSignatureAppearance sap = null;
//	private PdfSignature dic = null;
//	private PdfPKCS7 sgn = null;
//
//	private String signerName = "Me";
//	private String issuerName = "CA";
//
//	private X509Certificate signer;
//	private X509Certificate[] certChain;
//
//	private ByteArrayOutputStream outStream;
//
//	private byte[] hash;
//	private byte[] _hashOnlyBytes;
//	private byte[] secondHash;
//	private Calendar signingTime;
//	private Font font;
//	private PdfSignatureView signatureView;
//	private int r = 0, g = 0, b = 0;
//	private PdfSignatureAppearance.RenderingMode renderMode;
//	private FontName fontName;
//	private FontStyle fontStyle;
//	private int fontSize;
//	private String reason;
//	private String location;
//	private String contact;
//	private TSAClient tsaClient;
//
//	private String tsaUrl;
//	private String tsaUsername;
//	private String tsaPassword;
//
//	public enum FontName {
//		Times_New_Roman, Roboto, Arial
//	}
//
//	public enum FontStyle {
//		Normal, Bold, Italic, BoldItalic, Underline
//	}
//
//	private int _estimatedSize = 0;
//
//	private static final String X509CERT = "x509";
////	private static final String CIPHER_CONFIG = "RSA/ECB/PKCS1Padding";
//	private Image image;
//	private static final String DEFAULT_IMG = "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";
//
//	private String signatureText;
//
//	/**
//	 * 
//	 */
//	public PdfHashSigner() {
//	}
//
//	private void calculateSignature() throws Exception {
//		outStream = new ByteArrayOutputStream();
//
//		try {
//			reader = new PdfReader(this.param.getUnsignData());
//		} catch (IOException e) {
//			throw e;
//		}
//
//		ExternalDigest externalDigest = new ExternalDigest() {
//			@Override
//			public MessageDigest getMessageDigest(String hashAlgorithm) throws GeneralSecurityException {
//				return DigestAlgorithms.getMessageDigest(hashAlgorithm, null);
//			}
//		};
//		try {
//			sgn = new PdfPKCS7(null, certChain, HASH_ALGORITHM, "BC", null, false);
//		} catch (InvalidKeyException e) {
//			throw e;
//		} catch (NoSuchProviderException e) {
//			throw e;
//		} catch (NoSuchAlgorithmException e) {
//			throw e;
//		}
//
//		try {
//			stamper = PdfStamper.createSignature(reader, outStream, '\0', null, true);
//		} catch (DocumentException e2) {
//			try{
//				reader = new PdfReader(this.param.getUnsignData());
//				stamper = PdfStamper.createSignature(reader, outStream, '\0', null, false);
//			}
//			catch (DocumentException e3){
//				throw new HashSignerException(e2.getMessage(), e2);
//			}
//
//		}
//		sap = stamper.getSignatureAppearance();
//
//		initSignatureFont();
//		sap.setLayer2Font(font);
//
////		sap.setVisibleSignature(new Rectangle(10, 10), 1, _calculateSigFieldName(reader, "SignService-Signature"));
//		if (this.signatureText == null || this.signatureText.isEmpty()) {
//			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss dd:MM:yyyy");
//			String singingTimeStr = df.format(signingTime.getTime());
//			this.signatureText = String.format("Ký bởi: %s\nNgày ký: %s\nTổ chức xác thực: %s", signerName,
//					singingTimeStr, issuerName);
//		}
//		sap.setCertificate(this.signer);
//		sap.setLayer2Text(this.signatureText);
//
//		dic = new PdfSignature(PdfName.ADOBE_PPKLITE, new PdfName("adbe.pkcs7.detached"));
//		dic.setDate(new PdfDate(signingTime));
//		dic.setName(this.signerName);
//		dic.setReason(this.reason);
//		dic.setLocation(this.location);
//		dic.setContact(this.contact);
//
//		try {
//			signatureView.setId(UUID.randomUUID().toString());
//			sap.setVisibleSignature(signatureView.getRectangle(), signatureView.getPage(), signatureView.getId());
//		} catch (Exception e) {
//			throw e;
//		}
//
//		sap.setCryptoDictionary(dic);
//		sap.setReuseAppearance(false);
//		sap.setRenderingMode(this.renderMode);
//
//		if (this.image == null) {
//			throw new HashSignerException("Graphic mode signature require image data");
//		} else {
//			sap.setSignatureGraphic(this.image);
////			sap.setImage(this.image);
//		}
//
//		_estimatedSize = _calculateEstimatedSignatureSize(certChain, tsaClient, null, null);
//
//		// Async signing
//		ExternalSignatureContainer external = new ExternalBlankSignatureContainer(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
//		MakeSignature.signExternalContainer(sap, external, 8192);
//
//		tempData = outStream.toByteArray();
//		BouncyCastleDigest digest = new BouncyCastleDigest();
//		PdfPKCS7 sgn = new PdfPKCS7(null, certChain, HASH_ALGORITHM, "BC", digest, false);
//		InputStream inp = sap.getRangeStream();
//
//		_hashOnlyBytes = DigestAlgorithms.digest(inp, digest.getMessageDigest(HASH_ALGORITHM));
//
//		_signerInfoData = sgn.getAuthenticatedAttributeBytes(_hashOnlyBytes, signingTime, null, null, MakeSignature.CryptoStandard.CMS);
//		secondHash = DigestAlgorithms.digest(new ByteArrayInputStream(_signerInfoData),
//				externalDigest.getMessageDigest(HASH_ALGORITHM));
//		reader.close();
//		stamper.close();
//	}
//	private byte[] _signerInfoData;
//	private byte[] tempData;
//	public byte[] getTempPdf()
//	{
//		return tempData;
//	}
//
//	/**
//	 *
//	 * @param reader
//	 * @param fieldName
//	 * @return
//	 */
//	private static String _calculateSigFieldName(PdfReader reader, String fieldName) {
//		return UUID.randomUUID().toString();
//	}
//
//	/**
//	 *
//	 * @return
//	 */
//	private int _calculateEstimatedSignatureSize(Certificate[] certChain, TSAClient tsc, byte[] ocsp, CRL[] crlList) {
//		int estimatedSize = 0;
//
//		for (Certificate cert : certChain) {
//			try {
//				int certSize = cert.getEncoded().length;
//				estimatedSize += certSize;
//
//			} catch (CertificateEncodingException e) {
//			}
//		}
//
//		// add estimate for PKCS#7 structure + hash
//		estimatedSize += 2000;
//
//		// add space for OCSP response
//		if (ocsp != null) {
//			estimatedSize += ocsp.length;
//		}
//
//		if (tsc != null) {
//			// add guess for timestamp response (which we can't really know)
//			final int tscSize = 4096;
//
//			estimatedSize += tscSize;
//		}
//
//		// add estimate for CRL
//		if (crlList != null) {
//			for (CRL crl : crlList) {
//				if (crl instanceof X509CRL) {
//					X509CRL x509Crl = (X509CRL) crl;
//
//					try {
//						int crlSize = x509Crl.getEncoded().length;
//						// the CRL is included twice in the signature...
//						estimatedSize += crlSize * 2;
//
//					} catch (CRLException e) {
//					}
//				}
//			}
//			estimatedSize += 100;
//		}
//
//		return estimatedSize;
//	}
//
//	public void init(SignatureParameter param) throws HashSignerException {
//		this.param = param;
//		CertificateFactory fac;
//		try {
//			fac = CertificateFactory.getInstance(X509CERT);
//			Certificate cert = fac.generateCertificate(new ByteArrayInputStream(Base64.decode(param.getCertBase64())));
//			if (!(cert instanceof X509Certificate)) {
//				throw new HashSignerException("cert property in param must instance of X509Certificate");
//			}
//			signer = (X509Certificate) cert;
//			
//			certChain = new X509Certificate[] { signer };
//		} catch (CertificateException e) {
//			throw new HashSignerException("Cannot init signer certificate. " + e.getMessage());
//		}
//		reason = "Signing document";
//		location = "Signer's office";
//		signingTime = Calendar.getInstance();
//		fontSize = 13;
//		renderMode = PdfSignatureAppearance.RenderingMode.DESCRIPTION;
//		this.fontStyle = FontStyle.Normal;
//		this.fontName = FontName.Times_New_Roman;
//
//		this.setImage(null);
//
//		signerName = getCommonName(signer, false);
//		contact = signerName;
//		issuerName = getCommonName(signer, true);
//	}
//	
//	/**
//	 * 
//	 * @throws HashSignerException cover origin exception
//	 */
//	public void useCertChain() throws HashSignerException {
//		try {
//			List<X509Certificate> certs = new ArrayList<>();
//			X509Certificate c = signer;
//			while(c != null) {
//				certs.add(c);
//				if(X509CertificateInfo.isRoot(c)){
//					break;
//				}
//				X509CertificateInfo certInfo = new X509CertificateInfo(c);
//				c = certInfo.getIssuer();
//			}
//			this.certChain = certs.toArray(new X509Certificate[certs.size()]);
//			
//		} catch (Exception e) {
//			throw new HashSignerException(e.getMessage(), e);
//		}
//	}
//
//	public String getSecondHashAsBase64() throws HashSignerException {
//		if (secondHash != null) {
//			return com.itextpdf.text.pdf.codec.Base64.encodeBytes(secondHash);
//		}
//		try {
//			calculateSignature();
//		} catch (DocumentException e) {
//			throw new HashSignerException(e.getMessage(), e);
//		} catch (IOException e) {
//			throw new HashSignerException(e.getMessage(), e);
//		} catch (GeneralSecurityException e) {
//			throw new HashSignerException(e.getMessage(), e);
//		} catch (Exception e) {
//			String m = e.getMessage();
//			if (m != null && m.contains("The table width must be greater than zero")) {
//				throw new HashSignerException("PdfSignatureView rectangle not valid (llx,lly,rrx,rry)", e);
//			}
//			throw new HashSignerException(e.getMessage(), e);
//		}
//
//		if (secondHash != null) {
//			return com.itextpdf.text.pdf.codec.Base64.encodeBytes(secondHash);
//		}
//
//		return null;
//	}
//
//	public byte[] sign(SignerProfile profile, byte[] signedBytes){
//		try {
//			BouncyCastleDigest digest = new BouncyCastleDigest();
//			PdfPKCS7 sgn = new PdfPKCS7(null, certChain, HASH_ALGORITHM, "BC", digest, false);
//			sgn.setExternalDigest(signedBytes, null, ENCRYPT_ALGORITHM);
//			if(!(profile.getTsaUrl() == null || profile.getTsaUrl().isEmpty())){
//				tsaClient = new TSAClientBouncyCastle(profile.getTsaUrl(), profile.getTsaUsername(), profile.getTsaPassword());
//			}
//
//			byte[] encodedPKCS7 = sgn.getEncodedPKCS7(profile.getDataHashFile(), profile.getSigningTime(), tsaClient, null, null, MakeSignature.CryptoStandard.CMS);
//			reader = new PdfReader(profile.getTempData());
//				MyExternalSignatureContainer external = new MyExternalSignatureContainer(encodedPKCS7);
//				ByteArrayOutputStream stream = new ByteArrayOutputStream();
//				MakeSignature.signDeferred(reader, profile.getFieldName(), stream, external);
//				return stream.toByteArray();
//		} catch (IOException | DocumentException | GeneralSecurityException e) {
//			e.printStackTrace();
//		}
//        return null;
//	}
//
//	private class MyExternalSignatureContainer implements ExternalSignatureContainer
//	{
//		private byte[] signedBytes;
//
//            public MyExternalSignatureContainer(byte[] signedBytes)
//		{
//			this.signedBytes = signedBytes;
//		}
//
//		@Override
//		public byte[] sign(InputStream data) throws GeneralSecurityException {
//			return this.signedBytes;
//		}
//
//		@Override
//		public void modifySigningDictionary(PdfDictionary signDic) {
//
//		}
//	}
//
//	public SignerProfile getSignerProfile(){
//		try {
//			calculateSignature();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//		SignerProfile profile = new SignerProfile();
//		profile.setHashAlgorithm(HASH_ALGORITHM);
//		profile.setDocType("PDF");
//		profile.setEstimatedSize(_estimatedSize);
//		profile.setSigningTime(signingTime);
//		profile.setTempData(tempData);
//		profile.setFieldName(this.signatureView.getId());
//		profile.setDataHashFile(_hashOnlyBytes);
//		profile.setSecondHash(secondHash);
//		profile.setSignerInfo(_signerInfoData);
//
//		profile.setTsaUrl(this.tsaUrl);
//		profile.setTsaUsername(this.tsaUsername);
//		profile.setTsaPassword(this.tsaPassword);
//		profile.setCertBase64(this.param.getCertBase64());
//
//		return profile;
//	}
//
//	public String getFistHashAsBase64() throws HashSignerException {
//		if (_hashOnlyBytes != null) {
//			return com.itextpdf.text.pdf.codec.Base64.encodeBytes(_hashOnlyBytes);
//		}
//
//		try {
//			calculateSignature();
//		} catch (DocumentException e) {
//			throw new HashSignerException(e.getMessage(), e);
//		} catch (IOException e) {
//			throw new HashSignerException(e.getMessage(), e);
//		} catch (GeneralSecurityException e) {
//			throw new HashSignerException(e.getMessage(), e);
//		} catch (Exception e) {
//			String m = e.getMessage();
//			if (m != null && m.contains("The table width must be greater than zero")) {
//				throw new HashSignerException("PdfSignatureView rectangle not valid (llx,lly,rrx,rry)", e);
//			}
//			throw new HashSignerException(e.getMessage(), e);
//		}
//
//		if (_hashOnlyBytes != null) {
//			return com.itextpdf.text.pdf.codec.Base64.encodeBytes(_hashOnlyBytes);
//		}
//
//		return null;
//	}
//
//	public boolean checkHashSignature(SignerProfile signerProfile, String signedHashBase64){
//		Signature verify = null;
//		try {
//			verify = Signature.getInstance( signerProfile.getHashAlgorithm() + "withRSA");
//			CertificateFactory fac = CertificateFactory.getInstance(X509CERT);
//			Certificate cert = fac.generateCertificate(new ByteArrayInputStream(Base64.decode(signerProfile.getCertBase64())));
//			if (!(cert instanceof X509Certificate)) {
//				return false;
//			}
//			X509Certificate signerFromProfile = (X509Certificate) cert;
//			verify.initVerify(signerFromProfile.getPublicKey());
//			verify.update(signerProfile.getSignerInfo(),0,signerProfile.getSignerInfo().length);
//			Boolean r = verify.verify(Base64.decode(signedHashBase64));
//			return r;
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		} catch (CertificateException e) {
//			e.printStackTrace();
//		} catch (SignatureException e) {
//			e.printStackTrace();
//		} catch (InvalidKeyException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}
//
//	public boolean checkHashSignature(String signedHashBase64) throws HashSignerException {
//		Signature verify = null;
//		try {
//			verify = Signature.getInstance(_hashAlgorithm.toString() + "withRSA");
//			CertificateFactory x509 = CertificateFactory.getInstance("X509");
//			verify.initVerify(signer.getPublicKey());
//			verify.update(_hashOnlyBytes);
//			return verify.verify(Base64.decode(signedHashBase64));
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		} catch (CertificateException e) {
//			e.printStackTrace();
//		} catch (SignatureException e) {
//			e.printStackTrace();
//		} catch (InvalidKeyException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}
//
//	public byte[] sign(String externalSignature) throws HashSignerException {
//		try {
//			sgn.setExternalDigest(Base64.decode(externalSignature), null, ENCRYPT_ALGORITHM);
//			byte[] encodedSign = sgn.getEncodedPKCS7(hash, signingTime, tsaClient, null, null, MakeSignature.CryptoStandard.CMS);
//
//			byte[] paddedSign = new byte[_estimatedSize];
//			System.arraycopy(encodedSign, 0, paddedSign, 0, encodedSign.length);
//
//			PdfDictionary dic2 = new PdfDictionary();
//			dic2.put(PdfName.CONTENTS, new PdfString(paddedSign).setHexWriting(true));
//			sap.close(dic2);
//			return outStream.toByteArray();
//		} catch (IOException e) {
//			throw new HashSignerException(e.getMessage(), e);
//		} catch (DocumentException e) {
//			throw new HashSignerException(e.getMessage(), e);
//		}
//	}
//
//	public byte[] sign(String hashValue, String externalSignature) throws HashSignerException {
//		throw new UnsupportedOperationException();
//	}
//
//	public void setHashAlgorithm(MessageDigestAlgorithm alg) {
//		_hashAlgorithm = alg;
//		HASH_ALGORITHM = alg.toString();
//	}
//
//	/**
//	 * 
//	 * @throws HashSignerException
//	 */
//	private void initSignatureFont() throws HashSignerException {
//		String fontDir = "fonts/TIMES.TTF";
//		switch (this.fontName) {
//		case Roboto:
//			fontDir = "fonts/ROBOTOCONDENSED-REGULAR.TTF";
//			break;
//		case Arial:
//			fontDir = "fonts/ARIAL.TTF";
//			break;
//		default:
//			break;
//		}
//		try {
//			BaseFont bf = BaseFont.createFont(fontDir, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
//			font = new Font(bf, this.fontSize);
//			int style = Font.NORMAL;
//			switch (this.fontStyle) {
//			case Normal:
//				style = Font.NORMAL;
//				break;
//			case Bold:
//				style = Font.BOLD;
//				break;
//			case Italic:
//				style = Font.ITALIC;
//				break;
//			case BoldItalic:
//				style = Font.BOLDITALIC;
//				break;
//			case Underline:
//				style = Font.UNDERLINE;
//				break;
//
//			default:
//				break;
//			}
//			font.setStyle(style);
//			font.setColor(r, g, b);
//		} catch (DocumentException e2) {
//			throw new HashSignerException(e2.getMessage(), e2);
//		} catch (IOException e2) {
//			throw new HashSignerException(e2.getMessage(), e2);
//		}
//	}
//
//	/**
//	 * 
//	 * @throws Exception
//	 */
//
//
//	/**
//	 * 
//	 * @param cert
//	 * @param caName
//	 * @return
//	 */
//	private static String getCommonName(X509Certificate cert, boolean caName) {
//		String dnName = "";
//		if (!caName) {
//			dnName = cert.getSubjectDN().getName();
//		} else {
//			dnName = cert.getIssuerDN().getName();
//		}
//		LdapName ldap;
//		try {
//			ldap = new LdapName(dnName);
//			for (Rdn rdn : ldap.getRdns()) {
//				if ("CN".equalsIgnoreCase(rdn.getType())) {
//					return rdn.getValue().toString();
//				}
//			}
//		} catch (InvalidNameException e) {
//		}
//
//		return "Unknown";
//	}
//
//	/**
//	 * Set signature custom image
//	 * 
//	 * @param image image byte array
//	 */
//	public void setImage(byte[] image) {
//		try {
//			this.image = Image.getInstance(image);
//		} catch (Exception ex) {
//			try {
//				this.image = Image.getInstance(com.itextpdf.text.pdf.codec.Base64.decode(DEFAULT_IMG));
//			} catch (BadElementException e) {
//			} catch (MalformedURLException e) {
//			} catch (IOException e) {
//			}
//		}
//	}
//
//	public void setSignatureText(String signatureText) {
//		if (signatureText == null || "".equals(signatureText)) {
//			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//			this.signatureText = "Ký bởi: " + signerName + "\nNgày ký: " + df.format(signingTime.getTime())
//					+ "\nTổ chức xác thực: " + issuerName;
//		} else {
//			this.signatureText = signatureText;
//		}
//	}
//
//	public PdfSignatureAppearance.RenderingMode getRenderMode() {
//		return renderMode;
//	}
//
//	public void setRenderMode(PdfSignatureAppearance.RenderingMode renderMode) {
//		this.renderMode = renderMode;
//	}
//
//	public FontStyle getFontStyle() {
//		return fontStyle;
//	}
//
//	public void setFontStyle(FontStyle fontStyle) {
//		this.fontStyle = fontStyle;
//	}
//
//	public FontName getFontName() {
//		return fontName;
//	}
//
//	public void setFontName(FontName fontName) {
//		this.fontName = fontName;
//	}
//
//	public PdfSignatureView getSignatureView() {
//		return signatureView;
//	}
//
//	public void setSignatureViews(PdfSignatureView signView) {
//		if (signView == null) {
//			signatureView = new PdfSignatureView(1, "10,10,150,85");
//		}
//        else signatureView = signView;
//    }
//
//	public void setFontColor(int r, int g, int b) {
//		this.r = (r > -1 && r < 256) ? r : 0;
//		this.g = (g > -1 && g < 256) ? g : 0;
//		this.b = (b > -1 && b < 256) ? b : 0;
//	}
//
//	/**
//	 * Set signature view text color
//	 * 
//	 * @param colorcode Color code in hex string format
//	 */
//	public void setFontColor(String colorcode) {
//		r = 0;
//		g = 0;
//		b = 0;
//		if (colorcode == null) {
//			return;
//		}
//		if (!colorcode.startsWith("#")) {
//			colorcode = String.format("#%s", colorcode);
//		}
//		String pattern = "^(?i)#?(([0-9a-fA-F]{2}){3}|([0-9a-fA-F]){3})$";
//		Pattern hexPattern = Pattern.compile(pattern);
//		Matcher hexMatcher = hexPattern.matcher(colorcode);
//
//		if (hexMatcher.find()) {
//			int hexInt = Integer.valueOf(colorcode.substring(1), 16).intValue();
//
//			this.r = (hexInt & 0xFF0000) >> 16;
//			this.g = (hexInt & 0xFF00) >> 8;
//			this.b = (hexInt & 0xFF);
//		}
//	}
//
//	public int getFontSize() {
//		return fontSize;
//	}
//
//	public void setFontSize(int fontSize) {
//		this.fontSize = fontSize;
//	}
//
//	public String getReason() {
//		return reason;
//	}
//
//	public void setReason(String reason) {
//		this.reason = reason;
//	}
//
//	public String getLocation() {
//		return location;
//	}
//
//	public void setLocation(String location) {
//		this.location = location;
//	}
//
//	public String getContact() {
//		return contact;
//	}
//
//	public void setContact(String contact) {
//		this.contact = contact;
//	}
//
//	public Image getImage() {
//		return image;
//	}
//
//	public TSAClient getTsaClient() {
//		return tsaClient;
//	}
//
//	/**
//	 * 
//	 * @param tsaUrl  TSA server url
//	 * @param tsaUser TSA username (optional)
//	 * @param tsaPass TSA password (optional)
//	 */
//	public void setTsaClient(String tsaUrl, String tsaUser, String tsaPass) {
//		this.tsaClient = new TSAClientBouncyCastle(tsaUrl, tsaUser, tsaPass);
//		this.tsaUrl = tsaUrl;
//		this.tsaUsername = tsaUser;
//		this.tsaPassword = tsaPass;
//	}
//
//	/**
//	 * 
//	 * @param frm1
//	 * @param p
//	 * @param rect
//	 * @return
//	 */
//	private PdfTemplate normalizeTemplate(PdfTemplate frm1, int p, Rectangle rect) {
//		int rotation = this.reader.getPageRotation(p);
//		Rectangle rotated = new Rectangle(rect.getWidth(), rect.getHeight());
//		int n = rotation;
//		while (n > 0) {
//			rotated = rotated.rotate();
//			n -= 90;
//		}
//		PdfTemplate frm = PdfTemplate.createTemplate(this.stamper.getWriter(), 10, 10);
//		frm.setBoundingBox(rotated);
//		if (rotation == 90) {
//			frm.concatCTM(0, 1, -1, 0, rect.getHeight(), 0);
//		} else if (rotation == 180) {
//			frm.concatCTM(-1, 0, 0, -1, rect.getWidth(), rect.getHeight());
//		} else if (rotation == 270) {
//			frm.concatCTM(0, -1, 1, 0, 0, rect.getWidth());
//		}
//		frm.addTemplate(frm1, 0, 0);
//
//		PdfTemplate napp = PdfTemplate.createTemplate(this.stamper.getWriter(), 10, 10);
//		napp.setBoundingBox(rotated);
//		napp.addTemplate(frm, 0, 0);
//		return napp;
//	}
//
//}
