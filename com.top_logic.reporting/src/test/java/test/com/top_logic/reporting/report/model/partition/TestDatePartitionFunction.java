/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.report.model.partition;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.TLTestSetup;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.reporting.report.model.partition.Partition;
import com.top_logic.reporting.report.model.partition.TimeRangeFactory;
import com.top_logic.reporting.report.model.partition.function.DatePartitionFunction;
import com.top_logic.reporting.report.model.partition.function.DatePartitionFunction.DatePartitionConfiguration;
import com.top_logic.reporting.report.model.partition.function.PartitionFunction;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestDatePartitionFunction extends AbstractPartitionFunctionTest {
    
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestDatePartitionFunction.class);
    }

    public static void main(String[] args) {
        TestRunner theRunner = new TestRunner();
        theRunner.doRun(suite());
    }
    
    @Override
    protected String getAttribute() {
        return DATE;
    }
    
    public void testProcessObjectSimple() throws Exception {
        DatePartitionConfiguration theConf = (DatePartitionConfiguration) this.createPartitionConfiguration(DatePartitionFunction.class);
        theConf.setDateRange(DatePartitionFunction.DATE_RANGE_AUTOMATIC);
        theConf.setSubIntervalLength(TimeRangeFactory.DAY_INT);
        PartitionFunction theFunc = this.createPartitionFunction(theConf);
        List<Partition> theParts = theFunc.processObjects(this.getBusinessObjects());
        assertEquals(5, theParts.size());
        
        for (Partition thePartition : theParts) {
            assertEquals(1000, thePartition.getObjects(false).size());
        }
        
        theConf = (DatePartitionConfiguration) this.createPartitionConfiguration(DatePartitionFunction.class);
        theConf.setDateRange(DatePartitionFunction.DATE_RANGE_AUTOMATIC);
        theConf.setSubIntervalLength(TimeRangeFactory.WEEK_INT);
        theFunc = this.createPartitionFunction(theConf);
        theParts = theFunc.processObjects(this.getBusinessObjects());
        assertEquals(2, theParts.size());
        
        assertEquals(4000, theParts.get(0).getObjects(false).size());
        assertEquals(1000, theParts.get(1).getObjects(false).size());
    }

	public void testProcessObjectSimple2() throws Exception {
		DatePartitionConfiguration theConf =
			(DatePartitionConfiguration) this.createPartitionConfiguration(DatePartitionFunction.class);
        theConf.setDateRange(DatePartitionFunction.DATE_RANGE_MANUAL);
        theConf.setSubIntervalLength(TimeRangeFactory.DAY_INT);
        theConf.setIntervalStart(DateUtil.addDays(new Date(0), 0));
        theConf.setIntervalEnd(DateUtil.addDays(new Date(0), 3));
		PartitionFunction theFunc = this.createPartitionFunction(theConf);
		List<Partition> theParts = theFunc.processObjects(this.getBusinessObjects());
        assertEquals(4, theParts.size());
        
        for (Partition thePartition : theParts) {
            assertEquals(1000, thePartition.getObjects(false).size());
        }
 	}

	public void testProcessObjectSimple3() throws Exception {
		DatePartitionConfiguration theConf =
			(DatePartitionConfiguration) this.createPartitionConfiguration(DatePartitionFunction.class);
        theConf.setDateRange(DatePartitionFunction.DATE_RANGE_MANUAL);
        theConf.setSubIntervalLength(TimeRangeFactory.HOUR_INT);
        theConf.setIntervalStart(DateUtil.adjustToDayBegin(new Date(0)));
        theConf.setIntervalEnd(DateUtil.adjustToDayEnd(new Date(0)));
		PartitionFunction theFunc = this.createPartitionFunction(theConf);
		List<Partition> theParts = theFunc.processObjects(this.getBusinessObjects());
        assertEquals(24, theParts.size());

		int offset = TimeZones.systemTimeZone().getOffset(0L);
		int offsetInHours = (int) TimeUnit.MILLISECONDS.toHours(offset);
		assertEquals(1000, theParts.get(offsetInHours).getObjects(false).size());
         
		for (int i = 0; i < theParts.size(); i++) {
			if (i == offsetInHours) {
				continue;
			}
            assertEquals(0, theParts.get(i).getObjects(false).size());
        }
    }
}

