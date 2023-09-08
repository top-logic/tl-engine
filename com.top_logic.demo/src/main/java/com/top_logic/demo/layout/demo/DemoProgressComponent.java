/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.demo.layout.demo.progess.ThreadedProgressInfo;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.progress.AbstractProgressComponent;
import com.top_logic.layout.progress.ProgressInfo;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demonstration of how to use the {@link AbstractProgressComponent}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class DemoProgressComponent extends AbstractProgressComponent {

	/** Starts the demo activity and opens a progress dialog for it. */
	public static final class DemoProgressCommandHandler extends AbstractProgressCommandHandler {

		/**
		 * Configuration options for {@link DemoProgressCommandHandler}.
		 */
		public interface Config extends AbstractProgressCommandHandler.Config {
			/**
			 * @see com.top_logic.layout.progress.AbstractProgressComponent.AbstractProgressCommandHandler#getProgressComponent()
			 */
			ComponentName getProgressComponent();
		}

		/** Nothing unexpected */
		public static final String COMMAND_ID = "demoProgressCommand";

		private ComponentName _progressComponent;

		/** Nothing unexpected */
		public DemoProgressCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
			_progressComponent = config.getProgressComponent();
		}

		@Override
		protected ProgressInfo startThread(DisplayContext displayContext, LayoutComponent component,
				Object model, Map<String, Object> arguments) {
			int expectedSteps = 23;
			int increment = 2;
			String threadName = _progressComponent.qualifiedName();
			ThreadedProgressInfo progressInfo =
				new ThreadedProgressInfo(threadName, expectedSteps, increment, 1, false);
			progressInfo.start();
			return progressInfo;
		}

		@Override
		protected ComponentName getProgressComponent() {
			return _progressComponent;
		}
	}

	/** Nothing unexpected */
	public DemoProgressComponent(InstantiationContext context, Config attributes) throws ConfigurationException {
		super(context, attributes);
	}

	@Override
	public void finishParent() {
		// Nothing to do
	}

}
