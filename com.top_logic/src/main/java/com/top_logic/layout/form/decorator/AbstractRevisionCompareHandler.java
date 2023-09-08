/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.decorator.RevisionSelectDialog.RevisionCallback;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractCompareCommandHandler} that opens a dialog to select a revision to compare the
 * model with.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractRevisionCompareHandler<C extends FormComponent> extends AbstractCompareCommandHandler<C> {

	static String COMPARE_REVISION = "__compareRevision";

	/**
	 * Creates a new {@link AbstractRevisionCompareHandler}.
	 */
	public AbstractRevisionCompareHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Returns the {@link Revision} selected in the dialog opened by this handler.
	 * 
	 * @param someArguments
	 *        Arguments given in
	 *        {@link #createCompareObject(DisplayContext, FormComponent, Map)}
	 * @return The revision formerly selected in the dialog, or <code>null</code> when no revision
	 *         was selected.
	 */
	protected Revision getRevision(Map<String, Object> someArguments) {
		return (Revision) someArguments.get(COMPARE_REVISION);
	}

	@Override
	protected HandlerResult openDialog(DisplayContext aContext, LayoutComponent aComponent,
			final Map<String, Object> someArguments) {
		Object model = aComponent.getModel();
		Revision revision = getRevision(someArguments);
		if (revision != null) {
			return super.openDialog(aContext, aComponent, someArguments);
		}
	
		final HandlerResult suspended = HandlerResult.suspended();
		RevisionSelectDialog.newRevisionSelectDialog(model, new RevisionCallback() {
	
			@Override
			public Command commandForRevision(Revision rev) {
				Map<String, Object> args = new HashMap<>(someArguments);
				args.put(COMPARE_REVISION, rev);
				return suspended.resumeContinuation(args);
			}
	
		}).open(aContext);
	
		return suspended;
	}

	@Override
	protected boolean mustNotRecord(DisplayContext context, LayoutComponent component, Map<String, Object> someArguments) {
		if (getCompareObject((C) component) != null) {
			return super.mustNotRecord(context, component, someArguments);
		}
		if (!someArguments.containsKey(COMPARE_REVISION)) {
			/* Do not record the opening of the revision select dialog. Instead, the execution with
			 * the concrete revision is recorded. */
			return true;
		}

		return super.mustNotRecord(context, component, someArguments);
	}



}

