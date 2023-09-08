/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa;

import java.io.InputStream;
import java.io.OutputStream;

import com.top_logic.dob.DataObject;
import com.top_logic.dsa.evt.DataChangeEvent;
import com.top_logic.dsa.evt.DataChangeListener;
import com.top_logic.dsa.ex.DataChangeException;
import com.top_logic.dsa.ex.NotSupportedException;

/** 
 * DataSource adaptors are gateways into storage areas.
 * 
 *<p>
 *  They are registered using the top-logic.xml file and can be accessed
 *  using a {@link  com.top_logic.dsa.DataAccessProxy} (which
 *  effectively hides the implementation.) There may be any number of
 *  proxies for a single instance of a DataSourceAdaptor (DSA). 
 *  The documentation here is mostly a copy from the one found
 *  in the DataAccessProxy. In case the documentation is unclear
 *  or contradictory see there.
 *</p><p>
 *  Historically DSAs where designed to support Filesystems like
 *  structures. So a container is usually seen as a folder and an entry
 *  is seen as a file. With never Implementations this is not always
 *  true and unexpected behavior of isEntry() and isContainer()
 *  can be found.
 *</p><p>
 *  DSA can be <em>structured</em> or <em>unstructured</em> {@link #isStructured()}
 *  depending on this value they support functions using
 *  {@link com.top_logic.dob.DataObject}s or streams to 
 *  access their content.
 *  BHU: This allows a tree-like view at Attribute value Pairs (DataObjects) 
 *</p> 
 *
 * @author  Karsten Buch / Klaus Halfmann
 */
public interface DataSourceAdaptor {

    /** 
     * This method is called if the DataSourceAdaptor is not needed any more.
     * 
     * The DSA should then free all its resources. Any further call to any
     * other method may result in an Exception. Calling close() twice should
     * however be possible.
     */
    public void close() throws DatabaseAccessException;
    
    /**
     * Get the MIME type associated with the element with the specified name.
     *
     * @param path the name to use; must not be null
     *
     * @return the MIME type; null exactly when isEntry(aName) == false
     *
     * @throws DatabaseAccessException if a DatabaseAccessException occurred
     *
     * #author Michael Eriksson
     */
    public String getMimeType (String path) throws DatabaseAccessException;
 
    /**
     * Structured -&gt; use {@link DataObject} , unstructured -&gt; use streams.
     *
     * @return  true when the Data is structured.
     */
    public boolean isStructured ();

    /**
     * Is the DataSourceAdaptor a repository (supports locking and versioning)?
     *
     * @return  true if the underlying DataSourceAdaptor is a repository, otherwise false
     */
    public boolean isRepository ();

    /**
     * A private DSA accesses different things based on the current used,
     *
     * @return  true when content is different based on the current user.
     */
    public boolean isPrivate ();

    /**
     * Does the element already exist?
     *
     * @param   path the name of the element
     * @return  true if the element exists
     */
    public boolean exists (String path)
        throws DatabaseAccessException;

    /**
     * A container is an element that may contain child elements.  
     *
     * A container may still be used to retrieve data.
     * Implies that getEntryNames() is supported, 
     * can be different from !isEntry().
     *
     * @param   path the name of the element
     * @return  true if it is a container
     */
    public boolean isContainer (String path)
        throws DatabaseAccessException;

    /**
	 * Implies that {@link #getEntry(String)} and {@link #putEntry(String, InputStream)} and similar
	 * functions are supported.
	 *
	 * The functions that must be supported are: {@link #getEntry(String)},
	 * {@link #getEntryOutputStream(String)}, {@link #putEntry(String, InputStream)}
	 *
	 * @param path
	 *        the name of the element.
	 * @return true if it is an entry, always false when !exists()
	 */
    public boolean isEntry (String path)
        throws DatabaseAccessException;

    /**
     * Get the name of the element (entry/container) in a way
     * suitable for end users.
     *
     * @param       path      the name of the element
     * @return        the display name of the entry/contain
     */
    public String getDisplayName(String path)
        throws DatabaseAccessException;

    /**
     * Return the last part of a potential path. This must work
     * independent of the existence of the entry/container
     *
     * The following assertion will hold:
     *<pre>
     * DSA x; String path;
     * path.equals(x.getChild(x.getParent(path), x.getName(path));
     *</pre>
     *
     * @param       path 	the name of the element
     * @return  	the name of the entry/container (not fully qualified)
     */
    public String getName (String path)
        throws DatabaseAccessException;

    /**
     * Rename the element (entry/container)
     *
     * @param       oldPath 		the fully qualified name of the element
	 * @param		newName 		the new element name of the element
     * @return  	the complete path of the renamed element.
     */
    public String rename (String oldPath, String newName)
        throws DatabaseAccessException;
	
