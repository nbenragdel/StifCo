package org.chamedu.stifco;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import network.OnResultListener;
import network.RestClient;

import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import repositories.AccountRepository;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	Button proposer, rechercher;
	TextView numEMAIL;
	
	AccountRepository accRepo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		proposer = (Button)findViewById(R.id.proposer);
		rechercher = (Button)findViewById(R.id.rechercher);
		numEMAIL = (TextView)findViewById(R.id.tvEmail);
		
		proposer.setOnClickListener(this);
		rechercher.setOnClickListener(this);

		accRepo = new AccountRepository(getApplicationContext());

		if (accRepo.isAccountConfigured()) {
			String info = accRepo.getEmail();
			numEMAIL.setText("Email : "+info);
		} else {
			// Lance la fenetre pour la recherche d'un compte
			LayoutInflater factory = LayoutInflater.from(this);
			final View CompteView = factory.inflate(R.layout.alertdialog_compte, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
			final EditText email = (EditText)CompteView.findViewById(R.id.etEmail);
			email.setText("DefaultValue", TextView.BufferType.EDITABLE);
			
			alertDialogBuilder.setView(CompteView);
			alertDialogBuilder.setIconAttribute(android.R.attr.alertDialogIcon);
			alertDialogBuilder.setTitle(R.string.ad_compte);

			alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					List<NameValuePair> nvp = new ArrayList<NameValuePair>();
					
					String leEmail = email.getText().toString().trim();
					nvp.add(new BasicNameValuePair("email",""+leEmail));
					
					try {				
						RestClient.doPost("/verification.php", nvp, new OnResultListener() {					
							@Override
							public void onResult(String json) {
								doOnTrueResult( json );
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
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					finish();

					Builder adCancel = new AlertDialog.Builder(MainActivity.this);
					adCancel.setTitle("Information");
					adCancel.setMessage("Arrêt demandé par l'utilisateur...");
					adCancel.setIcon(R.drawable.cool);
					adCancel.setPositiveButton("Ok",null);		           
					adCancel.show();
					finish();
				}
			})
			.create();

			AlertDialog ad = alertDialogBuilder.create();
			ad.show();
		}
	}

	private void doOnTrueResult( String info ) {
		if ( info.equals("pas_ok" )) {
			String message = "Désolé, mais votre adresse email n'est pas enregistrée sur le site !";
			Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
			finish();
		} else {
			// Dans ce cas le script PHP a retourné l'adresse email dans la variable info
			accRepo.setAccount(info);
			numEMAIL.setText("Email : "+info);
		}
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)	{
		switch (item.getItemId()) {
		case R.id.menu_suppcompte:
			accRepo.unsetAccount();
			Toast.makeText(getApplicationContext(), "Compte supprimé", Toast.LENGTH_LONG).show();
			finish();
			break;
		
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}	

	@Override
	public void onClick(View v) {
		if ( v == proposer ) { 
			Intent iProposer = new Intent(this,Proposer.class); 
			this.startActivityForResult( iProposer,10 ); 
		}

		if ( v == rechercher ) { 
			Intent iRecherche = new Intent(this,Rechercher.class); 
			this.startActivityForResult(iRecherche,10 ); 
		}	
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Log.i( "Retour", ""+resultCode );
		//this.finish();

		//-> ElÃ©ments non soutenus par Android pour un arrÃªt "propre" ;-))
		//android.os.Process.killProcess( android.os.Process.myPid() );
		//System.exit( 0 );
		
		//-> Vraiment la fin de l'application...
		//getParent().finish();
	}


}
