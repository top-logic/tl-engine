/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyInitializer;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.misc.DescendingConfigurationItemVisitor;
import com.top_logic.basic.shared.string.StringServicesShared;
import com.top_logic.layout.editor.scripting.Identifiers;
import com.top_logic.layout.form.values.edit.initializer.UUIDInitializer;
import com.top_logic.mig.html.layout.LayoutInfo;
import com.top_logic.mig.html.layout.LayoutReference;

/**
 * Visits the {@link ConfigurationItem} and stores all {@link LayoutTemplate}s into the database and
 * replaces afterwards the configured template with a {@link LayoutReference} linking to the layout
 * key where the corresponding {@link LayoutTemplate} is stored in the database.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LayoutTemplateConfigurationVisitor extends DescendingConfigurationItemVisitor {

	List<String> _componentIdentifiers = new ArrayList<>();

	List<String> _uuids = new ArrayList<>();

	/**
	 * Replaces all {@link LayoutTemplate}s with {@link LayoutReference}s and stores the replaced
	 * template into the database.
	 * 
	 * @param config
	 *        {@link ConfigurationItem} which shall be replaced.
	 */
	public void replaceAndStore(ConfigurationItem config) {
		handleProperties(config);
	}

	@Override
	protected void handleListProperty(ConfigurationItem config, PropertyDescriptor property,
			List<? extends ConfigurationItem> listValue) {
		super.handleListProperty(config, property, listValue);

		ListIterator<ConfigurationItem> listIterator = getListIterator(listValue);

		while (listIterator.hasNext()) {
			ConfigurationItem entry = listIterator.next();

			if (entry instanceof LayoutTemplate) {
				listIterator.set(handleLayoutTemplate((LayoutTemplate) entry));
			}
		}
	}

	/**
	 * Identifiers collected during visit.
	 */
	public Identifiers getIdentifiers() {
		Identifiers ids = TypedConfiguration.newConfigItem(Identifiers.class);
		if (!_componentIdentifiers.isEmpty()) {
			/* Optimization: When setting an empty list, the property is written. */
			ids.setComponentKeys(_componentIdentifiers);
		}
		if (!_uuids.isEmpty()) {
			/* Optimization: When setting an empty list, the property is written. */
			ids.setUUIDs(_uuids);
		}
		return ids;
	}

	@Override
	protected void handleItemProperty(ConfigurationItem config, PropertyDescriptor property,
			ConfigurationItem itemValue) {
		super.handleItemProperty(config, property, itemValue);

		if (itemValue instanceof LayoutTemplate) {
			config.update(property, handleLayoutTemplate((LayoutTemplate) itemValue));
		}
	}

	private LayoutReference handleLayoutTemplate(LayoutTemplate entry) {
		String layoutKey = LayoutTemplateUtils.createNewComponentLayoutKey();
		_componentIdentifiers.add(layoutKey);

		LayoutReference layoutReference = LayoutTemplateUtils.createLayoutReference(layoutKey);
		moveLayoutInfoToReference(entry, layoutReference);

		storeLayoutTemplate(entry, layoutKey);

		return layoutReference;
	}

	private void moveLayoutInfoToReference(LayoutTemplate template, LayoutReference reference) {
		ConfigurationItem templateArguments = template.getTemplateArguments();
		
		if (templateArguments != null) {
			PropertyDescriptor property = templateArguments.descriptor().getProperty("layoutInfo");
			if (property != null) {
				LayoutInfo layoutInfo = (LayoutInfo) templateArguments.value(property);
				templateArguments.reset(property);
				if (layoutInfo != null) {
					reference.setLayoutInfo(layoutInfo);
				}
			}
		}
	}

	private void storeLayoutTemplate(LayoutTemplate template, String layoutKey) {
		ConfigurationItem arguments = template.getTemplateArguments();
		String path = template.getTemplatePath();

		if (arguments == null) {
			arguments = TypedConfiguration.newConfigItem(ConfigurationItem.class);
		}

		LayoutTemplateUtils.storeLayout(layoutKey, path, arguments);
	}

	@SuppressWarnings("unchecked")
	private ListIterator<ConfigurationItem> getListIterator(List<? extends ConfigurationItem> listValue) {
		return (ListIterator<ConfigurationItem>) listValue.listIterator();
	}

	@Override
	protected void handlePlainProperty(ConfigurationItem config, PropertyDescriptor property) {
		ensureWellKnownUUID(config, property);
	}

	/**
	 * Ensures that a property with {@link UUIDInitializer} uses a "well-known" identifier for
	 * tests.
	 */
	private void ensureWellKnownUUID(ConfigurationItem config, PropertyDescriptor property) {
		ValueInitializer valueInitializerAnnotation = property.getAnnotation(ValueInitializer.class);
		if (valueInitializerAnnotation != null) {
			Class<? extends PropertyInitializer> initializerClass = valueInitializerAnnotation.value();
			if (initializerClass == UUIDInitializer.class) {
				String idValue = (String) config.value(property);
				if (StringServicesShared.isEmpty(idValue) || !UUIDInitializer.ID_PATTERN.matcher(idValue).matches()) {
					// value was changed by user. Do not change it.
					return;
				}
				String identifier = LayoutTemplateUtils.wellKnownUUIDs();
				if (identifier == null) {
					identifier = idValue;
				}
				_uuids.add(identifier);
				config.update(property, identifier);
			}
		}
	}

}
