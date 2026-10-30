package com.madhuarigela.starsproutsprint;

import android.app.Activity;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Play Billing client. Purchases are locally signature-checked and deduplicated. */
final class BillingManager {
    private static final int DIAMONDS_PER_PACK = 100;
    private final Activity activity;
    private final GameData data;
    private final BillingClient client;
    private final Map<String, ProductDetails> products = new HashMap<>();
    private boolean connected;

    BillingManager(Activity activity, GameData data) {
        this.activity = activity;
        this.data = data;
        client = BillingClient.newBuilder(activity)
                .setListener((result, purchases) -> {
                    if (result.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                        processPurchases(purchases);
                    }
                })
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build();
    }

    void connectAndRestore() {
        if (connected || !isConfigured()) return;
        client.startConnection(new com.android.billingclient.api.BillingClientStateListener() {
            @Override public void onBillingSetupFinished(com.android.billingclient.api.BillingResult result) {
                if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
                connected = true;
                queryProducts();
                client.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP).build(),
                        (billingResult, purchases) -> {
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                processPurchases(purchases);
                            }
                        });
            }
            @Override public void onBillingServiceDisconnected() { connected = false; }
        });
    }

    private boolean isConfigured() {
        return !BuildConfig.PLAY_LICENSE_PUBLIC_KEY.isEmpty()
                && !BuildConfig.PLAY_LICENSE_PUBLIC_KEY.startsWith("REPLACE_")
                && !BuildConfig.REMOVE_ADS_PRODUCT_ID.startsWith("REPLACE_")
                && !BuildConfig.DIAMOND_PACK_PRODUCT_ID.startsWith("REPLACE_");
    }

    private void queryProducts() {
        List<QueryProductDetailsParams.Product> requested = new ArrayList<>();
        for (String id : Arrays.asList(BuildConfig.REMOVE_ADS_PRODUCT_ID, BuildConfig.DIAMOND_PACK_PRODUCT_ID)) {
            requested.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id).setProductType(BillingClient.ProductType.INAPP).build());
        }
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder().setProductList(requested).build();
        client.queryProductDetailsAsync(params, (result, response) -> {
            if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                for (ProductDetails product : response.getProductDetailsList()) {
                    products.put(product.getProductId(), product);
                }
                ProductDetails removeAds = products.get(BuildConfig.REMOVE_ADS_PRODUCT_ID);
                ProductDetails diamonds = products.get(BuildConfig.DIAMOND_PACK_PRODUCT_ID);
                ((MainActivity) activity).onStorePrices(formattedPrice(removeAds), formattedPrice(diamonds));
            }
        });
    }

    private static String formattedPrice(ProductDetails product) {
        if (product == null || product.getOneTimePurchaseOfferDetailsList() == null
                || product.getOneTimePurchaseOfferDetailsList().isEmpty()) return "Play setup required";
        return product.getOneTimePurchaseOfferDetailsList().get(0).getFormattedPrice();
    }

    void purchaseRemoveAds() { purchase(BuildConfig.REMOVE_ADS_PRODUCT_ID); }
    void purchaseDiamondPack() { purchase(BuildConfig.DIAMOND_PACK_PRODUCT_ID); }

    void restorePurchases() {
        if (!isConfigured()) {
            Toast.makeText(activity, "Add your Play product IDs and licensing public key in local.properties first.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!connected) {
            connectAndRestore();
            Toast.makeText(activity, "Connecting to Play purchases. Try again shortly.", Toast.LENGTH_SHORT).show();
            return;
        }
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP).build(),
                (result, purchases) -> {
                    if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) processPurchases(purchases);
                });
    }

    private void purchase(String productId) {
        if (!isConfigured()) {
            Toast.makeText(activity, "Add your Play product IDs and licensing public key in local.properties first.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!connected) {
            connectAndRestore();
            Toast.makeText(activity, "Connecting to Play purchases. Try again shortly.", Toast.LENGTH_SHORT).show();
            return;
        }
        ProductDetails details = products.get(productId);
        if (details == null) {
            queryProducts();
            Toast.makeText(activity, "Store items are not available yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<ProductDetails.OneTimePurchaseOfferDetails> offers = details.getOneTimePurchaseOfferDetailsList();
        BillingFlowParams.ProductDetailsParams.Builder item = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details);
        if (offers != null && !offers.isEmpty() && offers.get(0).getOfferToken() != null) {
            item.setOfferToken(offers.get(0).getOfferToken());
        }
        BillingFlowParams flow = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Arrays.asList(item.build())).build();
        client.launchBillingFlow(activity, flow);
    }

    private void processPurchases(List<Purchase> purchases) {
        for (Purchase purchase : purchases) {
            if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) continue;
            if (!BuildConfig.APPLICATION_ID.equals(purchase.getPackageName())) continue;
            if (!PurchaseSignatureVerifier.verify(purchase.getOriginalJson(), purchase.getSignature(),
                    BuildConfig.PLAY_LICENSE_PUBLIC_KEY)) continue;
            List<String> ids = purchase.getProducts();
            if (ids.contains(BuildConfig.REMOVE_ADS_PRODUCT_ID)) {
                data.setAdsRemoved();
                acknowledge(purchase);
                Toast.makeText(activity, "Ads have been removed from this game.", Toast.LENGTH_SHORT).show();
            } else if (ids.contains(BuildConfig.DIAMOND_PACK_PRODUCT_ID)) {
                boolean alreadyProcessed = data.hasProcessedPurchase(purchase.getPurchaseToken());
                if (!alreadyProcessed && !data.grantDiamondsForPurchase(purchase.getPurchaseToken(), DIAMONDS_PER_PACK)) {
                    continue;
                }
                if (!alreadyProcessed) Toast.makeText(activity, "100 diamonds added.", Toast.LENGTH_SHORT).show();
                client.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(),
                        (result, token) -> { });
            }
        }
    }

    private void acknowledge(Purchase purchase) {
        if (purchase.isAcknowledged()) return;
        client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken()).build(), result -> { });
    }

    void close() {
        if (client.isReady()) client.endConnection();
        connected = false;
    }
}

