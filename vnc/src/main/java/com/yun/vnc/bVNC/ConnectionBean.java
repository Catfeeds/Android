/**
 * Copyright (C) 2012 Iordan Iordanov
 * Copyright (C) 2009 Michael A. MacDonald
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 */

package com.yun.vnc.bVNC;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView.ScaleType;

import com.antlersoft.android.dbimpl.NewInstance;
import com.yun.vnc.R;
import com.yun.vnc.bVNC.input.InputHandlerDirectDragPan;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Iordan Iordanov
 * @author Michael A. MacDonald
 * @author David Warden
 *
 */
public class ConnectionBean extends AbstractConnectionBean implements Comparable<ConnectionBean> {
    
	private static final String TAG = "ConnectionBean";
    static Context c = null;
    protected boolean m_isReadyForConnection = true; // saved connections are OK
    protected boolean m_saved = false;
    private String masterPassword;
    
    static final NewInstance<ConnectionBean> newInstance=new NewInstance<ConnectionBean>() {
        public ConnectionBean get() { return new ConnectionBean(c); }
    };
    ConnectionBean(Context context)
    {
        set_Id(0);
        setAddress("");
        setPassword("");
        setKeepPassword(true);
        setNickname("");
        setUserName("");
        setPort(Constants.DEFAULT_PROTOCOL_PORT);
        setColorModel(COLORMODEL.C24bit.nameString());
        setPrefEncoding(RfbProto.EncodingTight);
        setScaleMode(ScaleType.MATRIX);
        setInputMode(InputHandlerDirectDragPan.ID);
        setUseDpadAsArrows(true);
        setUseLocalCursor(false);
        setExtraKeysToggleType(1);
        setMetaListId(1);
        setViewOnly(false);
        c = context;
    }
    
    boolean isNew()
    {
        return get_Id()== 0;
    }
    
    public synchronized void save(SQLiteDatabase database) {
        ContentValues values = Gen_getValues();
        values.remove(GEN_FIELD__ID);
        // Never save the SSH password and passphrase.
        if (!getKeepPassword()) {
            values.put(GEN_FIELD_PASSWORD, "");
        }
        if (isNew()) {
            set_Id(database.insert(GEN_TABLE_NAME, null, values));
        } else {
            database.update(GEN_TABLE_NAME, values, GEN_FIELD__ID + " = ?", new String[] { Long.toString(get_Id()) });
        }
    }
    
    public boolean isReadyForConnection()
    {
    	return m_isReadyForConnection;
    }
    public boolean isSaved()
    {
    	return m_saved;
    }
    
    ScaleType getScaleMode()
    {
        return ScaleType.valueOf(getScaleModeAsString());
    }
    
    void setScaleMode(ScaleType value)
    {
        setScaleModeAsString(value.toString());
    }
    
    static ConnectionBean createLoadFromUri(Uri dataUri, Context ctx)
    {    
    	ConnectionBean connection = new ConnectionBean(ctx);
    	if (dataUri == null) return connection;
		Database database = new Database(ctx);
      	String host = dataUri.getHost();
      	
    	// Intent generated by connection shortcut widget
    	if (host != null && host.startsWith(Utils.getConnectionString(ctx))) {
            int port = 0;
            int idx = host.indexOf(':');

            if (idx != -1) {
                try {
                    port = Integer.parseInt(host.substring(idx + 1));
                }
                catch (NumberFormatException nfe) { }
                host = host.substring(0, idx);
            }
            
            if (connection.Gen_read(database.getReadableDatabase(), port))
            {
                MostRecentBean bean = getMostRecent(database.getReadableDatabase());
                if (bean != null)
                {
                    bean.setConnectionId(connection.get_Id());
                    bean.Gen_update(database.getWritableDatabase());
                    database.close();
                }
            }
            return connection;
    	}
    	
    	// search based on nickname
    	SQLiteDatabase queryDb = database.getReadableDatabase();
    	String connectionName = dataUri.getQueryParameter(Constants.PARAM_CONN_NAME);
    	Cursor nickCursor = null;
    	if (connectionName != null)
    		nickCursor = queryDb.query(GEN_TABLE_NAME, new String[] { GEN_FIELD__ID }, GEN_FIELD_NICKNAME  + " = ?", new String[] { connectionName }, null, null, null);
    	if (nickCursor != null && nickCursor.moveToFirst())
    	{
    		// there could be many values, so we will just pick one
    		Log.i(TAG, String.format(Locale.US, "Loding connection info from nickname: %s", connectionName));
    		connection.Gen_populate(nickCursor, connection.Gen_columnIndices(nickCursor));
    		nickCursor.close();
    		database.close();
    		return connection;
    	}
    	if (nickCursor != null)
    		nickCursor.close();
    	
    	// search based on hostname
    	Cursor hostCursor = null;
    	if (host != null)
    		hostCursor = queryDb.query(GEN_TABLE_NAME, new String[] { GEN_FIELD__ID }, GEN_FIELD_ADDRESS  + " = ?", new String[] { host }, null, null, null);
    	if (hostCursor != null && hostCursor.moveToFirst())
    	{
    		Log.i(TAG, String.format(Locale.US, "Loding connection info from hostname: %s", host));
    		connection.Gen_populate(hostCursor, connection.Gen_columnIndices(hostCursor));
    		hostCursor.close();
    		database.close();
    		return connection;
    	}
    	if (hostCursor != null)
    		hostCursor.close();
		database.close();
    	return connection;
    }
    
