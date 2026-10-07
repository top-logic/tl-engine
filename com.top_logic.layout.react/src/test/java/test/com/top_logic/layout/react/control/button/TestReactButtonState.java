/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.button;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonAction;
import com.top_logic.layout.react.control.button.ButtonAppearance;
import com.top_logic.layout.react.control.button.ButtonSize;
import com.top_logic.layout.react.control.button.ButtonTone;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Erscheinung, Tönung und Größe eines {@link ReactButtonControl} landen als externe Namen im
 * Zustand — und der Standardwert lässt den Schlüssel weg, damit der Container die Vorgabe macht.
 */
public class TestReactButtonState extends TestCase {

	public void testAppearanceDefaultLeavesKeyUnset() {
		Button button = newButton();
		button.setAppearance(ButtonAppearance.DEFAULT);
		assertNull(button.appearance());
		button.setAppearance(ButtonAppearance.GHOST);
		assertEquals("ghost", button.appearance());
	}

	public void testToneDangerIsExternalName() {
		Button button = newButton();
		assertNull(button.tone());
		button.setTone(ButtonTone.DANGER);
		assertEquals("danger", button.tone());
		button.setTone(ButtonTone.DEFAULT);
		assertNull(button.tone());
	}

	public void testSizeKnowsOnlySmall() {
		assertEquals(2, ButtonSize.values().length);
		Button button = newButton();
		button.setSize(ButtonSize.SMALL);
		assertEquals("small", button.size());
	}

	private Button newButton() {
		// Wie ReactButtonControl in ReauthenticationPromptDialogControl.java:90 ff. gebaut wird.
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		return new Button(context, "Press me", ctx -> HandlerResult.DEFAULT_RESULT);
	}

	/**
	 * A {@link ReactButtonControl} exposing its server-side appearance, tone, and size state for
	 * assertions.
	 */
	private static final class Button extends ReactButtonControl {

		Button(ReactContext context, String label, ButtonAction action) {
			super(context, label, action);
		}

		Button(ReactContext context, CommandModel model) {
			super(context, model);
		}

		Object appearance() {
			return getState("appearance");
		}

		Object tone() {
			return getState("tone");
		}

		Object size() {
			return getState("size");
		}
	}

	/**
	 * A button built from a command takes the command's tone, so that a destructive command is
	 * drawn as such wherever it is offered; an ordinary command sends no tone.
	 */
	public void testToneFollowsTheModel() {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		assertEquals("danger", new Button(context, new TonedModel(ButtonTone.DANGER)).tone());
		assertNull(new Button(context, new TonedModel(ButtonTone.DEFAULT)).tone());
	}

	/**
	 * A command of the given tone, offered and executable.
	 */
	private static final class TonedModel implements CommandModel {

		private final ButtonTone _tone;

		TonedModel(ButtonTone tone) {
			_tone = tone;
		}

		@Override
		public ButtonTone getTone() {
			return _tone;
		}

		@Override
		public String getName() {
			return "toned";
		}

		@Override
		public String getLabel() {
			return "Toned";
		}

		@Override
		public ThemeImage getImage() {
			return null;
		}

		@Override
		public boolean isExecutable() {
			return true;
		}

		@Override
		public boolean isVisible() {
			return true;
		}

		@Override
		public HandlerResult perform(ReactContext context) {
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public CommandPlacement getPlacement() {
			return CommandPlacement.NONE;
		}

		@Override
		public void addStateChangeListener(Runnable listener) {
			// The tone never changes.
		}

		@Override
		public void removeStateChangeListener(Runnable listener) {
			// The tone never changes.
		}
	}

	public static Test suite() {
		return ServiceTestSetup.createSetup(TestReactButtonState.class, TypeIndex.Module.INSTANCE);
	}

}
