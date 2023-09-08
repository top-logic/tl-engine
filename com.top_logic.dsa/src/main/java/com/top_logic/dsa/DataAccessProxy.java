/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import com.top_logic.basic.Logger;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.dob.DataObject;
import com.top_logic.dsa.evt.DataChangeEvent;
import com.top_logic.dsa.ex.DataChangeException;
import com.top_logic.dsa.ex.NotSupportedException;
import com.top_logic.dsa.ex.UnknownDBException;

/**
 * Main access point to the <i>TopLogic</i> data-access scheme.
 *<p>
 *  Every datasource- (DS-) name starts with a unique name
 *  called the protocol . After The Protocol follows a
 *  separator (:// {@link com.top_logic.dsa.DataAccessProxy#SEPARATOR} )
 *  all entries after the protocoll are specific for the DataSource accessed.
 *  For every protocoll there is one instance of a
 *  {@link com.top_logic.dsa.DataSourceAdaptor} representing
 *  the Data as a whole.
 *</p><p>
 *  This class is a proxy for DataSourceAdaptors. On the one hand it
 *  denies any direct access to the DataSourceAdaptors. This way any
 *  attempt to cast them cant work. On the Other hand it represents
 *  an Element of a DataSourceAdaptor identified by the string following
 *  the seperator.
 *</p>
 *<p>
 *  DAPs are immutable. The empty String (after the protocoll) identifies the
 *  top-level container.
 *</p>
 *  <h4>Features / Problems</h4>
 *<p>
 *  For every seperate entry of a DS a new (DataAccessProxy) DAP must
 *  be created. The syntax and meaning of the Strings following the
 *  separartor is very specific. Strings that might work with one will
 *  not work with some other. The exact results and behaviours
 *  of some functions are very specific to the DSA. 
 *</p><p>
 *  Although the DS-names look like URLs/URIs there meaning is very
 *  different.
 *</p>
 *
 * history 2001-11-23   kha reviewed and enhanced Documentation.
 *
 * @author  Karsten Buch / Klaus Halfmann
 */
public class DataAccessProxy {

    /**
     * Separator used to separate the DSA name from the DSA-specific path
     */
    public static final String SEPARATOR = "://";

    /** Length of the Separator for String operations */
    public static final int SEPARATOR_LEN = SEPARATOR.length();

    /**
     * DS-Specific part of the DS-name.
     *
     *  E.g. suelz/laber?here=there (blah://suelz/laber?here=there)
     */
    private final String path;

    /** The DataSourceAdaptor actually found for the protocol
     */
    private final DataSourceAdaptor dsa;

    /**
     * Constructor used internally to return Proxies.
     *
     * @param aDsa   the DataSourceAdaptor used
     * @param aPath  the path inside the dsa.
     */
    private DataAccessProxy(DataSourceAdaptor aDsa, String aPath) {
        this.path = aPath;
        this.dsa  = aDsa;
    }

    /**
     * Constructor to create a DataAccessProxy from a given entry name.
     *
     * @param uri the fully qualified DS-name e.g. blah://suelz/laber?here=there
     *
     * @throws UnknownDBException in case protocol of datasource is not known.
     */
    public DataAccessProxy(String uri) throws UnknownDBException {

        if (uri == null) {
            throw new IllegalArgumentException("Name must not be null");
        }

        int separatorPos = uri.indexOf(SEPARATOR);
        if (separatorPos == -1) {
        	// separator not found -> invalid
        	throw new UnknownDBException("Not a valid DataSource name: + '" + uri + '"');
        }

        String protocol = uri.substring(0, separatorPos);
        dsa = DataAccessService.getInstance().getDataSourceByName(protocol);
        path = uri.substring(separatorPos + SEPARATOR_LEN);
    }

