/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.dataset;

import java.util.Date;

import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriod;

import com.top_logic.basic.DummyIDFactory;
import com.top_logic.basic.TLID;

/**
 * Extend Task with infos about the Tree-Structure.
 * 
 * Be carefull TreeTask with same name and same parent index will be suppressed by JFreechart
 * 
 * @author <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TreeTask extends Task implements Comparable {
    
    /** Index of Parent in {@link TaskSeries} */
    int parentIndex;

    /** depth of node in TreeStructure */
    int depth;

    private String type;

	private final TLID id;

    /** 
     * Create a new TreeTask.
     */
    public TreeTask(TLID anID, String aDescription, TimePeriod aDuration, int aParent, int aDepth, String aType) {
        super(aDescription, aDuration);
        this.id          = anID;
        this.parentIndex = aParent;
        this.depth       = aDepth;
        this.type        = aType;
    }

    /** 
     * Create a new TreeTask.
     */
    public TreeTask(String aDescription, Date aStart, Date anEnd, int aParent, int aDepth, String aType) {
		this(DummyIDFactory.createId(), aDescription, new SimpleTimePeriod(aStart, anEnd), aParent, aDepth, aType);
    }

    public TreeTask(TLID anID, String aDescription, Date aStart, Date anEnd, int aParent, int aDepth, String aType) {
        this(anID, aDescription, new SimpleTimePeriod(aStart, anEnd), aParent, aDepth, aType);
    }

    /**
     * Use {@link #getDescription()}.
     */
    @Override
	public String toString() {
		return this.id.toString() + this.parentIndex;
    }

    @Override
	public int compareTo(Object o) {
        return compareTo((TreeTask) o);
    }

    /**
     * Compare by description, then by depth.
     */
    public int compareTo(TreeTask other) {
		int theResult = ((Comparable) this.id).compareTo(other.id);

        if (theResult == 0) {
            theResult = this.depth - other.depth;
        }

        return theResult;
    }
    
    @Override
	public boolean equals(Object anOther) {
		// Ignoring SpotBugs(EQ_OVERRIDING_EQUALS_NOT_SYMMETRIC)
        return (anOther instanceof TreeTask) && equals((TreeTask) anOther); 
    }
    
    /**
     * hash code only depends on id and parentIndex
     */
    public boolean equals(TreeTask anOther) {
        return this.parentIndex == anOther.parentIndex && this.id.equals(anOther.id);
    }

    /**
     * hashCode only depends on id and parentIndex
     */
    @Override
	public int hashCode() {
        return this.id.hashCode() + this.parentIndex;
    }

    public int getDepth() {
        return (this.depth);
    }
    
    public int getParentIndex() {
        return (this.parentIndex);
    }

    /**
     * This method returns the type.
     * 
     * @return    Returns the type.
     */
    public String getType() {
        return (this.type);
    }
    
}