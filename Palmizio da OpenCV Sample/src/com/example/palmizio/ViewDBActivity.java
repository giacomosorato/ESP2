package com.example.palmizio;

import java.util.ArrayList;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ViewDBActivity extends ActionBarActivity{
	
	private TextView tv;
	
	@Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.view_db_layout);
        
//        tv = (TextView) findViewById(R.id.textProva);
//        
//        tv.setText("ciao");
       
        DBHelper mydb = new DBHelper(this);
		ArrayList array_list = mydb.getAllImagesStrings();

		ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, array_list);

		ListView obj = (ListView) findViewById(R.id.DBListView1);
		obj.setAdapter(arrayAdapter);
		
//codice per aprire ogni elemento con id		
//		obj.setOnItemClickListener(
//				new OnItemClickListener() {
//					@Override
//					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//						// TODO Auto-generated method stub
//						int id_To_Search = arg2 + 1;
//		
//						Bundle dataBundle = new Bundle();
//						dataBundle.putInt("id", id_To_Search);
//		
//						Intent intent = new Intent(getApplicationContext(), com.example.pit.DBExample.DisplayContact.class);
//		
//						intent.putExtras(dataBundle);
//						startActivity(intent);
//					}
//				}
//		);
        
	}
}
