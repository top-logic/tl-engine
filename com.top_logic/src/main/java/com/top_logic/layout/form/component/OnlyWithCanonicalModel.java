/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} that hides a command, if it is not executed on its canonical model
 * (selected by {@link CommandHandler#getTargetModel(LayoutComponent, Map)}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class OnlyWithCanonicalModel implements ExecutabilityRule {

	private final AbstractCommandHandler _handler;

	/**
	 * Creates a {@link OnlyWithCanonicalModel}.
	 *
	 * @param handler
	 *        The {@link CommandHandler} that is checked.
	 */
	public OnlyWithCanonicalModel(AbstractCommandHandler handler) {
		_handler = handler;
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> values) {

		// Note: Here is not the actual target model requested (this is passed as the model
		// argument) but the target model that the command would normally select when
		// executed on a component.
		@SuppressWarnings("deprecation")
		Object canonicalTargetModel = _handler.getTargetModel(component, values);

		if (isEquivalent(model, canonicalTargetModel)) {
			return ExecutableState.EXECUTABLE;
		} else {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
	}

	private boolean isEquivalent(Object left, Object right) {
		if (Objects.equals(left, right)) {
			return true;
		}
		Object unwrappedLeft = unwrapCollection(left);
		Object unwrappedRight = unwrapCollection(right);
		return Objects.equals(unwrappedLeft, unwrappedRight);
	}

	private Object unwrapCollection(Object object) {
		if (!(object instanceof Collection)) {
			return object;
		}
		Collection<?> collection = (Collection<?>) object;
		if (collection.isEmpty()) {
			return null;
		}
		if (collection.size() == 1) {
			return collection.iterator().next();
		}
		return collection;
	}

}