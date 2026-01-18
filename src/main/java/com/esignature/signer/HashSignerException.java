/**
 * 
 */
package com.esignature.signer;

/**
 * @author TuanBS (tuanbs@vnpt.vn)
 *
 */
public class HashSignerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public HashSignerException() {
	}

	/**
	 * @param arg0
	 */
	public HashSignerException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public HashSignerException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public HashSignerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public HashSignerException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