    void parseFromUri(Uri dataUri) {
    	Log.i(TAG, "Parsing VNC URI.");
    	if (dataUri == null) {
    		m_isReadyForConnection = false;
    		m_saved = true;
    		return;
    	}
    	
    	String host = dataUri.getHost();
    	if (host != null) {
    		setAddress(host);
    		
    		// by default, the connection name is the host name
    		String nickName = getNickname();
    		if (Utils.isNullOrEmptry(nickName)) {
    			setNickname(host);
    		}
    		
    	}
    	
    	final int PORT_NONE = -1;
        int port = dataUri.getPort();
        if (port != PORT_NONE && !isValidPort(port)) {
        	throw new IllegalArgumentException("The specified VNC port is not valid.");
        }
        setPort(port);
        
    	// handle legacy android-vnc-viewer parsing vnc://host:port/colormodel/password            
        List<String> path = dataUri.getPathSegments();
        if (path.size() >= 1) {
            setColorModel(path.get(0));
        }
        
        if (path.size() >= 2) {
            setPassword(path.get(1));
        }
        
    	// query based parameters
        String connectionName = dataUri.getQueryParameter(Constants.PARAM_CONN_NAME);
        
        if (connectionName != null) {
        	setNickname(connectionName);
        }
        
    	ArrayList<String> supportedUserParams = new ArrayList<String>() {{
    	    add(Constants.PARAM_VNC_USER);
    	}};
    	for (String userParam : supportedUserParams) {
            String username = dataUri.getQueryParameter(userParam);
            if (username != null) {
                setUserName(username);
                break;
            }
        }
    	
        ArrayList<String> supportedPwdParams = new ArrayList<String>() {{
            add(Constants.PARAM_VNC_PWD);
        }};
        for (String pwdParam : supportedPwdParams) {
            String password = dataUri.getQueryParameter(pwdParam);
            if (password != null) {
                setPassword(password);
                break;
            }
        }
        
    	setKeepPassword(false); // we should not store the password unless it is encrypted
    	
    	String viewOnlyParam = dataUri.getQueryParameter(Constants.PARAM_VIEW_ONLY);
    	if (viewOnlyParam != null) setViewOnly(Boolean.parseBoolean(viewOnlyParam));
    	
    	String scaleModeParam = dataUri.getQueryParameter(Constants.PARAM_SCALE_MODE);
    	if (scaleModeParam != null) setScaleMode(ScaleType.valueOf(scaleModeParam));
    	
    	String extraKeysToggleParam = dataUri.getQueryParameter(Constants.PARAM_EXTRAKEYS_TOGGLE);
    	if (extraKeysToggleParam != null) setExtraKeysToggleType(Integer.parseInt(extraKeysToggleParam));
    	
    	// color model
    	String colorModelParam = dataUri.getQueryParameter(Constants.PARAM_COLORMODEL);
    	if (colorModelParam != null) {
    		int colorModel = Integer.parseInt(colorModelParam); // throw if invalid
    		switch (colorModel) {
    		case Constants.COLORMODEL_BLACK_AND_WHITE:
    			setColorModel(COLORMODEL.C2.nameString());
    			break;
    		case Constants.COLORMODEL_GREYSCALE:
    			setColorModel(COLORMODEL.C4.nameString());
    			break;
    		case Constants.COLORMODEL_8_COLORS:
    			setColorModel(COLORMODEL.C8.nameString());
    			break;
    		case Constants.COLORMODEL_64_COLORS:
    			setColorModel(COLORMODEL.C64.nameString());
    			break;
    		case Constants.COLORMODEL_256_COLORS:
    			setColorModel(COLORMODEL.C256.nameString());
    			break;
    			// use the best currently available model
    		case Constants.COLORMODEL_16BIT:
    			setColorModel(COLORMODEL.C24bit.nameString());
    			break;
    		case Constants.COLORMODEL_24BIT:
    			setColorModel(COLORMODEL.C24bit.nameString());
    			break;
    		case Constants.COLORMODEL_32BIT:
    			setColorModel(COLORMODEL.C24bit.nameString());
    			break;
    		default:
    			// we are given a bad parameter
    			throw new IllegalArgumentException("The specified color model is invalid or unsupported.");
    		}
    	}
    	String saveConnectionParam = dataUri.getQueryParameter(Constants.PARAM_SAVE_CONN);
    	boolean saveConnection = true;
    	if (saveConnectionParam != null) {
    		saveConnection = Boolean.parseBoolean(saveConnectionParam); // throw if invalid
    	}
    	
    	// if we are going to save the connection, we will do so here
    	// it may make sense to confirm overwriting data but is probably unnecessary
    	if (saveConnection) {
    		Database database = new Database(c);
    		save(database.getWritableDatabase());
    		database.close();
    		m_saved = true;
    	}
    	
    	// we do not currently use API keys
    	
    	// check if we need to show data-entry screen
    	// it may be possible to prompt for data later
    	m_isReadyForConnection = true;
    	if (Utils.isNullOrEmptry(getAddress())) {
    		m_isReadyForConnection = false;
    		Log.i(TAG, "URI missing remote address.");
    	}
    }
    
