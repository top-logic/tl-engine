/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.report.model.partition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.reporting.report.model.aggregation.TestValueAccessor;
import test.com.top_logic.reporting.report.model.aggregation.TestValueObject;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.function.PartitionFunction;
import com.top_logic.reporting.report.model.partition.function.PartitionFunctionFactory;

/**
 * Testcase for AbstractPartitionFunction.
 * 
 * @author    <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public abstract class AbstractPartitionFunctionTest extends BasicTestCase {
    
    protected static final String NUMBER = "number";
    protected static final String STRING = "string";
    protected static final String DATE   = "date";
    
    public void testFactory() throws Exception {
        try {
            // Class is not a PartitonFunction
            createPartitionConfiguration(AbstractPartitionFunctionTest.class);
            fail("Expected error, invalid implementation class");
        } catch (AssertionError e) {
            // exspected
        }
    }
    
    protected abstract String getAttribute();
    
    protected Collection<?> getBusinessObjects() {
        List theList = new ArrayList();
        for (int i=0; i<5000; i++) {
            TestValueObject theValue = new TestValueObject(NUMBER, i%5);
            theValue.setValue(DATE,   DateUtil.addDays(new Date(0), i%5));
            theValue.setValue(STRING, String.valueOf(i%5));
            theList.add(theValue);
        }
        for (int i=0; i<1000; i++) {
            TestValueObject theValue = new TestValueObject(NUMBER, null);
            theValue.setValue(DATE,   null);
            theValue.setValue(STRING, null);
            theList.add(theValue);
        }
        return theList;
    }
    
//    private ReportConfiguration createReportConfiguration() {
//        ReportConfiguration theConfig = TypedConfiguration.newConfigItem(ReportConfiguration.class);
//        return theConfig;
//    }
    
//    private AggregationFunctionConfiguration createAggregationConfiguration(Class aAggregationClass) {
//        try {
//            AggregationFunctionConfiguration theConfig = AggregationFunctionFactory.getInstance().createAggregationFunctionConfiguration(aAggregationClass);
//            theConfig.setAttributePath("number");
//            theConfig.setAccessor(new TestValueAccessor());
//            return theConfig;
//        }
//        catch (ConfigurationException ex) {
//            throw new ConfigurationError("Setup of aggregation configuration failed", ex);
//        }
//    }
    
    protected PartitionFunctionConfiguration createPartitionConfiguration(Class aPartitionClass) {
        try {
            PartitionFunctionConfiguration theConfig = PartitionFunctionFactory.getInstance().createPartitionFunctionConfiguration(aPartitionClass);
            theConfig.setAttribute(this.getAttribute());
            theConfig.setAccessor(new TestValueAccessor());
            return theConfig;
        }
        catch (ConfigurationException ex) {
            throw new ConfigurationError("Setup of partition configuration failed", ex);
        }
    }
    
    protected PartitionFunction createPartitionFunction(PartitionFunctionConfiguration aConfig) {
        return PartitionFunctionFactory.getInstance().getPartitionFunction(aConfig);
    }
}

