/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.lang.reflect.Field;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link ModelNamingScheme} that represents the value of a static field.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class JavaConstantNaming extends AbstractModelNamingScheme<Object, JavaConstantNaming.Name> {

	/**
	 * Configuration for a {@link JavaConstantNaming}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Name extends ModelName {

		/**
		 * The class in which the constant field is defined.
		 */
		@Mandatory
		Class<?> getDeclaringClass();

		/**
		 * Setter for {@link #getDeclaringClass()}.
		 */
		void setDeclaringClass(Class<?> clazz);
		
		/**
		 * The name of the static field to locate.
		 */
		@Mandatory
		String getFieldName();

		/**
		 * Setter for {@link #getFieldName()}.
		 */
		void setFieldName(String fieldName);
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	public Object locateModel(ActionContext context, Name name) {
		Object objectInstance = null; // field is static, object is ignored.
		try {
			Class<?> declaringClass = name.getDeclaringClass();
			String fieldName = name.getFieldName();
			Field field = declaringClass.getField(fieldName);
			return field.get(objectInstance);
		} catch (IllegalArgumentException ex) {
			throw fail(name, ex);
		} catch (IllegalAccessException ex) {
			throw fail(name, ex);
		} catch (NoSuchFieldException ex) {
			throw fail(name, ex);
		} catch (SecurityException ex) {
			throw fail(name, ex);
		}
	}

	private RuntimeException fail(Name name, Exception ex) {
		return ApplicationAssertions.fail(name, "Unable to get value of field '" + name.getFieldName()
			+ "' defined in class '" + name.getDeclaringClass() + "'.", ex);
	}

	@Override
	protected void initName(Name name, Object model) {
		throw new UnsupportedOperationException(
			"It is impossible to determine declaring class and field name from the object instance.");
	}

	@Override
	protected boolean isCompatibleModel(Object model) {
		return false;
	}

}

