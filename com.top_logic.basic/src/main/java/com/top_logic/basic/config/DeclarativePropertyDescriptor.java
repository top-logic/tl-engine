/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.annotation.Mandatory;

/**
 * {@link PropertyDescriptorImpl} which is created directly.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FrameworkInternal
public class DeclarativePropertyDescriptor extends PropertyDescriptorImpl {

	private AnnotatedElement _annotations = SimpleAnnotatedElement.NO_ANNOTATIONS;

	private Class<?> _declaredType = null;

	private Class<?> _type = null;

	private Class<?> _declaredInstanceType = null;

	/**
	 * Creates a new {@link DeclarativePropertyDescriptor}.
	 */
	@FrameworkInternal
	public DeclarativePropertyDescriptor(Protocol protocol, AbstractConfigurationDescriptor descriptor,
			String propertyName, PropertyDescriptorImpl[] superProperties) {
		super(protocol, descriptor, propertyName, superProperties);
		// No special configuration name.
		setConfigAttributeName(protocol, propertyName);
	}

	/**
	 * Sets the {@link Annotation}s for this {@link DeclarativePropertyDescriptor}.
	 * 
	 * @param annotations
	 *        The new {@link AnnotatedElement} to get {@link Annotation} from.
	 */
	public void setAnnotations(AnnotatedElement annotations) {
		_annotations = annotations;
	}

	/**
	 * The declared return type of this {@link PropertyDescriptor}.
	 */
	public void setType(Class<?> type) {
		_declaredType = type;
	}

	/**
	 * The declared return type of this {@link PropertyDescriptor}.
	 */
	public void setDeclaredInstanceType(Class<?> instanceType) {
		_declaredInstanceType = instanceType;
	}

	@Override
	public Annotation[] getLocalAnnotations() {
		return _annotations.getDeclaredAnnotations();
	}

	@Override
	public <T extends Annotation> T getLocalAnnotation(Class<T> annotationClass) {
		return _annotations.getDeclaredAnnotation(annotationClass);
	}


	@Override
	public Class<?> getType() {
		return _type;
	}

	@Override
	public Class<?> getKeyType() {
		if (getKeyProperty() == null) {
			return null;
		}
		return getKeyProperty().getType();
	}

	@Override
	void resolveFromDeclaredReturnType(Protocol protocol) {
		if (_declaredType == null) {
			// Inherited.
			return;
		}
		if (_declaredType == Void.class) {
			error(protocol, "Declared void type.");
			return;
		}
		ensureType(protocol, _declaredType);

		// Use this for the generic case.
		if (isArrayType(_declaredType)) {
			Class<?> componentType = _declaredType.getComponentType();
			initInstanceType(componentType);
		} else {
			if (!isMultiRefType(_declaredType) && !isIndexedRefType(_declaredType)) {
				initInstanceType(_declaredType);
			}
		}

		if ((PolymorphicConfiguration.class.isAssignableFrom(_declaredType)
			|| PolymorphicConfiguration.class.isAssignableFrom(getElementType())) && _declaredInstanceType != null) {
			setInstanceType(_declaredInstanceType);
		}
	}

	private void ensureType(Protocol protocol, Class<?> declaredType) {
		if (_type == null) {
			_type = declaredType;
		} else {
			Class<?> propertyMinType = declaredType;
			if (_type.isAssignableFrom(propertyMinType)) {
				// The inherited property requires a stronger type guarantee.
				_type = propertyMinType;
			} else if (propertyMinType.isAssignableFrom(declaredType)) {
				// Current guarantee fits property requirements.
			} else {
				// Incompatible types.
				error(protocol, "Mismatch of inherited declared types: '" + propertyMinType.getName()
					+ "' is not compatible with '" + declaredType.getName() + "'.");
			}
		}
	}

	@Override
	boolean hasSetter() {
		return false;
	}

	@Override
	void resolveSetter(Protocol protocol) {
		throw new UnsupportedOperationException("Descriptor has no setter");
	}

	@Override
	protected void acceptAnnotationMandatory() {
		if (hasLocalAnnotation(Mandatory.class)) {
			setHasLocalMandatoryAnnotation(true);
		}
	}

	@Override
	protected void appendName(StringBuilder result) {
		result.append(getPropertyName());
	}

	@Override
	public Type getGenericType() {
		throw new UnsupportedOperationException("No generic type");
	}

}
