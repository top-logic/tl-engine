/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime;

import javax.servlet.http.HttpSession;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * The {@link ActionContext} for an {@link ApplicationAction} that is executed "live", meaning: In a
 * running system with the tester watching the GUI.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class LiveActionContext extends AbstractActionContext {

	private final MainLayout mainLayout;

	private final HttpSession session;

	/**
	 * Convenience shortcut for: {@link #LiveActionContext(HttpSession, MainLayout, DisplayContext)}
	 */
	public LiveActionContext(DisplayContext displayContext, LayoutComponent component) {
		this(
			displayContext.asRequest().getSession(),
			component.getMainLayout(),
			displayContext);
	}

	/** Creates a new {@link LiveActionContext}. */
	public LiveActionContext(HttpSession session, MainLayout mainLayout, DisplayContext displayContext) {
		super(displayContext);
		this.session = session;
		this.mainLayout = mainLayout;
	}

	@Override
	public HttpSession getSession() {
		return session;
	}

	@Override
	public MainLayout getMainLayout() {
		return mainLayout;
	}

	@Override
	public Application getApplication() {
		throw new UnsupportedOperationException();
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