/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.script;

import java.io.StringReader;
import java.util.List;
import java.util.function.Function;

import com.top_logic.tl.ai.core.tools.script.messages.ErrorType;
import com.top_logic.tl.ai.core.tools.script.messages.ValidationError;
import com.top_logic.tl.ai.core.tools.script.messages.ValidationResult;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.parser.SearchExpressionParserConstants;
import com.top_logic.model.search.expr.parser.Token;
import com.top_logic.model.search.expr.parser.TokenMgrError;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ModelReferenceChecker;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * Core utility for validating, compiling and executing TL-Script, shared by the tools that run it.
 *
 * <p>
 * It performs:
 * </p>
 * <ul>
 * <li>Syntax validation (parser errors)</li>
 * <li>Model reference validation (undefined classes/properties/types)</li>
 * <li>Type checking (type compatibility)</li>
 * <li>The split between reading and changing: what a script does besides computing its value
 * ({@link SideEffects})</li>
 * <li>Execution, either discarding every modification or within the transaction of the turn</li>
 * </ul>
 * <p>
 * The tool uses the same validation pipeline as the TLScript console editor, leveraging
 * {@link SearchExpressionParser} for parsing and {@link ModelReferenceChecker} for semantic
 * validation.
 * </p>
 */
public class TLScriptExecutor {

	/**
	 * The message a read-only tool refuses a script with that changes something.
	 */
	/** What the transaction of a change is called. */
	private static final ResKey CHANGE = ResKey.text("changeData");

	public static final String NOT_READ_ONLY = "The expression changes data (it creates, sets, deletes or has"
		+ " another side effect), and this tool only reads. Remove the writing part. To change data, use"
		+ " updateObject, createObject or deleteObject for one known object, or changeData.";

	/**
	 * Result of TLScript execution.
	 */
	public static class ExecutionResult {
		private final boolean _success;

		private final Object _result;

		private final String _errorMessage;

		private final boolean _refusal;

		private ExecutionResult(boolean success, Object result, String errorMessage, boolean refusal) {
			_success = success;
			_result = result;
			_errorMessage = errorMessage;
			_refusal = refusal;
		}

		/**
		 * Creates a successful execution result.
		 */
		public static ExecutionResult success(Object result) {
			return new ExecutionResult(true, result, null, false);
		}

		/**
		 * Creates a failed execution result.
		 */
		public static ExecutionResult error(String errorMessage) {
			return new ExecutionResult(false, null, errorMessage, false);
		}

		/**
		 * Creates the result of a script a read-only caller refused because it changes something:
		 * nothing is wrong with how it is written.
		 */
		public static ExecutionResult refused(String errorMessage) {
			return new ExecutionResult(false, null, errorMessage, true);
		}

		/**
		 * Whether the execution was successful.
		 */
		public boolean isSuccess() {
			return _success;
		}

		/**
		 * Whether the script was refused for what it would change, rather than failing because of how
		 * it is written - it does not compile, or it fails while it runs.
		 */
		public boolean isRefusal() {
			return _refusal;
		}

		/**
		 * The execution result (only available if execution succeeded).
		 */
		public Object getResult() {
			return _result;
		}

		/**
		 * The error message (only available if execution failed).
		 */
		public String getErrorMessage() {
			return _errorMessage;
		}
	}

	/**
	 * A script compiled to change data, with what it does besides computing its value.
	 *
	 * @param executor
	 *        The compiled script.
	 * @param changes
	 *        Whether it has any effect at all; a script without one only reads.
	 * @param outsideKnowledgeBase
	 *        The name of a function whose effect lies outside the knowledge base, which rolling a
	 *        transaction back does not take back; <code>null</code> if every effect is a write to the
	 *        knowledge base.
	 */
	public record Change(QueryExecutor executor, boolean changes, String outsideKnowledgeBase) {
		// Sole record.
	}

	/**
	 * Thrown for a script a read-only caller refuses because it has an effect.
	 *
	 * <p>
	 * An {@link IllegalArgumentException} like a script that does not compile, so a caller that does
	 * not care tells nobody apart; one that does - {@code present}, which helps with a query that does
	 * not compile - can.
	 * </p>
	 */
	public static final class NotReadOnly extends IllegalArgumentException {

