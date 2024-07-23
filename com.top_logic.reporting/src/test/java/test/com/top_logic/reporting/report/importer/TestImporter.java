/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.report.importer;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.reporting.ReportingSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.ListBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.Accessor;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.ReportFactory;
import com.top_logic.reporting.report.model.RevisedReport;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionConfiguration;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionFactory;
import com.top_logic.reporting.report.model.aggregation.CountFunction;
import com.top_logic.reporting.report.model.aggregation.SumFunction;
import com.top_logic.reporting.report.model.objectproducer.AbstractObjectProducer;
import com.top_logic.reporting.report.model.objectproducer.ObjectProducerConfiguration;
import com.top_logic.reporting.report.model.objectproducer.ObjectProducerFactory;
import com.top_logic.reporting.report.model.objectproducer.WrapperObjectProducer;
import com.top_logic.reporting.report.model.objectproducer.WrapperObjectProducer.WrapperObjectProducerConfiguration;
import com.top_logic.reporting.report.model.partition.function.DatePartitionFunction;
import com.top_logic.reporting.report.model.partition.function.DatePartitionFunction.DatePartitionConfiguration;
import com.top_logic.reporting.report.model.partition.function.PartitionFunctionFactory;
import com.top_logic.reporting.report.util.ReportConstants;


/**
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TestImporter extends BasicTestCase {
    
    public static final String ATTRIBUTE     = "value";
    public static final String ACCESSOR_NAME = ValueObjectAccessor.class.getName();
    public static final String PRODUCER_NAME = ValueObjectProducer.class.getName();
    
    public static final String DATE_REPORT   = "file://test/reports/DateReport.xml";
    public static final String NUMBER_REPORT = "file://test/reports/NumberReport.xml";
    public static final String STRING_REPORT = "file://test/reports/StringReport.xml";
    public static final String SAME_REPORT   = "file://test/reports/SameReport.xml";

    
    public void testExport() throws Exception {
        
        ReportConfiguration theConf = ReportFactory.getInstance().createReportConfiguration(RevisedReport.class);
        theConf.setChartType(ReportConstants.REPORT_TYPE_LINE_CHART);
        
        DatePartitionConfiguration   thePart = (DatePartitionConfiguration) PartitionFunctionFactory.getInstance().createPartitionFunctionConfiguration(DatePartitionFunction.class);
        thePart.setDateRange(DatePartitionFunction.DATE_RANGE_MONTH);
        
        AggregationFunctionConfiguration theAgg1 = AggregationFunctionFactory.getInstance().createAggregationFunctionConfiguration(CountFunction.class);
        theAgg1.setAttributePath("dateStart");
        theAgg1.setLineVisible(false);
        
        AggregationFunctionConfiguration theAgg2 = AggregationFunctionFactory.getInstance().createAggregationFunctionConfiguration(SumFunction.class);
        theAgg2.setAttributePath("amount");
        theAgg2.setLineVisible(true);
        
        WrapperObjectProducerConfiguration theProd = (WrapperObjectProducerConfiguration) ObjectProducerFactory.getInstance().createObjectProducerConfiguration(WrapperObjectProducer.class);
        theProd.setObjectType("Contract");
        
        theConf.setBusinessObjectProducer(theProd);
        
        theConf.setPartitionConfiguration(thePart);
        theConf.setAggregationConfigurations(new ListBuilder().add(theAgg1).add(theAgg2).toList());
        
        StringWriter theString = new StringWriter(1024);
		try (ConfigurationWriter theWriter = new ConfigurationWriter(theString)) {
			theWriter.write("report", ReportConfiguration.class, theConf);
		}
        theString.flush();
        
        String theWrittenConf = theString.getBuffer().toString();
        
        Logger.error("" + thePart.getImplementationClass(), TestImporter.class);
        
        Logger.error(theWrittenConf, TestImporter.class);
        
		CharacterContent content = CharacterContents.newContent(theWrittenConf);
		Map<String, ConfigurationDescriptor> globalDescriptors =
			Collections.singletonMap("report", theConf.descriptor());
		ConfigurationItem theReadConf =
			new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, globalDescriptors)
				.setSource(content).read();
        
        assertTrue("Not instanceof ReportConfigurtaion", ReportConfiguration.class.isAssignableFrom(theReadConf.getClass()));
        
        //assertTrue(theConf.equals(theReadConf));
        
        theConf = (ReportConfiguration) theReadConf; 
        
        // TODO: check for set values
//        assertEquals(1, theConf.getPartitionConfiguration().size());
        assertEquals(2, theConf.getAggregationConfigurations().size());
        assertEquals(ReportConstants.REPORT_TYPE_LINE_CHART, theConf.getChartType());
        
    }

    public static class ValueObject {
        Object value;
        
        public ValueObject(Object aValue) {
            this.value = aValue;
        }
        
        public Object getValue() {
            return this.value;
        }
    }
    
    public static class ValueObjectProducer extends AbstractObjectProducer {
        
        // Constructors

        public ValueObjectProducer(InstantiationContext aContext, ObjectProducerConfiguration aConfig) {
            super(aContext, aConfig);
        }
        
        @Override
		protected Collection _getObjectsInternal() {
            if (getObjectType().equals("date")) {
                return getDateVOs();
            }
            else if(getObjectType().equals("number")) {
                return getNumberVOs();
            }
            else if(getObjectType().equals("string")) {
                return getStringVOs();
            }
            else {
                return Collections.EMPTY_SET;
            }
        }
        
//      size 12
        // null 1
        // same 2
        // sum  54
        private Collection getNumberVOs() {
            Set theColl = new HashSet();
            
            for (int i=0; i<5; i++) {
				theColl.add(new ValueObject(Integer.valueOf(i)));
            }

            for (int i=5; i<10; i++) {
				theColl.add(new ValueObject(Double.valueOf(i)));
            }
            
			theColl.add(new ValueObject(Integer.valueOf(9)));
            theColl.add(new ValueObject(null));
            
            return theColl;
        }
        
        private Collection getStringVOs() {
            Set theColl = new HashSet();
            
            for (int i=0; i<10; i++) {
                theColl.add(new ValueObject("i"+String.valueOf(i)));
            }
            theColl.add(new ValueObject("X"+String.valueOf(9)));
            theColl.add(new ValueObject(null));
            
            return theColl;
        }
        
        private Collection getDateVOs() {
            Set theColl = new HashSet();
            
            for (int i=0; i<10; i++) {
                theColl.add(new ValueObject(new Date(107, i, 1)));
            }
            
            theColl.add(new ValueObject(new Date(107, 9, 1)));
            theColl.add(new ValueObject(null));
            
            return theColl;
        }
    }
    
    public static class ValueObjectAccessor implements Accessor {
        
        @Override
		public Object getValue(Object object, String property) {
            ValueObject theObject = (ValueObject) object;
            if (ATTRIBUTE.equals(property)) {
                return theObject.getValue();
            }
            return null;
        }
        
        @Override
		public void setValue(Object object, String property, Object value) {
        }
    }
    /** 
     * This method returns the test in a suite.
     * 
     * @return Returns the test in a suite.
     */
    public static Test suite () {
        return ReportingSetup.createReportingSetup(TestImporter.class);
    }
    
    /**
     * This method can be used for direct testing.
     * 
     * @param args A string array of argument.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }

}
