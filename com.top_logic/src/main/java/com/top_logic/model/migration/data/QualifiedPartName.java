/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.migration.data;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.func.Function1;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Qualified name of a {@link TLTypePart}.
 *
 * <p>
 * {@link #getName()} is the actual qualified name. {@link QualifiedPartName#getModuleName()}
 * computes the name of the {@link TLModule} from the qualified name,
 * {@link QualifiedPartName#getTypeName()} computes the name of the {@link TLType} from the
 * qualified name, and {@link QualifiedPartName#getPartName()} computes the name of the
 * {@link TLTypePart} from the qualified name.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Format(QualifiedPartName.Format.class)
public interface QualifiedPartName extends QualifiedTypeName {

	/**
	 * The name of the {@link TLTypePart} in the qualified name.
	 */
	@Nullable
	@Derived(fun = PartName.class, args = @Ref(NAME_ATTRIBUTE))
	String getPartName();


	/**
	 * Serialization format of a {@link QualifiedPartName}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Format extends AbstractConfigurationValueProvider<QualifiedPartName> {

		/**
		 * Creates a new {@link Format}.
		 */
		public Format() {
			super(QualifiedPartName.class);
		}

		@Override
		protected QualifiedPartName getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			QualifiedPartName result = TypedConfiguration.newConfigItem(QualifiedPartName.class);
			result.setName(propertyValue.toString());
			return result;
		}

		@Override
		protected String getSpecificationNonNull(QualifiedPartName configValue) {
			return configValue.getName();
		}

	}

	/**
	 * Computation of the part name of a {@link QualifiedPartName}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class PartName extends Function1<String, String> {

		@Override
		public String apply(String qualifiedName) {
			if (qualifiedName == null) {
				return null;
			}
			int typePartSepIndex = qualifiedName.lastIndexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR);
			if (typePartSepIndex >= 0) {
				// qualified name of a TLTypePart
				return qualifiedName.substring(typePartSepIndex + 1);
			} else {
				// qualified name of a TLModule or TLType
				return null;
			}
		}

	}

}

