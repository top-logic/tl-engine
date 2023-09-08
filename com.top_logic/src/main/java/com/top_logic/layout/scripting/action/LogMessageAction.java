/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;


/**
 * Logs the given message at the given log level.
 * 
 * Use for example to log the progress of your test by including this action after every major chunk
 * of tests. <br/>
 * But be aware, that the framework for scripted tests throws an Exception and marks the current
 * test as failed, if an error or an fatal is logged during its execution. <br/>
 * You might use this to warn about incomplete testcases: As the last action of the incomplete
 * testcase, log an error telling "this testcase is still incomplete" (or something alike) and they
 * will fail and remember the person executing them at their incompleteness.
 * 
 * @see #setLevel(String) for valid/recognized levels.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface LogMessageAction extends ApplicationAction {

	public String getMessage();
	public void setMessage(String message);

	/**
	 * @see #setLevel
	 */
	public String getLevel();

	/**
	 * Sets the log level.
	 * 
	 * <p>
	 * Allowed values for <code>level</code> are:
	 * </p>
	 * <ul>
	 * <li>FATAL</li>
	 * <li>ERROR</li>
	 * <li>WARN</li>
	 * <li>INFO</li>
	 * <li>DEBUG</li>
	 * </ul>
	 * <p>
	 * Upper- and lowercase may be mixed arbitrarily.
	 * </p>
	 * 
	 * @see #getLevel()
	 */
	public void setLevel(String level);

}
