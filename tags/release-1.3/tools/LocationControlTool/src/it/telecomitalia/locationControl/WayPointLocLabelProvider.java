/**
 * 
 */
package it.telecomitalia.locationControl;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author s.semeria
 *
 */
public class WayPointLocLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnText(Object obj, int colIndex) {
		// TODO Auto-generated method stub
		StringBuilder wayPointData = new StringBuilder("");
		
		if (obj instanceof WayPointLocation){
			WayPointLocation waypoint = (WayPointLocation) obj;
			//The table columns are (WaypointIndex , Latitude, Longitude, Altitude) in this order
			if (colIndex == 0)
				wayPointData.append(waypoint.index);
			else if (colIndex == 1)
				wayPointData.append(waypoint.latitude);
			else if (colIndex == 2)
				wayPointData.append(waypoint.longitude);
			else if (colIndex == 3)
				wayPointData.append(waypoint.altitude);
		}
		
		return wayPointData.toString();
	}

	public void addListener(ILabelProviderListener arg0) {
		// TODO Auto-generated method stub
		
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public boolean isLabelProperty(Object arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener arg0) {
		// TODO Auto-generated method stub
		
	}

	 
}
