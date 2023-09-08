/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.impl;

import com.top_logic.common.remote.shared.SharedObject;
import com.top_logic.graph.common.model.GraphModel;

/**
 * A {@link GraphModel} that is a {@link SharedObject}, too.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface SharedGraph extends GraphModel, SharedObject {

	// Only the combination of these two interfaces is required, nothing else.

}
