/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.model;

import com.top_logic.common.remote.shared.SharedObject;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.impl.SharedGraph;

/**
 * A diagramJS specific {@link GraphModel} that is a {@link SharedObject} too.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface DiagramJSGraphModel extends SharedGraph {

	// Marker interface

}
