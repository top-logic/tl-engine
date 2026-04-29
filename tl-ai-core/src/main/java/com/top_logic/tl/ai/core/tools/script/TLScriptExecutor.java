/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.script;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.core.tools.script.messages.ErrorType;
import com.top_logic.tl.ai.core.tools.script.messages.ValidationError;
import com.top_logic.tl.ai.core.tools.script.messages.ValidationResult;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
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
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * Core utility for validating and executing TLScript code.
 *
 * <p>
 * This class provides the shared logic for TLScript validation and execution used by both
 * AI tools (in tl-ai-core) and MCP tools (in tl-ai-mcp-server). It performs:
 * </p>
 * <ul>
 * <li>Syntax validation (parser errors)</li>
 * <li>Model reference validation (undefined classes/properties/types)</li>
 * <li>Type checking (type compatibility)</li>
 * <li>Script execution with configurable transaction control</li>
 * <li>Result serialization (primitives, collections, TLObjects)</li>
 * </ul>
 * <p>
 * The tool uses the same validation pipeline as the TLScript console editor, leveraging
 * {@link SearchExpressionParser} for parsing and {@link ModelReferenceChecker} for semantic
 * validation.
 * </p>
 */
public class TLScriptExecutor {

	/**
	 * Result of TLScript execution.
	 */
	public static class ExecutionResult {
		private final boolean _success;

		private final Object _result;

		private final String _errorMessage;

		private ExecutionResult(boolean success, Object result, String errorMessage) {
			_success = success;
			_result = result;
			_errorMessage = errorMessage;
		}

		/**
		 * Creates a successful execution result.
		 */
		public static ExecutionResult success(Object result) {
			return new ExecutionResult(true, result, null);
		}

		/**
		 * Creates a failed execution result.
		 */
		public static ExecutionResult error(String errorMessage) {
			return new ExecutionResult(false, null, errorMessage);
		}