		NotReadOnly() {
			super(NOT_READ_ONLY);
		}
	}

	/**
	 * Validates TLScript code without executing it.
	 *
	 * @param script
	 *        The TLScript code to validate.
	 * @return The validation result.
	 */
	public static ValidationResult validate(String script) {
		SearchExpressionParser parser = new SearchExpressionParser(new StringReader(script));

		try {
			// Step 1: Parse (syntax validation)
			Expr expr = parser.expr();

			// Step 2: Check for trailing content (garbage after valid expression)
			Token currentToken = parser.token;
			if (currentToken.kind != SearchExpressionParserConstants.EOF) {
				currentToken = parser.getNextToken();
			}
			if (currentToken.kind != SearchExpressionParserConstants.EOF) {
				// Trailing garbage found
				int line = currentToken.beginLine;
				int column = currentToken.beginColumn;
				return ValidationResult.create()
					.setValid(false)
					.setError(ValidationError.create()
						.setType(ErrorType.SYNTAX_ERROR)
						.setMessage("Unexpected content after expression")
						.setLine(line)
						.setColumn(column));
			}

			// Step 3: Full validation (model references + type checking)
			ModelReferenceChecker.checkModelElements(expr);

			// Success - all validation passed
			return ValidationResult.create()
				.setValid(true)
				.setExpr(expr);

		} catch (ParseException ex) {
			// Syntax error with location information
			Token errorToken = ex.currentToken.next;
			int line = errorToken.beginLine;
			int column = errorToken.beginColumn;
			String message = ex.getMessage();
			return ValidationResult.create()
				.setValid(false)
				.setError(ValidationError.create()
					.setType(ErrorType.SYNTAX_ERROR)
					.setMessage(message)
					.setLine(line)
					.setColumn(column));

		} catch (TokenMgrError ex) {
			// Lexical error (invalid characters, malformed tokens)
			return ValidationResult.create()
				.setValid(false)
				.setError(ValidationError.create()
					.setType(ErrorType.LEXICAL_ERROR)
					.setMessage(ex.getMessage())
					.setLine(0)
					.setColumn(0));

		} catch (I18NRuntimeException ex) {
			// Model reference or type checking error
			String message = Resources.getInstance().getString(ex.getErrorKey());
			return ValidationResult.create()
				.setValid(false)
				.setError(ValidationError.create()
					.setType(ErrorType.VALIDATION_ERROR)
					.setMessage(message)
					.setLine(0)
					.setColumn(0));

		} catch (Exception ex) {
			// Unexpected error during validation
			return ValidationResult.create()
				.setValid(false)
				.setError(ValidationError.create()
					.setType(ErrorType.VALIDATION_ERROR)
					.setMessage("Validation failed: " + ex.getMessage())
					.setLine(0)
					.setColumn(0));
		}
	}

	/**
	 * Evaluates TL-Script code, discarding every modification it makes.
	 *
	 * <p>
	 * For a caller whose expressions are its own configuration rather than a model's - the eval's
	 * graders. A model's script goes through {@link #executeReadOnly(String)}, which refuses one that
	 * would change something before it runs.
	 * </p>
	 *
	 * @param script
	 *        The TLScript code to execute.
	 * @param args
	 *        Optional arguments to pass to the script ($arg1, $arg2, ...).
	 * @return The execution result.
	 */
	public static ExecutionResult execute(String script, List<?> args) {
		QueryExecutor executor;
		try {
			executor = compile(script);
		} catch (RuntimeException ex) {
			return ExecutionResult.error(messageOf(ex));
		}

		try {
			Object result = PersistencyLayer.getKnowledgeBase().withoutModifications(() -> executor.executeWith(
				executor.context(true, null, null),
				prepareArgs(args)));
			return ExecutionResult.success(result);
		} catch (RuntimeException ex) {
			return ExecutionResult.error(messageOf(ex));
		}
	}

