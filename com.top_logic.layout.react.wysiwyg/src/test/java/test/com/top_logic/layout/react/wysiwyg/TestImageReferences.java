/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.wysiwyg;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.wysiwyg.ReactWysiwygControl;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextUtil;

/**
 * Tests that the images of a {@link StructuredText} reach the editor as download URLs and are
 * stored as image references again when the edited text comes back.
 *
 * @see I18NStructuredTextUtil#getImageRefID(String)
 */
public class TestImageReferences extends TestCase {

	/** The command the client sends when the text of the editor changed. */
	private static final String CMD_VALUE_CHANGED = ReactFormFieldControl.CMD_VALUE_CHANGED;

	/** The {@link #CMD_VALUE_CHANGED} argument holding the text, also the state it is sent in. */
	private static final String VALUE = "value";

	/** Path of the download URLs the editor serves images under. */
	private static final String DATA_PATH = "/react-api/data?";

	/** Parameter of a download URL naming the image, as it appears in serialized HTML. */
	private static final String KEY_PARAM = "&amp;key=";

	/** Name of the image the edited text has. */
	private static final String IMAGE = "picture 1.png";

	/** An image source outside the application. */
	private static final String EXTERNAL = "https://example.com/react-api/data?x=1&amp;key=picture.png";

	private AbstractFieldModel _model;

	/** An image reference is sent as download URL and stored as reference again. */
	public void testReferenceRoundTrip() {
		ReactWysiwygControl editor = editor(img(I18NStructuredTextUtil.getImageRefID(IMAGE)));

		String sent = sent(editor);
		assertTrue(sent, sent.contains(DATA_PATH));
		assertTrue(sent, sent.contains(KEY_PARAM + "picture+1.png"));
		assertFalse(sent, sent.contains(I18NStructuredTextUtil.getImageRefID(IMAGE)));

		assertEquals(img(I18NStructuredTextUtil.getImageRefID(IMAGE)), receive(editor, sent));
	}

	/** An image named by its plain key is shown and stored as reference after the edit. */
	public void testPlainKeyBecomesReference() {
		ReactWysiwygControl editor = editor(img(IMAGE));

		String sent = sent(editor);
		assertTrue(sent, sent.contains(DATA_PATH));

		assertEquals(img(I18NStructuredTextUtil.getImageRefID(IMAGE)), receive(editor, sent));
	}

	/** A source naming no image of the text is left as it is in both directions. */
	public void testUnknownSourceIsUntouched() {
		String missing = img(I18NStructuredTextUtil.getImageRefID("missing.png")) + img("other.png");
		ReactWysiwygControl editor = editor(missing);

		assertEquals(missing, sent(editor));
		assertEquals(missing, receive(editor, missing));
	}

	/** An external URL is no download URL of the editor, even if it looks like one. */
	public void testExternalUrlIsUntouched() {
		String external = img(EXTERNAL);
		ReactWysiwygControl editor = editor(external);

		assertEquals(external, sent(editor));
		assertEquals(external, receive(editor, external));
	}

	private ReactWysiwygControl editor(String source) {
		BinaryData image = BinaryDataFactory.createBinaryData("png".getBytes(StandardCharsets.UTF_8), "image/png");
		StructuredText text = new StructuredText(source, Map.of(IMAGE, image));
		_model = new AbstractFieldModel(text);
		return new ReactWysiwygControl(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")), _model);
	}

	private static String sent(ReactWysiwygControl editor) {
		return (String) editor.scriptingScalarState().get(VALUE);
	}

	private String receive(ReactWysiwygControl editor, String html) {
		editor.executeCommand(CMD_VALUE_CHANGED, Map.of(VALUE, html));
		return ((StructuredText) _model.getValue()).getSourceCode();
	}

	private static String img(String src) {
		return "<p><img src=\"" + src + "\"></p>";
	}

	/**
	 * Test suite requiring the session resources an editor is created with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestImageReferences.class, ThreadContextManager.Module.INSTANCE));
	}

}
