/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service.scope.listener;

import com.top_logic.client.diagramjs.core.Diagram;
import com.top_logic.client.diagramjs.model.Base;
import com.top_logic.client.diagramjs.model.Label;
import com.top_logic.client.diagramjs.model.Shape;
import com.top_logic.client.diagramjs.util.DiagramJSObjectUtil;

/**
 * Handles updates for display graph parts.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DisplayGraphPartUpdater {

	private Diagram _diagram;

	/**
	 * Creates a {@link DisplayGraphPartUpdater}.
	 */
	public DisplayGraphPartUpdater(Diagram diagram) {
		_diagram = diagram;
	}

	/**
	 * Updates the given display graph part.
	 */
	public void update(Base displayPart) {
		if (DiagramJSObjectUtil.isLabel(displayPart)) {
			Label label = (Label) displayPart;
			Shape owner = (Shape) label.getOwner();

			_diagram.getCanvas().addShape(label, owner);
			_diagram.getModeler().resizeShape(owner, owner.getBounds());
		}
	}

}
