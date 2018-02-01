package com.green.wcms.app.retrofit;

import android.app.ProgressDialog;
import android.widget.Toast;

import com.green.wcms.app.adaptor.CheckAdapter;
import com.green.wcms.app.menu.MainActivity;
import com.green.wcms.app.util.UtilClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by GS on 2017-08-07.
 */
public class NetworkInterceptor implements Interceptor {
    private static final String TAG = "NetworkInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();

        builder.addHeader("comp_database", MainActivity.comp_database);

        Request request = chain.request();
//        UtilClass.logD(TAG, "method="+request.method());
//        UtilClass.logD(TAG, "headers="+request.headers());
//        UtilClass.logD(TAG, "connection="+chain.connection());

        Response response = chain.proceed(request);
//        UtilClass.logD(TAG, "////////////////////////////////");
//        UtilClass.logD(TAG, "url="+response.request().url());
//        UtilClass.logD(TAG, "headers="+response.headers());

        return response;
    }

}
