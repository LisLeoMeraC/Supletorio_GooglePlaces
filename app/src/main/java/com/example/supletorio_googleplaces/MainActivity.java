package com.example.supletorio_googleplaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btnbuscar;
    Spinner spTipos;
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;
    GoogleMap mapa;
    double log=-0;
    double lat=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnbuscar = findViewById(R.id.btnBuscar);
        spTipos = findViewById(R.id.sp_tipo);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        String[] TiposLugares = {"hotel", "hospital", "restaurant","bank","school"};

        //Este es el arreglo que contiene los diferentes tipos de lugares que se van a mostrar en el Spinner
        String[] nombresLugares = {"Hoteles", "Hospitales", "Restaurantes","Bancos","Escuelas"};

        spTipos.setAdapter(new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item, nombresLugares));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            getCurrentLocation();
        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this
                    ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);


            }

        btnbuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i= spTipos.getSelectedItemPosition();
                String url="https://maps.googleapis.com/maps/api/place/nearbysearch/json"+
                        "?location="+-1.0280412464443522+","+-79.4688596890154+"&radius=5000"+"&types="+TiposLugares[i]+
                        "&sensor=true" +"&key=" + getResources().getString(R.string.google_map_key);

                new PlaceTask().execute(url);
            }
        });

    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if(location!=null) {

                    lat=location.getLatitude();
                    log=location.getLongitude();
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            mapa=googleMap;
                            mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(-1.0280412464443522, -79.4688596890154),10

                            ));
                        }
                    });


                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==44){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){

                getCurrentLocation();

            }
    }
}

    private class PlaceTask extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... strings) {
            String data=null;
            try {
              data  = BajarUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            new ParserTask().execute(s);

        }
    }

    private String BajarUrl(String string) throws IOException {
        URL url=new URL(string);
        HttpURLConnection httpURLConnection= (HttpURLConnection)url.openConnection();

        httpURLConnection.connect();
        InputStream inputStream= httpURLConnection.getInputStream();
        BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder= new StringBuilder();
        String linea="";

        while((linea=bufferedReader.readLine())!=null){
            stringBuilder.append(linea);
        }
        String data=stringBuilder.toString();
        bufferedReader.close();
        return data;
    }

    private class ParserTask extends  AsyncTask<String,Integer, List<HashMap<String,String >>>{
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {

            Json jsonParser= new Json();
            List<HashMap<String,String>> mapList= null;
            JSONObject jsonObject= null;
            try{
               jsonObject= new JSONObject(strings[0]);
               mapList=jsonParser.resultParse(jsonObject);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
            return mapList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            mapa.clear();
            for(int i=0;i<hashMaps.size();i++){
                HashMap<String,String> hashMapsList= hashMaps.get(i);
                double lat=Double.parseDouble(hashMapsList.get("lat"));
                double lng=Double.parseDouble(hashMapsList.get("lng"));
                String name= hashMapsList.get("name");

                LatLng latLng= new LatLng(lat,lng);

                //Mostrar Marcadores

                MarkerOptions markerOptions= new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(name);
                //mapa.addMarker(markerOptions);

                //Cambio de icono en los marcadores dependiendo de las opción que se elija en el Spinner

                if(spTipos.getSelectedItem().equals("Restaurantes")) {
                    mapa.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_restaurants)).anchor(0.0f, 1.0f));
                }
                else if(spTipos.getSelectedItem().equals("Hoteles")){
                    mapa.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_hotel)).anchor(0.0f, 1.0f));
                }
                else if(spTipos.getSelectedItem().equals("Hospitales")){
                    mapa.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_hospital)).anchor(0.0f, 1.0f));
                }
                else if(spTipos.getSelectedItem().equals("Bancos")){
                    mapa.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_banco)).anchor(0.0f, 1.0f));
                }
                else{
                    mapa.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_school_round)).anchor(0.0f, 1.0f));
                }



            }
        }
    }
}