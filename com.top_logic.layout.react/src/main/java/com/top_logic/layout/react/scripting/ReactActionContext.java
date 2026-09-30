/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.scripting;

import jakarta.servlet.http.HttpSession;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.scripting.runtime.AbstractActionContext;
import com.top_logic.layout.scripting.runtime.Application;
import com.top_logic.layout.scripting.runtime.ApplicationSession;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * The {@link com.top_logic.layout.scripting.runtime.ActionContext} for resolving {@link
 * com.top_logic.layout.scripting.recorder.ref.ModelName}s in the React view layer.
 *
 * <p>
 * It carries only what the React view layer actually has — a {@link DisplayContext} and the {@link
 * HttpSession} — and resolves through the registered {@link
 * com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme}s via {@link AbstractActionContext}.
 * Schemes that resolve relative to a value context (a {@link ReactOptionScope}, a selected row's
 * container, …) or against the {@link com.top_logic.knowledge.service.KnowledgeBase} resolve without
 * further ado. {@link #getMainLayout()} throws: the React view layer has no component-tree {@link
 * MainLayout}, so a scheme that demands one is not applicable here and says so loudly rather than
 * silently mis-resolving.
 * </p>
 */
public final class ReactActionContext extends AbstractActionContext {

	private final HttpSession _session;

	/**
	 * Creates a {@link ReactActionContext}.
	 *
	 * @param displayContext
	 *        The ambient {@link DisplayContext} of the request driving the resolution.
	 * @param session
	 *        The {@link HttpSession} of the acting user.
	 */
	public ReactActionContext(DisplayContext displayContext, HttpSession session) {
		super(displayContext);
		_session = session;
	}

	@Override
	public HttpSession getSession() {
		return _session;
	}

	@Override
	public MainLayout getMainLayout() {
		throw new UnsupportedOperationException("The React view layer has no MainLayout.");
	}

	@Override
	public Application getApplication() {
		throw new UnsupportedOperationException("The React view layer has no scripting Application.");
	}

	@Override
	public ApplicationSession getApplicationSession() {
		return null;
	}

	@Override
	public GlobalVariableStore getGlobalVariableStore() {
		return GlobalVariableStore.getElseCreateFromSession();
	}

}
