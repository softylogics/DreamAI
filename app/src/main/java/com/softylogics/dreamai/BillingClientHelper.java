package com.softylogics.dreamai;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class BillingClientHelper implements PurchasesUpdatedListener {

    private Context context;
    private BillingClient client;

    private List<ProductDetails> productList;

    private List<Purchase> purchaseList;


    private boolean restored;


    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("billing", "User cancelled purchase");

        } else {
            Log.d("billing", billingResult.getDebugMessage());
        }

    }

    private void handlePurchase(Purchase purchase) {

            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    client.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            for (String pur : purchase.getProducts()) {
                                if (pur.equalsIgnoreCase(Constants.SUBSCRIPTION_ID)) {
                                    Log.d("billing", "Purchase is successful " + pur);
                                    UserPreferences.setBoolean(Constants.IS_PURCHASED, true);
                                }
                            }
                        }
                    });
                }
            }

    }

    public BillingClientHelper(Context context) {
        this.context = context;
        this.client = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        this.productList = new ArrayList<>();


        this.purchaseList = new ArrayList<>();


    }

    public BillingClient getClient(){
        return this.client;
    }


    public void startConnection(){
        this.client.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    Log.d("billing", "Connection setup finished");
                    // The BillingClient is ready. You can query purchases here.
                    queryProductsDetails();
                    queryPurchases();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                Log.d("billing", "Retrying Connection");
                startConnection();
            }
        });
    }

    public void queryPurchases() {
        Log.d("billing" , "querying");
        this.client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                (billingResult, purchases) -> {
                    // check billingResult
                    // process returned purchase list, e.g. display the plans user owns
                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                        if(purchases.size()!=0){
                            Log.d("billing", purchases.toString());
                            UserPreferences.setBoolean(Constants.IS_PURCHASED, true);

                        }
                        else{
                            Log.d("billing", "No products purchased");
                            UserPreferences.setBoolean(Constants.IS_PURCHASED, false);
                        }
                    }
                    else{
                        Log.d("billing", billingResult.getDebugMessage());
                    }
                }
        );

    }

    public void queryProductsDetails() {
        if(this.client.isReady()){
            QueryProductDetailsParams queryProductDetailsParams =
                    QueryProductDetailsParams.newBuilder()
                            .setProductList(
                                    ImmutableList.of(
                                            QueryProductDetailsParams.Product.newBuilder()
                                                    .setProductId(Constants.SUBSCRIPTION_ID )
                                                    .setProductType(BillingClient.ProductType.SUBS)
                                                    .build()))
                            .build();


            this.client.queryProductDetailsAsync(
                    queryProductDetailsParams,
                    (billingResult, productDetailsList) -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if(productDetailsList.size() > 0){
                                Log.d("billing", productDetailsList.toString());
                                productList = productDetailsList;
                            }
                        }
                        else{
                            Log.e("billing", billingResult.getDebugMessage());
                        }
                    }
            );
        }
    }


    public void startPurchaseFlow(Activity activity){
        assert this.productList.get(0).getSubscriptionOfferDetails() != null;
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                .setProductDetails(this.productList.get(0))
                                // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                // for a list of offers that are available to the user
                                .setOfferToken(this.productList.get(0).getSubscriptionOfferDetails().get(0).getOfferToken())
                                .build()
                );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();
        Log.d("billing", "Launching billing flow");
        // Launch the billing flow
        this.client.launchBillingFlow(activity, billingFlowParams);
    }

    public boolean restoreSubscriptions(){
        restored = false;
        this.client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(), (billingResult1, list) -> {
                    if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        if (list.size() != 0) {
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i).getProducts().contains(Constants.SUBSCRIPTION_ID)) {
                                    UserPreferences.setBoolean(Constants.IS_PURCHASED, true);
                                    Log.d("billing", "Product id " + Constants.SUBSCRIPTION_ID + " will restore here");
                                    restored = true;
                                    break;
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                                    builder.setTitle("Subscription Restored")
//                                            .setMessage("Congratulations! Your subscription has been successfully restored. You can now enjoy all the benefits of our premium plan.")
//                                            .setPositiveButton("OK", null)
//                                            .show();

                                }
                            }

                        } else {
                            Log.d("billing", "No product to restore");
                            restored = false;
                            showToast("Thank you for your interest in our premium subscriptions! To enjoy all the benefits, please subscribe to our premium plan.");
//                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                            builder.setTitle("Restore Subscriptions")
//                                    .setMessage("Thank you for your interest in our premium subscriptions! To enjoy all the benefits, please subscribe to our premium plan.")
//                                    .setPositiveButton("Subscribe", (dialog, which) -> {
//                                        startPurchaseFlow(this.activity);
//                                    })
//                                    .setNegativeButton("Cancel", null)
//                                    .show();
                        }
                    }
                });
        return restored;
    }


    private void showToast(String message) {
        Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
    }

}
