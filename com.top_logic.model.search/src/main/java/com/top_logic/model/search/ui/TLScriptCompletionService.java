/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;
import com.top_logic.util.regex.TLRegexBuilder;

/**
 * Reusable service for computing TL-Script code completions.
 *
 * <p>
 * Extracted from {@link TLScriptAutoCompletionCommand} so that both the legacy Ace editor and the
 * new CodeMirror 6 editor can share the same completion logic.
 * </p>
 */
public class TLScriptCompletionService implements TLScriptConstants {

	/**
	 * Computes completions for the given line and prefix.
	 *
	 * @param context
	 *        The display context (needed for locale-aware documentation).
	 * @param line
	 *        The full text of the current line.
	 * @param prefix
	 *        The prefix typed so far (may be empty).
	 * @param caseSensitive
	 *        Whether to match case-sensitively.
	 * @return Sorted list of completions with scores assigned, possibly empty.
	 */
	public static List<CodeCompletion> computeCompletions(DisplayContext context, String line,
			String prefix, boolean caseSensitive) {
		Optional<List<CodeCompletion>> completions = createCompletions(context, line, prefix, caseSensitive);

		completions.ifPresent(list -> orderCompletions(list));

		return completions.orElse(Collections.emptyList());
	}

	private static Optional<List<CodeCompletion>> createCompletions(DisplayContext context, String line,
			String prefix, boolean caseSensitive) {
		if (inTLModelPartCompletionMode(line)) {
			return createTLModelPartCompletions(line, caseSensitive);
		} else if (inTextMode(line)) {
			return Optional.empty();
		} else {
			return createDefaultCompletion(context, line, prefix, caseSensitive);
		}
	}

	private static Optional<List<CodeCompletion>> createDefaultCompletion(DisplayContext context, String line,
			String prefix, boolean caseSensitive) {
		Optional<List<CodeCompletion>> completions = createFunctionCompletions(context, line, caseSensitive);

		if (!completions.isPresent()) {
			return createDefaultFunctions(context, line, prefix, caseSensitive);
		}

		return completions;
	}

	private static Optional<List<CodeCompletion>> createDefaultFunctions(DisplayContext context, String line,
			String prefix, boolean caseSensitive) {
		int lastIndexOf = line.lastIndexOf(prefix);

		if (lastIndexOf >= 0) {
			if (lastIndexOf == 0
				|| !Character.isJavaIdentifierPart(line.charAt(lastIndexOf - 1))) {
				return Optional.of(createFunctionCompletionsInternal(context, prefix, caseSensitive));
			}
		}

		return Optional.empty();
	}

	private static boolean inTLModelPartCompletionMode(String line) {
		return insideOf(line, "`", "[\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}:.# ]");
	}

	private static boolean inTextMode(String line) {
		String allowedCharacters = "[a-zA-Z0-9.{} ]";

		return insideOf(line, "\"", allowedCharacters) || insideOf(line, "'", allowedCharacters);
	}

	private static boolean insideOf(String line, String delimiter, String allowedCharacters) {
		String regex = createOddNumberOfDelimiterRegex(delimiter, allowedCharacters);

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);