		/**
		 * Whether the execution was successful.
		 */
		public boolean isSuccess() {
			return _success;
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

		/**
		 * Converts a TLScript execution result to a JSON-compatible data structure.
		 *
		 * @param result
		 *        The result to convert (may be null).
		 * @return JSON-compatible object (primitives, Lists, Maps).
		 */
		private static Object convertToJsonData(Object result) {
			if (result == null) {
				return null;
			}

			// Primitives - return as-is
			if (result instanceof String || result instanceof Number || result instanceof Boolean) {
				return result;
			}

			// Collections - recursively convert elements
			if (result instanceof Collection<?>) {
				List<Object> jsonList = new ArrayList<>();
				for (Object item : (Collection<?>) result) {
					jsonList.add(convertToJsonData(item));
				}
				return jsonList;
			}

			// Maps - recursively convert keys and values
			if (result instanceof Map<?, ?>) {
				Map<String, Object> jsonMap = new LinkedHashMap<>();
				for (Map.Entry<?, ?> entry : ((Map<?, ?>) result).entrySet()) {
					String key = String.valueOf(entry.getKey());
					Object value = convertToJsonData(entry.getValue());
					jsonMap.put(key, value);
				}
				return jsonMap;
			}

			// TLObject (model instances) - minimal serialization
			if (result instanceof TLObject) {
				TLObject obj = (TLObject) result;
				Map<String, Object> serialized = new LinkedHashMap<>();
				serialized.put("_tid", obj.tId().asString());
				serialized.put("_type", TLModelUtil.qualifiedName(obj.tType()));
				serialized.put("_display", MetaLabelProvider.INSTANCE.getLabel(obj));
				return serialized;
			}

			// Arrays - convert to list and process
			if (result.getClass().isArray()) {
				Object[] array = (Object[]) result;
				return convertToJsonData(Arrays.asList(array));
			}

			// Fallback: use {@link Object#toString()} representation
			return String.valueOf(result);
		}

		/**
		 * Determines the result type for JSON response.
		 *
		 * @param result
		 *        The result object.
		 * @return Type name: "null", "string", "number", "boolean", "collection", "map", "object".
		 */
		private static String determineResultType(Object result) {
			if (result == null) {
				return "null";
			}
			if (result instanceof String) {
				return "string";
			}
			if (result instanceof Number) {
				return "number";
			}
			if (result instanceof Boolean) {
				return "boolean";
			}
			if (result instanceof Collection<?> || result.getClass().isArray()) {
				return "collection";
			}
			if (result instanceof Map<?, ?>) {
				return "map";
			}
			return "object";
		}

		/**
		 * Serializes this execution result to a JSON string.
		 *
		 * <p>
		 * On success, returns a JSON object with:
		 * </p>
		 * <ul>
		 * <li>success: boolean - true</li>
		 * <li>executed: boolean - true</li>
		 * <li>result: any - the execution result</li>
		 * <li>resultType: string - type of the result</li>
		 * <li>message: string - success message</li>
		 * </ul>
		 *
		 * <p>
		 * On failure, returns the same format as {@link com.top_logic.tl.ai.core.tools.script.messages.ErrorResult}.
		 * </p>
		 *
		 * @return JSON string representation of the execution result.
		 */
		public String toJson() {
			if (!_success) {
				// Use ErrorResult for failures
				return com.top_logic.tl.ai.core.tools.script.messages.ErrorResult.create()
					.setError(_errorMessage)
					.toString();
			}

			try {
				java.io.StringWriter buffer = new java.io.StringWriter();
				com.top_logic.common.json.gstream.JsonWriter json =
					new com.top_logic.common.json.gstream.JsonWriter(buffer);
				json.setIndent("  ");

				json.beginObject();
				json.name("success").value(true);
				json.name("executed").value(true);

				// Write the result value
				Object jsonData = convertToJsonData(_result);
				json.name("result");
				writeJsonValue(json, jsonData);

				json.name("resultType").value(determineResultType(_result));
				json.name("message").value("Script executed successfully");
				json.endObject();

				return buffer.toString();

			} catch (Exception ex) {
				// Fallback to ErrorResult if serialization fails
				return com.top_logic.tl.ai.core.tools.script.messages.ErrorResult.create()
					.setError("Serialization failed: " + ex.getMessage())
					.toString();
			}
		}

		/**
		 * Writes a JSON value recursively.
		 */
		private static void writeJsonValue(com.top_logic.common.json.gstream.JsonWriter json, Object value)
				throws java.io.IOException {
			if (value == null) {
				json.nullValue();
			} else if (value instanceof String) {
				json.value((String) value);
			} else if (value instanceof Number) {
				json.value((Number) value);
			} else if (value instanceof Boolean) {
				json.value((Boolean) value);
			} else if (value instanceof List<?>) {
				json.beginArray();
				for (Object item : (List<?>) value) {
					writeJsonValue(json, item);
				}
				json.endArray();
			} else if (value instanceof Map<?, ?>) {
				json.beginObject();
				for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
					json.name(String.valueOf(entry.getKey()));
					writeJsonValue(json, entry.getValue());
				}
				json.endObject();
			} else {
				json.value(String.valueOf(value));
			}
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
	 * Executes TLScript code and returns the result.
	 *
	 * @param script
	 *        The TLScript code to execute.
	 * @param withCommit
	 *        If true, execute within transaction and commit changes; if false, discard
	 *        modifications.
	 * @param args
	 *        Optional arguments to pass to the script ($arg1, $arg2, ...).
	 * @return The execution result.
	 */
	public static ExecutionResult execute(String script, boolean withCommit, List<?> args) {
		// First validate the script
		ValidationResult validation = validate(script);
		if (!validation.isValid()) {
			ValidationError error = validation.getError();
			return ExecutionResult.error(error.getMessage());
		}

		// Build and compile expression
		try {
			TLModel model = ModelService.getApplicationModel();
			SearchExpression searchExpr = SearchBuilder.toSearchExpression(model, validation.getExpr());

			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			QueryExecutor executor = QueryExecutor.compile(kb, model, searchExpr);

			// Prepare script arguments
			Args scriptArgs = prepareArgs(args);

			// Execute with transaction control
			Object result = executeWithTransactionControl(executor, scriptArgs, withCommit, kb);

			return ExecutionResult.success(result);

		} catch (I18NRuntimeException ex) {
			// Execution error (runtime)
			String message = Resources.getInstance().getString(ex.getErrorKey());
			return ExecutionResult.error(message);

		} catch (Exception ex) {
			// Unexpected execution error
			return ExecutionResult.error(ex.getMessage());
		}
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

	/**
	 * Executes the script with transaction control based on {@code withCommit} flag.
	 */
	private static Object executeWithTransactionControl(QueryExecutor executor, Args scriptArgs,
			boolean withCommit, KnowledgeBase kb) {
		if (withCommit) {
			// Execute within transaction - commit changes
			try (Transaction tx = kb.beginTransaction(ResKey.text("Execute TLScript"))) {
				Object result = executor.executeWith(
					executor.context(true, null, null),
					scriptArgs);
				tx.commit();
				return result;
			}
		} else {
			// Execute read-only - discard modifications
			return kb.withoutModifications(() ->
				executor.executeWith(
					executor.context(true, null, null),
					scriptArgs));
		}
	}

}
