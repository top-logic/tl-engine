/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

import java.util.HashMap;
import java.util.Map;

/**
 * Mutable {@link Match} implementation that can be bound in {@link Node#match(Match, Node)}
 * operations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultMatch implements Match {

	private int _nextId = 0;

	private Map<NewIdReplacement, String> _ids = new HashMap<>();
	private Map<Capture, Node> _data = new HashMap<>();

	@Override
	public void bind(Capture capture, Node node) {
		_data.put(capture, node);
	}

	@Override
	public Node getBinding(Capture capture) {
		return _data.get(capture);
	}

	@Override
	public void clear() {
		_ids.clear();
		_data.clear();
	}

	@Override
	public String getValue(NewIdReplacement expr) {
		String id = _ids.get(expr);
		if (id == null) {
			id = expr.getName() + (_nextId++);
			_ids.put(expr, id);
		}
		return id;
	}

}