	/**
	 * Compiles TLScript code, after the checks {@link #validate(String)} makes.
	 *
	 * <p>
	 * For a caller that runs the same expression more than once - a function applied to every row of
	 * a result - and therefore must not parse it per row. {@link #evaluate(QueryExecutor, Object...)}
	 * runs what this returns the way {@link #execute(String, List)} runs a script.
	 * </p>
	 *
	 * @param script
	 *        The TLScript code.
	 * @return The compiled expression.
	 * @throws IllegalArgumentException
	 *         If the code does not parse, names something the model does not have, or does not
	 *         compile; the message says which, in the words {@link #validate(String)} uses.
	 */
	public static QueryExecutor compile(String script) {
		return executor(expression(script));
	}

	/**
	 * Compiles TLScript code that must not change anything: an expression that creates, sets,
	 * deletes or otherwise has a side effect is refused before it runs.
	 *
	 * <p>
	 * For the tools that read with TL-Script - {@code query}, {@code present} - and are classified
	 * {@code READ} for it, so they run without asking anybody. Discarding the modifications after
	 * the fact ({@link #evaluate(QueryExecutor, Object...)}) is not enough for those: a side effect
	 * outside the knowledge base - a mail, a message - is not discarded. Every part of the expression
	 * is looked at, the arguments of a function included ({@link SideEffects}).
	 * </p>
	 *
	 * @throws NotReadOnly
	 *         For an expression with a side effect.
	 * @throws IllegalArgumentException
	 *         As {@link #compile(String)}.
	 */
	public static QueryExecutor compileReadOnly(String script) {
		SearchExpression expression = expression(script);
		if (SideEffects.any(expression)) {
			throw new NotReadOnly();
		}
		return executor(expression);
	}

	/**
	 * Compiles a script that is to change data, and tells what it does besides computing a value.
	 *
	 * <p>
	 * The caller decides with it: a script without any effect is a read and belongs to {@code query},
	 * and a script whose effect reaches outside the knowledge base cannot be tried in a transaction
	 * that is rolled back afterwards.
	 * </p>
	 *
	 * @throws IllegalArgumentException
	 *         As {@link #compile(String)}.
	 */
	public static Change compileChange(String script) {
		SearchExpression expression = expression(script);
		return new Change(executor(expression), SideEffects.any(expression),
			SideEffects.outsideKnowledgeBase(expression));
	}

	private static SearchExpression expression(String script) {
		ValidationResult validation = validate(script);
		if (!validation.isValid()) {
			throw new IllegalArgumentException(validation.getError().getMessage());
		}
		try {
			return SearchBuilder.toSearchExpression(ModelService.getApplicationModel(), validation.getExpr());
		} catch (I18NRuntimeException ex) {
			throw new IllegalArgumentException(Resources.getInstance().getString(ex.getErrorKey()), ex);
		}
	}

	private static QueryExecutor executor(SearchExpression expression) {
		try {
			return QueryExecutor.compile(PersistencyLayer.getKnowledgeBase(), ModelService.getApplicationModel(),
				expression);
		} catch (I18NRuntimeException ex) {
			throw new IllegalArgumentException(Resources.getInstance().getString(ex.getErrorKey()), ex);
		}
	}

	/**
	 * Evaluates TLScript code that must not change anything, see {@link #compileReadOnly(String)}.
	 *
	 * @return The result; an error result for code that does not compile, has a side effect, or
	 *         fails while it runs. Only the second is {@link ExecutionResult#isRefusal() a refusal}.
	 */
	public static ExecutionResult executeReadOnly(String script) {
		QueryExecutor executor;
		try {
			executor = compileReadOnly(script);
		} catch (NotReadOnly ex) {
			return ExecutionResult.refused(ex.getMessage());
		} catch (RuntimeException ex) {
			return ExecutionResult.error(messageOf(ex));
		}
		try {
			return ExecutionResult.success(evaluate(executor));
		} catch (RuntimeException ex) {
			return ExecutionResult.error(messageOf(ex));
		}
	}

