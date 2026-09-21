/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * {@link Tool}s for tests that need a catalog entry rather than a tool.
 *
 * <p>
 * A test about authorization, about catalog order or about what a run advertises cares about a
 * tool's name, its {@link ToolMode} and its group. It does not care what the tool does. Written out
 * as an anonymous {@code new Tool() {...}} that is five overrides of boilerplate around the one
 * line that matters, and the repository had 49 of them across 27 classes before #487.
 * </p>
 *
 * <p>
 * Not named {@code TestTools}: the build's includes collect every class under these package roots
 * whose name starts with {@code Test}, and a helper collected as a test class is a test class with
 * no tests. See {@code TestSuiteReachability}.
 * </p>
 */
public final class ToolStubs {

	/** The empty object schema every stub declares - no arguments, because nothing calls them. */
	public static final String NO_ARGUMENTS = "{\"type\":\"object\",\"properties\":{}}";

	private ToolStubs() {
		// Static factory only.
	}

	/** A stub in the given mode, ungrouped, answering {@code "ok"}. */
	public static Tool tool(String name, ToolMode mode) {
		return builder(name).mode(mode).build();
	}

	/** A stub in the given mode, ungrouped, answering {@code result}. */
	public static Tool tool(String name, ToolMode mode, String result) {
		return builder(name).mode(mode).result(result).build();
	}

	/** A {@link ToolMode#READ} stub declaring the given tool group. */
	public static Tool inGroup(String name, String group) {
		return builder(name).mode(ToolMode.READ).group(group).build();
	}

	/** A stub in the given mode declaring the given tool group. */
	public static Tool inGroup(String name, ToolMode mode, String group) {
		return builder(name).mode(mode).group(group).build();
	}

	/** A {@link ToolMode#READ} stub declaring the given qualified model types. */
	public static Tool onTypes(String name, String... types) {
		return builder(name).mode(ToolMode.READ).types(List.of(types)).build();
	}

	/**
	 * A stub for a test that does care what the tool does - one that throws, records its arguments
	 * or reads its {@link ToolExecutionContext}.
	 */
	public static Builder builder(String name) {
		return new Builder(name);
	}

	/** Collects what a stub should answer before {@link #build() building} it. */
	public static final class Builder {

		private final String _name;

		private String _description;

		private String _group = "";

		private List<String> _types = List.of();

		private ToolSchema _schema;

		private ToolMode _mode = ToolMode.READ;

		private boolean _concurrencySafe = true;

		private BiFunction<Map<String, Object>, ToolExecutionContext, ToolResult> _execute =
			(args, context) -> ToolResult.of("ok");

		Builder(String name) {
			_name = name;
			_description = "Stub: " + name;
		}

		/** What the tool tells the model it does. Defaults to {@code "Stub: <name>"}. */
		public Builder description(String description) {
			_description = description;
			return this;
		}

		/** The arguments the stub declares. Defaults to none. */
		public Builder schema(ToolSchema schema) {
			_schema = schema;
			return this;
		}

		/** The tool group the stub declares. Defaults to ungrouped, which is fail-closed. */
		public Builder group(String group) {
			_group = group;
			return this;
		}

		/** The model types the stub declares it works on. Defaults to none, i.e. no claim. */
		public Builder types(List<String> types) {
			_types = types;
			return this;
		}

		/** What kind of state change the stub claims to make. Defaults to {@link ToolMode#READ}. */
		public Builder mode(ToolMode mode) {
			_mode = mode;
			return this;
		}

		/** Whether the stub may run beside other concurrency-safe tools. Defaults to {@code true}. */
		public Builder concurrencySafe(boolean concurrencySafe) {
			_concurrencySafe = concurrencySafe;
			return this;
		}

		/** A fixed answer. */
		public Builder result(String result) {
			return execute((args, context) -> ToolResult.of(result));
		}

		/** What the stub does when it is called. */
		public Builder execute(BiFunction<Map<String, Object>, ToolExecutionContext, ToolResult> execute) {
			_execute = execute;
			return this;
		}

		/** The stub. */
		public Tool build() {
			String name = _name;
			String description = _description;
			String group = _group;
			List<String> types = _types;
			ToolMode mode = _mode;
			boolean concurrencySafe = _concurrencySafe;
			BiFunction<Map<String, Object>, ToolExecutionContext, ToolResult> execute = _execute;
			ToolSchema schema =
				_schema != null ? _schema : new ToolSchema(_name, _description, NO_ARGUMENTS);

			return new Tool() {
				@Override
				public String name() {
					return name;
				}

				@Override
				public String description() {
					return description;
				}

				@Override
				public ToolSchema schema() {
					return schema;
				}

				@Override
				public String group() {
					return group;
				}

				@Override
				public List<String> types() {
					return types;
				}

				@Override
				public ToolMode mode() {
					return mode;
				}

				@Override
				public boolean isConcurrencySafe() {
					return concurrencySafe;
				}

				@Override
				public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
					return execute.apply(args, context);
				}
			};
		}
	}
}
