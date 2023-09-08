/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AbstractStorageBase;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.impl.generated.TLModelPartBase;
import com.top_logic.model.internal.PersistentModelPart.AnnotationConfigs;
import com.top_logic.util.error.TopLogicException;

/**
 * Special {@link CollectionStorage} accessing annotations property of a {@link TLModelPart}.
 * 
 * <p>
 * The storage accesses a database value of type {@link AnnotationConfigs} and transforms it to a
 * list of {@link TLAnnotation}.
 * </p>
 * 
 * @see TLModelPartBase#ANNOTATIONS_ATTR
 * @see TLModelPart#getAnnotations()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FrameworkInternal
public class AnnotationConfigsStorage extends CollectionStorage<AnnotationConfigsStorage.Config> {

	private final String _dbAttribute;

	/**
	 * Configuration options for {@link AnnotationConfigsStorage}.
	 */
	public interface Config extends AbstractStorageBase.Config<AnnotationConfigsStorage> {

		/**
		 * Name of the database attribute which holds the {@link AnnotationConfigs}.
		 */
		@Mandatory
		String getDBAttribute();
	}

	/**
	 * Creates a new {@link AnnotationConfigsStorage}.
	 */
	public AnnotationConfigsStorage(InstantiationContext context, Config config) {
		super(context, config);
		_dbAttribute = config.getDBAttribute();
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) throws AttributeException {
		AnnotationConfigs annotations = annotations(object);
		if (annotations == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableCollection(annotations.getAnnotations());
	}

	private AnnotationConfigs annotations(TLObject object) {
		return (AnnotationConfigs) object.tGetData(_dbAttribute);
	}

	private void setAnnotations(TLObject object, AnnotationConfigs newConfig) {
		object.tSetData(_dbAttribute, newConfig);
	}

	private void putAnnotation(AnnotationConfigs configs, TLAnnotation annotation) {
		TLAnnotation oldAnnotation = configs.getAnnotation(annotation.getConfigurationInterface());
		if (oldAnnotation != null) {
			configs.getAnnotations().remove(oldAnnotation);
		}
		configs.getAnnotations().add(annotation);
	}

	@Override
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		this.checkAddValue(object, attribute, aValue);

		TLAnnotation annotation = (TLAnnotation) aValue;
		AnnotationConfigs annotations = annotations(object);
		AnnotationConfigs newConfig;
		if (annotations == null) {
			newConfig = TypedConfiguration.newConfigItem(AnnotationConfigs.class);
		} else {
			newConfig = TypedConfiguration.copy(annotations);
		}
		putAnnotation(newConfig, TypedConfiguration.copy(annotation));
		setAnnotations(object, newConfig);
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, AttributeException {
		this.checkRemoveValue(object, attribute, aValue);

		AnnotationConfigs annotations = annotations(object);
		if (annotations == null) {
			return;
		}
		Class<? extends TLAnnotation> annotationInterface = ((TLAnnotation) aValue).getClass();
		if (annotations.getAnnotation(annotationInterface) == null) {
			return;
		}
		AnnotationConfigs newConfig = TypedConfiguration.copy(annotations);
		newConfig.getAnnotations().remove(newConfig.getAnnotation(annotationInterface));
		setAnnotations(object, newConfig);
	}

	@Override
	protected void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValues)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		if (aValues == null) {
			aValues = Collections.emptyList();
		} else if (!(aValues instanceof Collection)) {
			throw new IllegalArgumentException("Given value is no Collection");
		}
		checkSetValue(object, attribute, aValues);

		AnnotationConfigs newConfig = TypedConfiguration.newConfigItem(AnnotationConfigs.class);
		for (Object elem : (Collection<?>) aValues) {
			putAnnotation(newConfig, TypedConfiguration.copy((TLAnnotation) elem));
		}
		setAnnotations(object, newConfig);
	}

	@Override
	protected void checkSetValues(TLObject object, TLStructuredTypePart attribute, Collection collectionSetUpdate)
			throws TopLogicException {
		for (Object elem : collectionSetUpdate) {
			checkAnnotation(elem);
		}
	}

	@Override
	protected void checkAddValue(TLObject object, TLStructuredTypePart attribute, Object collectionAddUpdate)
			throws TopLogicException {
		checkAnnotation(collectionAddUpdate);
	}

	@Override
	protected void checkRemoveValue(TLObject object, TLStructuredTypePart attribute, Object collectionRemoveUpdate)
			throws TopLogicException {
		checkAnnotation(collectionRemoveUpdate);
	}

	private void checkAnnotation(Object value) {
		if (!(value instanceof TLAnnotation)) {
			throw new TopLogicException(I18NConstants.ERROR_VALUE_IS_NOT_TLANNOTATION__VALUE.fill(value));
		}
	}

}

