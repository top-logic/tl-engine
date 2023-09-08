/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.processor;

import junit.framework.TestCase;

import test.com.top_logic.basic.AssertProtocol;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.Filter;
import com.top_logic.layout.processor.LayoutResolver;

/**
 * Base class for test of layout model processor parts.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class LayoutModelTest extends TestCase {

	protected static final Filter<? super String> ONLY_NO_NAMESPACE = new Filter<Object>() {
		@Override
		public boolean accept(Object anObject) {
			return anObject == null;
		}
	};

	private LayoutResolver _resolver;

	public LayoutModelTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_resolver = LayoutResolver.newRuntimeResolver(new AssertProtocol());
	}

	@Override
	protected void tearDown() throws Exception {
		_resolver = null;

		super.tearDown();
	}

	public LayoutResolver getResolver() {
		return _resolver;
	}

	public Protocol getProtocol() {
		return getResolver().getProtocol();
	}

	public void setProtocol(Protocol protocol) {
		_resolver.setProtocol(protocol);
	}

}
