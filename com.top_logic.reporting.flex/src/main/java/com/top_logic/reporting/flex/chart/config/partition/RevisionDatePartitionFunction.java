/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;

/**
 * Create revision partitions for different (configured) dates.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class RevisionDatePartitionFunction<C extends RevisionDatePartitionFunction.Config> extends AbstractRevisionPartitionFunction<C> {

	/**
	 * Supported date types for the partition function. 
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
    @SuppressWarnings("javadoc")
    public enum DateType {
        DAY, WEEK, MONTH, YEAR;
	}

	/**
	 * Configuration of the revision date partition. 
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config extends AbstractRevisionPartitionFunction.Config {

	    /** Date type to be used for the partition (default is "DAY"). */
		@FormattedDefault("DAY")
		DateType getType();

		/** Number of partitions to be created by this function. */
		@IntDefault(30)
		Integer getSlots();
	}

	/** 
	 * Creates a {@link RevisionDatePartitionFunction}.
	 */
	public RevisionDatePartitionFunction(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected List<Revision> getRevisions() {
		Integer        theSize      = this.getConfig().getSlots();
		List<Revision> theRevisions = new ArrayList<>(theSize);
		Date           theDate      = new Date();

		for (int thePos = 0; thePos < theSize; thePos++) {
			Revision theRev = this.getRevision(theDate);

			if ((theRev == null) || (theRev == Revision.INITIAL)) {
				break;
			}

			theRevisions.add(theRev);
			theDate = this.shiftDate(theDate);
		}

		return theRevisions;
	}

	private Revision getRevision(Date aDate) {
		return HistoryUtils.getRevisionAt(aDate.getTime());
	}

	private Date shiftDate(Date aDate) {
		Date theDate = DateUtil.adjustToDayEnd(aDate);

		switch (this.getConfig().getType()) {
			case DAY:
				return DateUtil.addDays(theDate, -1);
			case WEEK:
				return DateUtil.addDays(theDate, -7);
			case MONTH:
				return DateUtil.addMonths(theDate, -1);
			case YEAR:
				return DateUtil.addYears(theDate, -1);
			default:
				return theDate;
		}
	}
}
