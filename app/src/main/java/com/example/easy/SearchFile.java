package com.example.easy;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;


public class SearchFile extends Activity {

    FileListAdapter filelistadapter;
    /**
     * Called when the activity is first created.
     * @param icicle	Bundle
     */
    @Override
    protected void onCreate(Bundle icicle) {
        final ListView listView;
        final Context c = SearchFile.this;
        super.onCreate(icicle);
        setContentView(R.layout.search_main);

        listView = (ListView) findViewById(R.id.list_files);
        filelistadapter = new FileListAdapter();

        final ProgressDialog progressdialog = ProgressDialog.show(this, "Loading", null, true);
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    Thread.sleep(1500);
                    progressdialog.dismiss();
                }catch(InterruptedException ex){
                    Log.e("Thread", ex.getMessage());
                }
            }
        }).start();

        Search();

        if ( filelistadapter == null ) Log.e("Search file", "error");
        listView.setAdapter(filelistadapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            /**
             * Set a listener on an item override the method from the interface OnItemClickListener
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                TextView tx = (TextView)view.findViewById(R.id.txt_list_absolute_path);
                String s = tx.getText().toString();

                Intent data = new Intent();
                data.putExtra("fichero", s);
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });
    }

    /**
     * onDestroy method
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        finish();
    }

    /**
     * Search files in the sdcard
     * Note: future versions set parameter for a custom search in a directory
     */
    public void Search(){
        try{
            @SuppressLint("SdCardPath")
            File f = new File("/mnt/sdcard/download");
            if ( f.isDirectory() ){
                File[] docfiles = f.listFiles((dir, name) ->
                        name.endsWith(".pdf") || name.endsWith(".txt")
                );

                assert docfiles != null;
                for (File docfile : docfiles) {
                    filelistadapter.add(new FileList(f.getPath(), docfile.getName()));
                }
            }
        }catch(Exception ex){
            Log.e("SearchFile", ex.getMessage());
        }
    }

    /**
     * Cancel de search and return to the previous activity
     * @param view		the view of the button
     */
    public void onCancelSearch(View view){
        onDestroy();
    }
}