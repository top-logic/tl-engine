/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.wysiwyg;

import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.wysiwyg.Mention;
import com.top_logic.layout.react.wysiwyg.MentionArguments;
import com.top_logic.layout.react.wysiwyg.MentionPresentation;
import com.top_logic.layout.react.wysiwyg.ReactWysiwygControl;
import com.top_logic.layout.react.wysiwyg.WysiwygControlProvider;
import com.top_logic.layout.view.form.AttributeFieldModel;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.TLObjectLinkUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.commandhandlers.BookmarkService;

/**
 * Tests the {@link WysiwygControlProvider.Config#getMentions() mentions} of the HTML editor: the
 * objects a {@link Mention} offers for the text typed after its trigger character, computed by a
 * TL-Script function and answered to the command {@link MentionArguments#COMMAND}.
 */
public class TestMentions extends AbstractSearchExpressionTest {

	/** Function offering the module whose name is the query. */
	private static final String MODULE_BY_NAME =
		"self -> query -> all(`tl.model:TLModule`).filter(m -> $m.get(`tl.model:TLNamedPart#name`) == $query)";

	/** Function offering one object, a value that is no object, and nothing. */
	private static final String SINGLE_AND_NON_OBJECTS =
		"self -> query -> [`tl.model:TLClass`, 'no object', null]";

	/** Function offering the edited object. */
	private static final String EDITED_OBJECT = "self -> query -> $self";

	/** Function failing for every query. */
	private static final String FAILING = "self -> query -> throw('failure')";

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

	/**
	 * The client is told the trigger characters and presentations of the configured mentions.
	 */
	public void testMentionsAreAnnounced() throws ConfigurationException {
		ReactWysiwygControl editor = editor(new AbstractFieldModel(null),
			mention("@", MODULE_BY_NAME, null),
			mention("#", SINGLE_AND_NON_OBJECTS, MentionPresentation.LINK));

		assertEquals(List.of(
			Map.of(ReactWysiwygControl.MENTION_TRIGGER, "@",
				ReactWysiwygControl.MENTION_PRESENTATION, MentionPresentation.CHIP.getExternalName()),
			Map.of(ReactWysiwygControl.MENTION_TRIGGER, "#",
				ReactWysiwygControl.MENTION_PRESENTATION, MentionPresentation.LINK.getExternalName())),
			editor.scriptingScalarState().get(ReactWysiwygControl.MENTIONS));
	}

	/**
	 * An editor without mentions announces none.
	 */
	public void testNoMentions() throws ConfigurationException {
		ReactWysiwygControl editor = editor(new AbstractFieldModel(null));

		assertNull(editor.scriptingScalarState().get(ReactWysiwygControl.MENTIONS));
		assertTrue(editor.getMentions().isEmpty());
	}

	/**
	 * The query reaches the function, which filters by it; the answer names the objects by label
	 * and link and echoes the trigger and the query.
	 */
	public void testQueryReachesFunction() throws ConfigurationException {
		ReactWysiwygControl editor = editor(new AbstractFieldModel(null),
			mention("@", MODULE_BY_NAME, null),
			mention("#", SINGLE_AND_NON_OBJECTS, MentionPresentation.LINK));

		TLModule core = TLModelUtil.findModule("tl.core");
		assertEquals(List.of(entry(core)), entries(mention(editor, "@", "tl.core"), "@", "tl.core"));

		TLModule model = TLModelUtil.findModule("tl.model");
		assertEquals(List.of(entry(model)), entries(mention(editor, "@", "tl.model"), "@", "tl.model"));

		assertEquals(List.of(), entries(mention(editor, "@", "no such module"), "@", "no such module"));
	}

	/**
	 * A single object is offered as such, values that are no objects are skipped.
	 */
	public void testSingleObjectAndNonObjects() throws ConfigurationException {
		ReactWysiwygControl editor = editor(new AbstractFieldModel(null),
			mention("@", MODULE_BY_NAME, null),
			mention("#", SINGLE_AND_NON_OBJECTS, MentionPresentation.LINK));

		TLObject type = (TLObject) TLModelUtil.findType("tl.model:TLClass");
		assertEquals(List.of(entry(type)), entries(mention(editor, "#", ""), "#", ""));
	}

	/**
	 * A trigger of no configured mention is answered with no objects.
	 */
	public void testUnknownTrigger() throws ConfigurationException {
		ReactWysiwygControl editor = editor(new AbstractFieldModel(null), mention("@", MODULE_BY_NAME, null));

		assertEquals(List.of(), entries(mention(editor, "!", "tl.core"), "!", "tl.core"));
	}

