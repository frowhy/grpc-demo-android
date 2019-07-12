package com.frowhy.grpc_demo_android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import go.micro.srv.demo.DemoGrpc;
import go.micro.srv.demo.DemoOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

public class MainActivity extends AppCompatActivity {

    static final String kHost = "10.0.2.2";
    static final String kService = "go.micro.srv.demo";

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = findViewById(R.id.text);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((view) -> {
            GRPCClient<DemoOuterClass.Response> grpcClient = new GRPCClient<>(kHost, kService);
            grpcClient.setOnListener(new GRPCClient.OnListener<DemoOuterClass.Response>() {
                @Override
                public DemoOuterClass.Response onRequest(ManagedChannel channel, String... params) {
                    DemoGrpc.DemoBlockingStub stub = DemoGrpc.newBlockingStub(channel);
                    DemoOuterClass.Request request = DemoOuterClass.Request.newBuilder().setName("Android").build();
                    return stub.call(request);
                }

                @Override
                public void onSuccess(DemoOuterClass.Response response) {
                    textView.setText(response.getMsg());
                }

                @Override
                public void onFail(StatusRuntimeException e) {
                    Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
            grpcClient.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
