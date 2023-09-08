/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.impl;

import java.io.InputStream;
import java.io.OutputStream;

import com.top_logic.dob.DataObject;
import com.top_logic.dsa.DataSourceAdaptor;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.evt.DataChangeEvent;
import com.top_logic.dsa.evt.DataChangeListener;
import com.top_logic.dsa.ex.DataChangeException;
import com.top_logic.dsa.ex.NotSupportedException;

/**
 * General delegator data source adaptor, which is doing nothing but delegation.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractDelegatorDataSourceAdaptor implements DataSourceAdaptor {

    /** The inner data source to be wrapped. */
    protected DataSourceAdaptor innerDSA;

    /** The name of the protocol. */
    protected String protocol;

    /** 
     * @see com.top_logic.dsa.impl.AbstractDataSourceAdaptor#getProtocol()
     */
    @Override
	public String getProtocol() {
        return this.protocol;
    }

    /** 
     * @see com.top_logic.dsa.impl.AbstractDataSourceAdaptor#setProtocol(java.lang.String)
     */
    @Override
	public void setProtocol(String newProtocol) {
        this.protocol = newProtocol;
		innerDSA.setProtocol(protocol);
    }

    /** 
     * @see com.top_logic.dsa.impl.AbstractDataSourceAdaptor#getObjectEntry(java.lang.String, java.lang.String)
     */
    @Override
	public DataObject getObjectEntry(String aName, String aVersion) throws DatabaseAccessException {
        return this.innerDSA.getObjectEntry(aName, aVersion);
    }

    /** 
     * @see com.top_logic.dsa.impl.AbstractDataSourceAdaptor#getObjectEntry(java.lang.String)
     */
    @Override
	public DataObject getObjectEntry(String aName) throws DatabaseAccessException {
        return this.innerDSA.getObjectEntry(aName);
    }

    /** 
     * @see com.top_logic.dsa.impl.AbstractDataSourceAdaptor#addDataChangeListener(com.top_logic.dsa.evt.DataChangeListener)
     */
    @Override
	public boolean addDataChangeListener(DataChangeListener aListener) {
        return this.innerDSA.addDataChangeListener(aListener);
    }

    @Override
	public void close() throws DatabaseAccessException {
        this.innerDSA.close();
    }

    @Override
	public String createContainer(String containerPath, String elementName) throws DatabaseAccessException {
        return this.innerDSA.createContainer(containerPath, elementName);
    }

    @Override
	public String createEntry(String containerPath, String elementName, InputStream data) throws DatabaseAccessException {
        return this.innerDSA.createEntry(containerPath, elementName, data);
    }

    @Override
	public OutputStream createEntry(String containerPath, String elementName) throws DatabaseAccessException {
        return this.innerDSA.createEntry(containerPath, elementName);
    }

    @Override
	public String createNewEntryName(String containerPath, String prefix, String suffix) throws DatabaseAccessException {
        return this.innerDSA.createNewEntryName(containerPath, prefix, suffix);
    }

    @Override
	public DataObject createObjectEntry(String containerPath, String elementName) throws DatabaseAccessException {
        return this.innerDSA.createObjectEntry(containerPath, elementName);
    }

    @Override
	public void delete(String path, boolean force) throws DatabaseAccessException {
        this.innerDSA.delete(path, force);
    }

    @Override
	public void deleteRecursively(String path) throws DatabaseAccessException {
        this.innerDSA.deleteRecursively(path);
    }

    @Override
	public boolean exists(String path) throws DatabaseAccessException {
        return this.innerDSA.exists(path);
    }

    @Override
	public boolean fireCheckAllow(DataChangeEvent anEvent) throws DataChangeException {
        return this.innerDSA.fireCheckAllow(anEvent);
    }

    @Override
	public void fireDataChanged(DataChangeEvent anEvent) {
        this.innerDSA.fireDataChanged(anEvent);
    }

    @Override
	public String getChild(String containerPath, String elementName) throws DatabaseAccessException {
        return this.innerDSA.getChild(containerPath, elementName);
    }

    @Override
	public String getDisplayName(String path) throws DatabaseAccessException {
        return this.innerDSA.getDisplayName(path);
    }

    @Override
	public InputStream getEntry(String path) throws DatabaseAccessException {
        return this.innerDSA.getEntry(path);
    }

    @Override
	public InputStream getEntry(String path, String version) throws DatabaseAccessException {
        return this.innerDSA.getEntry(path, version);
    }

    @Override
	public OutputStream getEntryAppendStream(String path) throws DatabaseAccessException {
        return this.innerDSA.getEntryAppendStream(path);
    }

    @Override
	public String[] getEntryNames(String path) throws DatabaseAccessException {
        return this.innerDSA.getEntryNames(path);
    }

    @Override
	public OutputStream getEntryOutputStream(String path) throws DatabaseAccessException {
        return this.innerDSA.getEntryOutputStream(path);
    }

    @Override
	public String getForwardURL(String path) throws NotSupportedException {
        return this.innerDSA.getForwardURL(path);
    }

    @Override
	public String getMimeType(String path) throws DatabaseAccessException {
        return this.innerDSA.getMimeType(path);
    }

    @Override
	public String getName(String path) throws DatabaseAccessException {
        return this.innerDSA.getName(path);
    }

    @Override
	public String getParent(String path) throws DatabaseAccessException {
        return this.innerDSA.getParent(path);
    }

    @Override
	public DataObject getProperties(String path) throws DatabaseAccessException {
        return this.innerDSA.getProperties(path);
    }

    @Override
	public DataObject getProperties(String path, String version) throws DatabaseAccessException {
        return this.innerDSA.getProperties(path, version);
    }

    @Override
	public String getURL(String path) throws NotSupportedException {
        return this.innerDSA.getURL(path);
    }

    @Override
	public String[] getVersions(String path) throws DatabaseAccessException {
        return this.innerDSA.getVersions(path);
    }

    @Override
	public boolean isContainer(String path) throws DatabaseAccessException {
        return this.innerDSA.isContainer(path);
    }

    @Override
	public boolean isEntry(String path) throws DatabaseAccessException {
        return this.innerDSA.isEntry(path);
    }

    @Override
	public boolean isPrivate() {
        return this.innerDSA.isPrivate();
    }

    @Override
	public boolean isRepository() {
        return this.innerDSA.isRepository();
    }

    @Override
	public boolean isStructured() {
        return this.innerDSA.isStructured();
    }

    @Override
	public boolean lock(String path) throws DatabaseAccessException {
        return this.innerDSA.lock(path);
    }

    @Override
	public String move(String oldPath, String newPath) throws DatabaseAccessException {
        return this.innerDSA.move(oldPath, newPath);
    }

    @Override
	public void putEntry(String path, InputStream data) throws DatabaseAccessException {
        this.innerDSA.putEntry(path, data);
    }

    @Override
	public void putEntry(String path, DataObject data) throws DatabaseAccessException {
        this.innerDSA.putEntry(path, data);
    }

    @Override
	public OutputStream putEntry(String containerPath, String elementName) throws DatabaseAccessException {
        return this.innerDSA.putEntry(containerPath, elementName);
    }

    @Override
	public boolean removeDataChangeListener(DataChangeListener aListener) {
        return this.innerDSA.removeDataChangeListener(aListener);
    }

    @Override
	public String rename(String oldPath, String newName) throws DatabaseAccessException {
        return this.innerDSA.rename(oldPath, newName);
    }

    @Override
	public void setProperties(String path, DataObject props) throws DatabaseAccessException {
        this.innerDSA.setProperties(path, props);
    }

    @Override
	public boolean unlock(String path) throws DatabaseAccessException {
        return this.innerDSA.unlock(path);
    }

    /** 
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return this.getClass().getName() + " ["
                + this.toStringValues()
                + ']';
    }

    /** 
     * Return the values defining this instance.
     * 
     * @return    The requested values.
     */
    protected String toStringValues() {
        return "protocol: '" + this.protocol + 
               "', inner: " + this.innerDSA;
    }

}
