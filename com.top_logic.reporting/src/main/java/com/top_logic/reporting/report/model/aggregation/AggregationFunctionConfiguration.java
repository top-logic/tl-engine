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
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public interface AggregationFunctionConfiguration extends PolymorphicConfiguration<AbstractAggregationFunction> {
    
    /**
     * Return the attribute path this function will use
     */
	String ATTRIBUTE_PATH_NAME = "attributePath";
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
    void    setLineVisible(boolean isVisible);
    
    /**
     * If true, the function will ignore business object 
     * that return <code>null</code> as attribute value during its calculation.
     * 
     * Note that not all implementations of {@link AggregationFunction} use this
     * flag. 
     */
    String IGNORE_NULL_VALUES_PROPERTY_NAME = "ignoreNullValues";
    @Name(IGNORE_NULL_VALUES_PROPERTY_NAME)
    boolean isIgnoreNullValues();
    void    setIgnoreNullValues(boolean ignoreNullValues);
    
    @DoubleDefault(1)
    @Name("divisor")
    double getDivisor();
    void setDivisor(double aDivisor);
    
    @FormattedDefault(AccessorFactory.DEFAULT_ACCESSOR)
    @Format(AccessorFactory.class)
    @Name("accessor")
    Accessor getAccessor();
    void setAccessor(Accessor anAccessor);
    
    @BooleanDefault(false)
    @Name("accumulated")
    boolean isAccumulated();
    void setAccumulated(boolean accumulated);
}

