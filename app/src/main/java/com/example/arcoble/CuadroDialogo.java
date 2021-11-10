package com.example.arcoble;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//Cuadro de diálogo para darle un nuevo identificador al módulo
public class CuadroDialogo  {

    public CuadroDialogo(Context context){

        final Dialog dialogo = new Dialog(context);
        dialogo.setCancelable(false);
        dialogo.setContentView(R.layout.dialogo_id);

        TextView tvIdDialogTitle = dialogo.findViewById(R.id.tvIdDialogTitle);
        TextView tvIdDialog = dialogo.findViewById(R.id.tvIdDialog);
        EditText etCambio = dialogo.findViewById(R.id.etCambio);
        Button btnIdCancel = dialogo.findViewById(R.id.btnIdCancel);
        Button btnIdOk = dialogo.findViewById(R.id.btnIdOk);

        etCambio.setHint(Datos2Activity.idNuevo);

        btnIdOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(etCambio.getText().toString().length()==10){
                    if(Datos2Activity.validarLetras(etCambio.getText().toString().substring(0,7)) && Datos2Activity.validarNumeros(etCambio.getText().toString().substring(7,10))){
                        Datos2Activity.idNuevo = etCambio.getText().toString().toUpperCase();
                        Toast.makeText(context, "Se va a reiniciar el módulo y salir de la aplicación, para que el cambio de ID, se haga efectivo. Tendrá que esperar un minuto aprox. para poder volver a conectarse.", Toast.LENGTH_LONG).show();
                        Datos2Activity.caracEscritura.setValue("$n" + Datos2Activity.idNuevo);
                        Datos2Activity.mBluetoothGatt.writeCharacteristic(Datos2Activity.caracEscritura);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        dialogo.dismiss();

                    }else {
                        Toast.makeText(context,"Por favor, el formato del ID, debe ser 7 letras y 3 números", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(context,"Por favor, la longitud del ID, debe ser de 10 caracteres", Toast.LENGTH_LONG).show();
                }

            }
        });

        btnIdCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogo.dismiss();
            }
        });
        dialogo.show();
    }

}
