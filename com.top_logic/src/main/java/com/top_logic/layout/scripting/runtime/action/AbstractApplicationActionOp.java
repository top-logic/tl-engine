/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.List;

import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.layout.window.WindowManager;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutUtils;

/**
 * Base class for {@link ApplicationActionOp} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractApplicationActionOp<S extends ApplicationAction> implements ApplicationActionOp<S> {

	/** Treat as private! I will make it private as soon as I have some time for it. */
	protected final S config;

	/**
	 * Creates a {@link AbstractApplicationActionOp} from a configuration.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} for instantiation of dependent
	 *        configured objects.
	 * @param config
	 *        The configuration of this {@link AbstractApplicationActionOp}.
	 */
	public AbstractApplicationActionOp(InstantiationContext context, S config) {
		this.config = config;
	}

	@Override
	public S getConfig() {
		return config;
	}

	@Override
	public final Object process(ActionContext context, Object argument) throws ApplicationAssertion {
		setWindowScope(context, getRecordedWindowScope(context));
		try {
			return processInternal(context, argument);
		} catch (ApplicationAssertion ex) {
			throw ex;
		} catch (Throwable throwable) {
			throw ApplicationAssertions.fail(config, "Script execution failed", throwable);
		} finally {
			/* The WindowScope is not switched back, as that would cause errors to appear in the
			 * ScriptingGui and not in their correct window. That happens, as the code for showing
			 * the error runs when this method has already returned. If the WindowScope is switched
			 * back, the error-handling code uses the original WindowScope (i.e. the WindowScope of
			 * the ScriptingGui) and not the recorded WindowScope. */
		}
	}

	private void setWindowScope(ActionContext context, WindowScope newWindowScope) {
		LayoutUtils.setWindowScope(context.getDisplayContext(), newWindowScope);
	}

	private WindowScope getRecordedWindowScope(ActionContext context) {
		ComponentName windowName = getConfig().getWindowName();
		if (windowName == null) {
			/* If no window-name has been recorded, the action happened in the main window. */
			return context.getMainLayout().getWindowScope();
		}
		WindowManager windowManager = context.getMainLayout().getWindowManager();
		WindowComponent window = windowManager.getWindowByName(windowName);
		if (window == null) {
			List<ComponentName> windowNames =
				Mappings.map((openedWindow) -> openedWindow.getName(), windowManager.getWindows());
			throw ApplicationAssertions.fail(getConfig(), "Window with name '" + windowName + "' not found. Known Windows: " + windowNames +".");
		}
		return window.getWindowScope();
	}

	/**
	 * Subclasses have to override the this method instead of
	 * {@link #process(ActionContext, Object)} directly.
	 */
	protected abstract Object processInternal(ActionContext context, Object argument) throws Throwable;

	/**
	 * Throws an {@ApplicationAssertion} with the given message.
	 * 
	 * @param message
	 *        Is not allowed to be null.
	 * @result This method returns never. The return type is only for convenience, to write "throw
	 *         fail(...)" when Java demands either a "return" or a "throw" statement.
	 */
	protected RuntimeException fail(String message) {
		throw ApplicationAssertions.fail(getConfig(), message);
	}

}
