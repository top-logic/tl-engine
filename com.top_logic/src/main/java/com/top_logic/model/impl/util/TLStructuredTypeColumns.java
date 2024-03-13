/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl.util;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Constants defining the database layout of a {@link TLStructuredTypePart} stored in the database.
 */
public interface TLStructuredTypeColumns {

	/**
	 * Marker value for {@link TLReference}s.
	 * 
	 * @see ApplicationObjectUtil#IMPLEMENTATION_NAME
	 */
	String REFERENCE_IMPL = "reference";
	
	/**
	 * Marker value for {@link TLProperty} instances of a {@link TLClass}.
	 * 
	 * @see ApplicationObjectUtil#IMPLEMENTATION_NAME
	 */
	String CLASS_PROPERTY_IMPL = "property";
	
	/**
	 * Marker value for {@link TLProperty} instances of a {@link TLAssociation}.
	 * 
	 * @see ApplicationObjectUtil#IMPLEMENTATION_NAME
	 */
	String ASSOCIATION_PROPERTY_IMPL = "association-property";
	
	/**
	 * Marker value for {@link TLAssociationEnd}s.
	 * 
	 * @see ApplicationObjectUtil#IMPLEMENTATION_NAME
	 */
	String ASSOCIATION_END_IMPL = "association-end";

	/**
	 * Marker for different implementations stored in the
	 * {@link ApplicationObjectUtil#META_ELEMENT_OBJECT_TYPE} table.
	 * 
	 * @see #CLASS_TYPE
	 * @see #ASSOCIATION_TYPE
	 */
	String META_ELEMENT_IMPL = "impl";

	/**
	 * The ordering attribute of generalization links.
	 */
	String META_ELEMENT_GENERALIZATIONS__ORDER = "order";

	/**
	 * Value of the {@link #META_ELEMENT_IMPL} attribute marking an association class.
	 */
	String ASSOCIATION_TYPE = "association";

	/**
	 * Value of the {@link #META_ELEMENT_IMPL} attribute marking a regular class.
	 */
	String CLASS_TYPE = "class";

	/**
	 * Name of the source {@link TLAssociationEnd}.
	 */
	@FrameworkInternal String SELF_ASSOCIATION_END_NAME = "self";

	/**
	 * Name of an internal association implementing a {@link TLReference}.
	 * 
	 * @see #isSyntheticAssociationName(String)
	 */
	@FrameworkInternal
	static String syntheticAssociationName(String typeName, String referenceName) {
		return typeName + "$" + referenceName;
	}

	/**
	 * Whether the given name is the name of an internal association implementing a
	 * {@link TLReference}.
	 * 
	 * @see #syntheticAssociationName(String, String)
	 */
	@FrameworkInternal
	static boolean isSyntheticAssociationName(String name) {
		return name.indexOf('$') >= 0;
	}

}
