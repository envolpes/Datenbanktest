package com.sportfest.grundschul.datenbanktest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SprungEingabe extends Menue {
    //Deklaration der Textboxen
    TextView Name, Klasse, Nummer, UKlasse, Änderung;

    //Deklaration Variablen Sprung
    EditText Satz1, Satz2, Satz3;
    Button Springen;
    RequestQueue requestQueue;

    //URL Location
    String insertURL = "http://91.67.234.134//json_insert.php";


    //String JSON_Text;//nicht nötig


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sprungeingabe);

        //Toolbar einfügen (Methode aus der Klasse Menue")
        ToolbarEinfügen();

        //Infotextboxen und Sprungdaten der Activity befüllen
        SpringerDatenVorbefüllen();

        //Refernzierung des Buttons und festlegen der Funktionalität
        Springen = (Button) findViewById(R.id.springen);
        Springen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Initialisierung, falls noch nicht vorbefüllt

                Satz1 = (EditText) findViewById(R.id.Satz1);
                String satz1 = Satz1.getText().toString();

                Satz2 = (EditText) findViewById(R.id.Satz2);
                String satz2 = Satz2.getText().toString();

                Satz3 = (EditText) findViewById(R.id.Satz3);
                String satz3 = Satz3.getText().toString();

                String modus = Änderung.getText().toString();

                //GET-String zur Übertragung der Daten
                insertURL = insertURL + "?Springer=" + Nummer.getText().toString() + "&Satz1=" + satz1 + "&Satz2=" + satz2 + "&Satz3=" + satz3 + "&Neu=" + modus;

                //Aufrufen von SpringerDatenSpeichern
                SpringerDatenSpeichern();

                //Sprung in ListView
                finish();
            }
        });
    }

    class BackgroundTask extends AsyncTask<Void, Void, String> {

        //Dekleration Variablen
        String json_url;
        String Nummer;
        String JSON_STRING;

        public BackgroundTask(String Nummer) {

            //Übergabe von Parameter Nummer
            this.Nummer = Nummer;

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... voids) {
            json_url = "http://91.67.234.134/sprungabfrage.php?Springer=" + Nummer;

            try {

                //AUFBAU HTTP Connecton und Builden des Strings
                URL url = new URL(json_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while ((JSON_STRING = bufferedReader.readLine()) != null) {

                    stringBuilder.append(JSON_STRING + "\n");
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }


        @Override
        protected void onPostExecute(String result) {

            //result ist ein JSONString dieser wird aufgelöst, sodass aus dem JSON Object die einzelnen Sprünge ausgelesen werden können
            try {
                //Auflösen des Strings in das finale JSONObject
                JSONObject Sprünge = new JSONObject(result).getJSONArray("server_response").getJSONObject(0);

                //Referenzieren der Sprungfelder und vorbefüllen mit existierenden Werten
                Satz1 = (EditText) findViewById(R.id.Satz1);
                Satz1.setText(Sprünge.getString("Satz1"));

                Satz2 = (EditText) findViewById(R.id.Satz2);
                Satz2.setText(Sprünge.getString("Satz2"));

                Satz3 = (EditText) findViewById(R.id.Satz3);
                Satz3.setText(Sprünge.getString("Satz3"));


                //Textview Änderung bei Vorbefüllung

                if (Satz1.getText().toString() != null) {
                    Änderung.setText("Änderungsmodus");
                }
            } catch (JSONException e) {
                e.printStackTrace();

            }

        }
    }

    private void SpringerDatenVorbefüllen() {
        //PersonenDaten der Ausgewählten Person aus der vorherigen Activity abholen
        Bundle bundle = getIntent().getExtras();

        //Eigenes Objekt mit den Personendaten erschaffen
        Gson gson = new Gson();
        String jsonInString = bundle.getString("Schueler");
        PersonenDaten user = gson.fromJson(jsonInString, PersonenDaten.class);

        //Referenzierung der Info-Textboxen und mit Info-PersonenDaten aus dem Personenobjekt vorbefüllen
        Name = (TextView) findViewById(R.id.Name);
        Name.setText(user.getName());

        Klasse = (TextView) findViewById(R.id.klasse);
        Klasse.setText(user.getKlasse());

        Nummer = (TextView) findViewById(R.id.Nummer);
        Nummer.setText(user.getNummer());

        UKlasse = (TextView) findViewById(R.id.unterklasse);
        UKlasse.setText(user.getUnterklasse());

        Änderung = (TextView) findViewById(R.id.Änderung);
        Änderung.setText("Neu");


        //Sprungweiten der Person aus Datenbank holen und die Felder vorbefüllen
        new SprungEingabe.BackgroundTask(user.getNummer()).execute();

    }

    private void SpringerDatenSpeichern() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        //GET Methode, um Weiten zur DB zu schicken
        StringRequest request = new StringRequest(Request.Method.GET, insertURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        // Einfügen des Einfügen in die Warteschlange
        requestQueue.add(request);
        try {
            URL url = new URL(insertURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}