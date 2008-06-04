package com.tilab.msn;

import android.app.Dialog;
import android.content.Context;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class JadeParameterDialog extends Dialog {

	private String jadeAddress;
	private String jadePort;
	private EditText jadeAddressEdt;
	private EditText jadePortEdt;
	public static final int JADE_DLG_OK=0;
	public static final int JADE_DLG_CANCEL=1;
	
	
	public JadeParameterDialog(Context context) {
		super(context);
		View v = initUI(context);
		this.setTitle(context.getString(R.string.label_params_title));
		this.setCancelable(false);
		this.setContentView(v);
		fillWithDefaults(context);
	}
	
	//Default values should be retrieved from resource file and phone cfgs
	private void fillWithDefaults(Context ctx){
		jadeAddress = ctx.getString(R.string.jade_platform_host);
		jadePort = ctx.getString(R.string.jade_platform_port);
		jadeAddressEdt.setText(jadeAddress);
		jadePortEdt.setText(jadePort);
	}
	
	public String getJadeAddress(){
		return jadeAddress;
	}
	
	public String getJadePort(){
		return jadePort;
	}
	
	
	private View initUI(Context ctx){
		RelativeLayout layout = new RelativeLayout(ctx);
		layout.setPreferredHeight(LayoutParams.WRAP_CONTENT);
		layout.setPreferredWidth(LayoutParams.WRAP_CONTENT);
		
		TextView jadeAddress = new TextView(ctx);
		jadeAddress.setText("Jade platform address");
		jadeAddress.setId(1);
		layout.addView(jadeAddress,new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		jadeAddressEdt = new EditText(ctx);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.POSITION_BELOW, 1);
		jadeAddressEdt.setId(2);
		layout.addView(jadeAddressEdt,params);
		
		TextView jadePort = new TextView(ctx);
		jadePort.setText("Jade platform port");
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.POSITION_BELOW, 2);
		jadePort.setId(3);
		layout.addView(jadePort,params);
		
		jadePortEdt = new EditText(ctx);
		jadePortEdt.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.POSITION_BELOW, 3);
		jadePortEdt.setId(4);
		layout.addView(jadePortEdt,params);
		
				
		Button closeButton = new Button(ctx);
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.addRule(RelativeLayout.POSITION_BELOW, 4);
		closeButton.setText("Close");
		closeButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
					String tmpVar = JadeParameterDialog.this.jadeAddressEdt.getText().toString();
					if (tmpVar.length() > 0){
						JadeParameterDialog.this.jadeAddress = tmpVar;
					}
					
					tmpVar = JadeParameterDialog.this.jadePortEdt.getText().toString();
					if (tmpVar.length() > 0){
						JadeParameterDialog.this.jadePort = tmpVar;
					}
					
					JadeParameterDialog.this.dismiss();
			}
			
		});
		
		layout.addView(closeButton,params);
		
		return layout;
	}
	
	
}
