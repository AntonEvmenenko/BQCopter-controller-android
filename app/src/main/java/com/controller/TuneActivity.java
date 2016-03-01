package com.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.controller.utils.BluetoothThread;
import com.controller.utils.DataFormat;

import java.util.LinkedList;

public class TuneActivity extends AppCompatActivity {
    private static Context appContext;

    private static Handler handler = null;

    private Button writeButton;
    private Button readButton;

    private EditText rollP;
    private EditText rollD;
    private EditText pitchP;
    private EditText pitchD;
    private EditText yawP;
    private EditText yawD;

    private Float currentRollP = 0.0f;
    private Float currentRollD = 0.0f;
    private Float currentPitchP = 0.0f;
    private Float currentPitchD = 0.0f;
    private Float currentYawP = 0.0f;
    private Float currentYawD = 0.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tune);

        appContext = this;

        writeButton = (Button)findViewById(R.id.writeButton);
        readButton = (Button)findViewById(R.id.readButton);

        rollP = (EditText)findViewById(R.id.rollP);
        rollD = (EditText)findViewById(R.id.rollD);
        pitchP = (EditText)findViewById(R.id.pitchP);
        pitchD = (EditText)findViewById(R.id.pitchD);
        yawP = (EditText)findViewById(R.id.yawP);
        yawD = (EditText)findViewById(R.id.yawD);

        rollP.clearFocus();

        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Float currentValue = currentRollP;
                String newValueString = rollP.getText().toString();
                boolean newValueExist = !newValueString.equals("");
                Float newValue = newValueExist ? Float.parseFloat(newValueString) : 0.0f;
                if (newValueExist && currentValue.compareTo(newValue) != 0) {
                    String message = DataFormat.format(DataFormat.SET_ROLL_P, newValue);
                    ((Globals)appContext.getApplicationContext()).getBluetoothAdapter().write(message);
                }

                currentValue = currentRollD;
                newValueString = rollD.getText().toString();
                newValueExist = !newValueString.equals("");
                newValue = newValueExist ? Float.parseFloat(newValueString) : 0.0f;
                if (newValueExist && currentValue.compareTo(newValue) != 0) {
                    String message = DataFormat.format(DataFormat.SET_ROLL_D, newValue);
                    ((Globals)appContext.getApplicationContext()).getBluetoothAdapter().write(message);
                }

                currentValue = currentPitchP;
                newValueString = pitchP.getText().toString();
                newValueExist = !newValueString.equals("");
                newValue = newValueExist ? Float.parseFloat(newValueString) : 0.0f;
                if (newValueExist && currentValue.compareTo(newValue) != 0) {
                    String message = DataFormat.format(DataFormat.SET_PITCH_P, newValue);
                    ((Globals)appContext.getApplicationContext()).getBluetoothAdapter().write(message);
                }

                currentValue = currentPitchD;
                newValueString = pitchD.getText().toString();
                newValueExist = !newValueString.equals("");
                newValue = newValueExist ? Float.parseFloat(newValueString) : 0.0f;
                if (newValueExist && currentValue.compareTo(newValue) != 0) {
                    String message = DataFormat.format(DataFormat.SET_PITCH_D, newValue);
                    ((Globals)appContext.getApplicationContext()).getBluetoothAdapter().write(message);
                }

                currentValue = currentYawP;
                newValueString = yawP.getText().toString();
                newValueExist = !newValueString.equals("");
                newValue = newValueExist ? Float.parseFloat(newValueString) : 0.0f;
                if (newValueExist && currentValue.compareTo(newValue) != 0) {
                    String message = DataFormat.format(DataFormat.SET_YAW_P, newValue);
                    ((Globals)appContext.getApplicationContext()).getBluetoothAdapter().write(message);
                }

                currentValue = currentYawD;
                newValueString = yawD.getText().toString();
                newValueExist = !newValueString.equals("");
                newValue = newValueExist ? Float.parseFloat(newValueString) : 0.0f;
                if (newValueExist && currentValue.compareTo(newValue) != 0) {
                    String message = DataFormat.format(DataFormat.SET_YAW_D, newValue);
                    ((Globals)appContext.getApplicationContext()).getBluetoothAdapter().write(message);
                }
            }
        });

        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = DataFormat.format(DataFormat.GET_ALL);
                ((Globals)appContext.getApplicationContext()).getBluetoothAdapter().write(message);
            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(android.os.Message message) {
                if (message.what == BluetoothThread.MESSAGE_RECEIVED) {
                    byte[] buffer = (byte[])message.obj;
                    ((Globals)appContext.getApplicationContext()).getSerialStreamParser().appendData(new String(buffer, 0, message.arg1));
                    LinkedList<DataFormat> data = ((Globals)appContext.getApplicationContext()).getSerialStreamParser().parseStream();

                    for (int i = 0; i < data.size(); i++) {
                        if (data.get(i).getDataSize() == 1) {
                            short id = data.get(i).getId();
                            float value = data.get(i).getData()[0];
                            if (id == DataFormat.GOT_ROLL_P) {
                                currentRollP = value;
                                rollP.setText(String.valueOf(value));
                            } else if (id == DataFormat.GOT_ROLL_D) {
                                currentRollD = value;
                                rollD.setText(String.valueOf(value));
                            } else if (id == DataFormat.GOT_PITCH_P) {
                                currentPitchP = value;
                                pitchP.setText(String.valueOf(value));
                            } else if (id == DataFormat.GOT_PITCH_D) {
                                currentPitchD = value;
                                pitchD.setText(String.valueOf(value));
                            } else if (id == DataFormat.GOT_YAW_P) {
                                currentYawP = value;
                                yawP.setText(String.valueOf(value));
                            } else if (id == DataFormat.GOT_YAW_D) {
                                currentYawD = value;
                                yawD.setText(String.valueOf(value));
                            }
                        }
                    }

                    return true;
                }
                return false;
            }
        });

        if (((Globals)this.getApplicationContext()).getBluetoothAdapter().connect()) {
            ((Globals)this.getApplicationContext()).getBluetoothAdapter().setHandler(handler);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tune, menu);
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
