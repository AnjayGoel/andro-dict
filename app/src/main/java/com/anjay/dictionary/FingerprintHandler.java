package com.anjay.dictionary;

/**
 * Created by Anjay on 19-11-2016.
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.M)
class FingerprintHandler extends
    FingerprintManager.AuthenticationCallback {
    private FingerprintManager manager;
    private CancellationSignal cancellationSignal;
    private Callback c;

    public FingerprintHandler(Callback c) {
        this.c = c;
        Toast.makeText((Context)c,"fp created",Toast.LENGTH_SHORT).show();
    }

    void startAuth(FingerprintManager manager) {
        this.manager = manager;
        Toast.makeText((Context)c,"fp startauth",Toast.LENGTH_SHORT).show();
        cancellationSignal  = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission((Context)c, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return;
        }
        manager.authenticate(null, null, 0, this, null);
    }
    public void cancel (){
        if (!cancellationSignal.isCanceled()){

        cancellationSignal.cancel();
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId,CharSequence errString) {

        Toast.makeText((Context)c,"fp err",Toast.LENGTH_SHORT).show();
       c.callback(null);
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId,CharSequence helpString) {
    }

    @Override
    public void onAuthenticationFailed() {

        Toast.makeText((Context)c,"fp failed",Toast.LENGTH_SHORT).show();
        c.callback(null);

    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {

        Toast.makeText((Context)c,"fp success",Toast.LENGTH_SHORT).show();
        c.callback(null);
    }


}