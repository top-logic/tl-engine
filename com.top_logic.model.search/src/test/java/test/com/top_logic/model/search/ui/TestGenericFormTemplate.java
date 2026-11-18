/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.ui;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.providers.CommandHandlerByExpression;
import com.top_logic.model.search.providers.GenericFormBuilderByExpression;
import com.top_logic.model.search.providers.TransactionHandlerByExpression;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;

/**
 * Test instantiating the layout template {@value #TEMPLATE} with and without a default action.
 */
public class TestGenericFormTemplate extends BasicTestCase {

	/** The template under test. */
	private static final String TEMPLATE = "com.top_logic.model.search/genericForm.template.xml";

	/** Name of the template property holding the title of the form. */
	private static final String NAME_PROPERTY = "name";

	/** Name of the template property holding the builder of the form model. */
	private static final String MODEL_BUILDER_PROPERTY = "modelBuilder";

	/** Name of the template property holding the default action of the form. */
	private static final String DEFAULT_ACTION_PROPERTY = "defaultAction";

	/** Layout key the template is instantiated for. */
	private static final String LAYOUT_KEY = "test/genericForm.layout.xml";

	/** Title of the form. */
	private static final ResKey FORM_TITLE = ResKey.text("Form");

	/** Function creating the form model. */
	private static final String FORM_CREATION = "model -> $model";

	/** Operation of the default action chosen in the layout editor. */
	private static final String OPERATION = "null";

	/** Command ID of the default action. */
	private static final String ACTION_ID = "testDefaultAction";

	/**
	 * A configured default action is the first button and the default action of the form.
	 */
	public void testWithDefaultAction() throws ConfigurationException {
		DynamicComponentDefinition definition = definition();
		ConfigurationItem arguments = arguments(definition);
		arguments.update(definition.descriptor().getProperty(DEFAULT_ACTION_PROPERTY),
			AbstractCommandHandler.createConfig(CommandHandlerByExpression.class, ACTION_ID));

		LayoutComponent.Config form = instantiate(definition, arguments);

		CommandHandler.ConfigBase<? extends CommandHandler> defaultAction = form.getDefaultAction();
		assertNotNull("The default action must be passed to the form.", defaultAction);
		assertEquals(ACTION_ID, id(defaultAction));

		List<CommandHandler.ConfigBase<? extends CommandHandler>> buttons = form.getButtons();
		assertFalse("The default action must be displayed as button.", buttons.isEmpty());
		assertEquals("The default action must be the first button.", ACTION_ID, id(buttons.get(0)));

		LayoutComponent.Config plainForm = instantiate(definition, arguments(definition));
		assertEquals("The default action is the only additional button.", plainForm.getButtons().size() + 1,
			buttons.size());
	}

	/**
	 * A default action chosen in the layout editor is offered as implementation class filtered by
	 * the acceptable classifiers, and the form instantiated from it uses that implementation.
	 */
	public void testDefaultActionFromEditor() throws ConfigurationException {
		DynamicComponentDefinition definition = definition();
		ConfigurationItem arguments = arguments(definition);
		PropertyDescriptor property = definition.descriptor().getProperty(DEFAULT_ACTION_PROPERTY);

		DerivedProperty<? extends Iterable<?>> optionProvider = optionProvider(property);
		List<Object> options = new ArrayList<>();
		for (Object option : optionProvider.get(arguments)) {
			options.add(option);
		}
		assertTrue("The editor must offer the transaction handler as option: " + options,
			options.contains(TransactionHandlerByExpression.class));
		for (Object option : options) {
			assertTrue("The editor must offer implementation classes only: " + option, option instanceof Class);
			Class<?> implementation = (Class<?>) option;
			assertFalse("The editor must not offer abstract types: " + implementation,
				Modifier.isAbstract(implementation.getModifiers()));
			assertTrue("The editor must offer command handlers only: " + implementation,
				CommandHandler.class.isAssignableFrom(implementation));
		}

		CommandHandler.Config selection = (CommandHandler.Config) Fields.optionMapping(optionProvider)
			.toSelection(TransactionHandlerByExpression.class);
		selection.update(selection.descriptor().getProperty(CommandHandler.Config.ID_PROPERTY), ACTION_ID);
		setScript(selection, TransactionHandlerByExpression.Config.OPERATION, OPERATION);
		arguments.update(property, selection);

		LayoutComponent.Config form = instantiate(definition, arguments);

		assertSame(TransactionHandlerByExpression.class, form.getDefaultAction().getImplementationClass());
		assertEquals(ACTION_ID, id(form.getDefaultAction()));
		CommandHandler.ConfigBase<? extends CommandHandler> firstButton = form.getButtons().get(0);
		assertSame(TransactionHandlerByExpression.class, firstButton.getImplementationClass());
		assertEquals(ACTION_ID, id(firstButton));
	}

