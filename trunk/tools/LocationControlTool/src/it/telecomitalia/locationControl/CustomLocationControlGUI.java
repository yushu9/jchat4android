/**
 * 
 */
package it.telecomitalia.locationControl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.Client;
import com.android.ddmlib.Device;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmuilib.DevicePanel;
import com.android.ddmuilib.IImageLoader;
import com.android.ddmuilib.ImageLoader;
import com.android.ddmuilib.DevicePanel.IUiSelectionListener;
import com.android.ddmuilib.location.KmlParser;
import com.android.ddmuilib.location.WayPoint;
import com.android.ddmuilib.location.WayPointContentProvider;
import com.android.ddmuilib.location.WayPointLabelProvider;

/**
 * @author s.semeria
 *
 */
public class CustomLocationControlGUI  {

	private Display mDisplay;
	private DevicePanel mDevicePanel;
	private Composite mPanelArea;
	private Button mKmlUploadButton;
	private Table mKmlWayPointTable;
	private TableViewer mKmlTableViewer;
	private Button mLoopModeCheckButton;
	private Button mKmlPlayButton;
	private Button mKmlStopButton;
	private Text mDelayMillisecs;
	private Map<String, LocationPlayer> mPlayerMap;
	private LocationPlayer mCurrentPlayer;
	private static final String SHELL_TITLE = "Dalvik Debug Monitor ";
	
	private void populateUI(Shell mainWnd){
		FillLayout shellLayout = new FillLayout();
		shellLayout.type = SWT.HORIZONTAL;
		shellLayout.spacing = 5;
		shellLayout.marginWidth=10;
		shellLayout.marginHeight=10;
		
		mainWnd.setLayout(shellLayout);
	
		SashForm form = new SashForm(mainWnd,SWT.HORIZONTAL);
		
		Group deviceGroup = new Group(form,SWT.NONE);
		deviceGroup.setText("Available Devices");
	    ImageLoader loader = new ImageLoader(CustomLocationControlGUI.class); 
	    mDevicePanel = new DevicePanel(loader, true);
		mDevicePanel.createPanel(deviceGroup);
		
	
		
		mDevicePanel.addSelectionListener(new IUiSelectionListener(){

			public void selectionChanged(Device arg0, Client arg1) {
				if (arg0 != null ){
					if (mPlayerMap.containsKey(arg0.getSerialNumber())){
						mCurrentPlayer = mPlayerMap.get(arg0.getSerialNumber());
						enableLocationControls();
						refreshUI();
					}
				}
				
			}
			
		});
		
		
		
		AndroidDebugBridge.addDeviceChangeListener(new IDeviceChangeListener(){

			public void deviceChanged(Device arg0, int arg1) {
				// TODO Auto-generated method stub
				if (!mPlayerMap.containsKey(arg0.getSerialNumber())){
					LocationPlayer player = new LocationPlayer(arg0);
					mPlayerMap.put(arg0.getSerialNumber(), player);
				}
			}

			public void deviceConnected(Device arg0) {
				// TODO Auto-generated method stub
				if (!mPlayerMap.containsKey(arg0.getSerialNumber())){
					LocationPlayer player = new LocationPlayer(arg0);
					mPlayerMap.put(arg0.getSerialNumber(), player);
				}
			}

			public void deviceDisconnected(Device arg0) {
				// TODO Auto-generated method stub
				if (mPlayerMap.containsKey(arg0.getSerialNumber())){
					
					if (mCurrentPlayer!= null && mCurrentPlayer.getSerialNumber().equals(arg0.getSerialNumber())){
						
						mDisplay.asyncExec(new Runnable(){

							public void run() {
								
								if (AndroidDebugBridge.getBridge().getDevices().length == 0){
									mKmlWayPointTable.removeAll();
									disableLocationControls();
								} else {
									mKmlWayPointTable.removeAll();	
									CustomLocationControlGUI.this.mDelayMillisecs.setText(String.valueOf(LocationPlayer.DEFAULT_DELAY_MS));
									CustomLocationControlGUI.this.mLoopModeCheckButton.setSelection(true);
									CustomLocationControlGUI.this.mKmlPlayButton.setEnabled(false);
									CustomLocationControlGUI.this.mKmlStopButton.setEnabled(false);
									CustomLocationControlGUI.this.mDelayMillisecs.setEnabled(false);
									CustomLocationControlGUI.this.mLoopModeCheckButton.setEnabled(false);
								}
								 
							}
							
						});
						
						if (mCurrentPlayer.isPlaying())
							mCurrentPlayer.stop();
						mCurrentPlayer = null;
					}
					
					mPlayerMap.remove(arg0.getSerialNumber());
					
				}
			}
			
		});
		
		Group locationGroup = new Group(form,SWT.NONE);
		locationGroup.setText("Location Controls");
		GridLayout locationGroupLayout = new GridLayout(1,false);
		locationGroup.setLayout(locationGroupLayout);
		createLocationControls(locationGroup);
	}
	
