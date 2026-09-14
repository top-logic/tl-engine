/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.chat;

import com.top_logic.ai.chat.view.AgentChatElement;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * The demo's own {@code <demo-agent-chat/>}: the chat plus the invoices of this application.
 *
 * <p>
 * It is what a host element looks like. The composition itself lives in the browser
 * ({@code DemoChat} in {@code tl-ai-demo/ui}), which the React element name points at; the only
 * thing the server has to add is the data the composition shows - the invoices of
 * {@code tl.ai.demo.imageclassification:Invoice}, read by {@link DemoChatControl} and put into the
 * control's state.
 * </p>
 *
 * @see AgentChatElement
 */
public class DemoChatElement extends AgentChatElement {

	/** Configuration for {@link DemoChatElement}. */
	@TagName("demo-agent-chat")
	public interface Config extends AgentChatElement.Config {

		/**
		 * Configuration property name for {@link #getInvoiceType()}.
		 *
		 * @see #getInvoiceType()
		 */
		String INVOICE_TYPE = "invoice-type";

		@Override
		@ClassDefault(DemoChatElement.class)
		Class<? extends UIElement> getImplementationClass();

		@Override
		@StringDefault("DemoChat")
		String getReactElement();

		/**
		 * Qualified name of the type whose instances the "Rechnungen" section lists.
		 */
		@Name(INVOICE_TYPE)
		@StringDefault("tl.ai.demo.imageclassification:Invoice")
		String getInvoiceType();
	}

	private final Config _config;

	/** Creates a {@link DemoChatElement}. */
	@CalledByReflection
	public DemoChatElement(InstantiationContext context, Config config) {
		super(context, config);
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new DemoChatControl(context, _config.getTraceMode(), _config.getPromptImproverAgent(),
			_config.getReactElement(), _config.getInvoiceType());
	}
}
