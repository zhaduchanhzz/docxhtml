package com.esignature.signer;

import com.esignature.utils.MessageDigestAlgorithm;

public class BaseHashSigner {
	protected SignatureParameter param;
    protected String HASH_ALGORITHM = "SHA1";
    protected static final String ENCRYPT_ALGORITHM = "RSA";
    protected MessageDigestAlgorithm _hashAlgorithm = MessageDigestAlgorithm.SHA256;
}
