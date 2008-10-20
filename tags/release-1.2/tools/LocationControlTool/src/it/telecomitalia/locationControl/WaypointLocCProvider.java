/**
 * 
 */
package it.telecomitalia.locationControl;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author s.semeria
 *
 */
public class WaypointLocCProvider implements IStructuredContentProvider {

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// TODO Auto-generated method stub
		
	}

	public Object[] getElements(Object arg0) {
		// TODO Auto-generated method stub
		if (arg0 instanceof WayPointLocation[] )
			return (WayPointLocation[]) arg0;
		else 
			return null;
	}

}
