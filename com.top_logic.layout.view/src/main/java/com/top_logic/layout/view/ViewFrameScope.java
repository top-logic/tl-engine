/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.SimpleCommandListenerRegistry;
import com.top_logic.layout.URLBuilder;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.WindowScope;
import com.top_logic.model.listen.DummyModelScope;
import com.top_logic.model.listen.ModelScope;

/**
 * Standalone {@link FrameScope} for the view system.
 *
 * <p>
 * Unlike {@link com.top_logic.mig.html.layout.LayoutComponentScope}, this scope does not depend on
 * a {@link com.top_logic.mig.html.layout.LayoutComponent} tree. It provides ID generation and
 * command listener management for controls rendered by the view system.
 * </p>
 */
public class ViewFrameScope implements FrameScope {

	private final AtomicInteger _nextId = new AtomicInteger(1);

	private final SimpleCommandListenerRegistry _registry = new SimpleCommandListenerRegistry();

	private final ModelScope _modelScope = new DummyModelScope();

	@Override
	public String createNewID() {
		return "v" + _nextId.getAndIncrement();
	}

	@Override
	public void addClientAction(ClientAction update) {
		// View system delivers updates via SSE, not through client actions.
	}

	@Override
	public WindowScope getWindowScope() {
		// The view system does not use a WindowScope.
		return null;
	}

	@Override
	public FrameScope getEnclosingScope() {
		return null;
	}

	@Override
	public ModelScope getModelScope() {
		return _modelScope;
	}

	@Override
	public <T extends Appendable> T appendClientReference(T out) throws IOException {
		throw new UnsupportedOperationException("View system does not support client references.");
	}

	@Override
	public void addCommandListener(CommandListener listener) {
		_registry.addCommandListener(listener);
	}

	@Override
	public boolean removeCommandListener(CommandListener listener) {
		return _registry.removeCommandListener(listener);
	}

	@Override
	public Collection<CommandListener> getCommandListener() {
		return _registry.getCommandListener();
	}

	@Override
	public CommandListener getCommandListener(String id) {
		return _registry.getCommandListener(id);
	}

	@Override
	public void clear() {
		_registry.clear();
	}

	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException {
		throw new UnsupportedOperationException("View system does not handle content requests.");
	}

	@Override
	public void registerContentHandler(String id, ContentHandler handler) {
		// Ignore.
	}

	@Override
	public boolean deregisterContentHandler(ContentHandler handler) {
		return false;
	}

	@Override
	public Collection<? extends ContentHandler> inspectSubHandlers() {
		return Collections.emptyList();
	}

	@Override
	public URLBuilder getURL(DisplayContext context, ContentHandler handler) {
		throw new UnsupportedOperationException("View system does not generate content URLs.");
	}

	@Override
	public URLBuilder getURL(DisplayContext context) {
		throw new UnsupportedOperationException("View system does not generate content URLs.");
	}
}
