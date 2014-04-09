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

package com.example.bibliotecauclm.misc;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.format.Time;
import com.example.bibliotecauclm.objetos.Libro;

public class Utiles {

	//KEYs preferencias
	public static final String PREF_NOTIF = "pref_notif";
	public static final String PREF_INTER = "pref_intervalo";
	
	
	/**
	 * Devuelve un libro con el nombre especificado de la lista pasada como
	 * parámetro.
	 * 
	 * @param nombre
	 * @param libros
	 * @return
	 */
	public static Libro getLibro(String nombre, ArrayList<Libro> libros) {

		for (Libro lib : libros) {
			if (lib.getTitulo().equals(nombre))
				return lib;

		}

		return null;

	}

	/**
	 * Devuelve el día de un objeto tipo Date
	 * 
	 * @param fecha
	 * @return
	 */
	public static Integer diaDate(Date fecha) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(fecha);
		return (Integer) cal.get(Calendar.DAY_OF_MONTH);

	}

	/**
	 * Devuelve el dia siguiente a la fecha pasada como argumento
	 * 
	 * @param actual
	 * @return Date (dia siguiente)
	 */
	public static Date diaSiguiente(Date actual) {

		long milisDia = 86400000;

		return new Date(actual.getTime() + milisDia + 10);

	}

	public static String recortar34(String texto) {

		if (texto.length() <= 34)
			return texto;
		else
			return texto.substring(0, 30) + "...";

	}

	public static String fechaString(Time fecha) {

		return fecha.monthDay + "/" + fecha.month + "/" + fecha.year;
	}

	/**
	 * Las sesiones en la biblioteca son temporales, con esto podremos obtener una sesi�n.
	 * Concatenado con el url b�sico y el ACC obtenemos una conexi�n correcta.
	 * @param usuario
	 * @param contrasena
	 * @return
	 * @throws IOException
	 */
	public static String obtenerLinkConexion(String usuario, String contrasena) throws IOException{

		Document doc = null;
		Element el = null;
		String res = null;

		
			int intentos = 9;
			do {

				doc = Jsoup.connect(
						"https://catalogobiblioteca.uclm.es/cgi-bin/abnetopac?ACC=210&leid="
								+ usuario.trim() + "&lepass="
								+ contrasena.toString().trim()
								+ "&Enviar=Enviar").timeout(12000).get();
				if (doc != null) {
					el = doc.select("meta[http-equiv=Refresh]").first();
					
					if(el != null){
						res = el.toString().substring(43, el.toString().length() - 12);
						break;
					}else{
						
						intentos--;
				
					}
					
				} else {
					
					intentos--;
				}
				
			} while (intentos > 0);

		

		return res;

	}
	
	public static String mesNumToNombre(int numeroMes){
		
		switch(numeroMes){
		case 0:
			return "Enero";

		case 1:
			return "Feb.";

		case 2:
			return "Marzo";

		case 3:
			return "Abril";

		case 4:
			return "Mayo";

		case 5:
			return "Junio";

		case 6:
			return "Julio";

		case 7:
			return "Agosto";

		case 8:
			return "Sept.";

		case 9:
			return "Oct.";

		case 10:
			return "Nov.";

		case 11:
			return "Dic.";

		default:
			return "ErrMes";

		}

	}

	/**
	 * 
	 * @param l
	 * @return 0 --> libro pasado de fecha 1 --> el libro se renueva HOY 2 -->
	 *         el libro se renueva MAÑANA -1--> ERROR
	 */
	public static int estadoLibro(Libro l) {

		if (l.estaPasado())
			return 0;
		if (l.esHoy())
			return 1;
		if (l.esManana())
			return 2;
		else
			return -1;

	}
	/**
	 * Comprueba si tenemos conexión a internet, o el dispositivo está conectando
	 * a la red.
	 * @param Context
	 * @return
	 */
	public static boolean isOnline(Context Context) {
		ConnectivityManager cm = (ConnectivityManager) Context
				.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}
	
	public static void iniciarAlarmaServicio(Context context){
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		Intent myAlarm = new Intent(context, AlarmaServicio.class);
		PendingIntent recurringAlarm = PendingIntent.getBroadcast(context, 0, myAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
		
		AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Calendar updateTime = Calendar.getInstance();
		updateTime.setTimeInMillis(System.currentTimeMillis());
		
		
		if ((sharedPref.getString(PREF_INTER, "0").equals("1"))){
				alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),AlarmManager.INTERVAL_FIFTEEN_MINUTES , recurringAlarm);
		} else if (sharedPref.getString(PREF_INTER, "0").equals("2")){
				alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),AlarmManager.INTERVAL_HALF_HOUR , recurringAlarm);
		} else if (sharedPref.getString(PREF_INTER, "0").equals("3")){
				alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),AlarmManager.INTERVAL_HOUR , recurringAlarm);
		} else if (sharedPref.getString(PREF_INTER, "0").equals("0") || sharedPref.getString(PREF_INTER, "0").equals("4")){
				alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),AlarmManager.INTERVAL_HALF_DAY , recurringAlarm);
		} else if (sharedPref.getString(PREF_INTER, "0").equals("5")){
				alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),AlarmManager.INTERVAL_DAY , recurringAlarm);
		}
				
		
	}
	
public static void cancelarAlarmaServicio(Context context){
		
		
		Intent myAlarm = new Intent(context, AlarmaServicio.class);
		PendingIntent recurringAlarm = PendingIntent.getBroadcast(context, 0, myAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarms.cancel(recurringAlarm);
		
	}
	
}
