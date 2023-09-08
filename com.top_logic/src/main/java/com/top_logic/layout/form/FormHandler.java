/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import java.util.Collection;

import com.top_logic.basic.listener.EventType;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;

/**
 * Interface for accessing the model of a form.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormHandler extends ChangeHandler, NamedModel {

	/**
	 * {@link EventType type} for changing {@link FormContext}s.
	 * 
	 * @see FormContextListener
	 */
	EventType<FormContextListener, FormHandler, FormContext> FORM_CONTEXT_EVENT =
		new EventType<>("formContext") {
	
			@Override
			public Bubble dispatch(FormContextListener listener, FormHandler sender, FormContext oldValue,
					FormContext newValue) {
				return listener.handleFormContextChanged(sender, oldValue, newValue);
			}

		};

	/**
	 * The corresponding forms model.
	 */
	public FormContext getFormContext();

	/**
	 * Whether this {@link FormHandler} has already (lazily) allocated its {@link FormContext}.
	 * 
	 * @see #getFormContext()
	 */
	public boolean hasFormContext();

	@Override
	default boolean isChanged() {
		return hasFormContext() && getFormContext().isChanged();
	}

	@Override
	default boolean hasError() {
		return hasFormContext() && !getFormContext().checkAllConstraints();
	}

	@Override
	default String getChangeDescription() {
		if (!hasFormContext()) {
			return null;
		}
	
		FormContext formContext = getFormContext();
		Collection<FormMember> changedMembers = formContext.getChangedMembers();
		if (changedMembers.isEmpty()) {
			return null;
		}
	
		StringBuffer buffer = new StringBuffer();
		for (FormMember member : changedMembers) {
			buffer.append(member.getLabel());
			buffer.append(", ");
		}
		if (buffer.length() > 0) {
			buffer.setLength(buffer.length() - 2);
		}
		return buffer.toString();
	}

}