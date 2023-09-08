/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution.service.filter;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Algorithm that selects a certain command execution context.
 * 
 * @see com.top_logic.tool.execution.service.ConfiguredApprovalRule.Config#getContexts()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ExecutionContextFilter {

	/**
	 * Decision of a context match.
	 * 
	 * @see ExecutionContextFilter#matchesExecutionContext(LayoutComponent, BoundCommandGroup,
	 *      String)
	 */
	public static enum MatchResult {
		/**
		 * The context matches.
		 */
		MATCHES,

		/**
		 * The context does not match.
		 */
		NOT_MATCHES,

		/**
		 * It is unknown, whether the context matches.
		 * 
		 * <p>
		 * This is the case, if the filter must check special context (e.g. the command ID) but this
		 * context is not available during check.
		 * </p>
		 */
		UNDECIDED;

		/**
		 * Converts a boolean match decision to a defined {@link MatchResult}.
		 */
		public static MatchResult fromBoolean(boolean b) {
			return b ? MATCHES : NOT_MATCHES;
		}

		/**
		 * The result matches, if this and the other match.
		 */
		public MatchResult and(MatchResult other) {
			switch (this) {
				case MATCHES:
					return other;
				case NOT_MATCHES:
					return other.ifDefined(NOT_MATCHES);
				case UNDECIDED:
					return UNDECIDED;
			}
			throw unreacheable();
		}

		/**
		 * The result matches, if this or the other match.
		 */
		public MatchResult or(MatchResult other) {
			switch (this) {
				case MATCHES:
					return other.ifDefined(MATCHES);
				case NOT_MATCHES:
					return other;
				case UNDECIDED:
					return UNDECIDED;
			}
			throw unreacheable();
		}

		/**
		 * The result matches, if this does not match.
		 */
		public MatchResult not() {
			switch (this) {
				case MATCHES:
					return NOT_MATCHES;
				case NOT_MATCHES:
					return MATCHES;
				case UNDECIDED:
					return UNDECIDED;
			}
			throw unreacheable();
		}

		/**
		 * Whether there is a match.
		 */
		public boolean toBoolean() {
			switch (this) {
				case MATCHES:
					return true;
				case NOT_MATCHES:
					return false;
				case UNDECIDED:
					return false;
			}
			throw unreacheable();
		}

		private MatchResult ifDefined(MatchResult other) {
			return this == UNDECIDED ? UNDECIDED : other;
		}

		private UnreachableAssertion unreacheable() throws UnreachableAssertion {
			throw new UnreachableAssertion("No such result: " + this);
		}
	}

	/**
	 * Checks, whether a condition matches the given command execution context.
	 *
	 * @param component
	 *        The context component in which the command should be executed. A value of
	 *        <code>null</code> means to check whether a command with the other parameter value can
	 *        be executed in some component.
	 * @param commandGroup
	 *        The command group of the executed command.
	 * @param commandId
	 *        The concrete {@link CommandHandler#getID() ID} of the executed command. A value of
	 *        <code>null</code> means to check, if some command with the given command group can be
	 *        executed.
	 * @return Whether the given context is relevant, see {@link MatchResult#fromBoolean(boolean)}.
	 */
	MatchResult matchesExecutionContext(LayoutComponent component, BoundCommandGroup commandGroup, String commandId);

	/**
	 * The {@link ExecutionContextFilter} that matches all contexts.
	 */
	ExecutionContextFilter ALL = new ExecutionContextFilter() {
		@Override
		public MatchResult matchesExecutionContext(LayoutComponent component, BoundCommandGroup commandGroup,
				String commandId) {
			return MatchResult.MATCHES;
		}

		@Override
		public ExecutionContextFilter or(ExecutionContextFilter other) {
			return ALL;
		}
	};

	/**
	 * The {@link ExecutionContextFilter} that matches no context at all.
	 */
	ExecutionContextFilter NONE = new ExecutionContextFilter() {
		@Override
		public MatchResult matchesExecutionContext(LayoutComponent component, BoundCommandGroup commandGroup,
				String commandId) {
			return MatchResult.NOT_MATCHES;
		}

		@Override
		public ExecutionContextFilter or(ExecutionContextFilter other) {
			return other;
		}
	};

	/**
	 * Combines this with the given {@link ExecutionContextFilter}.
	 * 
	 * <p>
	 * The resulting filter matches all contexts of this filter and the other one.
	 * </p>
	 */
	default ExecutionContextFilter or(ExecutionContextFilter other) {
		ExecutionContextFilter self = this;
		return new ExecutionContextFilter() {
			@Override
			public MatchResult matchesExecutionContext(LayoutComponent component, BoundCommandGroup commandGroup,
					String commandId) {
				return self.matchesExecutionContext(component, commandGroup, commandId).or(
					other.matchesExecutionContext(component, commandGroup, commandId));
			}
		};
	}

	/**
	 * Combines this with the given {@link ExecutionContextFilter}.
	 * 
	 * <p>
	 * The resulting filter matches all contexts of this filter except those of the other one.
	 * </p>
	 */
	default ExecutionContextFilter except(ExecutionContextFilter other) {
		ExecutionContextFilter self = this;
		if (other == NONE) {
			return self;
		}
		return new ExecutionContextFilter() {
			@Override
			public MatchResult matchesExecutionContext(LayoutComponent component, BoundCommandGroup commandGroup,
					String commandId) {
				return self.matchesExecutionContext(component, commandGroup, commandId).and(
					other.matchesExecutionContext(component, commandGroup, commandId).not());
			}
		};
	}
}