	public void runUI(){
		Display.setAppName("Custom DDMS");
		mPlayerMap = new HashMap<String, LocationPlayer>();
		mCurrentPlayer = null;
		mDisplay = new Display();
		
		Shell mainWnd = new Shell(mDisplay,SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX);
		mainWnd.addShellListener(new ShellListener(){

			public void shellActivated(ShellEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void shellClosed(ShellEvent arg0) {
				// Close all the available playes
				CustomLocationControlGUI.this.dispose();
			}

			public void shellDeactivated(ShellEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void shellDeiconified(ShellEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void shellIconified(ShellEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		String adbLocation = System.getProperty("com.android.ddms.bindir");
        if(adbLocation != null && adbLocation.length() != 0)
            adbLocation = (new StringBuilder()).append(adbLocation).append(File.separator).append("adb").toString();
        else
            adbLocation = "adb";
        AndroidDebugBridge.init(true);
        AndroidDebugBridge.createBridge(adbLocation, true);
        mainWnd.setText(SHELL_TITLE);
        populateUI(mainWnd);
        
        mainWnd.pack();
        setSizeAndPosition(mainWnd);
        mainWnd.open();

        while (!mainWnd.isDisposed()){
        	if (!mDisplay.readAndDispatch()){
        		mDisplay.sleep();
        	}
        }
        
        
	}

	/**
	 * @param mainWnd
	 */
	private void setSizeAndPosition(Shell mainWnd) {
		// TODO Auto-generated method stub
		mainWnd.setBounds(new Rectangle(50,50,800,600));
	}

	 private void createLocationControls(Composite kmlLocationGroup)
	 {
	         
	     mKmlUploadButton = new Button(kmlLocationGroup, SWT.PUSH);
	     mKmlUploadButton.setText("Load KML...");
	     mKmlUploadButton.addSelectionListener(new SelectionAdapter(){

			public void widgetSelected(SelectionEvent e) {
				 FileDialog fileDialog = new FileDialog(CustomLocationControlGUI.this.mKmlUploadButton.getShell(), 4096);
	             fileDialog.setText("Load KML Route");
	             fileDialog.setFilterExtensions(new String[] {
	                    "*.kml"
	             });
	             String fileName = fileDialog.open();
	                
	             if(fileName != null)
	             {
	                    KmlTrackParser parser = new KmlTrackParser(fileName);
	                    if(parser.parse())
	                    {
	                    	if (mCurrentPlayer != null){
	                    	  mCurrentPlayer.setRouteData(parser.getWayPoints());
	                    	}
	                    	refreshUI();
	                    }
	            }	
			}
	    	 
	     });
	     
	     
	     mKmlWayPointTable = new Table(kmlLocationGroup, SWT.SINGLE | SWT.FULL_SELECTION | SWT.VIRTUAL );
	 
	     mKmlWayPointTable.setHeaderVisible(true);
         mKmlWayPointTable.setLinesVisible(true);
         mKmlWayPointTable.setItemCount(20);
         TableColumn waypointIndex = new TableColumn(mKmlWayPointTable,0);
         waypointIndex.setText("Waypoint");
         waypointIndex.setWidth(100);
         TableColumn latitudeColumn = new TableColumn(mKmlWayPointTable, 0);
         latitudeColumn.setText("Latitude");
         latitudeColumn.setWidth(100);
         latitudeColumn.setResizable(true);
         TableColumn longitudeColumn = new TableColumn(mKmlWayPointTable,0);
         longitudeColumn.setText("Longitude");
         longitudeColumn.setWidth(80);
         longitudeColumn.setResizable(true);
         TableColumn altitudeColumn = new TableColumn(mKmlWayPointTable,0);
         altitudeColumn.setText("Altitude");
         altitudeColumn.setWidth(80);
         altitudeColumn.setResizable(true);        
         mKmlTableViewer = new TableViewer(mKmlWayPointTable);
         mKmlTableViewer.setContentProvider(new WaypointLocCProvider());
         WayPointLocLabelProvider labelProvider = new WayPointLocLabelProvider();
         mKmlTableViewer.setLabelProvider(labelProvider);
         Composite animationOptions = new Composite(kmlLocationGroup,0);
         GridLayout animationOptionsLayout = new GridLayout(1,true);
         animationOptions.setLayout(animationOptionsLayout);
         mLoopModeCheckButton = new Button(animationOptions, SWT.CHECK);
         mLoopModeCheckButton.setText("Loop Mode enabled");
         mLoopModeCheckButton.setSelection(true);
         mLoopModeCheckButton.addSelectionListener(new SelectionAdapter(){

			public void widgetSelected(SelectionEvent e) {
				if (mCurrentPlayer != null)
				  mCurrentPlayer.setLoopMode(mLoopModeCheckButton.getSelection());
			}
        	 
         });
          
         Label delayLabel = new Label(animationOptions, SWT.LEFT);
         delayLabel.setText("Delay between positions (ms)");
         mDelayMillisecs = new Text(animationOptions,SWT.SINGLE);
         
         Composite rowOfButtonPanel = new Composite(kmlLocationGroup,0);
         RowLayout rowOfButtonLayout = new RowLayout();
         rowOfButtonLayout.wrap= false;
         rowOfButtonLayout.marginWidth = 5;
         rowOfButtonPanel.setLayout(rowOfButtonLayout);
         mKmlPlayButton = new Button(rowOfButtonPanel,SWT.PUSH);
         mKmlPlayButton.addSelectionListener(new SelectionAdapter(){

			public void widgetSelected(SelectionEvent e) {
				
				try{
				if (mCurrentPlayer != null && mCurrentPlayer.getRouteData() != null){
					mCurrentPlayer.setLoopMode(mLoopModeCheckButton.getSelection());
					String delayStr = mDelayMillisecs.getText();
					if (delayStr != null && !delayStr.isEmpty()){
						long millisecs = Long.parseLong(delayStr);
						mCurrentPlayer.setDelay(millisecs);
					}
					mKmlStopButton.setEnabled(true);
					mKmlPlayButton.setEnabled(false);
					mDelayMillisecs.setEnabled(false);
					mKmlUploadButton.setEnabled(false);
					mLoopModeCheckButton.setEnabled(false);
					mCurrentPlayer.play();
				}
				} catch (NumberFormatException ex) {
					
				}
			}
        	 
         });
         mKmlPlayButton.setText("Play Route");
         mKmlStopButton = new Button(rowOfButtonPanel,SWT.PUSH);
         mKmlStopButton.setEnabled(false);
         mKmlStopButton.addSelectionListener(new SelectionAdapter(){

			public void widgetSelected(SelectionEvent e) {
				
				if (mCurrentPlayer != null){
					
					if (mCurrentPlayer.isPlaying()) {
						mDelayMillisecs.setEnabled(true);
						mKmlPlayButton.setEnabled(true);
						mKmlUploadButton.setEnabled(true);
						mLoopModeCheckButton.setEnabled(true);
						mKmlStopButton.setEnabled(false);
						mCurrentPlayer.stop();
					}
					
				}
			}
        	 
         });
         mKmlStopButton.setText("Stop Route");
         disableLocationControls();
	    }	       	
	 
	 private void refreshUI(){
		 if (mCurrentPlayer != null){
			 if (mCurrentPlayer.getRouteData() != null) {
				 CustomLocationControlGUI.this.mKmlTableViewer.setInput(mCurrentPlayer.getRouteData());
			 } else {
				mKmlWayPointTable.removeAll();
			 }
			 CustomLocationControlGUI.this.mDelayMillisecs.setText(String.valueOf(mCurrentPlayer.getDelay()));
			 CustomLocationControlGUI.this.mLoopModeCheckButton.setSelection(mCurrentPlayer.getLoopMode());
			 CustomLocationControlGUI.this.mKmlPlayButton.setEnabled(!mCurrentPlayer.isPlaying());
			 CustomLocationControlGUI.this.mKmlStopButton.setEnabled(mCurrentPlayer.isPlaying());
			 CustomLocationControlGUI.this.mKmlUploadButton.setEnabled(!mCurrentPlayer.isPlaying());
			 CustomLocationControlGUI.this.mDelayMillisecs.setEnabled(!mCurrentPlayer.isPlaying());
			 CustomLocationControlGUI.this.mLoopModeCheckButton.setEnabled(!mCurrentPlayer.isPlaying());
		 }
	 }
	 
	 public void disableLocationControls(){
		 mKmlUploadButton.setEnabled(false);
		 mDelayMillisecs.setEnabled(false);
		 mKmlPlayButton.setEnabled(false);
		 mKmlStopButton.setEnabled(false);
		 mLoopModeCheckButton.setEnabled(false);
		 mKmlWayPointTable.setEnabled(false);
	 }
	 
	 public void enableLocationControls(){
		 mKmlUploadButton.setEnabled(true);
		 mDelayMillisecs.setEnabled(true);
		 mKmlPlayButton.setEnabled(true);
		 mKmlStopButton.setEnabled(false);
		 mLoopModeCheckButton.setEnabled(true);
		 mKmlWayPointTable.setEnabled(true);
	 }
	 
	 public void dispose(){
		for (String id : mPlayerMap.keySet()) {
			LocationPlayer player = mPlayerMap.get(id);
			
			if (player.isPlaying())
				player.stop();
			
		}
		mPlayerMap = null;
		mDisplay.dispose();
	 }
}
