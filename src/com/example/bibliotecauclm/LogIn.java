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

import com.example.bibliotecauclm.net.Actualizadores;
import com.example.bibliotecauclm.net.AsyncTaskInterface;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class LogIn extends Activity {

	private static String usuarioTemp = "";
	private static String contrasenaTemp = "";
	private static Boolean recordar = false;
	private SharedPreferences sharedPref;
	
	
	public static String getUsuarioTemp(){
		return usuarioTemp;
	}
	
	public static Boolean getRecordar(){
		return recordar;
	}
	
	public static String getContrasenaTemp(){
		return contrasenaTemp;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_in);
		
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		if(sharedPref.getBoolean("recordar", false)){
			openActivityLibros();
			finish();
		}
		
		Button boton_entrar = (Button) findViewById(R.id.boton_entrar);
		final TextView usuario =(TextView) findViewById(R.id.editText_usuario);
		final TextView contrasena =(TextView) findViewById(R.id.editText_contrasena);
		final CheckBox recordar = (CheckBox) findViewById(R.id.checkBox_recordar);
		
		
		recordar.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				LogIn.recordar = arg1;
			}
			
		});
		
		boton_entrar.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
		        usuarioTemp = usuario.getText().toString();
		        contrasenaTemp = contrasena.getText().toString();
		       
		        AsyncTaskInterface tarea = Actualizadores.crearComprobadorUsuario(LogIn.this);
		        
				tarea.ejecutar();
				
			}
			
			
			
			
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	public void openActivityLibros(){
		
		Intent activityLibros = new Intent(this,ActivityLibros.class);
		startActivity(activityLibros);
		
	}
	
	@Override
	public void onResume()
	{
	    super.onResume();
	    sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

	    
	}
	
}