    /**
     * Constructor to create a DataAccessProxy from a given container and entry name.
     *
     * @param uri      the fully qualified DS-name e.g.  blah:// (denoting the top-level container)
     * @param childName	    the name of an element (container/entry) in the container denoted by contName
     *
     * @throws UnknownDBException in case contName uses a unknow protocol
     */
    public DataAccessProxy(String uri, String childName)
        throws UnknownDBException, DatabaseAccessException {

        int separatorPos = uri.indexOf(SEPARATOR);
        if (separatorPos == -1) {
        	// separator not found -> invalid
        	throw new UnknownDBException("Not a valid DataSource name: + '" + uri + '"');
        }

        String protocol = uri.substring(0, separatorPos);
        dsa = DataAccessService.getInstance().getDataSourceByName(protocol);
        
        String parentPath = uri.substring(separatorPos + SEPARATOR_LEN);
        path = dsa.getChild(parentPath, childName);
    }

    /**
     * Constructor to create a DataAccessProxy from a given container and entry name.
     *
     * @param container DataAccessProxy specifiying the parent container.
     * @param childName	    the name of an element (container/entry) in the container denoted by container
     *
     * @throws DatabaseAccessException based on the actual implementation.
     */
    public DataAccessProxy(DataAccessProxy container, String childName)
        throws DatabaseAccessException {

        dsa = container.dsa;
        path = dsa.getChild(container.path, childName);
    }

    /**
     * Constructor to return a DataAccessProxy from the current container and entry name.
     *
     * @param childName	the name of an element (container/entry) in the container.
     *
     * @throws DatabaseAccessException based on the actual implementation.
     */
    public DataAccessProxy getChildProxy(String childName)
        throws DatabaseAccessException {
        return new DataAccessProxy(this, childName);
    }

    /**
     * Delivers the description of the instance of this class.
     *
     * @return    A description for debugging.
     */
    @Override
	public String toString() {
		// Note: This value is printed in the application monitor to identify the repository.
		return dsa + "/" + path;
    }

    /** Standard equals semantics, relies on correct equals semantics of DSA.
     */
    @Override
	public boolean equals(Object other) {
    	if (other == this) {
    		return true;
    	}
        if (other instanceof DataAccessProxy) {
            DataAccessProxy dap = (DataAccessProxy) other;
            return dsa.equals(dap.dsa) && path.equals(dap.path);
        }
        return false;
    }

    /** Standard hashCode semantics, relies on correct hashCode semantics of DSA.
     */
    @Override
	public int hashCode() {
        return dsa.hashCode() ^ path.hashCode();
    }

    /**
     * Get the mime type associated with this instance.
     * <p>
     *   KHA: this does not allow to get a MIME-TYPE for
     *   a file that does not yet exists, mmh.
     * </p>
     *
     * @return the mime type or null when !isEntry () or 
     * 								  no mimetyp could be found.
     *
     * @throws DatabaseAccessException based on the actual implementation.
     *
     * #author Michael Eriksson
     */
    public String getMimeType() throws DatabaseAccessException {
        return dsa.getMimeType(path);
    }

    /**
     * Structured -&gt; use Dataobjects , Unstructured -&gt; use Strams.
     *
     * @return  true when the Data is structured.
     */
    public boolean isStructured() {
        return dsa.isStructured();
    }

    /**
     * Is the DataSourceAdaptor a Repository (supports locking and versioning) ?
     *
     * @return  true if the underlying DataSourceAdaptor a Repository, false otherwise
     */
    public boolean isRepository() {
        return dsa.isRepository();
    }

    /**
     * true when the underlying DSA is differnt per (current) User.
     *
     * E.G. users JZA and DKO will see different things when accesing the
     * same name.
     *
     * @return  true if the underlying DataSourceAdaptor is per user.
     */
    public boolean isPrivate() {
        return dsa.isPrivate();
    }

    /**
     * Whether this element exists.
     *
     * <p>
     * Note: Even deleted versions exist in a repository and will be answered with <code>true</code>.
     * </p>
     *
     * @return Whether this element exists.
     */
    public boolean exists() throws DatabaseAccessException {
    	return dsa.exists(this.path);
    }

