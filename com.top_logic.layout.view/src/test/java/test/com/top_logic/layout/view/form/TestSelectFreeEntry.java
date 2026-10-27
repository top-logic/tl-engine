/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.select.EnterTextArguments;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.DropdownSelectState;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.SelectControlProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests a select field accepting {@link SelectControlProvider.Config#hasFreeEntry() free entry}:
 * a text the user enters chooses the option of that label or becomes a value of its own.
 */
@SuppressWarnings("javadoc")
public class TestSelectFreeEntry extends AbstractModelAccessTest {

	/** Name of the {@link #PROJECT} attribute holding several texts stored as one comma separated text. */
	private static final String KEYWORDS = "keywords";

	/** The options of the field without attribute. */
	private static final List<String> COLORS = List.of("red", "green", "blue");

	/** A select accepting the entered text as its value. */
	private static final String FREE_TEXT = "<input-control " + SelectControlProvider.Config.FREE_ENTRY + "='true'/>";

	/** A select creating a new category named by the entered text. */
	private static final String CREATE_CATEGORY = "<input-control " + SelectControlProvider.Config.FREE_ENTRY
		+ "='true' " + SelectControlProvider.Config.CREATE + "='new(`" + qualified(CATEGORY)
		+ "`, transient: true)..set(`" + qualified(CATEGORY) + "#" + NAME + "`, $"
		+ SelectControlProvider.Config.TEXT_VARIABLE + ")'/>";

	private FormControl _form;

	private AttributeFieldControl _field;

	@Override
	protected void tearDown() throws Exception {
		_field = null;
		_form = null;
		super.tearDown();
	}

	/** A text no option is labelled with becomes the value of a field holding texts. */
	public void testTextBecomesValue() throws Exception {
		ReactDropdownSelectControl control = colors(FREE_TEXT);
		assertTrue(control.hasFreeEntry());
		assertEquals(Boolean.TRUE, control.scriptingScalarState().get(DropdownSelectState.FREE_ENTRY__PROP));

		assertSuccess(enter(control, "  purple "));
		assertEquals("purple", single(control));
		assertEquals("purple", shownLabels(control).get(0));
	}

	/** A text matching the label of an option, ignoring case, chooses that option. */
	public void testTextMatchingOptionChoosesIt() throws Exception {
		ReactDropdownSelectControl control = colors(FREE_TEXT);

		assertSuccess(enter(control, "GREEN"));
		assertEquals("green", single(control));
	}

	/** An empty text changes nothing. */
	public void testBlankTextChangesNothing() throws Exception {
		ReactDropdownSelectControl control = colors(FREE_TEXT);
		assertSuccess(enter(control, "red"));

		assertSuccess(enter(control, "   "));
		assertEquals("red", single(control));
		assertSuccess(enter(control, null));
		assertEquals("red", single(control));
	}

	/** A field without free entry refuses an entered text and keeps its value. */
	public void testFreeEntryOffRefusesText() throws Exception {
		ReactDropdownSelectControl control = colors("<input-control/>");
		assertFalse(control.hasFreeEntry());
		assertNotSame(Boolean.TRUE, control.scriptingScalarState().get(DropdownSelectState.FREE_ENTRY__PROP));

		HandlerResult result = enter(control, "purple");
		assertRefused(com.top_logic.layout.react.I18NConstants.ERROR_NO_FREE_ENTRY, result);
		assertNull(single(control));
	}

	/**
	 * An entered value is offered like an option, so the client can choose it again by the id it is
	 * offered with.
	 */
	public void testEnteredValueIsOffered() throws Exception {
		ReactDropdownSelectControl control = colors(FREE_TEXT);
		assertSuccess(enter(control, "purple"));
		assertSuccess(enter(control, "red"));
		assertEquals("red", single(control));

		control.executeCommand("loadOptions", Map.of());
		String purpleId = null;
		for (Object option : (List<?>) control.scriptingScalarState().get(DropdownSelectState.OPTIONS__PROP)) {
			Map<?, ?> descriptor = (Map<?, ?>) option;
			if ("purple".equals(descriptor.get(DropdownSelectState.Option.LABEL__PROP))) {
				purpleId = (String) descriptor.get(DropdownSelectState.Option.VALUE__PROP);
			}
		}
		assertNotNull("The entered value is offered.", purpleId);

		control.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
			Map.of(FieldValueArguments.VALUE, List.of(purpleId)));
		assertEquals("purple", single(control));
	}

	/**
	 * The command entering a text names the text, so it is recorded as it was dispatched and
	 * replays in a later session.
	 */
	public void testEnteredTextIsRecordedAsIs() throws Exception {
		ReactDropdownSelectControl control = colors(FREE_TEXT);

		EnterTextArguments recorded = (EnterTextArguments) control
			.recordCommand(EnterTextArguments.COMMAND, Map.of(EnterTextArguments.TEXT, "purple")).command();
		assertEquals(EnterTextArguments.COMMAND, recorded.getName());
		assertEquals("purple", recorded.getText());
		assertTrue(control.isRecordable(EnterTextArguments.COMMAND));
	}

	/**
	 * Entered texts are added to the values of a field holding several texts, each once, and a text
	 * that cannot be stored is refused.
	 */
	public void testTextsAddedToSeveralValues() throws Exception {
		becomeUser(_responsible);
		createField(KEYWORDS, FREE_TEXT);
		assertTrue(_form.enterEditMode());
		ReactDropdownSelectControl control = input();
		assertTrue(control.hasFreeEntry());

		assertSuccess(enter(control, "alpha"));
		assertSuccess(enter(control, "beta"));
		assertEquals(List.of("alpha", "beta"), value(control));
		assertTrue("The form holds a change.", _form.isDirty());

		assertSuccess(enter(control, "ALPHA"));
		assertEquals(List.of("alpha", "beta"), value(control));

		HandlerResult result = enter(control, "gamma, delta");
		assertRefused(I18NConstants.ERROR_ENTRY_CONTAINS_SEPARATOR__TEXT, result);
		assertEquals(List.of("alpha", "beta"), value(control));

		_form.executeSave();
		assertEquals(Set.of("alpha", "beta"), new HashSet<>((Collection<?>) _project.tValueByName(KEYWORDS)));
	}

	/**
	 * A text no option is labelled with creates a new object through the script, once; storing the
	 * form makes the object persistent.
	 */
	public void testTextCreatesObject() throws Exception {
		becomeUser(_root);
		createField(CATEGORY_REF, CREATE_CATEGORY);
		assertTrue(_form.enterEditMode());
		ReactDropdownSelectControl control = input();
		assertTrue(control.hasFreeEntry());

		assertSuccess(enter(control, "Fresh"));
		Object created = value(control);
		assertTrue(created instanceof TLObject);
		TLObject keyword = (TLObject) created;
		assertTrue("The new object is kept in the form until it is stored.", keyword.tTransient());
		assertEquals("Fresh", keyword.tValueByName(NAME));
		assertTrue("The form holds a change.", _form.isDirty());

		assertSuccess(enter(control, "fresh"));
		assertSame("A text entered again chooses the object created before.", keyword, value(control));

		assertSuccess(enter(control, "Category"));
		assertSame("A text matching an option chooses the option.", _category, value(control));

		assertSuccess(enter(control, "FRESH"));
		assertSame("The object created before is still chosen by its label.", keyword, value(control));

		_form.executeSave();
		TLObject stored = (TLObject) _project.tValueByName(CATEGORY_REF);
		try {
			assertFalse(stored.tTransient());
			assertNotSame(_category, stored);
			assertEquals("Fresh", stored.tValueByName(NAME));
		} finally {
			delete(stored);
		}
	}

	/**
	 * A field holding no texts and without a script making its values cannot accept free entry: it
	 * is switched off, and the configuration error is logged.
	 */
	public void testFreeEntryOffWithoutConversion() throws Exception {
		becomeUser(_root);
		CollectingLogListener log = new CollectingLogListener(Set.of(Level.ERROR), true);
		List<LogEntry> errors;
		try {
			createField(CATEGORY_REF, FREE_TEXT);
			assertTrue(_form.enterEditMode());
			input();
		} finally {
			log.deactivate();
			errors = log.getLogEntries();
		}
		ReactDropdownSelectControl control = input();
		assertFalse(control.hasFreeEntry());
		assertNotSame(Boolean.TRUE, control.scriptingScalarState().get(DropdownSelectState.FREE_ENTRY__PROP));

		List<String> messages = new ArrayList<>();
		for (LogEntry entry : errors) {
			messages.add(entry.getMessage());
		}
		assertTrue(messages.toString(), messages.stream().anyMatch(m -> m.contains(CATEGORY_REF)
			&& m.contains(SelectControlProvider.Config.CREATE)));
	}

	private static HandlerResult enter(ReactControl control, String text) {
		return control.executeCommand(EnterTextArguments.COMMAND,
			Collections.singletonMap(EnterTextArguments.TEXT, text));
	}

	/**
	 * The value of a field holding one value, built on a model that holds its selection as list.
	 */
	private static Object single(ReactDropdownSelectControl control) {
		Object value = value(control);
		if (!(value instanceof List<?>)) {
			return value;
		}
		List<?> selection = (List<?>) value;
		assertTrue(selection.toString(), selection.size() <= 1);
		return selection.isEmpty() ? null : selection.get(0);
	}

	private static Object value(ReactDropdownSelectControl control) {
		return control.getFieldModel().getValue();
	}

	@SuppressWarnings("unchecked")
	private static List<Object> shownLabels(ReactDropdownSelectControl control) {
		List<Object> result = new ArrayList<>();
		for (Map<String, Object> descriptor : (List<Map<String, Object>>) control.scriptingScalarState()
			.get(FieldState.VALUE__PROP)) {
			result.add(descriptor.get(DropdownSelectState.Option.LABEL__PROP));
		}
		return result;
	}

	private static void assertSuccess(HandlerResult result) {
		assertTrue(String.valueOf(result.getEncodedErrors()), result.isSuccess());
	}

	private static void assertRefused(ResKey expected, HandlerResult result) {
		assertFalse(result.isSuccess());
		assertEquals(expected.getKey(), result.getEncodedErrors().get(0).getKey());
	}

	private static void assertRefused(ResKeyTemplate expected, HandlerResult result) {
		assertFalse(result.isSuccess());
		assertEquals(expected.getKey(), result.getEncodedErrors().get(0).getKey());
	}

	/** A select picking one of the {@link #COLORS}, built by a provider of the given configuration. */
	private ReactDropdownSelectControl colors(String configXml) throws Exception {
		ReactContext context =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		SelectControlProvider provider = (SelectControlProvider) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(readConfig(configXml));
		SimpleSelectFieldModel model = new SimpleSelectFieldModel(null, COLORS, false);
		return (ReactDropdownSelectControl) provider.createControl(context, FieldSpec.of(String.class, "Color"), model);
	}

	private void createField(String attribute, String configXml) throws Exception {
		_form = new FormControl(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")), _project, "no model", NoTokenHandling.INSTANCE);
		ViewChannel input = new DefaultViewChannel("input");
		input.set(_project);
		_form.setInputChannel(input);
		_field = new AttributeFieldControl(_form.getReactContext(), _form, _form, attribute, null, false, null, null,
			readConfig(configXml));
		_field.createChromeControl();
		_form.attach();
	}

	private ReactDropdownSelectControl input() {
		ReactControl result = _field.getInnerControl();
		assertTrue("A select edits the field, but is: " + result, result instanceof ReactDropdownSelectControl);
		return (ReactDropdownSelectControl) result;
	}

	/** The provider configuration the given XML declares. */
	private static SelectControlProvider.Config readConfig(String xml) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestSelectFreeEntry.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap("input-control",
			TypedConfiguration.getConfigurationDescriptor(SelectControlProvider.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		Object result = reader.read();
		context.checkErrors();
		return (SelectControlProvider.Config) result;
	}

	/**
	 * The test suite, additionally starting the {@link FieldControlService} building the inputs of
	 * the fields and the {@link SearchBuilder} compiling the script creating values.
	 */
	public static Test suite() {
		return suiteWith(TestSelectFreeEntry.class, TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE,
			FieldControlService.Module.INSTANCE);
	}
}
