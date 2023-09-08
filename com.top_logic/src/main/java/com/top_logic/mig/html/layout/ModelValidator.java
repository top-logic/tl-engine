/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.Logger;
import com.top_logic.layout.DisplayContext;

/**
 * One model revalidating pass of a component tree. 
 * 
 * <p>
 * TODO TSA document me!
 * </p>
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ModelValidator extends DefaultDescendingLayoutVisitor {

	private DisplayContext context;

	/** whether a component's model is invalid. */
	private LayoutComponent _detectedInvalid;

    @Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
        if (aComponent.isVisible()) {
			context.getProcessingInfo().setComponentName(aComponent.getName());

			try {
				boolean wasAlreadyValid = aComponent.performValidation(context);
				if (!wasAlreadyValid) {
					_detectedInvalid = aComponent;
				}
			} catch (Throwable ex) {
				Logger.error("Cannot validate " + aComponent, ex, ModelValidator.class);
			}
            
            return super.visitLayoutComponent(aComponent);
        } else {
            return false;
        }
    }

	public void init(DisplayContext context) {
		this.context = context;
	}

	public void reset() {
		_detectedInvalid = null;
	}

	public boolean detectedInvalidModel() {
		return _detectedInvalid != null;
	}

	public LayoutComponent getInvalidComponentExample() {
		return _detectedInvalid;
	}
    
}

