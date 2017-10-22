package com.murat.bluetoothController;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.UUID;


public class ControlActivity extends ActionBarActivity {

    ProgressDialog progressDialog;
    BTManager btConnection;
    BluetoothSocket bluetoothSocket = null;
    private boolean isConnected = false;

    static final UUID BTUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        String address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);

        setContentView(R.layout.activity_control);

        Button disconnectButton = (Button)findViewById(R.id.button4);

        btConnection = new BTManager(address);
        btConnection.connect();

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btConnection.close();
                finish();
            }
        });

        buildListener(R.id.motor1power,R.id.motor1seek,"1");
        buildListener(R.id.motor2power,R.id.motor2seek,"2");
        buildListener(R.id.motor3power,R.id.motor3seek,"3");
        buildListener(R.id.motor4power,R.id.motor4seek,"4");
    }
    public void buildListener(int textViewId, int seekbarId, final String motorId){
        final TextView motortext  = (TextView) findViewById(textViewId);
        SeekBar motorseek = (SeekBar) findViewById(seekbarId);
        motorseek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                motortext.setText(String.valueOf(i));
                btConnection.send(motorId+ " " + String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(0);
            }
        });
    }
    private void showMessage(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class BTManager extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private String address;
        private BluetoothSocket socket;
        BTManager(String address){
            this.address = address;
        }
        void connect(){
            this.execute();
        }
        void send(String message){
            if(socket!=null){
                try {
                    socket.getOutputStream().write(message.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        void close(){
            if(socket!=null){
                try {
                    socket.close();
                } catch (IOException e) {}
            }
        }
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(ControlActivity.this, "Bağlanılıyor...", "Lütfen Bekleyiniz!!!");
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try {
                if (bluetoothSocket == null || !isConnected) {
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
                    socket = remoteDevice.createInsecureRfcommSocketToServiceRecord(BTUUID);
                    socket.connect();
                }
            }
            catch (IOException e) {
                showMessage("Bağlanılamadı.");
                isConnected = false;
                finish();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (isConnected) {
                showMessage("Bağlantı sağlandı.");
                isConnected = true;
            }
            progressDialog.dismiss();
        }
    }
}
