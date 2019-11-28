package com.example.proyectocomidas;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatosCompraActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    EditText nombreText, emailText, direccionText, telefonoText, observacionesText;
    Spinner spinner;
    Button btnPedido;
    SharedPreferences preferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_compra);
        init();
    }

    private void init(){
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        nombreText = findViewById(R.id.nombreText);
        emailText = findViewById(R.id.emailText);
        direccionText = findViewById(R.id.direccionText);
        telefonoText = findViewById(R.id.telefonoText);
        observacionesText = findViewById(R.id.observacionesText);
        btnPedido = findViewById(R.id.btnPedido);
        spinner = findViewById(R.id.horaSelect);

        ArrayList<String> horas = getHours();

        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, horas));

        //spinner.getSelectedItem().toString();

        if(firebaseAuth.getCurrentUser() != null){

            getUser(firebaseAuth.getCurrentUser().getEmail());

        }else{
            Log.e("Usuario", "No hay usuario logueado");
        }

        btnPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doOrder();
            }
        });
    }

    private void getUser(final String email){
        firebaseFirestore.collection("Usuarios").whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() > 0) {

                      String nombre = task.getResult().getDocuments().get(0).getData().get("nombre").toString();
                      String email = task.getResult().getDocuments().get(0).getData().get("email").toString();
                      String direccion = task.getResult().getDocuments().get(0).getData().get("direccion").toString();
                      String telefono = task.getResult().getDocuments().get(0).getData().get("telefono").toString();

                      if(!nombre.isEmpty()){
                          nombreText.setText(nombre);
                          nombreText.setEnabled(false);
                      }

                      if(!email.isEmpty()){
                          emailText.setText(email);
                          emailText.setEnabled(false);
                      }

                      if(!direccion.isEmpty()){
                          direccionText.setText(direccion);
                          direccionText.setEnabled(false);
                      }

                      if(!telefono.isEmpty()){
                          telefonoText.setText(telefono);
                          telefonoText.setEnabled(false);
                      }

                      Log.e("Usuario", task.getResult().getDocuments().get(0).getData().toString());

                    } else {
                        if(!firebaseAuth.getCurrentUser().getDisplayName().isEmpty()){
                           nombreText.setText(firebaseAuth.getCurrentUser().getDisplayName());
                        }
                        emailText.setText(firebaseAuth.getCurrentUser().getEmail());
                        emailText.setEnabled(false);
                        Log.e("Usuario", "Logueado con google");
                    }
                }
            }
        });
    }

    private ArrayList<String> getHours(){
        int horaAperturaMañana = 9;
        int minutosAperturaMañana = 0;
        int horaAperturaTarde = 19;
        int minutosAperturaTarde = 0;

        int horaCierreMañana = 14;
        int minutosCierreMañana = 30;
        int horaCierreTarde = 22;
        int minutosCierraTarde = 0;

        ArrayList<String> horasRecogida = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        Calendar calendarioAhora = Calendar.getInstance();
        calendarioAhora.add(Calendar.HOUR_OF_DAY, 1);
        // String hour = format.format(calendar.getTime());

        Calendar calendarioCierreMañana = Calendar.getInstance();
        calendarioCierreMañana.set(Calendar.HOUR_OF_DAY, horaCierreMañana);
        calendarioCierreMañana.set(Calendar.MINUTE, minutosCierreMañana);

        Calendar calendarioCierreTarde = Calendar.getInstance();
        calendarioCierreTarde.set(Calendar.HOUR_OF_DAY, horaCierreTarde);
        calendarioCierreTarde.set(Calendar.MINUTE, minutosCierraTarde);

        Calendar calendarioAperturaTarde = Calendar.getInstance();
        calendarioAperturaTarde.set(Calendar.HOUR_OF_DAY, horaAperturaTarde);
        calendarioAperturaTarde.set(Calendar.MINUTE, minutosAperturaTarde);


        Calendar calendarioAperturaMañana = Calendar.getInstance();
        calendarioAperturaMañana.set(Calendar.HOUR_OF_DAY, horaAperturaMañana);
        calendarioAperturaMañana.set(Calendar.MINUTE, minutosAperturaMañana);


        if(calendarioAhora.before(calendarioAperturaMañana) || calendarioAhora.after(calendarioCierreTarde)){
            calendarioAhora = calendarioAperturaMañana;

            while(calendarioAhora.before(calendarioCierreMañana)){
                horasRecogida.add(format.format(calendarioAhora.getTime()));
                calendarioAhora.add(Calendar.MINUTE, 30);
            }

        } else if(calendarioAhora.before(calendarioCierreMañana)){
            calendarioAhora.add(Calendar.MINUTE, 30);

            if(calendarioAhora.after(calendarioCierreMañana))
                calendarioAhora = calendarioAperturaTarde;

            while(calendarioAhora.before(calendarioCierreMañana)){

                if(calendarioAhora.after(calendarioCierreMañana))
                    break;

                horasRecogida.add(format.format(calendarioAhora.getTime()));
                calendarioAhora.add(Calendar.MINUTE, 30);
            }

        } else if(calendarioAhora.before(calendarioAperturaTarde)){
            calendarioAhora = calendarioAperturaTarde;

            while(calendarioAhora.before(calendarioCierreTarde)){
                horasRecogida.add(format.format(calendarioAhora.getTime()));
                calendarioAhora.add(Calendar.MINUTE, 30);
            }
        } else {
            calendarioAhora.add(Calendar.MINUTE, 30);

            if(calendarioAhora.after(calendarioCierreTarde))
                calendarioAhora = calendarioAperturaMañana;

            while(calendarioAhora.before(calendarioCierreTarde)){

                if(calendarioAhora.after(calendarioCierreTarde))
                    break;

                horasRecogida.add(format.format(calendarioAhora.getTime()));
                calendarioAhora.add(Calendar.MINUTE, 30);
            }
        }

        return horasRecogida;
    }

    private void doOrder(){
        if(nombreText.getText().toString().isEmpty() || emailText.getText().toString().isEmpty() || direccionText.getText().toString().isEmpty() || telefonoText.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(), "No puede haber campos vacíos", Toast.LENGTH_SHORT).show();
        } else {
            String hora = "";

            try{

               hora =  spinner.getSelectedItem().toString();

            }catch (Exception e){
                Log.e("Error", e.getMessage());
            }

            if(hora.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Debes seleccionar una hora de recogida", Toast.LENGTH_SHORT).show();
            } else {

                preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                String json = preferences.getString("productos", "");
                final List<Producto> productos = ProductosCompra.fromJSON(json).getListaProductos();

                Timestamp fechaPedido = new Timestamp(new Date());

                Pedido pedido;

               if(observacionesText.getText().toString().isEmpty()){
                   pedido = new Pedido(fechaPedido,nombreText.getText().toString().trim(), direccionText.getText().toString().trim(), telefonoText.getText().toString().trim(), hora);
               }else{
                   pedido = new Pedido(fechaPedido,nombreText.getText().toString().trim(), direccionText.getText().toString().trim(), telefonoText.getText().toString().trim(), observacionesText.getText().toString().trim(), hora);
               }

               firebaseFirestore.collection("Pedidos").add(pedido).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                   @Override
                   public void onSuccess(DocumentReference documentReference) {
                       for(int i = 0; i < productos.size(); i++){
                           final PedidoProducto pp = new PedidoProducto(documentReference.getId(), productos.get(i).getId());
                           firebaseFirestore.collection("PedidoProductos").add(pp).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                               @Override
                               public void onSuccess(DocumentReference documentReference) {

                                   SharedPreferences.Editor editor = preferences.edit();
                                   editor.clear();
                                   editor.commit();

                                   makeDialog(pp.getIdPedido());
                               }
                           });
                       }
                   }
               });
            }
        }
    }

    private void makeDialog(String idPedido){
        final EditText nombrePedidoText = new EditText(this);
        nombrePedidoText.setHint("Nombre para guardar el pedido");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Realizado pedido")
                .setMessage("¿Quieres guardar este pedido como favorito?")
                .setView(nombrePedidoText)
                .setCancelable(false)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Modificar pedido fav = true si se ha introducido un nombre
                        //Intent a Main Activity
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Intent a Main Activity
                    }
                })
                .create();
        dialog.show();
    }
}
