/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;

/**
 * A TL-Script property of an element or channel whose script sees the {@link Inputs inputs} of
 * its configuration and the values the element {@link Supplies supplies} as variables.
 *
 * <p>
 * The variables of such a script are, in this order:
 * </p>
 *
 * <ol>
 * <li>the {@link Inputs#getInputs() inputs} (or the {@link SingleInput#getInput() single input}) of
 * the configuration holding the property, each named after its channel or by its
 * {@value CommaSeparatedInputRefs#AS} name. A configuration without inputs of its own, a part of an
 * element, sees the inputs of the element containing it, see {@link #inputs(ConfigurationItem,
 * PropertyDescriptor)} and {@link InputsOf},</li>
 * <li>the values the element supplies on each evaluation, as declared by the {@link Supplies}
 * annotations of the property.</li>
 * </ol>
 *
 * <p>
 * A later name shadows an equal earlier one. The script uses the variables as <code>$name</code>
 * without declaring them; a script declaring a parameter is a configuration error, see
 * {@link ViewScripts}.
 * </p>
 *
 * <p>
 * To give a script property these variables, annotate it with
 * {@link ScriptContextVariables}({@link Variables}) for the script editor and one {@link Supplies}
 * per supplied value, compile it with {@link #compile(ConfigurationItem, String)}, and evaluate
 * the result with the values of {@link ChannelInputs#arguments(List, Object...)} - the current
 * values of the inputs followed by the supplied values.
 * </p>
 */
public final class InputScript {

	private InputScript() {
		// Static utilities only.
	}

	/**
	 * Compiles the script in the given property of the given configuration.
	 *
	 * @param config
	 *        The configuration holding the script.
	 * @param property
	 *        The name of the script property.
	 * @return The executor taking the values of the {@link #variables(ConfigurationItem, PropertyDescriptor)
	 *         variables} in their order, {@code null} when the property holds no script.
	 */
	public static QueryExecutor compile(ConfigurationItem config, String property) {
		PropertyDescriptor descriptor = config.descriptor().getProperty(property);
		if (descriptor == null) {
			throw new IllegalArgumentException(
				"No property '" + property + "' in '" + config.getConfigurationInterface().getName() + "'.");
		}
		Expr script = (Expr) config.value(descriptor);
		return ViewScripts.compileOptional(script, variables(config, descriptor));
	}

	/**
	 * The variables a script in the given property sees.
	 *
	 * @param config
	 *        The configuration holding the script.
	 * @param property
	 *        The script property.
	 * @return The names of the {@link #inputs(ConfigurationItem, PropertyDescriptor) inputs},
	 *         followed by the names of the supplied values.
	 */
	public static List<String> variables(ConfigurationItem config, PropertyDescriptor property) {
		List<String> result = new ArrayList<>(InputRef.names(inputs(config, property)));
		for (Supplies supplied : supplies(property)) {
			result.add(suppliedName(config, supplied));
		}
		return result;
	}

	/**
	 * The inputs a script in the given property sees.
	 *
	 * @param config
	 *        The configuration holding the script.
	 * @param property
	 *        The script property.
	 * @return The {@link Inputs#getInputs() inputs} or the {@link SingleInput#getInput() input} of
	 *         the nearest configuration having them, starting at the given one and continuing with
	 *         its containers; with an {@link InputsOf} annotation on the property, those of the
	 *         nearest configuration of the declared type. Empty, if there is no such configuration.
	 */
	public static List<InputRef> inputs(ConfigurationItem config, PropertyDescriptor property) {
		InputsOf inputsOf = property.getAnnotation(InputsOf.class);
		ConfigurationItem item = config;
		while (item != null) {
			if (inputsOf == null || inputsOf.value().isInstance(item)) {
				if (item instanceof Inputs inputs) {
					return inputs.getInputs();
				}
				if (item instanceof SingleInput single) {
					InputRef input = single.getInput();
					return input == null ? Collections.emptyList() : List.of(input);
				}
			}
			item = item instanceof ConfigPart part ? part.container() : null;
		}
		return Collections.emptyList();
	}

	/**
	 * The {@link Supplies} declarations of the given property, in their order.
	 */
	public static List<Supplies> supplies(PropertyDescriptor property) {
		Supplies single = property.getAnnotation(Supplies.class);
		if (single != null) {
			return List.of(single);
		}
		Supplies.List multiple = property.getAnnotation(Supplies.List.class);
		if (multiple != null) {
			return List.of(multiple.value());
		}
		return Collections.emptyList();
	}

	/**
	 * The name of the variable holding a supplied value.
	 *
	 * @param config
	 *        The configuration holding the script.
	 * @param supplied
	 *        The declaration of the supplied value.
	 * @return The name configured in the property {@link Supplies#renamedBy()} of the nearest
	 *         configuration declaring it, starting at the given one and continuing with its
	 *         containers; the {@link Supplies#value() default name} when none of them sets it.
	 */
	public static String suppliedName(ConfigurationItem config, Supplies supplied) {
		String renamedBy = supplied.renamedBy();
		if (!renamedBy.isEmpty()) {
			ConfigurationItem item = config;
			while (item != null) {
				PropertyDescriptor property = item.descriptor().getProperty(renamedBy);
				if (property != null) {
					Object name = item.value(property);
					if (name instanceof String text && !text.isBlank()) {
						return text.trim();
					}
					break;
				}
				item = item instanceof ConfigPart part ? part.container() : null;
			}
		}
		return supplied.value();
	}

	/**
	 * {@link ScriptContextVariablesProvider} offering the
	 * {@link InputScript#variables(ConfigurationItem, PropertyDescriptor) variables} of an
	 * {@link InputScript} property.
	 */
	public static class Variables implements ScriptContextVariablesProvider {

		@Override
		public List<String> getVariables(ValueModel valueModel) {
			return variables(valueModel.getModel(), valueModel.getProperty());
		}
	}
}
