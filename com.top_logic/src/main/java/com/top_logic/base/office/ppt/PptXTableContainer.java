/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
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
 * Container class for hierarchical tables with a list of {@link PptXTableCell}s to represent a
 * table in MS Powerpoint.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class PptXTableContainer {
	private String name;

	private List<PptXTableContainer> children;

	private Map<String, PptXTableCell> cells;
    /**
     * The indexMapping is used for replacements of variable tables.
     */
	private List<String> indexMapping;
    
	/**
	 * Create a new PptXTableContainer
	 * 
	 * @param name
	 *        the container name
	 */
    public PptXTableContainer(String name) {
        this.name = name;
		this.cells = new HashMap<>();
		this.indexMapping = new ArrayList<>();
		this.children = new ArrayList<>();
    }
    
	/**
	 * Add a new child container
	 * 
	 * @param aChild
	 *        the new child
	 */
    public void addChild(PptXTableContainer aChild) {
        this.children.add(aChild);
    }
    
	/**
	 * all children
	 */
	public List<PptXTableContainer> getChildren() {
        return this.children;
    }
    
	/**
	 * the name
	 */
    public String getName() {
        return this.name;
    }
    
	/**
	 * Add a cell using its shape name for mappping
	 * 
	 * @param aCell
	 *        the cell
	 */
	public void addCell(PptXTableCell aCell) {
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
	public void addCell(PptXTableCell aCell, boolean useIndexMapping) {
		this.cells.put(aCell.getShapeName(), aCell);
        if(useIndexMapping) {
			this.indexMapping.add(aCell.getShapeName());
        }
    }
    
	/**
	 * Get a cell using the shape name mapping
	 * 
	 * @param aCellName
	 *        the cell shape name
	 * @return the cell
	 */
	public PptXTableCell getCell(String aCellName) {
		return this.cells.get(aCellName);
    }
    
	/**
	 * Get a cell by its list index
	 * 
	 * @param anIndex
	 *        the index
	 * @return the cell
	 */
	public PptXTableCell getCellByIndex(int anIndex) {
		return this.cells.get(this.indexMapping.get(anIndex));
    }
    
	/**
	 * true if this container has children
	 */
    public boolean hasChildren() {
        return !CollectionUtil.isEmptyOrNull(children);
    }
}
