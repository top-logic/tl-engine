/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.InAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link TLAttributeAnnotation} to annotate {@link TLChangeHandler change handlers} to an attribute.
 *
 * <p>
 * The handlers are called within the commit of a transaction that changes the value of the
 * annotated attribute of an existing object, see {@link TLChangeHandler}.
 * </p>
 *
 * <p>
 * The handlers of an attribute include the handlers annotated to the attributes it overrides,
 * unless {@link #isIgnoreInherited()} is set.
 * </p>
 *
 * <p>
 * Only an attribute whose storage {@link StorageDetail#canDetectValueChanges() detects value
 * changes} can have change handlers, e.g. an attribute stored in a column of the object's table,
 * a reference stored in a link table or in the rows of the referenced objects, or the reverse of
 * such a reference. When a model is loaded, the annotation is rejected for a derived, transient,
 * or computed attribute (see {@link TLAnnotationTargetCheck}).
 * </p>
 */
@InApp
@TagName(TLChangeHandlers.TAG_NAME)
// Handlers are collected explicitly by code over the whole inheritance hierarchy.
@AnnotationInheritance(Policy.REDEFINE)
@DisplayOrder({
	TLChangeHandlers.HANDLERS_ATTRIBUTE,
	TLChangeHandlers.IGNORE_INHERITED_ATTRIBUTE,
})
public interface TLChangeHandlers extends TLAttributeAnnotation, TLAnnotationTargetCheck {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/**
	 * Tag name of the {@link TLChangeHandlers} annotation.
	 */
	String TAG_NAME = "change-handlers";

	/**
	 * Configuration name for the property {@link #getHandlers()}.
	 */
	String HANDLERS_ATTRIBUTE = "handlers";

	/**
	 * Tag name of an entry of {@link #getHandlers()}.
	 */
	String HANDLER_TAG = "handler";

	/**
	 * Configuration name for the property {@link #isIgnoreInherited()}.
	 */
	String IGNORE_INHERITED_ATTRIBUTE = "ignore-inherited";

	/**
	 * Handlers to call within the commit of a change of the annotated attribute.
	 */
	@DefaultContainer
	@EntryTag(HANDLER_TAG)
	@Options(fun = InAppImplementations.class)
	@Name(HANDLERS_ATTRIBUTE)
	List<PolymorphicConfiguration<? extends TLChangeHandler>> getHandlers();

	/**
	 * Whether the handlers of overridden attributes should be ignored.
	 */
	@Name(IGNORE_INHERITED_ATTRIBUTE)
	boolean isIgnoreInherited();

	/**
	 * Rejects an attribute whose storage does not
	 * {@link StorageDetail#canDetectValueChanges() detect value changes}.
	 *
	 * <p>
	 * An abstract attribute is accepted, its handlers are inherited by the attributes overriding
	 * it.
	 * </p>
	 */
	@Override
	default String checkTarget(TLTypePart target) {
		if (!(target instanceof TLStructuredTypePart part)) {
			return "Change handlers can only be annotated to attributes of classes.";
		}
		if (part.isAbstract()) {
			return null;
		}
		StorageDetail storage = part.getStorageImplementation();
		if (storage != null && storage.canDetectValueChanges()) {
			return null;
		}
		return "Changes of the values of '" + TLModelUtil.qualifiedName(part)
			+ "' cannot be detected, since its values are not stored in the database.";
	}

}
