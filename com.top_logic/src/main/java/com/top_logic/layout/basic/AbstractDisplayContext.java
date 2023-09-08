/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.DefaultInteractionContext;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.ProcessingInfo;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.Validator;
import com.top_logic.layout.WindowScope;
import com.top_logic.mig.html.Media;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Base class for {@link DisplayContext} implementations that consistently
 * handles the current scope.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDisplayContext extends DefaultInteractionContext implements DisplayContext {

	/**
	 * The current scope.
	 * 
	 * @see DisplayContext#getExecutionScope()
	 */
	private ControlScope scope;
	
	private boolean invalid;

	private Resources resources;

	private ProcessingInfo processingInfo;

	private Media _outputMedia = Media.BROWSER;

	/**
	 * Constructs a new {@link DisplayContext} with an initial scope and some Resources,
	 * 
	 * @see DisplayContext#getExecutionScope() for a description under what circumstance the initial
	 *      scope may be <code>null</code>.
	 */
	protected AbstractDisplayContext() {
		this.processingInfo = new ProcessingInfo();
    }

    @Override
	public final ControlScope getExecutionScope() {
    	checkNotInvalid();
        return scope;
    }
    
    @Override
	public LayoutContext getLayoutContext() {
		return getSubSessionContext().getLayoutContext();
	}

	@Override
	public TLSubSessionContext getSubSessionContext() {
		return (TLSubSessionContext) super.getSubSessionContext();
	}

	@Override
	public TLSessionContext getSessionContext() {
		return (TLSessionContext) super.getSessionContext();
	}

	@Override
	public void initScope(ControlScope initialScope) {
    	if (this.scope != null) {
    		throw new IllegalStateException("There is already a different " + ControlScope.class.getSimpleName() + ": " + this.scope);
    	}
    	assert initialScope != null: "control scope must not be null";
    	this.scope = initialScope;
    }
    
    @Override
	public WindowScope getWindowScope() {
    	if (this.scope == null) {
    		return LayoutUtils.getWindowScope(this);
    	}
    	return this.scope.getFrameScope().getWindowScope();
    }

    @Override
	public Resources getResources() {
    	checkNotInvalid();
		if (resources == null) {
			resources = Resources.getInstance(getSubSessionContext());
		}
		return resources;
	}

	@Override
	public final <T> void renderScoped(ControlScope renderingScope, Renderer<T> renderer, TagWriter out, T value)
			throws IOException {
		checkNotInvalid();
		assert renderingScope != null: "control scope must not be null";
		ControlScope originalScope = this.scope;
		this.scope = renderingScope;
		try {
			renderer.write(this, out, value);
		} finally {
			this.scope = originalScope;
		}
	}
	
	@Override
	public <T> void validateScoped(ControlScope validationScope, Validator<? super T> validator, UpdateQueue queue, T obj) {
		checkNotInvalid();
		assert validationScope != null: "control scope must not be null";
		ControlScope originalScope = this.scope;
		this.scope = validationScope;
		try {
			validator.validate(this, queue, obj);
		} finally {
			this.scope = originalScope;
		}
	}

	@Override
	public HandlerResult executeScoped(ControlScope executionScope, Command command) {
		checkNotInvalid();
		assert executionScope != null : "control scope must not be null";
		ControlScope originalScope = this.scope;
		this.scope = executionScope;
		try {
			return command.executeCommand(this);
		} finally {
			this.scope = originalScope;
		}

	}

	@Override
	public ProcessingInfo getProcessingInfo() {
		return this.processingInfo;
	}

	/**
	 * Invalidates this {@link AbstractDisplayContext}. After calling this method
	 * each access to the {@link DisplayContext} interface throws an
	 * {@link IllegalStateException}.
	 * 
	 */
	@Override
	public void invalidate() {
		this.invalid = true;
		super.invalidate();
	}

	protected final void checkNotInvalid() {
		if (invalid) {
			throw new IllegalStateException("This DisplayContext is invalid since the corresponding request is already responded");
		}
	}

	public <T, E1 extends Throwable, E2 extends Throwable> T runWithContext(ComputationEx2<T, E1, E2> computation)
			throws E1, E2 {
		HttpServletRequest request = asRequest();
		if (DefaultDisplayContext.hasDisplayContext(request)) {
			return computation.run();
		} else {
			DefaultDisplayContext.setupDisplayContext(request, this);
			try {
				return computation.run();
			} finally {
				DefaultDisplayContext.teardownDisplayContext(request, this);
			}
		}
	}

	@Override
	public void installSubSessionContext(SubSessionContext subSession) {
		if (subSession != null && !(subSession instanceof TLSubSessionContext)) {
			throw new IllegalArgumentException();
		}
		// Resources depend on sub session and are created lazily.
		resources = null;
		super.installSubSessionContext(subSession);
	}

	@Override
	public void installSessionContext(com.top_logic.basic.SessionContext session) {
		if (session != null && !(session instanceof TLSessionContext)) {
			throw new IllegalArgumentException();
		}
		super.installSessionContext(session);
	}

	@Override
	public Media getOutputMedia() {
		return _outputMedia;
	}

	/**
	 * Setter for {@link #getOutputMedia()}.
	 */
	public void setOutputMedia(Media outputMedia) {
		_outputMedia = Objects.requireNonNull(outputMedia);
	}

}
