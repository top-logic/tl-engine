/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLClass;

/**
 * A generalization is moved within the list of {@link TLClass#getGeneralizations() generalizations}
 * of the referenced {@link #getType()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("move-generalization")
public interface MoveGeneralization extends OrderedGeneralizationUpdate {

	// Pure marker interface.

}
