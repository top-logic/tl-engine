/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a parameterized SQL statement with arguments.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class SQLFragment implements SQL {

	private final String source;
	private final List arguments;
	
	public SQLFragment(String source) {
		this.source = source;
		this.arguments = new ArrayList();
	}
	
	public SQLFragment(String source, int expectedSize) {
		this.source = source;
		this.arguments = new ArrayList(expectedSize);
	}
	
	public SQLFragment(String source, Argument arg1) {
		this(source, 1);
		
		addArgument(arg1);
	}

	public SQLFragment(String source, Argument arg1, Argument arg2) {
		this(source, 2);
		
		addArgument(arg1);
		addArgument(arg2);
	}
	
	public SQLFragment(String source, Argument arg1, Argument arg2, Argument arg3) {
		this(source, 2);
		
		addArgument(arg1);
		addArgument(arg2);
		addArgument(arg3);
	}
	
	public SQLFragment(String source, Argument[] args) {
		this(source, args.length);

		addArguments(args);
	}
	
	public SQLFragment(String source, List args) {
		this(source, args.size());

		addArguments(args);
	}
	
	public SQLFragment(String source, Argument arg1, List otherArgs) {
		this(source, otherArgs.size() + 1);
		
		addArgument(arg1);
		addArguments(otherArgs);
	}

	@Override
	public String getSource() {
		return source;
	}
	
	public void addArgument(Argument arg) {
		this.arguments.add(arg);
	}

	public void addArguments(List args) {
		this.arguments.addAll(args);
	}
	
	public void addArguments(Argument[] args) {
		for (int n = 0, cnt = args.length; n < cnt; n++) {
			addArgument(args[n]);
		}
	}
	
	@Override
	public List getArguments() {
		return arguments;
	}
	
	@Override
	public String toString() {
		return source + " " + arguments;
	}
	
}
