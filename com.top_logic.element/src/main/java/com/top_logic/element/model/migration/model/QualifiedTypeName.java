/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.equal.EqualityByValue;
import com.top_logic.basic.func.Function1;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
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