    boolean isValidPort(int port) {
    	final int PORT_MAX = 65535;
    	if (port <= 0 || port > PORT_MAX)
    		return false;
    	return true;
    }
    
    @Override
    public String toString() {
        if (isNew()) {
            return c.getString(R.string.new_connection);
        }
        String result = new String("");
        
        // Add the nickname if it has been set.
        if (!getNickname().equals("")) {
            result += getNickname()+":";
        }
        
        // Add the VNC server and port.
        result += getAddress()+":"+getPort();
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ConnectionBean another) {
        int result = getNickname().compareTo(another.getNickname());
        if (result == 0) {
            result = getAddress().compareTo(another.getAddress());
        }
        if ( result == 0) {
            result = getPort() - another.getPort();
        }
        return result;
    }
    
    /**
     * Return the object representing the app global state in the database, or null
     * if the object hasn't been set up yet
     * @param db VNCApp's database -- only needs to be readable
     * @return Object representing the single persistent instance of MostRecentBean, which
     * is the app's global state
     */
    public static MostRecentBean getMostRecent(SQLiteDatabase db) {
        ArrayList<MostRecentBean> recents = new ArrayList<MostRecentBean>(1);
        MostRecentBean.getAll(db, MostRecentBean.GEN_TABLE_NAME, recents, MostRecentBean.GEN_NEW);
        if (recents.size() == 0)
            return null;
        return recents.get(0);
    }
    
    public void saveAndWriteRecent(boolean saveEmpty, Database database) {
        
        // We need server address or SSH server to be filled out to save. Otherwise,
        // we keep adding empty connections. 
        // However, if there is partial data from a URI, we can present the edit screen. 
        // Alternately, perhaps we could process some extra data
        if ((getAddress().equals("")) && !saveEmpty) {
            return;
        }
        
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {
            save(db);
            MostRecentBean mostRecent = getMostRecent(db);
            if (mostRecent == null) {
                mostRecent = new MostRecentBean();
                mostRecent.setConnectionId(get_Id());
                mostRecent.Gen_insert(db);
            } else {
                mostRecent.setConnectionId(get_Id());
                mostRecent.Gen_update(db);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        if (db.isOpen()) {
            db.close();
        }
    }
}
