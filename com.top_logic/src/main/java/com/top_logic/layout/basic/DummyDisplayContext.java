/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Objects;

import jakarta.servlet.ServletContext;

import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.component.ControlSupport;
import com.top_logic.mig.html.Media;
import com.top_logic.mig.html.UserAgent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.util.TLContext;

/**
 * A stub {@link com.top_logic.layout.DisplayContext} that is not based on a request.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DummyDisplayContext extends AbstractDisplayContext {
    
	private static final UserAgent FEATURE_DETECTION_USER_AGENT = new UserAgent(UserAgent.FEATURE_DETECTION_BROWSER);

	private String _contextPath = "";

	private Media _outputMedia;

	/**
	 * Creates a {@link DummyDisplayContext}.
	 * 
	 * @see #forScope(ControlScope)
	 */
	public DummyDisplayContext(ServletContext servletContext) {
		super(servletContext, null, null);
	}

	/**
	 * Creates a new {@link DummyDisplayContext}.
	 *
	 */
	public DummyDisplayContext() {
		super(null, null, null);
	}

	@Override
	public Object getAttribute(String name) {
		return null;
	}

	@Override
	public String getContextPath() {
		return _contextPath;
	}

	/**
	 * Sets the {@link #getContextPath()}.
	 */
	public DummyDisplayContext initContextPath(String contextPath) {
		_contextPath = contextPath;
		return this;
	}
	
	@Override
	public String getCharacterEncoding() {
		return LayoutConstants.UTF_8;
	}

	@Override
	public UserAgent getUserAgent() {
		return FEATURE_DETECTION_USER_AGENT;
	}
	
	@Override
	public Media getOutputMedia() {
		return _outputMedia;
	}

	/**
	 * Setter for {@link #getOutputMedia()}.
	 */
	public DummyDisplayContext initOutputMedia(Media outputMedia) {
		_outputMedia = Objects.requireNonNull(outputMedia);
		return this;
	}

	@Override
	public long getInteractionRevision(HistoryManager historyManager) {
		return historyManager.getLastRevision();
	}

	@Override
	public long updateInteractionRevision(HistoryManager historyManager, long newSessionRevision) {
		// Interaction revision is always "current".
		return 0;
	}

	/**
	 * Factory method for creating a new {@link DisplayContext} for the given
	 * scope.
	 */
	public static AbstractDisplayContext forScope(ControlScope scope) {
		DummyDisplayContext dummyDisplayContext = new DummyDisplayContext();
		dummyDisplayContext.initScope(scope);
		dummyDisplayContext.installSubSessionContext(TLContext.getContext());
		return dummyDisplayContext;
	}

	/**
	 * Factory method for {@link DefaultDisplayContext}.
	 */
	public static AbstractDisplayContext newInstance() {
		return forScope(new ControlSupport(null));
	}
}