/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.values.Fields.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.col.Equality;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.func.Identity;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.PasswordInputControlProvider;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.Fields.UIConversion;
import com.top_logic.layout.form.values.edit.ConfigLabelProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.IdentityOptionMapping;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link Editor} creating the UI for a plain (atomic) property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ValueEditor extends AbstractEditor {

	/**
	 * Singleton {@link ValueEditor} instance.
	 */
	public static final ValueEditor INSTANCE = new ValueEditor();

	/**
	 * Creates a {@link ValueEditor}.
	 */
	protected ValueEditor() {
		super();
	}

	@Override
	protected FormField addField(EditorFactory editorFactory, FormContainer container, ValueModel model,
			String fieldName) {
		final FormField field;
		PropertyDescriptor property = model.getProperty();
		Class<?> type = property.getType();

		Mapping<Object, Object> uiConversion = Identity.INSTANCE;
		Mapping<Object, Object> storageConversion = Converters.valueConverter(property);

		DeclarativeFormOptions formOptions = editorFactory.formOptions(property);
		DerivedProperty<? extends Iterable<?>> optionProvider = optionProvider(formOptions);
		boolean hasOptionProvider = optionProvider != null;
		boolean hasFormatAnnotation = property.getAnnotation(Format.class) != null;
		boolean hasBinding = property.getValueBinding() != null;
		boolean isSpecializedPrimitive = hasOptionProvider || hasFormatAnnotation || hasBinding;

		if (!isSpecializedPrimitive && type == String.class) {
			field = line(container, fieldName);
		} else if (!isSpecializedPrimitive && (type == Boolean.class || type == boolean.class)) {
			field = checkbox(container, fieldName);
		} else if (!isSpecializedPrimitive
			&& (type == Double.class || type == double.class || type == Float.class || type == float.class)) {
			field = doubleField(container, fieldName);
		} else if (!isSpecializedPrimitive && type == Date.class) {
			field = calendar(container, fieldName);
		} else if (!isSpecializedPrimitive && (type == Byte.class || type == byte.class)) {
			field = byteField(container, fieldName);
		} else if (!isSpecializedPrimitive && (type == Short.class || type == short.class)) {
			field = shortField(container, fieldName);
		} else if (!isSpecializedPrimitive && (type == Integer.class || type == int.class)) {
			field = intField(container, fieldName);
		} else if (!isSpecializedPrimitive && (type == Long.class || type == long.class)) {
			field = longField(container, fieldName);
		} else {
			boolean isEnum = Enum.class.isAssignableFrom(property.getElementType());
			PropertyKind kind = property.kind();
			boolean isCollection = kind == PropertyKind.LIST || kind == PropertyKind.MAP || kind == PropertyKind.ARRAY;

			if (hasOptionProvider || isEnum || isCollection) {
				final SelectField select;
				if (isCollection || Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)
					|| type.isArray()) {
					select = multiSelectField(container, fieldName);
					boolean hasCustomOrder = List.class.isAssignableFrom(type);
					select.setCustomOrder(hasCustomOrder);
				} else {
					select = selectField(container, fieldName);
				}
				Labels.emptyLabel(select, model);
				LabelProvider optionLabels = optionLabelsOrNull(formOptions);
				if (optionLabels == null && (isCollection || kind == PropertyKind.ITEM)) {
					optionLabels = new ConfigLabelProvider();
				}
				select.setOptionLabelProvider(optionLabels);
				if (hasOptionProvider) {
					bindOptions(select, property, model.getModel(), optionProvider, true);
					select.setOptionComparator(getOptionComparator(property, optionLabels));
				}
				if (!hasOptionProvider) {
					if (isEnum) {
						options(select, Arrays.asList(property.getElementType().getEnumConstants()));
						select.setOptionComparator(Equality.INSTANCE);
					}
				}
				field = select;

				uiConversion = new UIConversion(select);
				if (optionProvider != null) {
					final OptionMapping optionMapping = Fields.optionMapping(optionProvider);
					if (optionMapping != IdentityOptionMapping.INSTANCE) {
						select.set(OPTION_MAPPING, optionMapping);
						final Mapping<Object, Object> defaultConversion = storageConversion;
						storageConversion = new Mapping<>() {
							@Override
							public Object map(Object uiValue) {
								if (uiValue instanceof Collection<?>) {
									Collection<?> uiCollection = (Collection<?>) uiValue;
									return uiCollection.stream()
										.map(e -> optionMapping.toSelection(defaultConversion.map(e)))
										.collect(Collectors.toList());
								} else {
									return optionMapping.toSelection(defaultConversion.map(uiValue));
								}
							}
						};
					}
				}
			} else {
				return PlainEditor.INSTANCE.addField(editorFactory, container, model, fieldName);
			}
		}

		if (editorFactory.getAnnotation(property, Encrypted.class) != null) {
			field.setControlProvider(PasswordInputControlProvider.INSTANCE);
		}

		init(editorFactory, model, field, uiConversion, storageConversion);

		return field;
	}

	/**
	 * Converts any {@link Throwable} into a "fake" {@link TopLogicException} that has the original
	 * messages of the given exception and its causes as literal text messages.
	 * 
	 * <p>
	 * The reason for using this method is to prevent an ERROR in the log when a
	 * {@link TopLogicException} with a non {@link TopLogicException} reason is thrown. Just leaving
	 * out the exception causes makes analyzing the problem hard. Therefore its better to have
	 * reason messages in the wrong language instead of only a generic internationalized message.
	 * </p>
	 * 
	 * @param ex
	 *        The {@link Throwable} to convert.
	 * @param messages
	 *        A buffer of messages to skip.
	 * @return The "fake" {@link TopLogicException}.
	 */
	static TopLogicException wrap(Throwable ex, Set<String> messages) {
		if (ex == null) {
			return null;
		}
		if (ex instanceof TopLogicException) {
			return (TopLogicException) ex;
		}

		String message = ex.getMessage();
		if (message == null) {
			message = ex.getClass().getSimpleName();
		}
		if (messages == null) {
			messages = new HashSet<>();
		}
		if (messages.add(message)) {
			return new TopLogicException(ResKey.text(message), wrap(ex.getCause(), messages));
		} else {
			// Prevent duplicate messages.
			return wrap(ex.getCause(), messages);
		}
	}
}
