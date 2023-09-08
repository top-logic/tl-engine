/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import java.util.Date;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.DateTimeField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Component to select date in which a model should be displayed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DateCompareSelector extends DateSelectComponent {

	/** Name of the field containing the comparison date. */
	public static final String COMPARE_VERSION_FIELD = "compareVersion";

	/**
	 * Creates a new {@link DateCompareSelector}.
	 */
	public DateCompareSelector(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FormContext createFormContext() {
		DateTimeField compareVersionField = new DateTimeField(COMPARE_VERSION_FIELD, null, false);
		compareVersionField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				DateCompareSelector.this.handleCompareVersionChanged((Date) oldValue, (Date) newValue);
			}

		});

		FormContext context = super.createFormContext();
		context.addMember(compareVersionField);
		return context;
	}

	void handleCompareVersionChanged(Date oldValue, Date newValue) {
		if (newValue == null) {
			dispatchCompareAlgorithm(null);
		} else {
			final Revision newRevision = HistoryUtils.getRevisionAt(newValue.getTime());
			if (oldValue != null) {
				Revision oldRevision = HistoryUtils.getRevisionAt(oldValue.getTime());
				if (oldRevision.equals(newRevision)) {
					/* The date changed, but the revision remains the same. No change necessary. */
					return;
				}
			}
			dispatchCompareAlgorithm(new RevisionCompareAlgorithm(newRevision));
		}
	}

	private void dispatchCompareAlgorithm(final CompareAlgorithm compareAlgorithm) {
		getParent().acceptVisitorRecursively(new DefaultDescendingLayoutVisitor() {

			@Override
			public boolean visitLayoutComponent(LayoutComponent aComponent) {
				if (aComponent instanceof CompareAlgorithmHolder) {
					((CompareAlgorithmHolder) aComponent).setCompareAlgorithm(compareAlgorithm);
				}
				return super.visitLayoutComponent(aComponent);
			}
		});
	}

}

