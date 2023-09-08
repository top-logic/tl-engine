/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo.model;

import java.util.List;

/**
 * Demo class that represents a phone carrier. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoCarrier {
	String name;
	List dialPrefixes;
	
	public DemoCarrier(String name, List dialPrefixes) {
		this.name = name;
		this.dialPrefixes = dialPrefixes;
	}

	public List getDialPrefixes() {
		return dialPrefixes;
	}

	public String getName() {
		return name;
	}
	
}
