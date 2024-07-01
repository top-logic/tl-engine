/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime;

import java.util.Collection;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.action.ApplicationActionOp;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Context for executing {@link ApplicationActionOp}s in an application under test.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ActionContext {

	/**
	 * The current {@link HttpSession} of the current {@link ApplicationSession}.
	 */
	public HttpSession getSession();

	/**
	 * The {@link MainLayout} of the current {@link ApplicationSession}.
	 */
	public MainLayout getMainLayout();

	/**
	 * The {@link Application} of the current {@link ApplicationSession}.
	 */
	public Application getApplication();

	/**
	 * The current test session.
	 * 
	 * <p>
	 * This is an optional operation only available during unit testing. While executing an action
	 * script in a real application, there is no test session available.
	 * </p>
	 * 
	 * @return The test session or <code>null</code> when operating in a live application.
	 */
	ApplicationSession getApplicationSession();

	/**
	 * The {@link DisplayContext} of the current processing.
	 */
	public DisplayContext getDisplayContext();

	/**
	 * Resolve the given {@link ModelName} without a value context.
	 * <p>
	 * If there is a value context, use {@link #resolve(ModelName, Object)} instead.
	 * </p>
	 */
	public Object resolve(ModelName value);

	/**
	 * Resolve the given {@link ModelName} with the given value context.
	 * <p>
	 * If there is no value context, use {@link #resolve(ModelName)} instead.
	 * </p>
	 */
	public Object resolve(ModelName value, Object valueContext);

	/**
	 * Resolve the given {@link ModelName}s without a value context.
	 * <p>
	 * If there is a value context, use {@link #resolveCollection(Class, Collection, Object)}
	 * instead.
	 * </p>
	 */
	public <T> List<T> resolveCollection(Class<T> exprectedType, Collection<? extends ModelName> values);

	/**
	 * Resolve the given {@link ModelName}s with the given value context.
	 * <p>
	 * If there is no value context, use {@link #resolveCollection(Class, Collection)} instead.
	 * </p>
	 */
	public <T> List<T> resolveCollection(Class<T> exprectedType, Collection<? extends ModelName> values,
			Object valueContext);

	/**
	 * Getter for the {@link GlobalVariableStore}.
	 * 
	 * @return Never <code>null</code>.
	 */
	public GlobalVariableStore getGlobalVariableStore();

}