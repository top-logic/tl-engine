/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.navigation.DisplayTargets;
import com.top_logic.layout.view.navigation.ObjectNavigation;
import com.top_logic.layout.view.navigation.ShowViewAction;
import com.top_logic.layout.view.navigation.ShowViewsAction;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link ShowViewAction} and {@link ShowViewsAction} displaying the views a command chain
 * names, with the values they compute from what the chain carries.
 *
 * <p>
 * The chain's value is a text here, which nothing declares a display target for: what is displayed
 * and what its channels receive is decided by the action alone.
 * </p>
 */
public class TestShowViewAction extends AbstractNavigationTest {

	/** The value the command chain carries. */
	private static final String INPUT = "alpha";

	/** The values the chains of this test settled with. */
	private final List<Object> _completions = new ArrayList<>();

	/**
	 * Displaying a view opens the tab holding it and writes the values its bindings compute.
	 */
	public void testShowViewWritesItsBindings() throws ConfigurationException {
		run(action("""
			<show-view view="nav-item.view.xml">
				<bind channel="item"
					expr="s -> $s + '!'"
				/>
				<bind channel="raw"/>
			</show-view>
			"""));

		assertEquals("The tab holding the view is displayed.", TAB_SECOND, activeTab());
		ViewContext instance = instanceOf(ITEM_VIEW, TAB_SECOND);
		assertEquals("The channel received what its expression computed from the chain's value.",
			INPUT + "!", channelOf(instance, ITEM_CHANNEL));
		assertEquals("Without an expression, the chain's value is what the channel receives.",
			INPUT, channelOf(instance, RAW_CHANNEL));
		assertEquals("The chain continued with the value it had, not with what was displayed.",
			List.of(INPUT), _completions);
	}

	/**
	 * A view that only a drilled-down frame holds is revealed on its tab of the frame.
	 *
	 * <p>
	 * The frame is pushed by the show before it, and the view sits at no place within the window,
	 * so it is found solely by looking within that frame.
	 * </p>
	 */
	public void testShowViewsFindsTheViewInsideTheFrame() throws ConfigurationException {
		run(action("""
			<show-views>
				<show view="nav-home.view.xml"/>
				<show view="nav-frame.view.xml"/>
				<show view="nav-detail.view.xml">
					<bind channel="item"/>
				</show>
			</show-views>
			"""));

		assertEquals("The tab holding the stack is displayed.", TAB_STACK, activeTab());
		assertEquals("The frame alone was pushed: the view it holds was revealed within it, not"
			+ " drilled down to as a further frame.", List.of(FRAME_VIEW), pushedViews());
		assertEquals("The tab of the frame holding the view is displayed.",
			FRAME_TAB_INNER, frameActiveTab());
		assertEquals("The view on that tab received the chain's value.",
			INPUT, itemOf(frameInstance(DETAIL_VIEW, FRAME_TAB_INNER)));
		assertEquals("The chain continued with the value it had.", List.of(INPUT), _completions);
	}

	/**
	 * A binding naming a channel the view does not declare is reported.
	 */
	public void testUnknownChannelIsReported() throws ConfigurationException {
		ViewAction action = action("""
			<show-view view="nav-item.view.xml">
				<bind channel="missing"/>
			</show-view>
			""");

		try {
			run(action);
			fail("The view declares no channel 'missing'.");
		} catch (TopLogicException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("missing"));
		}
	}

	/**
	 * Displaying views needs a view to display.
	 */
	public void testShowViewsNeedsAView() {
		assertRejected("<show-views/>", ShowViewsAction.Config.SHOWS);
	}

	/**
	 * Displaying a view needs the view to display.
	 */
	public void testShowViewNeedsItsView() {
		assertRejected("<show-view/>", ShowViewAction.Config.VIEW);
	}

	/**
	 * Displaying an object where its type belongs still needs a model object.
	 */
	public void testShowObjectNeedsAModelObject() {
		try {
			ObjectNavigation.show(_root, new DisplayTargets(List.of(), () -> null), INPUT, new Recorder());
			fail("A text has no type declaring where it is displayed.");
		} catch (TopLogicException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(INPUT));
		}
	}

	/**
	 * Asserts that the given action configuration is rejected, naming the property it lacks.
	 */
	private void assertRejected(String xml, String property) {
		try {
			action(xml);
			fail("The configuration '" + xml + "' lacks the property '" + property + "'.");
		} catch (ConfigurationException | RuntimeException expected) {
			String message = String.valueOf(expected.getMessage());
			assertTrue(message, message.toLowerCase().contains(property));
		}
	}

	/**
	 * Runs the given action as the only action of a command chain over the window's root display.
	 */
	private void run(ViewAction action) {
		ViewActionChain.run(_root, List.of(action), INPUT, _completions::add);
	}

	/**
	 * The action the given configuration describes.
	 *
	 * @param xml
	 *        The action as it is written within a command.
	 * @return The configured action, ready to run in a chain.
	 */
	private static ViewAction action(String xml) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(ShowViewAction.Config.TAG_NAME,
			TypedConfiguration.getConfigurationDescriptor(ShowViewAction.Config.class));
		descriptors.put(ShowViewsAction.Config.TAG_NAME,
			TypedConfiguration.getConfigurationDescriptor(ShowViewsAction.Config.class));

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestShowViewAction.class);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml, "action"));
		PolymorphicConfiguration<?> config = (PolymorphicConfiguration<?>) reader.read();
		context.checkErrors();

		ViewAction result = (ViewAction) context.getInstance(config);
		context.checkErrors();
		return result;
	}

	/**
	 * Test suite requiring a knowledge base and the application model, which the TL-Script of a
	 * binding is compiled against.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestShowViewAction.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}
}
