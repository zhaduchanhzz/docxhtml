package com.esignature.pdf;

import com.esignature.signer.HashSignerException;
import com.esignature.signer.SignatureParameter;

public class PdfParameter extends SignatureParameter {
	private String reason;
	private String location;
	private String signerContact;
	private String signPosition;
	private int signPage;
	private byte[] customImage;
//	private Rectangle signRectangle = new Rectangle(25, 25, 225, 75);
	private int fontSize = 8;
	private String tsaUrl;
	private String tsaUser;
	private String tsaPass;

	public enum RenderMode {
		TEXT_ONLY, BOTH_IMAGE_AND_TEXT, IMAGE_ONLY
	}

	private RenderMode renderMode;

	public PdfParameter() {
	}

	public PdfParameter(byte[] unsignData, String certBase64, int page,
			RenderMode renderMode) {
		super(unsignData, certBase64);
		this.signPage = page;
		this.renderMode = renderMode;
	}

	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the signPosition
	 */
	public String getSignPosition() {
		return signPosition;
	}

	/**
	 * @param signPosition the signPosition to set in format (llx,lly,urx,ury)
	 * @throws HashSignerException
	 */
	public void setSignPosition(String signPosition)
			throws HashSignerException {
		if (signPosition == null || "".equals(signPosition)) {
			throw new HashSignerException(
					"Signature posistion must not be null");
		}
		String[] split = signPosition.split(",");
		if (split.length != 4) {
			throw new HashSignerException("Signature posistion invalid");
		}

		this.signPosition = signPosition;
	}

	/**
	 * @return the signPage
	 */
	public int getSignPage() {
		return signPage;
	}

	/**
	 * @param signPage the signPage to set
	 */
	public void setSignPage(int signPage) {
		this.signPage = signPage;
	}

	/**
	 * @return the customImage
	 */
	public byte[] getCustomImage() {
		return customImage;
	}

	/**
	 * @param customImage the customImage to set
	 */
	public void setCustomImage(byte[] customImage) {
		this.customImage = customImage;
	}

	/**
	 * @return the renderMode
	 */
	public RenderMode getRenderMode() {
		return renderMode;
	}

	/**
	 * @param renderMode the renderMode to set
	 */
	public void setRenderMode(RenderMode renderMode) {
		this.renderMode = renderMode;
	}

	/**
	 * @return the signerContact
	 */
	public String getSignerContact() {
		return signerContact;
	}

	/**
	 * @param signerContact the signerContact to set
	 */
	public void setSignerContact(String signerContact) {
		this.signerContact = signerContact;
	}

	/**
	 * @return the fontSize
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**
	 * @param fontSize the fontSize to set
	 */
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public String getTsaUrl() {
		return tsaUrl;
	}

	public void setTsaUrl(String tsaUrl) {
		this.tsaUrl = tsaUrl;
	}

	public String getTsaUser() {
		return tsaUser;
	}

	public void setTsaUser(String tsaUser) {
		this.tsaUser = tsaUser;
	}

	public String getTsaPass() {
		return tsaPass;
	}

	public void setTsaPass(String tsaPass) {
		this.tsaPass = tsaPass;
	}

}
