/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelAdapter;
import com.top_logic.layout.basic.ComponentCommandModel;
import com.top_logic.layout.basic.WrappedCommandModel;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.form.values.edit.initializer.UUIDInitializer;
import com.top_logic.layout.scripting.action.CheckExecutability;
import com.top_logic.layout.scripting.action.CheckLabeledExecutability;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorCommand;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorPluginFactory;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.gui.inspector.InspectorModel;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.AssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.button.ButtonLabelAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.button.ButtonTooltipAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.button.ButtonTooltipCaptionAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.button.ButtonTooltipElseLabelAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.button.ButtonVisibilityAssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo.StaticInfoPlugin;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ReferenceFactory;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.recorder.ref.ui.button.LabeledButtonNaming;
import com.top_logic.layout.scripting.runtime.action.CheckExecutabilityOp;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponentScope;
import com.top_logic.mig.html.layout.TopLevelComponentScope;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.Resources;

/**
 * {@link GuiInspectorCommand} for {@link ButtonControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ButtonInspector extends GuiInspectorCommand<ButtonControl, ButtonControl> {

	private static final class CommandExecutabilityAssertionPlugin extends AssertionPlugin<ButtonControl> {

		private static final String FIELD_REASON = "reason";

		private static final String FIELD_EXECUTABLE = "executable";

		private static final Document TEMPLATE = DOMUtil.parseThreadSafe(
			"	<tbody"
				+ "		xmlns='" + HTMLConstants.XHTML_NS + "'"
				+ "		xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
				+ "		xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
				+ "	>"
				+ "		<tr>"
				+ "			<td class='content'>"
				+ "				<p:field name='" + FIELD_NAME_ADD_ASSERT + "' style='input' />"
				+ "			</td>"
				+ "			<td class='label'>"
				+ "				<p:field name='" + CONTENT_GROUP + '.' + FIELD_EXECUTABLE + "' style='label' />"
				+ "			</td>"
				+ "			<td class='content'>"
				+ "				<p:field name='" + CONTENT_GROUP + '.' + FIELD_EXECUTABLE + "' style='input' />"
				+ "			</td>"
				+ "		</tr>"
				+ "		<tr>"
				+ "			<td class='content'>"
				+ "			</td>"
				+ "			<td class='label'>"
				+ "				<p:field name='" + CONTENT_GROUP + '.' + FIELD_REASON + "' style='label' />"
				+ "			</td>"
				+ "			<td class='content'>"
				+ "				<p:field name='" + CONTENT_GROUP + '.' + FIELD_REASON + "' style='input' />"
				+ "			</td>"
				+ "		</tr>"
				+ "		<tr>"
				+ "			<td class='content'>"
				+ "			</td>"
				+ "			<td class='label'>"
				+ "				<p:field name='" + CONTENT_GROUP + '.' + FIELD_NAME_COMMENT + "' style='label' />"
				+ "			</td>"
				+ "			<td class='content'>"
				+ "				<p:field name='" + CONTENT_GROUP + '.' + FIELD_NAME_COMMENT + "' style='input' />"
				+ "			</td>"
				+ "		</tr>"
				+ "	</tbody>");

		BooleanField _executableField;

		StringField _nonExecutableReasonField;

		public CommandExecutabilityAssertionPlugin(ButtonControl model, boolean assertByDefault) {
			super(model, assertByDefault, "commandExecutability");
		}

		@Override
		protected void initAssertionContents(FormContainer group) {
			_executableField = FormFactory.newBooleanField(FIELD_EXECUTABLE);
			_nonExecutableReasonField = FormFactory.newStringField(FIELD_REASON);
			ValueListener dependency = new ValueListener() {
				@Override
				public void valueChanged(FormField field, Object oldValue, Object newValue) {
					_nonExecutableReasonField.setDisabled(_executableField.getAsBoolean());
				}
			};
			_executableField.addValueListener(dependency);
			_executableField.setAsBoolean(commandModel().isExecutable());
			ResKey notExecutableReasonKey = commandModel().getNotExecutableReasonKey();
			if (notExecutableReasonKey != null) {
				_nonExecutableReasonField.setAsString(notExecutableReasonKey.direct().plain().toString());
			}

			group.addMember(_executableField);
			group.addMember(_nonExecutableReasonField);
		}

		@Override
		protected Document getFormTemplateDocument() {
			return TEMPLATE;
		}

		@Override
		protected List<? extends GuiAssertion> buildAssertions() {
			CommandModel model = commandModel();
			GuiAssertion result = null;
			if (model instanceof ComponentCommandModel) {
				result = newDefaultCommandModelAssertion((ComponentCommandModel) model);
			} else {
				ModelName labeledButtonName = ButtonInspector.labeledButtonName(getModel());
				if (labeledButtonName == null) {
					return Collections.emptyList();
				}
				result = newCommandModelAssertion(labeledButtonName);
			}
			if (!StringServices.isEmpty(getComment())) {
				result.setComment(getComment());
			}
			return toList(result);
		}

		private GuiAssertion newCommandModelAssertion(ModelName commandName) {
			CheckLabeledExecutability result = TypedConfiguration.newConfigItem(CheckLabeledExecutability.class);
			result.setModelName(commandName);
			result.setExecutable(_executableField.getAsBoolean());
			if (!_executableField.getAsBoolean()) {
				result.setReasonKey(_nonExecutableReasonField.getAsString());
			}
			return result;
		}

		private CommandModel commandModel() {
			return getModel().getModel();
		}

		private GuiAssertion newDefaultCommandModelAssertion(ComponentCommandModel command) {
			CheckExecutability result = TypedConfiguration.newConfigItem(CheckExecutability.class);
			result.setImplementationClass(CheckExecutabilityOp.class);
			result.setArguments(ReferenceFactory.attributeValues(command.getArguments()));
			CommandHandler commandHandler = command.getCommandHandler();
			result.setCommandImplementationComment(commandHandler.getClass().getName());
			result.setCommandLabel(command.getLabel());
			String id = commandHandler.getID();
			if (!UUIDInitializer.ID_PATTERN.matcher(id).matches()) {
				result.setCommandName(id);
			}
			result.setComponentImplementationComment(command.getComponent().getClass().getName());
			result.setComponentName(command.getComponent().getName());
			if (!_executableField.getAsBoolean()) {
				result.setReasonKey(_nonExecutableReasonField.getAsString());
			}
			return result;
		}

		@Override
		protected ResPrefix getI18nPrefix() {
			return I18NConstants.COMMAND_EXECUTABILITY;
		}
	}

	/**
	 * Singleton {@link ButtonInspector} instance.
	 */
	public static final ButtonInspector INSTANCE = new ButtonInspector();

	private ButtonInspector() {
		// Singleton constructor.
	}

	@Override
	protected ButtonControl findInspectedGuiElement(ButtonControl control, Map<String, Object> arguments)
			throws AssertionError {
		return control;
	}

	@Override
	protected void buildInspector(InspectorModel inspector, ButtonControl button) {
		ModelName modelName = labeledButtonName(button);
		CommandModel model = button.getModel();
		while (true) {
			if (model instanceof ComponentCommandModel) {
				inspector.add(new CommandExecutabilityAssertionPlugin(button, true));
				ComponentCommandModel command = ((ComponentCommandModel) model);
				LayoutComponent component = command.getComponent();

				addLabelAssertion(inspector, model, modelName);
				addTooltipAssertions(inspector, model, modelName);

				CommandHandler handler = command.getCommandHandler();
				inspector.add(new StaticInfoPlugin(handler.getID(), I18NConstants.COMMAND_ID, "id"));
				inspector.add(new StaticInfoPlugin(toString(handler.getResourceKey(component)),
					I18NConstants.RESOURCE_KEY,
					"resourceKey"));
				inspector
					.add(new StaticInfoPlugin(handler.getCommandGroup(), I18NConstants.COMMAND_GROUP, "commandGroup"));
				inspector.add(new StaticInfoPlugin(handler.getClique(), I18NConstants.COMMAND_CLIQUE, "commandClique"));
				addDisplayProperties(inspector, model);
				inspector.add(new StaticInfoPlugin(handler.getClass().getName(), I18NConstants.COMMAND_IMPLEMENTATION,
					"commandImplementation", handler));

				GuiInspectorPluginFactory.createComponentInformation(inspector, component);
				break;
			}
			
			if (model instanceof CommandModelAdapter) {
				model = ((CommandModelAdapter) model).unwrap();
				continue;
			}

			if (modelName != null) {
				// Button is identified using the model name.
				inspector.add(new CommandExecutabilityAssertionPlugin(button, true));
			}
			addTooltipAssertions(inspector, model, modelName);
			addDisplayProperties(inspector, model);
			addNotExecutableReasonKey(inspector, model);
			inspector.add(new StaticInfoPlugin(getImplementation(model),
				I18NConstants.COMMAND_IMPLEMENTATION, "commandImplementation", model));

			if (model instanceof FormMember) {
				GuiInspectorPluginFactory.createFormMemberAssertions(inspector, (FormMember) model);
			}
			break;
		}
	}

	private String getImplementation(Command model) {
		if (model instanceof WrappedCommandModel) {
			return model.getClass().getSimpleName() + "(" + getImplementation(((WrappedCommandModel) model).unwrap())
				+ ")";
		}
		return model.getClass().getName();
	}

	private void addTooltipAssertions(InspectorModel inspector, CommandModel model, ModelName name) {
		if (name == null) {
			return;
		}
		inspector.add(new ButtonTooltipAssertionPlugin(model, name, true));
		inspector.add(new ButtonTooltipCaptionAssertionPlugin(model, name, true));
		inspector.add(new ButtonTooltipElseLabelAssertionPlugin(model, name, true));
		inspector.add(new ButtonVisibilityAssertionPlugin(model, name, false));
	}

	private void addLabelAssertion(InspectorModel inspector, CommandModel model, ModelName name) {
		if (name == null) {
			return;
		}
		inspector.add(new ButtonLabelAssertionPlugin(model, name, true));
	}

	static ModelName labeledButtonName(ButtonControl button) {
		String commandLabel = button.getLabel();
		if (StringServices.isEmpty(commandLabel)) {
			// Can not find button with empty label
			return null;
		}
		FrameScope frameScope = button.getFrameScope();
		if (frameScope instanceof TopLevelComponentScope) {
			return ReferenceInstantiator.labeledButtonName(commandLabel, null,
				button.getModel().get(LabeledButtonNaming.BUSINESS_OBJECT));
		} else if (frameScope instanceof LayoutComponentScope) {
			LayoutComponent component = ((LayoutComponentScope) frameScope).getComponent();
			return ReferenceInstantiator.labeledButtonName(commandLabel, component,
				button.getModel().get(LabeledButtonNaming.BUSINESS_OBJECT));
		} else {
			// Button can not be found during execution, because frame scope can not be identified.
			return null;
		}
	}

	private void addDisplayProperties(InspectorModel inspector, CommandModel model) {
		addAltText(inspector, model);
		addImages(inspector, model);
		addCssClasses(inspector, model);
		addCheckScope(inspector, model);
	}

	private void addAltText(InspectorModel inspector, CommandModel model) {
		inspector.add(new StaticInfoPlugin(model.getAltText(), I18NConstants.COMMAND_ALT, "altText"));
	}

	private void addImages(InspectorModel inspector, CommandModel model) {
		inspector.add(new StaticInfoPlugin(model.getImage(), I18NConstants.COMMAND_IMAGE, "image"));
		inspector.add(new StaticInfoPlugin(model.getNotExecutableImage(), I18NConstants.COMMAND_DISABLED_IMAGE,
			"disabledImage"));
	}

	private void addNotExecutableReasonKey(InspectorModel inspector, CommandModel model) {
		inspector.add(new StaticInfoPlugin(toString(model.getNotExecutableReasonKey()),
			I18NConstants.NON_EXECUTABLE_REASON_KEY, "nonExecutableReasonKey"));
	}

	private String toString(ResKey key) {
		return key == null ? null : key.debug(Resources.getInstance());
	}

	private void addCssClasses(InspectorModel inspector, CommandModel model) {
		inspector.add(new StaticInfoPlugin(model.getCssClasses(), I18NConstants.COMMAND_CSS, "cssClasses"));
	}

	private void addCheckScope(InspectorModel inspector, CommandModel model) {
		inspector.add(new StaticInfoPlugin(model.getCheckScope(), I18NConstants.COMMAND_CHECK_SCOPE, "checkScope"));
	}

}
