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
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.select.EnterTextArguments;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
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
import com.top_logic.layout.view.form.SelectControlProvider.FreeEntryConfig;
import com.top_logic.layout.view.form.TLFreeEntry;
import com.top_logic.layout.view.form.TextInputControlProvider;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests a select field accepting free entry, by the {@link TLFreeEntry} annotation of the edited
 * attribute or by the {@link SelectControlProvider.Config#getFreeEntry() setting of the field}: a
 * text the user enters chooses the option of that label or becomes a value of its own.
 */
@SuppressWarnings("javadoc")
public class TestSelectFreeEntry extends AbstractModelAccessTest {

	/** Name of the {@link #PROJECT} attribute holding several texts stored as one comma separated text. */
	private static final String KEYWORDS = "keywords";

	/** The options of the field without attribute. */
	private static final List<String> COLORS = List.of("red", "green", "blue");

	/** Name of the {@link #PROJECT} text attribute annotated with {@link TLFreeEntry}. */
	private static final String PLACE = "place";

	/** Name of the {@link #PROJECT} whole number attribute annotated with {@link TLFreeEntry}. */
	private static final String COUNT = "count";

	/**
	 * Name of the {@link #PROJECT} reference to a {@link #CATEGORY} annotated with
	 * {@link TLFreeEntry} creating a category named by the entered text.
	 */
	private static final String TOPIC = "topic";

	/** A select with the free entry the edited attribute says. */
	private static final String SELECT = "<input-control/>";

	/** A select accepting the entered text as its value. */
	private static final String FREE_TEXT =
		"<input-control><" + SelectControlProvider.Config.FREE_ENTRY + "/></input-control>";

	/** A select accepting no free entry, whatever the edited attribute says. */
	private static final String NO_FREE_ENTRY = "<input-control><" + SelectControlProvider.Config.FREE_ENTRY + " "
		+ FreeEntryConfig.ENABLED + "='false'/></input-control>";

	/** A select creating a new category named by the entered text. */
	private static final String CREATE_CATEGORY = createCategory("$" + FreeEntryConfig.TEXT_VARIABLE);

	private static String createCategory(String nameExpr) {
		return "<input-control><" + SelectControlProvider.Config.FREE_ENTRY + " " + FreeEntryConfig.CREATE + "='new(`"
			+ qualified(CATEGORY) + "`, transient: true)..set(`" + qualified(CATEGORY) + "#" + NAME + "`, "
			+ nameExpr + ")'/></input-control>";
	}

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
		assertEquals("purple", single(control));

		String purpleId = offeredId(control, "purple");
		assertNotNull("The entered value is offered while the field holds it.", purpleId);

		control.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
			Map.of(FieldValueArguments.VALUE, List.of(purpleId)));
		assertEquals("purple", single(control));

		assertSuccess(enter(control, "red"));
		assertEquals("red", single(control));
		assertNull("A replaced entered value is offered no longer.", offeredId(control, "purple"));
	}

	/** The option id of the option with the given label after loading the options, or null. */
	private static String offeredId(ReactDropdownSelectControl control, String label) {
		control.executeCommand("loadOptions", Map.of());
		for (Object option : (List<?>) control.scriptingScalarState().get(DropdownSelectState.OPTIONS__PROP)) {
			Map<?, ?> descriptor = (Map<?, ?>) option;
			if (label.equals(descriptor.get(DropdownSelectState.Option.LABEL__PROP))) {
				return (String) descriptor.get(DropdownSelectState.Option.VALUE__PROP);
			}
		}
		return null;
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
		Object recreated = value(control);
		assertNotSame("An object taken out of the field again is not chosen by its label.", keyword, recreated);
		assertEquals("FRESH", ((TLObject) recreated).tValueByName(NAME));

		_form.executeSave();
		TLObject stored = (TLObject) _project.tValueByName(CATEGORY_REF);
		try {
			assertFalse(stored.tTransient());
			assertNotSame(_category, stored);
			assertEquals("FRESH", stored.tValueByName(NAME));
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
			&& m.contains(FreeEntryConfig.CREATE)));
	}

	/**
	 * The text form of the free entry setting of a field is a nested element, read into the
	 * setting.
	 */
	public void testFreeEntryConfigParses() throws Exception {
		SelectControlProvider.Config config = readConfig(NO_FREE_ENTRY);
		assertNotNull(config.getFreeEntry());
		assertFalse(config.getFreeEntry().isEnabled());

		FreeEntryConfig create = readConfig(CREATE_CATEGORY).getFreeEntry();
		assertTrue(create.isEnabled());
		assertNotNull(create.getCreate());

		assertNull(readConfig(SELECT).getFreeEntry());
	}

	/** The annotation of a text attribute lets a field without a setting of its own take a text. */
	public void testAnnotationOnText() throws Exception {
		becomeUser(_root);
		createField(PLACE);
		assertTrue(_form.enterEditMode());
		ReactDropdownSelectControl control = input();
		assertTrue(control.hasFreeEntry());

		assertSuccess(enter(control, " Berlin "));
		assertEquals("Berlin", single(control));
	}

	/** The function of the annotation makes the value of a reference from the entered text. */
	public void testAnnotationCreatesObject() throws Exception {
		becomeUser(_root);
		createField(TOPIC);
		assertTrue(_form.enterEditMode());
		ReactDropdownSelectControl control = input();
		assertTrue(control.hasFreeEntry());

		assertSuccess(enter(control, "Fresh"));
		TLObject created = (TLObject) value(control);
		assertTrue("The new object is kept in the form until it is stored.", created.tTransient());
		assertEquals("Fresh", created.tValueByName(NAME));

		assertSuccess(enter(control, "Category"));
		assertSame("A text matching an option chooses the option.", _category, value(control));
	}

	/**
	 * An attribute taking free entry without options of its own offers the values it holds.
	 */
	public void testAnnotatedTextOffersItsValues() throws Exception {
		becomeUser(_root);
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(PLACE, "Hamburg");
			tx.commit();
		}
		createField(PLACE);
		assertTrue(_form.enterEditMode());
		ReactDropdownSelectControl control = input();

		assertNotNull("The value of the attribute is offered.", offeredId(control, "Hamburg"));
	}

	/** A control the field names wins over the select the annotation asks for. */
	public void testFieldControlOverridesAnnotation() throws Exception {
		becomeUser(_root);
		createField(PLACE, readControlConfig(
			"<input-control class='" + TextInputControlProvider.class.getName() + "'/>"));
		assertTrue(_form.enterEditMode());
		ReactControl control = _field.getInnerControl();
		assertFalse("A text input edits the field, but is: " + control, control instanceof ReactDropdownSelectControl);
	}

	/**
	 * A field switching free entry on without a script of its own makes the value by the function
	 * of the annotation.
	 */
	public void testFieldKeepsAnnotationFunction() throws Exception {
		becomeUser(_root);
		createField(TOPIC, FREE_TEXT);
		assertTrue(_form.enterEditMode());
		ReactDropdownSelectControl control = input();

		assertSuccess(enter(control, "Fresh"));
		TLObject created = (TLObject) value(control);
		assertTrue(created.tTransient());
		assertEquals("Fresh", created.tValueByName(NAME));
	}

	/** A field switches off the free entry the annotation of the edited attribute grants. */
	public void testFieldSwitchesOffAnnotation() throws Exception {
		becomeUser(_root);
		createField(PLACE, NO_FREE_ENTRY);
		assertTrue(_form.enterEditMode());
		ReactDropdownSelectControl control = input();
		assertFalse(control.hasFreeEntry());

		assertRefused(com.top_logic.layout.react.I18NConstants.ERROR_NO_FREE_ENTRY, enter(control, "Berlin"));
	}

	/** The script of a field makes the value instead of the function of the annotation. */
	public void testFieldCreateOverridesAnnotation() throws Exception {
		becomeUser(_root);
		createField(TOPIC, createCategory("\"by field\""));
		assertTrue(_form.enterEditMode());
		ReactDropdownSelectControl control = input();

		assertSuccess(enter(control, "Fresh"));
		assertEquals("by field", ((TLObject) value(control)).tValueByName(NAME));
	}

	/**
	 * A whole number attribute reads the entered text as number, and refuses a text that is no
	 * whole number.
	 */
	public void testNumberFromText() throws Exception {
		becomeUser(_root);
		createField(COUNT);
		assertTrue(_form.enterEditMode());
		ReactDropdownSelectControl control = input();
		assertTrue(control.hasFreeEntry());

		assertSuccess(enter(control, "42"));
		assertEquals(Long.valueOf(42), single(control));

		assertRefused(com.top_logic.layout.react.I18NConstants.ERROR_INVALID_NUMBER__VALUE, enter(control, "abc"));
		assertEquals(Long.valueOf(42), single(control));
	}

	/**
	 * A number read from the entered text that equals an option chooses that option, whatever the
	 * label of the option looks like.
	 */
	public void testNumberMatchingOptionChoosesIt() throws Exception {
		ReactContext context =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		SelectControlProvider provider = (SelectControlProvider) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(readConfig(FREE_TEXT));
		SimpleSelectFieldModel model = new SimpleSelectFieldModel(null, List.of(1000L, 2000L), false);
		ReactDropdownSelectControl control = (ReactDropdownSelectControl) provider.createControl(context,
			FieldSpec.of(Long.class, "Amount"), model);

		assertSuccess(enter(control, "2000"));
		assertEquals(Long.valueOf(2000), single(control));
		control.executeCommand("loadOptions", Map.of());
		assertEquals("The option is offered once.", 2,
			((List<?>) control.scriptingScalarState().get(DropdownSelectState.OPTIONS__PROP)).size());
	}

	/**
	 * The annotation fits an attribute whose values are read from a text, and any attribute when it
	 * has a function making the value.
	 */
	public void testAnnotationChecksTarget() {
		TLFreeEntry plain = part(PROJECT, PLACE).getAnnotation(TLFreeEntry.class);
		assertNull(plain.checkTarget(part(PROJECT, PLACE)));
		assertNull(plain.checkTarget(part(PROJECT, COUNT)));
		assertNull(plain.checkTarget(part(PROJECT, KEYWORDS)));
		assertNotNull(plain.checkTarget(part(PROJECT, CATEGORY_REF)));
		assertNotNull(plain.checkTarget(part(PROJECT, APPROVED)));

		TLFreeEntry create = part(PROJECT, TOPIC).getAnnotation(TLFreeEntry.class);
		assertNull(create.checkTarget(part(PROJECT, CATEGORY_REF)));
	}

	/**
	 * Loading a model rejects the annotation without function on a reference, and keeps it with a
	 * function.
	 */
	public void testModelRejectsAnnotation() {
		BufferingProtocol log = new BufferingProtocol();
		TLModel model = new TLModelImpl();
		try {
			DynamicModelService.extendModel(log, model, TransientObjectFactory.INSTANCE,
				ClassRelativeBinaryContent.withSuffix(TestSelectFreeEntry.class, "rejected.model.xml"));
		} catch (RuntimeException ex) {
			// The protocol aborts on the error reported.
		}
		assertTrue(log.getErrors().toString(), log.getErrors().stream()
			.anyMatch(m -> m.contains("testFreeEntryRejected:X#withoutCreate is not allowed")
				&& m.contains(TLFreeEntry.CREATE)));
		assertFalse(log.getErrors().toString(),
			log.getErrors().stream().anyMatch(m -> m.contains("withCreate is not allowed")));
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
		assertEquals("The message is the title of the report.", expected.getKey(), result.getErrorTitle().getKey());
	}

	private static void assertRefused(ResKeyTemplate expected, HandlerResult result) {
		assertFalse(result.isSuccess());
		assertEquals("The message is the title of the report.", expected.getKey(), result.getErrorTitle().getKey());
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

	/** A field of the given attribute without a control of its own. */
	private void createField(String attribute) throws Exception {
		createField(attribute, (PolymorphicConfiguration<? extends ReactFieldControlProvider>) null);
	}

	private void createField(String attribute, String configXml) throws Exception {
		createField(attribute, readConfig(configXml));
	}

	private void createField(String attribute, PolymorphicConfiguration<? extends ReactFieldControlProvider> control)
			throws Exception {
		_form = new FormControl(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")), _project, "no model", NoTokenHandling.INSTANCE);
		ViewChannel input = new DefaultViewChannel("input");
		input.set(_project);
		_form.setInputChannel(input);
		_field = new AttributeFieldControl(_form.getReactContext(), _form, _form, attribute, null, false, null, null,
			control);
		_field.createChromeControl();
		_form.attach();
	}

	private ReactDropdownSelectControl input() {
		ReactControl result = _field.getInnerControl();
		assertTrue("A select edits the field, but is: " + result, result instanceof ReactDropdownSelectControl);
		return (ReactDropdownSelectControl) result;
	}

	/** The configuration of the control the given XML declares by its class. */
	@SuppressWarnings("unchecked")
	private static PolymorphicConfiguration<? extends ReactFieldControlProvider> readControlConfig(String xml)
			throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestSelectFreeEntry.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap("input-control",
			TypedConfiguration.getConfigurationDescriptor(PolymorphicConfiguration.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		Object result = reader.read();
		context.checkErrors();
		return (PolymorphicConfiguration<? extends ReactFieldControlProvider>) result;
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
