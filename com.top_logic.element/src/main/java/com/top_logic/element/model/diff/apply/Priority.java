/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.apply;

import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Priority of {@link DiffElement}s defining the order in which elements of a model patch are
 * applied.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
enum Priority {

	/**
	 * Removal of an annotation.
	 * 
	 * @see #CHANGE_ANNOTATIONS
	 */
	REMOVE_ANNOTATION,

	/**
	 * Removal of a {@link TLClass#getGeneralizations() generalization}.
	 * 
	 * @implNote Must occur before removing types, because removing types removes specializations.
	 * 
	 * @see #CREATE_OR_MOVE_GENERALISATION
	 */
	REMOVE_GENERALISATION,

	/**
	 * Renames a part.
	 * 
	 * <p>
	 * A rename might be used to allow re-creation of a part with the same name. Therefore, a rename
	 * must happen before a create.
	 * </p>
	 */
	RENAME,

	/**
	 * Deletion of a {@link BoundedRole}.
	 * 
	 * @implNote Must occur before removing {@link TLModule}s.
	 * 
	 * @see #CREATE_ROLE
	 */
	DELETE_ROLE,

	/**
	 * Deletion of backwards references.
	 * 
	 * <p>
	 * Note: This must happen before deleting of forwards references to include the deletion in a
	 * generated patch. The deletion of the forwards reference automatically deletes existing
	 * backwards references. Therefore, deleting a backwards reference after a forwards references
	 * is not possible.
	 * </p>
	 * 
	 * <p>
	 * Note: Deleting inverse references must happen before creating references, because changing
	 * the type on a reference causes the inverse reference to be moved to the new target type. This
	 * is only possible, if the old reverse reference has been deleted before.
	 * </p>
	 */
	DELETE_BACKWARDS_REF,

	/**
	 * Deletion of forwards references.
	 */
	DELETE_REF,

	/**
	 * Deletion of {@link TLTypePart}.
	 * 
	 * @see #CREATE_TYPE_PART
	 * @see #CREATE_TYPE_PART_OVERRIDE
	 */
	DELETE_TYPE_PART,

	/**
	 * Creation of a {@link TLModule}.
	 * 
	 * @see #DELETE_MODULE
	 */
	CREATE_MODULE,

	/**
	 * Creation of a {@link TLType}.
	 * 
	 * @see #DELETE_TYPE
	 */
	CREATE_TYPE,

	/**
	 * Creation of a {@link TLTypePart} which does not override another part.
	 * 
	 * @see #CREATE_TYPE_PART_OVERRIDE
	 * @see #DELETE_TYPE_PART
	 */
	CREATE_TYPE_PART,

	/**
	 * Creation of a {@link TLTypePart} which overrides another part.
	 * 
	 * @see #CREATE_TYPE_PART
	 * @see #DELETE_TYPE_PART
	 */
	CREATE_TYPE_PART_OVERRIDE,

	/**
	 * Creation or moving of an {@link TLClass#getGeneralizations()}.
	 * 
	 * @see #REMOVE_GENERALISATION
	 */
	CREATE_OR_MOVE_GENERALISATION,

	/**
	 * Change of {@link TLClass#isAbstract()}
	 */
	CHANGE_TYPE_ABSTRACT,

	/**
	 * Move of a {@link TLTypePart} within its {@link TLTypePart#getOwner() owner}.
	 */
	MOVE_TYPE_PART,

	/**
	 * Update of the storage mapping of a {@link TLStructuredTypePart}.
	 */
	UPDATE_STORAGE_MAPPING,

	/**
	 * Update of an {@link TLTypePart}.
	 */
	UPDATE_TYPE_PART_TYPE,

	/**
	 * Change of {@link TLStructuredTypePart#isMandatory()}.
	 */
	CHANGE_TYPE_PART_MANDATORY,

	/**
	 * Change of {@link TLStructuredTypePart#isMultiple()}.
	 */
	CHANGE_TYPE_PART_MULTIPLE,

	/**
	 * Change of {@link TLStructuredTypePart#isOrdered()}.
	 */
	CHANGE_TYPE_PART_ORDERED,

	/**
	 * Change of {@link TLStructuredTypePart#isBag()}.
	 */
	CHANGE_TYPE_PART_BAG,

	/**
	 * Change of annotations. Either complete override or add of a new {@link TLAnnotation}.
	 * 
	 * @see #REMOVE_ANNOTATION
	 */
	CHANGE_ANNOTATIONS,

	/**
	 * Deletion of a {@link TLType}
	 * 
	 * @see #CREATE_TYPE
	 */
	DELETE_TYPE,

	/**
	 * Deletion of a {@link TLModule}.
	 * 
	 * @see #CREATE_MODULE
	 */
	DELETE_MODULE,

	/**
	 * Creation of a {@link TLModuleSingleton}.
	 */
	CREATE_SINGLETONS,

	/**
	 * Creation of a {@link BoundedRole}.
	 * 
	 * @see #DELETE_ROLE
	 */
	CREATE_ROLE

}
