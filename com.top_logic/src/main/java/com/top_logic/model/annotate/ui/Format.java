/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.format.FormatFactory;
import com.top_logic.basic.func.Function2;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} that defines a display format constraint for attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("format")
@InApp
@TargetType({ TLTypeKind.DATE, TLTypeKind.FLOAT, TLTypeKind.INT })
public interface Format extends TLAttributeAnnotation, TLTypeAnnotation, FormatBase {

	// Note: The option provider is defined in the form builder XML of the model editor.
	@Override
	@Mandatory
	PolymorphicConfiguration<? extends FormatFactory> getDefinition();

	/**
	 * Option provider function for {@link Format#getDefinition()}.
	 */
	class FormatOptions extends Function2<ListOptionModel<Class<?>>, TLType, TLTypeKind> {

		private AllInAppImplementationsWithTargetType _inner;

		/**
		 * Creates a {@link FormatOptions}.
		 */
		public FormatOptions(DeclarativeFormOptions options) {
			_inner = new AllInAppImplementationsWithTargetType(options);
		}

		@Override
		public ListOptionModel<Class<?>> apply(TLType arg1, TLTypeKind arg2) {
			return _inner.apply(arg2);
		}

		private final class AllInAppImplementationsWithTargetType extends AllInAppImplementations {
			private final AnnotationCustomizations _customizations;
		
			private AllInAppImplementationsWithTargetType(DeclarativeFormOptions options) {
				super(options);
				_customizations = options.getCustomizations();
			}
		
			public ListOptionModel<Class<?>> apply(TLTypeKind kind) {
				return new PrioritizedListOptionModel(_customizations, filter(kind).collect(Collectors.toList()));
			}
		
			private Stream<? extends Class<?>> filter(TLTypeKind kind) {
				if (kind == null) {
					return Stream.empty();
				}
				return acceptableImplementations()
					.filter(c -> containsKind(_customizations.getAnnotation(c, TargetType.class), kind));
			}
		
			private boolean containsKind(TargetType annotation, TLTypeKind kind) {
				return annotation == null || Arrays.asList(annotation.value()).contains(kind);
			}
		}
	}

}
