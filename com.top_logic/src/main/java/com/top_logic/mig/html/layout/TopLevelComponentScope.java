/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.ServletException;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.basic.Logger;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.layout.CompositeContentHandler;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ErrorPage;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.WindowContext;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.WindowScopeProvider;
import com.top_logic.util.TLContextManager;

/**
 * The class {@link TopLevelComponentScope} is a {@link LayoutComponentScope}
 * which does not have an enclosing scope
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TopLevelComponentScope extends LayoutComponentScope {

	/**
	 * Creates a {@link FrameScope} which is a top level frame scope, i.e.
	 * {@link FrameScope#getEnclosingScope() enclosing scope} is
	 * <code>null</code>.
	 * 
	 * @param windowProvider
	 *        returns the {@link WindowScope} which will be returned by
	 *        {@link TopLevelComponentScope#getWindowScope()}
	 * @param businessComponent
	 *        the component which shall be represented
	 * @param updates
	 *        a storage for updates added via
	 *        {@link FrameScope#addClientAction(ClientAction)}
	 */
	public static TopLevelComponentScope createTopLevelComponentScope(WindowScopeProvider windowProvider, LayoutComponent businessComponent,
			UpdateQueue updates) {
		return new TopLevelComponentScope(windowProvider, businessComponent, updates);
	}

	private final WindowScopeProvider windowProvider;
	private final UpdateQueue updates;

	private ScheduledFuture<?> _disposalTimeout;

	private TopLevelComponentScope(WindowScopeProvider windowProvider, LayoutComponent businessComponent, UpdateQueue updates) {
		super(businessComponent);
		this.windowProvider = windowProvider;
		this.updates = updates;
	}

	@Override
	public WindowScope getWindowScope() {
		return windowProvider.getWindowScope();
	}

	@Override
	public void addClientAction(ClientAction update) {
		updates.add(update);
	}

	/**
	 * always <code>null</code>
	 * 
	 * @see LayoutComponentScope#getEnclosingScope()
	 */
	@Override
	public FrameScope getEnclosingScope() {
		// top level scope don't have parentScopes
		return null;
	}

	@Override
	public <T extends Appendable> T appendClientReference(T out) throws IOException {
		// since there is no parent nothing is to append
		return out;
	}

	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException, ServletException {
		synchronized (this) {
			// If an onunload notification has been received recently, the session
			// timeout must be restored.
			if (_disposalTimeout != null) {
				boolean success = _disposalTimeout.cancel(false);

				if (success) {
					Logger.info("Reload detected, subsession disposal canceled.", TopLevelComponentScope.class);
					_disposalTimeout = null;
				} else {
					Logger.info("Reload after subsession disposal has completed.", TopLevelComponentScope.class);
					ErrorPage.showPage(context, "srcNotFoundErrorPage");
					return;
				}
			}

		}
		super.handleContent(context, id, url);
	}

	synchronized void markReloadPending() {
		if (_disposalTimeout == null) {
			Logger.info("Potential window close, scheduling subsession disposal.", TopLevelComponentScope.class);

			final CompositeContentHandler urlContext = getUrlContext();
			final TLSessionContext session = TLContextManager.getSession();
			final String windowName = ((WindowContext) urlContext).getWindowId().getWindowName();

			Runnable disposalTimer = new Runnable() {
				@Override
				public void run() {
					Logger.info("Disposing subsession after window close.", TopLevelComponentScope.class);
					urlContext.deregisterContentHandler(TopLevelComponentScope.this);
					session.removeSubSession(windowName);
				}
			};

			_disposalTimeout =
				SchedulerService.getInstance().schedule(disposalTimer, 10, TimeUnit.SECONDS);
		}
	}

	@Override
	public void setUrlContext(CompositeContentHandler newUrlContext, String pathName) {
		// Need window context to get window id in disposal timer.
		assert newUrlContext instanceof WindowContext;
		super.setUrlContext(newUrlContext, pathName);
	}

}
