/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.SimpleBinaryDataValue;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.audio.ReactAudioPlayerControl;
import com.top_logic.layout.react.control.audio.ReactAudioRecorderControl;
import com.top_logic.layout.react.control.download.ReactDownloadControl;
import com.top_logic.layout.react.control.photo.ReactPhotoCaptureControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.AudioPlayerElement;
import com.top_logic.layout.view.element.AudioRecorderElement;
import com.top_logic.layout.view.element.BinaryChannelBinding;
import com.top_logic.layout.view.element.DownloadElement;
import com.top_logic.layout.view.element.PhotoCaptureElement;

/**
 * Tests the elements that record, play, capture and download binary data on a channel -
 * {@link AudioRecorderElement}, {@link AudioPlayerElement}, {@link PhotoCaptureElement} and
 * {@link DownloadElement} - and the {@link BinaryChannelBinding} they share.
 *
 * <p>
 * Each element is loaded from view XML the way the application loads its views, and its control
 * is created for a view context that holds the bound channel.
 * </p>
 */
public class TestMediaElements extends TestCase {

	private static final String INPUT = "media";

	/** The tag {@code <audio-recorder>} creates the audio recorder. */
	public void testAudioRecorder() throws ConfigurationException {
		assertTrue(createControl("audio-recorder", new DefaultViewChannel(INPUT)) instanceof ReactAudioRecorderControl);
	}

	/** The tag {@code <audio-player>} creates the audio player. */
	public void testAudioPlayer() throws ConfigurationException {
		assertTrue(createControl("audio-player", new DefaultViewChannel(INPUT)) instanceof ReactAudioPlayerControl);
	}

	/** The tag {@code <photo-capture>} creates the photo capture. */
	public void testPhotoCapture() throws ConfigurationException {
		assertTrue(createControl("photo-capture", new DefaultViewChannel(INPUT)) instanceof ReactPhotoCaptureControl);
	}

	/** The tag {@code <download>} creates the download, which follows the data on its channel. */
	public void testDownload() throws ConfigurationException {
		ViewChannel channel = new DefaultViewChannel(INPUT);
		ReactControl download = createControl("download", channel);
		assertTrue(download instanceof ReactDownloadControl);
		assertEquals("Nothing on the channel, nothing to download.", Boolean.FALSE, state(download, "hasData"));

		channel.set(data("first"));
		assertEquals("The data of the channel is offered for download.", Boolean.TRUE, state(download, "hasData"));
		assertEquals("first", state(download, "fileName"));
	}

	/** The model starts with the value the channel holds, and follows the channel. */
	public void testChannelToModel() {
		ViewChannel channel = new DefaultViewChannel(INPUT);
		BinaryData first = data("first");
		channel.set(first);
		SimpleBinaryDataValue model = new SimpleBinaryDataValue();

		BinaryChannelBinding.bind(channel, model);
		assertSame("The value of the channel is taken over.", first, model.getData());

		BinaryData second = data("second");
		channel.set(second);
		assertSame("A new value of the channel reaches the model.", second, model.getData());

		channel.set("no binary data");
		assertNull("A value that is no binary data clears the model.", model.getData());
	}

	/** What the control writes into its model reaches the channel. */
	public void testModelToChannel() {
		ViewChannel channel = new DefaultViewChannel(INPUT);
		SimpleBinaryDataValue model = new SimpleBinaryDataValue();
		BinaryChannelBinding.bind(channel, model);

		BinaryData recorded = data("recorded");
		model.setData(recorded);
		assertSame("A recording reaches the channel.", recorded, channel.get());

		model.setData(null);
		assertNull("Clearing the model clears the channel.", channel.get());
	}

	/** A value of the channel that is no binary data clears the model but stays on the channel. */
	public void testChannelKeepsOtherValue() {
		ViewChannel channel = new DefaultViewChannel(INPUT);
		SimpleBinaryDataValue model = new SimpleBinaryDataValue();
		BinaryChannelBinding.bind(channel, model);
		model.setData(data("recorded"));

		String address = "https://example/x.png";
		channel.set(address);

		assertNull("The model holds no binary data.", model.getData());
		assertEquals("A change that came from the channel is not written back.", address, channel.get());
	}

	/** Data with the same content as the channel's value leaves the channel alone. */
	public void testEqualContentKeepsChannel() {
		ViewChannel channel = new DefaultViewChannel(INPUT);
		SimpleBinaryDataValue model = new SimpleBinaryDataValue();
		BinaryChannelBinding.bind(channel, model);

		BinaryData recorded = data("recorded");
		model.setData(recorded);
		int[] notifications = { 0 };
		channel.addListener((sender, oldValue, newValue) -> notifications[0]++);

		BinaryData same = data("recorded");
		assertFalse("Data of the same content is no new value.", channel.set(same));
		model.setData(same);
		assertSame("The channel keeps its value.", recorded, channel.get());
		assertEquals("The channel reports no change.", 0, notifications[0]);
	}

	/** After the binding is disposed, neither side follows the other. */
	public void testDispose() {
		ViewChannel channel = new DefaultViewChannel(INPUT);
		SimpleBinaryDataValue model = new SimpleBinaryDataValue();
		Registration binding = BinaryChannelBinding.bind(channel, model);

		binding.dispose();

		BinaryData fromChannel = data("channel");
		channel.set(fromChannel);
		assertNull("The model no longer follows the channel.", model.getData());

		BinaryData fromModel = data("model");
		model.setData(fromModel);
		assertSame("The channel no longer follows the model.", fromChannel, channel.get());
	}

	/**
	 * The control of the element of the given tag, loaded from view XML and bound to the given
	 * channel.
	 */
	private static ReactControl createControl(String tag, ViewChannel channel) throws ConfigurationException {
		String xml = """
				<view>
					<%s input="%s"/>
				</view>
				""".formatted(tag, INPUT);
		ViewElement.Config view =
			ViewLoader.parseConfig(List.of(CharacterContents.newContent(xml, "test-media.view.xml")));

		DefaultInstantiationContext instantiationContext = new DefaultInstantiationContext(TestMediaElements.class);
		UIElement element = instantiationContext.getInstance(view.getContent());
		instantiationContext.checkErrors();

		ViewContext context = new DefaultViewContext(new DefaultReactContext("/app", "test",
			new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		context.registerChannel(INPUT, channel);

		return (ReactControl) element.createControl(context);
	}

	/**
	 * Binary data named after its content.
	 *
	 * @implNote Data of the same name have the same content and are therefore equal.
	 */
	private static BinaryData data(String name) {
		return BinaryDataFactory.createBinaryData(name.getBytes(StandardCharsets.UTF_8), "audio/webm", name);
	}

	private static Object state(ReactControl control, String key) {
		String json = control.stateAsJSON();
		try {
			return ((Map<?, ?>) JSON.fromString(json)).get(key);
		} catch (ParseException ex) {
			throw new RuntimeException("Not a state object: " + json, ex);
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view,
	 * and the {@link ApplicationConfig}, from which the recorder and the photo capture take the
	 * upload limit they announce to the client.
	 */
	public static Test suite() throws ModuleException {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestMediaElements.class, TypeIndex.Module.INSTANCE,
				ApplicationConfig.Module.INSTANCE));
	}

}
