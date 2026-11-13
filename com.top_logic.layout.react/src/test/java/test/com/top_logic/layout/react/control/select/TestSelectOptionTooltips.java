/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.select;

import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.select.SelectDisplay;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.TLI18NKey;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.resources.TLPartResourceProvider;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;

/**
 * Tests the explanation a {@link ReactDropdownSelectControl} answers as the tooltip of an option:
 * the tooltip the label provider gives the option, asked for by the option's id.
 */
public class TestSelectOptionTooltips extends BasicTestCase {

	/** The {@code loadOptions} command of {@link ReactDropdownSelectControl}. */
	private static final String CMD_LOAD_OPTIONS = "loadOptions";

	private TLClassifier _high;

	private TLClassifier _low;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.addModule(model, "test");
		TLEnumeration priority = TLModelUtil.addEnumeration(module, "Priority");

		_low = TLModelUtil.addClassifier(priority, "low");
		_low.setAnnotation(i18n(I18NConstants.PRIORITY_LOW));
		_high = TLModelUtil.addClassifier(priority, "high");
		_high.setAnnotation(i18n(I18NConstants.PRIORITY_HIGH));
	}

	/** A classifier with a description is explained by that description. */
	public void testClassifierExplainedByItsDescription() {
		ReactDropdownSelectControl select = loadedSelect(TLPartResourceProvider.INSTANCE, List.of(_low, _high));

		TooltipContent tooltip = select.getTooltipContent(optionKey(select, _high));

		String description = Resources.getInstance().getString(I18NConstants.PRIORITY_HIGH.tooltip());
		assertFalse("The test bundle must describe the classifier.", description.isEmpty());
		assertNotNull("The option with a description must have a tooltip.", tooltip);
		assertEquals(description, tooltip.getHtml());
	}

	/** A classifier without a description has no tooltip. */
	public void testClassifierWithoutDescriptionHasNoTooltip() {
		ReactDropdownSelectControl select = loadedSelect(TLPartResourceProvider.INSTANCE, List.of(_low, _high));

		assertNull(select.getTooltipContent(optionKey(select, _low)));
	}

	/** An id that names no option, and a key that names no option at all, have no tooltip. */
	public void testUnknownKeyHasNoTooltip() {
		ReactDropdownSelectControl select = loadedSelect(TLPartResourceProvider.INSTANCE, List.of(_low, _high));

		assertNull(select.getTooltipContent(ReactDropdownSelectControl.OPTION_TOOLTIP_PREFIX + "unknown"));
		assertNull(select.getTooltipContent(idOf(select, _high)));
		assertNull(select.getTooltipContent(null));
	}

	/** An option that is an object is explained by the tooltip its label provider gives it. */
	public void testObjectExplainedByItsLabelProvider() {
		ResourceProviderStub provider = new ResourceProviderStub();
		ReactDropdownSelectControl select = loadedSelect(provider, List.of("alpha", "beta"));

		TooltipContent tooltip = select.getTooltipContent(optionKey(select, "beta"));

		assertNotNull(tooltip);
		assertEquals(provider.getTooltip("beta"), tooltip.getHtml());
		assertNull("An object without a tooltip has none.", select.getTooltipContent(optionKey(select, "alpha")));
	}

	/** A label provider that answers labels only gives no option a tooltip. */
	public void testPlainLabelProviderGivesNoTooltip() {
		LabelProvider labels = String::valueOf;
		ReactDropdownSelectControl select = loadedSelect(labels, List.of("alpha", "beta"));

		assertNull(select.getTooltipContent(optionKey(select, "beta")));
	}

	/** The select of the given options, its option list loaded as the client does on opening it. */
	private static ReactDropdownSelectControl loadedSelect(LabelProvider labels, List<?> options) {
		ReactDropdownSelectControl select = new ReactDropdownSelectControl(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")),
			new SimpleSelectFieldModel(null, options, false), labels, null, false, SelectDisplay.DROPDOWN);
		select.executeClientCommand(CMD_LOAD_OPTIONS, Map.of());
		return select;
	}

	/** The tooltip key the client declares for the given option. */
	private static String optionKey(ReactDropdownSelectControl select, Object option) {
		return ReactDropdownSelectControl.OPTION_TOOLTIP_PREFIX + idOf(select, option);
	}

	/** The id the given option is sent to the client with. */
	private static String idOf(ReactDropdownSelectControl select, Object option) {
		String id = select.objectIdentities().idOf(option);
		assertNotNull("The option must have an id.", id);
		assertTrue("The option must have been sent with its id.", select.stateAsJSON().contains("\"" + id + "\""));
		return id;
	}

	private static TLI18NKey i18n(ResKey key) {
		TLI18NKey result = TypedConfiguration.newConfigItem(TLI18NKey.class);
		result.setValue(key);
		return result;
	}

	/**
	 * Labels its string options by themselves and gives every option but {@code alpha} a tooltip
	 * of markup around its label.
	 */
	private static final class ResourceProviderStub extends AbstractResourceProvider {

		@Override
		public String getLabel(Object object) {
			return String.valueOf(object);
		}

		@Override
		public String getTooltip(Object object) {
			return "alpha".equals(object) ? null : "<b>" + object + "</b>";
		}
	}

	/**
	 * The suite of tests, with the resource bundles of the option labels.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestSelectOptionTooltips.class, TypeIndex.Module.INSTANCE,
				ResourcesModule.Module.INSTANCE));
	}

}
