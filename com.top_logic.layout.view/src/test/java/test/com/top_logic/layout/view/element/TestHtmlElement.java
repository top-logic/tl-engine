/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

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
 */
public class TestHtmlElement extends TestCase {

	private static final String INPUT = "content";

	private ViewChannel _channel;

	private ReactControl _view;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_channel = new DefaultViewChannel(INPUT);
		_view = createView(_channel);
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

	private static void assertUnsupported(Object value) {
		try {
			String html = HtmlValues.toHtml(value);
			fail("Displayed as HTML: " + html);
		} catch (TopLogicException expected) {
			assertNotNull(expected.getErrorKey());
		}
	}

	private static ReactControl createView(ViewChannel channel) {
		HtmlElement.Config config = TypedConfiguration.newConfigItem(HtmlElement.Config.class);
		config.update(config.descriptor().getProperty(HtmlElement.Config.INPUT), new ChannelRef(INPUT));

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
		return (String) state(ReactHtmlControl.HTML);
	}

	private String error() {
		return (String) state(ReactHtmlControl.ERROR);
	}

	private Object state(String key) {
		String json = _view.stateAsJSON();
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
