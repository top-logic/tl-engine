/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormHandlerUtil;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;

/**
 * The class {@link TestFormHandlerUtil} tests the methods in {@link FormHandlerUtil}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestFormHandlerUtil extends BasicTestCase {

	/*package protected*/ static class TestedFormHandler implements FormHandler {

		private FormContext ctx;

		@Override
		public FormContext getFormContext() {
			return ctx;
		}

		public void createFormContext(String name, ResPrefix resPrefix) {
			ctx = new FormContext(name, resPrefix);
		}

		@Override
		public boolean hasFormContext() {
			return ctx != null;
		}

		@Override
		public Command getApplyClosure() {
			return null;
		}

		@Override
		public Command getDiscardClosure() {
			return null;
		}

		@Override
		public ModelName getModelName() {
			return ModelResolver.buildModelName(this);
		}
	}

	public void testSetImmutable() {
		TestedFormHandler formHandler = new TestedFormHandler();
		assertFalse(formHandler.hasFormContext());

		FormHandlerUtil.setImmutable(formHandler, true);
		assertFalse(formHandler.hasFormContext());

		formHandler.createFormContext("name", ResPrefix.forTest("resPrefix"));
		assertTrue(formHandler.hasFormContext());
		assertFalse(formHandler.getFormContext().isImmutable());

		FormHandlerUtil.setImmutable(formHandler, true);
		assertTrue(formHandler.getFormContext().isImmutable());
		FormHandlerUtil.setImmutable(formHandler, false);
		assertFalse(formHandler.getFormContext().isImmutable());

		FormHandlerUtil.setImmutable(formHandler, true);
		FormHandlerUtil.setImmutable(formHandler, true);
		assertTrue(formHandler.getFormContext().isImmutable());
		FormHandlerUtil.setImmutable(formHandler, false);
		assertTrue(formHandler.getFormContext().isImmutable());
		FormHandlerUtil.setImmutable(formHandler, false);
		assertFalse(formHandler.getFormContext().isImmutable());

		FormHandlerUtil.setImmutable(formHandler, false);
		FormHandlerUtil.setImmutable(formHandler, true);
		assertTrue(formHandler.getFormContext().isImmutable());
	}
	
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestFormHandlerUtil.class);
	}
}
