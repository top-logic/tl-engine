/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.schema;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link RefConf Reference} to the context model on which the import is based on.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("model-ref")
public interface ModelRefConf extends RefConf {

	/**
	 * Name of the model part to reference.
	 * 
	 * <p>
	 * The format of the model part name is as follows:
	 * </p>
	 * 
	 * <dl>
	 * <dt>Module</dt>
	 * <dd><code>[module-name]</code></dd>
	 * 
	 * <dt>Type</dt>
	 * <dd><code>[module-name]:[type-name]</code></dd>
	 * 
	 * <dt>Type part (property, reference, or enumeration literal)</dt>
	 * <dd><code>[module-name]:[type-name]#[part-name]</code></dd>
	 * 
	 * <dt>Singleton object</dt>
	 * <dd><code>[module-name]#[singleton-name]</code></dd>
	 * </dl>
	 */
	@Mandatory
	String getName();

}
