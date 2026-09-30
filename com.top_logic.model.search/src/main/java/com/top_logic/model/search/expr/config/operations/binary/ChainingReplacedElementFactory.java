/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

/**
 * {@link ReplacedElementFactory} holding a sequence of {@link ReplacedElementFactory} and
 * returning the first possible replaced element.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChainingReplacedElementFactory implements ReplacedElementFactory {

	private List<ReplacedElementFactory> _factories = new ArrayList<>();

	/**
	 * Modifiable sequence of the factories to call.
	 */
	public List<ReplacedElementFactory> getFactories() {
		return _factories;
	}

	/**
	 * Adds another factory to the list of available factories.
	 */
	public void addFactory(ReplacedElementFactory factory) {
		_factories.add(factory);
	}

	@Override
	public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box,
			UserAgentCallback uac, int cssWidth, int cssHeight) {
		for (ReplacedElementFactory factory : _factories) {
			ReplacedElement element = factory.createReplacedElement(c, box, uac, cssWidth, cssHeight);
			if (element != null) {
				return element;
			}
		}
		return null;
	}

	@Override
	public void reset() {
		for (ReplacedElementFactory replacedElementFactory : _factories) {
			replacedElementFactory.reset();
		}
	}

	@Override
	public void remove(Element e) {
		for (ReplacedElementFactory replacedElementFactory : _factories) {
			replacedElementFactory.remove(e);
		}
	}

	@Override
	public void setFormSubmissionListener(FormSubmissionListener listener) {
		for (ReplacedElementFactory replacedElementFactory : _factories) {
			replacedElementFactory.setFormSubmissionListener(listener);
		}
	}
}