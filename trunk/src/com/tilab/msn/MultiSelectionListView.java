package com.tilab.msn;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class MultiSelectionListView extends ListView{

	public MultiSelectionListView(Context context, AttributeSet attrs, Map params) {
		super(context, attrs, params);
		// TODO Auto-generated constructor stub
	}
	
	public List<String> getAllSelectedItems () {				
		return ((ContactListAdapter) getAdapter()).getAllSelectedItemIds();
	}
	
	public void uncheckAllSelectedItems(){
		((ContactListAdapter) getAdapter()).uncheckAll();	
		invalidate();
	}
		
}
 
