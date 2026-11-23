/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.wysiwyg;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.utility.MaskedValues;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ForwardingReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.navigation.ObjectNavigator;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.wysiwyg.ReactWysiwygControl;
import com.top_logic.layout.react.wysiwyg.WysiwygControlProvider;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.StructuredTextPDFRenderer;
import com.top_logic.layout.wysiwyg.ui.TLObjectLinkUtil;
import com.top_logic.layout.wysiwyg.ui.excel.StructuredTextExcelHandler;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.DiscoverRight;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.commandhandlers.BookmarkService;
import com.top_logic.tool.boundsec.manager.AccessManager;

/**
 * Tests that an object link in a formatted text follows the rule for a masked element: a reader who
 * may not discover the linked object receives a placeholder, and no label, no link and no identity
 * of the object, from the editor as from the outputs derived from the text. Saving the text keeps
 * each hidden link that the reader did not delete. Following a link to such an object leads
 * nowhere.
 *
 * <p>
 * The test model {@value #MODULE} and its access rights are installed from the configuration next
 * to this class: a topic may be read, and thereby discovered, by its reader only. The
 * {@link #_reader} is reader of {@link #_shown}, but not of {@link #_hidden}.
 * </p>
 *
 * @see ReactWysiwygControl
 */
@SuppressWarnings("javadoc")
public class TestMaskedObjectLinks extends AbstractSearchExpressionTest {

	/** Name of the test model module. */
	private static final String MODULE = "TestMaskedObjectLinks";

	/** Qualified name of the test type. */
	private static final String TOPIC = MODULE + ":Topic";

	/** Name of the reference to the person holding the reader role on a topic. */
	private static final String READER = "reader";

	/** The command the client sends when the user follows an object link. */
	private static final String CMD_SHOW_OBJECT_LINK = "showObjectLink";

	/** The {@link #CMD_SHOW_OBJECT_LINK} argument naming the object to display. */
	private static final String ARG_HREF = "href";

	/** The label of {@link #_hidden}, which the stored text gives the link to it. */
	private static final String HIDDEN_LABEL = "hiddenTopic";

	/** The label of {@link #_shown}, which the stored text gives the link to it. */
	private static final String SHOWN_LABEL = "shownTopic";

	/**
	 * Name of the untyped configuration file next to this class; the typed configuration file
	 * installing the test model and its access rights is derived from it.
	 */
	private static final String CONFIG_FILE = "TestMaskedObjectLinks.xml";

	/** The trigger character a mention stores in the chip it inserts. */
	private static final String MENTION_TRIGGER = "@";

	/** The command the client sends when the text of the editor changed. */
	private static final String CMD_VALUE_CHANGED = ReactFormFieldControl.CMD_VALUE_CHANGED;

	/** The {@link #CMD_VALUE_CHANGED} argument holding the text. */
	private static final String ARG_VALUE = "value";

	/** A link destination naming an object that does not exist. */
	private static final String GONE_HREF = "?id=987654321&branch=1&type=GenericObject";

	/** The label of the link to {@link #GONE_HREF}. */
	private static final String GONE_LABEL = "goneTopic";

	/** A placeholder of a masked object link in the text of the client. */
	private static final Pattern PLACEHOLDER = Pattern.compile("<span data-masked=\"[^\"]*\">[^<]*</span>");

	/** May discover {@link #_shown}, but not {@link #_hidden}. */
	private Person _reader;

	/** A topic {@link #_reader} may discover. */
	private TLObject _shown;

	/** A topic {@link #_reader} may not discover. */
	private TLObject _hidden;

	private Targets _navigator;

