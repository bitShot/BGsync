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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.example.bibliotecauclm.misc.Utiles;
import com.example.bibliotecauclm.net.Actualizadores;
import com.example.bibliotecauclm.net.AsyncTaskInterface;
import com.example.bibliotecauclm.objetos.BaseLibros;
import com.example.bibliotecauclm.objetos.Libro;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityLibros extends Activity{

	private boolean cambiarMenu = false;
	private TableLayout tableLayoutLibros;
	
	//IDs de los elementos del action bar
	private final int ACCION_CANCELAR = 1;
	private final int ACCION_RENOVAR = 2;
	private final int ACCION_REFRESCAR = 3;
	private final int ACCION_PREFERENCIAS = 4;
	private final int ACCION_ACERCADE = 5;
	
	//Preferencias
	private SharedPreferences sharedPref;
	
	HashMap<TableRow,Libro> libros = new HashMap<TableRow,Libro>();
	ArrayList<Libro> librosRenovar = new ArrayList<Libro>();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_libros);
	
		this.getActionBar().setDisplayShowTitleEnabled(false);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		tableLayoutLibros = (TableLayout) findViewById(R.id.tableLayout_libros);
		//AsyncTaskInterface actualizador = Actualizadores.crearActualizadorActivityLibros(this);
		//actualizador.ejecutar();
		BaseLibros db = BaseLibros.obtenerDB(this);
		anadirLibros(db.obtenerLibros());
		
		if(sharedPref.getBoolean(Utiles.PREF_NOTIF, true) && sharedPref.getBoolean("recordar", false)){
			Utiles.iniciarAlarmaServicio(getApplicationContext());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		
		MenuItem renovar = menu.add(Menu.NONE,
                ACCION_RENOVAR,
                Menu.NONE,
                "Renovar");
		renovar.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		if(cambiarMenu){
			MenuItem cancelarSeleccion = menu.add(Menu.NONE,
												  ACCION_CANCELAR,
												  Menu.NONE,
												  "Cancelar selección");
			cancelarSeleccion.setIcon(R.drawable.ic_menu_close_clear_cancel);
			cancelarSeleccion.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			cambiarMenu = false;
		}
		
		MenuItem refrescar = menu.add(Menu.NONE,
                ACCION_REFRESCAR,
                Menu.NONE,
                "Refrescar libros");
		refrescar.setIcon(R.drawable.refrescar);
		refrescar.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		MenuItem opciones = menu.add(Menu.NONE,
                ACCION_PREFERENCIAS,
                Menu.NONE,
                "Preferencias");
		opciones.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		
		MenuItem acercaDe = menu.add(Menu.NONE,
                ACCION_ACERCADE,
                Menu.NONE,
                "Acerca de BGsync");
		acercaDe.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		AsyncTaskInterface actualizador;
		switch (item.getItemId()) {
        case ACCION_CANCELAR:
        	limpiarSeleccion();
        	invalidateOptionsMenu();
            return true;
        case ACCION_RENOVAR:
        	actualizador = Actualizadores.crearActualizadorActivityLibros(this);
        	if(actualizador != null){
	        	renovar();
	        	limpiarSeleccion();
	        	invalidateOptionsMenu();
	        	tableLayoutLibros.removeAllViews();
	        	actualizador.ejecutar();
        	}
        	return true;
        case ACCION_REFRESCAR:
        	actualizador = Actualizadores.crearActualizadorActivityLibros(this);
        	if(actualizador != null){
	        	limpiarSeleccion();
	        	invalidateOptionsMenu();
	        	tableLayoutLibros.removeAllViews();
	        	actualizador.ejecutar();
        	}
        	return true;
        case ACCION_PREFERENCIAS:
        	//Abrimos el Fragment con las preferencias
        	openPreferencias();
        	return true;
        case ACCION_ACERCADE:
        	openAcercaDe();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
		}
		
	}
	/**
	 * Escribe en la tabla principal (en el activity_libros) la lista de libros 
	 * pasada como argumento.
	 * 
	 * @param librosArray
	 */
	public void anadirLibros(ArrayList<Libro> librosArray){

		Collections.sort(librosArray);
		for (Libro libro : librosArray){
		TableRow inflateRow = (TableRow) View.inflate(this, R.layout.fila_libro, null);
		libros.put(inflateRow,libro);
	    tableLayoutLibros.addView(inflateRow);
	    
	    ((TextView)inflateRow.findViewById(R.id.textView_nombre)).setText(Utiles.recortar34(libro.getTitulo()));
	    pintarCuadradoDia(inflateRow);
	    
	    inflateRow.setOnClickListener(new OnClickListener(){
	
			@Override
			public void onClick(View v) {
				Fragment displayFrag = (Fragment) getFragmentManager()
                        .findFragmentById(R.id.fragment_detalles);
				if (displayFrag == null) {
					
					Intent intent = new Intent(getApplicationContext(), ActivityDetalles.class);
					intent.putExtra("biblioteca",libros.get(v).getBiblioteca());
					intent.putExtra("sucursal",libros.get(v).getSucursal());
					intent.putExtra("titulo",libros.get(v).getTitulo());
					intent.putExtra("fecha",libros.get(v).getFechaPlana());
					startActivity(intent);
					
				} else {
					
					TextView titulo = (TextView) findViewById(R.id.textView_titulo);
					titulo.setText(libros.get(v).getTitulo());
					TextView biblioteca = (TextView) findViewById(R.id.textView_biblioteca);
					biblioteca.setText(libros.get(v).getBiblioteca());
					TextView sucursal = (TextView) findViewById(R.id.textView_sucursal);
					sucursal.setText(libros.get(v).getSucursal());
					TextView fecha = (TextView) findViewById(R.id.textView_fecha);
					fecha.setText(Utiles.fechaString(libros.get(v).getFecha()));
					
				}
			}
	    });
	  
	    
	    
		}
	}
	
	private void limpiarSeleccion(){
		librosRenovar.clear();
		for(int i = 0; i<tableLayoutLibros.getChildCount();i++){
			
			tableLayoutLibros.getChildAt(i)			
						.findViewById(R.id.imageView_okey)
						.setVisibility(View.GONE);
			pintarCuadradoDia((TableRow)tableLayoutLibros.getChildAt(i));
		}
		
	}
	/**
	 * Dibuja el día de devolución y añade un OnClickListener para seleccionar la fila
	 * @param inflateRow
	 */
	private void pintarCuadradoDia(final TableRow inflateRow){
		
		Libro libro = libros.get(inflateRow);
		TextView textViewDia = (TextView) inflateRow.findViewById(R.id.textView_dia);
    	textViewDia.setText(((Integer)libro.getFecha().monthDay).toString());
    	
    	LinearLayout layoutDia = (LinearLayout) inflateRow.findViewById(R.id.layout_dia);
    	switch (Utiles.estadoLibro(libro)){
    		case 0:
    			layoutDia.setBackgroundResource(R.drawable.fondo_dia_rojo);
    			break;
    		
    		case 1:
    			layoutDia.setBackgroundResource(R.drawable.fondo_dia_amarillo);
    			break;
    			
    		case 2:
    			layoutDia.setBackgroundResource(R.drawable.fondo_dia_amarillo);
    			break;
    			
    		default:
    			layoutDia.setBackgroundResource(R.drawable.fondo_dia);
    			break;
    	}
    	layoutDia.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Libro libro = libros.get(inflateRow);
				if(!librosRenovar.contains(libro) && libro.getRenovar()){
					v.findViewById(R.id.imageView_okey).setVisibility(View.VISIBLE); //Mostramos tick
					//v.setBackgroundResource(R.drawable.btn_okey);
					librosRenovar.add(libro);
					cambiarMenu = true;
					invalidateOptionsMenu();
				} else if(!libro.getRenovar()){
					Toast.makeText(getApplicationContext(), "No puedes renovar este libro", Toast.LENGTH_SHORT).show();
				}
			}
    		
    		
    		
    		
    	});
    	TextView mes = (TextView) inflateRow.findViewById(R.id.textView_mes);
    	mes.setText(Utiles.mesNumToNombre(libro.getFecha().month));
	}
	
	private void renovar(){
		
		AsyncTaskInterface renovador = null;
		
		if(librosRenovar.isEmpty()){
			ArrayList<Libro> todosLibros = new ArrayList<Libro>(libros.values());
			renovador = Actualizadores.crearRenovadorLibros(todosLibros,this);
			if(renovador != null)
				renovador.ejecutar();
			
		}
		else{
			//FIXME: Cuidao con el casteo del clone
			ArrayList<Libro> clone = (ArrayList<Libro>) librosRenovar.clone();
			renovador = Actualizadores.crearRenovadorLibros(clone,this);
			if(renovador != null)
				renovador.ejecutar();
			
		}
		
	}
	
	@Override
	public void onBackPressed() {
	   this.finish();
	}
	
	public void openPreferencias(){
		
		Intent preferencias = new Intent(this,ActivityPreferencias.class);
		startActivityForResult(preferencias,0);
	}

	private void openAcercaDe(){
		
		Intent acercaDe = new Intent(this,ActivityAcercaDe.class);
		startActivity(acercaDe);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(resultCode == 0)
			finish();
		else {}
	}
	
}
