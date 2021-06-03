package com.example.arcoble;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView tvEstado, tvEstado2, tvTrama;

    Button btnSalir, btnScan;

    ProgressBar progressBar;

    ListView scanListView;

    private int REQUEST_ENABLE_BT = 1;

    ArrayList<DeviceBLE> listaDispositivos = new ArrayList<DeviceBLE>();
    ArrayAdapter<DeviceBLE> adaptador ;

    BluetoothAdapter myAdapter = BluetoothAdapter.getDefaultAdapter();

    public static BluetoothGatt mBluetoothGatt;

    BluetoothDevice dispositivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Quito la ActionBar, ya que uso un toolbar personalizado
        //getSupportActionBar().hide();

        //Mantengo la aplicación fija en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        tvEstado = findViewById(R.id.tvEstado);
        tvEstado2 = findViewById(R.id.tvEstado2);
        tvTrama = findViewById(R.id.tvTrama);

        btnSalir = findViewById(R.id.btnSalir);
        btnScan = findViewById(R.id.btnScan);

        progressBar = findViewById(R.id.progressBar);

        scanListView = findViewById(R.id.listView);

        progressBar.setVisibility(View.INVISIBLE);

        ///////////////////////////   PERMISOS   ////////////////////////////////////////////////////////

        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {

            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }

        //Compruebo que el dispositivo sea compatible con Bluetooth
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            Toast.makeText(this, "Dispositivo no compatible con Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Comprueba que el bluetooth esté activado
        if(!bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //Compruebo que la locaclización esté activada
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            activaLoc();
        }


        //Compruebo que el dispositivo sea compatible
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Dispositivo no compatible con Bluetooth_LE", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            Toast.makeText(getApplicationContext(), "Dispositivo compatible con BLE", Toast.LENGTH_LONG).show();
        }

        /////////////////////////////////////////////////////////////////////////////////////////////

        //Botón para comenzar el escaneo de dispositivos
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                new Hilo().start();//En el hilo, está toda la funcionalidad
            }
        });



        //Botón para salir de la aplicación
        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salir();
            }
        });

        adaptador = new ArrayAdapter<DeviceBLE>(getApplicationContext(), android.R.layout.simple_list_item_1,listaDispositivos);
        scanListView.setAdapter(adaptador);

        scanListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice deviceSelected = listaDispositivos.get(position).getMAC();
                dispositivo = deviceSelected;

                Intent intent = new Intent(getApplicationContext(), DatosActivity.class);
                intent.putExtra("Dispositivo", dispositivo);
                startActivity(intent);

                //mBluetoothGatt = deviceSelected.connectGatt(getApplicationContext(),false, mGattCallback);
                Toast.makeText(getApplicationContext(), deviceSelected.getName(),Toast.LENGTH_LONG).show();

            }
        });

    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Método para activar la localización
    private void activaLoc(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El GPS está desactivado y es necesario para usar la aplicación. ¿Quiere activarlo?")
                .setCancelable(false)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    //Método para escanear dispositivos BLE
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            // Se ha encontrado el dispositivo. Podemos obtener la información
            if(device.getName()!=null){
                Map<String,DeviceBLE> mapa = new HashMap<>();

                for(DeviceBLE dev : listaDispositivos){
                    mapa.put(dev.toString(),dev);

                }
                mapa.put(device.getName(), new DeviceBLE(device.getName(), device));

                listaDispositivos.clear();
                listaDispositivos.addAll(mapa.values());
                adaptador.notifyDataSetChanged();

            }
        }
    };


    public void salir(){
        if(mBluetoothGatt != null){
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            finishAffinity();

        }else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            finishAffinity();

        }
    }


    class Hilo extends Thread{
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adaptador.clear();
                    myAdapter.startLeScan(mLeScanCallback);

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    myAdapter.stopLeScan(mLeScanCallback);
                    scanListView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

}

