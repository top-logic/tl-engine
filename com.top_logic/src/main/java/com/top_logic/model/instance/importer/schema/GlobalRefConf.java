/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.schema;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;

/**
 * {@link RefConf Reference} to a pre-existing instance in the target application of an import.
 * 
 * @see AttributeValueConf#getReferences()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("global-ref")
public interface GlobalRefConf extends RefConf {

	/**
	 * The {@link InstanceResolver} kind the {@link #getId()} is made for.
	 * 
	 * @see XMLInstanceImporter#addResolver(String, InstanceResolver)
	 */
	@Mandatory
	String getKind();

	/**
	 * @see #getKind()
	 */
	void setKind(String value);


	/**
	 * Identifier of the object being referenced.
	 * 
	 * <p>
	 * The syntax of the identifier may vary from {@link #getKind() type} to {@link #getKind()
	 * type}.
	 * </p>
	 * 
	 * @see InstanceResolver#resolve(String, String)
	 */
	@Mandatory
	String getId();

	/**
	 * @see #getId()
	 */
	void setId(String value);

}
