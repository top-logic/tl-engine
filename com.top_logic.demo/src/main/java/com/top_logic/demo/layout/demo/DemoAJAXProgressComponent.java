/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.demo.layout.demo.progess.ThreadedProgressInfo;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.progress.AJAXProgressComponent;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Demo implementation of a the AJAXProgressComponent. 
 * 
 * @author    <a href="mailto:cca@top-logic.com>cca</a>
 */
public class DemoAJAXProgressComponent extends AJAXProgressComponent {

	/**
	 * Configuration for the {@link WindowComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AJAXProgressComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			AJAXProgressComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(SignalStopProgress.COMMAND_ID);
			registry.registerButton("resetProgressBar");
		}

	}

	/** Use this a ProgressInfo  */
    protected transient ThreadedProgressInfo model;

    public DemoAJAXProgressComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(initialModel());
			return true;
		}
		return super.validateModel(context);
	}

	private Object initialModel() {
		Thread initialModel = new ThreadedProgressInfo("ProgressDemo", 1197, 73, 1, true);
		initialModel.start();
		return initialModel;
    }

    /**
     * called by the resetProgressBar-Command to reset the state
     * of the DemoProgressBar and invalidate the component. 
     */
    public static class ResetProgressBar extends AbstractCommandHandler {

        public ResetProgressBar(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
            ((DemoAJAXProgressComponent) aComponent).resetState();
            ((DemoAJAXProgressComponent) aComponent).invalidate();
            
            
            return HandlerResult.DEFAULT_RESULT;
        }

    }

}
