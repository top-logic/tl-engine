/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.schema;

import java.util.List;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLClass;
import com.top_logic.model.instance.importer.XMLInstanceImporter;

/**
 * Import description of a single new object to create.
 * 
 * <p>
 * The object configuration can either occur top-level or within a composition reference to be
 * exported as part of another object.
 * </p>
 * 
 * @see XMLInstanceImporter#importInstances(ObjectsConf)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("object")
public interface ObjectConf extends ValueConf {

	/**
	 * Optional import-local identifier that is used to reference this object from references of
	 * other objects that are imported.
	 * 
	 * @see InstanceRefConf#getId()
	 */
	@Nullable
	String getId();

	/**
	 * @see #getId()
	 */
	void setId(String value);

	/**
	 * A global ID for the exported object.
	 * 
	 * <p>
	 * Setting this property marks this exported object as fallback. During re-import, the object is
	 * only allocated, if there is not yet an object identified by the given global ID in the target
	 * system. If the given global ID can be resolved in the target system, this object is used
	 * instead of the exported one.
	 * </p>
	 */
	@Nullable
	String getGlobalId();

	/**
	 * @see #getGlobalId()
	 */
	void setGlobalId(String value);

	/**
	 * The type name (qualified name of the {@link TLClass}) the imported object should be an
	 * instance of.
	 */
	@Mandatory
	String getType();

	/**
	 * @see #getType()
	 */
	void setType(String value);

	/**
	 * Attributes to set on the imported object.
	 */
	@DefaultContainer
	List<AttributeValueConf> getAttributes();

}
