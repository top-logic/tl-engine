/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.trigger;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.ai.service.agents.background.BackgroundAgentConfig;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.ItemOptionMapping;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.OptionMappingProvider;

/**
 * Option provider for the trigger property that hides {@link PreTurnTrigger} and
 * {@link PostTurnTrigger} when the scope is
 * {@link com.top_logic.ai.service.agents.background.BackgroundAgentConfig.Scope#APPLICATION}.
 *
 * <p>
 * Pre-turn and post-turn triggers operate within the context of a chat session and are not
 * compatible with application-scoped agents.
 * </p>
 */
public class TriggerOptionsForScope extends Function1<ListOptionModel<Class<?>>, BackgroundAgentConfig.Scope>
		implements OptionMappingProvider {

	private final Class<?> _type;

	/**
	 * Creates a {@link TriggerOptionsForScope}.
	 */
	@CalledByReflection
	public TriggerOptionsForScope(DeclarativeFormOptions options) {
		PropertyDescriptor property = options.getProperty();
		_type = property.getElementType();
	}

	@Override
	public ListOptionModel<Class<?>> apply(BackgroundAgentConfig.Scope scope) {
		List<? extends Class<?>> options =
			TypeIndex.getInstance().getSpecializations(_type, true, true, false).stream()
				.filter(type -> isAllowedForScope(type, scope))
				.collect(Collectors.toList());

		return new DefaultListOptionModel<>(options);
	}

	private boolean isAllowedForScope(Class<?> type, BackgroundAgentConfig.Scope scope) {
		if (scope == BackgroundAgentConfig.Scope.APPLICATION) {
			return !PreTurnTrigger.class.isAssignableFrom(type)
				&& !PostTurnTrigger.class.isAssignableFrom(type);
		}
		return true;
	}

	@Override
	public OptionMapping getOptionMapping() {
		return ItemOptionMapping.INSTANCE;
	}
}
