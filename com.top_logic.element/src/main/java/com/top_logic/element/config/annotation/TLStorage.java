/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config.annotation;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Function1;
import com.top_logic.element.config.ObjectTypeConfig;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.ClassifiedInAppImplementations;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.OptionMappingProvider;
import com.top_logic.layout.form.values.edit.annotation.AcceptableClassifiers;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} choosing a custom {@link StorageImplementation} for an attribute.
 * 
 * <p>
 * The annotation can either be set on the {@link TLStructuredTypePart}'s type, or directly on a
 * {@link TLStructuredTypePart} itself.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName(TLStorage.TAG_NAME)
@InApp
public interface TLStorage extends TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * Tag name for {@link TLStorage} annotations.
	 */
	String TAG_NAME = "storage-algorithm";

	/**
	 * Classifier selecting {@link StorageImplementation}s that are valid to be chosen for model
	 * references (in contrast to basic-typed properties).
	 */
	String REFERENCE_CLASSIFIER = "reference";

	/**
	 * Classifier selecting {@link StorageImplementation}s that are valid to be chosen for
	 * basic-typed properties.
	 */
	String PRIMITIVE_CLASSIFIER = "primitive";

	/** Property name of {@link #getImplementation()}. */
	String IMPLEMENTATION = "implementation";

	/**
	 * The {@link StorageImplementation} configuration.
	 */
	@Name(IMPLEMENTATION)
	@Options(fun = StorageImplementations.class, args = @Ref(ANNOTATED))
	@AcceptableClassifiers(REFERENCE_CLASSIFIER)
	@DefaultContainer
	@Mandatory
	PolymorphicConfiguration<? extends StorageImplementation> getImplementation();

	/** @see #getImplementation() */
	void setImplementation(PolymorphicConfiguration<? extends StorageImplementation> value);

	/**
	 * Option provider for {@link TLStorage#getImplementation()} that selects
	 * {@link StorageImplementation}s based on the context that is being annotated.
	 */
	class StorageImplementations extends Function1<List<? extends Class<?>>, AnnotatedConfig<?>>
			implements OptionMappingProvider {

		private ClassifiedInAppImplementations _referenceImpl;

		private ClassifiedInAppImplementations _propertyImpl;

		/**
		 * Creates a {@link StorageImplementation}.
		 */
		public StorageImplementations(DeclarativeFormOptions options) {
			_referenceImpl = new ClassifiedInAppImplementations(options, REFERENCE_CLASSIFIER);
			_propertyImpl = new ClassifiedInAppImplementations(options, PRIMITIVE_CLASSIFIER);
		}

		@Override
		public List<? extends Class<?>> apply(AnnotatedConfig<?> context) {
			ClassifiedInAppImplementations impl;
			if (context instanceof ReferenceConfig || context instanceof ObjectTypeConfig) {
				impl = _referenceImpl;
			} else {
				impl = _propertyImpl;
			}
			ListOptionModel<Class<?>> options = impl.apply();
			return options.getBaseModel();
		}

		@Override
		public OptionMapping getOptionMapping() {
			/* It doesn't matter which impl is used, because the option mapping bases on the
			 * property which is the same in both implementations. */
			return _referenceImpl.getOptionMapping();
		}

	}
}
