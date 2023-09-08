
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.top_logic.basic.Logger;
import com.top_logic.dob.DataObject;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DataSourceAdaptor;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.evt.DataChangeEvent;
import com.top_logic.dsa.evt.DataChangeListener;
import com.top_logic.dsa.ex.DataChangeException;
import com.top_logic.dsa.ex.NotSupportedException;
import com.top_logic.dsa.util.MimeTypes;


/**
 * Defines the <i>TopLogic</i> database access scheme.
 *
 * @author  Karsten Buch / Klaus Halfmann
 */
public abstract class AbstractDataSourceAdaptor implements DataSourceAdaptor {

    /** The protocol of this adaptor. */
    private String protocol;

    /** The list of known listeners to this instance. */
    private Collection listeners;

    /** 
     * This method is called when the DataSourceAdaptor is not needed any more.
     * 
     * This method does nothing ...
     */
    @Override
	public void close() throws DatabaseAccessException {
        // does nothing ...
    }
        
    /**
     * Get the mime type associated with the element with the specified name.
     *
     * @param path the name to use; must not be null
     *
     * @return the mime type; null exactly when isEntry(aName) == false
     *
     * @throws DatabaseAccessException if a DatabaseAccessException occured
     *
     * #author Michael Eriksson
     */
     @Override
	public String getMimeType (String path) throws DatabaseAccessException {
         String theMimeType = null;

        if (this.isEntry (path)) {
            theMimeType = MimeTypes.getInstance ().getMimeType (path);
        }

        return theMimeType;
     }

    /**
     * Structured -&gt; use Dataobjects , Unstructured -&gt; use Strams.
     *
     * @return  false for now, since all old DSAs are unstructured.
     */
    @Override
	public boolean isStructured () {
        return false;
    }

    /**
     * Is the DataSourceAdaptor a Repository (supports locking and versioning)?
     *
     * @return  true if the underlying DataSourceAdaptor a Repository, false otherwise
     */
    @Override
	public boolean isRepository () {
        return false;
    }
    

    /**
     * Is the DataSourceAdaptor a Private (does not need creation of assoziations)?
     *
     * @return  true if the underlying DataSourceAdaptor a Repository, false otherwise
     */
    @Override
	public boolean isPrivate () {
        return(false);
    }


