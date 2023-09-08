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
    
    public void addChild(PptTableContainer aChild) {
        this.children.add(aChild);
    }
    
    public List getChildren() {
        return this.children;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void addCell(PptCell aCell) {
        this.addCell(aCell, true);
    }
    
    /**
     * Adds a new cell to this container. If useIndexMapping is
     * <code>true</code> the cell will be accessible via
     * {@link #getCellByIndex(int)}.
     * 
     * @param aCell
     *            the {@link PptCell} to add.
     * @param useIndexMapping
     *            If <code>true</code> this cell will be added to the
     *            indexMapping.
     */
    public void addCell(PptCell aCell, boolean useIndexMapping) {
        this.cells.put(aCell.getName(), aCell);
        if(useIndexMapping) {
            this.indexMapping.add(aCell.getName());
        }
    }
    
    public PptCell getCell(String aCellName) {
        return (PptCell) this.cells.get(aCellName);
    }
    
    public PptCell getCellByIndex(int anIndex) {
        return (PptCell) this.cells.get(this.indexMapping.get(anIndex));
    }
    
    public boolean hasChildren() {
        return !CollectionUtil.isEmptyOrNull(children);
    }
}
