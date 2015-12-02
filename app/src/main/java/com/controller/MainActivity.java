package com.controller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Date;
import java.util.LinkedList;

import com.controller.utils.BluetoothThread;
import com.controller.utils.DataFormat;
import com.controller.utils.MyBluetoothAdapter;
import com.controller.utils.MyGLSurfaceView;
import com.controller.utils.RenderingThread;
import com.controller.utils.SerialStreamParser;

public class MainActivity extends AppCompatActivity {
    private static Handler handler = null;
    private BluetoothThread bluetoothThread = null;
    private RenderingThread renderingThread = null;

    private MyGLSurfaceView mGLView;

    private SerialStreamParser serialStreamParser = new SerialStreamParser();
    private MyBluetoothAdapter myBluetoothAdapter = new MyBluetoothAdapter();

    private Button connectButton;
    private Button disconnectButton;
    private Button resetButton;
    private Button tuneButton;

    private float pseudoTime = 0;

    private boolean working = false;

    public void connect() {
        if (myBluetoothAdapter.connect()) {
            renderingThread = new RenderingThread(mGLView);
            renderingThread.start();
            bluetoothThread = new BluetoothThread(myBluetoothAdapter.getBluetoothSocket(), handler);
            bluetoothThread.start();
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            tuneButton.setEnabled(true);
        }
    }

    public void disconnect() {
        try {
            bluetoothThread.interrupt();
            renderingThread.interrupt();
        } catch (Exception e) {}
        myBluetoothAdapter.disconnect();
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        tuneButton.setEnabled(false);
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(  savedInstanceState );
        mGLView = new MyGLSurfaceView( this );
        setContentView(R.layout.activity_main);

        //setContentView(mGLView);
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.openglLayout);
        linearLayout.addView(mGLView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));;

        connectButton = (Button)findViewById(R.id.connectButton);
        disconnectButton = (Button)findViewById(R.id.disconnectButton);
        resetButton = (Button)findViewById(R.id.resetButton);
        tuneButton = (Button)findViewById(R.id.tuneButton);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                working = true;
                connect();
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                working = false;
                disconnect();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGLView.mRenderer.roll.clear();
                mGLView.mRenderer.pitch.clear();
                mGLView.mRenderer.yaw.clear();
                mGLView.requestRender();
            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(android.os.Message message) {
                if (message.what == BluetoothThread.MESSAGE_RECEIVED) {
                    byte[] buffer = (byte[])message.obj;
                    serialStreamParser.appendData(new String(buffer, 0, message.arg1));
                    LinkedList<DataFormat> data = serialStreamParser.parseStream();

                    for (int i = 0; i < data.size(); i++) {
                        mGLView.mRenderer.roll.addPoint(pseudoTime, data.get(i).getRoll() / 300.0f);
                        mGLView.mRenderer.pitch.addPoint(pseudoTime, data.get(i).getPitch() / 300.0f);
                        mGLView.mRenderer.yaw.addPoint(pseudoTime, data.get(i).getYaw() / 300.0f);
                        pseudoTime += 0.001;
                    }

                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onResume( ) {
        super.onResume();

        if (working) {
            connect();
        }
    }

    @Override
    public void onPause( ){
        super.onPause();

        if (working) {
            disconnect();
        }
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
