/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.basic.config.LocaleValueProvider;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TimeZoneValueProvider;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationActionOp;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.QualifiedComponentNameConstraint;

/**
 * Base configuration interface for all {@link ApplicationActionOp}s in the scripting framework.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ApplicationAction extends PolymorphicConfiguration<ApplicationActionOp<?>> {

	/** Name of {@link #getWindowName()}. */
	String WINDOW_NAME_ATTR = "window-name";

	@Override
	@ClassDefault(ApplicationActionOp.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * A comment about this action.
	 * 
	 * <p>
	 * The comment should describe this action for humans.
	 * </p>
	 */
	String getComment();

	/**
	 * @see #getComment()
	 */
	void setComment(String comment);

	/**
	 * The message that should be reported, if a failure happens during execution of this action.
	 */
	String getFailureMessage();

	/**
	 * @see #getFailureMessage()
	 */
	void setFailureMessage(String value);

	/**
	 * Returns the name of the user which has done / should execute this action.
	 */
	String getUserID();

	/**
	 * @see #getUserID()
	 */
	void setUserID(String id);

	/**
	 * Returns the {@link Locale} of the user which has done / should execute this action.
	 * 
	 * @return May be <code>null</code>. In that case the default user locale is used.
	 */
	@Format(LocaleValueProvider.class)
	Locale getLocale();

	/**
	 * @see #getLocale()
	 */
	void setLocale(Locale locale);

	/**
	 * Returns the {@link TimeZone} of the user which has done / should execute this action.
	 * 
	 * @return May be <code>null</code>. In that case the default user time zone is used.
	 */
	@Format(TimeZoneValueProvider.class)
	TimeZone getTimeZone();

	/**
	 * @see #getTimeZone()
	 */
	void setTimeZone(TimeZone timeZone);

	/**
	 * The window in which the action has to be executed.
	 * <p>
	 * The empty {@link String} means: The main application window.
	 * </p>
	 * <p>
	 * This is the {@link WindowScope#getName() name} of the {@link WindowScope}.
	 * </p>
	 */
	@Name(WINDOW_NAME_ATTR)
	@Constraint(QualifiedComponentNameConstraint.class)
	ComponentName getWindowName();

	/**
	 * @see #getWindowName()
	 */
	void setWindowName(ComponentName windowName);

}
