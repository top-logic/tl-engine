/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.revision;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.DateTimeField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} creating the form context for the filter component of the {@link ChangeSet
 * change set} tree.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeSetSelector implements ModelBuilder {

	/**
	 * Field name for boolean selector to choose whether to include non-wrapper objects into the
	 * list of changed objects or not.
	 */
	public static final String SHOW_TECHNICAL_FIELD = "showTechnical";

	/**
	 * Field name for boolean selector to choose whether to include changes that are made by system.
	 */
	public static final String SHOW_SYSTEM_CHANGES_FIELD = "showSystemChanges";

	/**
	 * Field to select whether the revisions are selected by a time range or by a range of commit
	 * numbers.
	 * 
	 * @see RangeMode
	 */
	public static final String RANGE_MODE_FIELD = "rangeMode";

	/**
	 * Field to select the point in time (inclusive) from which on the revisions are displayed.
	 * 
	 * <p>
	 * Only visible in {@link RangeMode#TIME_RANGE}.
	 * </p>
	 */
	public static final String RANGE_START_FIELD = "rangeStart";

	/**
	 * Field to select the point in time (exclusive) until which the revisions are displayed.
	 * 
	 * @see #RANGE_START_FIELD
	 */
	public static final String RANGE_END_FIELD = "rangeEnd";

	/**
	 * Field to select the commit number of the first revision (inclusive) to display.
	 * 
	 * <p>
	 * Only visible in {@link RangeMode#REVISION_RANGE}.
	 * </p>
	 */
	public static final String REVISION_START_FIELD = "revisionStart";

	/**
	 * Field to select the commit number of the revision (exclusive) until which the revisions are
	 * displayed.
	 * 
	 * @see #REVISION_START_FIELD
	 */
	public static final String REVISION_END_FIELD = "revisionEnd";

	/**
	 * Singleton {@link ChangeSetSelector} instance.
	 */
	public static final ChangeSetSelector INSTANCE = new ChangeSetSelector();

	private ChangeSetSelector() {
		// Singleton constructor.
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		// Compute the day boundaries in the time zone of the user, in which the fields display
		// their values.
		Calendar calendar = CalendarUtil.createCalendar(ThreadContext.getTimeZone());
		Date today = DateUtil.adjustToDayBegin(calendar);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		Date tomorrow = calendar.getTime();
		DateTimeField rangeStart = new DateTimeField(RANGE_START_FIELD, today, !FormFactory.IMMUTABLE);
		DateTimeField rangeEnd = new DateTimeField(RANGE_END_FIELD, tomorrow, !FormFactory.IMMUTABLE);
		ComplexField revisionStart = FormFactory.newLongField(REVISION_START_FIELD, null, !FormFactory.IMMUTABLE);
		ComplexField revisionEnd = FormFactory.newLongField(REVISION_END_FIELD, null, !FormFactory.IMMUTABLE);
		SelectField rangeMode = FormFactory.newSelectField(RANGE_MODE_FIELD, Arrays.asList(RangeMode.values()),
			!FormFactory.MULTIPLE, Collections.singletonList(RangeMode.TIME_RANGE), !FormFactory.IMMUTABLE);
		rangeMode.setMandatory(true);
		rangeMode.addValueListener((field, oldValue, newValue) -> {
			convertRange(rangeMode, rangeStart, rangeEnd, revisionStart, revisionEnd);
			updateVisibility(rangeMode, rangeStart, rangeEnd, revisionStart, revisionEnd);
		});
		updateVisibility(rangeMode, rangeStart, rangeEnd, revisionStart, revisionEnd);
		BooleanField includeTechnicalObjects =
			FormFactory.newBooleanField(SHOW_TECHNICAL_FIELD, Boolean.FALSE, !FormFactory.IMMUTABLE);
		BooleanField includeSystemChanges =
			FormFactory.newBooleanField(SHOW_SYSTEM_CHANGES_FIELD, Boolean.FALSE, !FormFactory.IMMUTABLE);
		FormContext fc = new FormContext("context", aComponent.getResPrefix());
		fc.addMember(rangeMode);
		fc.addMember(rangeStart);
		fc.addMember(rangeEnd);
		fc.addMember(revisionStart);
		fc.addMember(revisionEnd);
		fc.addMember(includeTechnicalObjects);
		fc.addMember(includeSystemChanges);
		return fc;
	}

	/**
	 * Shows the fields for the selected {@link RangeMode} and hides the others.
	 */
	static void updateVisibility(SelectField rangeMode, FormField rangeStart, FormField rangeEnd,
			FormField revisionStart, FormField revisionEnd) {
		boolean timeRange = getRangeMode(rangeMode) == RangeMode.TIME_RANGE;
		rangeStart.setVisible(timeRange);
		rangeEnd.setVisible(timeRange);
		revisionStart.setVisible(!timeRange);
		revisionEnd.setVisible(!timeRange);
	}

	/**
	 * Fills the fields of the selected {@link RangeMode} with the range given in the fields of the
	 * other mode.
	 * 
	 * <p>
	 * A time range is converted into the revisions created within it, a revision range into the
	 * points in time the bounding revisions were created at. A field with an invalid input is not
	 * converted.
	 * </p>
	 */
	static void convertRange(SelectField rangeMode, FormField rangeStart, FormField rangeEnd,
			FormField revisionStart, FormField revisionEnd) {
		HistoryManager hm = PersistencyLayer.getKnowledgeBase().getHistoryManager();
		if (getRangeMode(rangeMode) == RangeMode.REVISION_RANGE) {
			if (rangeStart.hasValue()) {
				revisionStart.setValue(RevisionRanges.firstCommitAt(hm, (Date) rangeStart.getValue()));
			}
			if (rangeEnd.hasValue()) {
				revisionEnd.setValue(RevisionRanges.firstCommitAt(hm, (Date) rangeEnd.getValue()));
			}
		} else {
			if (revisionStart.hasValue()) {
				rangeStart.setValue(RevisionRanges.dateOfCommit(hm, toLong(revisionStart.getValue()), true));
			}
			if (revisionEnd.hasValue()) {
				rangeEnd.setValue(RevisionRanges.dateOfCommit(hm, toLong(revisionEnd.getValue()), false));
			}
		}
	}

	/**
	 * The given value of a commit number field as {@link Long}.
	 */
	static Long toLong(Object value) {
		return value == null ? null : Long.valueOf(((Number) value).longValue());
	}

	/**
	 * The {@link RangeMode} selected in the given {@link #RANGE_MODE_FIELD}.
	 */
	public static RangeMode getRangeMode(SelectField rangeMode) {
		Object selection = rangeMode.getSingleSelection();
		return selection == null ? RangeMode.TIME_RANGE : (RangeMode) selection;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

}