    /**
     * Move some element or container to some other location.
     *
     * @param       oldPath	the fully qualified name of the element
	 * @param       newPath 	the path where the new Object will be found
     * @return  	 path to the new object, not necessary equal to oldPath
     */
    public String move (String oldPath, String newPath)
        throws DatabaseAccessException;
 
    /**
     * Get the name of the parent container of the given element (entry/container)
     *
     * The top-level container (empty String) will return null.
     * The following assertion will hold:
     *<pre>
     * DSA x; String path;
     * path.equals(x.getChild(x.getParent(path), x.getName(path));
     *</pre>
     * This function must work independent of the existence of 
     * entry or parent.
     *
     * @param       path 	the name of the element
     * @return  	the name of the parent container (fully qualified)
     */
    public String getParent (String path)
        throws DatabaseAccessException;

    /**
     * Create the complete path to a child for a given parent.
     *
     * The following assertion will hold:
     *<pre>
     * DSA x; String path;
     * path.equals(x.getChild(x.getParent(path), x.getName(path));
     *</pre>
     *
     * @param       containerPath  complete path to the parent
     * @param       elementName    name of a (potential) child.
     * @return  	the complete path to the child.
     */
    public String getChild (String containerPath, String elementName)
        throws DatabaseAccessException;

    /**
     * Get an entry from a database.
     *
     * @param   path the name of the entry
     * @return  an InputStream for reading the entry's data
     */
    public InputStream getEntry (String path)
        throws DatabaseAccessException;

    /**
     * Get an entry as DataObject from a database.
     *
     * @param   path the name of the entry
     * @return  A dataObect representing the Entry
     */
    public DataObject getObjectEntry (String path)
        throws DatabaseAccessException;

    /**
     * Get an entry from a database.
     *
     * @param   path the name of the entry
     * @param   version the desired version of the entry (format?)
     * @return  an InputStream for reading the entry's data
     */
    public InputStream getEntry (String path, String version)
        throws DatabaseAccessException;
		
    /**
     * Get an entry as DataObject from a database.
     *
     * @param   path the name of the entry
     * @param   version the desired version of the entry (format?)
     * @return  A DataObject representing the Entry
     */
    public DataObject getObjectEntry (String path, String version)
        throws DatabaseAccessException;


    /**
     * Put an entry in a database (UPDATE).
     *
     * The DSA will exhaust and then close the InputStream.
     * Will fail in case the element does not exist, or is
     * not an Entry. Structured DSA'S do not need to implement this function.
     *
     * @param       path the name of the element
     * @param       data the physical data given as an OutputStream
     */
    public void putEntry (String path, InputStream data)
        throws DatabaseAccessException;

    /**
     * Put an entry in a database (UPDATE).
     *
     * @param       path the name of the element
     * @param       data The DataObejct holding the data.
     */
    public void putEntry (String path, DataObject data)
        throws DatabaseAccessException;

    /**
     * Put an entry in a container.
     *
     * @param elementName   the name/id for the new entry
     *
     * @return an OutputStream to the entry
     */
    public OutputStream putEntry (String containerPath, String elementName)
                                                throws DatabaseAccessException;

    /**
     * Get an OutputStream for putting an entry in a database.
     *
	 * @param	path	the name of the element
     * @return 	an OutputStream to write data into the entry
     */
    public OutputStream getEntryOutputStream (String path)
            throws DatabaseAccessException;

    /**
     * Get an OutputStream for appending to an existing entry.
     *
     * @param   path    the name of the element
     * @return  an OutputStream to append data to the entry
     */
    public OutputStream getEntryAppendStream (String path)
            throws DatabaseAccessException;

    /**
     * Get a URL string for redirecting to an entry.
     *
     * @param	path	the name of the element
     * @return 	a URL string or null if the entry is not reachable via a URL
     *
     * @throws NotSupportedException in case no URL to the data can be provided.
     */
    public String getURL (String path)
			throws NotSupportedException;

	/**
	 * Get a URL string for forwarding to an entry.
	 *
	 * @param	path	the name of the element
	 * @return 	a URL string or null if the entry is not reachable via a URL
	 *
     * @throws NotSupportedException in case no URL to forward to can be provided.
	 */
	public String getForwardURL (String path)
			throws NotSupportedException;


    /**
     * Create a new entry in a container.
     *
     * @param elementName   the name/id for the new entry
     * @param data the physical data given as an InputStream 
     * 			 The stream will be exhausted and close()d.
     */
    public String createEntry (String containerPath, String elementName, InputStream data)
        throws DatabaseAccessException;

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
     * @return a String suitable fore one of the <code>create...</code> Methods
     */
    public String createNewEntryName (String containerPath, String prefix, String suffix)
        throws DatabaseAccessException;

