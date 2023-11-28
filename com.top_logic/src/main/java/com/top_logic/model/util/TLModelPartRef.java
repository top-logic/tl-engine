/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.regex.Pattern;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.config.AbstractModelPartMapping;
import com.top_logic.model.resources.TLPartScopedResourceProvider;

/**
 * Textual reference to a {@link TLModelPart} that can be safely used in configurations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Format(TLModelPartRef.TLModelPartRefValueProvider.class)
@ControlProvider(SelectionControlProvider.class)
@Options(fun = AllClasses.class, mapping = TLModelPartRef.PartMapping.class)
@OptionLabels(TLPartScopedResourceProvider.class)
public class TLModelPartRef {

	/**
	 * Creates a {@link TLModelPartRef} referencing the given type.
	 */
	public static TLModelPartRef ref(TLModelPart type) {
		return ref(TLModelUtil.qualifiedName(type));
	}

	/**
	 * Creates a {@link TLModelPartRef} referencing the {@link TLModelPart} with the given qualified
	 * name.
	 * 
	 * @see #ref(TLModelPart)
	 * @see TLModelUtil#qualifiedName(com.top_logic.model.TLModelPart)
	 */
	public static TLModelPartRef ref(String qualifiedName) {
		return new TLModelPartRef(qualifiedName);
	}

	final String _qualifiedName;

	/**
	 * Creates a {@link TLModelPartRef}.
	 *
	 * @param qualifiedName
	 *        See {@link #qualifiedName()}.
	 */
	private TLModelPartRef(String qualifiedName) {
		assert !StringServices.isEmpty(qualifiedName);
		_qualifiedName = qualifiedName;
	}

	/**
	 * The qualified name of the referenced {@link TLModelPart}.
	 */
	public String qualifiedName() {
		return _qualifiedName;
	}

	/**
	 * Resolves a referenced type.
	 */
	public TLModelPart resolve() {
		return TLModelUtil.resolveModelPart(qualifiedName());
	}

	/**
	 * Resolves a referenced type.
	 */
	public TLType resolveType() {
		return TLModelUtil.findType(qualifiedName());
	}

	/**
	 * Resolves a referenced type.
	 */
	public TLTypePart resolvePart() {
		return TLModelUtil.findPart(qualifiedName());
	}

	/**
	 * Resolves the referenced type to a {@link TLClass}.
	 */
	public TLClass resolveClass() throws ConfigurationException {
		TLType result = resolveType();
		if (result instanceof TLClass) {
			return (TLClass) result;
		} else {
			throw new ConfigurationException(
				I18NConstants.ERROR_NOT_A_CLASS__VALUE_ACTUAL.fill(qualifiedName(), result.getModelKind()), null,
				qualifiedName());
		}
	}

	@Override
	public String toString() {
		return qualifiedName();
	}

	@Override
	public int hashCode() {
		return _qualifiedName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TLModelPartRef other = (TLModelPartRef) obj;
		return _qualifiedName.equals(other._qualifiedName);
	}

	/**
	 * {@link ConfigurationValueProvider} for {@link TLModelPartRef}s referencing
	 * {@link TLModelPart}s.
	 */
	public static class TLModelPartRefValueProvider extends AbstractRefValueProvider {

		/**
		 * Singleton {@link TLModelPartRefValueProvider} instance.
		 */
		public static final TLModelPartRefValueProvider INSTANCE = new TLModelPartRefValueProvider();

		private TLModelPartRefValueProvider() {
			super(Pattern.compile(TLModelUtil.MODEL_PART_NAME_PATTERN_SRC),
				I18NConstants.ERROR_INVALID_PART_REFERENCE__VALUE);
		}

	}

	/**
	 * {@link ConfigurationValueProvider} for {@link TLModelPartRef}s.
	 */
	public static class AbstractRefValueProvider extends AbstractConfigurationValueProvider<TLModelPartRef> {

		private final Pattern _pattern;

		private final ResKey1 _errorKey;

		/**
		 * Creates a {@link AbstractRefValueProvider}.
		 *
		 * @param pattern
		 *        The value pattern.
		 * @param errorKey
		 *        The error message to report, when a value with an invalid format is parsed.
		 */
		protected AbstractRefValueProvider(Pattern pattern, ResKey1 errorKey) {
			super(TLModelPartRef.class);

			_pattern = pattern;
			_errorKey = errorKey;
		}

		@Override
		protected TLModelPartRef getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			String qualifiedName = propertyValue.toString();
			if (!_pattern.matcher(qualifiedName).matches()) {
				throw new ConfigurationException(_errorKey.fill(qualifiedName),
					propertyName, propertyValue);
			}
			return TLModelPartRef.ref(qualifiedName);
		}

		@Override
		protected String getSpecificationNonNull(TLModelPartRef configValue) {
			return configValue.qualifiedName();
		}

	}

	/**
	 * {@link OptionMapping} for {@link TLModelPartRef} properties.
	 */
	public static class PartMapping extends AbstractModelPartMapping<TLModelPartRef> {

		@Override
		protected TLModelPart resolveName(TLModelPartRef name) throws ConfigurationException {
			return name.resolve();
		}

		@Override
		protected TLModelPartRef buildName(TLModelPart option) {
			return ref(option);
		}

	}

	/**
	 * Function wrapping a {@link TLModelPart} in a textual {@link TLModelPartRef} used in
	 * configurations.
	 */
	public static class BuildRef extends Function1<TLModelPartRef, TLModelPart> {
		@Override
		public TLModelPartRef apply(TLModelPart arg) {
			return arg == null ? null : ref(arg);
		}
	}

	/**
	 * {@link Function1} resolving a {@link TLModelPartRef} to a {@link TLClass}.
	 * 
	 * @see Derived
	 * @see TLModelPartRef#resolveClass()
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ResolveToClass extends Function1<TLClass, TLModelPartRef> {

		@Override
		public TLClass apply(TLModelPartRef arg) {
			if (arg == null) {
				return null;
			}
			try {
				return arg.resolveClass();
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
		}
	}

	/**
	 * {@link Function1} resolving a {@link TLModelPartRef} to a {@link TLType}.
	 * 
	 * @see Derived
	 * @see TLModelPartRef#resolveType()
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ResolveToType extends Function1<TLType, TLModelPartRef> {

		@Override
		public TLType apply(TLModelPartRef arg) {
			if (arg == null) {
				return null;
			}
			return arg.resolveType();
		}
	}

}
