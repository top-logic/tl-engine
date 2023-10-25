/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.FormMember;
import com.top_logic.mig.html.Media;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * Description of an atomic model edit operation such as editing a single attribute of some object.
 * 
 * <p>
 * A form presented to the user is composed of multiple edit contexts each of them representing a
 * single input field.
 * </p>
 * 
 * @implNote This interface is split between tl-core and tl-element, see
 *           <code>com.top_logic.element.meta.form.EditContext</code>.
 */
public interface EditContextBase extends AnnotationLookup {
	/**
	 * A label for this edit context.
	 */
	ResKey getLabelKey();

	/**
	 * Label rendered in the table title, if this edit context is displayed as table.
	 */
	default ResKey getTableTitleKey() {
		return getLabelKey();
	}

	/**
	 * Description of this edit context to be used in messages to the user describing e.g. locations
	 * of errors.
	 */
	ResKey getDescriptionKey();

	/**
	 * Type of the object being edited, created or searched.
	 * 
	 * <p>
	 * If {@link #getObject() the base object} is non-<code>null</code>, the base object's
	 * {@link TLObject#tType()} is equal to this value.
	 * </p>
	 * 
	 * @see #getValueType()
	 */
	TLStructuredType getType();

	/**
	 * The object being edited or created.
	 * 
	 * <p>
	 * For an edit operation, the value is the persistent object to which the new values are stored
	 * (never <code>null</code>). For a create operation, the value is <code>null</code> up to the
	 * time the creation is performed.
	 * </p>
	 */
	TLObject getObject();

	/**
	 * Type of the value being edited.
	 * 
	 * @see #getType()
	 */
	TLType getValueType();

	/**
	 * Get the value(s) according to the update type. Note: if the type is TYPE_SEARCH_RANGE the
	 * result will be a List with the from value as first and the to value as second element.
	 *
	 * @return the value(s) according to the update type.
	 */
	Object getCorrectValues();

	/**
	 * Whether the user must provide some input to successfully complete this edit operation.
	 */
	boolean isMandatory();

	/**
	 * Whether multiple values are expected in this context.
	 */
	boolean isMultiple();

	/**
	 * Whether values in this context have a custom order that can be specified by the user.
	 */
	boolean isOrdered();

	/**
	 * Whether the same value can occur multiple times.
	 */
	boolean isBag();

	/**
	 * Whether the value cannot be edited by the user.
	 * 
	 * @see #isDerived()
	 */
	boolean isDisabled();

	/**
	 * Whether the value is a "part" of its base object, i.e. when the base object is deleted, the
	 * value can also be removed.
	 */
	boolean isComposition();

	/**
	 * Whether the current edit location cannot be stored for technical reasons (is read-only).
	 * 
	 * @see #isDisabled()
	 */
	boolean isDerived();

	/**
	 * Whether this is an edit operation in a search mask.
	 */
	default boolean isSearchUpdate() {
		return false;
	}

	/**
	 * Media to which the form is rendered.
	 */
	default Media getOutputMedia() {
		return Media.BROWSER;
	}

	/**
	 * A personalization key for this edit location.
	 * 
	 * <p>
	 * Personal settings such as sort order and column selections are stored under this key, if this
	 * edit location is displayed as table.
	 * </p>
	 * 
	 * @param field
	 *        The field to produce store settings for.
	 */
	default String getConfigKey(FormMember field) {
		return field.getQualifiedName();
	}

	/**
	 * The requested annotation that is associated with this edit context.
	 * 
	 * @param annotationType
	 *        The type of the annotation to retrieve.
	 */
	@Override
	default <T extends TLAnnotation> T getAnnotation(Class<T> annotationType) {
		return null;
	}

}
