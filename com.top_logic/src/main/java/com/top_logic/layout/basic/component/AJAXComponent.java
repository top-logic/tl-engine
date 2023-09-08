/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.component;


import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateWriter;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponentVisitor;

/**
 * Abstract base class for all {@link com.top_logic.mig.html.layout.LayoutComponent}s that can be
 * incrementally updated by AJAX {@link com.top_logic.base.services.simpleajax.ClientAction}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AJAXComponent extends LayoutComponent {

	/**
	 * Configuration of the {@link AJAXComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends LayoutComponent.Config {
		// No special properties here
	}

	public AJAXComponent(InstantiationContext context, Config attr) throws ConfigurationException {
		super(context, attr);
	}
    
    public boolean isRevalidateRequested() {
    	return ajaxSupport().isRevalidateRequested();
    }

    public void revalidate(DisplayContext context, UpdateWriter actions) {
		LayoutComponent currentTargetComponent = actions.getTargetComponent();
		actions.setTargetComponent(this);
		try {
			ajaxSupport().revalidate(context, actions);
		} finally {
			actions.setTargetComponent(currentTargetComponent);
		}

	}
    
    /**
	 * Accessor for the AJAX mixin implementation.
	 */
	protected AJAXSupport ajaxSupport() {
		return NoAJAXSupport.INSTANCE;
	}

	@Override
	public void beforeRendering(DisplayContext displayContext) {
		super.beforeRendering(displayContext);
		ajaxSupport().startRendering();
	}

	@Override
	public boolean acceptVisitor(LayoutComponentVisitor aVisitor) {
		return aVisitor.visitAJAXComponent(this);
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		invalidateAJAXSupport();
	}

	public void invalidateAJAXSupport() {
		ajaxSupport().invalidate();
	}
}
