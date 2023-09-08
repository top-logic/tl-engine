/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.util.error.TopLogicException;

/**
 * Only override the {@link #getSteps()} method.
 *
 * TODO this should be named ...Controller
 *
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public abstract class StepAssistantComponent extends SimpleAssistentController {

	public static final boolean FIRST_STEP = true;
	public static final boolean LAST_STEP = true;
	public static final boolean SHOW_CANCEL = false;
	public static final boolean FINISH_STEP = true;

	public StepAssistantComponent(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * This method returns the
	 * {@link com.top_logic.tool.boundsec.assistent.SimpleAssistentController.SimpleStepInfo}s of
	 * this assistent and NEVER <code>null</code>.
	 */
	protected abstract SimpleStepInfo[] getSteps();

	/**
	 * Register all stepes here.
	 */
	@Override
	public void becomingVisible() {
		super.becomingVisible();
		if (!hasSteps()) {
  			registerSteps(getSteps());
		}
	}

	@Override
	protected SimpleStepInfo getStep(ComponentName aStepName) {
		SimpleStepInfo step = super.getStep(aStepName);

		if (step == null) {
			throw new TopLogicException(this.getClass(), "The step ('" + aStepName + "') is unknown. Please register the steps first you call this method.");
		}

		return step;
	}

	/**
	 * @see com.top_logic.tool.boundsec.assistent.AbstractAssistentController#isCloseInfoStep(ComponentName)
	 */
	@Override
	protected boolean isCloseInfoStep(ComponentName aCurrentStepName) {
		return getStep(aCurrentStepName).isCloseInfoStep();
	}

	/**
	 * @see com.top_logic.tool.boundsec.assistent.AbstractAssistentController#isFinishStep(ComponentName)
	 */
	@Override
	protected boolean isFinishStep(ComponentName aCurrentStepName) {
		return getStep(aCurrentStepName).isFinishStep();
	}

	/**
	 * @see com.top_logic.tool.boundsec.assistent.AbstractAssistentController#isFirstStep(ComponentName)
	 */
	@Override
	protected boolean isFirstStep(ComponentName aCurrentStepName) {
		return getStep(aCurrentStepName).isFirstStep();
	}

	/**
	 * @see com.top_logic.tool.boundsec.assistent.AbstractAssistentController#isLastStep(ComponentName)
	 */
	@Override
	public boolean isLastStep(ComponentName aCurrentStepName) {
		return getStep(aCurrentStepName).isLastStep();
	}

	/**
	 * @see com.top_logic.tool.boundsec.assistent.AbstractAssistentController#showForwardButton(ComponentName)
	 */
	@Override
	public boolean showForwardButton(ComponentName aCurrentStepName) {
		return getStep(aCurrentStepName).hasForwardButton();
	}

	/**
	 * @see com.top_logic.tool.boundsec.assistent.AbstractAssistentController#showBackwardButton(ComponentName)
	 */
	@Override
	public boolean showBackwardButton(ComponentName aCurrentStepName) {
		return getStep(aCurrentStepName).hasBackwardButton();
	}

	/**
	 * @see com.top_logic.tool.boundsec.assistent.AbstractAssistentController#showCancelButton(ComponentName)
	 */
	@Override
	public boolean showCancelButton(ComponentName aCurrentStepName) {
		return getStep(aCurrentStepName).hasCancelButton();
	}

}
