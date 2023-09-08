/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.progress.AbstractProgressComponent;
import com.top_logic.layout.progress.ProgressInfo;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.AwaitProgressAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Await the completion of a {@link ProgressInfo}.
 * 
 * <p>
 * The model of the target component has to be {@link ProgressInfo}.
 * </p>
 * 
 * In case of an {@link AbstractProgressComponent} this action will close the dialog.
 * 
 * @author <a href=mailto:kha@top-logic.com>kha</a>
 * 
 * @see com.top_logic.layout.messagebox.AwaitProgressDialog.Op for waiting for new-style progress
 *      dialogs.
 */
public class AwaitProgressActionOp extends ComponentActionOp<AwaitProgressAction> {

	private static final int POLLING_INTERVAL_MILLIS = 100;

	/**
	 * Creates a {@link AwaitProgressActionOp}.
	 * 
	 * @see AbstractApplicationActionOp#AbstractApplicationActionOp(InstantiationContext,
	 *      ApplicationAction)
	 */
    public AwaitProgressActionOp(InstantiationContext context, AwaitProgressAction config) {
        super(context, config);
    }
    
    @Override
	protected final Object process(ActionContext context, LayoutComponent component, Object argument) {
		if (!component.isVisible()) {
			// Progress component has not been displayed at all. Action has finished quickly enough.
			return argument;
		}
		try {
        	long startTime = System.currentTimeMillis();
			ProgressInfo progressInfo = null;
			while (!timedOut(startTime)) {
				progressInfo = fetchProgressInfo(component);
				if (finished(progressInfo)) {
					break;
				}

				Thread.sleep(POLLING_INTERVAL_MILLIS);
			}
			if (!finished(progressInfo)) {
				ApplicationAssertions.fail(config,
					"Progress did not finish in time, elapsed '" + elapsedMillisSince(startTime) + "ms'.");
			}
		} catch (InterruptedException ex) {
			throw new AbortExecutionException(ex);
		}

		if (component instanceof AbstractProgressComponent) {
			// During unit test execution, there is not UI timer component that closes the dialog
			// after the operation has finished. Therefor the await action must explicitly close the
			// dialog and trigger the actions that are "normally" executed by the client-side
			// progess update timer.
			closeDialog((AbstractProgressComponent) component);
		}

        return argument;
    }

	private boolean finished(ProgressInfo progressInfo) {
		return progressInfo != null && progressInfo.isFinished();
	}

	private ProgressInfo fetchProgressInfo(LayoutComponent component) {
		return (ProgressInfo) component.getModel();
	}

	private boolean timedOut(long startTime) {
		long maxWait = config.getMaxSleep();
		return maxWait <= 0 || elapsedMillisSince(startTime) > maxWait;
	}

	private long elapsedMillisSince(long startTime) {
		return System.currentTimeMillis() - startTime;
	}

	private void closeDialog(AbstractProgressComponent progressComponent) {
		progressComponent.finishParent();
		progressComponent.closeDialog();
	}

}