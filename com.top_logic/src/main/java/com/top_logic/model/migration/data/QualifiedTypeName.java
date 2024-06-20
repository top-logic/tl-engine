/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.migration.data;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.ListConfigValueProvider;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.equal.EqualityByValue;
import com.top_logic.basic.func.Function1;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.util.QualifiedTypeNameConstraint;
import com.top_logic.model.util.TLModelUtil;

/**
 * Qualified name of a {@link TLType}.
 * 
 * <p>
 * {@link #getName()} is the actual qualified name. {@link QualifiedTypeName#getModuleName()}
 * computes the name of the {@link TLModule} from the qualified name and
 * {@link QualifiedTypeName#getTypeName()} computes the name of the {@link TLType} from the
 * qualified name.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Format(QualifiedTypeName.Format.class)
public interface QualifiedTypeName extends NamedConfigMandatory, EqualityByValue {

	/**
	 * The qualified name of the {@link TLType type}.
	 */
	@Override
	@Constraint(QualifiedTypeNameConstraint.class)
	String getName();

	/**
	 * The name of the {@link TLModule} in the qualified name.
	 */
	@Nullable
	@Derived(fun = ModuleName.class, args = @Ref(NAME_ATTRIBUTE))
	String getModuleName();

	/**
	 * The name of the {@link TLType} in the qualified name.
	 */
	@Nullable
	@Derived(fun = TypeName.class, args = @Ref(NAME_ATTRIBUTE))
	String getTypeName();

	/**
	 * Serialization format of a {@link QualifiedTypeName}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Format extends AbstractConfigurationValueProvider<QualifiedTypeName> {

		/**
		 * Creates a new {@link Format}.
		 */
		public Format() {
			super(QualifiedTypeName.class);
		}

		@Override
		protected QualifiedTypeName getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			QualifiedTypeName result = TypedConfiguration.newConfigItem(QualifiedTypeName.class);
			result.setName(propertyValue.toString());
			return result;
		}

		@Override
		protected String getSpecificationNonNull(QualifiedTypeName configValue) {
			return configValue.getName();
		}

	}

	/**
	 * Serialization format for lists of {@link QualifiedTypeName} separated by comma.
	 */
	public static final class ListFormat extends ListConfigValueProvider<QualifiedTypeName> {

		private static final String SEPARATOR = ",";

		private ConfigurationValueProvider<QualifiedTypeName> _inner = new Format();

		@Override
		public List<QualifiedTypeName> getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			String[] encodedValues = propertyValue.toString().split(SEPARATOR);
			List<QualifiedTypeName> result = new ArrayList<>(encodedValues.length);
			for (String encodedValue : encodedValues) {
				encodedValue = encodedValue.trim();
				result.add(_inner.getValue(propertyName, encodedValue));
			}
			return result;
		}

		@Override
		public String getSpecificationNonNull(List<QualifiedTypeName> configValues) {
			StringBuilder encodedEnums = new StringBuilder();
			boolean addSeparator = false;
			for (QualifiedTypeName value : configValues) {
				if (addSeparator) {
					encodedEnums.append(SEPARATOR);
					encodedEnums.append(' ');
				} else {
					addSeparator = true;
				}
				encodedEnums.append(_inner.getSpecification(value));
			}
			return encodedEnums.toString();
		}

	}

	/**
	 * Computation of the type name of a {@link QualifiedTypeName}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class TypeName extends Function1<String, String> {

		@Override
		public String apply(String qualifiedName) {
			if (qualifiedName == null) {
				return null;
			}
			int moduleTypeSepIndex = qualifiedName.lastIndexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
			if (moduleTypeSepIndex >= 0) {
				int typePartSepIndex = qualifiedName.lastIndexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR);
				if (typePartSepIndex >= 0) {
					// qualified name of a TLTypePart
					return qualifiedName.substring(moduleTypeSepIndex + 1, typePartSepIndex);
				} else {
					// qualified name of a TLType
					return qualifiedName.substring(moduleTypeSepIndex + 1);
				}
			} else {
				// qualified name of a TLModule
				return null;
			}
		}

	}

	/**
	 * Computation of the module name of a {@link QualifiedTypeName}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class ModuleName extends Function1<String, String> {

		@Override
		public String apply(String qualifiedName) {
			if (qualifiedName == null) {
				return null;
			}
			int moduleTypeSepIndex = qualifiedName.lastIndexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
			if (moduleTypeSepIndex >= 0) {
				// qualified name of a TLType or TLTypePart
				return qualifiedName.substring(0, moduleTypeSepIndex);
			} else {
				// qualified name of a TLModule
				return qualifiedName;
			}
		}

	}

}
