package com.tilab.msn;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

// TODO: Auto-generated Javadoc
/**
 * The Class MultiSelectionListView.
 */
public class MultiSelectionListView extends ListView{

	/**
	 * Instantiates a new multi selection list view.
	 * 
	 * @param context the context
	 * @param attrs the attrs
	 * @param params the params
	 */
	public MultiSelectionListView(Context context, AttributeSet attrs, Map params) {
		super(context, attrs, params);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Gets the all selected items.
	 * 
	 * @return the all selected items
	 */
	public List<String> getAllSelectedItems () {				
		return ((ContactListAdapter) getAdapter()).getAllSelectedItemIds();
	}
	
	/**
	 * Uncheck all selected items.
	 */
	public void uncheckAllSelectedItems(){
		((ContactListAdapter) getAdapter()).uncheckAll();	
		invalidate();
	}
		
}
 
