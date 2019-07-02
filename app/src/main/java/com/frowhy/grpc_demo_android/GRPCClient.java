package com.frowhy.grpc_demo_android;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class GRPCClient<T> extends AsyncTask<String, Void, T> {
    private ManagedChannel channel;
    private OnListener<T> listener;

    GRPCClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    @Override
    protected @Nullable
    T doInBackground(String... params) {
        if (listener != null) {
            try {
                return listener.onRequest(channel, params);
            } catch (StatusRuntimeException e) {
                listener.onFail(e);
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
