/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import java.io.IOException;
import java.util.Collection;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.layout.AbstractCompositeContentHandler;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.CommandListenerRegistry;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.SimpleCommandListenerRegistry;
import com.top_logic.layout.URLBuilder;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.WindowScope;
import com.top_logic.model.listen.ModelScope;

/**
 * The class {@link SimpleFrameScope} is a simple implementation of {@link FrameScope}
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class SimpleFrameScope extends AbstractCompositeContentHandler implements FrameScope {
	
	private WindowScope windowScope;
	private final FrameScope parentScope;
	private CommandListenerRegistry commandListenerRegistry;
	
	private SimpleFrameScope(FrameScope parentScope) {
		this.parentScope = parentScope;
		this.commandListenerRegistry = new SimpleCommandListenerRegistry();
		// use factory method
	}
	
	/**
	 * Creates a new {@link SimpleFrameScope}
	 * 
	 * @param parentScope
	 *        the {@link FrameScope#getEnclosingScope() enclosing scope} of the
	 *        returned {@link FrameScope}. may be <code>null</code> if the
	 *        returned {@link FrameScope} is a top level Scope.
	 */
	public static SimpleFrameScope createSimpleFrameScope(FrameScope parentScope) {
		SimpleFrameScope result = new SimpleFrameScope(parentScope);
		if (parentScope != null) {
			result.windowScope = parentScope.getWindowScope();
		} else {
			result.windowScope = new SimpleWindowScope(null, result);
		}
		return result;
		
	}

	@Override
	public void addClientAction(ClientAction update) {
		// Ignore;
	}

	@Override
	public WindowScope getWindowScope() {
		return windowScope;
	}

	@Override
	public URLBuilder getURL(DisplayContext context) {
		return null;
	}
	
	@Override
	protected void internalHandleContent(DisplayContext context, URLParser url) throws IOException {
	}
	
	@Override
	public FrameScope getEnclosingScope() {
		return parentScope;
	}

	@Override
	public ModelScope getModelScope() {
		return getEnclosingScope().getModelScope();
	}

	@Override
	public <T extends Appendable> T appendClientReference(T out) throws IOException {
		return out;
	}
	
	@Override
	public void addCommandListener(CommandListener listener) {
		commandListenerRegistry.addCommandListener(listener);
	}
	
	@Override
	public boolean removeCommandListener(CommandListener listener) {
		return commandListenerRegistry.removeCommandListener(listener);
	}
	
	@Override
	public void clear() {
		commandListenerRegistry.clear();
	}
	
	@Override
	public CommandListener getCommandListener(String id) {
		return commandListenerRegistry.getCommandListener(id);
	}
	
	@Override
	public Collection<CommandListener> getCommandListener() {
		return commandListenerRegistry.getCommandListener();
	}

}
