package repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

// Classe intermédiaire le compte de l'utilisateur et l'enregistrement Android
public class AccountRepository extends Repository {

	// Constructeur
	public AccountRepository(Context context) {
		super(context);
	}

	// Enregistre le compte dans les SharedPreferences
	public void setAccount(String email) {
		SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(Repository.context);
		Editor prefsEditor = appSharedPrefs.edit();

		prefsEditor.putString("EMAIL",email);
		prefsEditor.commit();
	}

	// Supprime le compte
	public void unsetAccount() {
		SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(Repository.context);
		Editor prefsEditor = appSharedPrefs.edit();
		
		prefsEditor.remove("EMAIL");
		prefsEditor.commit();
	}

	// Indique si le compte est configuré ou non
	public boolean isAccountConfigured() {
		AccountRepository accRepo = new AccountRepository(Repository.context);
		String email = accRepo.getEmail();

		if (email.equals("")) {
			return false;
		} else {
			return true;
		}
	}

	// Récupère l'adresse email du compte de l'utilisateur
	public String getEmail()	{
		SharedPreferences app = PreferenceManager.getDefaultSharedPreferences(Repository.context);
		return app.getString("EMAIL", "");
	}
}
