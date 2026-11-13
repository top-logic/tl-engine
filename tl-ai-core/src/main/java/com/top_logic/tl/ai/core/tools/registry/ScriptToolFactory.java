/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationDescriptorConfig;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.i18n.HtmlResKey;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.InternationalizationEditor.WithTooltipConfiguration;
import com.top_logic.layout.form.values.edit.editor.PlainEditor;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Define;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tl.ai.core.tools.parameters.DeclaredParameters;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.registry.ToolFactory;

/**
 * A {@link ToolFactory} that creates tools from TL-Script implementations.
 *
 * <p>
 * A tool declares its {@link ScriptToolConfig#getParameters() parameters} as an agent command does,
 * as {@link DeclaredParameters}: the model receives their JSON schema, the values it passes are bound
 * and checked against the declaration before the body runs, and the body sees each of them as
 * {@code $name}.
 * </p>
 */
public class ScriptToolFactory implements ToolFactory, ConfiguredInstance<ScriptToolFactory.Config> {

	/**
	 * Configuration for {@link ScriptToolFactory}.
	 */
	@DisplayOrder({
		Config.TOOLS
	})
	public interface Config extends PolymorphicConfiguration<ScriptToolFactory> {

		/**
		 * Configuration property name for tools.
		 *
		 * @see #getTools()
		 */
		String TOOLS = "tools";

		/**
		 * The tools to create.
		 */
		@Name(TOOLS)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, ScriptToolConfig> getTools();

	}

	/**
	 * Configuration for a single TL-Script tool.
	 */
	@DisplayOrder({
		NamedConfigMandatory.NAME_ATTRIBUTE,
		ScriptToolConfig.LABEL,
		ScriptToolConfig.DESCRIPTION,
		ScriptToolConfig.PARAMETERS,
		ScriptToolConfig.MODE,
		ScriptToolConfig.CONCURRENCY_SAFE,
		ScriptToolConfig.TRUNCATION_HINT,
		ScriptToolConfig.IMPLEMENTATION,
		ScriptToolConfig.SUMMARY,
		ScriptToolConfig.COMMIT_MESSAGE,
	})
	public interface ScriptToolConfig extends NamedConfigMandatory {

		/**
		 * Configuration property name for the label.
		 *
		 * @see #getLabel()
		 */
		String LABEL = "label";

		/**
		 * Configuration property name for the description.
		 *
		 * @see #getDescription()
		 */
		String DESCRIPTION = "description";

		/**
		 * Configuration property name for the parameters.
		 *
		 * @see #getParameters()
		 */
		String PARAMETERS = "parameters";

		/**
		 * Configuration property name for the implementation.
		 *
		 * @see #getImplementation()
		 */
		String IMPLEMENTATION = "implementation";

		/**
		 * Configuration property name for mode.
		 *
		 * @see #getMode()
		 */
		String MODE = "mode";

		/**
		 * Configuration property name for concurrency-safe.
		 *
		 * @see #isConcurrencySafe()
		 */
		String CONCURRENCY_SAFE = "concurrency-safe";

		/**
		 * Configuration property name for the truncation hint.
		 *
		 * @see #getTruncationHint()
		 */
		String TRUNCATION_HINT = "truncation-hint";

		/**
		 * Configuration property name for the summary expression.
		 *
		 * @see #getSummary()
		 */
		String SUMMARY = "summary";

		/**
		 * Configuration property name for the commit message expression.
		 *
		 * @see #getCommitMessage()
		 */
		String COMMIT_MESSAGE = "commit-message";

		/**
		 * The name of the tool, unique within its factory.
		 */
		@Override
		String getName();

		/**
		 * The internationalized name of the tool, the commit message of a call that has no other.
		 */
		@Name(LABEL)
		@Nullable
		@ItemDisplay(ItemDisplayType.MONOMORPHIC)
		@WithTooltipConfiguration(false)
		ResKey getLabel();

		/**
		 * Setter for {@link #getLabel()}.
		 */
		void setLabel(ResKey value);

		/**
		 * What the tool does and when to call it, told to the model.
		 */
		@Name(DESCRIPTION)
		@DisplayMinimized
		HtmlResKey getDescription();

		/**
		 * Setter for {@link #getDescription()}.
		 */
		void setDescription(HtmlResKey value);

		/**
		 * The values the agent passes when calling the tool.
		 *
		 * <p>
		 * Declared as the parameters of an agent command, in the form of the <code>&lt;properties&gt;</code>
		 * of a layout template: per parameter its name and type, a label whose tooltip describes
		 * the parameter to the model, whether it is mandatory, its default value, its allowed
		 * values (<code>&lt;options&gt;</code> or an enumeration type) and its constraints. A parameter
		 * holding a model object has the type {@code com.top_logic.model.TLObject}, the format
		 * {@code com.top_logic.tl.ai.core.tools.model.AgentObjectFormat} and the model type in
		 * <code>&lt;object-type&gt;</code>; the agent passes the id of the object, which is read with the
		 * rights of the run. See {@link DeclaredParameters}.
		 * </p>
		 *
		 * <pre>
		 * &lt;parameters&gt;
		 *   &lt;property name="title" type="String"&gt;
		 *     &lt;label&gt;
		 *       &lt;en&gt;Title&lt;/en&gt;
		 *       &lt;tooltip&gt;&lt;en&gt;The title of the new ticket.&lt;/en&gt;&lt;/tooltip&gt;
		 *     &lt;/label&gt;
		 *     &lt;mandatory/&gt;
		 *   &lt;/property&gt;
		 * &lt;/parameters&gt;
		 * </pre>
		 */
		@Name(PARAMETERS)
		@Label("Parameters")
		ConfigurationDescriptorConfig getParameters();

