/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;

/**
 * Container class for {@link PptCell}s to represent a table in MS Powerpoint.
 * 
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class PptTableContainer {
	private String name;
	private List children;
	private Map cells;
    /**
     * The indexMapping is used for replacements of variable tables.
     */
    private List indexMapping;
    
    public PptTableContainer(String name) {
        this.name = name;
        this.cells = new HashMap();
        this.indexMapping = new ArrayList();
        this.children = new ArrayList();
    }
    
    /** Adds a child container. */
    public void addChild(PptTableContainer aChild) {
        this.children.add(aChild);
    }
    
    /** The children. */
    public List getChildren() {
        return this.children;
    }
    
    /** The name. */
    public String getName() {
        return this.name;
    }
    
    /**
     * Adds a new cell to this container, accessible by its index.
     *
     * @see #addCell(PptCell, boolean)
     */
    public void addCell(PptCell aCell) {
        this.addCell(aCell, true);
    }
    
    /**
     * Adds a new cell to this container. If {@code useIndexMapping} is
     * <code>true</code> the cell will be accessible via
     * {@link #getCellByIndex(int)}.
     * 
     * @param aCell
     *            the {@link PptCell} to add.
     * @param useIndexMapping
     *            If <code>true</code> this cell will be added to the
     *            index mapping.
     */
    public void addCell(PptCell aCell, boolean useIndexMapping) {
        this.cells.put(aCell.getName(), aCell);
        if(useIndexMapping) {
            this.indexMapping.add(aCell.getName());
        }
    }
    
    /** The cell with the given name. */
    public PptCell getCell(String aCellName) {
        return (PptCell) this.cells.get(aCellName);
    }
    
    /**
     * The cell with the given index.
     *
     * @see #addCell(PptCell, boolean)
     */
    public PptCell getCellByIndex(int anIndex) {
        return (PptCell) this.cells.get(this.indexMapping.get(anIndex));
    }
    
    /** Whether this has children. */
    public boolean hasChildren() {
        return !CollectionUtil.isEmptyOrNull(children);
    }
}
