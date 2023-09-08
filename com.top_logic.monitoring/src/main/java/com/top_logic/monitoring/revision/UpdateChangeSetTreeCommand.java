/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.revision;

import java.util.Date;
import java.util.Map;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.component.AbstractFormCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.monitoring.revision.ChangeSetTreeBuilder.ChangeSetTreeRoot;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.FormFieldHelper;

/**
 * Computes the new selection for the filter component of the change set tree.
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
	 * Creates a new {@link UpdateChangeSetTreeCommand}.
	 */
	public UpdateChangeSetTreeCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult applyChanges(LayoutComponent component, FormContext formContext, Object model, Map<String, Object> arguments) {
		Date date = FormFieldHelper.getDateValue(formContext.getMember(ChangeSetSelector.RANGE_START_FIELD));
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

		ChangeSetTreeRoot newSelection =
			newInstance(PersistencyLayer.getKnowledgeBase(), date, includeTechnicalObjects, filter);
		((Selectable) component).setSelected(newSelection);

		return HandlerResult.DEFAULT_RESULT;
	}

	private ChangeSetTreeRoot newInstance(KnowledgeBase kb, Date day, boolean showSystemObjects,
			Filter<? super ChangeSet> filter) {
		Revision firstRev = kb.getHistoryManager().getRevision(Revision.FIRST_REV);
		Revision startRev;
		Date dayBegin = DateUtil.adjustToDayBegin(day);
	
		if (dayBegin.getTime() <= firstRev.getDate()) {
			// time given that is before the first revision
			startRev = firstRev;
		} else {
			startRev = HistoryUtils.getRevisionAt(dayBegin.getTime());
		}
		Revision stopRev;
		Date dayEnd = DateUtil.adjustToDayEnd(day);
	
		if (dayEnd.getTime() <= firstRev.getDate()) {
			// time given that is before the first revision
			stopRev = firstRev;
		} else {
			stopRev = HistoryUtils.getRevisionAt(dayEnd.getTime());
		}
		return new ChangeSetTreeRoot(kb, startRev, stopRev, showSystemObjects, filter);
	}

}

