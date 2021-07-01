package com.example.arcoble;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Datos2Activity extends AppCompatActivity {

    Context context;

    TextView tvEstado;
    TextView tvIcc, tvImei, tvRssi, tvConexionRed, tvConexionInternet, tvServicio, tvMarcha, tvGiro, tvRpm, tvTemperatura,tvPresion, tvAux, tvContador, tvContador2, tvEstadoGPS, tvLatitud, tvLongitud, tvSatelites;
    ImageView  imgSenal;
    Button  btnPosicion, btnID, btnRs232, btnDesconectar;
    ToggleButton btnRele, btnCAN;

    String datosCan;

    Bundle datos;

    static String conexionRed, conexionInternet, servicio, marcha, cuba, analogica, analogica2, pulsos, estadoGPS, tramaNS, latitud, tramaEW, longitud, satelites, giroCuba, icc, imei, can, rele, valor, idNuevo, aux, contador2;
    int servic, contador, cuenta;
    byte []value;
    boolean conectado, rs232;

    static BluetoothDevice dispositivo;
    public static BluetoothGatt mBluetoothGatt;

    UUID uuidServ, uuidCarac, uuidCaracCan;
    UUID uuid ;

    List<BluetoothGattService> services;
    List<BluetoothGattCharacteristic> caracteristicas;

    static BluetoothGattCharacteristic caracEscritura, caracCan;

    private int connectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos2);

        context = this;

        //Mantengo la aplicación fija en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        conectado = false;
        rs232 = false;

        datos = getIntent().getExtras();
        dispositivo = (BluetoothDevice) datos.get("Dispositivo");
        idNuevo = dispositivo.getName();

        tvEstado = findViewById(R.id.tvEstado);

        tvIcc = findViewById(R.id.tvIcc);
        tvImei = findViewById(R.id.tvImei);
        tvRssi = findViewById(R.id.tvRssi);
        tvConexionRed = findViewById(R.id.tvConexionRed);
        tvConexionInternet = findViewById(R.id.tvConexionInternet);
        tvServicio = findViewById(R.id.tvServicio);
        tvMarcha = findViewById(R.id.tvMarcha);
        tvGiro = findViewById(R.id.tvGiro);
        tvRpm = findViewById(R.id.tvRpm);
        tvTemperatura = findViewById(R.id.tvTemperatura);
        tvPresion = findViewById(R.id.tvPresion);
        tvAux = findViewById(R.id.tvAux);
        tvContador = findViewById(R.id.tvContador);
        tvContador2 = findViewById(R.id.tvContador2);
        tvEstadoGPS = findViewById(R.id.tvEstadoGPS);
        tvLatitud = findViewById(R.id.tvLatitud);
        tvLongitud = findViewById(R.id.tvLongitud);
        tvSatelites = findViewById(R.id.tvSatelites);

        imgSenal = findViewById(R.id.imgSenal);

        btnRele = findViewById(R.id.btnRele);
        btnCAN = findViewById(R.id.btnCAN);
        btnPosicion = findViewById(R.id.btnPosicion);
        btnID = findViewById(R.id.btnID);
        btnRs232 = findViewById(R.id.btnRs232);
        btnDesconectar = findViewById(R.id.btnDesconectar);


        //Servicio y característica creados en aplicación smartBasic
        uuidServ = UUID.fromString("00000333-0000-1000-8000-00805f9b34fb");//Servicio
        uuidCarac = UUID.fromString("00000777-0000-1000-8000-00805f9b34fb");//Característica
        uuidCaracCan = UUID.fromString("00000888-0000-1000-8000-00805f9b34fb");//Característica para lectura datos CAN


        //mBluetoothGatt = deviceSelected.connectGatt(getApplicationContext(),false, mGattCallback);
        mBluetoothGatt = dispositivo.connectGatt(getApplicationContext(),false, mGattCallback);

        imgSenal.setVisibility(View.VISIBLE);


        //////////////////////////////   BOTONES   //////////////////////////////////////////////////////////////

        btnDesconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desconectar();
            }
        });

        btnRs232.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cuenta++;

                caracEscritura.setValue("$z" + cuenta);
                mBluetoothGatt.writeCharacteristic(caracEscritura);

            }
        });


        //Botón activa relé
        btnRele.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    contador++;
                    caracEscritura.setValue("$r" + contador);
                    mBluetoothGatt.writeCharacteristic(caracEscritura);

                }catch(Exception e){
                    Toast.makeText(getApplicationContext(),"Por favor, conéctese a un dispositivo", Toast.LENGTH_LONG).show();
                    tvEstado.setText("Por favor, conéctese a un dispositivo");
                }
            }
        });


        //Botón muestra mapa con la posición
        btnPosicion.setOnClickListener(v -> {
            Intent intent = new Intent(this,MapsActivity.class);

            if(tramaNS!=null && tramaEW!=null) {
                if (tramaNS.equals("S")) {
                    latitud = "-" + latitud;
                }
                if (tramaEW.equals("W")) {
                    longitud = "-" + longitud;
                }

                intent.putExtra("lati", latitud);
                intent.putExtra("longi", longitud);
                startActivity(intent);
            }else{
                Toast.makeText(getApplicationContext(), "No existe posición actual, por favor conéctese a algún dispositivo que le ofrezca sus coordenadas",Toast.LENGTH_LONG).show();
                tvEstado.setText("Por favor, conéctese a un dispositivo");
            }
        });


        //Botón para cambiar el ID
        btnID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CuadroDialogo(context);
            }
        });


        //Botón activar CAN
        btnCAN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(can == null){
                    Toast.makeText(getApplicationContext(),"Por favor, espere un momento a que llegue la trama", Toast.LENGTH_LONG).show();
                    can = "3";
                }

                if(can.equals("0")){
                    Toast.makeText(getApplicationContext(),"El bus CAN, está desactivado", Toast.LENGTH_LONG).show();
                    btnCAN.setChecked(false);
                    activarCAN();
                }
                if(can.equals("1")){
                    Toast.makeText(getApplicationContext(),"El bus CAN, está activado, pero no tiene comunicación", Toast.LENGTH_LONG).show();
                    btnCAN.setChecked(true);
                    desactivarCAN();
                }
                if(can.equals("2")){
                    Toast.makeText(getApplicationContext(),"El bus CAN está OK, activado y con comunicación", Toast.LENGTH_LONG).show();
                    btnCAN.setChecked(true);
                    desactivarCAN();
                }
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////////////////

    }


    public static boolean validarLetras(String cadena){
        return cadena.matches("[a-zA-Z]*");
    }

    public static boolean validarNumeros(String cadena){
        return cadena.matches("[0-9]*");
    }

    public void activarCAN(){
        AlertDialog.Builder alerta = new AlertDialog.Builder(Datos2Activity.this);
        alerta.setMessage("El bus CAN está desactivado. Se va a reiniciar el módulo y salir de la aplicación, para que la activación, se haga efectiva. Tendrá que esperar un minuto aprox. para poder volver a conectarse. ¿Realmente quiere continuar y activar el bus CAN?")
                .setCancelable(false)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Toast.makeText(getApplicationContext(),"Ha pulsado no activar el bus CAN",Toast.LENGTH_LONG).show();
                    }
                })
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        caracEscritura.setValue("$a" + "");
                        mBluetoothGatt.writeCharacteristic(caracEscritura);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                       // salir();
                    }
                });

        AlertDialog titulo = alerta.create();
        titulo.setTitle("Activar bus CAN");
        titulo.show();
    }

    public void desactivarCAN(){

        AlertDialog.Builder alerta = new AlertDialog.Builder(Datos2Activity.this);
        alerta.setMessage("El bus CAN está activado. Se va a reiniciar el módulo y salir de la aplicación, para que la desactivación, se haga efectiva. Tendrá que esperar un minuto aprox. para poder volver a conectarse. ¿Realmente quiere continuar y desactivar el bus CAN?")
                .setCancelable(false)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Toast.makeText(getApplicationContext(),"Ha pulsado no desactivar el bus CAN",Toast.LENGTH_LONG).show();
                    }
                })
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        caracEscritura.setValue("$q"+"");
                        mBluetoothGatt.writeCharacteristic(caracEscritura);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //salir();
                    }
                });

        AlertDialog titulo = alerta.create();
        titulo.setTitle("Desactivar bus CAN");
        titulo.show();
    }


    //Método para conectar al dispositivo BLE a través de un servidor GATT
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                tvEstado.setText("Conectado al servidor GATT. Intentando descubrir servicios");
                conectado = true;

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothGatt.discoverServices();
                intentAction = ACTION_GATT_CONNECTED;
                connectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                conectado = false;
                tvEstado.setText("Conexión caída. Reconectando...");
                imgSenal.setImageResource(R.drawable.senal_0);
                gatt.close();
                mBluetoothGatt = dispositivo.connectGatt(getApplicationContext(),false, mGattCallback);//Intento reconectar
                Log.e("Conexión caída:","Fallo en conexión. Reconectando...");
                intentAction = ACTION_GATT_DISCONNECTED;
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            //super.onReadRemoteRssi(gatt, rssi, status);
            if(status==BluetoothGatt.GATT_SUCCESS){
                tvRssi.setText("Señal Bluetooth: " + rssi + " dBm");

                if(rssi>= -50  && conectado){
                    imgSenal.setImageResource(R.drawable.senal_100);
                }else if(rssi>= -70 && rssi< -50 && conectado){
                    imgSenal.setImageResource(R.drawable.senal_100);
                }else if(rssi>= -80 && rssi< -70 && conectado){
                    imgSenal.setImageResource(R.drawable.senal_75);
                }else if(rssi>= -90 && rssi < -80 && conectado){
                    imgSenal.setImageResource(R.drawable.senal_50);
                }else if(rssi>= -95 && rssi < -90 && conectado){
                    imgSenal.setImageResource(R.drawable.senal_25);
                }else if(rssi < -95 || !conectado){
                    imgSenal.setImageResource(R.drawable.senal_0);
                }
            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if(services != null) {
                services.clear();
                services = null;
            }

            if (status == BluetoothGatt.GATT_SUCCESS) {

                services = new ArrayList<>();
                services.addAll(gatt.getServices());
                servic = gatt.getServices().size();
                //tvEstado.setText(dispositivo.getName());
                tvEstado.setText(idNuevo);

                for(int i=0;i<servic;i++){
                    Log.d("onServicesDiscovered","uuid::" + gatt.getServices().get(i).getUuid());
                }

                caracEscritura = mBluetoothGatt.getService(uuidServ).getCharacteristic(uuidCarac);
                caracCan = mBluetoothGatt.getService(uuidServ).getCharacteristic(uuidCaracCan);
                uuid = gatt.getServices().get(servic-1).getUuid();
                BluetoothGattService servicio = mBluetoothGatt.getService(uuid);
                caracteristicas = servicio.getCharacteristics();

                /*mBluetoothGatt.readCharacteristic(caracteristicas.get(0));
                mBluetoothGatt.readCharacteristic(caracEscritura);*/

                for(int i=0;i<caracteristicas.size();i++){
                    mBluetoothGatt.readCharacteristic(caracteristicas.get(i));
                }


                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
        }


        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

           // rs232 = false;

            value = new byte[0];
            if (status == BluetoothGatt.GATT_SUCCESS) {

                value = characteristic.getValue();
                valor = new String(value);


                Log.d("TAG","onCharacteristicRead: " + valor);


                if(valor.startsWith("$d")) {

                    valor = valor.replace(" $d,", "");

                    try {
                        //Separo la trama
                        String[] partes = valor.split("\\|");
                        conexionRed = partes[0];
                        conexionRed = conexionRed.replace("$d,","");
                        tvConexionRed.setText("Red: " + asignaConexionRed(conexionRed));
                        conexionInternet = partes[1];
                        tvConexionInternet.setText("Internet: " + asignaConexionInternet(conexionInternet));
                        servicio = partes[2];
                        tvServicio.setText("Servicio ARCO: " + asignaServicio(servicio));
                        marcha = partes[3];
                        tvMarcha.setText("I1 - Marcha: " + asignaMarcha(marcha));
                        //asignaMarcha(marcha);
                        giroCuba = partes[4];
                        tvRpm.setText("I2 + I3 - RPM: " + giroCuba + " rpm");
                        analogica = partes[5];
                        tvPresion.setText("A2 - Presión: " + analogica + " mv");
                        analogica2 = partes[6];
                        tvTemperatura.setText("A1 - Temp: " + analogica2 + " mv");
                        pulsos = partes[7];
                        tvContador.setText("C1 - Contador : " + pulsos);
                        estadoGPS = partes[8];
                        tvEstadoGPS.setText("Estado GPS: " + asignaEstadoGPS(estadoGPS));
                        tramaNS = partes[9];
                        latitud = partes[10];
                        tvLatitud.setText("Latitud: " + trataCoordenada(latitud));
                        tramaEW = partes[11];
                        longitud = partes[12];
                        tvLongitud.setText("Longitud: " + trataCoordenada(longitud));
                        satelites = partes[13];
                        tvSatelites.setText("Nº Satélites: " + satelites);
                        cuba = partes[14];
                        tvGiro.setText("I2 + I3 - Giro: " + asignaGiro(cuba));
                        icc = partes[15];
                        tvIcc.setText("ICC: " + icc);
                        imei = partes[16];
                        tvImei.setText("IMEI: " + imei);
                        can = partes[17];
                        asignaCAN(can);
                        rele = partes[18];
                        asignaRele(rele);
                        aux = partes[19];
                        if (aux.equals("")) {
                            aux = "0";
                        }
                        tvAux.setText("A3 - Aux: " + aux);
                        contador2 = partes[20];
                        tvContador2.setText("C2 - Contador: " + contador2);


                        boolean rssiStatus = mBluetoothGatt.readRemoteRssi();


                    } catch (Exception e) {
                        Log.e("Fallo trama", "Ha habido un error al recibir la trama");
                        Log.e("TAG", e.getMessage());
                    }
                }

                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                if(valor.startsWith("$Z")){
                    Log.i("Trama CAN","Valor recibido: " + valor);
                    Log.i("Resultado prueba", "Satisfactoria");
                    rs232 = true;
                    notifica(rs232);
                    }

                if(valor.startsWith("$z")){
                    Log.i("Trama CAN","Valor recibido: " + valor);
                    Log.i("Resultado prueba","Fallida");
                    rs232 = false;
                    notifica(rs232);
                    caracEscritura.setValue("$w" + contador);
                    mBluetoothGatt.writeCharacteristic(caracEscritura);
                    //caracEscritura.setValue("");
                    //mBluetoothGatt.writeCharacteristic(caracEscritura);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

                try {
                    mBluetoothGatt.readCharacteristic(caracteristicas.get(0));
                }catch(Exception ex){
                    Log.e("Error característica","La característica está vacía");
                }


            }else{
                tvEstado.setText("Conexión caída, reconectando...");

                gatt.close();
                mBluetoothGatt = dispositivo.connectGatt(getApplicationContext(),false, mGattCallback);//Intento reconectar
                Log.e("Conexión caída:","Fallo en característica. Reconectando...");
            }

        }

        //////////////////Asigno valores///////////////////////////////////

        //Tratar coordenadas: acortar el dato, si viene muy largo
        public String trataCoordenada(String coordenada){
            if(coordenada.length()>10){
                coordenada = coordenada.substring(0,9);
            }
            return coordenada;
        }

        //Conexión de red
        public String asignaConexionRed(String conexion){
            switch(conexion){
                case "0":
                    conexion = "Sin Registrar";
                    break;
                case "1":
                    conexion = "Registrada";
                    break;
                case "2":
                    conexion = "Registrando";
                    break;
                case "3":
                    conexion = "Denegada";
                    break;
                case "4":
                    conexion = "Desconocida";
                    break;
                case "5":
                    conexion = "Roaming";
                    break;
                default:
                    conexion = "";
                    break;

            }
            return  conexion;
        }

        //Conexión a Internet
        public String asignaConexionInternet(String conexionInternet){
            switch (conexionInternet){
                case "0":
                    conexionInternet = "Caído";
                    break;
                case "1":
                    conexionInternet = "Conectando";
                    break;
                case "2":
                    conexionInternet = "Conectado";
                    break;
                case "3":
                    conexionInternet = "Limitado";
                    break;
                case "4":
                    conexionInternet = "Cerrando";
                    break;

            }
            return conexionInternet;
        }

        //Servicio ARCO
        public String asignaServicio(String servicio){
            switch(servicio){
                case "2":
                    servicio = "Definido sin conectar";
                    break;
                case "3":
                    servicio = "Conectando";
                    break;
                case "4":
                    servicio = "OK";
                    break;
                case "5":
                    servicio = "Cerrando";
                    break;
                case "6":
                    //servicio = "Caído";
                    servicio = "Sin servicio";
                    break;
                case "7":
                    servicio = "Alerta";
                    break;
                case "8":
                    servicio = "Conectado";
                    break;
                case "9":
                    servicio = "Desconectado";
                    break;
                case "-1":
                    servicio = "Sin Cobertura";
                    break;
            }
            return servicio;
        }

        //Marcha
        public String asignaMarcha(String marcha){

            switch (marcha){
                case "0":
                    //tvMarcha.setText("I1 - Marcha: ON" );
                    marcha = "ON";
                    break;
                case "1":
                    //tvMarcha.setText("I1 - Marcha: ON" );
                    marcha = "OFF";
                    break;
            }
            return marcha;
        }


        //Estado GPS
        public String asignaEstadoGPS(String estado){
            switch(estado){
                case "1":
                    estado = "No Disponible";
                    //ledGPS.setImageResource(R.drawable.led_rojo);
                    //ledGPS.setImageResource(R.drawable.rojo);
                    break;
                case "2":
                    estado = "2D";
                    //ledGPS.setImageResource(R.drawable.led_verde);
                    //ledGPS.setImageResource(R.drawable.naranja);
                    break;
                case "3":
                    estado = "3D";
                    //ledGPS.setImageResource(R.drawable.led_verde);
                    //ledGPS.setImageResource(R.drawable.verde);
                    break;
            }
            return estado;
        }

        //Giro Cuba
        public String asignaGiro(String giro){
            switch (giro){
                case "false":
                    giro = "Derecha";
                    break;
                case "true":
                    giro = "Izquierda";
                    break;
            }
            return giro;
        }

        public void asignaCAN(String valor){

            if(valor.equals("0")){
                btnCAN.setChecked(false);
               // imgCan.setImageResource(R.drawable.rojo);
            }
            if(valor.equals("1") || valor.equals("2")){
                btnCAN.setChecked(true);
               // imgCan.setImageResource(R.drawable.verde);
            }
            if(valor.equals("3")){
               // imgCan.setImageResource(R.drawable.gris);
            }
        }

        public void asignaRele(String valor){
            if(valor.equals("0")){
                btnRele.setChecked(false);
              //  imgRele.setImageResource(R.drawable.rojo);
            }
            if(valor.equals("1")){
                btnRele.setChecked(true);
               // imgRele.setImageResource(R.drawable.verde);
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            value = characteristic.getValue();
            valor = new String(value);
            Log.e("TAG","onCharacteristicRead cuando cambia: " + valor);
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            Log.d("Escribiendo","Escribiendo característica: " + characteristic.getStringValue(0));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBluetoothGatt.readCharacteristic(characteristic);


        }
    };

    private void notifica(boolean correcto) {

        if(correcto){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"La prueba ha resultado satisfactoria", Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"La prueba ha resultado fallida", Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (uuid.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;

            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;

            }
            final int heartRate = characteristic.getIntValue(format, 1);

            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                        stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public void desconectar(){
        if(mBluetoothGatt != null){
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

        }else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }


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
}