    /**
     * Create a new DataObject as entry for the container.
     *<p>
     *  You must call putEntry() to finally store the object in the database.
     *</p>
     * @param containerPath  Name of the conat9ner to create the Object in.
     * @param elementName   the name/id for the new entry
     *
     * @return a DataObject suitable to hold the structured data.
     */
    public DataObject createObjectEntry (String containerPath, String elementName)
        throws DatabaseAccessException;

    /**
     * Create a new entry in a container.
     *
     * @param elementName   the name/id for the new entry
     *
     * @return an OutputStream to the new entry
     */
    public OutputStream createEntry (String containerPath, String elementName)
        throws DatabaseAccessException;
        

    /**
     * Create a new container in the given parent container.
     *
     * @param containerPath	the fully qualified name of the parent container
     * @param elementName   the name/id for the new container
     *
     * @return	the fully qualified name of the created container
     */
    public String createContainer (String containerPath, String elementName)
        throws DatabaseAccessException;

    /**
     * Delete an element from a database. If the element is a container, it is
     * deleted only if it is empty.
     *
     * @param   path the name of the element
     * @param force 
     *        Whether locks are automatically broken. See {@link #unlock(String)}.
     */
    public void delete (String path, boolean force)
        throws DatabaseAccessException;

    /**
     * Delete the container and all sub-elements (works for an entry as well).
     * Can delete non-empty containers. For entries it is equivalent to delete(String).
     *
     * @param       path the name of the element
     */
    public void deleteRecursively (String path)
        throws DatabaseAccessException;

    /**
	 * Get the element names of the current container.
	 *
	 * @param path
	 *        the name of the container.
	 * @return an array of Strings with the fully qualified element names, may be null if no
	 *         elements exist.
	 */
    public String[] getEntryNames (String path)
        throws DatabaseAccessException;

    /**
     * Return an array of Strings identifying all Version in the DSA.
     * 
     * @param     path   DS-Specific part of the DS-name
     * @return    the version strings from all entries.
     * @throws    DatabaseAccessException in case the entry path does not exits,
     *            is a container or in case of low level failures
     */
    public String[] getVersions(String path) throws DatabaseAccessException;

    /**
     * Get the database specific properties of an element.
     *
     * @param   path    the path of the element
     * @return  the database specific properties
     */
    public DataObject getProperties (String path)
        throws DatabaseAccessException;

    /**
     * Get the database specific properties of an element.
     * The param 'version' specifies the version of the element
     * to get.
     *
     * @param   path        the path of the element
     * @param   version     the version of the element
     * @return  the database specific properties
     */
    public DataObject getProperties (String path, String version)
        throws DatabaseAccessException;

    /**
     * Set the database specific properties of an element.
     *
     * @param       path the name of the element
     * @param       props   the database specific properties
     */
    public void setProperties (String path, DataObject props)
        throws DatabaseAccessException;

    /**
     * Lock an element. The user locking the document has exclusive
	 * write access to the element.
     *
     * @param       path the name of the element
	 * @return		true, if lock was successful, false if lock failed
     */
    public boolean lock (String path)
        throws DatabaseAccessException;

    /**
     * Unlock an element. Only the user who locked the document or
	 * an administrator is allowed to do this.
     *
     * @param       path the name of the element
     * @return		true, if unlock was successful, false if the element is locked but the current user is not the one who locked it
     */
    public boolean unlock (String path)
        throws DatabaseAccessException;

    /**
     * Set the name of the protocol used by this adaptor. This should not be
     * used by the adaptor itself but identify it.
     *
     * @param    newProtocol    The name of the protocol.
     */
    public void setProtocol (String newProtocol);

    /**
     * Return the name of the protocol used by this adaptor.
     *
     * The protocol used by the accessing DAP. An implementation should
     * never depend on it in any way.
     *
     * @return    The name of the protocol.
     * @see       #setProtocol(String)
     */
    public String getProtocol ();

    /**
     * Appends a listener to this instance. If something happens (create, 
     * change or delete), this listeners will be notified.
     * 
     * @return   false when aListener was registered before.
     *
     * @param    aListener    The listener to be added.
     */
    public boolean addDataChangeListener (DataChangeListener aListener);

    /**
     * Removes the listener from this instance.
     *
     * @param    aListener    The listener to be removed.
     * @return   true when aListener was actually registered.
     */
    public boolean removeDataChangeListener (DataChangeListener aListener);


    /**
     * Notifies all listeners, that the given changes have been done.
     *
     * @param anEvent holding the information about the change.
     */
    public void fireDataChanged (DataChangeEvent anEvent);

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
     *           or false if anEvent is null.
     * @throws   DataChangeException    Thrown, when the change is not allowed,
     *                                  the contained message should explain
     *                                  the reason.
     */
    public boolean fireCheckAllow (DataChangeEvent anEvent) 
                                                    throws DataChangeException;
}
