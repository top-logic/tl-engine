/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition.function;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.Accessor;
import com.top_logic.reporting.report.importer.node.parser.category.AbstractPartitionFunctionParser;
import com.top_logic.reporting.report.model.filter.FilterEntry;
import com.top_logic.reporting.report.model.partition.Partition;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.criteria.Criteria;

/**
 * A PartitionFunction is used to create {@link Partition}s for a report. For a detailed documentation
 * take a look at the {@link AbstractPartitionFunction} A PartitionFunction uses {@link FilterEntry}s to
 * categorize the business objects. Their attribute values will be accessed through an {@link Accessor}.
 * Each function can be configured in a related xml-file.
 * 
 * @see AbstractPartitionFunctionParser and AbstractRangeParser for details.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public interface PartitionFunction {

    /** @deprecated use {@link PartitionFunctionConfiguration#isIgnoreNullValues()} */
    @Deprecated
	public static final String IGNORE_NULL = "ignoreNullValues";
    
	/**
	 * Takes a {@link Collection} of Objects and puts them in partitions depending on the criteria used.
	 * 
	 * @param someObjects
	 *            the Objects to process
	 * 
	 * @return a list of {@link Partition}s.
	 */
	public List<Partition> processObjects(Collection<?> someObjects);

	/**
	 * @see AbstractPartitionFunction#ignoreEmptyPartitions()
	 * @deprecated use {@link ConfigurationItem}
	 */
	@Deprecated
	public boolean ignoreEmptyPartitions();

	/**
	 * @deprecated use {@link ConfigurationItem}
	 */
	@Deprecated
	public void setAttributeAccessor(Accessor anAccessor);

	/**
	 * Returns the type of the {@link PartitionFunction} (e.g. "date" or "number").
	 * @deprecated use {@link ConfigurationItem}
	 */
	@Deprecated
	public String getType();
	
	/**
	 * Returns the {@link Criteria} this {@link PartitionFunction} works with.
	 */
	public Criteria getCriteria();
}
