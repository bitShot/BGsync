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
import java.util.ArrayList;

import org.jsoup.Jsoup;
import com.example.bibliotecauclm.misc.Utiles;
import com.example.bibliotecauclm.objetos.Libro;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import android.widget.Toast;

class RenovadorLibros extends  AsyncTask<String ,Libro ,Boolean> implements AsyncTaskInterface{
	
	private ArrayList<Libro> libros;
	private SharedPreferences sharedPref;
	private Context ctx;
	protected RenovadorLibros(ArrayList<Libro> libros,Context ctx){
		
		this.libros = libros;
		sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
		this.ctx = ctx;
	}
	
	

	@Override
	protected Boolean doInBackground(String... arg0) {
		
		String link = null;
		/*Se ejecuta la conexión dos veces para que se pueda renovar el libro (no busques explicación),, de otra manera,
		el primer libro no se renueva.*/
		try {
			link = Utiles.obtenerLinkConexion(arg0[0], arg0[1]);
			if(link == null) return false;
			Jsoup.connect("https://catalogobiblioteca.uclm.es" + link + "/NT1?ACC=228&").timeout(12000).get();
		} catch (IOException e1) {
			
			return false;
		}
		
		for(Libro libro : libros){
			
			if(libro.getRenovar() && libro.getIdentificador() != null){
				try {
					Jsoup.connect("https://catalogobiblioteca.uclm.es" + link + "/NT1?ACC=228&" + libro.getIdentificador().trim()
							 + "=1" ).timeout(12000).get();
				} catch (IOException e) {
					return false;
				}
			}
			
		}
		
		return true;
	}



	@Override
	protected void onPostExecute(Boolean result) {
		if(!result) Toast.makeText(this.ctx, "Error actualizar libros", Toast.LENGTH_SHORT).show();
		super.onPostExecute(result);
		Actualizadores.liberarRenovadorLibro();
	}



	@Override
	public void ejecutar() {
		 
		this.execute(sharedPref.getString("usuario",null),sharedPref.getString("contrasena",null));
	}
	
	

}
