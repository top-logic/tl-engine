/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.ai.service.agents.background.BackgroundConfig;
import com.top_logic.ai.service.agents.background.trigger.PreTurnTrigger;
import com.top_logic.ai.service.agents.background.trigger.TriggerConfig;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.func.Function2;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.ItemOptionMapping;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.OptionMappingProvider;

/**
 * Option provider for the input property that hides {@link ChatContextInput} when it is not
 * applicable.
 *
 * <p>
 * {@link ChatContextInput} requires a completed chat turn to read from. It is hidden when the
 * trigger is a {@link PreTurnTrigger} (no completed turn yet) or when the scope is
 * {@link com.top_logic.ai.service.agents.background.BackgroundConfig.Scope#APPLICATION} (no chat
 * session at all).
 * </p>
 */
public class InputOptionsForTrigger
		extends Function2<ListOptionModel<Class<?>>, TriggerConfig, BackgroundConfig.Scope>
		implements OptionMappingProvider {

	private final Class<?> _type;

	/**
	 * Creates an {@link InputOptionsForTrigger}.
	 */
	@CalledByReflection
	public InputOptionsForTrigger(DeclarativeFormOptions options) {
		PropertyDescriptor property = options.getProperty();
		_type = property.getElementType();
	}

	@Override
	public ListOptionModel<Class<?>> apply(TriggerConfig trigger, BackgroundConfig.Scope scope) {
		List<? extends Class<?>> options =
			TypeIndex.getInstance().getSpecializations(_type, true, true, false).stream()
				.filter(type -> isAllowed(type, trigger, scope))
				.collect(Collectors.toList());

		return new DefaultListOptionModel<>(options);
	}

	private boolean isAllowed(Class<?> type, TriggerConfig trigger, BackgroundConfig.Scope scope) {
		if (ChatContextInput.class.isAssignableFrom(type)) {
			return !(trigger instanceof PreTurnTrigger) && scope != BackgroundConfig.Scope.APPLICATION;
		}
		return true;
	}

	@Override
	public OptionMapping getOptionMapping() {
		return ItemOptionMapping.INSTANCE;
	}
}
