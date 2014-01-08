package com.vaya.voicebox;

import java.io.File;  
import java.io.IOException;
import java.util.ArrayList;  
import java.util.List;  

import com.vaya.voicebox.ShakeInterface.OnShakeListener;

import android.app.ActionBar;
import android.app.AlertDialog;  
import android.app.ListActivity;  
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;  
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;  
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;  
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;  
import android.widget.EditText;
import android.widget.ListView;  
import android.widget.Toast;

public class FileListActivity extends ListActivity {

	private String TAG = "FileListActivity";
	public static final String activity_title = "Recording History";
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
			share(getfile());
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
		actionBar.setTitle(activity_title);


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
		String[] menu={"Playing","Rename","Delete","Share","As Ringtone"};
		final String path = paths.get(position);
		final File file = new File(path);
		OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which){
				System.out.println(""+ which);
				if(which == 4){
					setMyRingtone(path);
				}else if(which == 2){
					file.delete();
					getFileDir(rootPath);
				}else if(which == 1){
					renameDialog(path);
					//getFileDir(rootPath);
				}else if(which == 3){
					share(path);
				}else if(which == 0){
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
					new AlertDialog.Builder(FileListActivity.this).setTitle("Playing").setMessage(file.getName()+" is playing...").setPositiveButton("Stop", new DialogInterface.OnClickListener(){  

						public void onClick(DialogInterface dialog, int which) {
							mediaPlayer.stop();
							mediaPlayer.release();	        			                                          
						}  

					}).show();
				}
			}
		};

		new AlertDialog.Builder(FileListActivity.this).setTitle("Operater").setItems(menu,listener).show();
		//this.recreate();
		//this.finish();
		//startActivity(new Intent(FileListActivity.this, FileListActivity.class));
	}


	private void share(String path){
		setTransFile(path);
	}

	private void setTransFile(String path){
		/*
		 * Create a list of URIs, get a File,
		 * and set its permissions
		 */
		//private Uri[] mFileUris = new Uri[10];
		// Get a URI for the File and add it to the list of URIs
		Uri fileUri = Uri.fromFile(new File(path));
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
		shareIntent.setType("*/*");
		startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
	}

	private void renameDialog(String path) {

		final EditText inputServer = new EditText(this);
		final String pathOld = path;
		inputServer.setFocusable(true);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Rename").setIcon(
				android.R.drawable.ic_dialog_alert).setView(inputServer).setNegativeButton(
						"Cancel", null);
		builder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String inputName = inputServer.getText().toString();
				inputName = "/sdcard/VoiceBox/"+ inputName;
				File oleFile = new File(pathOld); //要重命名的文件或文件夹
				File newFile = new File(inputName);  //重命名为zhidian1
				oleFile.renameTo(newFile);  //执行重命名
				getFileDir(rootPath);
			}
		});
		builder.show();
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
	
	
	public void setMyRingtone(String path)  
    {   
      File sdfile = new File(path);  
      ContentValues values = new ContentValues();  
      values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());  
      values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());  
      values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");    
      values.put(MediaStore.Audio.Media.IS_RINGTONE, true);  
      values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);  
      values.put(MediaStore.Audio.Media.IS_ALARM, false);  
      values.put(MediaStore.Audio.Media.IS_MUSIC, false);  
       
      Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());  
      Uri newUri = this.getContentResolver().insert(uri, values); 
      System.out.println("start setRingtone");
      RingtoneManager.setActualDefaultRingtoneUri(FileListActivity.this, RingtoneManager.TYPE_RINGTONE, newUri);  
      Toast.makeText( getApplicationContext (),"Setting Success!！", Toast.LENGTH_SHORT ).show();  
      System.out.println("setMyRingtone()");  
    }
	
	
	
}
