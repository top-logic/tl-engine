/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.wysiwyg;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import jakarta.servlet.http.Part;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.TextPart;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataPart;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.wysiwyg.I18NConstants;
import com.top_logic.layout.react.wysiwyg.ReactWysiwygControl;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextUtil;
import com.top_logic.util.error.TopLogicException;

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

	/** State asking the client to insert markup at the cursor. */
	private static final String INSERT = "insert";

	/** Field of {@link #INSERT} holding the markup to insert. */
	private static final String INSERT_HTML = "html";

	/** Name of an uploaded image the edited text does not have yet. */
	private static final String UPLOADED = "upload.png";

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

	/**
	 * The images of an upload are asked of the client as one insertion of their download URLs, the
	 * editor serves them, and they are stored as references once the client reports the text.
	 */
	public void testUploadedImagesAreInsertedOnce() {
		ReactWysiwygControl editor = editor("<p>text</p>");

		editor.handleUpload(null, List.of(imagePart(UPLOADED), imagePart(IMAGE)));

		String inserted = insertedHtml(editor);
		assertNotNull("The upload asked nothing of the client.", inserted);
		assertTrue(inserted, inserted.contains(DATA_PATH));
		assertTrue(inserted, inserted.contains(KEY_PARAM + UPLOADED));
		assertTrue("A second image of the same name is inserted under a name of its own: " + inserted,
			inserted.contains(KEY_PARAM + "picture+1_1.png"));
		assertNotNull("The uploaded image is not served.", editor.getDownloadData(UPLOADED));

		String stored = receive(editor, "<p>text" + inserted + "</p>");

		assertNull("The insertion stands although the client carried it out.", insertedHtml(editor));
		assertEquals("<p>text<img src=\"" + I18NStructuredTextUtil.getImageRefID(UPLOADED) + "\"><img src=\""
			+ I18NStructuredTextUtil.getImageRefID("picture 1_1.png") + "\"></p>", stored);
	}

	/**
	 * An upload holding a file that is no image of an accepted type is rejected as a whole: nothing
	 * of it is served or inserted, not even the images beside it.
	 */
	public void testUnsupportedFilesAreRejected() {
		assertRejected(part("drawing.svg", "<svg xmlns=\"http://www.w3.org/2000/svg\"/>", "image/svg+xml"));
		assertRejected(part("fake.png", "<p>no image</p>", "image/png"));
		assertRejected(part("bitmap.png", bmp(), "image/png"));
	}

	private void assertRejected(Part unsupported) {
		ReactWysiwygControl editor = editor("<p>text</p>");

		try {
			editor.handleUpload(null, List.of(imagePart(UPLOADED), unsupported));
			fail("Accepted " + unsupported.getSubmittedFileName());
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_UNSUPPORTED_IMAGE_TYPE__NAME.fill(unsupported.getSubmittedFileName()),
				ex.getErrorKey());
		}

		assertNull(insertedHtml(editor));
		assertNull(editor.getDownloadData(UPLOADED));
		assertNull(editor.getDownloadData(unsupported.getSubmittedFileName()));
	}

	/** An upload without a file inserts nothing. */
	public void testUploadWithoutFileInsertsNothing() {
		ReactWysiwygControl editor = editor("<p>text</p>");

		editor.handleUpload(null, List.of(new TextPart("controlId", null, "x")));

		assertNull(insertedHtml(editor));
	}

	private static Part imagePart(String fileName) {
		return part(fileName, encode("png"), "image/png");
	}

	private static Part part(String fileName, String content, String contentType) {
		return part(fileName, content.getBytes(StandardCharsets.UTF_8), contentType);
	}

	private static Part part(String fileName, byte[] content, String contentType) {
		BinaryData data = BinaryDataFactory.createBinaryData(content, contentType, fileName);
		return new BinaryDataPart(new TextPart(UploadSupport.FILE_PART, fileName, ""), data);
	}

	/** A bitmap image, a format a browser shows but the editor does not accept. */
	private static byte[] bmp() {
		return encode("bmp");
	}

	/** A one-pixel image in the given {@link ImageIO} format. */
	private static byte[] encode(String format) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			assertTrue(ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), format, out));
		} catch (IOException ex) {
			throw new AssertionError(ex);
		}
		return out.toByteArray();
	}

	@SuppressWarnings("unchecked")
	private static String insertedHtml(ReactWysiwygControl editor) {
		Map<String, Object> insert = (Map<String, Object>) editor.scriptingScalarState().get(INSERT);
		return insert == null ? null : (String) insert.get(INSERT_HTML);
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
