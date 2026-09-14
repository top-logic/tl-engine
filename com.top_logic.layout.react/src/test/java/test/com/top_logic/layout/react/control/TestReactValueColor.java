/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactValueColor;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.ui.TLColor;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;

/**
 * Tests that the option descriptors a {@link ReactDropdownSelectControl} sends carry the color the
 * model gives an option, and carry none for an option the model gives no color.
 *
 * <p>
 * The client decides from that one field whether to draw a value as a pill or as plain text, so an
 * option whose descriptor lost its color is a value displayed as if the model said nothing about
 * it.
 * </p>
 */
public class TestReactValueColor extends BasicTestCase {

	private static final String TOKEN = "support-success";

	private static final Color LITERAL = new Color(0x04, 0xA3, 0x8D);

	/** The {@code loadOptions} command of {@link ReactDropdownSelectControl}. */
	private static final String CMD_LOAD_OPTIONS = "loadOptions";

	private static final LabelProvider LABELS = String::valueOf;

	private TLClassifier _colored;

	private TLClassifier _literal;

	private TLClassifier _plain;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.addModule(model, "test");
		TLEnumeration status = TLModelUtil.addEnumeration(module, "Status");

		_colored = TLModelUtil.addClassifier(status, "closed");
		_colored.setAnnotation(color(null, TOKEN));
		_literal = TLModelUtil.addClassifier(status, "blocked");
		_literal.setAnnotation(color(LITERAL, null));
		_plain = TLModelUtil.addClassifier(status, "open");
	}

	/** A literal annotated with a design token reaches the client as the CSS referencing it. */
	public void testTheColorOfAnOptionIsSent() {
		String state = optionsAsJSON(_colored, _plain);

		assertTrue("The token color must reach the client: " + state,
			state.contains("\"" + ReactValueColor.COLOR + "\":\"var(--" + TOKEN + ")\""));
	}

	/** ...and a literal color reaches it as that color. */
	public void testALiteralColorIsSentAsThatColor() {
		String state = optionsAsJSON(_literal, _plain);

		assertTrue("The literal color must reach the client: " + state,
			state.contains("\"" + ReactValueColor.COLOR + "\":\"#04A38D\""));
	}

	/**
	 * An option the model gives no color names none, rather than naming a fallback the client would
	 * draw a pill from.
	 */
	public void testAnUncoloredOptionNamesNoColor() {
		String state = optionsAsJSON(_plain);

		assertFalse("An uncolored option must name no color at all: " + state,
			state.contains("\"" + ReactValueColor.COLOR + "\""));
	}

	/**
	 * The color travels with the selected value too, not only with the options: the read-only
	 * display and the chips of the selection are drawn from the value descriptors.
	 */
	public void testTheColorOfTheSelectedValueIsSent() {
		String state = createSelect(_colored, List.of(_colored, _plain)).stateAsJSON();

		assertTrue("The value descriptor must carry the color even before the options load: " + state,
			state.contains("\"" + ReactValueColor.COLOR + "\":\"var(--" + TOKEN + ")\""));
	}

	/**
	 * The state after the client has asked for the options, so that the option descriptors - not
	 * only those of the selected value - are part of it.
	 */
	private String optionsAsJSON(Object... options) {
		ReactDropdownSelectControl select = createSelect(null, List.of(options));
		select.executeClientCommand(CMD_LOAD_OPTIONS, Map.of());
		return select.stateAsJSON();
	}

	private ReactDropdownSelectControl createSelect(Object value, List<?> options) {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue());
		return new ReactDropdownSelectControl(context,
			new SimpleSelectFieldModel(value, options, false), LABELS, null, false);
	}

	private static TLColor color(Color value, String token) {
		TLColor result = TypedConfiguration.newConfigItem(TLColor.class);
		result.setValue(value);
		result.setToken(token);
		return result;
	}

	/**
	 * The suite of tests.
	 *
	 * <p>
	 * The control asks {@link Resources} for the label of its empty option, which the default
	 * services of the test setup provide.
	 * </p>
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(new TestSuite(TestReactValueColor.class),
				ResourcesModule.Module.INSTANCE));
	}

}
