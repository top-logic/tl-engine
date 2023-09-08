/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.values;

/**
 * A singleton that serves as demo value for properties that contain singletons.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DemoSingletonFirst extends DemoAbstractSingleton {

	/** The instance of the {@link DemoSingletonFirst}. */
	public static final DemoSingletonFirst INSTANCE = new DemoSingletonFirst();

}
