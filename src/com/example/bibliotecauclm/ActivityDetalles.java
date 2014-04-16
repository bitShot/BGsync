package com.example.bibliotecauclm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ActivityDetalles extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detalles);
		
		Intent intent = getIntent();
		TextView biblioteca = (TextView) findViewById(R.id.textView_biblioteca);
		biblioteca.setText(intent.getStringExtra("biblioteca"));
		TextView sucursal = (TextView) findViewById(R.id.textView_sucursal);
		sucursal.setText(intent.getStringExtra("sucursal"));
		TextView titulo = (TextView) findViewById(R.id.textView_titulo);
		titulo.setText(intent.getStringExtra("titulo"));
		TextView fecha = (TextView) findViewById(R.id.textView_fecha);
		fecha.setText(intent.getStringExtra("fecha"));
	}

	
}
