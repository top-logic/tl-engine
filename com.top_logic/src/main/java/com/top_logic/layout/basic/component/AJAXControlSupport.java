/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.component;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link AJAXControlSupport} is a special {@link ControlSupport} which
 * additionally wraps an {@link AJAXSupport}. It works in the same way as a {@link ControlSupport}
 * does but it also uses its additional {@link AJAXSupport} in the methods defined in
 * {@link AJAXSupport}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AJAXControlSupport extends ControlSupport {

	/** a wrapped {@link AJAXSupport} */
	private final AJAXSupport 		additionalAJAXSupport;

	/**
	 * Creates a {@link AJAXControlSupport}.
	 * 
	 * @param component
	 *            the {@link LayoutComponent} to initialize the {@link ControlSupport} aspect.
	 * @param additionalAJAXSupport
	 *            must not be <code>null</code>
	 */
	public AJAXControlSupport(LayoutComponent component, AJAXSupport additionalAJAXSupport) {
		super(component);
		if (additionalAJAXSupport == null) {
			throw new IllegalArgumentException("'additionalAJAXSupport' must not be 'null'.");
		}
		this.additionalAJAXSupport = additionalAJAXSupport;
	}

	@Override
	public void startRendering() {
		super.startRendering();
		additionalAJAXSupport.startRendering();
	}
	
	@Override
	public boolean isRevalidateRequested() {
		if (additionalAJAXSupport.isRevalidateRequested()) {
			return true;
		}
		else {
			return super.isRevalidateRequested();
		}
	}
	
	@Override
	protected void revalidateScoped(DisplayContext context, UpdateQueue actions) {
		super.revalidateScoped(context, actions);
		additionalAJAXSupport.revalidate(context, actions);
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		additionalAJAXSupport.invalidate();
	}

}

