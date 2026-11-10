/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.InAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLTypeAnnotation} to annotate {@link TLLifecycleHandler lifecycle handlers} to a type.
 *
 * <p>
 * The handlers are informed when an object of the annotated type or one of its specializations is
 * created or deleted, see {@link TLLifecycleHandler}.
 * </p>
 *
 * <p>
 * The handlers of a type include the handlers annotated to all its generalizations, the handlers
 * of the more general types are called first (see
 * {@link TLAnnotations#getLifecycleHandlers(com.top_logic.model.TLStructuredType)}).
 * </p>
 */
@InApp
@TagName(TLLifecycleHandlers.TAG_NAME)
@TargetType({ TLTypeKind.REF })
// Handlers are collected explicitly by code over all generalizations.
@AnnotationInheritance(Policy.REDEFINE)
public interface TLLifecycleHandlers extends TLTypeAnnotation {

	/**
	 * Tag name of the {@link TLLifecycleHandlers} annotation.
	 */
	String TAG_NAME = "lifecycle-handlers";

	/**
	 * Configuration name for the property {@link #getHandlers()}.
	 */
	String HANDLERS_ATTRIBUTE = "handlers";

	/**
	 * Tag name of an entry of {@link #getHandlers()}.
	 */
	String HANDLER_TAG = "handler";

	/**
	 * Handlers to inform about the creation and deletion of objects of the annotated type.
	 */
	@DefaultContainer
	@EntryTag(HANDLER_TAG)
	@Options(fun = InAppImplementations.class)
	@Name(HANDLERS_ATTRIBUTE)
	List<PolymorphicConfiguration<? extends TLLifecycleHandler>> getHandlers();

}
