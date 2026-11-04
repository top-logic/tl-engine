/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ImplClasses;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.Accessor;
import com.top_logic.reporting.report.model.accessor.AccessorFactory;
import com.top_logic.reporting.report.model.aggregation.AggregationFunction;
import com.top_logic.reporting.report.model.partition.function.PartitionFunction;
import com.top_logic.reporting.report.model.partition.function.PartitionFunctionFactory;

/**
 * Configuration of a {@link PartitionFunction}.
 *
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public interface PartitionFunctionConfiguration extends PolymorphicConfiguration<PartitionFunction> {
    
    /**
     * If true, the function will ignore business object 
     * that return <code>null</code> as attribute and will them not put into
     * any partition.
     * 
     * Note that not all implementations of {@link AggregationFunction} use this
     * flag. 
     */
	String IGNORE_NULL_VALUES_NAME = "ignoreNullValues";
	/** Whether business objects with an empty attribute value are ignored. */
	@BooleanDefault(false)
    @Name(IGNORE_NULL_VALUES_NAME)
    boolean isIgnoreNullValues();
    /** Setter for {@link #isIgnoreNullValues()}. */
    void    setIgnoreNullValues(boolean ignoreNullValues);
    
    /**
     * If true, the function will not create partition that does not contain 
     * any business object
     *
     * Note that not all implementations of {@link AggregationFunction} use this
     * flag. 
     */
    String IGNORE_EMPTY_PARTITIONS_VALUES_NAME = "ignoreEmtpyPartitions";
    /** Whether partitions without business objects are omitted. */
    @Name(IGNORE_EMPTY_PARTITIONS_VALUES_NAME)
    @BooleanDefault(false)
    boolean isIgnoreEmptyPartitions();
    /** Setter for {@link #isIgnoreEmptyPartitions()}. */
    void setIgnoreEmptyPartitions(boolean ignoreEmtpyPartitions);
    
    /** Configuration name of {@link #getAttribute()}. */
    String ATTRIBUTE_PROPERTY_NAME = "attribute";
    
    /** The path of the attribute to partition by. */
    @Name(ATTRIBUTE_PROPERTY_NAME)
    String getAttribute();
    /** Setter for {@link #getAttribute()}. */
    void setAttribute(String anAtrributePath);
    
    /** Algorithm accessing the attribute values of the business objects. */
    @Format(AccessorFactory.class)
    @FormattedDefault(AccessorFactory.DEFAULT_ACCESSOR)
    @Name("accessor")
    Accessor getAccessor();
    /** Setter for {@link #getAccessor()}. */
    void setAccessor(Accessor anAccessor);
    
    /** Function partitioning each partition further. */
    @ImplClasses(PartitionFunctionFactory.class)
    @Name("partition")
    PartitionFunctionConfiguration  getPartitionConfiguration();
    /** Setter for {@link #getPartitionConfiguration()}. */
    void setPartitionConfiguration(PartitionFunctionConfiguration aFunction);
}

