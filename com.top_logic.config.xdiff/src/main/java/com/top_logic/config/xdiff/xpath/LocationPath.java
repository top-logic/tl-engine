/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.xpath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An XPath location path.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LocationPath extends XPathNode implements Iterable<Step> {
	private boolean _absolute;

	private final ArrayList<Step> _steps = new ArrayList<>();

	LocationPath(boolean absolute) {
		super();
		setAbsolute(absolute);
	}

	/**
	 * Whether this is an absolute path.
	 */
	public boolean isAbsolute() {
		return _absolute;
	}

	/**
	 * Setter for {@link #isAbsolute()}.
	 */
	public void setAbsolute(boolean absolute) {
		this._absolute = absolute;
	}

	/**
	 * The {@link Step}s of this path.
	 */
	public List<Step> getSteps() {
		return _steps;
	}

	/**
	 * Adds a {@link Step} to the end of this path.
	 */
	public void addStep(Step nextStep) {
		_steps.add(nextStep);
	}

	/**
	 * Removes and returns the last step from this path.
	 */
	public Step popStep() {
		return _steps.remove(_steps.size() - 1);
	}

	/**
	 * {@link Iterator} of the {@link Step}s in this path.
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Step> iterator() {
		return _steps.iterator();
	}

	/**
	 * The number of {@link Step}s in this path.
	 */
	public int size() {
		return _steps.size();
	}

	/**
	 * Shortens this path to the given maximum size.
	 */
	public void reset(int maxSize) {
		for (int n = _steps.size() - 1; n >= maxSize; n--) {
			_steps.remove(n);
		}
	}

	@Override
	public <R, A> R visit(XPathVisitor<R, A> v, A arg) {
		return v.visitLocationPath(this, arg);
	}

}