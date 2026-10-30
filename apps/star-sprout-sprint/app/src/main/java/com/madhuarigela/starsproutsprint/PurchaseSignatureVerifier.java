package com.madhuarigela.starsproutsprint;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/** Client-side signature check for Play purchase JSON. A secure backend is stronger. */
final class PurchaseSignatureVerifier {
    private PurchaseSignatureVerifier() { }

    static boolean verify(String purchaseJson, String signatureBase64, String publicKeyBase64) {
        if (purchaseJson == null || signatureBase64 == null || publicKeyBase64 == null
                || purchaseJson.isEmpty() || signatureBase64.isEmpty() || publicKeyBase64.isEmpty()
                || publicKeyBase64.startsWith("REPLACE_")) return false;
        try {
            String normalized = publicKeyBase64.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "").replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(normalized);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            Signature verifier = Signature.getInstance("SHA1withRSA");
            verifier.initVerify(KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes)));
            verifier.update(purchaseJson.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(signatureBytes);
        } catch (Exception ignored) {
            return false;
        }
    }
}