	/**
	 * The function receives the object the edited text is an attribute of.
	 */
	public void testSelfIsTheEditedObject() throws ConfigurationException {
		TLModule core = TLModelUtil.findModule("tl.core");
		TLClass moduleType = (TLClass) TLModelUtil.findType("tl.model:TLModule");
		AttributeFieldModel field = new AttributeFieldModel(core, moduleType.getPartOrFail("name"));
		ReactWysiwygControl editor = editor(field, mention("@", EDITED_OBJECT, null));

		assertEquals(List.of(entry(core)), entries(mention(editor, "@", ""), "@", ""));
	}

	/**
	 * A failing function answers with no objects and leaves the edited value alone.
	 */
	public void testFailingFunction() throws ConfigurationException {
		AbstractFieldModel field = new AbstractFieldModel(new StructuredText("<p>text</p>"));
		Object value = field.getValue();
		ReactWysiwygControl editor = editor(field, mention("@", FAILING, null));

		assertEquals(List.of(), entries(mention(editor, "@", "x"), "@", "x"));
		assertSame(value, field.getValue());
	}

	/**
	 * Two mentions of one editor cannot use the same trigger character.
	 */
	public void testDuplicateTriggerIsRejected() {
		assertConfigError(() -> editor(new AbstractFieldModel(null),
			mention("@", MODULE_BY_NAME, null),
			mention("@", SINGLE_AND_NON_OBJECTS, null)));
	}

	/**
	 * A trigger is a single character.
	 */
	public void testTriggerIsSingleCharacter() {
		assertConfigError(() -> editor(new AbstractFieldModel(null), mention("@@", MODULE_BY_NAME, null)));
	}

	private interface ConfigAction {
		void run() throws ConfigurationException;
	}

	private static void assertConfigError(ConfigAction action) {
		try {
			action.run();
		} catch (ConfigurationException | RuntimeException ex) {
			return;
		}
		fail("Expected the configuration to be rejected.");
	}

	private static Map<String, Object> entry(TLObject object) {
		return Map.of(
			ReactWysiwygControl.ENTRY_LABEL, MetaLabelProvider.INSTANCE.getLabel(object),
			ReactWysiwygControl.ENTRY_LINK, TLObjectLinkUtil.getLinkDestination(object, null));
	}

	/**
	 * Asks the editor for the objects of a mention, and returns the answer.
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> mention(ReactWysiwygControl editor, String trigger, String query) {
		editor.executeCommand(MentionArguments.COMMAND,
			Map.of(MentionArguments.TRIGGER, trigger, MentionArguments.QUERY, query));
		Map<String, Object> answer =
			(Map<String, Object>) editor.scriptingScalarState().get(ReactWysiwygControl.MENTION_OPTIONS);
		assertNotNull("The editor answers a mention request.", answer);
		return answer;
	}

	/**
	 * The entries of the given answer, after checking that it echoes the given trigger and query.
	 */
	private static Object entries(Map<String, Object> answer, String trigger, String query) {
		assertEquals(trigger, answer.get(ReactWysiwygControl.OPTIONS_TRIGGER));
		assertEquals(query, answer.get(ReactWysiwygControl.OPTIONS_QUERY));
		return answer.get(ReactWysiwygControl.OPTIONS_ENTRIES);
	}

	private static Mention.Config mention(String trigger, String options, MentionPresentation presentation)
			throws ConfigurationException {
		Mention.Config config = TypedConfiguration.newConfigItem(Mention.Config.class);
		TypedConfigUtil.setProperty(config, Mention.Config.TRIGGER, trigger);
		TypedConfigUtil.setProperty(config, Mention.Config.OPTIONS,
			ExprFormat.INSTANCE.getValue(Mention.Config.OPTIONS, options));
		if (presentation != null) {
			TypedConfigUtil.setProperty(config, Mention.Config.PRESENTATION, presentation);
		}
		return config;
	}

	private ReactWysiwygControl editor(FieldModel model, Mention.Config... mentions) throws ConfigurationException {
		WysiwygControlProvider.Config config = TypedConfiguration.newConfigItem(WysiwygControlProvider.Config.class);
		config.getMentions().addAll(List.of(mentions));
		WysiwygControlProvider provider =
			(WysiwygControlProvider) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		return (ReactWysiwygControl) provider.createControl(_context, FieldSpec.of(StructuredText.class, "Text"),
			model);
	}

	/**
	 * The suite starting the services the functions of the mentions are compiled and evaluated
	 * with, and the service the links to the objects are built with.
	 *
	 * <p>
	 * The functions do not depend on the database, so a single knowledge base suffices.
	 * </p>
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestMentions.class,
			ServiceTestSetup.createStarterFactoryForModules(getModules(BookmarkService.Module.INSTANCE)));
	}

}
