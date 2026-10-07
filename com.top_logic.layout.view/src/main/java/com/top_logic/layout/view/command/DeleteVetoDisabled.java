/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.Collection;
import java.util.Optional;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} disabling a command whose input is an object that refuses its
 * deletion.
 *
 * <p>
 * An object vetoing its deletion (see {@link TLObject}) cannot be deleted, independent of the access
 * rights of the user: the deletion would be rejected at commit. The command is disabled and gives
 * the veto as its reason. For a collection input, each object in it is checked, the first veto
 * decides. An input that is no object is nothing to check: the rule answers
 * executable, so that it composes with a rule like {@link NullInputDisabled} that decides about a
 * missing input.
 * </p>
 *
 * <p>
 * The {@link DeleteObjectAction} brings this rule of its own. Configure it explicitly for a command
 * deleting its input by other means, e.g. by a script:
 * </p>
 *
 * <pre>
 * &lt;executability&gt;
 *   &lt;null-input-disabled/&gt;
 *   &lt;delete-veto-disabled/&gt;
 * &lt;/executability&gt;
 * </pre>
 *
 * @implNote The veto is {@link TLObject#tDeleteVeto()}.
 */
public class DeleteVetoDisabled implements ViewExecutabilityRule {

	/**
	 * Configuration for {@link DeleteVetoDisabled}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends ViewExecutabilityRule.Config {

		/** Tag name of a {@link DeleteVetoDisabled} in a list of executability rules. */
		String TAG_NAME = "delete-veto-disabled";

		@Override
		@ClassDefault(DeleteVetoDisabled.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();
	}

	/** Singleton instance. */
	public static final DeleteVetoDisabled INSTANCE = new DeleteVetoDisabled();

	private DeleteVetoDisabled() {
		// Singleton.
	}

	/**
	 * Creates a {@link DeleteVetoDisabled} from configuration.
	 */
	@CalledByReflection
	public DeleteVetoDisabled(InstantiationContext context, Config config) {
		// No configuration.
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		if (input instanceof Collection<?> collection) {
			for (Object element : collection) {
				ExecutableState state = check(element);
				if (!state.isExecutable()) {
					return state;
				}
			}
			return ExecutableState.EXECUTABLE;
		}
		return check(input);
	}

	private static ExecutableState check(Object input) {
		if (input instanceof TLObject object) {
			Optional<ResKey> veto = object.tDeleteVeto();
			if (veto.isPresent()) {
				return ExecutableState.createDisabledState(veto.get());
			}
		}
		return ExecutableState.EXECUTABLE;
	}
}
