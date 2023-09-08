/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.report.model.filter;

import java.util.List;

import junit.framework.Test;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.DateUtil;
import com.top_logic.reporting.report.model.filter.DateInterval;
import com.top_logic.reporting.report.model.filter.DateIntervalProvider;
import com.top_logic.reporting.report.model.partition.TimeRangeFactory;

/**
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestDateIntervalProvider extends BasicTestCase {

    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestDateIntervalProvider.class);
    }
    
    public void testHourIntervals() throws Exception {
        DateIntervalProvider provider = new DateIntervalProvider(TimeRangeFactory.HOUR_INT, false);
        
        List<DateInterval> intervals;
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1, 0, 0), DateUtil.createDate(2010, 0, 1, 23, 0));
        assertEquals(24, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 0, 1)), intervals.get(23).getEnd());
        
    }
    
    public void testMonthIntervals() throws Exception {
        DateIntervalProvider provider = new DateIntervalProvider(TimeRangeFactory.MONTH_INT, false);
        
        List<DateInterval> intervals;
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 0, 1));
        assertEquals(1, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 0, 31)), intervals.get(0).getEnd());
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 0, 31));
        assertEquals(1, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 0, 31)), intervals.get(0).getEnd());
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 1, 1));
        assertEquals(2, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 0, 31)), intervals.get(0).getEnd());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 1, 1)), intervals.get(1).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 1, 28)), intervals.get(1).getEnd());
        
        intervals = provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 11, 31));
        assertEquals(12, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 0, 31)), intervals.get(0).getEnd());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 11, 1)), intervals.get(11).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 11, 31)), intervals.get(11).getEnd());
    }
    
    public void testDayIntervals() throws Exception {
        DateIntervalProvider provider = new DateIntervalProvider(TimeRangeFactory.DAY_INT, false);
        
        List<DateInterval> intervals;
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 0, 1));
        assertEquals(1, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getEnd());
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 0, 31));
        assertEquals(31, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getEnd());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 31)), intervals.get(30).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 0, 31)), intervals.get(30).getEnd());
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 1, 1));
        assertEquals(32, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getEnd());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 1, 1)), intervals.get(31).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 1, 1)), intervals.get(31).getEnd());
        
        intervals = provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 11, 31));
        assertEquals(365, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getEnd());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 11, 31)), intervals.get(364).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 11, 31)), intervals.get(364).getEnd());
    }
    
    public void testQuarterIntervals() throws Exception {
        DateIntervalProvider provider = new DateIntervalProvider(TimeRangeFactory.QUARTER_INT, false);
        
        List<DateInterval> intervals;
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 0, 1));
        assertEquals(1, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 2, 31)), intervals.get(0).getEnd());
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 2, 31));
        assertEquals(1, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 2, 31)), intervals.get(0).getEnd());
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 3, 1));
        assertEquals(2, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 2, 31)), intervals.get(0).getEnd());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 3, 1)), intervals.get(1).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 5, 30)), intervals.get(1).getEnd());
        
        intervals = provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 11, 31));
        assertEquals(4, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 2, 31)), intervals.get(0).getEnd());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 9, 1)), intervals.get(3).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 11, 31)), intervals.get(3).getEnd());
    }
    
    public void testHalfyearIntervals() throws Exception {
        DateIntervalProvider provider = new DateIntervalProvider(TimeRangeFactory.HALFYEAR_INT, false);
        
        List<DateInterval> intervals;
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 0, 1));
        assertEquals(1, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 5, 30)), intervals.get(0).getEnd());
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 5, 30));
        assertEquals(1, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 5, 30)), intervals.get(0).getEnd());
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 6, 1));
        assertEquals(2, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 5, 30)), intervals.get(0).getEnd());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 6, 1)), intervals.get(1).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 11, 31)), intervals.get(1).getEnd());
        
        intervals = provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 11, 31));
        assertEquals(2, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 5, 30)), intervals.get(0).getEnd());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 6, 1)), intervals.get(1).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 11, 31)), intervals.get(1).getEnd());
    }
    
    public void testYearIntervals() throws Exception {
        DateIntervalProvider provider = new DateIntervalProvider(TimeRangeFactory.YEAR_INT, false);
        
        List<DateInterval> intervals;
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 0, 1));
        assertEquals(1, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 11, 31)), intervals.get(0).getEnd());
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 5, 30));
        assertEquals(1, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 11, 31)), intervals.get(0).getEnd());
        
        intervals = (List<DateInterval>) provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2010, 11, 31));
        assertEquals(1, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 11, 31)), intervals.get(0).getEnd());
        
        intervals = provider.getIntervals(DateUtil.createDate(2010, 0, 1), DateUtil.createDate(2011, 0, 1));
        assertEquals(2, intervals.size());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2010, 0, 1)), intervals.get(0).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2010, 11, 31)), intervals.get(0).getEnd());
        assertEquals(DateUtil.adjustToDayBegin(DateUtil.createDate(2011, 0, 1)), intervals.get(1).getBegin());
        assertEquals(DateUtil.adjustToDayEnd(DateUtil.createDate(2011, 11, 31)), intervals.get(1).getEnd());
    }
}
