/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.image.ImageFit;
import com.top_logic.layout.react.control.image.ImageSource;
import com.top_logic.layout.react.control.image.ReactImageControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.ImageElement;

/**
 * Tests what an {@link ImageElement} displays: which channel values are shown as a picture, how a
 * configured resource fills in for a channel that holds none, and how the configured shape of the
 * box reaches the client.
 *
 * <p>
 * The element is exercised through its public seam - a configuration, a control created for a view
 * context, and the client state that control publishes.
 * </p>
 */
public class TestImageElement extends TestCase {

	private static final String INPUT = "image";

	/** Context path of the test application, prefixed to a configured resource. */
	private static final String CONTEXT_PATH = "/app";

	/** Path of a resource of the web application, as it is configured. */
	private static final String RESOURCE = "/images/logo.svg";

	/** Address the configured {@link #RESOURCE} is loaded from. */
	private static final String RESOURCE_URL = CONTEXT_PATH + RESOURCE;

	private ViewChannel _channel;

	private ReactControl _image;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_channel = new DefaultViewChannel(INPUT);
		_image = createImage(_channel);
	}

	/** A picture is displayed. */
	public void testImageIsDisplayed() {
		long revision = dataRevision();

		_channel.set(data("image/png"));

		assertTrue("A PNG is a picture.", hasData());
		assertTrue("The displayed data has changed.", dataRevision() > revision);
	}

	/** Binary data that is not a picture is not displayed. */
	public void testOtherBinaryDataIsNotDisplayed() {
		_channel.set(data("image/png"));
		long revision = dataRevision();

		_channel.set(data("application/pdf"));

		assertFalse("A PDF is not a picture.", hasData());
		assertTrue("The picture shown before is gone.", dataRevision() > revision);
	}

	/** The content type names the media type regardless of case and of its parameters. */
	public void testContentTypeSyntax() {
		_channel.set(data("IMAGE/PNG"));
		assertTrue("The media type is case-insensitive.", hasData());

		_channel.set(data("image/svg+xml; charset=utf-8"));
		assertTrue("Parameters after the media type are tolerated.", hasData());

		_channel.set(data(null));
		assertFalse("Data without a content type is not known to be a picture.", hasData());
	}

	/** A text value names the address the picture is loaded from. */
	public void testUrlIsDisplayed() {
		_channel.set("/media/logo.png");

		assertFalse("The address is loaded by the client, not served by the control.", hasData());
		assertEquals("/media/logo.png", url());
	}

	/** A value that is neither a picture nor an address is not displayed. */
	public void testUnrelatedValueIsNotDisplayed() {
		_channel.set(data("image/png"));

		_channel.set(Integer.valueOf(42));

		assertFalse("A number is no picture.", hasData());
		assertNull("A number names no address either.", url());
	}

	/** The value a channel already carries when the control is created is displayed the same way. */
	public void testInitialChannelValue() {
		ViewChannel pdfChannel = new DefaultViewChannel(INPUT);
		pdfChannel.set(data("application/pdf"));
		assertFalse("A PDF is not a picture.", hasData(createImage(pdfChannel)));

		ViewChannel imageChannel = new DefaultViewChannel(INPUT);
		imageChannel.set(data("image/png"));
		assertTrue("A PNG is a picture.", hasData(createImage(imageChannel)));
	}

	/** A configured resource alone is the picture, loaded from the context path of the application. */
	public void testResourceIsDisplayed() {
		ReactControl image = createImage(null, config -> set(config, ImageElement.Config.RESOURCE, RESOURCE));

		assertFalse("A resource is loaded by the client, not served by the control.", hasData(image));
		assertEquals("The resource is addressed within the application.", RESOURCE_URL, url(image));
	}

	/** Next to an input channel, the resource is what is shown while that channel holds no picture. */
	public void testResourceIsPlaceholder() {
		ViewChannel channel = new DefaultViewChannel(INPUT);
		ReactControl image =
			createImage(channel, config -> set(config, ImageElement.Config.RESOURCE, RESOURCE));

		assertEquals("Nothing on the channel leaves the resource.", RESOURCE_URL, url(image));

		channel.set(data("application/pdf"));
		assertFalse("A PDF is not a picture.", hasData(image));
		assertEquals("A value that is no picture leaves the resource.", RESOURCE_URL, url(image));

		channel.set(data("image/png"));
		assertTrue("The picture of the channel is served by the control.", hasData(image));
		assertNull("A picture served by the control is not loaded from an address.", url(image));

		channel.set("/media/other.png");
		assertFalse("An address is loaded by the client.", hasData(image));
		assertEquals("The address of the channel replaces the resource.", "/media/other.png", url(image));

		channel.set(null);
		assertEquals("Without a value the resource is shown again.", RESOURCE_URL, url(image));
	}

	/** Without any display option the picture keeps its natural shape and is shown at once. */
	public void testDisplayDefaults() {
		assertEquals("A picture of different proportions is cropped unless configured otherwise.",
			ImageFit.COVER.getExternalName(), state(_image, ReactImageControl.FIT));
		assertNull("The box takes its proportions from the picture.",
			state(_image, ReactImageControl.ASPECT_RATIO));
		assertNull("The box takes its width from the picture.", state(_image, ReactImageControl.WIDTH));
		assertNull("The box takes its height from the picture.", state(_image, ReactImageControl.HEIGHT));
		assertEquals("The picture is loaded right away.",
			Boolean.FALSE, state(_image, ReactImageControl.LAZY));
		assertNull("No CSS class is added.", state(_image, ReactControl.CSS_CLASS));
	}

	/** Every configured display option reaches the client. */
	public void testDisplayOptions() {
		ReactControl image = createImage(null, config -> {
			set(config, ImageElement.Config.RESOURCE, RESOURCE);
			set(config, ImageElement.Config.FIT, ImageFit.CONTAIN);
			set(config, ImageElement.Config.ASPECT_RATIO, "16/9");
			set(config, ImageElement.Config.WIDTH, "12rem");
			set(config, ImageElement.Config.HEIGHT, "8rem");
			set(config, ImageElement.Config.LAZY, Boolean.TRUE);
			set(config, ImageElement.Config.CSS_CLASS, "tlHero");
		});

		assertEquals(ImageFit.CONTAIN.getExternalName(), state(image, ReactImageControl.FIT));
		assertEquals("16/9", state(image, ReactImageControl.ASPECT_RATIO));
		assertEquals("12rem", state(image, ReactImageControl.WIDTH));
		assertEquals("8rem", state(image, ReactImageControl.HEIGHT));
		assertEquals(Boolean.TRUE, state(image, ReactImageControl.LAZY));
		assertEquals("tlHero", state(image, ReactControl.CSS_CLASS));
	}

	/** Proportions are two numbers separated by a slash; anything else fails the view load. */
	public void testAspectRatioSyntax() throws Exception {
		assertNotNull("Whole numbers around the slash.", loadImage("16/9"));
		assertNotNull("Space around the slash.", loadImage("4 / 3"));
		assertNotNull("Decimals on either side.", loadImage("1.5/1"));

		assertAspectRatioRejected("wide");
		assertAspectRatioRejected("16:9");
		assertAspectRatioRejected("16/");
	}

	private void assertAspectRatioRejected(String aspectRatio) {
		try {
			loadImage(aspectRatio);
			fail("'" + aspectRatio + "' does not name proportions.");
		} catch (ConfigurationException | AbortExecutionException ex) {
			String reported = messages(ex);
			assertTrue("The failure names the property: " + reported,
				reported.contains(ImageElement.Config.ASPECT_RATIO));
		}
	}

	/** The messages along the cause chain of the given failure. */
	private static String messages(Throwable failure) {
		StringBuilder result = new StringBuilder();
		for (Throwable current = failure; current != null; current = current.getCause()) {
			result.append(current.getMessage()).append('\n');
		}
		return result.toString();
	}

	/**
	 * Loads a view showing a picture of the given proportions, the way the application loads its
	 * views: with the constraints of the configuration checked.
	 */
	private static Object loadImage(String aspectRatio) throws ConfigurationException {
		String view = """
				<view>
					<image resource="%s" aspect-ratio="%s"/>
				</view>
				""".formatted(RESOURCE, aspectRatio);

		return ViewLoader.parseConfig(List.of(CharacterContents.newContent(view, "test-image.view.xml")));
	}

	private ReactControl createImage(ViewChannel channel) {
		return createImage(channel, config -> { /* Nothing but the channel. */ });
	}

	/**
	 * The control of an {@link ImageElement} configured by the given setup.
	 *
	 * @param channel
	 *        The channel bound to the element's input, or {@code null} for an element without one.
	 * @param setup
	 *        What the element configures besides its input.
	 */
	private ReactControl createImage(ViewChannel channel, Consumer<ImageElement.Config> setup) {
		ImageElement.Config config = TypedConfiguration.newConfigItem(ImageElement.Config.class);
		if (channel != null) {
			set(config, ImageElement.Config.INPUT, new ChannelRef(INPUT));
		}
		setup.accept(config);

		DefaultInstantiationContext instantiationContext = new DefaultInstantiationContext(TestImageElement.class);
		ImageElement element = (ImageElement) instantiationContext.getInstance(config);

		ViewContext context = new DefaultViewContext(new DefaultReactContext(CONTEXT_PATH, "test",
			new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		if (channel != null) {
			context.registerChannel(INPUT, channel);
		}

		return (ReactControl) element.createControl(context);
	}

	private static void set(ImageElement.Config config, String property, Object value) {
		PropertyDescriptor descriptor = config.descriptor().getProperty(property);
		config.update(descriptor, value);
	}

	/**
	 * Binary data of the given content type.
	 *
	 * @implNote Name and content are derived from the content type, so that data of different
	 *           content types are different values - a channel notifies its listeners only about a
	 *           value that actually changed.
	 */
	private static BinaryData data(String contentType) {
		String name = String.valueOf(contentType);
		return BinaryDataFactory.createBinaryData(name.getBytes(StandardCharsets.UTF_8), contentType, name);
	}

	private boolean hasData() {
		return hasData(_image);
	}

	private static boolean hasData(ReactControl image) {
		return Boolean.TRUE.equals(state(image, ImageSource.HAS_DATA));
	}

	private String url() {
		return url(_image);
	}

	private static String url(ReactControl image) {
		return (String) state(image, ImageSource.URL);
	}

	private long dataRevision() {
		return ((Number) state(_image, ImageSource.DATA_REVISION)).longValue();
	}

	private static Object state(ReactControl image, String key) {
		String json = image.stateAsJSON();
		try {
			return ((Map<?, ?>) JSON.fromString(json)).get(key);
		} catch (ParseException ex) {
			throw new RuntimeException("Not a state object: " + json, ex);
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view.
	 */
	public static Test suite() throws ModuleException {
		return ServiceTestSetup.createSetup(TestImageElement.class, TypeIndex.Module.INSTANCE);
	}

}
