/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.layout.security.AccessChecker;
import com.top_logic.layout.security.LiberalAccessChecker;
import com.top_logic.model.ModelKind;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.DefaultStrategy;
import com.top_logic.model.annotate.DefaultStrategy.Strategy;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.CompatibilityService;

/**
 * Default implementation of {@link TLStructuredTypePart}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractStructuredTypePart<O extends TLStructuredType> extends AbstractTLTypePart<O> implements TLStructuredTypePart {

	private boolean mandatory;

	private boolean multiple;

	private boolean ordered;

	private boolean bag;
	
	private TLStructuredTypePart _definition = this;

	private StorageDetail _storage;

	private TLType _type;

	AbstractStructuredTypePart(TLModel model, String name) {
		super(model, name);
	}
	
	@Override
	public boolean isMandatory() {
		return this.mandatory;
	}
	
	@Override
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	@Override
	public boolean isMultiple() {
		return this.multiple;
	}

	@Override
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	@Override
	public boolean isOrdered() {
		return this.ordered;
	}

	@Override
	public void setOrdered(boolean ordered) {
		this.ordered = ordered;
	}

	@Override
	public boolean isBag() {
		return this.bag;
	}

	@Override
	public void setBag(boolean unique) {
		this.bag = unique;
	}

	@Override
	public AccessChecker getAccessChecker() {
		return LiberalAccessChecker.INSTANCE;
	}

	@Override
	public TLStructuredTypePart getDefinition() {
		return _definition;
	}

	@Override
	public void setDefinition(TLStructuredTypePart newDefinition) {
		_definition = newDefinition;
	}

	@Override
	public void updateDefinition() {
		TLTypePartCommon.updateDefinition(this);
	}

	@Override
	public boolean isOverride() {
		return !getDefinition().equals(this);
	}

	@Override
	public StorageDetail getStorageImplementation() {
		if (_storage == null) {
			_storage = CompatibilityService.getInstance().createStorage(this);
		}
		return _storage;
	}

	@Override
	public final boolean isDerived() {
		StorageDetail storage = getStorageImplementation();
		if (storage == null) {
			/* Happens during tests in "com.top_logic". */
			return false;
		}
		return storage.isReadOnly();
	}

	@Override
	public TLType getType() {
		return _type;
	}

	/* package protected */void internalSetType(TLType value) {
		_type = value;
	}

	/**
	 * The default {@link TLAnnotation} for the part.
	 */
	@FrameworkInternal
	public static <T extends TLAnnotation> T getDefaultsFromAttribute(Class<T> annotationInterface,
			TLStructuredTypePart part) {
		if (AnnotationInheritance.Policy.getInheritancePolicy(annotationInterface) != Policy.REDEFINE) {
			T inherited = getInheritedAnnotation(annotationInterface, part);
			if (inherited != null) {
				return inherited;
			}
		}
		AttributeSettings attributeSettings = AttributeSettings.getInstance();
		T annotation = attributeSettings.getConfiguredPartAnnotation(annotationInterface, part);
		if (annotation != null) {
			return annotation;
		}
		return getDefaultsFromType(annotationInterface, part.getType());
	}

	private static <T extends TLAnnotation> T getInheritedAnnotation(Class<T> annotationInterface,
			TLStructuredTypePart part) {
		if (part.isOverride()) {
			for (TLClass generalization : generalizations(part.getOwner())) {
				TLStructuredTypePart overridden = generalization.getPart(part.getName());
				if (overridden != null) {
					T inherited = overridden.getAnnotationLocal(annotationInterface);
					if (inherited != null) {
						return inherited;
					}

					// Only inherit from the first generalization defining the same attribute.
					return getInheritedAnnotation(annotationInterface, overridden);
				}
			}
		}

		return null;
	}

	private static List<TLClass> generalizations(TLStructuredType owner) {
		if (owner.getModelKind() == ModelKind.CLASS) {
			return ((TLClass) owner).getGeneralizations();
		}

		return Collections.emptyList();
	}

	/**
	 * The default {@link TLAnnotation} for the target type of the attribute.
	 */
	private static <T extends TLAnnotation> T getDefaultsFromType(Class<T> annotationInterface, TLType type) {
		Strategy strategy = getDefaultStrategy(annotationInterface);
		if (strategy == Strategy.NONE) {
			return null;
		}

		T localAnnotation = type.getAnnotation(annotationInterface);
		if (localAnnotation != null || strategy == Strategy.VALUE_TYPE || type.getModelKind() != ModelKind.CLASS) {
			return localAnnotation;
		}

		return getDefaultsFromPrimaryGeneralization(annotationInterface, (TLClass) type);
	}

	private static Strategy getDefaultStrategy(Class<?> annotationInterface) {
		DefaultStrategy strategyAnnotation = annotationInterface.getAnnotation(DefaultStrategy.class);
		return strategyAnnotation == null ? Strategy.VALUE_TYPE : strategyAnnotation.value();
	}

	private static <T extends TLAnnotation> T getDefaultsFromPrimaryGeneralization(Class<T> annotationInterface,
			TLClass type) {
		TLClass primaryGeneralization = TLModelUtil.getPrimaryGeneralization(type);
		if (primaryGeneralization == null) {
			return null;
		}

		T localAnnotation = primaryGeneralization.getAnnotation(annotationInterface);
		if (localAnnotation != null) {
			return localAnnotation;
		}

		return getDefaultsFromPrimaryGeneralization(annotationInterface, primaryGeneralization);
	}

}
