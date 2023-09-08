/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.security;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.assistent.StepAssistantComponent;

/**
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public class DemoSecurityAssistentController extends StepAssistantComponent {

	/**
	 * Typed configuration interface definition for {@link DemoSecurityAssistentController}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends StepAssistantComponent.Config {

		@Mandatory
		@Name("firstStep")
		ComponentName getFirstStep();

		@Mandatory
		@Name("secondStep")
		ComponentName getSecondStep();
	}

	public DemoSecurityAssistentController(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected SimpleStepInfo[] getSteps() {
		return new SimpleStepInfo[] {
			getStep1(),
			getStep2()
		};
	}

	private SimpleStepInfo getStep1() {
		SimpleStepInfo result =
			new SimpleStepInfo(getConfig().getFirstStep(), getConfig().getSecondStep(), null,
				/* command holder */null, /* first */true, /* last */false, /* close */
				false, /* finish */false);
		return result;
	}

	private SimpleStepInfo getStep2() {
		SimpleStepInfo result =
			new SimpleStepInfo(getConfig().getSecondStep(), null, /* command holder */null, /* first */false,
				/* last */true, /* close */true, /* finish */
				true);
		result.setBackwardButton(false);
		result.setCancelButton(false);
		return result;
	}

}