    /**
     * A Container is an element that may contain child elements.  
     *
     * A container may still be used to retrieve data.
     * This implies that getEntryNames() is supported.
     * This can be different from !isEntry().
     * 
     * @return  true if it is a container, always false when !exists()
     */
    public boolean isContainer() throws DatabaseAccessException {
        return dsa.isContainer(path);
    }

    /**
     * Implies that get/putEntry() and similar functions are supported.
     *
     * The fucntions that must be supported are:
     *      getEntry(), getEntryOutputStream(), putEntry() 
     *
     * @return  true if it is an entry, always false when !exists()
     */
    public boolean isEntry() throws DatabaseAccessException {
        return dsa.isEntry(path);
    }

    /**
     * Return the last part of a potential path represented by the DSA.
     * This must work independent of the existance of the entry/container.
     *
     * The follwoing assertion will hold:
     *<pre>
     * DAP x = ...;
     * DAP y = new DataAccesProxy(x.getParent(), x.getName());
     * x.equals(y);
     *</pre>
     *
     * @return  the name of the entry/container 
     */
    public String getName() throws DatabaseAccessException {
        return dsa.getName(path);
    }

    /**
     * This function is DS-specific and may return any value (?)
     *
     * @return  the name of the entry/container in a representation suitable for end users.
     */
    public String getDisplayName() throws DatabaseAccessException {
        return dsa.getDisplayName(path);
    }

    /** Will try to rename the element represented by this class to the
     *  given name.
     *<p>
     *  The exact implementation of this function is DS-specific.
     *  It should not allow moving around in the container hierarchy.
     *  Expect it to fail for seome Exotic DSAs.
     *</p>
     * @param   aNewName that is suiteable for an entry, 
     *          you should avoid any special characters.
     * @return  a DataAccessProxy for the new, renamed entry.
     */
    public DataAccessProxy rename(String aNewName)
        throws DatabaseAccessException {
        return new DataAccessProxy(dsa, dsa.rename(path, aNewName));
    }

    /** Will try to move the element or container represented by this class 
     *  to the given complete path.
     *<p>
     *  The exact implementation of this function is DS-specific.
     *  The returned Proxy may have some other (e.g. normalized)
     *  path to the new location.
     *</p>
     * @param   aNewPath    complete path without protocol,
     * @return  a DataAccessProxy for the new, moved entry.
     *
     * @throws DatabaseAccessException in case the move fails
     */
    public DataAccessProxy move(String aNewPath)
        throws DatabaseAccessException {
        return new DataAccessProxy(dsa, dsa.move(path, aNewPath));
    }

    /**
     * Get the fully qualified name of the DAP.
     *
     * This name can be used to recreate a DAP wich is equal to this one.
     * 
     * new DataAccessProxy(dap.getPath()).equals(dap) is always true.
     *
     * @return a string containing the fully qualified name of the DAP.
     *         null in case of any error.
     */
    public String getPath() {
        return dsa.getProtocol() + SEPARATOR + path;
    }

    /**
     * Get the name of the parent container of the currenmt one.
     *
     * The top-level container (empty String) will return null.
     * The following assertion will hold:
     *<pre>
     * DAP x = ...;
     * DAP y = new DataAccesProxy(x.getParent(), x.getName);
     * x.equals(y);
     *</pre>
     * This function must work independant of the existance of 
     * entry or parent.
     *
     * @return the fully qualified name of the parent container.
     */
    public String getParent() throws DatabaseAccessException {

        String theParent = dsa.getParent(path);
        if (theParent != null)
            return dsa.getProtocol() + SEPARATOR + theParent;
        else
            return theParent;
    }

    /** Return the Protcol part of the original DataSourceName. */
    public final String getProtocol() {
        return dsa.getProtocol();
    }

