package com.esignature.pdf;

import com.itextpdf.text.Rectangle;

public class PdfSignatureView {
	private int page;
	private Rectangle rectangle;
	private String id;

	public PdfSignatureView(int page, String rectangle) {
		this.page = page;
		setRectangle(rectangle);
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public Rectangle getRectangle() {
		return rectangle;
	}

	public void setRectangle(String rectangle) {
		if (rectangle != null) {
			int llx, lly, rrx, rry;
			try {
				String[] commentRect = rectangle.split(",");
				llx = Integer.parseInt(commentRect[0]);
				lly = Integer.parseInt(commentRect[1]);
				rrx = Integer.parseInt(commentRect[2]);
				rry = Integer.parseInt(commentRect[3]);
				this.rectangle = new Rectangle(llx, lly, rrx, rry);
			} catch (NumberFormatException ex) {
				System.out.println("Set widget warning: " + ex.getMessage());
				this.rectangle = new Rectangle(10, 10, 150, 85);
			}
		}
		else this.rectangle = new Rectangle(10, 10, 150, 85);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
