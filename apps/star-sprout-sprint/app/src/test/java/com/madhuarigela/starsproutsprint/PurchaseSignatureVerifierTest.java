package com.madhuarigela.starsproutsprint;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class PurchaseSignatureVerifierTest {
    @Test public void acceptsValidRsaSignedPurchaseJson() throws Exception {
        KeyPair pair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        String json = "{\"productId\":\"garden.diamonds\",\"purchaseState\":0}";
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initSign(pair.getPrivate());
        signer.update(json.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signer.sign());
        String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        assertTrue(PurchaseSignatureVerifier.verify(json, signature, publicKey));
    }

    @Test public void rejectsChangedPurchaseJsonAndUnconfiguredKey() throws Exception {
        KeyPair pair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        String json = "{\"productId\":\"garden.diamonds\"}";
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initSign(pair.getPrivate());
        signer.update(json.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signer.sign());
        String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        assertFalse(PurchaseSignatureVerifier.verify(json + " ", signature, publicKey));
        assertFalse(PurchaseSignatureVerifier.verify(json, signature, "REPLACE_WITH_PLAY_LICENSE_PUBLIC_KEY"));
    }
}

