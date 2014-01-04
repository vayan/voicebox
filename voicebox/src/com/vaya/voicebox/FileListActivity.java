package com.vaya.voicebox;

import java.io.File;  
import java.util.ArrayList;  
import java.util.List;  

import com.vaya.voicebox.ShakeInterface.OnShakeListener;
  
import android.app.AlertDialog;  
import android.app.ListActivity;  
import android.content.Context;
import android.content.DialogInterface;  
import android.os.Bundle;  
import android.util.Log;
import android.view.View;  
import android.widget.ArrayAdapter;  
import android.widget.ListView;  
import android.widget.TextView;  

public class FileListActivity extends ListActivity {
	
	
	private String TAG = "FileListActivity";
	ShakeInterface shake;
	MySensorEventListener mySensorEventListener = new MySensorEventListener();
	Context context;
	
	
	/** Called when the activity is first created. */  
    private List<String> items = null;//存放名称  
    private List<String> paths = null;//存放路径  
    private String rootPath = "/sdcard/VoiceBox/";  
    private TextView tv;  
    
    
	private class MySensorEventListener implements OnShakeListener{

		@Override
		public void onShake() {
			// TODO Auto-generated method stub
			Log.d(TAG, getfile());
		}
		
	};
  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_filelist);  
        tv = (TextView) this.findViewById(R.id.TextView);  
        this.getFileDir(rootPath);//获取rootPath目录下的文件.  
        
        shake_phone(this);
    }  
  
    public void getFileDir(String filePath) {  
        try{  
            this.tv.setText("current:"+filePath);// 设置当前所在路径  
            items = new ArrayList<String>();  
            paths = new ArrayList<String>();  
            File f = new File(filePath);  
            File[] files = f.listFiles();// 列出所有文件  
            // 如果不是根目录,则列出返回根目录和上一目录选项  
            if (!filePath.equals(rootPath)) {  
                items.add("返回根目录");  
                paths.add(rootPath);  
                items.add("返回上一层目录");  
                paths.add(f.getParent());  
            }  
            // 将所有文件存入list中  
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
  
    @Override  
    protected void onListItemClick(ListView l, View v, int position, long id) {  
        super.onListItemClick(l, v, position, id);  
        String path = paths.get(position);  
        File file = new File(path);  
        //
        if(file.isDirectory()){  
            this.getFileDir(path);  
        }else{  
            new AlertDialog.Builder(this).setTitle("提示").setMessage(file.getName()+" 是一个文件！").setPositiveButton("OK", new DialogInterface.OnClickListener(){  
  
                public void onClick(DialogInterface dialog, int which) {  
                                          
                }  
                  
            }).show();  
        }  
    }
    
	public void shake_phone(Context context){
		//System.out.println("haha\n");
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