    /**
     * Get a Proxy for the parent container.
     *
     * The top-level container (empty String) will return null.
     * The following assertion will hold:
     *
     * DAP x = ...;
     * DAP y = new DataAccesProxy(x.getParent(), x.getName);
     * x.equals(y);
     *
     * @return the a DAP to the parnet Cotainer or null for the root-Container
     */
    public DataAccessProxy getParentProxy() throws DatabaseAccessException {
        String parentPath = dsa.getParent(path);
        if (parentPath != null)
            return new DataAccessProxy(dsa, parentPath);
        else
            return null;
    }

    /**
     * Get the unstructured data from the datasource.
     *
     * in case of !isEntry() an Exception will be thrown.
     * A repository will return the current (top-level) version.
     * A structured DSA will return an XML-Representation suitebale for
     * recreation of the Data. 
     *
     * @return an InputStream for reading the entry's data, never null. 
     *          <strong>.close()</strong> as soon as possible.
     */
    public InputStream getEntry() throws DatabaseAccessException {
        return dsa.getEntry(path);
    }

	/**
	 * The unstructured data from the datasource.
	 * 
	 * @see #getEntry()
	 */
	public BinaryContent getContent() {
		return new BinaryContent() {
			@Override
			public String toString() {
				return getProtocol() + ":" + getPath();
			}

			@Override
			public InputStream getStream() throws IOException {
				return getEntry();
			}
		};
	}

    /**
     * Get an entry from a repository Datasource.
     *
     * Will throw NotSupportException in case the DSA is
     * not a repository.
     *
     * @param   version the desired version of the entry
     *          What formats are allowed here ?
     * @return  an InputStream for reading the entry's data, never null
     *          <strong>.close()</strong> as soon as possible.
     */
    public InputStream getEntry(String version)
        throws DatabaseAccessException {
        return dsa.getEntry(path, version);
    }

    /**
     * Get an entry as DataObject from a database.
     *
     * @return  A dataObject representing the current entry.
     *
     * @throws DatabaseAccessException or one of its SubExecptions
     */
    public DataObject getObjectEntry() throws DatabaseAccessException {
        return dsa.getObjectEntry(path);
    }

    /**
     * Get an entry as DataObject from a database.
     *
     * @param   version    The desired version of the entry (format?).
     * @return  A DataObect representing the entry.
     *
     * @throws DatabaseAccessException or one of its SubExecptions
     */
    public DataObject getObjectEntry(String version)
        throws DatabaseAccessException {
        return dsa.getObjectEntry(path, version);
    }

	/**
	 * Put an entry in a database (UPDATE).
	 * <p>
	 * The DSA will exhaust the {@link InputStream}. Will fail in case the
	 * element does not exist, or is not an Entry. Structured DSAs do not need
	 * to implement this function.
	 * </p>
	 * 
	 * @param data
	 *        the physical data given as an InputStream. must not be <code>null</code>
	 */
    public void putEntry(InputStream data) throws DatabaseAccessException {
        dsa.putEntry(path, data);
    }

    /**
     * Put an entry in a database (UPDATE).
     * <p>
     *   The DSA will exhaust and then close the InputStream.
     *   Will fail in case the Element does not exist, or is
     *   not an Entry.
     *   Structured DSA do not need to implent this function.
     * </p>
     *
     * @param data The DataObject holding the data.
     */
    public void putEntry(DataObject data) throws DatabaseAccessException {
        dsa.putEntry(path, data);
    }

    /**
     * Get an OutputStream for putting an entry in a database.
     *<p>
     *  non-existant -&gt; create new Entry. <br />
     *  For repository -&gt; create new Revision unlock if locked (after Stream was closed ...)
     *  Not entry -&gt; Exception.
     *</p>
     *
     * @return an OutputStream to write data into the entry
     *         remember to <strong>.close()</strong> the Stream as soon as possible.
     */
    public OutputStream getEntryOutputStream() throws DatabaseAccessException {
        return dsa.getEntryOutputStream(path);
    }

