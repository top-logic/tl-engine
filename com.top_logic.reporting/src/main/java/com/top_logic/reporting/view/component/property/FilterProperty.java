/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.property;

import java.util.function.Consumer;

import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.util.Utils;

/**
 * A {@link FilterProperty} defines an object that has a name and an initial value. Besides it has
 * an a form member of type {@link AbstractFormField} that is created by the specific concrete class
 * depending on the specific type.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public abstract class FilterProperty {

	private final String name;

	private final String configBaseName;

	private final BoundComponent component;

	private Object initialValue;

	AbstractFormField formMember;

	/**
	 * Creates a new {@link FilterProperty}.
	 */
	public FilterProperty(String name, Object initialValue, BoundComponent aComponent) {
		this.name = name;
		this.component = aComponent;
		this.configBaseName = aComponent.getName().qualifiedName();

		this.initialValue = initialValue;
		loadFromPersonalConfiguration(getConfigBaseName(), this::setNonNullInitialValue);
	}

	/**
	 * Set the initial value.
	 * Changes to the value will not take place if value had been rendered.
	 */
	public void setInitialValue(Object value) {
	    this.initialValue = value;
	}
	
	/**
	 * Sets the given value as initial value when not <code>null</code>.
	 */
	public final void setNonNullInitialValue(Object value) {
		if (value != null) {
			setInitialValue(value);
		}
	}

	/**
	 * Gets the current field value.
	 */
	public Object getValue() {
		return getFormMember().getValue();
	}

	protected BoundComponent getComponent() {
		return component;
	}

	protected abstract AbstractFormField createNewFormMember();

	/**
	 * Converts the given field value to a personal configuration value.
	 */
	protected abstract Object getValueForPersonalConfiguration(Object fieldValue);

	/**
	 * Converts the given personal configuration value to a field value.
	 */
	protected abstract Object getValueFromPersonalConfiguration(Object confValue);

	public String getConfigBaseName() {
		return configBaseName;
	}

	public String getName() {
		return name;
	}

	/**
	 * Gets the initial field value.
	 */
	public Object getInitialValue() {
		return initialValue;
	}

	public AbstractFormField getFormMember() {
		if (formMember == null) {
			formMember = createNewFormMember();
			if (formMember != null) {
				formMember.addValueListener(new ValueListener() {
					@Override
					public void valueChanged(FormField field, Object oldValue, Object newValue) {
						if (!Utils.equals(oldValue, newValue)) {
							saveToPersonalConfig(getConfigBaseName(), newValue);
						}
					}
				});
			}
		}
		return formMember;
	}

	public void resetFormMember() {
		this.formMember = null;
	}

	/**
	 * Loads the value from the personal configuration, stored formerly using the given base key.
	 * 
	 * @param configBaseKey
	 *        Base key for the actual configuration key to load value for.
	 */
	public void loadConfiguredValue(String configBaseKey) {
		loadFromPersonalConfiguration(configBaseKey, storedValue -> formMember.setValue(storedValue));
	}

	/**
	 * Saves the current value to the personal configuration, using the given base key.
	 * 
	 * @param configBaseKey
	 *        Base key for the actual configuration key to save value to.
	 */
	public void saveValueToConfiguration(String configBaseKey) {
		saveToPersonalConfig(configBaseKey, formMember.getValue());
	}

	/**
	 * Deletes the configuration for this {@link FilterProperty} from the personal configuration.
	 * This {@link FilterProperty} is not changed.
	 * 
	 * @param configBaseKey
	 *        Base key for the actual configuration key to delete value from.
	 */
	public void deleteConfiguration(String configBaseKey) {
		PersonalConfiguration thePC = PersonalConfiguration.getPersonalConfiguration();
		if (thePC != null) {
			thePC.setValue(configKey(configBaseKey), null);
		}
	}

	/**
	 * Loads the value from the configuration. If the
	 * {@link #getValueFromPersonalConfiguration(Object) transformed value} is not
	 * <code>null</code>, it is offered to the given {@link Consumer}.
	 * 
	 * @param configKeyBase
	 *        The base for the config key. See {@link #getConfigBaseName()}.
	 * @param sink
	 *        {@link Consumer} that is filled with the value from the personal configuration.
	 * 
	 * @see #saveToPersonalConfig(String, Object)
	 */
	protected void loadFromPersonalConfiguration(String configKeyBase, Consumer<Object> sink) {
		PersonalConfiguration thePC = PersonalConfiguration.getPersonalConfiguration();
		if (thePC == null) {
			return; 
		}
		Object valueFromPersonalConfiguration =
			getValueFromPersonalConfiguration(thePC.getValue(configKey(configKeyBase)));
		sink.accept(valueFromPersonalConfiguration);
	}

	/**
	 * Stores the given value to the personal configuration
	 * 
	 * @param configKeyBase
	 *        The base for the config key. See {@link #getConfigBaseName()}.
	 * @param value
	 *        The value to store.
	 * 
	 * @see #loadFromPersonalConfiguration(String, Consumer)
	 */
	protected void saveToPersonalConfig(String configKeyBase, Object value) {
		PersonalConfiguration thePC = PersonalConfiguration.getPersonalConfiguration();
		if (thePC != null) {
			thePC.setValue(configKey(configKeyBase), getValueForPersonalConfiguration(value));
		}
	}

	private String configKey(String configKeyBase) {
		return configKeyBase + '.' + getName();
	}

}