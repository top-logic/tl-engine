/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.aggregation;

import java.awt.Color;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.format.ColorConfigFormat;
import com.top_logic.reporting.report.model.accessor.AccessorFactory;

/**
 * Configuration of an {@link AggregationFunction}.
 *
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public interface AggregationFunctionConfiguration extends PolymorphicConfiguration<AbstractAggregationFunction> {
    
    /**
     * Return the attribute path this function will use
     */
	String ATTRIBUTE_PATH_NAME = "attributePath";
    /** The path of the attribute the function computes its value from. */
    @Name(ATTRIBUTE_PATH_NAME)
    String  getAttributePath();
    
    /**
     * Set the attribute path used by that function
     */
    void    setAttributePath(String anAttributePath);
    
    /**
     * Store color as hex string
     */
    String COLOR_PROPERTY_NAME = "color";
    /**
     * Set the color which will be used for rendering of the function.
     */
    void    setColor(Color aColor);
    /**
     * Get the color which will be used for rendering of the function.
     */
	@Format(ColorConfigFormat.class)
    @Name(COLOR_PROPERTY_NAME)
    Color   getColor();
    
    /**
     * Set the lines visible for that function, used for line charts only.
     * If false, only dots will be rendered.
     */
    @BooleanDefault(true)
    boolean isLineVisible();
    /** Setter for {@link #isLineVisible()}. */
    void    setLineVisible(boolean isVisible);
    
    /**
     * If true, the function will ignore business object 
     * that return <code>null</code> as attribute value during its calculation.
     * 
     * Note that not all implementations of {@link AggregationFunction} use this
     * flag. 
     */
    String IGNORE_NULL_VALUES_PROPERTY_NAME = "ignoreNullValues";
    /** Whether business objects with an empty attribute value are ignored. */
    @Name(IGNORE_NULL_VALUES_PROPERTY_NAME)
    boolean isIgnoreNullValues();
    /** Setter for {@link #isIgnoreNullValues()}. */
    void    setIgnoreNullValues(boolean ignoreNullValues);
    
    /** The divisor the result of the function is divided by. */
    @DoubleDefault(1)
    @Name("divisor")
    double getDivisor();
    /** Setter for {@link #getDivisor()}. */
    void setDivisor(double aDivisor);
    
    /** Algorithm accessing the attribute values of the business objects. */
    @FormattedDefault(AccessorFactory.DEFAULT_ACCESSOR)
    @Format(AccessorFactory.class)
    @Name("accessor")
    Accessor getAccessor();
    /** Setter for {@link #getAccessor()}. */
    void setAccessor(Accessor anAccessor);
    
    /** Whether the values of the function are accumulated over the partitions. */
    @BooleanDefault(false)
    @Name("accumulated")
    boolean isAccumulated();
    /** Setter for {@link #isAccumulated()}. */
    void setAccumulated(boolean accumulated);
}

