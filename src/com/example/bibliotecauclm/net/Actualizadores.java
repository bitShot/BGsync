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

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.PowerManager;

import com.example.bibliotecauclm.ActivityLibros;
import com.example.bibliotecauclm.LogIn;
import com.example.bibliotecauclm.objetos.Libro;


public class Actualizadores {

	private static boolean actualizadorListaLibros = false;
	private static boolean renovadorLibro = false;
	
	private static PowerManager.WakeLock w1;
	private Actualizadores() { }
	
	public static AsyncTaskInterface crearActualizadorActivityLibros(ActivityLibros activity){
		if(!actualizadorListaLibros){
			actualizadorListaLibros = true;
			return new ActualizadorListaLibros(activity);
		}
		else 
			return null;
	}
	
	public static ActualizadorListaLibros crearActualizadorServicioLibros(Context servicio){
			PowerManager po;
			po = (PowerManager) servicio.getSystemService(Context.POWER_SERVICE) ;
			w1 = po.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "lock del servicio");
			return new ActualizadorListaLibros(servicio,w1);
	}
	
	public static AsyncTaskInterface crearComprobadorUsuario(LogIn activity){
		ProgressDialog	pDialog = new ProgressDialog(activity);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Procesando...");
        
        return new ComprobadorUsuario(pDialog,activity);
	}
	
	public static AsyncTaskInterface crearRenovadorLibros(ArrayList<Libro> libros,Context ctx){
		if(!renovadorLibro){
			renovadorLibro = true;
			return new RenovadorLibros(libros,ctx);
		} else{
			return null;
		}
	}
	
	public static void liberarActualizadorListaLibros(){
		actualizadorListaLibros = false;
	}
	
	public static void liberarRenovadorLibro(){
		
		renovadorLibro = false;
	}
}
