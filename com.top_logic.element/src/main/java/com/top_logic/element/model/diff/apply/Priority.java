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
 * Priority of {@link DiffElement}s.
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
	 * Deletion of {@link TLTypePart}.
	 * 
	 * @see #CREATE_TYPE_PART
	 * @see #CREATE_TYPE_PART_OVERRIDE
	 */
	DELETE_TYPE_PART,

	/**
	 * Removal of a {@link TLClass#getGeneralizations() generalization}.
	 * 
	 * @implNote Must occur before removing types, because removing types removes specializations.
	 * 
	 * @see #CREATE_OR_MOVE_GENERALISATION
	 */
	REMOVE_GENERALISATION,

	/**
	 * Deletion of a {@link TLType}
	 * 
	 * @see #CREATE_TYPE
	 */
	DELETE_TYPE,

	/**
	 * Deletion of a {@link BoundedRole}.
	 * 
	 * @implNote Must occur before removing {@link TLModule}e.
	 * 
	 * @see #CREATE_ROLE
	 */
	DELETE_ROLE,

	/**
	 * Deletion of a {@link TLModule}.
	 * 
	 * @see #CREATE_MODULE
	 */
	DELETE_MODULE,

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
	UPDATE_TYPE_PART,

	/**
	 * Change of {@link TLStructuredTypePart#isMandatory()}.
	 */
	CHANGE_TYPE_PART_MANDATORY,

	/**
	 * Change of annotations. Either complete override or add of a new {@link TLAnnotation}.
	 * 
	 * @see #REMOVE_ANNOTATION
	 */
	CHANGE_ANNOTATIONS,

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
