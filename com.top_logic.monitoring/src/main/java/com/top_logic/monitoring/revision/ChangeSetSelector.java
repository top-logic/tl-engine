/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.revision;

import java.util.Date;

import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
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
	 * Field to select the period to display the revisions for. Originally a start - end selector
	 * was planned, but for now just one single day can be selected, resulting in a display of all
	 * revisions of that day (24 hour period)
	 */
	public static final String RANGE_START_FIELD = "rangeStart";

	/**
	 * Singleton {@link ChangeSetSelector} instance.
	 */
	public static final ChangeSetSelector INSTANCE = new ChangeSetSelector();

	private ChangeSetSelector() {
		// Singleton constructor.
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		ComplexField dateField = FormFactory.newDateField(RANGE_START_FIELD, new Date(), !FormFactory.IMMUTABLE);
		BooleanField includeTechnicalObjects =
			FormFactory.newBooleanField(SHOW_TECHNICAL_FIELD, Boolean.FALSE, !FormFactory.IMMUTABLE);
		BooleanField includeSystemChanges =
			FormFactory.newBooleanField(SHOW_SYSTEM_CHANGES_FIELD, Boolean.FALSE, !FormFactory.IMMUTABLE);
		FormContext fc = new FormContext("context", aComponent.getResPrefix());
		fc.addMember(dateField);
		fc.addMember(includeTechnicalObjects);
		fc.addMember(includeSystemChanges);
		return fc;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

}

