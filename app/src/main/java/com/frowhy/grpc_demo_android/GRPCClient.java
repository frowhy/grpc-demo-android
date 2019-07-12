package com.frowhy.grpc_demo_android;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class GRPCClient<T> extends AsyncTask<String, Void, T> {
    private ManagedChannel channel;
    private OnListener<T> listener;
    private Call call;
    private String host;

    GRPCClient(String host, String service) {
        final String serviceDiscoveryUrl = "http://" + host + ":8500/v1/catalog/service/" + service;
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(serviceDiscoveryUrl)
                .get()
                .build();

        this.call = client.newCall(request);
        this.host = host;
    }

    @Override
    protected @Nullable
    T doInBackground(String... params) {
        if (listener != null) {
            try {
                Response response = call.execute();

                if (response.isSuccessful()) {
                    final JSONArray jsonArray = new JSONArray(response.body().string());
                    final int length = jsonArray.length();

                    int index = 0;
                    if (length > 1) {
                        index = (int) (Math.random() * length);
                    }

                    final int servicePort = ((JSONObject) jsonArray.get(index)).getInt("ServicePort");
                    channel = ManagedChannelBuilder.forAddress(host, servicePort).usePlaintext().build();
                } else {
                    listener.onFail(io.grpc.Status.ABORTED.asRuntimeException());
                }

                return listener.onRequest(channel, params);
            } catch (StatusRuntimeException e) {
                listener.onFail(e);
                return null;
            } catch (JSONException e) {
                listener.onFail(io.grpc.Status.ABORTED.asRuntimeException());
                return null;
            } catch (IOException e) {
                listener.onFail(io.grpc.Status.ABORTED.asRuntimeException());
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(T t) {
        if (listener != null) {
            try {
                channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
                if (t != null) {
                    listener.onSuccess(t);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                listener.onFail(io.grpc.Status.ABORTED.asRuntimeException());
            }
        }
    }

    void setOnListener(OnListener<T> listener) {
        this.listener = listener;
    }

    public interface OnListener<T> {
        T onRequest(ManagedChannel channel, String... params);

        void onSuccess(T t);

        void onFail(StatusRuntimeException e);
    }
}
