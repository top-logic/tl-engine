/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.html.ReactHtmlControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.HtmlValues;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.HtmlDisplay;
import com.top_logic.layout.view.element.HtmlElement;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests which channel values a {@link HtmlElement} displays as HTML and which of them it refuses.
 *
 * <p>
 * The conversion is exercised through {@link HtmlValues}, the display through the element's public
 * seam - a configuration naming an input channel, a control created for a view context, and the
 * client state that control publishes.
 * </p>
 *
 * <p>
 * Content displayed as a document does not travel with that state: it is the document the control
 * serves as a {@link DataProvider} to the frame showing it.
 * </p>
 */
public class TestHtmlElement extends TestCase {

	private static final String INPUT = "content";

	private ViewChannel _channel;

	private ReactControl _view;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_channel = new DefaultViewChannel(INPUT);
		_view = createView(_channel, HtmlDisplay.INLINE);
	}

	/** HTML source is displayed as it stands. */
	public void testSourceIsDisplayedAsItStands() {
		assertEquals("<p>Done.</p>", HtmlValues.toHtml("<p>Done.</p>"));

		_channel.set("<p>Done.</p>");

		assertEquals("<p>Done.</p>", html());
		assertNull("Nothing failed.", error());
	}

	/** A fragment is displayed as the source it writes. */
	public void testAFragmentIsDisplayedAsTheSourceItWrites() {
		HTMLFragment fragment = (context, out) -> {
			out.beginTag(HTMLConstants.PARAGRAPH);
			out.writeText("Done.");
			out.endTag(HTMLConstants.PARAGRAPH);
		};

		assertEquals("<p>Done.</p>", HtmlValues.toHtml(fragment));

		_channel.set(fragment);

		assertEquals("<p>Done.</p>", html());
	}

	/** An HTML document is read with the encoding it declares. */
	public void testADocumentIsReadWithTheEncodingItDeclares() {
		String source = "<p>Erledigt: 3 Pr\u00fcfungen.</p>";

		assertEquals(source, HtmlValues.toHtml(document(source, "text/html", StandardCharsets.UTF_8)));
		assertEquals(source,
			HtmlValues.toHtml(document(source, "text/html; charset=ISO-8859-1", StandardCharsets.ISO_8859_1)));

		_channel.set(document(source, "text/html; charset=utf-8", StandardCharsets.UTF_8));

		assertEquals(source, html());
	}

	/** A document that is not HTML is not displayed. */
	public void testADocumentThatIsNotHtmlIsNotDisplayed() {
		BinaryData pdf = document("%PDF-1.4", "application/pdf", StandardCharsets.UTF_8);

		assertUnsupported(pdf);

		_channel.set(pdf);

		assertEquals("Nothing of the document is shown.", "", html());
		assertNotNull("The reason is shown in its place.", error());
	}

	/** A value that carries no HTML is not displayed. */
	public void testAValueThatCarriesNoHtmlIsNotDisplayed() {
		assertUnsupported(Integer.valueOf(42));

		_channel.set("<p>Done.</p>");
		_channel.set(Integer.valueOf(42));

		assertEquals("The content shown before is gone.", "", html());
		assertNotNull("The reason is shown in its place.", error());
	}

	/** Nothing on the channel is no content, and no failure either. */
	public void testNothingOnTheChannelIsNoContent() {
		assertEquals("", HtmlValues.toHtml(null));

		_channel.set("<p>Done.</p>");
		_channel.set(null);

		assertEquals("", html());
		assertNull("Nothing failed.", error());
	}

	/** A script is not inserted into the page; the check that stopped it says so instead. */
	public void testAScriptIsNotInsertedIntoThePage() {
		String source = "<p>Done.</p><script>alert('!')</script>";

		assertEquals("The source itself is unchanged - the check is the element's, not the conversion's.",
			source, HtmlValues.toHtml(source));

		_channel.set(source);

		assertEquals("Nothing of the rejected content is shown.", "", html());
		assertNotNull("The reason is shown in its place.", error());
	}

	/** An attribute carrying a script is stopped the same way. */
	public void testAnAttributeCarryingAScriptIsStopped() {
		_channel.set("<p onclick=\"alert('!')\">Done.</p>");

		assertEquals("", html());
		assertNotNull("The reason is shown in its place.", error());
	}

	/** Content accepted after a rejected one replaces the message. */
	public void testAcceptedContentReplacesTheMessage() {
		_channel.set("<script>alert('!')</script>");
		assertNotNull(error());

		_channel.set("<p>Done.</p>");

		assertEquals("<p>Done.</p>", html());
		assertNull("The message of the content before is gone.", error());
	}

	/** A document is served as the HTML file the frame showing it fetches. */
	public void testADocumentIsServedAsAnHtmlFile() throws IOException {
		String source = "<!DOCTYPE html><html><head><title>Expos\u00e9</title></head><body><p>Erledigt.</p></body></html>";

		ViewChannel channel = new DefaultViewChannel(INPUT);
		ReactControl view = createView(channel, HtmlDisplay.DOCUMENT);

		channel.set(source);

		BinaryData served = served(view);
		assertTrue("Served as HTML: " + served.getContentType(),
			served.getContentType().startsWith(HtmlValues.HTML_CONTENT_TYPE));
		assertEquals(source, read(served));
		assertEquals("The document is fetched, not sent with the state.", "",
			state(view, ReactHtmlControl.HTML));
	}

	/** Every content is another revision, so the frame fetches it instead of the one before. */
	public void testEveryContentIsAnotherRevision() throws IOException {
		ViewChannel channel = new DefaultViewChannel(INPUT);
		ReactControl view = createView(channel, HtmlDisplay.DOCUMENT);

		int initial = revision(view);

		channel.set("<p>Erledigt.</p>");
		int first = revision(view);
		assertTrue("A content later than the one the control started with.", first > initial);

		channel.set("<p>Abgeschlossen.</p>");
		assertTrue("A content later than the one before.", revision(view) > first);
		assertEquals("<p>Abgeschlossen.</p>", read(served(view)));
	}

	/** What is not inserted into the page is served as a document, which the frame isolates. */
	public void testAScriptIsServedAsADocument() throws IOException {
		String source = "<p>Done.</p><script>alert('!')</script>";

		_channel.set(source);
		assertEquals("Not inserted into the page.", "", html());
		assertNotNull(error());

		ViewChannel channel = new DefaultViewChannel(INPUT);
		ReactControl view = createView(channel, HtmlDisplay.DOCUMENT);

		channel.set(source);

		assertEquals("No check stands between the content and the document.", source, read(served(view)));
		assertNull("Nothing failed.", state(view, ReactHtmlControl.ERROR));
	}

	/** A thumbnail is the same document as a document display, shown as a picture of itself. */
	public void testAThumbnailIsServedAsTheSameDocument() throws IOException {
		String source = "<!DOCTYPE html><html><body><p>Erledigt.</p></body></html>";

		ViewChannel channel = new DefaultViewChannel(INPUT);
		ReactControl view = createView(channel, HtmlDisplay.THUMBNAIL);

		channel.set(source);

		assertEquals(source, read(served(view)));
		assertEquals("The document is fetched, not sent with the state.", "",
			state(view, ReactHtmlControl.HTML));
	}

	/** The page size a thumbnail is laid out at is the one the configuration names. */
	public void testAThumbnailIsLaidOutAtTheConfiguredPageSize() {
		ViewChannel channel = new DefaultViewChannel(INPUT);
		ReactControl view = createView(channel, HtmlDisplay.THUMBNAIL, 400, 300);

		assertEquals(400, size(view, ReactHtmlControl.THUMBNAIL_WIDTH));
		assertEquals(300, size(view, ReactHtmlControl.THUMBNAIL_HEIGHT));
	}

	/** Without a configured page size, a thumbnail is laid out at a portrait page. */
	public void testAThumbnailIsAPortraitPageByDefault() {
		int width = size(_view, ReactHtmlControl.THUMBNAIL_WIDTH);
		int height = size(_view, ReactHtmlControl.THUMBNAIL_HEIGHT);

		assertTrue("A page of some width: " + width, width > 0);
		assertTrue("Taller than wide: " + width + "x" + height, height > width);
	}

	private static void assertUnsupported(Object value) {
		try {
			String html = HtmlValues.toHtml(value);
			fail("Displayed as HTML: " + html);
		} catch (TopLogicException expected) {
			assertNotNull(expected.getErrorKey());
		}
	}

	private static ReactControl createView(ViewChannel channel, HtmlDisplay display) {
		return createView(channel, display, null, null);
	}

	private static ReactControl createView(ViewChannel channel, HtmlDisplay display, Integer thumbnailWidth,
			Integer thumbnailHeight) {
		HtmlElement.Config config = TypedConfiguration.newConfigItem(HtmlElement.Config.class);
		config.update(config.descriptor().getProperty(HtmlElement.Config.INPUT), new ChannelRef(INPUT));
		config.update(config.descriptor().getProperty(HtmlElement.Config.DISPLAY), display);
		if (thumbnailWidth != null) {
			config.update(config.descriptor().getProperty(HtmlElement.Config.THUMBNAIL_WIDTH), thumbnailWidth);
		}
		if (thumbnailHeight != null) {
			config.update(config.descriptor().getProperty(HtmlElement.Config.THUMBNAIL_HEIGHT), thumbnailHeight);
		}

		DefaultInstantiationContext instantiationContext = new DefaultInstantiationContext(TestHtmlElement.class);
		HtmlElement element = (HtmlElement) instantiationContext.getInstance(config);

		ViewContext context = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")));
		context.registerChannel(INPUT, channel);

		return (ReactControl) element.createControl(context);
	}

	/** A document of the given source, content type and encoding. */
	private static BinaryData document(String source, String contentType, Charset charset) {
		return BinaryDataFactory.createBinaryData(source.getBytes(charset), contentType, "report");
	}

	private String html() {
		return (String) state(_view, ReactHtmlControl.HTML);
	}

	private String error() {
		return (String) state(_view, ReactHtmlControl.ERROR);
	}

	/** The document the given view serves to the frame showing it. */
	private static BinaryData served(ReactControl view) {
		return ((DataProvider) view).getDownloadData(null);
	}

	/** The source of the given document. */
	private static String read(BinaryData data) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		data.deliverTo(buffer);
		return buffer.toString(StandardCharsets.UTF_8);
	}

	/** The revision the given view announces its current content under. */
	private static int revision(ReactControl view) {
		return ((Number) state(view, ReactHtmlControl.DATA_REVISION)).intValue();
	}

	/** The page size the given view lays a thumbnail out at, under the given state key. */
	private static int size(ReactControl view, String key) {
		return ((Number) state(view, key)).intValue();
	}

	private static Object state(ReactControl view, String key) {
		String json = view.stateAsJSON();
		try {
			return ((Map<?, ?>) JSON.fromString(json)).get(key);
		} catch (ParseException ex) {
			throw new RuntimeException("Not a state object: " + json, ex);
		}
	}

	/** Suite requiring the HTML safety check and the resources its messages are resolved from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestHtmlElement.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				SafeHTML.Module.INSTANCE));
	}

}
