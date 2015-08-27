package mgeier63.openpgpapilibdemo;

import android.content.Context;
import android.content.Intent;

import org.openintents.openpgp.IOpenPgpService;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.io.InputStream;
import java.io.OutputStream;

public class Api implements OpenPgpServiceConnection.OnBound, MainActivity.IApi {

    private final Context mCtx;

    OpenPgpApi mApi;

    public Api(Context ctx) {
        mCtx = ctx;
        OpenPgpServiceConnection sc = new OpenPgpServiceConnection(ctx, "org.sufficientlysecure.keychain", this);
        sc.bindToService();

    }

    public Intent execute(Intent data, InputStream is, OutputStream os) {
        return mApi.executeApi(data, is, os);
    }


    @Override
    public Intent executeApi(Intent data, InputStream is, OutputStream os) {
        return mApi.executeApi(data, is, os);
    }

    @Override
    public boolean isServiceBound() {
        return mApi != null;
    }

    @Override
    public void onBound(IOpenPgpService service) {
        mApi = new OpenPgpApi(mCtx, service);
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
        OpenPgpServiceConnection sc = new OpenPgpServiceConnection(mCtx, "org.sufficientlysecure.keychain.debug", this);
        sc.bindToService();
    }
}
