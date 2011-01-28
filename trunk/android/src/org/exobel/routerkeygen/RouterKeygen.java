package org.exobel.routerkeygen;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.exobel.routerkeygen.WifiNetwork.TYPE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RouterKeygen extends Activity {

	WifiManager wifi;
	boolean wifi_state;
	ListView scanResuls;
	KeygenThread calculator;
	List<String> list_key = null;
	BroadcastReceiver scanFinished;
	BroadcastReceiver stateChanged;
	List<WifiNetwork> vulnerable;
	String router;
	long begin;
	static final String TAG = "RouterKeygen";
	Handler handler = new Handler() {
          public void handleMessage(Message msg) {
        	if ( thomson3g )
				removeDialog(PROGRESSBAR);
			if ( msg.what == 0 )
			{
				begin = System.currentTimeMillis()-begin;
				Log.d(TAG, "Time to solve:" + begin);
				showDialog(KEY_LIST);
			}
			if ( msg.what == 1 )
			{
				Toast.makeText( RouterKeygen.this , list_key.get(0) , Toast.LENGTH_SHORT).show();
				
			}
		}
	};
	final String welcomeScreenShownPref = "welcomeScreenShown";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifi_state = wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED ||  
						wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLING;
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		Boolean welcomeScreenShown = mPrefs.getBoolean( welcomeScreenShownPref, false);
		
		if (!welcomeScreenShown) {
		
			String whatsNewTitle = getResources().getString(R.string.msg_welcome_title);
			String whatsNewText = getResources().getString(R.string.msg_welcome_text);
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(whatsNewTitle).setMessage(whatsNewText).setPositiveButton(
			        R.string.bt_ok, new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int which) {
			                dialog.dismiss();
			            }
			        }).show();
			SharedPreferences.Editor editor = mPrefs.edit();
			editor.putBoolean(welcomeScreenShownPref, true);
			editor.commit();
		}
		

		scanResuls = (ListView) findViewById(R.id.ListWifi);
		scanResuls.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
					router = ((TextView)((RelativeLayout) view).getChildAt(2)).getText().toString();
					
					if ( !vulnerable.get(position).supported )
					{
						  Toast.makeText( RouterKeygen.this , 
								  RouterKeygen.this.getResources().getString(R.string.msg_unspported) ,
								  Toast.LENGTH_SHORT).show();
						  return;
					}
					else
						if (vulnerable.get(position).newThomson)
						{
							Toast.makeText( RouterKeygen.this ,
									RouterKeygen.this.getResources().getString(R.string.msg_newthomson) ,
									Toast.LENGTH_SHORT).show();
							  return;
						}
			        begin =  System.currentTimeMillis();
			        switch( vulnerable.get(position).type )
			        {
			        	case THOMSON: RouterKeygen.this.calculator = 
			        						new ThomsonKeygen(RouterKeygen.this , thomson3g);
			        				  		break;
			        	case DISCUS: RouterKeygen.this.calculator = 
    										new DiscusKeygen(RouterKeygen.this);
      				  						break;
			        	case EIRCOM: RouterKeygen.this.calculator = 
											new EircomKeygen(RouterKeygen.this);
					  						break;
			        	case DLINK: RouterKeygen.this.calculator = 
											new DlinkKeygen(RouterKeygen.this);
				  							break;
				  		case VERIZON: RouterKeygen.this.calculator = 
											new VerizonKeygen(RouterKeygen.this);
				  							break;
				  		case PIRELLI: RouterKeygen.this.calculator = 
				  						new PirelliKeygen(RouterKeygen.this);
  										break;
				  		case ALICE:	 RouterKeygen.this.calculator = 
										new AliceKeygen(RouterKeygen.this);
  										break;	
			        }
			        
					RouterKeygen.this.calculator.router = vulnerable.get(position);
					RouterKeygen.this.calculator.setPriority(Thread.MAX_PRIORITY);
					RouterKeygen.this.calculator.start();
					if (  vulnerable.get(position).type == TYPE.THOMSON && thomson3g )
						showDialog(PROGRESSBAR);
					removeDialog(KEY_LIST);
			}
		});
		stateChanged = new WifiStateReceiver(wifi);
    	scanFinished = new WiFiScanReceiver(this);
	}
	
	protected void onSaveInstanceState (Bundle outState){	
		PersitentData rot = new PersitentData(vulnerable, router, list_key);
		outState.putSerializable("backup", rot);
	}

	protected void onRestoreInstanceState (Bundle savedInstanceState){
		PersitentData rot = (PersitentData)savedInstanceState.getSerializable("backup");
		if ( rot == null )
			return;
		vulnerable = rot.list;
		list_key = rot.list_key;
		router = rot.router;
		scanResuls.setAdapter(new WifiListAdapter(vulnerable, this));
	}
	
    public void onStart() {
		super.onStart();
		getPrefs();
		if ( wifiOn )
		{
			if ( !wifi.setWifiEnabled(true))
				Toast.makeText( RouterKeygen.this , 
				  RouterKeygen.this.getResources().getString(R.string.msg_wifibroken),
				  Toast.LENGTH_SHORT).show();
			else
				wifi_state = true;
		}
		scan();
    }
    
	public void onStop() {
    	try{ 
    		super.onStop();
    		unregisterReceiver(scanFinished);
    		unregisterReceiver(stateChanged);
    		removeDialog(KEY_LIST);
			removeDialog(MANUAL_CALC); 
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	ProgressDialog progressDialog;
    private static final int PROGRESSBAR = 0; 
    private static final int KEY_LIST = 1;
    private static final int MANUAL_CALC = 2;
    protected Dialog onCreateDialog(int id ) {
        switch (id) {
        case PROGRESSBAR: {
            				progressDialog = new ProgressDialog(RouterKeygen.this);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.setTitle("Thomson 3G Lookup");
                            progressDialog.setMessage("Fetching Keys...");
                            progressDialog.setCancelable(false);
                            progressDialog.setProgress(0);
                            progressDialog.setButton("Cancel", new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which) {
                                    	RouterKeygen.this.calculator.stopRequested = true;
                                            removeDialog(PROGRESSBAR);
                                    }
    });
                            progressDialog.setIndeterminate(false);
            return progressDialog;
        }
            case KEY_LIST: {
            	Dialog dialog = new Dialog(this);
        		dialog.setContentView(R.layout.results);
            	dialog.setTitle(RouterKeygen.this.router);
            	
            	ListView list = (ListView) dialog.findViewById(R.id.list_keys);	
            	list.setOnItemClickListener(new OnItemClickListener() {
        			public void onItemClick(AdapterView<?> parent, View view,
        					int position, long id) {
        				String key =((TextView)((RelativeLayout) view).getChildAt(0)).getText().toString();
        					Toast.makeText(getApplicationContext(), key + " " 							
        							+ RouterKeygen.this.getResources().getString(R.string.msg_copied),
        							Toast.LENGTH_SHORT).show();
        					ClipboardManager clipboard = 
                                  (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 

                          clipboard.setText(key);
                          startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        			}
            	});
            	list.setAdapter(new KeylistAdapter(list_key , this));
            	Button share = ( Button ) dialog.findViewById(R.id.bt_share);
            	share.setOnClickListener(new View.OnClickListener(){
             		 public void onClick(View arg0) {
             			Intent i = new Intent(Intent.ACTION_SEND);
             			i.setType("text/plain");
             			i.putExtra(Intent.EXTRA_SUBJECT, router + 
             					RouterKeygen.this.getResources().getString(R.string.share_msg_begin));
             			Iterator<String> it = list_key.iterator();
             			String message = RouterKeygen.this.getResources().getString(R.string.share_msg_begin)
             			+ ":\n";
             			while ( it.hasNext() )
             				message += it.next() + "\n"; 
             			i.putExtra(Intent.EXTRA_TEXT, message);
             			message = RouterKeygen.this.getResources().getString(R.string.share_title);
             			startActivity(Intent.createChooser(i, message));
                }
           	});
            	return dialog;
            }
            case MANUAL_CALC: {
            	Dialog dialog = new Dialog(this);
            	dialog.setContentView(R.layout.manual);
            	dialog.setTitle("Manual Input");
            	final EditText edit = (EditText ) dialog.findViewById(R.id.manual_edittext);
            	
            	Button calc = ( Button ) dialog.findViewById(R.id.bt_manual_calc);
            	calc.setOnClickListener(new View.OnClickListener(){
           		 public void onClick(View arg0) {
        					router = edit.getText().toString().trim();
        					WifiNetwork wifi = new WifiNetwork(router , "" , 0);
        					if ( !wifi.supported )
        					{
        						  Toast.makeText( RouterKeygen.this , 
        								  RouterKeygen.this.getResources().getString(R.string.msg_unspported),
        								  Toast.LENGTH_SHORT).show();
        						  return;
        					}
        					switch( wifi.type )
        			        {
        			        	case THOMSON: RouterKeygen.this.calculator = 
        			        						new ThomsonKeygen(RouterKeygen.this , thomson3g);
        			        				  break;
        			        	case DISCUS: RouterKeygen.this.calculator = 
            										new DiscusKeygen(RouterKeygen.this);
              				  						break;
        			        	case EIRCOM: RouterKeygen.this.calculator = 
													new EircomKeygen(RouterKeygen.this);
							  						break;
        			        	case DLINK: RouterKeygen.this.calculator = 
													new DlinkKeygen(RouterKeygen.this);
						  							break;
        			        	case VERIZON: RouterKeygen.this.calculator = 
													new VerizonKeygen(RouterKeygen.this);
						  							break;
        				  		case PIRELLI: RouterKeygen.this.calculator = 
						  							new PirelliKeygen(RouterKeygen.this);
													break;
        				  		case ALICE:	 RouterKeygen.this.calculator = 
		  											new AliceKeygen(RouterKeygen.this);
        				  							break;				
													
        			        }
        					RouterKeygen.this.calculator.router = wifi;
        					RouterKeygen.this.calculator.setPriority(Thread.MAX_PRIORITY);
        					RouterKeygen.this.calculator.start();
        					removeDialog(KEY_LIST);
        					removeDialog(MANUAL_CALC);
        					if (  wifi.type == TYPE.THOMSON && thomson3g )
        						showDialog(PROGRESSBAR);
                   	
                        	
                }
           	});
            	Button cancel = ( Button ) dialog.findViewById(R.id.bt_manual_cancel);
            	cancel.setOnClickListener(new View.OnClickListener(){
              		 public void onClick(View arg0) {
                        	 removeDialog(MANUAL_CALC);
                 }
            	});
         
            	return dialog;
            }
        }
        return null;
    }
    

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wifi, menu);
        return true;
    }
    
    public void scan(){
    		registerReceiver(scanFinished, new IntentFilter(
    				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    		if ( !wifi_state && !wifiOn )
    		{
				  Toast.makeText( RouterKeygen.this , 
						  RouterKeygen.this.getResources().getString(R.string.msg_nowifi),
						  Toast.LENGTH_SHORT).show();
				  return;
    		}
	    	if ( wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLING )
	    	{
				  registerReceiver(stateChanged, new IntentFilter(
		    				WifiManager.WIFI_STATE_CHANGED_ACTION));
				  Toast.makeText( RouterKeygen.this ,
							RouterKeygen.this.getResources().getString(R.string.msg_wifienabling),
							Toast.LENGTH_SHORT).show();
	    	}
			else
				if ( wifi.startScan() )
					Toast.makeText( RouterKeygen.this ,
							RouterKeygen.this.getResources().getString(R.string.msg_scanstarted),
							Toast.LENGTH_SHORT).show();
				else
				  Toast.makeText( RouterKeygen.this ,
						  RouterKeygen.this.getResources().getString(R.string.msg_scanfailed),
						  Toast.LENGTH_SHORT).show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.wifi_scan:
            scan();
			return true;
        case R.id.manual_input:
        	showDialog(MANUAL_CALC);
        	return true;
        case R.id.pref:
        	startActivity( new Intent(this , Preferences.class ));
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    

    boolean wifiOn;
    boolean thomson3g;
    String folderSelect;
	final String folderSelectPref = "folderSelect";
	final String wifiOnPref = "wifion";
	final String thomson3gPref = "thomson3g";
	private void getPrefs() {
	    SharedPreferences prefs = PreferenceManager
	                    .getDefaultSharedPreferences(getBaseContext());
	    wifiOn = prefs.getBoolean(wifiOnPref , true);
	    thomson3g = prefs.getBoolean(thomson3gPref, false);
	    folderSelect = prefs.getString(folderSelectPref, 
	    		Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + 
	    		"thomson");
    }
	
	
}