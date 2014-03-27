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
import java.util.Calendar;
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
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class Proposer extends Activity implements ViewSwitcher.ViewFactory,View.OnClickListener {

	Button soumettre;
	EditText ville;
	RadioGroup lieu;
	String leLieu;

	// Variables pour la date et l'heure
	private TextView mDateDisplay;
	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMinute;

	static final int TIME_24_DIALOG_ID = 1;
	static final int DATE_DIALOG_ID = 2;

	// Variables pour le nombre de places
	private TextSwitcher mSwitcher;
	private int mCounter = 1;
	private Button plusButton, moinsButton;

	// Varaibles pour la lecture du flux Json
	private String jsonString;
	JSONObject jsonResponse;
	JSONArray arrayJson;
	AutoCompleteTextView tvGareAuto;
	ArrayList<String> items = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.proposer);

		lieu = (RadioGroup)findViewById(R.id.rgLieu);
		ville = (EditText)findViewById(R.id.etVille);

		// Traitement du changement de la date et de l'heure
		mDateDisplay = (TextView) findViewById(R.id.tvLaDate);

		setDialogOnClickListener(R.id.btChangeDate, DATE_DIALOG_ID);
		setDialogOnClickListener(R.id.btChangeHeure, TIME_24_DIALOG_ID);

		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);

		updateDisplay();

		// Traitement du changement du nombre de places
		mSwitcher = (TextSwitcher)findViewById(R.id.tsPlaces);
		mSwitcher.setFactory(this);

		Animation in = AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in);
		Animation out = AnimationUtils.loadAnimation(this,
				android.R.anim.fade_out);
		mSwitcher.setInAnimation(in);
		mSwitcher.setOutAnimation(out);

		plusButton = (Button)findViewById(R.id.btPlus);
		plusButton.setOnClickListener(this);
		moinsButton = (Button)findViewById(R.id.btMoins);
		moinsButton.setOnClickListener(this);

		updateCounter();

		// Traitement du textView en autocomplétion à  partir de la source Json
		jsonString = lireJSON();

		try {
			jsonResponse = new JSONObject(jsonString);
			// Création du tableau général à partir d'un JSONObject
			JSONArray jsonArray = jsonResponse.getJSONArray("gares");

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
		soumettre = (Button)findViewById(R.id.btOffre);
		soumettre.setOnClickListener(this);
	}	

	private void setDialogOnClickListener(int buttonId, final int dialogId) {
		Button b = (Button)findViewById(buttonId);
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(dialogId);
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case TIME_24_DIALOG_ID:
			return new TimePickerDialog(this,
					mTimeSetListener, mHour, mMinute, id == TIME_24_DIALOG_ID);
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this,
					mDateSetListener,
					mYear, mMonth, mDay);
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case TIME_24_DIALOG_ID:
			((TimePickerDialog) dialog).updateTime(mHour, mMinute);
			break;
		case DATE_DIALOG_ID:
			((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
			break;
		}
	}    

	private void updateDisplay() {
		mDateDisplay.setText(
				new StringBuilder()
				// Month is 0 based so add 1
				.append(mMonth + 1).append("-")
				.append(mDay).append("-")
				.append(mYear).append(" ")
				.append(pad(mHour)).append(":")
				.append(pad(mMinute)));
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener =
			new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDisplay();
		}
	};

	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;
			updateDisplay();
		}
	};

	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	public void onClick(View v) {
		if ( v == plusButton ) { 
			if (mCounter<3) {
				mCounter++;
			} else {
				Toast.makeText(Proposer.this, "Désolé, la limite maximale est fixée à  trois personnes !", Toast.LENGTH_SHORT).show();
			}

			updateCounter();
		} 

		if ( v == moinsButton ) { 
			if (mCounter>1) {
				mCounter--;
			} else {
				Toast.makeText(Proposer.this, "Désolé, la limite minimale est fixée à  une personne !", Toast.LENGTH_SHORT).show();
			}

			updateCounter();
		} 

		if ( v == soumettre ) {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			nameValuePairs.add(new BasicNameValuePair("date",""+mDateDisplay.getText()));
			nameValuePairs.add(new BasicNameValuePair("ville",""+ville.getText()));

			switch (lieu.getCheckedRadioButtonId()) {
			case R.id.rbPoste :
				leLieu = "poste";
				break;
			case R.id.rbMairie :
				leLieu = "mairie";
				break;	
			case R.id.rbEglise :
				leLieu = "eglise";
				break;	
			}

			nameValuePairs.add(new BasicNameValuePair("lieu",""+leLieu));
			nameValuePairs.add(new BasicNameValuePair("places",""+mCounter));
			nameValuePairs.add(new BasicNameValuePair("gare",""+tvGareAuto.getText()));

			try {				
				RestClient.doPost("/soumission.php", nameValuePairs, new OnResultListener() {					
					@Override
					public void onResult(String json) {
						if (json.equals("insertion_ok")) {
							Toast.makeText(Proposer.this, "Votre proposition a été enregistrée, merci.", Toast.LENGTH_LONG).show();
							finish();
						} else {
							Toast.makeText(Proposer.this, json, Toast.LENGTH_LONG).show();
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

	private void updateCounter() {
		mSwitcher.setText(String.valueOf(mCounter));
	}

	public View makeView() {
		TextView t = new TextView(this);
		t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		t.setTextSize(36);
		return t;
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
}
