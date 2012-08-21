package btcat.btcub;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class BtcatActivity extends Activity {
    /** Called when the activity is first created. */
	private static final UUID HeadSet_UUID = UUID.fromString("0000111E-0000-1000-8000-00805F9B34FB"); 
    private static final UUID    OBEX_UUID = UUID.fromString("00001105-0000-1000-8000-00805F9B34FB");
    private static final UUID    HID_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String prdName="8XX";
    private BluetoothAdapter mBluetoothAdapter = null;  
    private int devNum;
	
	private final String SD_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
	private MediaRecorder mediaRecorder=null;
	public String WK_File= SD_PATH+"/1ktone.wav";
	int playState=0;
			
			
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final Button rec=(Button)findViewById(R.id.rec);
        final Button btcat=(Button)findViewById(R.id.btcats);
        
        BtConnect();
        
        btcat.setOnClickListener(new View.OnClickListener(){			
			public void onClick(View v) {
				BtConnect();
				
			}
        });  
        
        
        rec.setOnClickListener(new View.OnClickListener(){			
			public void onClick(View v) {
				if(mediaRecorder==null)
				{
					String str=WK_File;
					rec.setText("Stop");
					mediaRecorder=new MediaRecorder(); 
					mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);				
					mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
					mediaRecorder.setOutputFile(WK_File);
					try {
		    			mediaRecorder.prepare();
	                    mediaRecorder.start();
	                }
	                catch (IOException e) {
	                    Log.e("Famile Time", e.getMessage(), e);
	                }	
				}else
				{
					rec.setText("Rec");
				    mediaRecorder.stop();
				    mediaRecorder.release();
				    mediaRecorder=null;
				}
			}
        });  
    }
    public void BtDiscover()
    { 
    	if (mBluetoothAdapter.isDiscovering()) {
        	mBluetoothAdapter.cancelDiscovery();
        }
    	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
    	filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);  
        mBluetoothAdapter.startDiscovery(); 
    }
    public void BtConnect()
    {
        mBluetoothAdapter = mBluetoothAdapter.getDefaultAdapter();
        registryEvent();
    	mBluetoothAdapter.enable();
        
	    devNum=0;
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String name= device.getName();
             	if(name.startsWith(prdName))
             	{

             		//	  devName=name;
             		      devNum++;
             		      mBluetoothAdapter.cancelDiscovery();
             		     chkConnection(device);
             		               	      
             	  }
               }
          }
        if(devNum==0) {
        	BtDiscover(); 
           } 	
	 
    }
	private void registryEvent()
	{
        
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
    	filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);  
       	filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        this.registerReceiver(mReceiver, filter);  
	}
	private boolean chkConnection(BluetoothDevice dev)
	{
		BluetoothSocket btsocket=null; 
        InputStream tmpIn = null; 
        OutputStream tmpOut = null;
        FileInputStream file =null;
        byte buf[]= new byte[512];
        int i;
        
        mBluetoothAdapter.cancelDiscovery();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
    
        this.registerReceiver(mReceiver, filter);  
		try{
			btsocket=dev.createRfcommSocketToServiceRecord(HeadSet_UUID);
			file =new FileInputStream(WK_File);
			
		}catch (IOException e) {
			return false;
		}	     	
		try{
			btsocket.connect();
			
			tmpIn = btsocket.getInputStream(); 
		    tmpOut = btsocket.getOutputStream();
		    
		    for(i=0;i<10000;i++)
		    {
		    	file.read(buf);
		    	tmpOut.write(buf);
		    }
                file.close();
            
		 //   tmpOut.
			btsocket.close();
		}catch (IOException e) {
			return false;
		}        
		return true;    	 		
	}
    
    
    
	 private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name= device.getName();
                if	(name.startsWith("8XX")||name.startsWith(prdName))
                {
                	if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                		try{
                			Method createBondMethod = BluetoothDevice.class.getMethod("createBond");   
                            Boolean ret =(Boolean) createBondMethod.invoke(device);    
                            Toast.makeText(getApplicationContext(), name + "added to list", Toast.LENGTH_LONG).show();
                		    
                		}catch (Exception e) {   
                            e.printStackTrace();   
                        }   
                	}                	
                }
            // 當發現藍牙裝置結束時，更改機動程式的標題
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            	Log.d("BlueToothTestActivity", "ACTION_FOUND");   
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name= device.getName();
          	    if(name.startsWith(prdName))
          	    {
                    Toast.makeText(getApplicationContext(), name+"Device connected", Toast.LENGTH_LONG).show();
          	    }    
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name= device.getName();
                if(name.startsWith(prdName))
          	    {
            //    	if(!chkConnection(device))
                	{
                        Toast.makeText(getApplicationContext(), name+"connection lost!!!!", Toast.LENGTH_LONG).show();
              //          playRingtone();
                	}
          	 //   }   
            }
	    }
	 }
	 };
    
}