    /**
     * Get an OutputStream in append mode for putting an entry in a database.
     *<p>
     *  non-existant -&gt; create new Entry. <br />
     *  For repository -&gt; create new Revision unlock if locked (after Stream was closed ...)
     *  Not entry -&gt; Exception.
     *</p>
     *
     * @return an OutputStream to write data into the entry
     *         remember to <strong>.close()</strong> the Stream as soon as possible.
     */
    public OutputStream getEntryAppendStream() throws DatabaseAccessException {
        return dsa.getEntryAppendStream(path);
    }

    /**
     * Get a URL string for redirecting to an entry (via a HTML-browser ?). 
     *
     * In case the DSA has accesible files in some webcontext an URL
     * to the file is returned. This is for speed reasons. Another problem
     * are local references from E.g. HTML-files. You might use the 
     * ResourceViewerSerlvet in case this function returns null. 
     * The result is suitable for HttpServletResponse.sendRedirect().
     *
     * See <code>com.top_logic.util.ResourceViewerServlet</code>
     *
     * @return a URL string or null if the entry is not reachable via a URL
     */
    public String getURL() throws NotSupportedException {
        return dsa.getURL(path);
    }

    /**
     * Get a URL string for forwarding to an entry (eg via a jsp:forward).
     *
     * In case the DSA has accesible files in the top-logc web-context an URL
     * to the file is returned. This is for speed reasons. Another problem
     * are local references from E.g. HTML-files. You might use the 
     * ResourceViewerSerlvet in case this function returns null.
     *
     * @return a URL string or null if the entry is not reachable via a URL
     *
     * See <code>com.top_logic.util.ResourceViewerServlet</code>
     */
    public String getForwardURL() throws NotSupportedException {
        return dsa.getForwardURL(path);
    }

    /**
     * Create a new entry in a container (INSERT).
     * <p>
     *   The DSA will exhaust and then close the InputStream.
     *   Will throw Exception in case Element already exist
     *   (even for Repositories)
     * </p>
     * @param entryName the name/id for the entry
     * @param data	the InputStream with the data to be stored in the created entry.
     *
     * @return  the Universal name of the newly created entry.
     */
    public String createEntry(String entryName, InputStream data)
        throws DatabaseAccessException {
        String name = dsa.createEntry(path, entryName, data);
        return dsa.getProtocol() + SEPARATOR + name;
    }

    /**
         * Create a new entry in a container (INSERT).
         * <p>
         *   The DSA will exhaust and then close the InputStream.
         *   Will throw Exception in case Element already exist
         *   (even for Repositories)
         * </p>
         * @param entryName the name/id for the entry
         * @param data  the InputStream with the data to be stored in the created entry.
         *
         * @return  the new DataAccessProxy of the created entry.
         */
    public DataAccessProxy createEntryProxy(String entryName, InputStream data)
        throws DatabaseAccessException {
        String childPath = dsa.createEntry(path, entryName, data);
        return new DataAccessProxy(dsa, childPath);
    }

