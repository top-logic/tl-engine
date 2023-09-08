/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.report.model.aggregation;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.reporting.report.model.aggregation.AggregationFunction;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionConfiguration;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionFactory;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public abstract class AbstractAggregationTest extends BasicTestCase {
    
    public AbstractAggregationTest(String aTest) {
        super(aTest);
    }
    
    protected AggregationFunction createFunction(Class aFunctionClass, String anAttributePath) {
        try {
            AggregationFunctionConfiguration theConf = AggregationFunctionFactory.getInstance().createAggregationFunctionConfiguration(aFunctionClass);
            theConf.setAttributePath(anAttributePath);
            theConf.setAccessor(new TestValueAccessor());
            return AggregationFunctionFactory.getInstance().getAggregationFunction(theConf);
        } catch (ConfigurationException c) {
            throw new RuntimeException(c);
        }
    }

}

