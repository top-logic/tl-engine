/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.form.FormContextListener;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.FormStateBuilder.FormState;
import com.top_logic.layout.form.model.FormContext;

/**
 * {@link GenericPropertyListener} that records transient {@link FormContext} state and re-applies it to
 * newly created form contexts.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormStateHandler implements FormContextListener, ChannelListener {

	private Map<Object, FormState> _stateBySender = new HashMap<>();

	@Override
	public Bubble handleFormContextChanged(FormHandler sender, FormContext oldContext, FormContext newContext) {
		if (oldContext != null) {
			FormState oldState = FormStateBuilder.extractState(oldContext);
			_stateBySender.put(sender, oldState);
		}

		if (newContext != null) {
			FormState state = _stateBySender.remove(sender);
			if (state != null) {
				state.applyTo(newContext);
			}
		}
		return Bubble.BUBBLE;
	}

	@Override
	public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
		_stateBySender.remove(sender.getComponent());
	}

}