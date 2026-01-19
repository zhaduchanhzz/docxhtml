//package com.esignature.utils;
//
//import org.bouncycastle.asn1.*;
//import org.bouncycastle.asn1.x509.*;
//import org.bouncycastle.asn1.x509.Extension;
//import org.bouncycastle.util.encoders.Hex;
//import org.bouncycastle.x509.extension.X509ExtensionUtil;
//
//import javax.naming.InvalidNameException;
//import javax.naming.ldap.LdapName;
//import javax.naming.ldap.Rdn;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.*;
//import java.security.cert.*;
//import java.security.cert.Certificate;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//public class X509CertificateInfo {
//	// System variable for proxy configuration
//	private static final String QUERY_THROUGH_PROXY_SERVER = "QUERY_THROUGH_PROXY_SERVER";
//	private static final String PROXY_IP = "PROXY_IP";
//	private static final String PROXY_PORT = "PROXY_PORT";
//	private static final String PROXY_USER = "PROXY_USER";
//	private static final String PROXY_PASSWORD = "PROXY_PASSWORD";
//	// ------------------------------
//
//	private X509Certificate x509Certificate = null;
//	private X509Certificate issuer;
//	private String urlOcsp = null;
//	private String urlCaCert = null;
//	private String urlCRL = null;
//	private X509CRL crl = null;
//
//	private static Map<String, X509Certificate> issuers = new HashMap<String, X509Certificate>();
//	public static boolean addIssuerCert(byte[] certBytes){
//		try {
//			ByteArrayInputStream inStream = new ByteArrayInputStream(certBytes);
//			CertificateFactory fac = CertificateFactory.getInstance("X.509");
//			Certificate cert = fac.generateCertificate(inStream);
//			if(cert instanceof X509Certificate){
//				String keyID = getSubjectKeyIdentifier((X509Certificate) cert);
//				if(keyID == null){
//					keyID = getCommonName((X509Certificate)cert, false);
//				}
//				issuers.put(keyID, (X509Certificate)cert);
//			}
//			return true;
//		} catch (CertificateException e) {
//			e.printStackTrace();
//		}
//
//		return false;
//	}
//
//	public X509CertificateInfo(X509Certificate certificate) {
//		this.x509Certificate = certificate;
//		byte[] value1 = certificate.getExtensionValue(X509Extensions.AuthorityInfoAccess.getId());
//		byte[] value2 = certificate.getExtensionValue(X509Extensions.CRLDistributionPoints.getId());
//		AuthorityInformationAccess authorityInformationAccess;
//		CRLDistPoint cRLDistPoint;
//		if (value1 != null) {
//			if (value1.length > 0) {
//				try {
//					byte[] extensionValue = certificate.getExtensionValue(X509Extensions.AuthorityInfoAccess.getId());
//					ASN1Sequence asn1Seq = (ASN1Sequence) X509ExtensionUtil.fromExtensionValue(extensionValue); // AuthorityInfoAccessSyntax
//					Enumeration<?> objects = asn1Seq.getObjects();
//
//					while (objects.hasMoreElements()) {
//						ASN1Sequence obj = (ASN1Sequence) objects.nextElement(); // AccessDescription
//						DERObjectIdentifier oid = (DERObjectIdentifier) obj.getObjectAt(0); // accessMethod
//						DERTaggedObject location = (DERTaggedObject) obj.getObjectAt(1); // accessLocation
//
//						if (location.getTagNo() == GeneralName.uniformResourceIdentifier) {
//							DEROctetString uri = (DEROctetString) location.getObject();
//							String str = new String(uri.getOctets());
//							if (oid.equals(X509ObjectIdentifiers.id_ad_ocsp)) {
//								this.urlOcsp = str;
//							}
//							if (oid.equals(X509ObjectIdentifiers.id_ad_caIssuers)) {
//								this.urlCaCert = str;
//							}
//						}
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//					this.urlCaCert = null;
//					this.urlOcsp = null;
//				}
//			}
//		}
//		if (value2 != null) {
//			if (value2.length > 0) {
//				try {
//					this.urlCRL = getCRLURL(certificate);
//				} catch (CertificateParsingException ex) {
//					this.urlCRL = null;
//				}
//			}
//		}
//	}
//
//	private static ASN1Primitive getExtensionValue(X509Certificate certificate, String oid) throws IOException {
//		byte[] bytes = certificate.getExtensionValue(oid);
//		if (bytes == null) {
//			return null;
//		}
//		ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(bytes));
//		ASN1OctetString octs = (ASN1OctetString) aIn.readObject();
//		aIn = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()));
//		return aIn.readObject();
//	}
//
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
//	private static String getSubjectKeyIdentifier(X509Certificate certificate){
//		ASN1Primitive obj;
//		try {
//			obj = getExtensionValue(certificate, Extension.subjectKeyIdentifier.getId());
//			if (obj == null) {
//				return null;
//			}
//			SubjectKeyIdentifier subjectKeyIdentifier = SubjectKeyIdentifier.getInstance(obj);
//			byte[] keyIdentifier = subjectKeyIdentifier.getKeyIdentifier();
//			return new String(Hex.encode(keyIdentifier));
//		} catch (IOException e) {
//			return null;
//		}
//
//	}
//
//	public static boolean isRoot(X509Certificate cert){
//		String subKeyID = getSubjectKeyIdentifier(cert);
//		String issKeyID = getIssuerKeyIdentifier(cert);
//		return subKeyID != null && subKeyID.equals(issKeyID);
//	}
//
//	private static String getIssuerKeyIdentifier(X509Certificate certificate){
//		ASN1Primitive obj;
//		try {
//			obj = getExtensionValue(certificate, Extension.authorityKeyIdentifier.getId());
//			if (obj == null) {
//				return null;
//			}
//			AuthorityKeyIdentifier subjectKeyIdentifier = AuthorityKeyIdentifier.getInstance(obj);
//			byte[] keyIdentifier = subjectKeyIdentifier.getKeyIdentifier();
//			return new String(Hex.encode(keyIdentifier));
//		} catch (IOException e) {
//			return null;
//		}
//
//	}
//
//	public static String getCRLURL(X509Certificate certificate) throws CertificateParsingException {
//		ASN1Primitive obj;
//		try {
//			obj = getExtensionValue(certificate, Extension.cRLDistributionPoints.getId());
//		} catch (IOException e) {
//			obj = null;
//		}
//		if (obj == null) {
//			return null;
//		}
//		CRLDistPoint dist = CRLDistPoint.getInstance(obj);
//		DistributionPoint[] dists = dist.getDistributionPoints();
//		for (DistributionPoint p : dists) {
//			DistributionPointName distributionPointName = p.getDistributionPoint();
//			if (DistributionPointName.FULL_NAME != distributionPointName.getType()) {
//				continue;
//			}
//			GeneralNames generalNames = (GeneralNames) distributionPointName.getName();
//			GeneralName[] names = generalNames.getNames();
//			for (GeneralName name : names) {
//				if (name.getTagNo() != GeneralName.uniformResourceIdentifier) {
//					continue;
//				}
//				DERIA5String derStr = DERIA5String.getInstance((ASN1TaggedObject) name.toASN1Primitive(), false);
//				return derStr.getString();
//			}
//		}
//		return null;
//	}
//
//	public String getIssuerName() {
//		return x509Certificate.getIssuerDN().getName();
//	}
//
//	/**
//	 *
//	 * @return
//	 * @throws Exception
//	 */
//	public X509Certificate getIssuer() throws Exception {
//		X509Certificate cert = null;
//		String keyID = getIssuerKeyIdentifier(this.x509Certificate);
//		if(keyID == null){
//			keyID = getCommonName(this.x509Certificate, false);
//		}
//		cert = issuers.get(keyID);
//		if(cert != null){
//			System.out.println("Found cached issuer certificate: " + keyID + ", " + getCommonName(this.x509Certificate, false));
//			return cert;
//		}
//		System.out.println("No cached certificate, download Issuer certificate at '" + this.urlCaCert + "'");
//		if (this.urlCaCert == null) {
//			return null;
//		}
//
//		try {
//			String rootSha1 = "https://rootca.gov.vn/crt/micnrca.crt";
//			if("http://public.rootca.gov.vn/crt/micnrca.crt".equals(this.urlCaCert)) {
//				this.urlCaCert = "https://rootca.gov.vn/crt/micnrca.crt";
//			}
//			InputStream inStream = queryUrlConnection(this.urlCaCert);
//			CertificateFactory factory = CertificateFactory.getInstance("X509");
//			byte[] buffer = new byte[inStream.available()];
//			inStream.read(buffer);
//
//			cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(buffer));
//		} catch (CertificateException ex) {
//			//ex.printStackTrace();
//		}
//		catch (NullPointerException ex){
//			//ex.printStackTrace();
//		}
//		catch (Exception ex){
//			//ex.printStackTrace();
//		}
//
//		System.out.println("Download Issuer certificate failed.");
//		return cert;
//	}
//
//	public String getUrlCaCert() {
//		return urlCaCert;
//	}
//
//	public void setUrlCaCert(String urlCaCert) {
//		this.urlCaCert = urlCaCert;
//	}
//
//	public String getUrlOcsp() {
//		return urlOcsp;
//	}
//
//	public void setUrlOcsp(String urlOcsp) {
//		this.urlOcsp = urlOcsp;
//	}
//
//	public X509Certificate getX509Certificate() {
//		return x509Certificate;
//	}
//
//	public void setX509Certificate(X509Certificate x509Certificate) {
//		this.x509Certificate = x509Certificate;
//	}
//
//	// code to test crl via set
//	public String getUrlCrl() {
//		return this.urlCRL;
//	}
//	// code to test crl via set
//
//	public void setUrlCrl(String urlCrl) {
//		this.urlCRL = urlCrl;
//	}
//
//	public X509CRL getCrl() throws Exception {
//		if (urlCRL == null) {
//			// khong tim thay duong dan toi crl server tren certificate;
//			throw new Exception("Khong tim thay duong dan toi CRL Store tren Server");
//		}
//		try {
//			System.setProperty("com.sun.security.enableCRLDP", "true");
//			CertificateFactory cf = CertificateFactory.getInstance("X509");
//
//			InputStream in = queryUrlConnection(this.urlCRL);
//			if (in == null) {
//				this.crl = null;
//			}
//
//			this.crl = (X509CRL) cf.generateCRL(in);
//			in.close();
//		} catch (CRLException ex) {
//			Logger.getLogger(X509CertificateInfo.class.getName()).log(Level.SEVERE, null, ex);
//		} catch (IOException ex) {
//			throw new Exception("Ket noi mang bi loi !!!");
//		} catch (CertificateException ex) {
//			Logger.getLogger(X509CertificateInfo.class.getName()).log(Level.SEVERE, null, ex);
//		}
//		return this.crl;
//	}
//
//	public static InputStream queryUrlConnection(String urlValue) {
//		// @TuanBS (26/09/2017) proxy configuration
//		boolean useProxy = false;
//		String proxyIp = null, proxyUser = null, proxyPass = null;
//		int proxyPort = -1;
//		try {
//			String confUseProxy = System.getenv(QUERY_THROUGH_PROXY_SERVER);
//			if ("TRUE".equalsIgnoreCase(confUseProxy)) {
//				useProxy = true;
//				proxyIp = System.getenv(PROXY_IP);
//				proxyUser = System.getenv(PROXY_USER);
//				proxyPass = System.getenv(PROXY_PASSWORD);
//
//				proxyPort = Integer.parseInt(System.getenv(PROXY_PORT));
//			}
//		} catch (Exception ex) {
//		}
//		// --------------------------------------
//
//		// Create a HTTP connection to the URL
//		URL url = null;
//		try {
//			url = new URL(urlValue);
//		} catch (MalformedURLException ex) {
//			return null;
//		}
//
//		// @TuanBS: proxy configuration
//		// 26/09/2017
//		HttpURLConnection con = null;
//		if (useProxy) {
//			final String userName = proxyUser;
//			final String password = proxyPass;
//			if (userName != null && !"".equals(userName) && password != null && !"".equals(password)) {
//				Authenticator authenticator = new Authenticator() {
//					@Override
//					public PasswordAuthentication getPasswordAuthentication() {
//						return (new PasswordAuthentication(userName, password.toCharArray()));
//					}
//				};
//				Authenticator.setDefault(authenticator);
//			}
//			try {
//				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp, proxyPort));
//
//				con = (HttpURLConnection) url.openConnection(proxy);
//			} catch (Exception ex) {
//			}
//		} else {
//			try {
//				con = (HttpURLConnection) url.openConnection();
//			} catch (Exception ex) {
//			}
//		}
//		// -----------------------------------------------------
//
//		if (con == null) {
//			// Logged before
//			return null;
//		}
//
//		try {
//			con.setConnectTimeout(6000);
//			if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
//				return null;
//			}
//		} catch (IOException ex) {
//			Logger.getLogger(X509CertificateInfo.class.getName()).log(Level.SEVERE,
//					ex.getMessage() + " at X509CertificateInfo[329]");
//			return null;
//		}
//
//		try {
//			InputStream in = con.getInputStream();
//			return in;
//		} catch (Exception ex) {
//
//		}
//		return null;
//	}
//
//	public void setCrl(X509CRL crl) {
//		this.crl = crl;
//	}
//}
