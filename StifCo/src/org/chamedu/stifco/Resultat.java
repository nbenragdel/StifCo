package org.chamedu.stifco;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Resultat extends Activity {
	
	
	JSONObject jsonResponse1;
	String json;
	ArrayList<String> items1 = new ArrayList<String>();
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resultat);
		
		Intent resultat = getIntent();
		// On suppose que tu as mis un String dans l'Intent via le putExtra()
		String json = resultat.getStringExtra("json");
		
		//finish();
		if ( json.equals("recherche_vide")) {
			items1.add("La recherche ne donne aucun résultat...");}
			else{
		
		
		try {
			jsonResponse1 = new JSONObject(json);
			JSONArray jsonArray = jsonResponse1.getJSONArray("propositions");
			
			for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObj = jsonArray.getJSONObject(i);
			String ville = jsonObj.getString("ville");
	        String horaire= jsonObj.getString("id");
			items1.add(horaire+"  |  "+ville);	
			}
			
			 
		} catch (JSONException e) {
			e.printStackTrace();
		}
			}
		ListView list;
        list = (ListView)findViewById(R.id.listView1);
        list.setAdapter(new ArrayAdapter<String>(Resultat.this,android.R.layout.simple_list_item_1,items1));
			
	}
	
	
		
}
