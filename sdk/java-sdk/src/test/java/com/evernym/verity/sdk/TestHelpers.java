package com.evernym.verity.sdk;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.evernym.verity.sdk.exceptions.WalletException;
import com.evernym.verity.sdk.exceptions.WalletOpenException;
import com.evernym.verity.sdk.utils.Context;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONObject;

public class TestHelpers {
    
    public static Context getContext() throws WalletException {
        String walletName = UUID.randomUUID().toString();
        String walletKey = UUID.randomUUID().toString();
        String endpointUrl = "http://localhost:3000";
        String verityUrl = "http://localhost:3000";
        TestWallet testWallet = new TestWallet(walletName, walletKey);
        JSONObject config = new JSONObject();
        config.put("walletName", walletName);
        config.put("walletKey", walletKey);
        config.put("verityUrl", verityUrl);
        config.put("verityPublicDID", testWallet.getVerityPublicVerkey());
        config.put("verityPublicVerkey", testWallet.getVerityPublicVerkey());
        config.put("verityPairwiseVerkey", testWallet.getVerityPairwiseVerkey());
        config.put("verityPairwiseDID", testWallet.getVerityPairwiseDID());
        config.put("sdkPairwiseDID", testWallet.getSdkPairwiseDID());
        config.put("sdkPairwiseVerkey", testWallet.getSdkPairwiseVerkey());
        config.put("endpointUrl", endpointUrl);
        return new Context(config.toString());
    }

    public static void cleanup(Context context) throws Exception {
        if(context != null) {
            if(! context.walletIsClosed()) {
                context.closeWallet();
            }
            Wallet.deleteWallet(context.walletConfig(), context.walletCredentials()).get();
        }
    }
}