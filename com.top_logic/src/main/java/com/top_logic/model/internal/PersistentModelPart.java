/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.internal;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.MOAttribute;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.util.model.ModelService;

/**
 * Base class for persistent versions of {@link TLModelPart} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class PersistentModelPart extends AbstractBoundWrapper implements TLModelPart {

	/**
	 * Generic holder for all annotations, added to a {@link PersistentModelPart}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface AnnotationConfigs extends AnnotatedConfig<TLAnnotation> {
		// Pure type-paramter-binding interface.
	}

	/**
	 * Name of the {@link MOAttribute} that stores the annotations for this model part.
	 * 
	 * see ModelMeta.xml
	 */
	public static final String ANNOTATIONS_MO_ATTRIBUTE = "annotations";

	/**
	 * Creates a {@link PersistentModelPart}.
	 */
	@CalledByReflection
	public PersistentModelPart(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public <T extends TLAnnotation> T getAnnotationLocal(Class<T> annotationInterface) {
		AnnotationConfigs annotations = annotations();
		if (annotations == null) {
			return null;
		}
		return getAnnotation(annotations, annotationInterface);
	}

	@Override
	public void setAnnotation(TLAnnotation annotation) {
		AnnotationConfigs annotations = annotations();
		AnnotationConfigs newConfig;
		if (annotations == null) {
			newConfig = TypedConfiguration.newConfigItem(AnnotationConfigs.class);
		} else {
			newConfig = TypedConfiguration.copy(annotations);
		}
		putAnnotation(newConfig, TypedConfiguration.copy(annotation));
		setAnnotations(newConfig);
	}

	@Override
	public void removeAnnotation(Class<? extends TLAnnotation> annotationInterface) {
		AnnotationConfigs annotations = annotations();
		if (annotations == null) {
			return;
		}
		if (annotations.getAnnotation(annotationInterface) == null) {
			return;
		}
		AnnotationConfigs newConfig = TypedConfiguration.copy(annotations);
		newConfig.getAnnotations().remove(newConfig.getAnnotation(annotationInterface));
		setAnnotations(newConfig);
	}

	/**
	 * Installs the given {@link AnnotationConfigs} as new value for {@link TLAnnotation} delivered
	 * by {@link #getAnnotation(Class)}.
	 */
	public final void setAnnotations(AnnotationConfigs annotationConfigs) {
		tSetData(ANNOTATIONS_MO_ATTRIBUTE, annotationConfigs);
	}

	public final AnnotationConfigs annotations() {
		return (AnnotationConfigs) tGetData(ANNOTATIONS_MO_ATTRIBUTE);
	}
	
	/**
	 * Cast is safe due to {@link #putAnnotation(AnnotationConfigs, TLAnnotation)}.
	 * 
	 * @see #putAnnotation(AnnotationConfigs, TLAnnotation)
	 */
	private <T extends TLAnnotation> T getAnnotation(AnnotationConfigs annotations, Class<T> annotationInterface) {
		return annotations.getAnnotation(annotationInterface);
	}

	/**
	 * Cast is safe due to {@link #getAnnotation(AnnotationConfigs, Class)}.
	 * 
	 * @see #getAnnotation(AnnotationConfigs, Class)
	 */
	private void putAnnotation(AnnotationConfigs configs, TLAnnotation annotation) {
		TLAnnotation oldAnnotation = configs.getAnnotation(annotation.getConfigurationInterface());
		if (oldAnnotation != null) {
			configs.getAnnotations().remove(oldAnnotation);
		}
		configs.getAnnotations().add(annotation);
	}

	@Override
	public Collection<? extends TLAnnotation> getAnnotations() {
		AnnotationConfigs annotations = annotations();
		if (annotations == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableCollection(annotations.getAnnotations());
	}

	@Override
	protected final String toStringSafe() throws Throwable {
		return TLModelUtil.qualifiedName(this);
	}

	@Override
	protected final String toStringValues() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TLStructuredType tType() {
		TLModel applicationModel = ModelService.getApplicationModel();
		if (applicationModel == null) {
			// Bootstrap...
			return null;
		}
		TLModule module = applicationModel.getModule(TlModelFactory.TL_MODEL_STRUCTURE);
		if (module == null) {
			/* May occur when the application does not include the "tl.model" structure. This should
			 * actually not happen in almost all cases but ist possible for tests in
			 * com.top_logic. */
			return null;
		}
		return visit(TTypeVisitor.INSTANCE, module);
	}

}