		return matcher.matches();
	}

	private static String createOddNumberOfDelimiterRegex(String delimiter, String allowedCharacters) {
		String noDelimiterWord = "[^" + delimiter + "]*";

		return "^"
			+ "("
			+ noDelimiterWord
			+ "(?:"
			+ delimiter + allowedCharacters + "*" + delimiter + noDelimiterWord
			+ ")*"
			+ delimiter
			+ ")"
			+ allowedCharacters
			+ "*"
			+ "$";
	}

	private static Optional<List<CodeCompletion>> createFunctionCompletions(DisplayContext context, String line,
			boolean caseSensitive) {
		return getFunctionRegexResult(line).map(name -> {
			return createFunctionCompletionsInternal(context, name, caseSensitive);
		});
	}

	private static List<CodeCompletion> createFunctionCompletionsInternal(DisplayContext context, String prefix,
			boolean caseSensitive) {
		return getFunctionNames()
			.stream()
			.filter(functionName -> startsWith(functionName, prefix, caseSensitive))
			.map(functionName -> createCodeCompletion(context, functionName))
			.collect(Collectors.toList());
	}

	private static CodeCompletion createCodeCompletion(DisplayContext context, String functionName) {
		CodeCompletion completion = new CodeCompletion();

		completion.setName(functionName);
		completion.setValue(functionName);
		completion.setSnippet(functionName + "($1)$2");

		getDocHTML(context, functionName).ifPresent(doc -> completion.setDocHTML(doc));

		return completion;
	}

	private static Optional<String> getDocHTML(DisplayContext context, String name) {
		if (context == null) {
			return Optional.empty();
		}
		return SearchBuilder.getInstance().getDocumentation(context, name);
	}

	private static CodeCompletion createCodeCompletion(TLNamed modelPart, ModelPartMatch matcher,
			boolean caseSensitive) {
		CodeCompletion completion = new CodeCompletion();

		String name = modelPart.getName();

		completion.setObject(modelPart);
		completion.setName(name);
		completion.setValue(name);
		completion.setSnippet(getTLModelPartCompletionSnippet(name, matcher.getLastNotEmptyMatch()));

		return completion;
	}

	private static String getTLModelPartCompletionSnippet(String name, Optional<String> lastNotEmptyMatch) {
		return lastNotEmptyMatch.map(match -> {
			int lastIndexOf = match.lastIndexOf(".");
			if (lastIndexOf != -1) {
				return name.substring(lastIndexOf + 1);
			}
			return name;
		}).orElse(name);
	}

	private static Optional<List<CodeCompletion>> createTLModelPartCompletions(String line,
			boolean caseSensitive) {
		return getTLModelPartRegexResult(line).map(modelPartMatcher -> {
			return getMatchedTLModelPart(modelPartMatcher, caseSensitive).map(modelParts -> {
				return modelParts.stream()
					.map(modelPart -> createCodeCompletion(modelPart, modelPartMatcher, caseSensitive))
					.collect(Collectors.toList());
			}).orElse(Collections.emptyList());
		});
	}

	private static Optional<String> getFunctionRegexResult(String line) {
		String regex = "\\.(\\w*)$";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);

		if (matcher.find()) {
			return Optional.of(matcher.group(1));
		} else {
			return Optional.empty();
		}
	}

	private static List<String> getFunctionNames() {
		return SearchBuilder.getInstance().getMethodNames();
	}

	private static Optional<ModelPartMatch> getTLModelPartRegexResult(String line) {
		String regex = createTLModelPartConstantRegEx();

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);

		if (matcher.find()) {
			return Optional.of(new ModelPartMatch(matcher.group(1), matcher.group(2), matcher.group(3)));
		} else {
			return Optional.empty();
		}
	}

	private static String createTLModelPartConstantRegEx() {
		return new TLRegexBuilder()
			.group().nonWord().or().startOfLine().endGroup().then(MODEL_SCOPE_SEPARATOR)
			.capture().javaIdentifier()
				.group().then(".").javaIdentifier().endGroup().zeroOrMore()
			.endCapture()
			.group().then(MODEL_TYPE_SEPARATOR)
				.capture().javaIdentifier()
					.group().then(".").javaIdentifier().endGroup().zeroOrMore()
				.endCapture()
			.endGroup().maybe()
			.group().then(MODEL_ATTRIBUTE_SEPARATOR)
				.capture().javaIdentifier().endCapture()
			.endGroup().maybe().endOfLine().toString();
	}

	private static Optional<Collection<TLNamed>> getMatchedTLModelPart(ModelPartMatch modelPart,
			boolean caseSensitive) {
		String module = modelPart.getModuleName();
		String type = modelPart.getTypeName();
		String attribute = modelPart.getAttributeName();

		if (module == null) {
			return getTLModelModules(StringServices.EMPTY_STRING, caseSensitive);
		} else if (type == null) {
			if (attribute == null) {
				return getTLModelModules(module, caseSensitive);
			} else {
				return getTLModuleSingletons(module, attribute, caseSensitive);
			}
		} else {
			if (attribute == null) {
				return getTLModelTypes(module, type, caseSensitive);
			} else {
				return getTLModelAttributes(module, type, attribute, caseSensitive);
			}
		}
	}

	private static Optional<Collection<TLNamed>> getTLModuleSingletons(String module, String attribute,
			boolean caseSensitive) {
		return getTLModule(module)
			.map(moduleTmp -> getAllPrefixedTLModelParts(moduleTmp.getSingletons().stream(), attribute, caseSensitive));
	}

	private static Optional<Collection<TLNamed>> getTLModelTypes(String module, String type,
			boolean caseSensitive) {
		return getTLModule(module)
			.map(moduleTmp -> getAllPrefixedTLModelParts(getAllTypeNameStream(moduleTmp), type, caseSensitive));
	}

	private static Optional<Collection<TLNamed>> getTLModelModules(String module, boolean caseSensitive) {
		return Optional.of(getAllPrefixedTLModelParts(getAllModuleNameStream(), module, caseSensitive));
	}

	private static Optional<Collection<TLNamed>> getTLModelAttributes(String module, String type, String attribute,
			boolean caseSensitive) {
		return getTLStructuredType(module, type)
			.map(tlType -> getAllPrefixedTLModelParts(
				getTLModelAttributeStream((TLStructuredType) tlType), attribute, caseSensitive));
	}

	private static Optional<TLType> getTLStructuredType(String module, String type) {
		return getTLType(module, type).filter(tlType -> tlType instanceof TLStructuredType);
	}

	private static Collection<TLNamed> getAllPrefixedTLModelParts(Stream<? extends TLNamed> parts, String prefix,
			boolean caseSensitive) {
		return parts.filter(tlModelPart -> startsWith(tlModelPart.getName(), prefix, caseSensitive))
			.collect(Collectors.toSet());
	}

	private static boolean startsWith(String text, String prefix, boolean caseSensitive) {
		if (caseSensitive) {
			return text.startsWith(prefix);
		}

		return text.toUpperCase().startsWith(prefix.toUpperCase());
	}

	private static Optional<TLType> getTLType(String moduleName, String typeName) {
		return Optional.ofNullable(TLModelUtil.findType(moduleName, typeName));
	}

	private static Optional<TLModule> getTLModule(String moduleName) {
		try {
			return Optional.ofNullable(TLModelUtil.findModule(moduleName));
		} catch (TopLogicException exception) {
			return Optional.empty();
		}
	}

	private static Stream<? extends TLTypePart> getTLModelAttributeStream(TLStructuredType modelPart) {
		if (modelPart instanceof TLEnumeration) {
			return ((TLEnumeration) modelPart).getClassifiers().stream();
		}

		return modelPart.getAllParts().stream();
	}

	private static Stream<TLType> getAllTypeNameStream(TLModule module) {
		return module.getTypes().stream().filter(type -> !(type instanceof TLAssociation));
	}

	private static Stream<TLModule> getAllModuleNameStream() {
		return ModelService.getApplicationModel().getModules().stream();
	}

	private static void orderCompletions(List<CodeCompletion> completions) {
		completions.sort(CompletionByNameComparator.INSTANCE);

		for (int i = 0; i < completions.size(); i++) {
			completions.get(i).setScore(completions.size() - i);
		}
	}
}
