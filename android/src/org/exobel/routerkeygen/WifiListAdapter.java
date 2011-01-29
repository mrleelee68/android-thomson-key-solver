package org.exobel.routerkeygen;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WifiListAdapter extends BaseAdapter {
	private List<WifiNetwork> listNetworks; 
	private Context context; 
	public WifiListAdapter(List<WifiNetwork> list, Context context) {
		if ( list != null )
			this.listNetworks = list;
		else
			this.listNetworks = new ArrayList<WifiNetwork>();
        this.context = context;
    }
	
	public int getCount() {
		return listNetworks.size();
	}

	public Object getItem(int position) {
		return listNetworks.get(position);
	}

	public long getItemId(int position) {
		return listNetworks.get(position).hashCode();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		 RelativeLayout itemLayout;
		 WifiNetwork wifi = listNetworks.get(position);
		 int strenght = listNetworks.get(position).getLevel();
	     itemLayout= (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.item_list_wifi, parent, false);
	 
	     TextView ssid = (TextView) itemLayout.findViewById(R.id.wifiName);
	     ssid.setText(wifi.ssid);
	     
	     TextView bssid = (TextView) itemLayout.findViewById(R.id.wifiMAC);
	     bssid.setText(wifi.mac);
	     
	     ImageView icon = (ImageView)itemLayout.findViewById(R.id.icon);
	     if ( wifi.supported && !wifi.newThomson)
	    	 icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_possible));
	     
	     ImageView networkS = (ImageView)itemLayout.findViewById(R.id.strenght);
	     
	     if ( strenght >= -55 )
	    	 networkS.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_wifi_verystrong));
	     else
	    	 if ( strenght >= -70 )
	    		 networkS.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_wifi_medium));
		return  itemLayout;
	}

}
