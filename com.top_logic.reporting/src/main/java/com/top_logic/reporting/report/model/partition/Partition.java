/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition;

import java.util.Collection;
import java.util.List;

import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.model.partition.criteria.Criteria;

/**
 * A Partition is a subset of <code>BusinessObjects</code>, which are used to create a
 * report. Each report has a {@link List} of partitions, which has either
 * <code>BusinessObjects</code> or <code>Partitions</code>. A
 * <code>BusinessObject</code> is any Object that can be used in a {@link Report}.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public interface Partition {

	/**
	 * The name of this partition.
	 * 
	 * @param aLanguage
	 *            the language which is used
	 * 
	 * @return the name of the partition in the right internationalization
	 */
	public String getName( String aLanguage );

	/**
	 * Adds a {@link Collection} of <code>Objects</code> to this partition.
	 * 
	 * @param someObjects
	 *            the <code>Objects</code> to add
	 * 
	 * @throws IllegalStateException
	 *             if this partition has <code>subPartitions</code>
	 */
	public void add( Collection<?> someObjects ) throws IllegalStateException;

	/**
	 * Adds a single <code>Object</code> to this partition.
	 * 
	 * @param anObject
	 *            the <code>Object</code> to add
	 * 
	 * @throws IllegalStateException
	 *             if this partition has <code>subPartitions</code>
	 */
	public void add( Object anObject ) throws IllegalStateException;

	/**
	 * Returns the <code>Objects</code> of this partition in form of a {@link List}. If
	 * <code>recursive</code> is <code>true</code> it will return the
	 * <code>Objects</code> of all subPartitions.
	 * 
	 * @param recursive
	 *            <code>true</code> if all <code>Objects</code> of all subPartitions
	 *            shall be returned, <code>false</code> (default) otherwise.
	 * 
	 * @return a {@link List} of <code>Objects</code>
	 */
	public List<Object> getObjects( boolean recursive );

	/**
	 * Adds a new <code>Partition</code> to this partition.
	 * 
	 * @throws IllegalStateException
	 *             if this partition has <code>BusinessObjects</code>
	 */
	public void addSubPartition( Partition aPartition ) throws IllegalStateException;

	/**
	 * Adds a {@link Collection} of {@link Partition}s to this Partition.
	 * 
	 * @param somePartitions
	 *            the Partitions to add
	 * 
	 * @throws IllegalStateException
	 *             if this partition has <code>BusinessObjects</code>
	 */
	public void addSubPartitions( Collection<Partition> somePartitions ) throws IllegalStateException;

	/**
	 * Returns a {@link List} of subPartitions
	 * 
	 * @return a {@link List} of <code>Partitions</code>
	 */
	public List<Partition> getSubPartitions();

	/**
	 * Checks if this partition has subPartitions.
	 * 
	 * @return <code>true</code> if this partition has at least one subPartition,
	 *         <code>false</code> otherwise
	 */
	public boolean hasSubPartitions();

	/**
	 * Checks if this partition has BusinessObjects.
	 * 
	 * @return <code>true</code> if this partition has at least one BusinessObject,
	 *         <code>false</code> otherwise
	 */
	public boolean hasBusinessObjects();
	
	/**
	 * Returns the {@link Criteria} used to generate this partition.
	 * 
	 * @return the {@link Criteria}.
	 */
	public Criteria getCriteria();
	
	/**
	 * Returns the unique Identifier for this Partition.
	 * 
	 * @return a String which identifies this Partition.
	 */
	public String getIdentifier();
	
	/**
	 * Sets the unique identifier for this partition.
	 */
	public void setIdentifier(String anID);

}
