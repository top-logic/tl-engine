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
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonAction;
import com.top_logic.layout.react.control.button.ButtonAppearance;
import com.top_logic.layout.react.control.button.ButtonSize;
import com.top_logic.layout.react.control.button.ButtonTone;
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

	public static Test suite() {
		return ServiceTestSetup.createSetup(TestReactButtonState.class, TypeIndex.Module.INSTANCE);
	}

}
