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
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.AvatarSize;
import com.top_logic.layout.react.control.common.ReactAvatarControl;
import com.top_logic.layout.react.control.image.ImageSource;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.AvatarElement;

/**
 * Tests what an {@link AvatarElement} shows: the initials of the value it represents, the picture of
 * a second channel where there is one, and the configured diameter.
 *
 * <p>
 * The element is exercised through its public seam - a configuration naming two channels, a control
 * created for a view context, and the client state that control publishes.
 * </p>
 */
public class TestAvatarElement extends TestCase {

	private static final String INPUT = "person";

	private static final String IMAGE = "photo";

	private ViewChannel _input;

	private ViewChannel _image;

	private ReactControl _avatar;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_input = new DefaultViewChannel(INPUT);
		_image = new DefaultViewChannel(IMAGE);
		_avatar = createAvatar(AvatarSize.MEDIUM);
	}

	/** Without a picture the avatar falls back to the initials of the input value's label. */
	public void testInitialsWithoutPicture() {
		_input.set("Jane Doe");

		assertEquals("The label of the input value is what the initials are derived from.",
			"Jane Doe", state(_avatar, ReactAvatarControl.NAME));
		assertFalse("Nothing on the picture channel leaves the initials.",
			hasData(_avatar));
		assertNull("Nothing on the picture channel names no address either.",
			state(_avatar, ImageSource.URL));
	}

	/** Picture data on the image channel is served by the control. */
	public void testPictureData() {
		_image.set(data("image/png"));

		assertTrue("A PNG is a picture.", hasData(_avatar));
	}

	/** A text on the image channel names the address the picture is loaded from. */
	public void testPictureUrl() {
		_image.set("/media/jane.png");

		assertFalse("The address is loaded by the client, not served by the control.", hasData(_avatar));
		assertEquals("/media/jane.png", state(_avatar, ImageSource.URL));
	}

	/** A value that is no picture leaves the initials. */
	public void testPictureCleared() {
		_image.set(data("image/png"));
		assertTrue(hasData(_avatar));

		_image.set(null);

		assertFalse("Without a picture the initials are shown again.", hasData(_avatar));
		assertNull("No address is left behind either.", state(_avatar, ImageSource.URL));
	}

	/** Binary data that is no picture leaves the initials. */
	public void testOtherBinaryDataIsNoPicture() {
		_image.set(data("application/pdf"));

		assertFalse("A PDF is no picture.", hasData(_avatar));
	}

	/** The picture a channel already carries when the control is created is shown as well. */
	public void testInitialPicture() {
		_image.set(data("image/png"));

		assertTrue("The value the channel already holds is displayed.", hasData(createAvatar(AvatarSize.MEDIUM)));
	}

	/** The configured diameter reaches the client. */
	public void testSize() {
		assertEquals("The avatar takes the standard diameter unless it says otherwise.",
			AvatarSize.MEDIUM.getExternalName(), state(_avatar, ReactAvatarControl.SIZE));

		assertEquals(AvatarSize.EXTRA_LARGE.getExternalName(),
			state(createAvatar(AvatarSize.EXTRA_LARGE), ReactAvatarControl.SIZE));
	}

	/** Both channels are let go of when the control is disposed. */
	public void testListenersAreRemovedOnCleanup() {
		_input.set("Jane Doe");
		_image.set("/media/jane.png");

		_avatar.cleanupTree();

		_input.set("John Doe");
		_image.set("/media/john.png");

		assertEquals("The disposed avatar no longer follows its input channel.",
			"Jane Doe", state(_avatar, ReactAvatarControl.NAME));
		assertEquals("The disposed avatar no longer follows its picture channel.",
			"/media/jane.png", state(_avatar, ImageSource.URL));
	}

	/**
	 * The control of an {@link AvatarElement} bound to both test channels.
	 *
	 * @param size
	 *        The configured diameter.
	 */
	private ReactControl createAvatar(AvatarSize size) {
		AvatarElement.Config config = TypedConfiguration.newConfigItem(AvatarElement.Config.class);
		set(config, AvatarElement.Config.INPUT, new ChannelRef(INPUT));
		set(config, AvatarElement.Config.IMAGE, new ChannelRef(IMAGE));
		set(config, AvatarElement.Config.SIZE, size);

		DefaultInstantiationContext instantiationContext = new DefaultInstantiationContext(TestAvatarElement.class);
		AvatarElement element = (AvatarElement) instantiationContext.getInstance(config);

		ViewContext context = new DefaultViewContext(new DefaultReactContext("/app", "test",
			new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		context.registerChannel(INPUT, _input);
		context.registerChannel(IMAGE, _image);

		return (ReactControl) element.createControl(context);
	}

	private static void set(AvatarElement.Config config, String property, Object value) {
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

	private static boolean hasData(ReactControl avatar) {
		return Boolean.TRUE.equals(state(avatar, ImageSource.HAS_DATA));
	}

	private static Object state(ReactControl avatar, String key) {
		String json = avatar.stateAsJSON();
		try {
			return ((Map<?, ?>) JSON.fromString(json)).get(key);
		} catch (ParseException ex) {
			throw new RuntimeException("Not a state object: " + json, ex);
		}
	}

}
