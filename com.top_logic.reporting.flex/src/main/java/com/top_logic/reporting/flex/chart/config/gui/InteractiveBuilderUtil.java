/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.FormGroup;



/**
 * Helper class to work with {@link InteractiveBuilder}.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class InteractiveBuilderUtil {

	/**
	 * Builds the UI by recursively calling fillContainer for all properties with return type
	 * {@link InteractiveBuilder}.
	 * 
	 * @param container
	 *        the container to fill
	 * @param item
	 *        the builder config for the current container
	 * @param observer
	 *        the chart-context used in {@link InteractiveChartBuilder}
	 */
	public static void fillContainer(FormContainer container, ConfigurationItem item, ChartContextObserver observer) {
		fillContainer(container, item, observer, ResPrefix.legacyString(item.getConfigurationInterface().getName()));
	}

	/**
	 * Builds the UI by recursively calling fillContainer for all properties with return type
	 * {@link InteractiveBuilder}.
	 * 
	 * @param container
	 *        the container to fill
	 * @param item
	 *        the builder config for the current container
	 * @param observer
	 *        the chart-context used in {@link InteractiveChartBuilder}
	 * @param resView
	 *        the resource prefix for the new container
	 */
	public static void fillContainer(FormContainer container, ConfigurationItem item, ChartContextObserver observer, ResPrefix resView) {
		for (PropertyDescriptor property : item.descriptor().getPropertiesOrdered()) {
			Object value = item.value(property);
			String propertyName = property.getPropertyName();
			if (value instanceof Collection) {
				int i = 0;
				FormContainer propertyContainer = new FormGroup(propertyName, resView);
				for (Object val : (Collection<?>) value) {
					if (val instanceof InteractiveBuilder) {
						i++;
						FormContainer innerContainer = new FormGroup(propertyName + i, resView);
						createUI(val, innerContainer, observer);
						propertyContainer.addMember(innerContainer);
					} else if (val instanceof PolymorphicConfiguration<?>) {
						Object builder =
								SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
								.getInstance((PolymorphicConfiguration<?>) val);
						if (builder instanceof InteractiveBuilder) {
							i++;
							FormContainer innerContainer = new FormGroup(propertyName + i, resView);
							createUI(builder, innerContainer, observer);
							propertyContainer.addMember(innerContainer);
						}
					}
				}
				if (i > 0) {
					container.addMember(propertyContainer);
				}
			}
			else if (value instanceof InteractiveBuilder) {
				FormContainer propertyContainer = new FormGroup(propertyName, resView);
				createUI(value, propertyContainer, observer);
				container.addMember(propertyContainer);
			}
			else if (value instanceof PolymorphicConfiguration<?>) {
				try {
					Object builder =
						SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
							.getInstance((PolymorphicConfiguration<?>) value);
					if (builder instanceof InteractiveBuilder) {
						FormContainer propertyContainer = new FormGroup(propertyName, resView);
						createUI(builder, propertyContainer, observer);
						container.addMember(propertyContainer);
					}
				}
				catch (Exception ex) {
					Logger.error("Could not fill container from instanciated config.", ex, InteractiveBuilderUtil.class);
				}
			}
		}

	}

	private static void createUI(Object builder, FormContainer propertyContainer, ChartContextObserver observer) {
		((InteractiveBuilder) builder).createUI(propertyContainer, observer);
	}

	/**
	 * Util-method to add a named sub-container to the given container.
	 * 
	 * @param parent
	 *        the parent {@link FormContainer} to add the new one to
	 * @param name
	 *        the name of the new {@link FormContainer}
	 * @param item
	 *        the item to be displayed in this container used to create an adequate res-prefix
	 * @return a new FormContainer with the given name added to the given parent
	 */
	public static FormContainer addContainer(FormContainer parent, String name, ConfigurationItem item) {
		return addContainer(parent, name, item, ResPrefix.legacyString(item.getConfigurationInterface().getName()));
	}

	/**
	 * Util-method to add a named sub-container to the given container.
	 * 
	 * @param parent
	 *        the parent {@link FormContainer} to add the new one to
	 * @param name
	 *        the name of the new {@link FormContainer}
	 * @param item
	 *        the item to be displayed in this container used to create an adequate res-prefix
	 * @param resView
	 *        the resource prefix for the new container
	 * @return a new FormContainer with the given name added to the given parent
	 */
	public static FormContainer addContainer(FormContainer parent, String name, ConfigurationItem item, ResPrefix resView) {
		FormContainer group = new FormGroup(name, resView);
		parent.addMember(group);
		return group;
	}

	/**
	 * Same as {@link #create(FormContainer, ConfigurationItem)} but initializes a new
	 * {@link ConfigurationItem} of the given target-type with the values retrieved from the GUI.
	 */
	public static <T extends ConfigurationItem> T create(FormContainer container, ConfigurationItem ui, Class<T> target) {
		Map<String, Object> map = create(container, ui);
		T result = TypedConfiguration.newConfigItem(target);
		for (PropertyDescriptor prop : result.descriptor().getProperties()) {
			String propertyName = prop.getPropertyName();
			if (ConfigurationItem.CONFIGURATION_INTERFACE_NAME.equals(propertyName)) {
				continue;
			}
			Object val = map.get(propertyName);
			if (val != null) {
				result.update(prop, val);
			}
		}
		return result;
	}
	
	/**
	 * Appropriate counterpart to
	 * {@link #fillContainer(FormContainer, ConfigurationItem, ChartContextObserver)}. Recursively
	 * retrieves all values from the GUI for the interactive parts. For convenience-reason all not
	 * interactive parts are also added to the result-map by simply adding the property-value.
	 * 
	 * @param container
	 *        the container that has been filled for the given item of a interactive builder
	 * @param item
	 *        the item with interactive properties
	 * @return a Map with property-name to value where the value may be retrieved from the GUI in
	 *         case of interactive builder properties
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, Object> create(FormContainer container, ConfigurationItem item) {
		Map<String, Object> result = new HashMap<>();
		for (PropertyDescriptor property : item.descriptor().getProperties()) {
			String propertyName = property.getPropertyName();
			Object value = item.value(property);
			if (!container.hasMember(propertyName)) {
				result.put(propertyName, value);
				continue;
			}
			FormContainer propertyContainer = container.getContainer(propertyName);

			if (value instanceof Collection) {
				List<Object> created = new ArrayList<>();
				int i = 0;
				for (Object val : (Collection<?>) value) {
					if (val instanceof InteractiveBuilder) {
						i++;
						FormContainer innerContainer = propertyContainer.getContainer(propertyName + i);
						created.add(((InteractiveBuilder) val).build(innerContainer));
					}
					else if (val instanceof PolymorphicConfiguration<?>) {
						try {
							Object instance =
								SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
									.getInstance((PolymorphicConfiguration<?>) val);
							if (instance instanceof InteractiveBuilder) {
								i++;
								FormContainer innerContainer = propertyContainer.getContainer(propertyName + i);
								Object create = ((InteractiveBuilder) instance).build(innerContainer);
								if (create instanceof ConfiguredInstance) {
									created.add(((ConfiguredInstance) create).getConfig());
								}
							}
							else {
								created.add(val);
							}
						} catch (Exception ex) {
							created.add(val);
							Logger.error("Could not fill container from instanciated config.", ex,
								InteractiveBuilderUtil.class);
						}
					}
					else {
						created.add(val);
					}
				}
				result.put(propertyName, created);
			}
			else if (value instanceof InteractiveBuilder) {
				result.put(propertyName, ((InteractiveBuilder) value).build(propertyContainer));
			}
			else if (value instanceof PolymorphicConfiguration<?>) {
				try {
					Object instance =
						SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
							.getInstance((PolymorphicConfiguration<?>) value);
					if (instance instanceof InteractiveBuilder) {
						Object create = ((InteractiveBuilder) instance).build(propertyContainer);
						if (create instanceof ConfiguredInstance) {
							result.put(propertyName, ((ConfiguredInstance) create).getConfig());
						}
					}
				} catch (Exception ex) {
					result.put(propertyName, value);
					Logger.error("Could not create value from instanciated config.", ex, InteractiveBuilderUtil.class);
				}
			}
			else {
				result.put(propertyName, value);
			}
		}
		return result;
	}

}
