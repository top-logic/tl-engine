/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.List;

import com.top_logic.basic.db.schema.setup.config.TypeProvider;

/**
 * Provider of {@link TypeProvider}s for tests.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface KnowledgeBaseTestScenario {

	/**
	 * The types to use for the test.
	 */
	List<TypeProvider> getTestTypes();

}
