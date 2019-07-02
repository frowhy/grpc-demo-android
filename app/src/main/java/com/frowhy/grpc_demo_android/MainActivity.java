package com.frowhy.grpc_demo_android;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import grpc_demo_go.protobuf.service.test_a.GreeterGrpc;
import grpc_demo_go.protobuf.service.test_a.TestAService;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

public class MainActivity extends AppCompatActivity {

    static final String kHost = "10.0.2.2";
    static final int kPort = 50051;

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = findViewById(R.id.text);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                GRPCClient<TestAService.EchoResponse> grpcClient = new GRPCClient<>(kHost, kPort);
                grpcClient.setOnListener(new GRPCClient.OnListener<TestAService.EchoResponse>() {
                    @Override
                    public TestAService.EchoResponse onRequest(ManagedChannel channel, String... params) {
                        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);
                        TestAService.EchoRequest request = TestAService.EchoRequest.newBuilder().setMessage("Android").build();
                        return stub.echo(request);
                    }

                    @Override
                    public void onSuccess(TestAService.EchoResponse response) {
                        textView.setText(response.getMeta().getMessage());
                    }

                    @Override
                    public void onFail(StatusRuntimeException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                grpcClient.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
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
