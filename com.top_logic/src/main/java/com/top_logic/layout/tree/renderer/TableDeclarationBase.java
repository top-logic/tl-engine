/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;

/**
 * Base class for {@link TableDeclaration}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public abstract class TableDeclarationBase implements TableDeclaration {

    /** 
     * Used to access column values of node objects.
     */
    protected Accessor accessor;
    /** When <code>true</code> headers will be render on top of the table */
    private boolean hasHeader;
    /** Used to render labels and icons for the tree nodes. */
    protected ResourceProvider resourceProvider;
    /** Used to translate things like the header names. */
	protected ResPrefix resourcePrefix = ResPrefix.GLOBAL;

    /** 
     * Creates a {@link TableDeclarationBase}.
     * 
     */
    public TableDeclarationBase() {
        super();
    }

	@Override
	public Accessor getAccessor() {
    	return accessor;
    }

    /** Sets the {@link Accessor} that retrieves the column values from a row object. */
    public void setAccessor(Accessor accessor) {
    	this.accessor = accessor;
    }

	@Override
	public ResPrefix getResourcePrefix() {
    	return resourcePrefix;
    }

	/** Setter for {@link #getResourcePrefix()}. */
	public void setResourcePrefix(ResPrefix resourcePrefix) {
    	this.resourcePrefix = resourcePrefix;
    }

	@Override
	public ResourceProvider getResourceProvider() {
    	return resourceProvider;
    }

    /** Setter for {@link #getResourceProvider()}. */
    public void setResourceProvider(ResourceProvider resourceProvider) {
    	this.resourceProvider = resourceProvider;
    }

	@Override
	public boolean hasHeader() {
    	return hasHeader;
    }

    /** Setter for {@link #hasHeader()}. */
    public void setHasHeader(boolean hasHeader) {
    	this.hasHeader = hasHeader;
    }

}
