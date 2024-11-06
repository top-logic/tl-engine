/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

import javax.lang.model.type.TypeKind;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.CommaSeparatedEnum;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.model.TLType;

/**
 * Meta-annotation for {@link TLAnnotation} types that specify the {@link TypeKind}s this annotation
 * is compatible with.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TargetType {

	/**
	 * The {@link TLTypeKind}s the annotated annotation is compatible with.
	 * 
	 * <p>
	 * {@link #name()} is used to define optional names for {@link TLType} that are compatible with
	 * the annotated annotation.
	 * </p>
	 */
	@Format(CommaSeparatedKinds.class)
	TLTypeKind[] value();

	/**
	 * This property returns optional type names that specify the super types, the annotated
	 * annotation is compatible with.
	 */
	String[] name() default {};

	/**
	 * Format for {@link TargetType#value()}.
	 */
	class CommaSeparatedKinds extends AbstractConfigurationValueProvider<TLTypeKind[]> {

		private CommaSeparatedEnum<TLTypeKind> _inner;

		/**
		 * Creates a {@link CommaSeparatedKinds}.
		 */
		public CommaSeparatedKinds() {
			super(TLTypeKind[].class);

			_inner = new CommaSeparatedEnum<>(TLTypeKind.class);
		}

		@Override
		protected TLTypeKind[] getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			return _inner.getValueNonEmpty(propertyName, propertyValue).toArray(new TLTypeKind[0]);
		}

		@Override
		protected String getSpecificationNonNull(TLTypeKind[] configValue) {
			return _inner.getSpecificationNonNull(Arrays.asList(configValue));
		}
	}

}
