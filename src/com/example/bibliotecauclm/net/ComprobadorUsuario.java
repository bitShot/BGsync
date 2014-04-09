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

package com.example.bibliotecauclm.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import com.example.bibliotecauclm.LogIn;
import com.example.bibliotecauclm.misc.Utiles;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

class ComprobadorUsuario  extends  AsyncTask<String ,Boolean ,Integer> implements AsyncTaskInterface{

	private ProgressDialog dialogo;
	private LogIn activityLogIn;
	private SharedPreferences sharedPref;
	private Editor editorPref;
	
	protected ComprobadorUsuario(ProgressDialog x, LogIn contexto){
		
		dialogo = x;
		this.activityLogIn = contexto;
	}
	
	protected void onPreExecute(){
		dialogo.show();
		
	}
	@Override
	protected void onPostExecute(Integer res){
		if(res == 1){
			
			//pasar a la siguiente actividad
			activityLogIn.openActivityLibros();
			activityLogIn.finish();
			
		}else if(res == -1){
			Toast.makeText(activityLogIn.getApplicationContext(),
					       "Usuario o contrase�a incorrecto", 
					       Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(activityLogIn.getApplicationContext(),
				       "Error de conexi�n con el servidor", 
				       Toast.LENGTH_LONG).show();
		}
		dialogo.dismiss();
	}
	
	
	@Override
	protected Integer doInBackground(String... arg0) {
		
	
		try {
			String link = Utiles.obtenerLinkConexion(arg0[0], arg0[1]) ;
			if (link == null) return null;
		    Jsoup.connect("https://catalogobiblioteca.uclm.es" + 
			link +"?ACC=210").timeout(12000).get();
		    
		    if(LogIn.getRecordar()){
			    sharedPref = PreferenceManager.getDefaultSharedPreferences(activityLogIn.getApplicationContext());
			    editorPref = sharedPref.edit();
			    editorPref.putString("usuario",arg0[0].toString());
			    editorPref.putString("contrasena",arg0[1].toString());
			    editorPref.putBoolean("recordar", true);
			    editorPref.apply();
			    
			    
		    }
		    
			return 1;
		}catch (HttpStatusException e){
			
			return -1;
		
		} catch (IOException e) {
			
			return 0;

		}
	
	
	}
	
	
	
	public void ejecutar(){
		
		try {
			this.execute(URLEncoder.encode(LogIn.getUsuarioTemp(),"utf-8"), 
				      URLEncoder.encode(LogIn.getContrasenaTemp(),"utf-8"));
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
	}
	
}
