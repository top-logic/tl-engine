/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import java.util.Date;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.DateTimeField;
import com.top_logic.layout.form.model.FormContext;

/**
 * Component to select date in which a model should be displayed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DateSelectComponent extends AbstractRevisionSelectComponent {

	static final Property<Boolean> NO_EVENT = TypedAnnotatable.property(Boolean.class, "no event");

	/** Name of the field containing the date in whose revision the model should be displayed. */
	public static final String VERSION_FIELD = "version";

	/**
	 * Creates a new {@link DateSelectComponent}.
	 */
	public DateSelectComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FormContext createFormContext() {
		DateTimeField versionField = new DateTimeField(VERSION_FIELD, null, false);
		versionField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (field.get(NO_EVENT) == null) {
					DateSelectComponent.this.handleVersionChanged((Date) newValue);
				}
			}
		});
		FormContext context = new FormContext(this);
		context.addMember(versionField);
		return context;
	}

	void handleVersionChanged(Date newValue) {
		Revision revision;
		if (newValue != null) {
			revision = HistoryUtils.getRevisionAt(newValue.getTime());
		} else {
			revision = null;
		}
		handleRevisionChanged(revision);
	}

	@Override
	protected Revision getSelectedRevision() {
		if (!hasFormContext()) {
			return null;
		}
		FormField versionField = getFormContext().getField(VERSION_FIELD);
		if (!versionField.hasValue()) {
			return null;
		}
		Date selectedDate = (Date) versionField.getValue();
		if (selectedDate == null) {
			return null;
		}
		return HistoryUtils.getRevisionAt(selectedDate.getTime());
	}

	@Override
	protected void setSelectedRevision(Revision wrapperRevision) {
		FormField versionField = getFormContext().getField(VERSION_FIELD);
		if (versionField.hasValue()) {
			Date currentVersionDate = (Date) versionField.getValue();
			if (currentVersionDate != null) {
				if (wrapperRevision.equals(HistoryUtils.getRevisionAt(currentVersionDate.getTime()))) {
					// Do not change input date if not needed.
					return;
				}
			} else if (wrapperRevision.isCurrent()) {
				// null means current.
				return;
			}
		}
		versionField.set(NO_EVENT, Boolean.TRUE);
		try {
			Date newFieldValue;
			if (wrapperRevision.isCurrent()) {
				newFieldValue = null;
			} else {
				newFieldValue = new Date(wrapperRevision.getDate());
			}
			versionField.setValue(newFieldValue);
		} finally {
			versionField.reset(NO_EVENT);
		}
	}

}
