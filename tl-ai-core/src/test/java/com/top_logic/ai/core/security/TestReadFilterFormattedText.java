/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.security;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.utility.MaskedValues;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.TLObjectLinkUtil;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.commandhandlers.BookmarkService;
import com.top_logic.util.model.ModelService;

/**
 * Tests that {@link ReadFilter} masks each object link of a formatted text that leads to an object
 * the principal may not read: the model reads neither its label nor its identity.
 */
public class TestReadFilterFormattedText extends BasicTestCase {

	private static final String BOOK = "test.objectedit:Book";

	private static final String SHOWN_LABEL = "shownBook";

	private static final String HIDDEN_LABEL = "hiddenBook";

	private TLObject _shown;

	private TLObject _hidden;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			_shown = newObject(BOOK);
			_hidden = newObject(BOOK);
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			_shown.tDelete();
			_hidden.tDelete();
			tx.commit();
		}
		_shown = null;
		_hidden = null;
		super.tearDown();
	}

	/**
	 * A link to an object the principal may not read reaches the model as the placeholder of a
	 * masked element, a link to a readable object as it is stored.
	 */
	public void testLinkToUnreadableObjectIsMasked() {
		ReadFilter filter = filter();

		String source = ((StructuredText) filter.visible(mixedText())).getSourceCode();
		assertTrue(source, source.contains(idArgument(_shown)));
		assertTrue(source, source.contains(MaskedValues.placeholder()));
		assertFalse(source, source.contains(HIDDEN_LABEL));
		assertFalse(source, source.contains(idArgument(_hidden)));

		String json = ReadFilter.json(filter.disclose(mixedText()));
		assertTrue(json, json.contains(SHOWN_LABEL));
		assertFalse(json, json.contains(HIDDEN_LABEL));
	}

	/**
	 * Each language of a formatted text in several languages is masked.
	 */
	public void testLinkInEachLanguageIsMasked() {
		I18NStructuredText texts =
			new I18NStructuredText(Map.of(Locale.ENGLISH, mixedText(), Locale.GERMAN, mixedText()));

		I18NStructuredText visible = (I18NStructuredText) filter().visible(texts);

		for (StructuredText text : visible.getEntries().values()) {
			String source = text.getSourceCode();
			assertTrue(source, source.contains(SHOWN_LABEL));
			assertFalse(source, source.contains(HIDDEN_LABEL));
			assertFalse(source, source.contains(idArgument(_hidden)));
		}
	}

	/**
	 * A formatted text without a link to mask is the same value, as every value the filter leaves
	 * as it is.
	 */
	public void testTextWithReadableLinksIsTheSameInstance() {
		StructuredText text = new StructuredText("<p>See " + link(_shown, SHOWN_LABEL) + ".</p>");

		assertSame(text, filter().visible(text));
	}

	private ReadFilter filter() {
		return ReadFilter.forSecurity(new ReadsOnly(_shown));
	}

	private StructuredText mixedText() {
		return new StructuredText(
			"<p>See " + link(_shown, SHOWN_LABEL) + " and " + link(_hidden, HIDDEN_LABEL) + ".</p>");
	}

	/** An object link as the editor stores it. */
	private static String link(TLObject object, String label) {
		return "<a class=\"" + TLObjectLinkUtil.TL_OBJECT + "\" href=\""
			+ TLObjectLinkUtil.getLinkDestination(object, null).replace("&", "&amp;") + "\">" + label + "</a>";
	}

	/** The argument of a stored link destination that identifies the given object. */
	private static String idArgument(TLObject object) {
		Object id = TLObjectLinkUtil.getObjectArgumentsMap(TLObjectLinkUtil.getLinkDestination(object, null)).get("id");
		assertNotNull("The link destination identifies the object.", id);
		return "id=" + id + "&";
	}

	private static TLObject newObject(String type) {
		return ModelService.getInstance().getFactory().createObject((TLClass) TLModelUtil.findType(type), null);
	}

	/**
	 * Reads the given object only and grants nothing else: what is under test is what the filter
	 * does with a formatted text, not the engine's role evaluation.
	 */
	private static final class ReadsOnly implements ModelSecurity {

		private final TLObject _readable;

		ReadsOnly(TLObject readable) {
			_readable = readable;
		}

		@Override
		public boolean canRead(TLObject obj) {
			return obj == _readable;
		}

		@Override
		public boolean canRead(TLObject obj, TLStructuredTypePart attribute) {
			return canRead(obj);
		}

		@Override
		public boolean canWrite(TLObject obj) {
			return false;
		}

		@Override
		public boolean canWrite(TLObject obj, TLStructuredTypePart attribute) {
			return false;
		}

		@Override
		public boolean canDelete(TLObject obj) {
			return false;
		}

		@Override
		public boolean canCreate(TLClass type, TLObject context) {
			return false;
		}

		@Override
		public boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute, TLClass type) {
			return false;
		}

		@Override
		public boolean canSetInitial(TLObject draft, TLStructuredTypePart attribute) {
			return false;
		}

		@Override
		public boolean canPerform(TLObject obj, BoundCommandGroup operation) {
			return false;
		}

		@Override
		public <T extends TLObject> List<T> filterReadable(Collection<T> objects) {
			return objects.stream().filter(this::canRead).toList();
		}

		@Override
		public Set<TLClass> accessibleTypes(BoundCommandGroup operation) {
			return Set.of();
		}
	}

	/**
	 * The knowledge base with the test model, and the services that build and resolve the
	 * destinations of object links.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestReadFilterFormattedText.class),
				ModelService.Module.INSTANCE, CommandHandlerFactory.Module.INSTANCE, BookmarkService.Module.INSTANCE));
	}
}
