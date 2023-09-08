/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.Logger;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.util.DefaultValidationQueue;
import com.top_logic.util.ToBeValidated;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ToBeValidated} which inspects the component tree and validates {@link LayoutComponent}
 * which state {@link LayoutComponent#isModelValid() model invalid}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LegacyModelValidator implements ToBeValidated {

	/** The component to validate */
	private final LayoutComponent _component;

	/** Visitor to validate the given {@link #_component} and its children */
	private final ModelValidator _validator = new ModelValidator();

	/** Number of usage of this {@link LegacyModelValidator} */
	private int _passes = 0;

	/**
	 * Creates a new {@link LegacyModelValidator}.
	 * @param component
	 *        the component to validate
	 */
	public LegacyModelValidator(LayoutComponent component) {
		_component = component;
	}

	@Override
	public void validate(DisplayContext context) {
		_validator.init(context);
		_component.acceptVisitorRecursively(_validator);
		boolean needsNext = _validator.detectedInvalidModel();
		LayoutComponent invalidComponent = _validator.getInvalidComponentExample();
		_validator.reset();
		_passes++;
		LayoutContext layoutContext = _component.getMainLayout().getLayoutContext();
		if (needsNext) {
			layoutContext.notifyInvalid(this);
			if (_passes > 1013) {
				Logger.warn("Excessive component validation: " + invalidComponent, DefaultValidationQueue.class);
			}
			if (_passes > 1023) {
				Logger.error("Component '" + invalidComponent + "' could not be validated.",
					LegacyModelValidator.class);
				throw new TopLogicException(MainLayout.class, "unableToValidateModel after " + _passes + " passes");
			}
		} else {
			if (layoutContext instanceof SubsessionHandler) {
				if (((SubsessionHandler) layoutContext).hasPendingToBeValidated()) {
					layoutContext.notifyInvalid(this);
				}
			}
//			com.top_logic.basic.Logger.info("Needed " + _passes + " step for validation of " + _component,
//				LegacyModelValidator.class);
		}

	}

}
