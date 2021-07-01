package com.example.arcoble;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.TextView;

import java.util.UUID;

public class CanActivity extends AppCompatActivity {

    TextView info;

    UUID uuidServ, uuidCaracCan;
    BluetoothGattCharacteristic caracCan;

    Bundle datos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_can);

        info = findViewById(R.id.tvInfo);

        //Mantengo la aplicación fija en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        datos = getIntent().getExtras();

        String recibo = datos.getString("Datos");

        info.setText("Información recibida: " + recibo);

    }



}

