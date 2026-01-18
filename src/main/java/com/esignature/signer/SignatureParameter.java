package com.esignature.signer;

/**
 * 
 * @author TuanBS (tuanbs@vnpt.vn)
 *
 */
public class SignatureParameter {
	private byte[] unsignData;
	private String certBase64;
	
	/**
	 * Default constructor
	 */
	public SignatureParameter() {}
	
	/**
	 * 
	 * @param unsignData
	 * @param certBase64
	 */
	public SignatureParameter(byte[] unsignData, String certBase64) {
		this.unsignData = unsignData;
		this.certBase64 = certBase64;
	}
	
	/**
	 * @return the unsignData
	 */
	public byte[] getUnsignData() {
		return unsignData;
	}
	/**
	 * @param unsignData the unsignData to set
	 */
	public void setUnsignData(byte[] unsignData) {
		this.unsignData = unsignData;
	}
	/**
	 * @return the certBase64
	 */
	public String getCertBase64() {
		return certBase64;
	}
	/**
	 * @param certBase64 the certBase64 to set
	 */
	public void setCertBase64(String certBase64) {
		this.certBase64 = certBase64;
	}
}