	private Messages _messages;

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reader = TestPerson.createPerson("maskedLinksReader");
		_shown = newObject(TOPIC, SHOWN_LABEL);
		_hidden = newObject(TOPIC, HIDDEN_LABEL);
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_shown.tUpdateByName(READER, _reader);
			tx.commit();
		}

		_navigator = new Targets();
		_messages = new Messages();
		_context = new ForwardingReactContext(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"))) {
			@Override
			public ObjectNavigator getObjectNavigator() {
				return _navigator;
			}

			@Override
			public ErrorSink getErrorSink() {
				return _messages;
			}
		};
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_messages = null;
		_navigator = null;
		// Deletes the topics.
		super.tearDown();
		TestPerson.deletePersonAndUser(_reader);
		_reader = null;
		_shown = null;
		_hidden = null;
	}

	@Override
	protected boolean checksSecurity() {
		return true;
	}

	/**
	 * The fixture the other tests rely on: the reader may discover the shown topic, but not the
	 * hidden one.
	 */
	public void testReaderMayDiscoverTheShownTopicOnly() {
		becomeUser(_reader);

		assertTrue(DiscoverRight.isDiscoverable(_shown));
		assertTrue(DiscoverRight.isMasked(_hidden));
	}

	/**
	 * The read-only display of a text sends no label and no identity of a linked object the reader
	 * may not discover, but a placeholder in its place.
	 */
	public void testDisplayMasksUndiscoverableLink() {
		becomeUser(_reader);
		ReactWysiwygControl display = control(field(textWith(link(_hidden, HIDDEN_LABEL)), false));

		assertMasked(display.stateAsJSON(), _hidden, HIDDEN_LABEL);
	}

	/**
	 * The editor of a text sends no label and no identity of a linked object the reader may not
	 * discover, but a placeholder in its place.
	 */
	public void testEditorMasksUndiscoverableLink() {
		becomeUser(_reader);
		ReactWysiwygControl editor = control(field(textWith(link(_hidden, HIDDEN_LABEL)), true));

		assertMasked(editor.stateAsJSON(), _hidden, HIDDEN_LABEL);
	}

	/**
	 * A mention, stored as a chip, of an object the reader may not discover is masked like any
	 * other object link.
	 */
	public void testMentionOfUndiscoverableObjectIsMasked() {
		becomeUser(_reader);
		ReactWysiwygControl editor = control(field(textWith(mention(_hidden, HIDDEN_LABEL)), true));

		assertMasked(editor.stateAsJSON(), _hidden, HIDDEN_LABEL);
	}

	/**
	 * A text that reaches a displayed control later, as an update of the value, is masked as well:
	 * the update sends the state the control holds then.
	 */
	public void testUpdatedTextMasksUndiscoverableLink() {
		becomeUser(_reader);
		AbstractFieldModel field = field(textWith(link(_shown, SHOWN_LABEL)), false);
		ReactWysiwygControl display = control(field);

		field.setValue(textWith(link(_hidden, HIDDEN_LABEL)));

		assertMasked(display.stateAsJSON(), _hidden, HIDDEN_LABEL);
	}

	/**
	 * A linked object the reader may discover is sent as before, with its label and its link.
	 */
	public void testDiscoverableLinkIsShown() {
		becomeUser(_reader);
		ReactWysiwygControl display = control(field(textWith(link(_shown, SHOWN_LABEL)), false));

		String html = (String) display.scriptingScalarState().get(FieldState.VALUE__PROP);
		assertTrue(html, html.contains(">" + SHOWN_LABEL + "<"));
		assertTrue(html, html.contains(idArgument(_shown)));
	}

	/**
	 * Following a link to an object the reader may not discover leads nowhere, and is answered as
	 * for a link to an unknown object: a crafted link must not tell whether the object exists or
	 * where it is displayed.
	 */
	public void testFollowingLinkToUndiscoverableObjectLeadsNowhere() {
		becomeUser(_reader);
		ReactWysiwygControl display = control(field(textWith(link(_hidden, HIDDEN_LABEL)), false));

		display.executeCommand(CMD_SHOW_OBJECT_LINK, Map.of(ARG_HREF, href(_hidden)));

		assertEquals("The view of an object the reader may not discover is not opened.",
			List.of(), _navigator.shown());
		assertEquals(1, _messages.errors().size());
	}

	/**
	 * Following a link to an object the reader may discover displays it.
	 */
	public void testFollowingLinkToDiscoverableObjectShowsIt() {
		becomeUser(_reader);
		ReactWysiwygControl display = control(field(textWith(link(_shown, SHOWN_LABEL)), false));

		display.executeCommand(CMD_SHOW_OBJECT_LINK, Map.of(ARG_HREF, href(_shown)));

		assertEquals(List.of(_shown), _navigator.shown());
		assertEquals(List.of(), _messages.errors());
	}

	/**
	 * A link to an object the reader may not discover gets the same answer as a link to an object
	 * that does not exist.
	 */
	public void testFollowingHiddenAndUnknownLinkGiveTheSameAnswer() throws IOException {
		becomeUser(_reader);
		ReactWysiwygControl display = control(field(textWith(link(_hidden, HIDDEN_LABEL)), false));

		display.executeCommand(CMD_SHOW_OBJECT_LINK, Map.of(ARG_HREF, href(_hidden)));
		display.executeCommand(CMD_SHOW_OBJECT_LINK, Map.of(ARG_HREF, GONE_HREF));

		assertEquals(2, _messages.errors().size());
		assertEquals(text(_messages.errors().get(1)), text(_messages.errors().get(0)));
	}

	/**
	 * A link whose destination names no existing object is masked as well, also for a user who
	 * may discover every object.
	 */
	public void testLinkToNoObjectIsMasked() {
		becomeUser(PersonManager.getManager().getRoot());
		ReactWysiwygControl display = control(field(textWith(anchor(GONE_HREF, GONE_LABEL)), false));

		String html = clientValue(display);
		assertFalse(html, html.contains(GONE_LABEL));
		assertTrue(html, html.contains(MaskedValues.placeholder()));
	}

	/**
	 * A user who may discover every object receives the hidden link as it is stored.
	 */
	public void testLinkShownToUserWhoMayDiscoverIt() {
		becomeUser(PersonManager.getManager().getRoot());
		ReactWysiwygControl display = control(field(textWith(link(_hidden, HIDDEN_LABEL)), false));

		assertTrue(clientValue(display).contains(">" + HIDDEN_LABEL + "<"));
	}

	/**
	 * Saving the text as the client received it, with an edit beside the placeholder, stores the
	 * hidden link unchanged.
	 */
	public void testSaveKeepsMaskedLink() {
		becomeUser(_reader);
		String link = link(_hidden, HIDDEN_LABEL);
		AbstractFieldModel field = field(textWith(link), true);
		ReactWysiwygControl editor = control(field);

		send(editor, clientValue(editor).replace("for details", "for all details"));

		assertEquals("<p>See " + link + " for all details.</p>", storedSource(field));
	}

	/**
	 * Deleting the placeholder deletes the hidden link.
	 */
	public void testDeletedPlaceholderDeletesLink() {
		becomeUser(_reader);
		AbstractFieldModel field = field(textWith(link(_hidden, HIDDEN_LABEL)), true);
		ReactWysiwygControl editor = control(field);

		send(editor, PLACEHOLDER.matcher(clientValue(editor)).replaceAll(""));

		assertEquals("<p>See  for details.</p>", storedSource(field));
	}

	/**
	 * Moving placeholders moves the hidden links they stand for.
	 */
	public void testMovedPlaceholdersMoveTheirLinks() {
		becomeUser(_reader);
		String link = link(_hidden, HIDDEN_LABEL);
		String mention = mention(_hidden, HIDDEN_LABEL);
		AbstractFieldModel field = field(new StructuredText("<p>A " + link + " B " + mention + " C</p>"), true);
		ReactWysiwygControl editor = control(field);

		List<String> placeholders = placeholders(clientValue(editor));
		assertEquals(2, placeholders.size());
		send(editor, "<p>A " + placeholders.get(1) + " B " + placeholders.get(0) + " C</p>");

		assertEquals("<p>A " + mention + " B " + link + " C</p>", storedSource(field));
	}

	/**
	 * Each copy of a placeholder in the same editor becomes a copy of the hidden link.
	 */
	public void testCopiedPlaceholderCopiesLink() {
		becomeUser(_reader);
		String link = link(_hidden, HIDDEN_LABEL);
		AbstractFieldModel field = field(textWith(link), true);
		ReactWysiwygControl editor = control(field);

		String placeholder = placeholders(clientValue(editor)).get(0);
		send(editor, "<p>" + placeholder + " and " + placeholder + "</p>");

		assertEquals("<p>" + link + " and " + link + "</p>", storedSource(field));
	}

	/**
	 * Two equal links of one text get placeholders of their own, so that the client cannot tell
	 * that they name the same object.
	 */
	public void testEqualLinksGetPlaceholdersOfTheirOwn() {
		becomeUser(_reader);
		String link = link(_hidden, HIDDEN_LABEL);
		ReactWysiwygControl editor = control(field(new StructuredText("<p>" + link + " " + link + "</p>"), true));

		List<String> placeholders = placeholders(clientValue(editor));
		assertEquals(2, placeholders.size());
		assertFalse(placeholders.get(0).equals(placeholders.get(1)));
	}

	/**
	 * A placeholder whose token this editor did not hand out brings in no link, neither a made up
	 * one nor one of another editor.
	 */
	public void testUnknownTokenBringsInNoLink() {
		becomeUser(_reader);
		ReactWysiwygControl other = control(field(textWith(link(_hidden, HIDDEN_LABEL)), true));
		String foreign = placeholders(clientValue(other)).get(0);
		AbstractFieldModel field = field(new StructuredText("<p>text</p>"), true);
		ReactWysiwygControl editor = control(field);

		send(editor, "<p>A " + foreign + " B <span data-masked=\"x\">*****</span> C</p>");

		assertEquals("<p>A  B  C</p>", storedSource(field));
	}

	/**
	 * A text the client sends after the value changed on the server still gets its hidden links
	 * back.
	 */
	public void testTokensSurviveAChangeOfTheValue() {
		becomeUser(_reader);
		String link = link(_hidden, HIDDEN_LABEL);
		AbstractFieldModel field = field(textWith(link), true);
		ReactWysiwygControl editor = control(field);
		String received = clientValue(editor);

		field.setValue(new StructuredText("<p>changed on the server</p>"));
		send(editor, received);

		assertEquals(textWith(link).getSourceCode(), storedSource(field));
	}

	/**
	 * The first line standing for the text where the editor has no room masks a hidden link.
	 */
	public void testPreviewMasksUndiscoverableLink() {
		becomeUser(_reader);

		String preview = WysiwygControlProvider.htmlPreview(mixedText());

		assertEquals("See " + SHOWN_LABEL + " and " + MaskedValues.placeholder() + " for details.", preview);
	}

	/**
	 * The label of a text, which table cells, their sort and filter, and exports by label use,
	 * masks a hidden link.
	 */
	public void testLabelMasksUndiscoverableLink() {
		becomeUser(_reader);

		String label = MetaLabelProvider.INSTANCE.getLabel(mixedText());

		assertEquals("See " + SHOWN_LABEL + " and " + MaskedValues.placeholder() + " for details.", label);
	}

	/**
	 * The text of an inherited value, which the editor shows as plain text, masks a hidden link.
	 */
	public void testInheritedValueTextMasksUndiscoverableLink() {
		becomeUser(_reader);
		ReactWysiwygControl editor = control(field(null, true));

		String text = editor.formatValue(mixedText());

		assertTrue(text, text.contains(SHOWN_LABEL));
		assertTrue(text, text.contains(MaskedValues.placeholder()));
		assertFalse(text, text.contains(HIDDEN_LABEL));
	}

	/**
	 * The PDF export of a text masks a hidden link.
	 */
	public void testPdfMasksUndiscoverableLink() throws IOException {
		becomeUser(_reader);
		StringWriter buffer = new StringWriter();
		try (TagWriter out = new TagWriter(buffer)) {
			StructuredTextPDFRenderer.INSTANCE.write(null, out, null, mixedText());
		}

		String pdfSource = buffer.toString();
		assertTrue(pdfSource, pdfSource.contains(SHOWN_LABEL));
		assertTrue(pdfSource, pdfSource.contains(MaskedValues.placeholder()));
		assertFalse(pdfSource, pdfSource.contains(HIDDEN_LABEL));
		assertFalse(pdfSource, pdfSource.contains(idArgument(_hidden)));
	}

	/**
	 * The Excel export of a text masks a hidden link.
	 */
	public void testExcelMasksUndiscoverableLink() throws IOException {
		becomeUser(_reader);
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			Cell cell = workbook.createSheet("Data").createRow(0).createCell(0);

			new StructuredTextExcelHandler().setValue(cell, workbook, mixedText(), null);

			String exported = cell.getRichStringCellValue().getString();
			assertTrue(exported, exported.contains(SHOWN_LABEL));
			assertTrue(exported, exported.contains(MaskedValues.placeholder()));
			assertFalse(exported, exported.contains(HIDDEN_LABEL));
		}
	}

	/**
	 * Asserts that the given client state holds no label and no identity of the given object, but
	 * the placeholder of a masked element.
	 */
	private static void assertMasked(String json, TLObject hidden, String label) {
		assertFalse("The client receives the label of an object the reader may not discover: " + json,
			json.contains(label));
		assertFalse("The client receives the identity of an object the reader may not discover: " + json,
			json.contains(idArgument(hidden)));
		assertTrue("The client receives no placeholder for the masked object: " + json,
			json.contains(MaskedValues.placeholder()));
	}

	/**
	 * The argument of a link destination identifying the given object, as it occurs in the
	 * {@code href} of a link to it.
	 */
	private static String idArgument(TLObject object) {
		Object id = TLObjectLinkUtil.getObjectArgumentsMap(href(object)).get("id");
		assertNotNull("The link destination identifies the object.", id);
		return "id=" + id;
	}

	private static String href(TLObject object) {
		return TLObjectLinkUtil.getLinkDestination(object, null);
	}

	/** An object link as the editor stores it for a link. */
	private static String link(TLObject object, String label) {
		return anchor(href(object), label);
	}

	/** An object link to the given destination, as the editor stores it for a link. */
	private static String anchor(String href, String label) {
		return "<a class=\"" + TLObjectLinkUtil.TL_OBJECT + "\" href=\"" + href.replace("&", "&amp;") + "\">" + label
			+ "</a>";
	}

	/** An object link as the editor stores it for a mention presented as chip. */
	private static String mention(TLObject object, String label) {
		return "<a class=\"" + TLObjectLinkUtil.TL_OBJECT + "\" data-mention=\"" + MENTION_TRIGGER + "\" href=\""
			+ href(object).replace("&", "&amp;") + "\">" + label + "</a>";
	}


	private static StructuredText textWith(String link) {
		return new StructuredText("<p>See " + link + " for details.</p>");
	}

	/** A text with a link to {@link #_shown} and one to {@link #_hidden}. */
	private StructuredText mixedText() {
		return textWith(link(_shown, SHOWN_LABEL) + " and " + link(_hidden, HIDDEN_LABEL));
	}

	/** The text the client received from the given control. */
	private static String clientValue(ReactWysiwygControl control) {
		return (String) control.scriptingScalarState().get(FieldState.VALUE__PROP);
	}

	/** Sends the given text as the client does after an edit. */
	private static void send(ReactWysiwygControl editor, String html) {
		editor.executeCommand(CMD_VALUE_CHANGED, Map.of(ARG_VALUE, html));
	}

	/** The source of the text the given field holds. */
	private static String storedSource(AbstractFieldModel field) {
		return ((StructuredText) field.getValue()).getSourceCode();
	}

	/** The placeholders of masked object links in the given text of the client, in their order. */
	private static List<String> placeholders(String html) {
		List<String> result = new ArrayList<>();
		Matcher matcher = PLACEHOLDER.matcher(html);
		while (matcher.find()) {
			result.add(matcher.group());
		}
		return result;
	}

	/** The text of the given message to the user. */
	private static String text(HTMLFragment message) throws IOException {
		StringWriter buffer = new StringWriter();
		try (TagWriter out = new TagWriter(buffer)) {
			message.write(DummyDisplayContext.newInstance(), out);
		}
		return buffer.toString();
	}

	private static AbstractFieldModel field(StructuredText text, boolean editable) {
		AbstractFieldModel result = new AbstractFieldModel(text);
		result.setEditable(editable);
		return result;
	}

	private ReactWysiwygControl control(AbstractFieldModel field) {
		return new ReactWysiwygControl(_context, field);
	}

	/**
	 * Navigator displaying every object, recording what it was asked to display.
	 */
	private static final class Targets implements ObjectNavigator {

		private final List<Object> _shownObjects = new ArrayList<>();

		@Override
		public boolean canShow(Object value) {
			return true;
		}

		@Override
		public void show(ReactContext context, Object value) {
			_shownObjects.add(value);
		}

		List<Object> shown() {
			return _shownObjects;
		}
	}

	/**
	 * Collects the error messages to the user.
	 */
	private static final class Messages implements ErrorSink {

		private final List<HTMLFragment> _errors = new ArrayList<>();

		@Override
		public void showError(HTMLFragment content) {
			_errors.add(content);
		}

		@Override
		public void showWarning(HTMLFragment content) {
			// Not part of what is tested here.
		}

		@Override
		public void showInfo(HTMLFragment content) {
			// Not part of what is tested here.
		}

		List<HTMLFragment> errors() {
			return _errors;
		}
	}

	/**
	 * The suite installing the test model with its access rights, and starting the services that
	 * decide about the access rights and build and resolve the links to the objects.
	 */
	public static Test suite() {
		Test kbTest = KBSetup.getSingleKBTest(TestMaskedObjectLinks.class,
			ServiceTestSetup.createStarterFactoryForModules(getModules(
				AccessManager.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				InitialRolesManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				BookmarkService.Module.INSTANCE)));
		String configFile = CustomPropertiesDecorator.createFileName(TestMaskedObjectLinks.class, CONFIG_FILE);
		return TLTestSetup.createTLTestSetup(TestUtils.doNotMerge(new CustomPropertiesSetup(kbTest, configFile, true)));
	}

}
