/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.annotation.Annotation;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * Abstract super class for implementations of {@link PropertyDescriptor}.
 * 
 * @param <PD>
 *        Concrete type of the {@link PropertyDescriptor}.
 * @param <CD>
 *        Concrete type of the {@link ConfigurationDescriptor} this
 *        {@link AbstractPropertyDescriptor} belongs to.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FrameworkInternal
public abstract class AbstractPropertyDescriptor<PD extends AbstractPropertyDescriptor<?, ?>, CD extends ConfigurationDescriptor>
		implements PropertyDescriptor {

	private final PD[] _superProperties;

	private final CD _descriptor;

	private final NamedConstant _propertyIdentifier;

	private final NamedConstant _valueSetIdentifier;

	private final NamedConstant _listenerIdentifier;

	/**
	 * Creates a {@link AbstractPropertyDescriptor}.
	 * 
	 * @param configurationDescriptor
	 *        The {@link ConfigurationDescriptor} this property belongs to. See
	 *        {@link #getDescriptor()}.
	 * @param propertyName
	 *        The name of this {@link PropertyDescriptor}. It is used to create
	 *        {@link #identifier()},{@link #getListenerIdentifier()}, and
	 *        {@link #getValueSetIdentifier()}.
	 */
	public AbstractPropertyDescriptor(CD configurationDescriptor, String propertyName,
			PD[] superProperties) {
		_descriptor = configurationDescriptor;
		_superProperties = superProperties;
		if (superProperties.length == 0) {
			_propertyIdentifier = new NamedConstant(propertyName);
			_valueSetIdentifier = new NamedConstant("valueSet(" + propertyName + ")");
			_listenerIdentifier = new NamedConstant("listeners(" + propertyName + ")");
		} else {
			_listenerIdentifier = superProperties[0].getListenerIdentifier();
			_propertyIdentifier = superProperties[0].identifier();
			_valueSetIdentifier = superProperties[0].getValueSetIdentifier();
		}
	}

	@Override
	public PD[] getSuperProperties() {
		return _superProperties;
	}

	@Override
	public NamedConstant identifier() {
		return _propertyIdentifier;
	}

	@Override
	public NamedConstant getValueSetIdentifier() {
		return _valueSetIdentifier;
	}

	@Override
	public NamedConstant getListenerIdentifier() {
		return _listenerIdentifier;
	}

	@Override
	public CD getDescriptor() {
		return _descriptor;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		T result = getLocalAnnotation(annotationClass);
		if (result != null) {
			return result;
		}
		return findInheritedAnnotation(annotationClass);
	}

	private <T extends Annotation> T findInheritedAnnotation(Class<T> annotationClass) {
		for (PropertyDescriptor superProperty : getSuperProperties()) {
			if (superProperty != null) {
				T inheritedAnnotation = superProperty.getAnnotation(annotationClass);
				if (inheritedAnnotation != null) {
					return inheritedAnnotation;
				}
			}
		}

		return null;
	}

}

