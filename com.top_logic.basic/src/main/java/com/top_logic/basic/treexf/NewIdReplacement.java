/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

/**
 * Special {@link Value} that expands to a new unique identifier.
 * 
 * <p>
 * Must only be used in expansion patterns.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NewIdReplacement extends AbstractValue {

	private final String _name;

	/**
	 * Creates a {@link NewIdReplacement}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 */
	public NewIdReplacement(String name) {
		_name = name;
	}

	/**
	 * The name of this generated identifier.
	 */
	public String getName() {
		return _name;
	}

	@Override
	public String getValue(Match match) {
		return match.getValue(this);
	}

	@Override
	public Node expand(Match match) {
		String id = getValue(match);

		return new LiteralValue(id);
	}

	@Override
	public void appendTo(StringBuilder buffer) {
		buffer.append('{');
		buffer.append(_name);
		buffer.append('}');
	}

	@Override
	protected boolean internalMatch(Match match, Node node) {
		throw new UnsupportedOperationException("Must only be used in a replacement expression.");
	}


}
