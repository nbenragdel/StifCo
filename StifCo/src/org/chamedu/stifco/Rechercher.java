package org.chamedu.stifco;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import network.OnResultListener;
import network.RestClient;

import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
 
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
 
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
 
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class Rechercher extends Activity implements ViewSwitcher.ViewFactory,View.OnClickListener {

	private Button recherche;
 
	private Spinner spinner1;
	JSONObject jsonResponse1;
 
	 Intent resultat;


	// Varaibles pour la lecture du flux JSON
	private String jsonString;


	AutoCompleteTextView tvGareAuto;
	ArrayList<String> items = new ArrayList<String>();
	ArrayList<String> items1 ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recherche);
		addItemsOnSpinner2();
 
 

		// Traitement du textView en autocomplétion à  partir de la source Json
		jsonString = lireJSON();

		try {
			jsonResponse1 = new JSONObject(jsonString);
			// Création du tableau général à partir d'un JSONObject
			JSONArray jsonArray = jsonResponse1.getJSONArray("gares");

			// Pour chaque élément du tableau
			for (int i = 0; i < jsonArray.length(); i++) {

				// Création d'un tableau élément à  partir d'un JSONObject
				JSONObject jsonObj = jsonArray.getJSONObject(i);

				// Récupération à partir d'un JSONObject nommé
				JSONObject fields  = jsonObj.getJSONObject("fields");

				// Récupération de l'item qui nous intéresse
				String nom = fields.getString("nom_de_la_gare");
				
				// Ajout dans l'ArrayList
				items.add(nom);	
				
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, items);
			tvGareAuto = (AutoCompleteTextView)findViewById(R.id.actvGare);
			tvGareAuto.setAdapter(adapter);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Listener sur le bouton d'envoi
		recherche = (Button)findViewById(R.id.btChercher);
		recherche.setOnClickListener(this);
	}	


	public View makeView() {
		TextView t = new TextView(this);
		t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		t.setTextSize(36);
		return t;
	}
	 public void addItemsOnSpinner2() {

			spinner1 = (Spinner) findViewById(R.id.spinner1);
			List<String> list = new ArrayList<String>();
			list.add("janvier");
			list.add("fevrier");
			list.add("mars");
			list.add("avril");
			list.add("mai");
			list.add("juin");
			list.add("juillet");
			list.add("août");
			list.add("septembre");
			list.add("août");
			list.add("octobre");
			list.add("novembre");
			list.add(" décembre");
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner1.setAdapter(dataAdapter);
		  }
	 


	public String lireJSON() {
		InputStream is = getResources().openRawResource(R.raw.gares);
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];
		try {
			Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return writer.toString();
	}

	@Override
	public void onClick(View v) {
		if ( v == recherche ) {
    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    nameValuePairs.add(new BasicNameValuePair("gare",""+tvGareAuto.getText()));	
    nameValuePairs.add(new BasicNameValuePair("mois",""+spinner1.getSelectedItem()));
    
    try {				
		RestClient.doPost("/recherche.php", nameValuePairs, new OnResultListener() {					
			@Override
			public void onResult(String json) {
				items1 = new ArrayList<String>();
				if ( json.equals("recherche_vide")) {
				    
					 resultat = new Intent(Rechercher.this,Resultat.class);
					 resultat.putExtra("json", json);
					 startActivity(new Intent(resultat));
				} else {
					 
					 resultat = new Intent(Rechercher.this,Resultat.class);
					 resultat.putExtra("json", json);
					 startActivity(new Intent(resultat));
					
				
				}					
			}
		});
	} catch (URISyntaxException e) {
		e.printStackTrace();
	} catch (HttpException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}

			
			   
		}
		
	}
}