    /**
     * Does the element already exist?
     *
     * @param       path the name of the element
     * @return  true if the element exists
     *
     * @throws NotSupportedException always, ovveride with your actual implementation.
     */
    @Override
	public boolean exists (String path)
            throws DatabaseAccessException {
        throw new NotSupportedException ("exists is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Is the element an entry or a container?
     *
     * @param       path the name of the element
     * @return  true if it is a container
     *
     * @throws NotSupportedException always, ovveride with your actual implementation.
     */
    @Override
	public boolean isContainer (String path)
            throws DatabaseAccessException {
        return false;
    }

    /**
     * Is the element an entry or a container?
     *
     * @param       path the name of the element
     * @return  true if it is an entry
     *
     * @throws NotSupportedException always, ovveride with your actual implementation.
     */
    @Override
	public boolean isEntry (String path)
            throws DatabaseAccessException {
        return false;
    }

    /**
     * Get the name of the element (entry/container) in a representation
     * suitable for end users.
     *
     * @param       path      the name of the element
     * @return        the display name of the entry/container
     */
    @Override
	public String getDisplayName(String path) throws DatabaseAccessException {
        return getName (path);
    }

    /**
     * Get the name of the element (entry/container)
     *
     * @param       path the name of the element
     * @return  the name of the entry/container (not fully qualified)
     *
     * @throws NotSupportedException always, ovveride with your actual implementation.
     */
    @Override
	public String getName (String path)
            throws DatabaseAccessException {
        throw new NotSupportedException ("getName is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Rename the element (entry/container)
     *
     * @param       oldPath 		the fully qualified name of the element
	 * @param       newName 	the new element name of the element
     * @return  	 the full path to the renamed element
     *
     * @throws NotSupportedException always, ovveride with your actual implementation.
     */
    @Override
	public String rename (String oldPath, String newName)
        	throws DatabaseAccessException {
        throw new NotSupportedException ("rename is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Move some element or container to some other location.
     *
     * @param       oldPath	the fully qualified name of the element
	 * @param       newPath 	the path where the new Object will be found
     * @return  	 path to the new object, not necessary equal to oldPath
     *
     * @throws DatabaseAccessException always, not supported here
     */
	@Override
	public String move(String oldPath, String newPath)
		throws DatabaseAccessException {
        throw new NotSupportedException ("move is not supported by "
                                         + this.getClass ().getName ());
	}

    /**
     * Get the name of the parent container of the given element (entry/container)
     *
     * @param       path 	the name of the element
     * @return  	the name of the parent container (fully qualified)
     *
     * @throws NotSupportedException always, ovveride with your actual implementation.
     */
    @Override
	public String getParent (String path)
        	throws DatabaseAccessException {
		throw new NotSupportedException ("getParent() is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Create the complete path to a child for a given parent.
     *
     * @param       containerPath  complete path to the parent
     * @param       elementName    name of a (potential) child.
     * @return  	the complete path to the child.
     */
    @Override
	public String getChild (String containerPath, String elementName)
        throws DatabaseAccessException  {
    	throw new NotSupportedException ("getChild() is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Get an entry from a database.
     *
     * @param   path the name of the entry
     * @return  an InputStream for reading the entry's data
     *
     * @throws NotSupportedException always, override with your implementation
     */
    @Override
	public InputStream getEntry (String path)
            throws DatabaseAccessException {
        throw new NotSupportedException ("getEntry is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Get an entry as DataObject from a database.
     *
     * @param   path the name of the entry
     * @return  A dataObect representing the Entry
     *
     * @throws NotSupportedException always, override with your own implementation
     */
    @Override
	public DataObject getObjectEntry (String path)
        throws DatabaseAccessException {
        throw new NotSupportedException ("getObjectEntry is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Get an entry from a database.
     *
     * @param   path the name of the entry
     * @param   version the desired version of the entry
     * @return  an InputStream for reading the entry's data
     *
     * @throws NotSupportedException always, override with your own implementation
     */
    @Override
	public InputStream getEntry (String path, String version)
        	throws DatabaseAccessException {
        throw new NotSupportedException ("getEntry with version is not supported by "
                                         + this.getClass ().getName ());
	}

    /**
     * Get an entry as DataObject from a database.
     *
     * @param   path the name of the entry
     * @param   version the desired version of the entry
     * @return  A dataObect representing the Entry
     *
     * @throws NotSupportedException always, override with your own implementation
     */
    @Override
	public DataObject getObjectEntry (String path, String version) 
        throws DatabaseAccessException {
        throw new NotSupportedException (
            "getObjectEntry with version is not supported by "
                                 + this.getClass ().getName ());
    }

    /**
     * Put an entry in a database.
     *
     * @param       path the name of the element
     * @param       data the physical data given as an OutputStream
     *
     * @throws NotSupportedException always, override with your own implementation
     */
    @Override
	public void putEntry (String path, InputStream data)
            throws DatabaseAccessException {
        throw new NotSupportedException ("putEntry is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Put an entry in a database.
     *
     * @param       path the name of the element
     * @param       data The DataObejct holding the data.
     *
     * @throws NotSupportedException always, override with your own implementation
     */
    @Override
	public void putEntry (String path, DataObject data)
        throws DatabaseAccessException  {
        throw new NotSupportedException ("putEntry is not supported by "
                                         + this.getClass ().getName ());
    }
        

    /**
     * Put an entry in a container.
     *
     * @param elementName   the name/id for the new entry
     *
     * @return an Outputstream to the entry
     */
    @Override
	public OutputStream putEntry (String containerPath, String elementName)
        throws DatabaseAccessException  {
        throw new NotSupportedException ("putEntry is not supported by "
                                         + this.getClass ().getName ());            
    }



    /**
     * Get an OutputStream for putting an entry in a database.
     *
	 * @param	path	the name of the element
     * @return 	an OutputStream to write data into the entry
     *
     * @throws NotSupportedException always, override with your own implementation
     */
    @Override
	public OutputStream getEntryOutputStream (String path)
            throws DatabaseAccessException {
        throw new NotSupportedException ("getEntryOutputStream is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Get an OutputStream for appendding to an existing entry.
     *
     * @param   path    the name of the element
     * @return  an OutputStream to append data to the entry
      */
    @Override
	public OutputStream getEntryAppendStream (String path)
             throws DatabaseAccessException {
        throw new NotSupportedException ("getEntryAppendStream is not supported by "
                                     + this.getClass ());
    }

    /**
     * Get a URL string for redirecting to an entry.
     *
     * @param	path	the name of the element
     * @return a URL string or null if the entry is not reachable via a URL
     *
     * @throws NotSupportedException always, override with your own implementation
     */
    @Override
	public String getURL (String path)
			throws NotSupportedException {
        throw new NotSupportedException ("getURL is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Get a URL string for forwarding to an entry.
     *
     * @param	path	the name of the element
     * @return a URL string or null if the entry is not reachable via a URL
     *
     * @throws NotSupportedException always, override with your own implementation
     */
    @Override
	public String getForwardURL (String path)
    		throws NotSupportedException {
        throw new NotSupportedException ("getForwardURL is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Create a new entry in a container.
     *
     * @param elementName   the name/id for the new entry
     * @param data the physical data given as an OutputStream
     *
     * @return nothing, the function is not supported.
     *
     * @throws NotSupportedException always, the function is not supported.
     */
    @Override
	public String createEntry (String containerPath, String elementName, InputStream data)
            throws DatabaseAccessException {
        throw new NotSupportedException ("createEntry is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Create a new entry in a container.
     *
     * @param elementName   the name/id for the new entry
     *
     * @return an OutputStream to the new entry
     *
     * @throws NotSupportedException always, override with your own implementation
     */
    @Override
	public OutputStream createEntry (String containerPath, String elementName)
            throws DatabaseAccessException {
        throw new NotSupportedException ("createEntry is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Create a new entry name that is guaranteed not to be used by any of the existing children.
     * 
     * <code>this</code> must be a container.
     * The prefix and/or suffix may be ignored by the implementation.
     * The name is unique as of the time of creation. Any subsequent call
     * may or may not return the same name. The container is not actually
     * modified.
     * 
     * @param containerPath	 name of container where entry shall be created
     * @param prefix    a prefix used a hint for the new name.
     * @param suffix	 a suffix used a hint for the new name.
     * 
     * @return a String suiteable fore one of the <code>create...</code> Methods
     */
    @Override
	public String createNewEntryName (String containerPath, String prefix, String suffix)
        throws DatabaseAccessException {
        throw new NotSupportedException ("createNewEntryName is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Create a new DataObject as entry for the container.
     *<p>
     *  You must call putEntry() to finally store the object in the database.
     *</p>
     * @param containerPath  Name of the conat9ner to create the Object in.
     * @param elementName   the name/id for the new entry
     *
     * @return a DataObject suiteable to hold the structured data.
     *
     * @throws NotSupportedException always, override with your own implementation
     */
    @Override
	public DataObject createObjectEntry (String containerPath, String elementName)
        throws DatabaseAccessException {
        throw new NotSupportedException ("createObjectEntry is not supported by "
                                 + this.getClass ().getName ());
    }

        
    /**
     * Create the container in the current one.
     *
     * @param elementName   the name/id for the new container
     *
     * @return nothing, the function is not supported.
     *
     * @throws NotSupportedException always, the function is not supported.
     */
    @Override
	public String createContainer (String containerPath, String elementName)
            throws DatabaseAccessException {
        throw new NotSupportedException (
            "createContainer is not supported by "
            + this.getClass ().getName ());
    }

    @Override
	public void delete (String path, boolean force)
            throws DatabaseAccessException {
        throw new NotSupportedException ("delete is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Delete the container and all sub-elements (works for an entry as well).
     * Can delete non-empty containers. For entries it is equivalent to delete(String).
     *
     * @param       path the name of the element
     *
     * @throws NotSupportedException always, the function is not supported.
     */
    @Override
	public void deleteRecursively (String path)
            throws DatabaseAccessException {
        throw new NotSupportedException (
            "deleteRecursively is not supported by "
            + this.getClass ().getName ());
    }

    /**
     * Get the element names of the current container.
     *
     *     @param       path the name of the container.
     *     @return      an array of Strings with the fully qualified element names
     *
     * @throws NotSupportedException always, the function is not supported.
     */
    @Override
	public String[] getEntryNames (String path)
            throws DatabaseAccessException {
        throw new NotSupportedException ("getEntryNames is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Return an array of Strings identifiying all Version in the DSA.
     * 
     * @param     path   DS-Specific part of the DS-name
     * @return    the version strings from all entries.
     * @throws    DatabaseAccessException in case the entry path does not exits,
     *            is a container or in case of low level failures
     */
    @Override
	public String[] getVersions(String path) 
             throws DatabaseAccessException{
        return null;
    }

    /**
     * Get the database specific properties of an element.
     *
     * @param   path    the path of the element
     * @return  the database specific properties
     *
     * @throws NotSupportedException always, the function is not supported.
     */
    @Override
	public DataObject getProperties (String path)
            throws DatabaseAccessException {
        throw new NotSupportedException ("getProperties is not supported by "
                                         + this.getClass ().getName ());
    }
    
    /**
     * Get the database specific properties of an element.
     * The param 'version' specifies the version of the element
     * to get.
     *
     * @param   path        the path of the element
     * @param   version     the version of the element
     * @return  the database specific properties
     */
    @Override
	public DataObject getProperties (String path, String version)
            throws DatabaseAccessException {
        throw new NotSupportedException ("getProperties with a version is not" 
                                         + " supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Set the database specific properties of an element.
     *
     * @param       path the name of the element
     * @param       props   the database specific properties
     *
     * @throws NotSupportedException always, the function is not supported.
     */
    @Override
	public void setProperties (String path, DataObject props)
            throws DatabaseAccessException {
        throw new NotSupportedException ("setProperties is not supported by "
                                         + this.getClass ().getName ());
    }

    /**
     * Lock an element. The user locking the document has exclusive
	 * write access to the element.
     *
     * @param       path the name of the element
	 * @return		true, if lock was successful, false if lock failed
     *
     * @throws NotSupportedException always, the function is not supported.
     */
    @Override
	public boolean lock (String path)
        throws DatabaseAccessException {
        throw new NotSupportedException ("lock is not supported by "
                                         + this.getClass ().getName ());
	}

    /**
     * Unlock an element. Only the user who locked the document or
	 * an administrator is allowed to do this.
     *
     * @param       path the name of the element
     * @return		true, if unlock was successful, false if the element is locked but the current user is not the one who locked it
     *
     * @throws NotSupportedException always, the function is not supported.
     */
    @Override
	public boolean unlock (String path)
        throws DatabaseAccessException {
        throw new NotSupportedException ("unlock is not supported by "
                                         + this.getClass ().getName ());
	}

    /**
     * Set the name of the protocol used by this adaptor. This should not be
     * used by the adaptor itself but identify it.
     *
     * @param    newProtocol    The name of the protocol.
     */
    @Override
	public void setProtocol (String newProtocol) {
        this.protocol = newProtocol;
    }

    /**
     * Return the name of the protocol used by this adaptor.
     *
     * @return    The name of the protocol.
     */
    @Override
	public String getProtocol () {
        return (this.protocol);
    }

    /**
     * Appends a listener to this instance. If something happens (create, 
     * change or delete), this listeners will be notified.
     *
     * @param   aListener    The listener to be added.
     * @return  false when aListener was not added again
     */
    @Override
	public boolean addDataChangeListener (DataChangeListener aListener) {
        if (listeners == null)
            listeners = new ArrayList();

        if (!listeners.contains (aListener)) {
            listeners.add (aListener);
            return true;
        }
        return false;
    }

    /**
     * Removes the listener from this instance.
     *
     * @param    aListener    The listener to be removed.
     */
    @Override
	public boolean removeDataChangeListener (DataChangeListener aListener) {
        boolean result = false;
        if (listeners != null) {
            result = listeners.remove (aListener);
            if (listeners.size() == 0) {
                listeners = null;
            }
        }
        return result;
    }

    /**
     * Notifies all listeners, that a change will be executed.
     *
     * If one of the listeners do not agree with the change a
     * DataChangeException is thrown. If all listeners agree
     * or if there are no listeners, true is returned. If the
     * DataChangeEvent is not valid, false is returned.
     *
     * @param    anEvent holding the information about the change.
     * @return   true, if all listeners allow the change,
     *           or false if anEvent is null
     * @throws   DataChangeException    Thrown, when the change is not allowed,
     *                                  the contained message should explain
     *                                  the reason.
     */
    @Override
	public boolean fireCheckAllow (DataChangeEvent anEvent) 
                                                throws DataChangeException {
        if (this.listeners == null) {
            return true;   // No Listeners -> no Fire
        }
            
        if (anEvent == null) {
            Logger.info ("Ignore empty event and returning without " +
                         "notification!", this);
            return false;
        }

        boolean debug = Logger.isDebugEnabled (this);

        DataChangeListener theListener = null;
        Iterator           theIt       = this.listeners.iterator ();

        if (debug) {
            Logger.debug ("Start notification for event " + 
                          anEvent, this);
        }

        while (theIt.hasNext ()) {
            theListener = (DataChangeListener) theIt.next ();

            try {
                theListener.checkChangeAllow (anEvent);

                if (debug) {
                    Logger.debug ("Change allowed by listener " + 
                                  theListener, this);
                }
            } 
            catch (DataChangeException ex) {
                throw (ex);
            }
            catch (Exception ex) {
                Logger.warn ("Unable to send notification to " + theListener +
                             ", reason is: " , ex, this);
            }
        }

        if (debug) {
            Logger.debug ("Finished notification for event " + 
                          anEvent, this);
        }

        return true;
    }

    /**
     * Notifies all listeners, that the given changes have been done.
     *
     * @param    anEvent holding the information about the change.
     */
    @Override
	public void fireDataChanged (DataChangeEvent anEvent) {

        if (listeners == null)
            return;   // No Listeners -> no Fire
    
        if (anEvent == null) {
            Logger.info ("Ignore empty event and returning without " +
                         "notification!", this);
            return;
        }

        boolean debug = Logger.isDebugEnabled (this);

        DataChangeListener theListener = null;
        Iterator           theIt       = listeners.iterator ();

        if (debug) {
            Logger.debug ("Start notification for event " + 
                          anEvent, this);
        }

        while (theIt.hasNext ()) {
            try {
                theListener = (DataChangeListener) theIt.next ();

                theListener.dataChanged (anEvent);

                if (debug) {
                    Logger.debug ("Notified " + theListener, this);
                }
            } 
            catch (Exception ex) {
                Logger.warn ("Unable to send notification to " + theListener +
                             ", reason is: " , ex, this);
            }
        }

        if (debug) {
            Logger.debug ("Finished notification for event " + 
                          anEvent, this);
        }
    }

    /**
     * Returns the fully qualified name of the given source. This method
     * prepends the protocol to the given ID.
     *
     * @param    aName    The unique ID of the requested resource.
     * @return   The fully qualified path for the resource.
     */
    protected String getSourceName (String aName) {
        return this.getProtocol () + DataAccessProxy.SEPARATOR + aName;
    }


}
