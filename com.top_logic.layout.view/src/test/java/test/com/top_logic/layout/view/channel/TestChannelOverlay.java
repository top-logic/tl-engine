/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelConfig;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DerivedChannelConfig;
import com.top_logic.layout.view.channel.ValueChannelConfig;
import com.top_logic.model.search.expr.config.ExprFormat;

/**
 * Tests how the channels of a view and of its same-path overlays are merged into one view.
 *
 * <p>
 * The views are read through {@link ViewLoader#parseConfig(List)}, the merge the view loader applies
 * to all module copies of a view path: the base view first, then each overlay.
 * </p>
 */
public class TestChannelOverlay extends TestCase {

	private static final String BASE_FILE = "base.view.xml";

	private static final String OVERLAY_FILE = "overlay.view.xml";

	/** The channel the base view declares and the overlays redefine. */
	private static final String ACCOUNT = "account";

	/** A channel the base view's derived channel reads. */
	private static final String SOURCE = "source";

	/** A channel only an overlay declares. */
	private static final String EXTRA = "extra";

	/** A string literal marking the expression of the base view. */
	private static final String BASE_VALUE = "baseValue";

	/** A string literal marking the expression of an overlay. */
	private static final String OVERLAY_VALUE = "overlayValue";

	/** A base view with a value channel and a derived channel {@link #ACCOUNT} reading it. */
	private static final String BASE_VIEW = """
			<view>
				<channels>
					<channel name="%1$s"/>
					<derived-channel name="%2$s" inputs="%1$s" expr="x -> '%3$s'"/>
				</channels>
			</view>
			""".formatted(SOURCE, ACCOUNT, BASE_VALUE);

	/**
	 * An overlay that declares a channel of a name the base declares, with an expression of its
	 * own, is merged into that channel: the view keeps one channel of the name, computed by the
	 * overlay's expression.
	 */
	public void testOverlayRedefinesChannel() throws ConfigurationException {
		ViewElement.Config view = merge(BASE_VIEW, """
				<view>
					<channels>
						<derived-channel name="%s" expr="x -> '%s'"/>
					</channels>
				</view>
				""".formatted(ACCOUNT, OVERLAY_VALUE));

		assertEquals("Redefining a channel does not add one.", List.of(SOURCE, ACCOUNT), names(view));
		DerivedChannelConfig account = (DerivedChannelConfig) channel(view, ACCOUNT);
		assertExpr(OVERLAY_VALUE, account);
		assertEquals("A property the overlay leaves unset keeps the value of the base.",
			List.of(new ChannelRef(SOURCE)), account.getInputs());
	}

	/**
	 * An overlay marking its channel with {@code config:override} replaces the channel of the base
	 * as a whole: nothing of the base declaration remains.
	 */
	public void testOverlayReplacesChannel() throws ConfigurationException {
		ViewElement.Config view = merge(BASE_VIEW, """
				<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
					<channels>
						<derived-channel name="%s" expr="x -> '%s'" config:override="true"/>
					</channels>
				</view>
				""".formatted(ACCOUNT, OVERLAY_VALUE));

		assertEquals(List.of(SOURCE, ACCOUNT), names(view));
		DerivedChannelConfig account = (DerivedChannelConfig) channel(view, ACCOUNT);
		assertExpr(OVERLAY_VALUE, account);
		assertEquals("A replaced channel keeps no input of the base.", List.of(), account.getInputs());
	}

	/** An overlay turns a value channel of the base into a derived channel of the same name. */
	public void testOverlayRedefinesValueChannelAsDerived() throws ConfigurationException {
		ViewElement.Config view = merge("""
				<view>
					<channels>
						<channel name="%s"/>
					</channels>
				</view>
				""".formatted(ACCOUNT), """
				<view>
					<channels>
						<derived-channel name="%s" expr="x -> '%s'"/>
					</channels>
				</view>
				""".formatted(ACCOUNT, OVERLAY_VALUE));

		assertEquals(List.of(ACCOUNT), names(view));
		ChannelConfig account = channel(view, ACCOUNT);
		assertTrue("The overlay's kind of channel wins, not " + account, account instanceof DerivedChannelConfig);
		assertExpr(OVERLAY_VALUE, (DerivedChannelConfig) account);
	}

	/** An overlay turns a derived channel of the base into a value channel of the same name. */
	public void testOverlayRedefinesDerivedChannelAsValue() throws ConfigurationException {
		ViewElement.Config view = merge(BASE_VIEW, """
				<view>
					<channels>
						<channel name="%s"/>
					</channels>
				</view>
				""".formatted(ACCOUNT));

		assertEquals(List.of(SOURCE, ACCOUNT), names(view));
		ChannelConfig account = channel(view, ACCOUNT);
		assertTrue("The overlay's kind of channel wins, not " + account, account instanceof ValueChannelConfig);
	}

	/** A channel of a name the base does not declare is appended; the base channels stay as they are. */
	public void testOverlayAddsChannel() throws ConfigurationException {
		ViewElement.Config view = merge(BASE_VIEW, """
				<view>
					<channels>
						<channel name="%s"/>
					</channels>
				</view>
				""".formatted(EXTRA));

		assertEquals(List.of(SOURCE, ACCOUNT, EXTRA), names(view));
		assertTrue(channel(view, SOURCE) instanceof ValueChannelConfig);
		assertExpr(BASE_VALUE, (DerivedChannelConfig) channel(view, ACCOUNT));
		assertTrue(channel(view, EXTRA) instanceof ValueChannelConfig);
	}

	/** Two channels of the same name in one view are a configuration error. */
	public void testDuplicateChannelNameInOneView() {
		String view = """
				<view>
					<channels>
						<channel name="%1$s"/>
						<derived-channel name="%1$s" expr="x -> '%2$s'"/>
					</channels>
				</view>
				""".formatted(ACCOUNT, BASE_VALUE);
		try {
			ViewLoader.parseConfig(List.of(CharacterContents.newContent(view, BASE_FILE)));
			fail("A view declaring two channels of the same name must not load.");
		} catch (ConfigurationException expected) {
			assertTrue("The failure names the duplicate channel: " + expected.getMessage(),
				expected.getMessage().contains(ACCOUNT));
		}
	}

	/** The view the given base and overlay merge to. */
	private static ViewElement.Config merge(String base, String overlay) throws ConfigurationException {
		return ViewLoader.parseConfig(List.of(
			CharacterContents.newContent(base, BASE_FILE),
			CharacterContents.newContent(overlay, OVERLAY_FILE)));
	}

	/** The names of the channels the given view declares, in order. */
	private static List<String> names(ViewElement.Config view) {
		return view.getChannels().stream().map(ChannelConfig::getName).toList();
	}

	/** The channel of the given name in the given view. */
	private static ChannelConfig channel(ViewElement.Config view, String name) {
		return view.getChannels().stream()
			.filter(channel -> channel.getName().equals(name))
			.findFirst()
			.orElseThrow(() -> new AssertionError("No channel '" + name + "' in " + names(view)));
	}

	/** Asserts that the expression of the given channel computes the given string literal. */
	private static void assertExpr(String expectedLiteral, DerivedChannelConfig channel) {
		String expr = ExprFormat.INSTANCE.getSpecification(channel.getExpr());
		assertTrue("Channel '" + channel.getName() + "' is computed by '" + expr + "', expected one yielding '"
			+ expectedLiteral + "'.", expr.contains(expectedLiteral));
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view.
	 */
	public static Test suite() throws ModuleException {
		return ServiceTestSetup.createSetup(TestChannelOverlay.class, TypeIndex.Module.INSTANCE);
	}

}
