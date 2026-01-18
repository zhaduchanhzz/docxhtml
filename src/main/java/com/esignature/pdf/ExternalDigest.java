package com.esignature.pdf;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;

public interface ExternalDigest {
    public MessageDigest getMessageDigest(String hashAlgorithm) throws GeneralSecurityException;

}