		/**
		 * Setter for {@link #getParameters()}.
		 */
		void setParameters(ConfigurationDescriptorConfig value);

		/**
		 * The TL-Script expression computing the result of a call.
		 *
		 * <p>
		 * Each of the {@link #getParameters() parameters} is bound as variable of its name, e.g.
		 * {@code $title}.
		 * </p>
		 */
		@Mandatory
		@PropertyEditor(PlainEditor.class)
		@Name(IMPLEMENTATION)
		Expr getImplementation();

		/**
		 * Setter for {@link #getImplementation()}.
		 */
		void setImplementation(Expr value);

		/**
		 * What kind of state change this tool makes. Defaults to {@link ToolMode#DATA_WRITE}.
		 *
		 * @see Tool#mode()
		 */
		@Name(MODE)
		@FormattedDefault("DATA_WRITE")
		ToolMode getMode();

		/**
		 * Whether this tool can safely run in parallel with other concurrency-safe tools.
		 *
		 * @see Tool#isConcurrencySafe()
		 */
		@Name(CONCURRENCY_SAFE)
		@BooleanDefault(true)
		boolean isConcurrencySafe();

		/**
		 * How many characters of this tool's result reach the model before it is cut short.
		 *
		 * <p>
		 * A tool that answers with a long document needs more room than the installation grants by
		 * default; one that answers with a status word needs far less, and saying so keeps a runaway
		 * result from crowding the conversation. Unset - zero or less - uses the installation's
		 * default.
		 * </p>
		 */
		@Name(TRUNCATION_HINT)
		@IntDefault(-1)
		int getTruncationHint();

		/**
		 * What is kept of this tool's result when the conversation is compacted, computed from the
		 * result.
		 *
		 * <p>
		 * One argument, the value the implementation answered; the expression states the short form
		 * of it, for example <code>rows -&gt; $rows.size() + " invoices"</code>. Compaction keeps this
		 * instead of the full result, so a long answer stops costing what it cost when it was fresh.
		 * Unset keeps the beginning of the result, as before.
		 * </p>
		 *
		 * <p>
		 * It is disclosed under the same read rights as the result itself, so it may name model
		 * objects.
		 * </p>
		 */
		@Name(SUMMARY)
		@Nullable
		Expr getSummary();

		/**
		 * What a call of this tool changes, in the words the revision log records it with, computed
		 * from the call's arguments.
		 *
		 * <p>
		 * The tool's {@link #getParameters() parameters} are bound as in the
		 * {@link #getImplementation() implementation}; for example
		 * <code>"Contact " + $name + " created"</code>, or an internationalized text such as
		 * <code>#("Contact {0} created"@en, "Kontakt {0} angelegt"@de).fill($name)</code>.
		 * It is evaluated right before the call runs, so it must not change anything. Unset, or
		 * answering nothing, records the call with the tool's {@link #getLabel() label}, or with a
		 * generic message naming the tool.
		 * </p>
		 *
		 * @see Tool#commitMessage(Map, com.top_logic.tl.ai.tool.api.ToolExecutionContext)
		 */
		@Name(COMMIT_MESSAGE)
		@Nullable
		Expr getCommitMessage();

	}

	private final Config _config;

	private final List<Tool> _tools;

	/**
	 * Creates a new {@link ScriptToolFactory} from the given configuration.
	 */
	public ScriptToolFactory(InstantiationContext context, Config config) {
		_config = config;
		_tools = new ArrayList<>();

		// Map key == tool name (enforced by @Key on getTools()), so values() is sufficient.
		for (ScriptToolConfig toolConfig : config.getTools().values()) {
			DeclaredParameters parameters = DeclaredParameters.of(toolConfig.getParameters());
			QueryExecutor executor =
				QueryExecutor.compile(withParameters(parameters, toolConfig.getImplementation()));

			String llmDescription = ToolDescriptions.englishPlainText(toolConfig.getDescription());
			Expr summary = toolConfig.getSummary();
			Expr commitMessage = toolConfig.getCommitMessage();

			_tools.add(new ScriptToolExecutor(
				toolConfig.getName(),
				llmDescription,
				parameters,
				executor,
				toolConfig.getMode(),
				toolConfig.isConcurrencySafe(),
				summary == null ? null : QueryExecutor.compile(summary),
				toolConfig.getTruncationHint(),
				commitMessage == null ? null : QueryExecutor.compile(withParameters(parameters, commitMessage)),
				toolConfig.getLabel()));
		}
	}

	/**
	 * The given expression as a function of the tool's parameters, in the order of
	 * {@link DeclaredParameters#names()}.
	 */
	private static Expr withParameters(DeclaredParameters parameters, Expr body) {
		return Define.withVariables(parameters.names(), body);
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public List<Tool> createTools() {
		return _tools;
	}

}
