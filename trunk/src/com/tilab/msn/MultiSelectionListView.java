package com.tilab.msn;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;


/**
 * Customized {@link ListView} that shows checkbox inside each item to allow multiple selections
 * of contacts. It is used in {@link ContactListActivity} to show the contact list.
 * Uses a customized xml-based layout
 * 
 * @author Cristina Cucè
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0
 */
public class MultiSelectionListView extends ListView{

	/**
	 * Instantiates a new multiple selection list view.
	 * 
	 */
	public MultiSelectionListView(Context context, AttributeSet attrs, Map params) {
		super(context, attrs, params);
	}
	
	/**
	 * Gets the all contacts checked in the list.
	 * 
	 * @return the list of all checked contacts 
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
 
