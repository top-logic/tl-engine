/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.revision;

import java.util.Date;
import java.util.Map;

import com.top_logic.basic.SessionContext;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.component.AbstractFormCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.monitoring.revision.ChangeSetTreeBuilder.ChangeSetTreeRoot;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.FormFieldHelper;

/**
 * Computes the new selection for the filter component of the change set tree.
 * 
 * @implNote A range whose start lies behind its end is normalized by exchanging the bounds. With a
 *           {@link Config#getShift() shift}, the range is moved by its own width before it is
 *           displayed. The filter fields are updated to the displayed range.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UpdateChangeSetTreeCommand extends AbstractFormCommandHandler {

	private static final Filter<ChangeSet> HIDE_SYSTEM_CHANGES = new Filter<>() {

		@Override
		public boolean accept(ChangeSet anObject) {
			CommitEvent commit = anObject.getCommit();
			if (commit == null) {
				return false;
			}
			return commit.getAuthor().startsWith(SessionContext.PERSON_ID_PREFIX);
		}
	};

	/**
	 * Configuration options for {@link UpdateChangeSetTreeCommand}.
	 */
	public interface Config extends AbstractFormCommandHandler.Config {

		/**
		 * Configuration name for {@link #getShift()}.
		 */
		String SHIFT = "shift";

		/**
		 * By how many widths of the selected range it is moved before it is displayed.
		 * 
		 * <p>
		 * <code>-1</code> displays the range before the selected one, <code>1</code> the range
		 * after it, and <code>0</code> the selected range itself.
		 * </p>
		 */
		@Name(SHIFT)
		int getShift();

	}

	/**
	 * Creates a new {@link UpdateChangeSetTreeCommand}.
	 */
	public UpdateChangeSetTreeCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult applyChanges(LayoutComponent component, FormContext formContext, Object model, Map<String, Object> arguments) {
		RangeMode rangeMode =
			ChangeSetSelector.getRangeMode((SelectField) formContext.getField(ChangeSetSelector.RANGE_MODE_FIELD));
		boolean timeRange = rangeMode == RangeMode.TIME_RANGE;
		Date rangeStart = (Date) formContext.getField(ChangeSetSelector.RANGE_START_FIELD).getValue();
		Date rangeEnd = (Date) formContext.getField(ChangeSetSelector.RANGE_END_FIELD).getValue();
		Number revisionStart = (Number) formContext.getField(ChangeSetSelector.REVISION_START_FIELD).getValue();
		Number revisionEnd = (Number) formContext.getField(ChangeSetSelector.REVISION_END_FIELD).getValue();
		boolean noRange = timeRange ? rangeStart == null && rangeEnd == null
			: revisionStart == null && revisionEnd == null;
		if (noRange) {
			return HandlerResult.error(I18NConstants.ERROR_NO_RANGE);
		}

		int shift = ((Config) getConfig()).getShift();
		if (timeRange) {
			if (rangeStart != null && rangeEnd != null && rangeStart.after(rangeEnd)) {
				Date tmp = rangeStart;
				rangeStart = rangeEnd;
				rangeEnd = tmp;
			}
			if (shift != 0) {
				if (rangeStart == null || rangeEnd == null) {
					return HandlerResult.error(I18NConstants.ERROR_SHIFT_OPEN_RANGE);
				}
				Date[] shifted = RevisionRanges.shift(PersistencyLayer.getKnowledgeBase().getHistoryManager(),
					rangeStart, rangeEnd, shift);
				rangeStart = shifted[0];
				rangeEnd = shifted[1];
			}
			formContext.getField(ChangeSetSelector.RANGE_START_FIELD).setValue(rangeStart);
			formContext.getField(ChangeSetSelector.RANGE_END_FIELD).setValue(rangeEnd);
		} else {
			if (revisionStart != null && revisionEnd != null
				&& revisionStart.longValue() > revisionEnd.longValue()) {
				Number tmp = revisionStart;
				revisionStart = revisionEnd;
				revisionEnd = tmp;
			}
			if (shift != 0) {
				if (revisionStart == null || revisionEnd == null) {
					return HandlerResult.error(I18NConstants.ERROR_SHIFT_OPEN_RANGE);
				}
				long[] shifted = RevisionRanges.shift(PersistencyLayer.getKnowledgeBase().getHistoryManager(),
					revisionStart.longValue(), revisionEnd.longValue(), shift);
				revisionStart = Long.valueOf(shifted[0]);
				revisionEnd = Long.valueOf(shifted[1]);
			}
			formContext.getField(ChangeSetSelector.REVISION_START_FIELD)
				.setValue(ChangeSetSelector.toLong(revisionStart));
			formContext.getField(ChangeSetSelector.REVISION_END_FIELD).setValue(ChangeSetSelector.toLong(revisionEnd));
		}

		boolean includeTechnicalObjects =
			FormFieldHelper.getbooleanValue(formContext.getMember(ChangeSetSelector.SHOW_TECHNICAL_FIELD));
		boolean includeSystemChanges =
			FormFieldHelper.getbooleanValue(formContext.getMember(ChangeSetSelector.SHOW_SYSTEM_CHANGES_FIELD));
		Filter<? super ChangeSet> filter;
		if (includeSystemChanges) {
			filter = TrueFilter.INSTANCE;
		} else {
			filter = HIDE_SYSTEM_CHANGES;
		}

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		HistoryManager hm = kb.getHistoryManager();
		long firstCommit;
		long lastCommit;
		if (!timeRange) {
			firstCommit = revisionStart == null ? hm.getFirstRevision() : revisionStart.longValue();
			lastCommit = revisionEnd == null ? hm.getLastRevision() : revisionEnd.longValue() - 1;
		} else {
			// The first revision created at or after the start of the range.
			firstCommit = rangeStart == null ? hm.getFirstRevision()
				: hm.getRevisionAt(rangeStart.getTime() - 1).getCommitNumber() + 1;
			// The last revision created before the end of the range.
			lastCommit = rangeEnd == null ? hm.getLastRevision()
				: hm.getRevisionAt(rangeEnd.getTime() - 1).getCommitNumber();
		}

		ChangeSetTreeRoot newSelection =
			newInstance(kb, firstCommit, lastCommit, includeTechnicalObjects, filter);
		((Selectable) component).setSelected(newSelection);

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Creates the {@link ChangeSetTreeRoot} displaying the revisions with commit numbers between
	 * the given commit numbers (both inclusive).
	 */
	private ChangeSetTreeRoot newInstance(KnowledgeBase kb, long firstCommit, long lastCommit,
			boolean showSystemObjects, Filter<? super ChangeSet> filter) {
		HistoryManager hm = kb.getHistoryManager();
		long first = Math.max(firstCommit, hm.getFirstRevision());
		long last = Math.min(lastCommit, hm.getLastRevision());

		Revision startRev;
		Revision stopRev;
		if (first > last) {
			// Empty range.
			startRev = null;
			stopRev = null;
		} else {
			startRev = hm.getRevision(first);
			stopRev = hm.getRevision(last);
		}
		return new ChangeSetTreeRoot(kb, startRev, stopRev, showSystemObjects, filter);
	}

}