	/**
	 * Runs a compiled change within the transaction that is open on this thread - the transaction of
	 * the agent's turn - and commits it into that one.
	 *
	 * <p>
	 * The engine's transactions do not really nest: a nested one that is rolled back marks the outer
	 * one as failed, and nothing it wrote can be taken back on its own. A failure is therefore
	 * rethrown after the rollback has named its reason, and the caller has to give up the whole turn
	 * for it.
	 * </p>
	 *
	 * @param executor
	 *        The compiled change, see {@link #compileChange(String)}.
	 * @param beforeCommit
	 *        Receives the script's result while the change is not yet committed, and returns what
	 *        this method returns; it refuses the commit by throwing. Where no transaction is open on
	 *        this thread the change's own one is committed for real, so this is the last moment to
	 *        look at what it did.
	 * @return What {@code beforeCommit} returned.
	 * @throws RuntimeException
	 *         Whatever the script or {@code beforeCommit} throws; {@link #messageOf(RuntimeException)}
	 *         says it.
	 */
	public static <R> R executeChange(QueryExecutor executor, Function<Object, R> beforeCommit) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(CHANGE)) {
			R result;
			try {
				result = beforeCommit.apply(run(executor));
			} catch (RuntimeException ex) {
				// Name the reason on the rollback. This transaction is nested inside the agent turn's
				// transaction, and the engine poisons the outer one with
				// NESTED_TRANSACTION_ROLLED_BACK__COMMIT_REASON, filling in whatever reason the
				// rollback was given. Rolling back without one - which is what the try-with-resources
				// close() does - leaves the agent's later commit failing with "rolled back: " and
				// nothing after the colon.
				tx.rollback(failureKey(ex), ex);
				throw ex;
			}
			tx.commit();
			return result;
		}
	}

	/**
	 * Runs a compiled script in the context of this thread, with the modifications it makes: the
	 * caller owns the transaction.
	 */
	static Object run(QueryExecutor executor) {
		return executor.executeWith(executor.context(true, null, null), Args.none());
	}

	/**
	 * Runs a {@link #compile(String) compiled} expression on the given arguments, discarding every
	 * modification it makes.
	 *
	 * @param executor
	 *        The compiled expression; a function receives the arguments, anything else ignores them.
	 * @param args
	 *        The arguments.
	 * @return The result.
	 * @throws RuntimeException
	 *         Whatever the expression throws; an {@link I18NRuntimeException} carries the engine's
	 *         own sentence, see {@link #messageOf(RuntimeException)}.
	 */
	public static Object evaluate(QueryExecutor executor, Object... args) {
		Args scriptArgs = prepareArgs(List.of(args));
		return PersistencyLayer.getKnowledgeBase().withoutModifications(() -> executor.executeWith(
			executor.context(true, null, null),
			scriptArgs));
	}

	/**
	 * The sentence a failed evaluation is reported with: the engine's own, translated, where it has
	 * one, otherwise the Java message.
	 */
	public static String messageOf(RuntimeException failure) {
		if (failure instanceof I18NRuntimeException i18n) {
			return Resources.getInstance().getString(i18n.getErrorKey());
		}
		String message = failure.getMessage();
		return message != null ? message : failure.getClass().getSimpleName();
	}

	/**
	 * The failure of a script execution, as a {@link ResKey} the engine can embed in the message of
	 * the transaction it poisons.
	 *
	 * <p>
	 * A veto - a denied create, a failed constraint - arrives as an {@link I18NRuntimeException} and
	 * already carries a translated sentence. Anything else has only a Java message.
	 * </p>
	 */
	private static ResKey failureKey(RuntimeException failure) {
		if (failure instanceof I18NRuntimeException i18n) {
			return i18n.getErrorKey();
		}
		String message = failure.getMessage();
		return ResKey.text(message != null ? message : failure.getClass().getName());
	}

	/**
	 * Prepares script arguments from a list.
	 */
	private static Args prepareArgs(List<?> argsList) {
		if (argsList == null || argsList.isEmpty()) {
			return Args.none();
		}
		return Args.some(argsList.toArray());
	}

}
