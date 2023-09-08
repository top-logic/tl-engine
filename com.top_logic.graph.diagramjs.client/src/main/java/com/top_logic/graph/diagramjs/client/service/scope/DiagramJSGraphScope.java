/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service.scope;

import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.common.remote.shared.HandleFactory;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.common.remote.shared.SharedObject;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.diagramjs.model.DiagramJSGraphModel;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSGraphModel;

/**
 * Binding of a DiagramJS graph object to a {@link GraphModel} implemented by
 * {@link DefaultSharedObject}s for synchronization over a network.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DiagramJSGraphScope extends ObjectScope {

	private DefaultDiagramJSGraphModel _graphModel;

	/**
	 * Creates a {@link DiagramJSGraphScope} with the given factory to create {@link SharedObject}s.
	 */
	public DiagramJSGraphScope(HandleFactory factory) {
		super(factory);

		_graphModel = new DefaultDiagramJSGraphModel(this);

		// Ignore the changes caused by the initialization.
		popChanges();
	}

	/**
	 * the graphModel
	 */
	public DiagramJSGraphModel getGraphModel() {
		return _graphModel;
	}

}
