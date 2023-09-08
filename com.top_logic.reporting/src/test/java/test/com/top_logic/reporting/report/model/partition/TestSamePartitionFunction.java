/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.report.model.partition;

import java.util.List;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.reporting.report.model.partition.Partition;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.function.PartitionFunction;
import com.top_logic.reporting.report.model.partition.function.SamePartitionFunction;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestSamePartitionFunction extends AbstractPartitionFunctionTest {
    
    public static Test suite() {
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(TestSamePartitionFunction.class,
			LabelProviderService.Module.INSTANCE));
    }

    public static void main(String[] args) {
        TestRunner theRunner = new TestRunner();
        theRunner.doRun(suite());
    }
    
    public void testProcessObjectSimple() throws Exception {
        PartitionFunctionConfiguration theConf = this.createPartitionConfiguration(SamePartitionFunction.class);
        theConf.setIgnoreNullValues(false);
        PartitionFunction theFunc = this.createPartitionFunction(theConf);
        List<Partition> theParts = theFunc.processObjects(this.getBusinessObjects());
        assertEquals(6, theParts.size());
        
        for (Partition thePartition : theParts) {
            assertEquals(1000, thePartition.getObjects(false).size());
        }
        
        theConf = this.createPartitionConfiguration(SamePartitionFunction.class);
        theConf.setIgnoreNullValues(true);
        theFunc = this.createPartitionFunction(theConf);
        theParts = theFunc.processObjects(this.getBusinessObjects());
        assertEquals(5, theParts.size());
        
        for (Partition thePartition : theParts) {
            assertEquals(1000, thePartition.getObjects(false).size());
        }
    }
    
    @Override
    protected String getAttribute() {
        return NUMBER;
    }
    
    
    
}

