/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.connect;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.URLName;
import jakarta.mail.event.ConnectionListener;
import jakarta.mail.event.FolderListener;
import jakarta.mail.event.StoreListener;

import com.top_logic.basic.Logger;

/**
 * Debugging class for simulating a broken communication to the mail server ({@link #checkConnect(String)}.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TLStore extends Store {

    /** Error flag for disabling the connection. */
    private static boolean errorFlag;

    /** The store wrapped. */
    private Store store;

    private static Map<Folder, TLFolder> folders = new HashMap<>();

    private static TLStore defaultStore;

    /** 
     * Create a new instance of this class.
     */
    public TLStore(Session aSession, Store aStore) {
        super(aSession, aStore.getURLName());

        this.store = aStore;

        if (TLStore.defaultStore != null) {
            throw new IllegalStateException("There is already a store defined!");
        }

        TLStore.defaultStore = this;
    }

    @Override
	public Folder getDefaultFolder() throws MessagingException {
        TLStore.checkConnect("Store.getFolder()");

        return TLStore.getWrappedFolder(this.store.getDefaultFolder());
    }

    @Override
	public Folder getFolder(String arg0) throws MessagingException {
        TLStore.checkConnect("Store.getFolder(String)");

        return TLStore.getWrappedFolder(this.store.getFolder(arg0));
    }

    @Override
	public Folder getFolder(URLName arg0) throws MessagingException {
        TLStore.checkConnect("Store.getFolder(URLName)");

        return TLStore.getWrappedFolder(this.store.getFolder(arg0));
    }

    @Override
	public Folder[] getPersonalNamespaces() throws MessagingException {
        TLStore.checkConnect("Store.getPersonalNamespaces()");

        return TLStore.getWrappedFolders(this.store.getPersonalNamespaces());
    }

    @Override
	public Folder[] getUserNamespaces(String s) throws MessagingException {
        TLStore.checkConnect("Store.getUserNamespaces(String)");

        return TLStore.getWrappedFolders(this.store.getUserNamespaces(s));
    }

    @Override
	public Folder[] getSharedNamespaces() throws MessagingException {
        TLStore.checkConnect("Store.getSharedNamespaces()");

        return TLStore.getWrappedFolders(this.store.getSharedNamespaces());
    }

    @Override
	public synchronized void addStoreListener(StoreListener storelistener) {
        this.store.addStoreListener(storelistener);
    }

    @Override
	public synchronized void removeStoreListener(StoreListener storelistener) {
        this.store.removeStoreListener(storelistener);
    }


    @Override
	public synchronized void addFolderListener(FolderListener folderlistener) {
        this.store.addFolderListener(folderlistener);
    }

    @Override
	public synchronized void removeFolderListener(FolderListener folderlistener) {
        this.store.removeFolderListener(folderlistener);
    }

    @Override
	public void connect() throws MessagingException {
        TLStore.checkConnect("Store.connect()");

        this.store.connect();
    }

    @Override
	public void connect(String s, String s1, String s2) throws MessagingException {
        TLStore.checkConnect("Store.connect(String,String,String)");

        this.store.connect(s, s1, s2);
    }

    @Override
	public synchronized void connect(String s, int i, String s1, String s2) throws MessagingException {
        TLStore.checkConnect("Store.connect(String,int,String,String)");

        this.store.connect(s, i, s1, s2);
    }

    @Override
	public synchronized boolean isConnected() {
        return this.store.isConnected();
    }

    @Override
	public synchronized void close() throws MessagingException {
        if (!TLStore.errorFlag) {
            for (Iterator<Folder> theIt = TLStore.folders.keySet().iterator(); theIt.hasNext();) {
                Folder theFolder = theIt.next();

                try {
                    theFolder.close(true);
                }
                catch (Exception ex) {
                    Logger.info("Problem closing the folder " + theFolder, ex, this);
                }
            }

            this.store.close();
        }
        else {
            Logger.info("Connection is down (by errorFlag)!", this);
        }

        TLStore.defaultStore = null;
        TLStore.folders.clear();
    }

    @Override
	public synchronized URLName getURLName() {
        return this.store.getURLName();
    }

    @Override
	public synchronized void addConnectionListener(ConnectionListener connectionlistener) {
        this.store.addConnectionListener(connectionlistener);
    }

    @Override
	public synchronized void removeConnectionListener(ConnectionListener connectionlistener) {
        this.store.removeConnectionListener(connectionlistener);
    }

    @Override
	public boolean equals(Object obj) {
        if (obj instanceof TLStore) {
            return super.equals(obj);
        }
        else {
            return (this.store.equals(obj));
        }
    }

    @Override
	public int hashCode() {
        return this.store.hashCode();
    }

    /** 
     * Return the matching wrapped folder for the given one.
     * 
     * <p>When the given folder is unknown, a new wrapper will be created.</p>
     * 
     * @param    aFolder    Folder to get the wrapped version from.
     * @return   The requested wrapped version.
     */
    protected static TLFolder getWrappedFolder(Folder aFolder) {
    	TLFolder theFolder = TLStore.folders.get(aFolder);

        if (theFolder == null) {
            theFolder = new TLFolder(TLStore.defaultStore, aFolder);

            TLStore.folders.put(aFolder, theFolder);
        }

        return (theFolder);
    }

    /** 
     * Return the matching wrapped folders for the given one.
     * 
     * <p>When some of the given folders are unknown, new wrappers will be created.</p>
     * 
     * @param    someFolder    Folders to get the wrapped versions from.
     * @return   The requested wrapped versions.
     */
    protected static TLFolder[] getWrappedFolders(Folder[] someFolder) {
        if ((someFolder == null) || (someFolder.length == 0)) {
            return new TLFolder[0];
        }
        else {
        	TLFolder[] theResult = new TLFolder[someFolder.length];

            for (int thePos = 0; thePos < someFolder.length; thePos++) {
                theResult[thePos] = TLStore.getWrappedFolder(someFolder[thePos]);
            }

            return (theResult);
        }
    }

    /** 
     * Debug method for deactivating the connection to the store.
     * 
     * @param    aMethod    Method calling.
     * @throws   MessagingException    When connection is closed (for debugging issues).
     * @see      #setErrorFlag(boolean)
     */
    protected static void checkConnect(String aMethod) throws MessagingException {
        if (TLStore.errorFlag) {
            throw new MessagingException("Error flag set (now in method '" + aMethod + "')!");
        }
    }

    /** 
     * Current error flag (for debugging issues).
     * 
     * @return   The error flag.
     * @see      #setErrorFlag(boolean)
     */
    public static boolean getErrorFlag() {
        return TLStore.errorFlag;
    }

    /** 
     * Handle connection to store (for debugging issues).
     * 
     * @param   aValue    <code>true</code> for error situation.
     */
    public static void setErrorFlag(boolean aValue) {
        if (TLStore.defaultStore != null) {
            try {
                TLStore.defaultStore.close();
            }
            catch (MessagingException ex) {
                Logger.error("Unable to close connection to real store.", ex, TLStore.class);
            }
        }

        TLStore.errorFlag = aValue;
    }
}