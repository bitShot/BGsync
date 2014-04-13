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

package com.example.bibliotecauclm.objetos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.text.format.Time;
public class Libro implements Comparable<Libro>{
	
	
	private String biblioteca;
	private String sucursal;
	private String titulo;
	private String fecha;
	private boolean renovar = false;
	private String identificador = null;
	
	public Libro (String biblioteca,String sucursal,String titulo,String fecha){
		
		this.biblioteca = biblioteca;
		this.sucursal = sucursal;
		this.titulo = titulo;
		this.fecha = fecha;
	}
	
	public Libro (String biblioteca,String sucursal,String titulo,String fecha,String id){
		this.biblioteca = biblioteca;
		this.sucursal = sucursal;
		this.titulo = titulo;
		this.fecha = fecha;
		this.identificador = id;
	
		
	}
	
	public String getBiblioteca(){
		
		return biblioteca;
	} 
	
	public String getIdentificador(){
		return identificador;
	}
	
	public String getSucursal(){
		
		return sucursal;
	}
	
	public String getTitulo(){
		return titulo;
	}
	
	public String getFechaPlana(){
		return fecha;
	}
	
	public Time getFecha(){
		
		Time fecha = new Time();
		int monthDay = Integer.parseInt(this.fecha.substring(0, 2));
		int month = Integer.parseInt(this.fecha.substring(3, 5));
		int year = Integer.parseInt(this.fecha.substring(6));
		fecha.set(monthDay, month-1, year);
		
		return fecha;
	}
	
	public void setBibiloteca(String biblioteca){
		this.biblioteca = biblioteca;
	}
	
	public void setSucursal(String sucursal){
		this.sucursal = sucursal;
	}
	
	public void setTitulo(String titulo){
		this.titulo = titulo;
	}
	
	public void setFecha (String fecha){
		this.fecha = fecha;
	}
	
	public void setRenovar (boolean renovar){
		this.renovar = renovar;
	}
	
	public boolean getRenovar (){
		return this.renovar;
	}
	
	public void setIdentificador(String string) {
		this.identificador  = string;
		
	}
	
	@Override
	public String toString(){
		
		return "titulo: " + titulo + " fecha: "+ fecha;
	}
	
	public static List <Libro> actualizarListaLibros(final String usuario,final String contrasena ){
		
		FutureTask<List <Libro>> trd = new FutureTask<List <Libro>> (new Callable<List <Libro>>(){
			public List <Libro> call(){
				
				List<Libro> resultado = new ArrayList<Libro>();
				Document doc;
				String res;
			
			 try {
				
				 doc = Jsoup.connect("https://catalogobiblioteca.uclm.es/cgi-bin/abnetopac?ACC=210&leid="+
						 			 usuario.trim() + "&lepass=" + contrasena.trim() + 
						 			 "&Enviar=Enviar").get();
			
			
				 Element el = doc.select("meta[http-equiv=Refresh]").first();
				 
				
				res = el.toString().substring(43, el.toString().length()-4);
				
				doc =  Jsoup.connect("https://catalogobiblioteca.uclm.es" + res).get();
								
				List<Element> Libros = doc.select("table").get(0).select("tr") ;
				Libros.remove(0);
				
				
				for(Element ele : Libros){
					List<Element> filas = ele.select("td");
					
					Libro libro = 
							new Libro(filas.get(0).text(),
									filas.get(1).text(),
									filas.get(2).text(),
									filas.get(3).text());
					resultado.add(libro);
				}
			
				return resultado;
			 
			 
			 } catch (IOException e) {
				 
				e.printStackTrace();
			 }
			 
				
				return resultado;
			}	
		});
		
		 ExecutorService es = Executors.newSingleThreadExecutor ();

	        es.submit (trd);
	        List <Libro> Libros = null;
	        try {

	        	Libros = (List <Libro>) trd.get ();
	        	}

	        	catch (Exception e) {

	        		e.printStackTrace();

	        	}
	        return Libros;
		
		
	}

	public boolean estaPasado(){
		Time now = new Time();
		now.setToNow();
		Time nowAbsolute = new Time();
		nowAbsolute.set(now.monthDay, now.month, now.year);
		return this.getFecha().before(nowAbsolute);
	}
	
	public boolean esHoy(){
		
		Time now = new Time();
		now.setToNow();
		Time nowAbsolute = new Time();
		nowAbsolute.set(now.monthDay, now.month, now.year);
		return Time.compare(this.getFecha(),nowAbsolute) == 0;
	}
	
	public boolean esManana(){
		
		Time now = new Time();
		now.setToNow();
		Time nowAbsolute = new Time();
		nowAbsolute.set(now.monthDay, now.month, now.year);
		
		Time libroManana = this.getFecha();
		nowAbsolute.set(nowAbsolute.toMillis(false)+86400000);
		return Time.compare(libroManana,nowAbsolute) == 0;
	}

	@Override
	public int compareTo(Libro another) {
		Libro otro = another;
		
		if(this.getFecha().before(otro.getFecha()))
			return -1;
		else
			return 1;
	}
}
