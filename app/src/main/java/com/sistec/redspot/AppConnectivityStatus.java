package com.sistec.redspot;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class AppConnectivityStatus {

    private static ProgressDialog pd;

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    public static void showProgress(Activity activity, String title, String msg) {
        pd = new ProgressDialog(activity);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setTitle(title);
        pd.setMessage(msg);
        pd.setIndeterminate(true);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    public static void hideProgress() {
        pd.dismiss();
    }
}
