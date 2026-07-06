/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.ArrayList;
import com.top_logic.base.locking.token.LockInfo;
import com.top_logic.base.locking.token.TokenService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;

/**
 * {@link ViewAction} returning the current cluster locks for display in a {@link LockTable}, and
 * optionally force-releasing the selected lock first.
 *
 * <p>
 * The returned {@code List<LockInfo>} is meant to be written to the channel feeding the
 * {@link LockTable}. Releasing a lock delegates to the {@link TokenService}, which handles its own
 * transaction.
 * </p>
 *
 * @implNote Releasing calls {@link TokenService#release(java.util.Collection)} with the selected
 *           lock's tokens; the snapshot is read via {@link TokenService#getAllLocks()}.
 */
public class LockListAction implements ViewAction {

	/**
	 * What a {@link LockListAction} does before returning the lock list.
	 */
	public enum Mode {
		/** Just return the current lock snapshot. */
		REFRESH,

		/** Force-release the lock passed as input, then return the updated snapshot. */
		RELEASE;
	}

	/**
	 * Configuration for {@link LockListAction}.
	 *
	 * <p>
	 * App-specific action, referenced by {@code class=} in the lock view rather than claiming a global
	 * {@code @TagName}.
	 * </p>
	 */
	public interface Config extends PolymorphicConfiguration<LockListAction> {

		/** Configuration name for {@link #getMode()}. */
		String MODE = "mode";

		@Override
		@ClassDefault(LockListAction.class)
		Class<? extends LockListAction> getImplementationClass();

		/**
		 * What the action does before returning the lock list.
		 */
		@Name(MODE)
		@Mandatory
		Mode getMode();
	}

	private final Mode _mode;

	/**
	 * Creates a new {@link LockListAction} from configuration.
	 */
	@CalledByReflection
	public LockListAction(InstantiationContext context, Config config) {
		_mode = config.getMode();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (_mode == Mode.RELEASE && input instanceof LockInfo lock) {
			TokenService.getInstance().release(lock.getTokens());
		}
		return new ArrayList<>(TokenService.getInstance().getAllLocks());
	}
}
