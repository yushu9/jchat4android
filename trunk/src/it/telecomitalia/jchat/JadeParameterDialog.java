/*****************************************************************
 jChat is a  chat application for Android based on JADE
  Copyright (C) 2008 Telecomitalia S.p.A. 
 
 GNU Lesser General Public License

 This is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, 
 version 2.1 of the License. 

 This software is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this software; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.
 *****************************************************************/

package it.telecomitalia.jchat;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * Custom dialog employed to show to the user current host and port for JADE main container
 * making him able to change default settings (read from strings.xml) if he wishes to.
 * 
 * @author Cristina Cucè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0 
 */
public class JadeParameterDialog extends Dialog {

	/** 
	 * The JADE main container host address. 
	 */
	private String jadeAddress;
	
	/**
	 * The JADE main container host port.
	 */
	private String jadePort;
	
	/** 
	 * GUI element containing the JADE address value
	 */
	private EditText jadeAddressEdt;
	
	/** 
	 * GUI element containing the JADE port value
	 */
	private EditText jadePortEdt;
	
	
	private final String JADE_DEFAULT_HOST="it.telecomitalia.jchat.JADE_DEFAULT_HOST";
	private final String JADE_DEFAULT_PORT="it.telecomitalia.jchat.JADE_DEFAULT_PORT";
	
	/**
	 * Main activity
	 */
	private Activity activity;
	
	/**
	 * Instantiates a new jade parameter dialog.
	 * 
	 * @param act the current application context
	 */
	public JadeParameterDialog(Activity act) {
		super(act);
		activity = act;
		View v = initUI();
		this.setTitle(act.getString(R.string.label_params_title));
		this.setCancelable(false);
		this.setContentView(v);
		fillWithDefaults();
	}
	
	/**
	 * Retrieve default values for JADE host/port from strings.xml file
	 * 
	 * @param ctx the application context
	 */
	private void fillWithDefaults(){
		
		SharedPreferences prefs = activity.getPreferences(Activity.MODE_PRIVATE);
		jadeAddress = prefs.getString(JADE_DEFAULT_HOST, activity.getString(R.string.jade_platform_host));
		jadePort = prefs.getString(JADE_DEFAULT_PORT, activity.getString(R.string.jade_platform_port));
		jadeAddressEdt.setText(jadeAddress);
		jadePortEdt.setText(jadePort);
	}
	
	/**
	 * Gets the JADE main container host address.
	 * 
	 * @return the JADE main container host address.
	 */
	public String getJadeAddress(){
		return jadeAddress;
	}
	
	/**
	 * Gets the JADE main container host port.
	 * 
	 * @return the JADE main container host port
	 */
	public String getJadePort(){
		return jadePort;
	}
	
	
	/**
	 * Initializes the dialog UI, preparing the parent view containing view hierarchy 
	 * Layout is hardcoded here, no xml.
	 * 
	 * @param ctx the application context
	 * @return the parent view
	 */
	private View initUI(){
		RelativeLayout layout = new RelativeLayout(activity);
		
		TextView jadeAddress = new TextView(activity);
		jadeAddress.setText("Jade platform address");
		jadeAddress.setId(1);
		layout.addView(jadeAddress,new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		jadeAddressEdt = new EditText(activity);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, 1);
		jadeAddressEdt.setId(2);
		layout.addView(jadeAddressEdt,params);
		
		TextView jadePort = new TextView(activity);
		jadePort.setText("Jade platform port");
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, 2);
		jadePort.setId(3);
		layout.addView(jadePort,params);
		
		jadePortEdt = new EditText(activity);
		jadePortEdt.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, 3);
		jadePortEdt.setId(4);
		layout.addView(jadePortEdt,params);
		
				
		Button closeButton = new Button(activity);
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.addRule(RelativeLayout.BELOW, 4);
		closeButton.setText("Close");
		closeButton.setOnClickListener(new View.OnClickListener(){
			/**
			 * Handles clicking on close button 
			 */
			public void onClick(View arg0) {
					String tmpVar = JadeParameterDialog.this.jadeAddressEdt.getText().toString();
					
					SharedPreferences.Editor prefsEdt = activity.getPreferences(Activity.MODE_PRIVATE).edit();
					
					
					if (tmpVar.length() > 0){
						JadeParameterDialog.this.jadeAddress = tmpVar;
						prefsEdt.putString(JADE_DEFAULT_HOST, tmpVar);
					}
					
					tmpVar = JadeParameterDialog.this.jadePortEdt.getText().toString();
					if (tmpVar.length() > 0){
						JadeParameterDialog.this.jadePort = tmpVar;
						prefsEdt.putString(JADE_DEFAULT_PORT, tmpVar);
					}
					
					prefsEdt.commit();
					JadeParameterDialog.this.dismiss();
			}
			
		});
		
		layout.addView(closeButton,params);
		
		return layout;
	}
	
	
}
