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


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.example.bibliotecauclm.ActivityLibros;
import com.example.bibliotecauclm.LogIn;
import com.example.bibliotecauclm.R;
import com.example.bibliotecauclm.misc.Utiles;
import com.example.bibliotecauclm.objetos.BaseLibros;
import com.example.bibliotecauclm.objetos.Libro;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;

import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

class ActualizadorListaLibros extends  AsyncTask<String ,Integer , List<Libro> > implements AsyncTaskInterface{

	private Context contexto;
	private NotificationManager mNotificationManager;
	private SharedPreferences sharedPref;
	
	private static boolean debug = false;
	
	private PowerManager.WakeLock w1;
	
	protected ActualizadorListaLibros(ActivityLibros nombre_activity){
		this.contexto = nombre_activity;
		sharedPref = PreferenceManager.getDefaultSharedPreferences(contexto.getApplicationContext());
		w1 = null;
	}
	
	protected ActualizadorListaLibros(Context servi, PowerManager.WakeLock wakeLock){
		
		this.contexto =  servi;
		sharedPref = PreferenceManager.getDefaultSharedPreferences(contexto.getApplicationContext());
		w1 = wakeLock;
	}
	
	protected void onPreExecute(){
		if(w1!=null){
			w1.acquire();
		}
		NotificationCompat.Builder mBuilder =
			    new NotificationCompat.Builder(contexto)
			        .setSmallIcon(R.drawable.ic_stat_logo)
			        .setLargeIcon((((BitmapDrawable)contexto.getResources()
			            .getDrawable(R.drawable.ic_launcher)).getBitmap()))
			        .setContentTitle("Mi Biblioteca")
			        .setContentText("Comprobando biblioteca...")
			        .setContentInfo("4")
			        .setTicker("Comprobando biblioteca...")
			        .setOngoing(true);
		
		
		 mNotificationManager =
			    (NotificationManager) contexto.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
			 
			mNotificationManager.notify(1, mBuilder.build());
		
	}
	
	
	@Override
	/**
	 * @param usuario
	 * @param contrasena
	 */
	protected List<Libro> doInBackground(String... params) {

		
		List<Libro> resultado = new ArrayList<Libro>();
		try {
		
			
			
			resultado = obtenerLibros(params[0].toString(),params[1].toString());
			

			return resultado;
		
		} catch (Exception e) {
			
			e.printStackTrace();
			return null;
		}
	
	}


	@Override
	protected void onPostExecute(List<Libro> res){
		
		BaseLibros db = BaseLibros.obtenerDB(this.contexto);
		if(res != null){
			if (this.contexto instanceof ActivityLibros ){
				 db.actualizarBase((ArrayList<Libro>)res);
				 ((ActivityLibros) this.contexto).anadirLibros((ArrayList<Libro>)res);
				 
			}else{
				lanzarNotificacionRenovar(res);
				db.actualizarBase((ArrayList<Libro>)res);
			}
		}else{
			Toast.makeText(this.contexto.getApplicationContext()
					, "No se han podido recuperar sus libros", 
					Toast.LENGTH_LONG).show();
			/* Aunque no se puedan recuperar los libros, nosotros tomamos los que hay
			 * en la base de datos
			 */
			if (this.contexto instanceof ActivityLibros )
				((ActivityLibros) this.contexto).anadirLibros(db.obtenerLibros());
			else
				lanzarNotificacionRenovar(db.obtenerLibros());
		}
		if(w1 != null)
			w1.release();
		Actualizadores.liberarActualizadorListaLibros();
		this.mNotificationManager.cancel(1);
	}
	
	public void ejecutar(){
		if(sharedPref.getBoolean("recordar", false))
			this.execute(sharedPref.getString("usuario", ""),sharedPref.getString("contrasena", ""));
		else
			this.execute(LogIn.getUsuarioTemp(),LogIn.getContrasenaTemp());
	}

	
	private List<Libro> obtenerLibros(String usuario, String contrasena) throws Exception{
		
		if(!Utiles.isOnline(contexto))return null;
		
		Document doc;
		String res;
		List<Libro> resultado = new ArrayList<Libro>();
	
		res = Utiles.obtenerLinkConexion(usuario,contrasena);
		
		if(res == null) return null;
		
		/*Para pruebas con gaseosa */
		if(!debug){
			doc =  Jsoup.connect("https://catalogobiblioteca.uclm.es" + res +"?ACC=210").timeout(12000).get();
		}else{
			
			File input = new File("/sdcard/html.htm");
			doc = Jsoup.parse(input,"UTF-8");
		}
		
		
		
		List<Element> Libros = doc.select("table").get(0).select("tr") ;
		Libros.remove(0);
		
		
		for(Element ele : Libros){
			List<Element> filas = ele.select("td");
			
				Libro libro = 
					new Libro(filas.get(0).text(),
							filas.get(1).text(),
							filas.get(2).text(),
							filas.get(3).text());
			
			try{
			
			libro.setIdentificador(filas.get(4).select("input").first().attr("name").toString());
			libro.setRenovar(true);
			
			}catch(NullPointerException ex){
					libro.setRenovar(false);
					libro.setIdentificador(null);
			}
			
			resultado.add(libro);
		
		}
		
		return resultado;
		
	}

	
	
	/**
	 * Este m�todo es el encargado de revisar la lista de libros recuperados
	 * por si alguno est� pasado de fecha, le falta un d�a o debe ser renovado hoy.
	 * 
	 * @param res
	 */
	private void lanzarNotificacionRenovar(List<Libro> res){
		int renovables = 0,
			expiranHoy = 0,
			pasadosDeFecha= 0;
		
	
		
		for(Libro l : res){
		
			int estado = Utiles.estadoLibro(l);
			if(estado == -1);
			else if(estado == 0){
				pasadosDeFecha++;
			}
			else if(estado == 1){
				expiranHoy++;
			}
			else if(estado == 2){
				renovables++;
			}
		
		
		}
		
		if((renovables != 0) || (expiranHoy != 0) || (pasadosDeFecha != 0)){
			Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			
			NotificationCompat.Builder mBuilder =
				    new NotificationCompat.Builder(contexto)
				        .setSmallIcon(R.drawable.ic_stat_logo)
				        .setLargeIcon((((BitmapDrawable)contexto.getResources()
				            .getDrawable(R.drawable.ic_launcher)).getBitmap()))
				        .setContentTitle("Estado de los libros...")
				        .setContentText(pasadosDeFecha + " Pasados " + expiranHoy +" Hoy " + renovables +" Mañana")
				        .setTicker("Estado de los libros...")
				        .setSound(notificationSound)
				        .setAutoCancel(true);  
			
			NotificationCompat.InboxStyle inboxStyle =
			        new NotificationCompat.InboxStyle();
			
			String[] events = new String[3];
			events[0] = pasadosDeFecha + " libros pasados";
			events[1] = expiranHoy + " a devolver hoy";
			events[2] = renovables + " a devolver mañana";
			inboxStyle.setBigContentTitle("Estado de los libros...");
			for (int i=0; i < events.length; i++) {

			    inboxStyle.addLine(events[i]);
			}
			mBuilder.setStyle(inboxStyle);
			
			Intent inten = new Intent(contexto,ActivityLibros.class);
			
			PendingIntent cont = PendingIntent.getActivity(contexto, 0, inten, 0);
			
			mBuilder.setContentIntent(cont);
			
			
			this.mNotificationManager.notify(2,mBuilder.build());
			
		}
		
	}
}