    /**
     * Create a new entry in a container (INSERT)
     * <p>
     *   Will throw Exception in case Element already exist.
     *   A Respository will automatically create a new, unlocked Version.
     * </p>
     * @param id        the name/id for the entry
     *
     * @return  an OutputStream to the the newly created entry.
     *          remember to <strong>.close()</strong> the Stream as soon as possible.
     */
    public OutputStream createEntry(String id) throws DatabaseAccessException {
        return dsa.createEntry(path, id);
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
     * @since Version_3_2
     *
     * @param prefix    a prefix used as a hint for the new name, must not be null
     * @param suffix    a suffix used as a hint for the new name, may be null
     * 
     * @return a String suiteable fore one of the <code>create...</code> Methods
     */
    public String createNewEntryName(String prefix, String suffix)
        throws DatabaseAccessException {
        return dsa.createNewEntryName(path, prefix, suffix);
    }

    /**
     * Create a container in the current one.
     *
     * Will throw Exeption in case such an Element already exists.
     *
     * @param id specified name/id for container.
     *
     * @return the complete path (getPath()) to the new Container
     * 
     * @throws DatabaseAccessException 
     *         i.e. in case the container with the id already exists
     */
    public String createContainer(String id) throws DatabaseAccessException {
        String contName = dsa.createContainer(path, id);
        return dsa.getProtocol() + SEPARATOR + contName;
    }

    /**
     * Create a container in the current one.
     *
     * Will throw Exeption in case such an Element already exists.
     *
     * @param id specified name/id for container.
     *
     * @return a DataAccessProxy respresenting the new Container
     */
    public DataAccessProxy createContainerProxy(String id)
        throws DatabaseAccessException {
        String childPath = dsa.createContainer(path, id);
        return new DataAccessProxy(dsa, childPath);
    }

    /**
     * Delete an element from a DataSource.
     *
     * Repositories will mark the current Version as deleted.
     * Locked Entries cannot be delted from other users.
     * Only empty Containers can be delted {@link #deleteRecursively() }.
     *
     * TODO KHA return true/false on succes/failure
     * @param force 
     *        Whether to automatically break locks. See {@link #unlock()}.
     */
    public void delete(boolean force) throws DatabaseAccessException {
        dsa.delete(path, force);
    }

    /**
     * Delete the container and all sub-elements (works for an entry as well).
     *
     * A Repository will mark the folder as deleted. It can be
     * resurrected via createContainer().
     */
    public void deleteRecursively() throws DatabaseAccessException {
        dsa.deleteRecursively(path);
    }

    /**
     * Get the full path to all elemnts of the current container.
     *
     * To create a new DAP to an Entry use new DAP(entry[i])
     *
     * @return an array of Strings, or null in case there are no entries.
     */
    public String[] getEntryNames() throws DatabaseAccessException {
        String[] entryNames = dsa.getEntryNames(path);
        int l;
        if (entryNames != null && (l = entryNames.length) > 0) {
            String protocol = dsa.getProtocol();
            for (int i = 0; i < l; i++) {
                entryNames[i] = protocol + SEPARATOR + entryNames[i];
            }
        }
        return entryNames;
    }

    /**
     * Get a List of DataAccessProxies as child of the current one.
     *
     * Use this in favour of getEntryNames() in case you are goind to 
     * access all the child anyway. Do not use it in case you just want to
     * display the names to the user.
     *
     * @return an array of DataAccessProxies, or null in case there are no entries.
     */
    public DataAccessProxy[] getEntries() throws DatabaseAccessException {
        String[] entryPaths = dsa.getEntryNames(path);
        DataAccessProxy[] result = null;
        int l;
        if (entryPaths != null && (l = entryPaths.length) > 0) {
            result = new DataAccessProxy[l];
            for (int i = 0; i < l; i++) {
                result[i] = new DataAccessProxy(dsa, entryPaths[i]);
            }
        }
        return result;
    }

    /* add public Iterator getEntries() // Iterator of DAPs */

    /**
     * Return an array of Strings identifiying all Version in the DSA.
     * 
     * The most recent Version will be <b>first</b>. The order of Versions
     * is descending.
     * 
     * @return    <code>null</code> when DSA is no repository,
     *            empty Array must not happen.
     * @throws    DatabaseAccessException in case the entry path does not exits,
     *            is a container or in case of low level failures
     */
    public String[] getVersions() throws DatabaseAccessException {
        return dsa.getVersions(this.path);
    }

    /**
     * Get the database specific properties of an element.
     *
     * <p>
     * These properties are <em>very</em> specific to the
     * DSA. You cannot assume anything about the Object returned.
     * </p>
     * <p>
     * For versioned entries this method is equivalent to
     * <code>getProperties(&lt;latestVersion&gt;)</code>.
     * </p>
     * <p>
     * The following Properties <em>may</em> exist. If they do they
     * <em>must</em> have the specified meaning and type.
     * Further, if there is a property with the specified meaning
     * it <em>must</em> be made available with this name and type.
     * Other properties with the same value (possibly with a different type)
     * <em>may</em> exist.
     * </p>
     * <dl>
     *  <dd>size</dd>
     *  <dt>Long: the length of the InputStream
     *      {@link #getEntry()}, if available.
     *      This value should not be &lt; 0</dt>
     *
     *  <dd>lastModified</dd>
     *  <dt>Long: Milliseconds (as with {@link java.util.Date#getTime()}) when
     *      underlying data was last changed.</dt>
     *
     *  <dd>isReadable</dd>
     *  <dt>Boolean: Reading of data will fail when false. If true there are no
     *  known
     *  obstacles for reading.</dt>
     *
     *  <dd>isWriteable</dd>
     *  <dt>Boolean: Writing of data will fail when false. If true there are no
     *  known
     *  obstacles fro writing.</dt>
     *
     *  <dd>isEntry</dd>
     *  <dt>Boolean: <em>Must</em> always match the return value of {@link 
     * com.top_logic.dsa.DataAccessProxy#isEntry()}.</dt>
     *
     *  <dd>isContainer</dd>
     *  <dt>Boolean: <em>Must</em> always match the return value of {@link 
     * com.top_logic.dsa.DataAccessProxy#isContainer()}.</dt>
     *
     *  <dd>exists</dd>
     *  <dt>Boolean: <em>Must</em> always match the return value of {@link 
     * com.top_logic.dsa.DataAccessProxy#exists()}.</dt>
     *
     *  <dd>name</dd>
     *  <dt>String: <em>Must</em> always match the return value of {@link 
     * com.top_logic.dsa.DataAccessProxy#getName()}.</dt>
     *
     *  <dd>author</dd>
     *  <dt>String: User that last changed the data.</dt>
     *
     *  <dd>numVersions</dd>
     *  <dt>Integer: Number of versions in the repository.
     *      (for entries only, not for a single version)
     *  </dt>
     * 
     *  <dd>locked</dd>
     *  <dt>Boolean: Indicates that revision is locked by a user.</dt>
     *
     *  <dd>deleted</dd>
     *  <dt>Boolean: Indicates that this revision has been deleted.</dt>
     *
     *  <dd>locker</dd>
     *  <dt>String: Person that currently locks the entry,
     *      only expected when isLocked is true.</dt>
     * </dl>
     *
     * @return the DSA specific properties
     */
    public DataObject getProperties() throws DatabaseAccessException {
        return dsa.getProperties(path);
    }

    /**
     * Get the database specific properties of an element.
     * The param 'version' specifies the version of the element
     * to get.
     * <p>
     * See getProperties() for further information.
     * </p>
     *
     * @param   version     the version of the element
     * @return  the DSA specific properties
     *
     * @since Version_3_2
     *
     * @see #getProperties()
     */
    public DataObject getProperties(String version)
        throws DatabaseAccessException {
        return dsa.getProperties(path, version);
    }

    /**
     * Set the database specific properties of an element.
     *
     * These properties are <em>very</em> specific to the
     * DSA. You cannot assume anything about the Object used.
     * How can I create a usefull DataObject without using 
     *  getProperties() ?.
     *
     * @param props the database specific properties
     */
    public void setProperties(DataObject props)
        throws DatabaseAccessException {
        dsa.setProperties(path, props);
    }

    /**
     * Lock an Entry. The user locking the document has exclusive
     * write and delete access to the element. Will usually fail on containers.
     * Works for Repositories only.
     *
     * See com.top_logic.base.security.SecurityContext#getCurrentUser() TODO TWI
     * @return	true, if lock was successful, false if lock failed
     */
    public boolean lock() throws DatabaseAccessException {
        return dsa.lock(path);
    }

    /**
     * Unlock an Entry. Only the user who locked the document or
     * an administrator is allowed to do this. Will usually fail on containers.
     * Works for Repositories only.
     *
     * See com.top_logic.base.security.SecurityContext#getCurrentUser()  TODO TWI
     *
     * @return	true, if unlock was successful, false if the element 
     *	        is locked but the current user is not the one who locked it.
     */
    public boolean unlock() throws DatabaseAccessException {
        return dsa.unlock(path);
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
     *           or false if anEvent is null or change rejected.
     * @throws   DataChangeException    Thrown, when the change is not allowed,
     *                                  the contained message should explain
     *                                  the reason.
     */
    public boolean fireCheckAllow(DataChangeEvent anEvent)
        throws DataChangeException {
        return (dsa.fireCheckAllow(anEvent));
    }

    /**
     * the part of the url right of the protocolseparator
     */
    public String getPathWithoutProtocol () {
    	return path;
    }

	/**
	 * Return the property from the given data access proxy.
	 * 
	 * @param dap
	 *        The accessing proxy to the repository, must not be
	 *        <code>null</code>.
	 * @param property
	 *        The name of the requested information, must not be
	 *        <code>null</code>.
	 * @param expectedValueType
	 *        the expected type of the value of the property
	 * 
	 * @return The stored value, may be <code>null</code>.
	 */
	public static <T> T getProperty(DataAccessProxy dap, String property, Class<T> expectedValueType) {
		try {
			DataObject someProps = dap.getProperties();

			if (someProps != null) {
				return expectedValueType.cast(someProps.getAttributeValue(property));
			}
		} catch (Exception ex) {
			Logger.debug("Unable to get '" + property + "' from '" + dap + "'!", ex, DataAccessProxy.class);
		}

		return null;
	}

	/**
	 * Return the property from the given data access proxy.
	 * 
	 * @param dap
	 *        The accessing proxy to the repository, must not be
	 *        <code>null</code>.
	 * @param version
	 *        The version of the requested information, must not be
	 *        <code>null</code>.
	 * @param property
	 *        The name of the requested information, must not be
	 *        <code>null</code>.
	 * @param expectedValueType
	 *        the expected type of the value of the property
	 * 
	 * @return The stored value, may be <code>null</code>.
	 */
	public static <T> T getProperty(DataAccessProxy dap, String version, String property, Class<T> expectedValueType) {
		try {
			DataObject someProps = dap.getProperties(version);

			if (someProps != null) {
				return expectedValueType.cast(someProps.getAttributeValue(property));
			}
		} catch (Exception ex) {
			Logger.debug("Unable to get '" + property + "' from '" + dap + "'!", ex, DataAccessProxy.class);
		}

		return null;
	}

	/**
	 * Returns the version of the dap which is the most recent version which is
	 * not later than the given date. if there is no version before the given
	 * date <code>null</code> is returned.
	 */
	public static String getVersion(final DataAccessProxy dap, long date) throws DatabaseAccessException {
		final String[] allAccessibleVersions = dap.getVersions();
		final Date date2 = new Date(date);
		final Object key = new NamedConstant("myFunnyConstant");
		final int insertIndex = Arrays.binarySearch(allAccessibleVersions, key, new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				Date d1 = getDate(o1);
				Date d2 = getDate(o2);
				// the most recent date is the first
				return d2.compareTo(d1);
			}

			private Date getDate(Object versionOrSpecial) {
				if (versionOrSpecial == key) {
					return date2;
				}
				String version = (String) versionOrSpecial;
				return DataAccessProxy.getProperty(dap, version, DAPropertyNames.LAST_MODIFIED, Date.class);
			}

		});

		int resultVersionIndex;
		if (insertIndex > -1) {
			resultVersionIndex = insertIndex;
		} else {
			resultVersionIndex = - insertIndex - 1;
		}

		if (resultVersionIndex == allAccessibleVersions.length) {
			return null;
		} else {
			return allAccessibleVersions[resultVersionIndex];
		}
	}

}
