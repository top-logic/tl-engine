/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.io.IOException;

import junit.framework.TestCase;

import test.com.top_logic.layout.basic.SimpleControlScope;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.Validator;

/**
 * The class {@link TestDisplayContext} is an abstract superclass for testing {@link DisplayContext}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TestDisplayContext extends TestCase {

	protected void testRenderScoped() {
		DisplayContext context = createNewDisplayContext();
		final ControlScope scope = new SimpleControlScope();

		ControlScope theCurrentScope = context.getExecutionScope();
		try {
			Renderer<Object> renderer = new Renderer<>() {
				@Override
				public void write(DisplayContext someContext, TagWriter out, Object value) throws IOException {
					assertEquals(scope, someContext.getExecutionScope());
				}
			};
			context.renderScoped(scope, renderer, null, null);
		} catch (IOException ex) {
			fail("should not occur");
		}
		assertEquals(theCurrentScope, context.getExecutionScope());

		theCurrentScope = context.getExecutionScope();
		boolean exceptionOccurs = false;
		try {
			Renderer<Object> renderer = new Renderer<>() {
				@Override
				public void write(DisplayContext someContext, TagWriter out, Object value) throws IOException {
					throw new IOException();
				}
			};
			context.renderScoped(scope, renderer, null, null);
		} catch (IOException ex) {
			// expected
			exceptionOccurs = true;
		}
		assertTrue(exceptionOccurs);
		assertEquals(theCurrentScope, context.getExecutionScope());
	}

	protected void testValidateScoped() {
		DisplayContext context = createNewDisplayContext();
		final ControlScope scope = new SimpleControlScope();

		ControlScope theCurrentScope = context.getExecutionScope();
		Validator<Object> validator = new Validator<>() {
			@Override
			public void validate(DisplayContext someContext, UpdateQueue queue, Object obj) {
				assertEquals(scope, someContext.getExecutionScope());
			}
		};
		context.validateScoped(scope, validator, null, null);
		assertEquals(theCurrentScope, context.getExecutionScope());

		theCurrentScope = context.getExecutionScope();
		boolean exceptionOccurs = false;
		try {
			validator = new Validator<>() {
				@Override
				public void validate(DisplayContext someContext, UpdateQueue queue, Object obj) {
					throw new RuntimeException();
				}
			};
			context.validateScoped(scope, validator, null, null);
		} catch (RuntimeException ex) {
			// expected
			exceptionOccurs = true;
		}
		assertTrue(exceptionOccurs);
		assertEquals(theCurrentScope, context.getExecutionScope());

	}

	protected abstract DisplayContext createNewDisplayContext();
	
}
