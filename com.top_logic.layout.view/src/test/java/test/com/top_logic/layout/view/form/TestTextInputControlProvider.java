/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.InputType;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.TextInputControlProvider;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Tests for {@link TextInputControlProvider} - the kind of value the text fields it builds edit,
 * and the model types the shipped configuration gives such a kind.
 */
public class TestTextInputControlProvider extends TestCase {

	/** Label of the built field, which decides no input type. */
	private static final String LABEL = "Value";

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;

		super.tearDown();
	}

	/** A configuration stating no kind of value edits a plain text, too. */
	public void testDefaultConfigurationIsPlainText() {
		assertEquals(InputType.TEXT, inputTypeOf(provider(null)));
	}

	/** The configured kind of value reaches the field. */
	public void testConfiguredInputTypeReachesTheField() {
		for (InputType inputType : InputType.values()) {
			assertEquals("A field configured for " + inputType + " must edit that kind of value.",
				inputType, inputTypeOf(provider(inputType)));
		}
	}

	/**
	 * The datatypes holding an address are edited as an input of that kind, so that the value is
	 * offered as a link opening it.
	 */
	public void testAddressTypesAreEditedAsAddresses() {
		assertConfiguredType("tl.util:URL", InputType.URL);
		assertConfiguredType("tl.util:EMail", InputType.EMAIL);
		assertConfiguredType("tl.util:Phone", InputType.TEL);
	}

	/**
	 * Checks that the application configuration edits values of the given model type with a text
	 * field of the given kind.
	 */
	private void assertConfiguredType(String qualifiedName, InputType expected) {
		Map<TLModelPartRef, FieldControlService.ProviderMapping> providers = serviceConfig().getProviders();
		FieldControlService.ProviderMapping mapping = providers.get(TLModelPartRef.ref(qualifiedName));
		assertNotNull("No control is configured for '" + qualifiedName + "'.", mapping);

		PolymorphicConfiguration<? extends ReactFieldControlProvider> impl = mapping.getImpl();
		assertTrue("A value of '" + qualifiedName + "' must be edited in a text field, but is edited by " + impl,
			impl instanceof TextInputControlProvider.Config);
		assertEquals(expected, ((TextInputControlProvider.Config) impl).getInputType());
	}

	/** The configuration of the service mapping model types to the controls editing their values. */
	private static FieldControlService.Config serviceConfig() {
		try {
			return (FieldControlService.Config) ApplicationConfig.getInstance()
				.getServiceConfiguration(FieldControlService.class);
		} catch (Exception ex) {
			throw new AssertionError("Cannot read the configuration of the field control service.", ex);
		}
	}

	/** A provider configured for the given kind of value, or for none if {@code null} is given. */
	private static TextInputControlProvider provider(InputType inputType) {
		TextInputControlProvider.Config config =
			TypedConfiguration.newConfigItem(TextInputControlProvider.Config.class);
		if (inputType != null) {
			config.update(config.descriptor().getProperty(TextInputControlProvider.Config.INPUT_TYPE), inputType);
		}
		return (TextInputControlProvider) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(config);
	}

	/** The kind of value the field built by the given provider edits. */
	private InputType inputTypeOf(TextInputControlProvider provider) {
		FieldSpec field = FieldSpec.of(String.class, LABEL);
		ReactControl control = provider.createControl(_context, field, new AbstractFieldModel(null));

		assertTrue("A text is entered in a text input, but is entered in " + control.getClass(),
			control instanceof ReactTextInputControl);
		return ((ReactTextInputControl) control).getInputType();
	}

	/** Suite loading the application configuration the type-to-control mapping is read from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestTextInputControlProvider.class);
	}

}
