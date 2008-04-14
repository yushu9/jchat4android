package com.tilab.msn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MultiSelectionListView extends ListView{

	public MultiSelectionListView(Context context, AttributeSet attrs, Map params) {
		super(context, attrs, params);
		// TODO Auto-generated constructor stub
	}
	public List getAllSelectedItems () {		
		List l= new ArrayList();
		ListAdapter la = this.getAdapter();
		for(int i=0; i< la.getCount(); i++)
		{
			View itemView = la.getView(i, null, this);
			CheckBox cb = (CheckBox)itemView.findViewById(R.id.contact_check_box);	
			if (cb.isChecked()){
				Contact c= (Contact)la.getItem(i);
			 	l.add(c);
			}
		}
		return l;	
	}
	
}
 
