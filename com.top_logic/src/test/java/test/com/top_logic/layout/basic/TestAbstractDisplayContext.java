/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.layout.TestDisplayContext;

import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.UpdateListener;
import com.top_logic.layout.basic.AbstractDisplayContext;
import com.top_logic.mig.html.UserAgent;

/**
 * The class {@link TestAbstractDisplayContext} tests the methods in {@link AbstractDisplayContext}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestAbstractDisplayContext extends TestDisplayContext {

	@Override
	protected DisplayContext createNewDisplayContext() {
		ControlScope scope = new ControlScope() {

			@Override
			public void addUpdateListener(UpdateListener listener) {
			}

			@Override
			public FrameScope getFrameScope() {
				return null;
			}

			@Override
			public boolean removeUpdateListener(UpdateListener listener) {
				return false;
			}
			
			@Override
			public void disableScope(boolean disable) {
			}
			
			@Override
			public boolean isScopeDisabled() {
				return false;
			}
			
		};
		AbstractDisplayContext context = new AbstractDisplayContext() {

			@Override
			public HttpServletRequest asRequest() {
				return null;
			}

			@Override
			public HttpServletResponse asResponse() {
				return null;
			}

			@Override
			public ServletContext asServletContext() {
				return null;
			}

			@Override
			public Object getAttribute(String name) {
				return null;
			}

			@Override
			public String getCharacterEncoding() {
				return null;
			}

			@Override
			public String getContextPath() {
				return null;
			}

			@Override
			public UserAgent getUserAgent() {
				return null;
			}

			@Override
			public long getInteractionRevision(HistoryManager historyManager) {
				return historyManager.getLastRevision();
			}

			@Override
			public long updateInteractionRevision(HistoryManager historyManager, long newSessionRevision) {
				// Interaction revision is always "current".
				return 0L;
			}

		};
		context.initScope(scope);
		return context;
	}

	@Override
	public void testRenderScoped() {
		super.testRenderScoped();
	}

	@Override
	public void testValidateScoped() {
		super.testValidateScoped();
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(new TestSuite(TestAbstractDisplayContext.class));
	}

}
