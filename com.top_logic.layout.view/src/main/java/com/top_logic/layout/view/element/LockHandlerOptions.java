/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.base.locking.handler.DefaultLockHandler;
import com.top_logic.base.locking.handler.LockHandler;
import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Configuration of a view element that locks the persistent objects it edits.
 *
 * <p>
 * A {@link LockHandler} holds the lock it acquired, so every control displaying the element needs a
 * handler of its own, see {@link #createLockHandler(LockHandlerOptions)}: the element is shared by
 * all windows displaying its view.
 * </p>
 */
public interface LockHandlerOptions extends ConfigurationItem {

	/** Configuration name for {@link #getLockHandler()}. */
	String LOCK_HANDLER = "lockHandler";

	/**
	 * Algorithm for locking the persistent object being edited.
	 *
	 * <p>
	 * A form locks the persistent object it displays while it is in edit mode; a table edited in
	 * place locks the object of a row while it writes a value of its cells. A transient object
	 * (e.g. the draft of a create dialog) is not locked. Every displayed form or table creates its
	 * own handler, so that the locks of different forms, tables, windows and users are
	 * independent. Defaults to {@link DefaultLockHandler} which uses the application-wide
	 * {@link com.top_logic.base.locking.LockService LockService}. To disable locking, configure
	 * {@link NoTokenHandling}.
	 * </p>
	 */
	@Name(LOCK_HANDLER)
	@ItemDefault
	@ImplementationClassDefault(DefaultLockHandler.class)
	@Options(fun = AllInAppImplementations.class)
	@DisplayMinimized
	PolymorphicConfiguration<? extends LockHandler> getLockHandler();

	/**
	 * Reports a {@link #getLockHandler() lock handler} that cannot be instantiated when the view
	 * is loaded, before a control needs one.
	 */
	static void checkLockHandler(InstantiationContext context, LockHandlerOptions options) {
		context.getInstance(options.getLockHandler());
	}

	/**
	 * Creates the {@link LockHandler} of a single control displaying an element with the given
	 * options.
	 *
	 * @throws ConfigurationError
	 *         If the configured handler cannot be instantiated.
	 */
	static LockHandler createLockHandler(LockHandlerOptions options) {
		PolymorphicConfiguration<? extends LockHandler> handlerConfig = options.getLockHandler();
		DefaultInstantiationContext problems = new DefaultInstantiationContext(LockHandlerOptions.class);
		LockHandler result = problems.getInstance(handlerConfig);
		if (problems.hasErrors() || result == null) {
			// Falling back to no locking would let concurrent edits through.
			throw new ConfigurationError("Invalid lock handler at " + handlerConfig.location() + ".");
		}
		return result;
	}

}
