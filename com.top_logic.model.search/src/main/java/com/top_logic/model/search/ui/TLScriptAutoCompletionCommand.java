/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;
import com.top_logic.util.regex.TLRegexBuilder;

/**
 * Command calculating autocompletion suggestions for the given prefix.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLScriptAutoCompletionCommand extends ControlCommand implements TLScriptConstants {

	private static final String COMMAND_ID = "tlScriptAutoCompletion";

	private boolean _completeCaseSensitive;

	/**
	 * Creates a {@link TLScriptAutoCompletionCommand}.
	 */
	public TLScriptAutoCompletionCommand(boolean completeCaseSensitive) {
		super(COMMAND_ID);

		_completeCaseSensitive = completeCaseSensitive;
	}

	@Override
	public ResKey getI18NKey() {
		return ResKey.text("TLScript Autocompletion");
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		TLScriptCodeEditorControl scriptCodeControl = (TLScriptCodeEditorControl) control;

		String prefix = (String) arguments.get("prefix");
		String line = (String) arguments.get("line");

		Optional<List<CodeCompletion>> completions = createCompletions(commandContext, line, prefix);

		completions.ifPresent(completionsTmp -> orderCompletions(completionsTmp, CompletionByNameComparator.INSTANCE));

		sendCompletions(scriptCodeControl, completions);

		return HandlerResult.DEFAULT_RESULT;
	}

	private void sendCompletions(TLScriptCodeEditorControl control, Optional<List<CodeCompletion>> completions) {
		createJSONParsableCompletions(completions).ifPresent(sendCompletions(control));
	}

	private Consumer<? super Collection<Map<String, ?>>> sendCompletions(TLScriptCodeEditorControl control) {
		return completions -> control.sendCompletions(JSON.toString(completions));
	}

	private Optional<List<Map<String, ?>>> createJSONParsableCompletions(Optional<List<CodeCompletion>> completions) {
		return completions.map(completionsTmp -> createCompletionsPropertiesMap(completionsTmp));
	}

	private void orderCompletions(List<CodeCompletion> completions, Comparator<CodeCompletion> comparator) {
		completions.sort(comparator);

		setCompletionsScore(completions);
	}

	private void setCompletionsScore(List<CodeCompletion> orderedCompletions) {
		for (int i = 0; i < orderedCompletions.size(); i++) {
			orderedCompletions.get(i).setScore(orderedCompletions.size() - i);
		}
	}

	private List<Map<String, ?>> createCompletionsPropertiesMap(Collection<CodeCompletion> completions) {
		return completions.stream().map(completion -> {
			return createCompletionPropertiesMap(completion);
		}).collect(Collectors.toList());
	}

	private Map<String, Object> createCompletionPropertiesMap(CodeCompletion completion) {
		LinkedHashMap<String, Object> completionProperties = new LinkedHashMap<>();

		completionProperties.put("name", completion.getName());
		completionProperties.put("value", completion.getValue());
		completionProperties.put("score", completion.getScore());
		completionProperties.put("snippet", completion.getSnippet());
		completionProperties.put("docHTML", completion.getDocHTML());

		return completionProperties;
	}

	private Optional<List<CodeCompletion>> createCompletions(DisplayContext context, String line, String prefix) {
		if (inTLModelPartCompletionMode(line)) {
			return createTLModelPartCompletions(line);
		} else if (inTextMode(line)) {
			return Optional.empty();
		} else {
			return createDefaultCompletion(context, line, prefix);
		}
	}

	private Optional<List<CodeCompletion>> createDefaultCompletion(DisplayContext context, String line, String prefix) {
		Optional<List<CodeCompletion>> completions = createFunctionCompletions(context, line);

		if (!completions.isPresent()) {
			return createDefaultFunctions(context, line, prefix);
		}

		return completions;
	}

	private Optional<List<CodeCompletion>> createDefaultFunctions(DisplayContext context, String line, String prefix) {
		int lastIndexOf = line.lastIndexOf(prefix);

		if (lastIndexOf >= 0) {
			if (lastIndexOf == 0
				|| StringServices.BLANK_CHAR == line.charAt(lastIndexOf - 1)
				|| ',' == line.charAt(lastIndexOf - 1)) {
				return Optional.of(createFunctionCompletionsInternal(context, prefix));
			}
		}

		return Optional.empty();
	}

	private boolean inTLModelPartCompletionMode(String line) {
		return insideOf(line, "`", "[\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}:.# ]");
	}

	private boolean inTextMode(String line) {
		String allowedCharacters = "[a-zA-Z0-9.{} ]";

		return insideOf(line, "\"", allowedCharacters) || insideOf(line, "'", allowedCharacters);
	}

	private boolean insideOf(String line, String delimiter, String allowedCharacters) {
		String regex = createOddNumberOfDelemiterRegex(delimiter, allowedCharacters);

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);

		return matcher.matches();
	}

	private String createOddNumberOfDelemiterRegex(String delimiter, String allowedCharacters) {
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

	private Optional<List<CodeCompletion>> createFunctionCompletions(DisplayContext context, String line) {
		return getFunctionRegexResult(line).map(name -> {
			return createFunctionCompletionsInternal(context, name);
		});
	}

	private List<CodeCompletion> createFunctionCompletionsInternal(DisplayContext context, String prefix) {
		return getFunctionNames()
			.stream()
			.filter(functionName -> startsWith(functionName, prefix))
			.map(functionName -> createCodeCompletion(context, functionName))
			.collect(Collectors.toList());
	}

	private CodeCompletion createCodeCompletion(DisplayContext context, String functionName) {
		CodeCompletion completion = new CodeCompletion();

		completion.setName(functionName);
		completion.setValue(functionName);
		completion.setSnippet(functionName + "($1)$2");

		getDocHTML(context, functionName).ifPresent(doc -> completion.setDocHTML(doc));

		return completion;
	}

	private Optional<String> getDocHTML(DisplayContext context, String name) {
		return SearchBuilder.getInstance().getDocumentation(context, name);
	}

	private CodeCompletion createCodeCompletion(TLNamed modelPart, ModelPartMatch matcher) {
		CodeCompletion completion = new CodeCompletion();

		String name = modelPart.getName();

		completion.setObject(modelPart);
		completion.setName(name);
		completion.setValue(name);
		completion.setSnippet(getTLModelPartCompletionSnippet(name, matcher.getLastNotEmptyMatch()));

		return completion;
	}

	private String getTLModelPartCompletionSnippet(String name, Optional<String> lastNotEmptyMatch) {
		return lastNotEmptyMatch.map(match -> {
			int lastIndexOf = match.lastIndexOf(".");
			if (lastIndexOf != -1) {
				return name.substring(lastIndexOf + 1);
			}
			return name;
		}).orElse(name);
	}

	private Optional<List<CodeCompletion>> createTLModelPartCompletions(String line) {
		return getTLModelPartRegexResult(line).map(modelPartMatcher -> {
			return getMatchedTLModelPart(modelPartMatcher).map(modelParts -> {
				return modelParts.stream().map(modelPart -> createCodeCompletion(modelPart, modelPartMatcher))
					.collect(Collectors.toList());
			}).orElse(Collections.emptyList());
		});
	}

	private Optional<String> getFunctionRegexResult(String line) {
		String regex = "\\.(\\w*)$";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);

		if (matcher.find()) {
			return Optional.of(matcher.group(1));
		} else {
			return Optional.empty();
		}
	}

	private List<String> getFunctionNames() {
		return SearchBuilder.getInstance().getMethodNames();
	}

	private Optional<ModelPartMatch> getTLModelPartRegexResult(String line) {
		String regex = createTLModelPartConstantRegEx();

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);

		if (matcher.find()) {
			return Optional.of(new ModelPartMatch(matcher.group(1), matcher.group(2), matcher.group(3)));
		} else {
			return Optional.empty();
		}
	}

	private String createTLModelPartConstantRegEx() {
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

	private Optional<Collection<TLNamed>> getMatchedTLModelPart(ModelPartMatch modelPart) {
		String module = modelPart.getModuleName();
		String type = modelPart.getTypeName();
		String attribute = modelPart.getAttributeName();

		if (module == null) {
			return getTLModelModules(StringServices.EMPTY_STRING);
		} else if (type == null) {
			if (attribute == null) {
				return getTLModelModules(module);
			} else {
				return getTLModuleSingletons(module, attribute);
			}
		} else {
			if (attribute == null) {
				return getTLModelTypes(module, type);
			} else {
				return getTLModelAttributes(module, type, attribute);
			}
		}
	}

	private Optional<Collection<TLNamed>> getTLModuleSingletons(String module, String attribute) {
		return getTLModule(module)
			.map(moduleTmp -> getAllPrefixedTLModelParts(moduleTmp.getSingletons().stream(), attribute));
	}

	private Optional<Collection<TLNamed>> getTLModelTypes(String module, String type) {
		return getTLModule(module).map(moduleTmp -> getAllPrefixedTLModelParts(getAllTypeNameStream(moduleTmp), type));
	}

	private Optional<Collection<TLNamed>> getTLModelModules(String module) {
		return Optional.of(getAllPrefixedTLModelParts(getAllModuleNameStream(), module));
	}

	private Optional<Collection<TLNamed>> getTLModelAttributes(String module, String type, String attribute) {
		return getTLStructuredType(module, type).map(tlType -> getTLModelAttributes(attribute, tlType));
	}

	private Collection<TLNamed> getTLModelAttributes(String attribute, TLType tlStructuredType) {
		return getAllPrefixedTLModelParts(getTLModelAttributeStream((TLStructuredType) tlStructuredType), attribute);
	}

	private Optional<TLType> getTLStructuredType(String module, String type) {
		return getTLType(module, type).filter(tlType -> tlType instanceof TLStructuredType);
	}

	private Collection<TLNamed> getAllPrefixedTLModelParts(Stream<? extends TLNamed> parts, String prefix) {
		return parts.filter(tlModelPart -> startsWith(tlModelPart.getName(), prefix)).collect(Collectors.toSet());
	}

	private boolean startsWith(String text, String prefix) {
		if (_completeCaseSensitive) {
			return text.startsWith(prefix);
		}

		return text.toUpperCase().startsWith(prefix.toUpperCase());
	}

	private Optional<TLType> getTLType(String moduleName, String typeName) {
		return Optional.ofNullable(TLModelUtil.findTypeOptional(moduleName, typeName));
	}

	private Optional<TLModule> getTLModule(String moduleName) {
		try {
			return Optional.ofNullable(TLModelUtil.findModule(moduleName));
		} catch (TopLogicException exception) {
			return Optional.empty();
		}
	}

	private Stream<? extends TLTypePart> getTLModelAttributeStream(TLStructuredType modelPart) {
		if (modelPart instanceof TLEnumeration) {
			return ((TLEnumeration) modelPart).getClassifiers().stream();
		}

		return modelPart.getAllParts().stream();
	}

	private Stream<TLType> getAllTypeNameStream(TLModule module) {
		return module.getTypes().stream().filter(type -> !(type instanceof TLAssociation));
	}

	private Stream<TLModule> getAllModuleNameStream() {
		return ModelService.getApplicationModel().getModules().stream();
	}

}
