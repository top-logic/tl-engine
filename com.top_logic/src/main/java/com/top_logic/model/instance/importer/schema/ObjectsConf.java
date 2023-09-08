/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.schema;

import java.util.List;

import com.top_logic.basic.config.annotation.DefaultContainer;

/**
 * Collections of {@link ObjectConf objects to import}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ObjectsConf {

	/**
	 * Description of objects to import.
	 */
	@DefaultContainer
	List<ObjectConf> getObjects();

}
