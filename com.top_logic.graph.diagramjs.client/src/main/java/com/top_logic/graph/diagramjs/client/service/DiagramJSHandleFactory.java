/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service;

import com.top_logic.common.remote.shared.HandleFactory;
import com.top_logic.common.remote.shared.ObjectData;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSClassNode;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSEdge;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSLabel;

/**
 * {@link HandleFactory} crating diagramJS objects as generalized shared objects on the client.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DiagramJSHandleFactory implements HandleFactory {

	static final DiagramJSHandleFactory INSTANCE = new DiagramJSHandleFactory();

	private DiagramJSHandleFactory() {
		// Singleton constructor.
	}

	@Override
	public ObjectData createHandle(String typeName, ObjectScope scope) {
		if (DefaultDiagramJSEdge.class.getName().equals(typeName)) {
			return new DefaultDiagramJSEdge(scope);
		} else if (DefaultDiagramJSClassNode.class.getName().equals(typeName)) {
			return new DefaultDiagramJSClassNode(scope);
		} else if (DefaultDiagramJSLabel.class.getName().equals(typeName)) {
			return new DefaultDiagramJSLabel(scope);
		} else {
			throw new UnsupportedOperationException("No factory for: " + typeName);
		}
	}

}
