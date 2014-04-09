/*
    This file is part of BGsync.

    BGsync is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BGsync is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BGsync.  If not, see <http://www.gnu.org/licenses/>
*/

package com.example.bibliotecauclm;

import com.example.bibliotecauclm.misc.Utiles;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class ActivityPreferencias extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	
	private SharedPreferences sharedPref;
	private Editor editorPref;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        addPreferencesFromResource(R.xml.preferences);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        if(!sharedPref.getBoolean("recordar", false)){
        	getPreferenceScreen().findPreference("pref_notif").setEnabled(false);
        }
        Preference boton_logout = (Preference)findPreference("pref_logout");
        boton_logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference arg0) { 
                        	editorPref = sharedPref.edit();
                        	editorPref.remove("usuario");
                        	editorPref.remove("contrasena");
                        	editorPref.remove(Utiles.PREF_INTER);
                        	editorPref.putBoolean("recordar", false);
                        	editorPref.apply();
                        	
                        	Utiles.cancelarAlarmaServicio(getApplicationContext());
                        	Intent i = new Intent(getApplicationContext(),LogIn.class);
		                    startActivity(i);
		                    setResult(0);
		                    finish();
                            return true;
                        }
                    });
    }
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(Utiles.PREF_NOTIF) && sharedPref.getBoolean("recordar", false)) {
            if(!sharedPref.getBoolean(Utiles.PREF_NOTIF, true)){
            	Utiles.cancelarAlarmaServicio(getApplicationContext());
            } else {
            	Utiles.iniciarAlarmaServicio(getApplicationContext());
            }
            	
        } else if (key.equals(Utiles.PREF_INTER)){
        	
        	ListPreference pref_intervalo = (ListPreference) findPreference(Utiles.PREF_INTER);
        	editorPref = sharedPref.edit();
        	editorPref.putString(Utiles.PREF_INTER, pref_intervalo.getValue());
        	editorPref.apply();
        	Utiles.cancelarAlarmaServicio(getApplicationContext());
        	Utiles.iniciarAlarmaServicio(getApplicationContext());
        }
		
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    // Set up a listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    // Unregister the listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
	
	public void onBackPressed(){
	    setResult(1);
	    super.onBackPressed();
	}
}