	/**
	 * Without a default action, the form has neither a default action nor an additional button.
	 */
	public void testWithoutDefaultAction() throws ConfigurationException {
		DynamicComponentDefinition definition = definition();

		LayoutComponent.Config form = instantiate(definition, arguments(definition));

		assertNull("No default action without configuration.", form.getDefaultAction());
		for (CommandHandler.ConfigBase<? extends CommandHandler> button : form.getButtons()) {
			assertNotNull("No empty button without default action.", button);
			if (button instanceof CommandHandler.Config handler) {
				assertNotNull("No empty button without default action.", handler.getId());
			}
		}
	}

	private static DerivedProperty<? extends Iterable<?>> optionProvider(PropertyDescriptor property) {
		EditorFactory.Config config = ApplicationConfig.getInstance().getConfig(EditorFactory.Config.class);
		EditorFactory editorFactory = new EditorFactory(config, new InitializerIndex());
		DerivedProperty<? extends Iterable<?>> result = Fields.optionProvider(editorFactory.formOptions(property));
		assertNotNull("No options for '" + property.getPropertyName() + "'.", result);
		return result;
	}

	private static String id(CommandHandler.ConfigBase<? extends CommandHandler> handler) {
		assertTrue("Not a command handler configuration: " + handler, handler instanceof CommandHandler.Config);
		return ((CommandHandler.Config) handler).getId();
	}

	private static DynamicComponentDefinition definition() {
		return LayoutTemplateUtils.getTemplateComponentDefinition(TEMPLATE)
			.orElseThrow(() -> new AssertionError("Template not found: " + TEMPLATE));
	}

	private static ConfigurationItem arguments(DynamicComponentDefinition definition)
			throws ConfigurationException {
		ConfigurationDescriptor descriptor = definition.descriptor();
		ConfigurationItem arguments = descriptor.factory().createNew();
		arguments.update(descriptor.getProperty(NAME_PROPERTY), FORM_TITLE);
		arguments.update(descriptor.getProperty(MODEL_BUILDER_PROPERTY), modelBuilder());
		return arguments;
	}

	private static PolymorphicConfiguration<?> modelBuilder() throws ConfigurationException {
		PolymorphicConfiguration<?> builder =
			TypedConfiguration.createConfigItemForImplementationClass(GenericFormBuilderByExpression.class);
		setScript(builder, GenericFormBuilderByExpression.UIOptions.FORM_CREATION, FORM_CREATION);
		return builder;
	}

	private static void setScript(ConfigurationItem config, String propertyName, String script)
			throws ConfigurationException {
		PropertyDescriptor property = config.descriptor().getProperty(propertyName);
		config.update(property, property.getValueProvider().getValue(propertyName, script));
	}

	private static LayoutComponent.Config instantiate(DynamicComponentDefinition definition,
			ConfigurationItem arguments) throws ConfigurationException {
		LayoutComponent.Config result =
			LayoutTemplateUtils.getInstantiatedLayoutTemplate(definition.definitionFile(), arguments, LAYOUT_KEY);
		assertNotNull("Template could not be instantiated.", result);
		return result;
	}

	/**
	 * Test suite requiring the services layout templates are instantiated with and the services the
	 * layout editor computes the options of a command property with.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(TestGenericFormTemplate.class,
			ThemeFactory.Module.INSTANCE,
			DynamicComponentService.Module.INSTANCE,
			SafeHTML.Module.INSTANCE,
			CommandGroupRegistry.Module.INSTANCE,
			CommandHandlerFactory.Module.INSTANCE);
		return KBSetup.getSingleKBTest(test);
	}

}
