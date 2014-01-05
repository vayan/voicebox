package com.vaya.voicebox;

import java.io.File;  
import java.io.IOException;
import java.util.ArrayList;  
import java.util.List;  

import com.vaya.voicebox.ShakeInterface.OnShakeListener;
  
import android.app.ActionBar;
import android.app.AlertDialog;  
import android.app.ListActivity;  
import android.content.Context;
import android.content.DialogInterface;  
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.media.MediaPlayer;
import android.os.Bundle;  
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;  
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;  
import android.widget.ListView;  
import android.widget.TextView;  
import android.widget.Toast;

public class FileListActivity extends ListActivity {
	
	private String TAG = "FileListActivity";
	ShakeInterface shake;
	MySensorEventListener mySensorEventListener = new MySensorEventListener();
	Context context;
	
	
	/** Called when the activity is first created. */  
    private List<String> items = null;
    private List<String> paths = null;
    private String rootPath = "/sdcard/VoiceBox/";  
    //private TextView tv;  
    
    MediaPlayer mediaPlayer;
    
    
	private class MySensorEventListener implements OnShakeListener{

		@Override
		public void onShake() {
			// TODO Auto-generated method stub
			Log.d(TAG, getfile());
		}
		
	};
	
	public void updateTheme() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		if (sharedPref.getBoolean("use_dark_theme", false)) setTheme(android.R.style.Theme_Holo);
		else setTheme(android.R.style.Theme_Holo_Light);
	}
  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        
        updateTheme();
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
        
        
        setContentView(R.layout.activity_filelist);  
        //tv = (TextView) this.findViewById(R.id.TextView);  
        this.getFileDir(rootPath); 
        
        getListView().setOnItemLongClickListener(new OnItemLongClickListener(){  
            @Override  
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,  
                    int arg2, long arg3) {  
                // TODO Auto-generated method stub  
                // When clicked, show a toast with the TextView text  
            	ListLongClick((ListView)arg0, arg1, arg2, arg3);
                return false;  
            }
        });  
        
        shake_phone(this);
    }  
  
    public void getFileDir(String filePath) {  
        try{  
            //this.tv.setText("current:"+filePath);
            items = new ArrayList<String>();  
            paths = new ArrayList<String>();  
            File f = new File(filePath);  
            File[] files = f.listFiles();  
             
            if (!filePath.equals(rootPath)) {  
                items.add("uproot");  
                paths.add(rootPath);  
                items.add("up level");  
                paths.add(f.getParent());  
            }  
            
            if(files != null){  
                int count = files.length;
                for (int i = 0; i < count; i++) {  
                    File file = files[i];  
                    items.add(file.getName());  
                    paths.add(file.getPath());  
                }  
            }  
  
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  
                    android.R.layout.simple_list_item_1, items);  
            this.setListAdapter(adapter);  
        }catch(Exception ex){  
            ex.printStackTrace();  
        }  
  
    }  
    
 
    protected void ListLongClick(ListView l, View v, int position, long id) {  
    	String[] menu={"Play","Rename","Delete","Share"};
    	OnClickListener listener = new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which){
    			System.out.println(""+ which);
    		}
    	};
    	
    	new AlertDialog.Builder(FileListActivity.this).setTitle("Operater").setItems(menu,listener).show();
    }
    
    
    @Override  
    protected void onListItemClick(ListView l, View v, int position, long id) {  
        super.onListItemClick(l, v, position, id);  
        String path = paths.get(position);  
        File file = new File(path);  
        //
        if(file.isDirectory()){  
            this.getFileDir(path);  
        }else{  
        	mediaPlayer = new MediaPlayer();
        	try {
        		  mediaPlayer.setDataSource(path);
        		} catch (IllegalArgumentException e) {
        		  e.printStackTrace();
        		} catch (IllegalStateException e) {
        		  e.printStackTrace();
        		} catch (IOException e) {
        		  e.printStackTrace();
        		}
        		//Prepare mediaplayer
        		try {
        		  mediaPlayer.prepare();
        		} catch (IllegalStateException e) {
        		  e.printStackTrace();
        		} catch (IOException e) {
        		 e.printStackTrace();
        		}
        		//start mediaPlayer
        		mediaPlayer.start();   
        		
        		 new AlertDialog.Builder(this).setTitle("Playing").setMessage(file.getName()+" is playing...").setPositiveButton("Stop", new DialogInterface.OnClickListener(){  
  
        			                public void onClick(DialogInterface dialog, int which) {
        			                	mediaPlayer.stop();
        			                	mediaPlayer.release();
        			                                          
        			                }  
        			                  
        			            }).show(); 
        }  
    }
    
	public void shake_phone(Context context){
	
		this.context = context;
		shake = new ShakeInterface(context);
		shake.registerOnShakeListener(mySensorEventListener);
		shake.start();
	}
	
	 
	 public String getfile(){
		 File file = new File("/sdcard/VoiceBox/");
		 int len = file.list().length-1;
		 String lastfile = "";
		 long time = 0;
		 for(;len >= 0;len--){
			 if(time < file.listFiles()[len].lastModified()){
				 time = file.listFiles()[len].lastModified();
				 lastfile = file.list()[len];
			 }
		 }
		 
		 return "/sdcard/VoiceBox/"+lastfile;
	 }
}
