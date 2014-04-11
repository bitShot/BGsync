package com.example.bibliotecauclm.objetos;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseLibros extends SQLiteOpenHelper{
	/**
	 * NOTA: investigar si se pueden sacar dos libros con el mismo titulo en la biblioteca,
	 * 		 de no poderse, la clave primaria es el título, de otra forma, nos las tenemos
	 * 		 que arreglar para saber cuando un libro esta desactualizado o no.
	 * 
	 * IDEA: podemos usar como clave primaria la posición que el libro ocupa en la tabla de la biblioteca,
	 * 		 aunque, si se pueden sacar dos iguales, debemos ver si se ordenan igual en dicha tabla o no (con el 
	 * 		 identificador podemos saberlo)
	 * 
	 * P.D. Lo idóneo sería poder hacerlo con los identificadores, el problema es que esos identificadores no los
	 * 		tenemos hasta que el libro se puede renovar (un día antes de la fecha de expiración). Normalmente ese campo
	 * 		va estar a null.
	 * 	
	 */
	private String sentenciaCreacion ="CREATE TABLE Libros (biblioteca TEXT, " +
															"sucursal TEXT, " +
															"titulo TEXT, " +
															"fecha TEXT," +
															"renovar INTEGER," +
															"identificador TEXT)" ;
	
	/**
	 * Obtiene la base de datos, tan solo llámalo y usa los métodos.
	 * No intentar crear bases de datos de otra forma.
	 * @param ctx
	 * @return 
	 */
	public static BaseLibros obtenerDB(Context ctx){
		return new BaseLibros(ctx,"Libros",null,1);
	}	
	
	/**
	 * Constructor privado de la base de datos. Está privado, para evitar que
	 * se puedan crear bases de datos de otra forma.
	 * 
	 * @param ctx
	 * @param nombre
	 * @param factoria
	 * @param version
	 */
	private BaseLibros(Context ctx, String nombre,
					CursorFactory factoria, int version){
		
		super(ctx,nombre,factoria,version);
		
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(sentenciaCreacion);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
		
		db.execSQL("DROP TABLE IF EXISTS Libros");
		db.execSQL(sentenciaCreacion);
		
	}
	/**
	 * Este método inserta en la base de datos, todos los libros que le pases
	 * en la lista. ¡CUIDADO! si el libro ya está, se insertará igual. Para actualizar,
	 * utiliza el método actualizarBase().
	 * 
	 * @param libros
	 */
	
	public void insertar(ArrayList<Libro> libros){
		SQLiteDatabase db = this.getWritableDatabase();
		
		for(Libro libro: libros){
			
			ContentValues reg = new ContentValues();
			reg.put("biblioteca", libro.getBiblioteca());
			reg.put("sucursal", libro.getSucursal());
			reg.put("titulo", libro.getTitulo());
			reg.put("fecha", libro.getFechaPlana());
			
			if(libro.getRenovar()){
				reg.put("renovar", 1);
			}else{
				reg.put("renovar", 0);
			}
			
			reg.put("identificador", libro.getIdentificador());
			
			db.insert("Libros",null , reg);
		}
		
		db.close();
		
		
	}
	/**
	 * Este método obtiene una lista completa de todos los libros existentes en la
	 * base de datos. La lista será vacía si no hay nada.
	 * 
	 * @return
	 */
	public ArrayList<Libro> obtenerLibros(){
		ArrayList<Libro> libros = new ArrayList<Libro>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(" SELECT * FROM Libros ", null);
		
		
		if(c.moveToFirst()){
			
			do{
				Libro libro = new Libro(c.getString(0),c.getString(1),
						  				c.getString(2),c.getString(3),
						  				c.getString(5));
				
				if(c.getInt(4) == 1){
					
					libro.setRenovar(true);
					
				}else{
					
					libro.setRenovar(false);
				}
				libros.add(libro);
			}while(c.moveToNext());			
			
		}
		db.close();
		return libros;
		
		
	}
	/**
	 * Este método elimina todos los registros de la base de datos
	 * y los inserta de nuevo. Versión temporal. 
	 * 
	 * @param libros
	 */
	public void actualizarBase(ArrayList<Libro> libros){
		SQLiteDatabase db = this.getReadableDatabase();
		db.delete("Libros", null, null);
		db.close();
		this.insertar(libros);
		
		
	}
	
	

}
