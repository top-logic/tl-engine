/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.SingleValueAssertionPlugin;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;

/**
 * {@link SingleValueAssertionPlugin} to check the currently
 * {@link RootTileComponent#displayedPath() displayed path}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DisplayedPathAssertion extends SingleValueAssertionPlugin<RootTileComponent, StringField> {

	/**
	 * Creates a new {@link DisplayedPathAssertion}.
	 * 
	 * @param model
	 *        The currently inspected {@link RootTileComponent}.
	 */
	public DisplayedPathAssertion(RootTileComponent model) {
		super(model, false, "displayedPathAssertion");
	}

	@Override
	protected StringField createValueField(String name) {
		return FormFactory.newStringField(name);
	}

	@Override
	protected Object getInitialValue() {
		List<String> path = toQualifiedNames(getModel().displayedPath());
		return BreadcrumbStrings.INSTANCE.getSpecification(path);
	}

	@Override
	protected GuiAssertion buildAssertion() {
		List<String> value;
		try {
			value = BreadcrumbStrings.INSTANCE.getValue("value", getExpectedValueField().getAsString());
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		ModelName expected = ModelResolver.buildModelName(toComponentNames(value));
		ModelName actualValue = DisplayedPathNaming.createName(getModel());
		return ActionFactory.valueAssertion(expected, actualValue, getComment());
	}

	/**
	 * Creates qualified names from the corresponding {@link ComponentName}s.
	 */
	public static List<String> toQualifiedNames(Collection<LayoutComponent> names) {
		return names.stream()
			.map(LayoutComponent::getName)
			.map(ComponentName::qualifiedName)
			.collect(Collectors.toList());
	}

	/**
	 * Creates {@link ComponentName}s from the corresponding qualified names.
	 */
	public static List<ComponentName> toComponentNames(Collection<String> qualifiedNames) {
		Mapping<String, ComponentName> nameMapping = new Mapping<>() {

			@Override
			public ComponentName map(String input) {
				try {
					return ComponentName.newConfiguredName("name", input);
				} catch (ConfigurationException ex) {
					throw new ConfigurationError(ex);
				}
			}
			
		};
		return qualifiedNames.stream()
			.map(nameMapping)
			.collect(Collectors.toList());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.TILE_PATH_ASSERTION;
	}

}
