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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class DatosActivity extends AppCompatActivity {

    TextView tvEstado, tvEstado2, tvID;
    TextView tvConexionRed, tvConexionInternet, tvServicio, tvMarcha, tvPulsos, tvAnalogica, tvAnalogica2, tvCuba, tvGiroCuba, tvLatitud, tvLongitud, tvEstadoGPS, tvSatelites, tvRssi;
    EditText etID;
    ImageView ledMarcha, ledGPS, imgSenal, imgRele, imgCan;
    Button btnRele, btnCAN, btnPosicion, btnID, btnDesconectar, btnCancelar;

    Bundle datos;

    String conexionRed, conexionInternet, servicio, marcha, cuba, analogica, analogica2, pulsos, estadoGPS, tramaNS, latitud, tramaEW, longitud, satelites, giroCuba, icc, imei, can, rele, valor, idNuevo;
    int servic, contador;
    byte []value;
    boolean conectado;

    BluetoothDevice dispositivo;
    public static BluetoothGatt mBluetoothGatt;

    UUID uuidServ, uuidCarac;
    UUID uuid ;

    List<BluetoothGattService> services;
    List<BluetoothGattCharacteristic> caracteristicas;

    BluetoothGattCharacteristic caracEscritura;

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
        setContentView(R.layout.activity_datos);

        //Mantengo la aplicación fija en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        conectado = false;

        datos = getIntent().getExtras();
        dispositivo = (BluetoothDevice) datos.get("Dispositivo");

        tvEstado = findViewById(R.id.tvEstado);
        tvEstado2 = findViewById(R.id.tvEstado2);
        tvID = findViewById(R.id.tvID);

        tvConexionRed = findViewById(R.id.tvConexionRed);
        tvConexionInternet = findViewById(R.id.tvConexionInternet);
        tvServicio = findViewById(R.id.tvServicio);
        tvMarcha = findViewById(R.id.tvMarcha);
        tvPulsos = findViewById(R.id.tvPulsos);
        tvAnalogica = findViewById(R.id.tvAnalogica);
        tvAnalogica2 = findViewById(R.id.tvAnalogica2);
        tvCuba = findViewById(R.id.tvCuba);
        tvGiroCuba = findViewById(R.id.tvGiroCuba);
        tvLatitud = findViewById(R.id.tvLatitud);
        tvLongitud = findViewById(R.id.tvLongitud);
        tvEstadoGPS = findViewById(R.id.tvEstadoGPS);
        tvSatelites = findViewById(R.id.tvSatelites);
        tvRssi = findViewById(R.id.tvRssi);
        etID = findViewById(R.id.etID);
        ledMarcha = findViewById(R.id.ledMarcha);
        ledGPS = findViewById(R.id.ledGPS);
        imgSenal = findViewById(R.id.imgSenal);
        imgRele = findViewById(R.id.imgRele);
        imgCan = findViewById(R.id.imgCan);

        btnRele = findViewById(R.id.btnRele);
        btnCAN = findViewById(R.id.btnCAN);
        btnPosicion = findViewById(R.id.btnPosicion);
        btnID = findViewById(R.id.btnID);
        btnDesconectar = findViewById(R.id.btnDesconectar);
        btnCancelar = findViewById(R.id.btnCancel);

        //Servicio y característica creados en aplicación smartBasic
        uuidServ = UUID.fromString("00000333-0000-1000-8000-00805f9b34fb");//Servicio
        uuidCarac = UUID.fromString("00000777-0000-1000-8000-00805f9b34fb");//Característica


        //mBluetoothGatt = deviceSelected.connectGatt(getApplicationContext(),false, mGattCallback);
        mBluetoothGatt = dispositivo.connectGatt(getApplicationContext(),false, mGattCallback);

        tvID.setVisibility(View.INVISIBLE);
        etID.setVisibility(View.INVISIBLE);
        btnCancelar.setVisibility(View.INVISIBLE);

        imgSenal.setVisibility(View.VISIBLE);

        //////////////////////////////   BOTONES   //////////////////////////////////////////////////////////////

        btnDesconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desconectar();
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
                AlertDialog.Builder alerta = new AlertDialog.Builder(DatosActivity.this);
                alerta.setMessage("¿Desea cambiar el ID del módulo?")
                        .setCancelable(false)
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tvID.setVisibility(View.VISIBLE);
                                etID.setVisibility(View.VISIBLE);
                                btnCancelar.setVisibility(View.VISIBLE);
                                etID.setHint(dispositivo.getName());
                                etID.setOnEditorActionListener(new TextView.OnEditorActionListener(){

                                    @Override
                                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                        if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId==EditorInfo.IME_ACTION_DONE ||
                                                event.getAction()==KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER){

                                            if(etID.getText().toString().length()==10){
                                                if(validarLetras(etID.getText().toString().substring(0,7)) && validarNumeros(etID.getText().toString().substring(7,10))){

                                                    idNuevo = etID.getText().toString().toUpperCase();

                                                    tvID.setVisibility(View.INVISIBLE);
                                                    etID.setVisibility(View.INVISIBLE);

                                                    alerta.setMessage("Se va a reiniciar el módulo y salir de la aplicación, para que el cambio de ID, se haga efectivo. Tendrá que esperar un minuto aprox. para poder volver a conectarse. ¿Quiere continuar?")
                                                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    caracEscritura.setValue("$n" + idNuevo);
                                                                    mBluetoothGatt.writeCharacteristic(caracEscritura);
                                                                    try {
                                                                        Thread.sleep(5000);
                                                                    } catch (InterruptedException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    salir();
                                                                }
                                                            })
                                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.cancel();
                                                                    Toast.makeText(getApplicationContext(),"Ha pulsado no cambiar ID",Toast.LENGTH_LONG).show();
                                                                }
                                                            });

                                                    AlertDialog titulo = alerta.create();
                                                    titulo.setTitle("Cambiar ID");
                                                    titulo.show();

                                                }else{
                                                    Toast.makeText(getApplicationContext(),"Por favor, el formato dle ID, debe ser 7 letras y 3 números", Toast.LENGTH_LONG).show();
                                                }
                                            }else{
                                                Toast.makeText(getApplicationContext(),"Por favor, la longitud del ID, debe ser de 10 caracteres", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        return false;
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                Toast.makeText(getApplicationContext(),"Ha pulsado no cambiar ID",Toast.LENGTH_LONG).show();
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("Cambiar ID");
                titulo.show();

                btnCancelar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvID.setVisibility(View.INVISIBLE);
                        etID.setVisibility(View.INVISIBLE);
                        btnCancelar.setVisibility(View.INVISIBLE);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etID.getWindowToken(), 0);
                        return;
                    }
                });
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
                    activarCAN();
                }
                if(can.equals("1")){
                    Toast.makeText(getApplicationContext(),"El bus CAN, está activado, pero no tiene comunicación", Toast.LENGTH_LONG).show();
                    desactivarCAN();
                }
                if(can.equals("2")){
                    Toast.makeText(getApplicationContext(),"El bus CAN está OK, activado y con comunicación", Toast.LENGTH_LONG).show();
                    desactivarCAN();
                }
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////////////////

    }


    public boolean validarLetras(String cadena){
        return cadena.matches("[a-zA-Z]*");
    }

    public boolean validarNumeros(String cadena){
        return cadena.matches("[0-9]*");
    }

    public void activarCAN(){
        AlertDialog.Builder alerta = new AlertDialog.Builder(DatosActivity.this);
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
                        salir();
                    }

                });

        AlertDialog titulo = alerta.create();
        titulo.setTitle("Activar bus CAN");
        titulo.show();
    }

    public void desactivarCAN(){

        AlertDialog.Builder alerta = new AlertDialog.Builder(DatosActivity.this);
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
                        salir();
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
                tvRssi.setText("Señal: " + rssi + " dBm");

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
                tvEstado.setText(dispositivo.getName());


                for(int i=0;i<servic;i++){
                    Log.d("onServicesDiscovered","uuid::" + gatt.getServices().get(i).getUuid());
                }

                caracEscritura = mBluetoothGatt.getService(uuidServ).getCharacteristic(uuidCarac);
                uuid = gatt.getServices().get(servic-1).getUuid();
                BluetoothGattService servicio = mBluetoothGatt.getService(uuid);
                caracteristicas = servicio.getCharacteristics();
                mBluetoothGatt.readCharacteristic(caracteristicas.get(0));
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
        }


        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            value = new byte[0];
            if (status == BluetoothGatt.GATT_SUCCESS) {

                value = characteristic.getValue();
                valor = new String(value);
                Log.d("TAG","onCharacteristicRead: " + valor);


                valor = valor.replace("$d,","");

                try {
                    //Separo la trama
                    String[] partes = valor.split("\\|");
                    conexionRed = partes[0];
                    tvConexionRed.setText("Red: " + asignaConexionRed(conexionRed));
                    conexionInternet = partes[1];
                    tvConexionInternet.setText("Internet: " + asignaConexionInternet(conexionInternet));
                    servicio = partes[2];
                    tvServicio.setText("Servicio ARCO: " + asignaServicio(servicio));
                    marcha = partes[3];
                    tvMarcha.setText("Estado Marcha ");
                    asignaMarcha(marcha);
                    giroCuba = partes[4];
                    tvGiroCuba.setText("Vueltas: " + giroCuba + " rpm");
                    analogica = partes[5];
                    tvAnalogica.setText("Presión: " + analogica + " mv");
                    analogica2 = partes[6];
                    tvAnalogica2.setText("Temperatura: " + analogica2 + " mv");
                    pulsos = partes[7];
                    tvPulsos.setText("Contador de Pulsos: " + pulsos);
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
                    tvCuba.setText("Cuba: " + asignaGiro(cuba));
                    icc = partes[15];
                    imei = partes[16];
                    can = partes[17];
                    asignaCAN(can);
                    rele = partes[18];
                    asignaRele(rele);
                    boolean rssiStatus = mBluetoothGatt.readRemoteRssi();

                }catch(Exception e){
                    Log.e("Fallo trama", "Ha habido un error al recibir la trama");
                    Log.e("TAG",e.getMessage());
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
            tvEstado2.setText("ICC ID: " + icc + "\nIMEI: " + imei);
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
                    servicio = "Caído";
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
        public void asignaMarcha(String marcha){

            switch (marcha){
                case "0":
                    //ledMarcha.setImageResource(R.drawable.led_verde);
                    ledMarcha.setImageResource(R.drawable.verde);
                    break;
                case "1":
                    //ledMarcha.setImageResource(R.drawable.led_rojo);
                    ledMarcha.setImageResource(R.drawable.rojo);
                    break;
            }
        }


        //Estado GPS
        public String asignaEstadoGPS(String estado){
            switch(estado){
                case "1":
                    estado = "No Disponible";
                    //ledGPS.setImageResource(R.drawable.led_rojo);
                    ledGPS.setImageResource(R.drawable.rojo);
                    break;
                case "2":
                    estado = "2D";
                    //ledGPS.setImageResource(R.drawable.led_verde);
                    ledGPS.setImageResource(R.drawable.naranja);
                    break;
                case "3":
                    estado = "3D";
                    //ledGPS.setImageResource(R.drawable.led_verde);
                    ledGPS.setImageResource(R.drawable.verde);
                    break;
            }
            return estado;
        }

        //Giro Cuba
        public String asignaGiro(String giro){
            switch (giro){
                case "false":
                    giro = "Mezclando;\nGiro drcha";
                    break;
                case "true":
                    giro = "Descargando;\nGiro izda";
                    break;
            }
            return giro;
        }

        public void asignaCAN(String valor){

            if(valor.equals("0")){
                imgCan.setImageResource(R.drawable.rojo);
            }
            if(valor.equals("1") || valor.equals("2")){
                imgCan.setImageResource(R.drawable.verde);
            }
            if(valor.equals("3")){
                imgCan.setImageResource(R.drawable.gris);
            }
        }

        public void asignaRele(String valor){
            if(valor.equals("0")){
                imgRele.setImageResource(R.drawable.rojo);
            }
            if(valor.equals("1")){
                imgRele.setImageResource(R.drawable.verde);
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            value = characteristic.getValue();
            valor = new String(value);
            Log.e("TAG","onCharacteristicRead: " + valor);
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            mBluetoothGatt.readCharacteristic(caracteristicas.get(1));
            Log.d("Escribiendo","Escribiendo característica: " + characteristic.getStringValue(0));

        }
    };

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