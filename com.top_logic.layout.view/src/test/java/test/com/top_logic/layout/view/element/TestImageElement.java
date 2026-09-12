/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.photo.ReactPhotoViewerControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.ImageElement;

/**
 * Tests which channel values an {@link ImageElement} displays: a picture is shown, anything else is
 * not.
 *
 * <p>
 * The element is exercised through its public seam - a configuration naming an input channel, a
 * control created for a view context, and the client state that control publishes.
 * </p>
 */
public class TestImageElement extends TestCase {

	private static final String INPUT = "image";

	private ViewChannel _channel;

	private ReactControl _viewer;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_channel = new DefaultViewChannel(INPUT);
		_viewer = createViewer(_channel);
	}

	/** A picture is displayed. */
	public void testImageIsDisplayed() {
		long revision = dataRevision();

		_channel.set(data("image/png"));

		assertTrue("A PNG is a picture.", hasPhoto());
		assertTrue("The displayed data has changed.", dataRevision() > revision);
	}

	/** Binary data that is not a picture is not displayed. */
	public void testOtherBinaryDataIsNotDisplayed() {
		_channel.set(data("image/png"));
		long revision = dataRevision();

		_channel.set(data("application/pdf"));

		assertFalse("A PDF is not a picture.", hasPhoto());
		assertTrue("The picture shown before is gone.", dataRevision() > revision);
	}

	/** The content type names the media type regardless of case and of its parameters. */
	public void testContentTypeSyntax() {
		_channel.set(data("IMAGE/PNG"));
		assertTrue("The media type is case-insensitive.", hasPhoto());

		_channel.set(data("image/svg+xml; charset=utf-8"));
		assertTrue("Parameters after the media type are tolerated.", hasPhoto());

		_channel.set(data(null));
		assertFalse("Data without a content type is not known to be a picture.", hasPhoto());
	}

	/** A value that is no binary data at all is not displayed. */
	public void testNonBinaryValueIsNotDisplayed() {
		_channel.set(data("image/png"));

		_channel.set("some text");

		assertFalse("Only binary data can be a picture.", hasPhoto());
	}

	/** The value a channel already carries when the control is created is displayed the same way. */
	public void testInitialChannelValue() {
		ViewChannel pdfChannel = new DefaultViewChannel(INPUT);
		pdfChannel.set(data("application/pdf"));
		assertFalse("A PDF is not a picture.", hasPhoto(createViewer(pdfChannel)));

		ViewChannel imageChannel = new DefaultViewChannel(INPUT);
		imageChannel.set(data("image/png"));
		assertTrue("A PNG is a picture.", hasPhoto(createViewer(imageChannel)));
	}

	private ReactControl createViewer(ViewChannel channel) {
		ImageElement.Config config = TypedConfiguration.newConfigItem(ImageElement.Config.class);
		config.update(config.descriptor().getProperty(ImageElement.Config.INPUT), new ChannelRef(INPUT));

		DefaultInstantiationContext instantiationContext = new DefaultInstantiationContext(TestImageElement.class);
		ImageElement element = (ImageElement) instantiationContext.getInstance(config);

		ViewContext context = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue()));
		context.registerChannel(INPUT, channel);

		return (ReactControl) element.createControl(context);
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

	private boolean hasPhoto() {
		return hasPhoto(_viewer);
	}

	private static boolean hasPhoto(ReactControl viewer) {
		return Boolean.TRUE.equals(state(viewer, ReactPhotoViewerControl.HAS_PHOTO));
	}

	private long dataRevision() {
		return ((Number) state(_viewer, ReactPhotoViewerControl.DATA_REVISION)).longValue();
	}

	private static Object state(ReactControl viewer, String key) {
		String json = viewer.stateAsJSON();
		try {
			return ((Map<?, ?>) JSON.fromString(json)).get(key);
		} catch (ParseException ex) {
			throw new RuntimeException("Not a state object: " + json, ex);
		}
	}

}
