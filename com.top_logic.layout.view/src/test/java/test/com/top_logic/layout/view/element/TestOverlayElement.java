/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.image.ImageSource;
import com.top_logic.layout.react.control.layout.LayerAnchor;
import com.top_logic.layout.react.control.layout.ReactOverlayControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.element.LayerElement;
import com.top_logic.layout.view.element.OverlayElement;

/**
 * Tests how an {@link OverlayElement} splits its children into the base and the layers over it, and
 * what a {@link LayerElement} contributes to the layer it becomes.
 *
 * <p>
 * The element is exercised through its public seam - a view read the way the application reads it, a
 * control created for a view context, and the client state that control publishes.
 * </p>
 */
public class TestOverlayElement extends TestCase {

	private static final String CONTEXT_PATH = "/app";

	/** The React component of a picture, which the test uses as distinguishable content. */
	private static final String IMAGE_MODULE = "TLImage";

	/** State key naming the React component a control descriptor stands for. */
	private static final String MODULE = "module";

	/** State key holding the client state of a control descriptor. */
	private static final String STATE = "state";

	private static final String VIEW = """
			<view>
				<overlay css-class="tlHero">
					<image resource="/base.png"/>
					<layer position="top-left" css-class="tlBadge">
						<image resource="/badge.png"/>
					</layer>
					<image resource="/scrim.png"/>
				</overlay>
			</view>
			""";

	private Map<?, ?> _state;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_state = state(createOverlay(VIEW));
	}

	/** The first child is the base the layers are placed over. */
	public void testFirstChildIsBase() {
		Map<?, ?> base = (Map<?, ?>) _state.get(ReactOverlayControl.BASE);

		assertEquals("The base is the picture written first.", IMAGE_MODULE, base.get(MODULE));
		assertEquals(CONTEXT_PATH + "/base.png", ((Map<?, ?>) base.get(STATE)).get(ImageSource.URL));
	}

	/** Every child after the first is a layer, in the order it is written. */
	public void testFurtherChildrenAreLayers() {
		assertEquals("Two children follow the base.", 2, layers().size());

		assertEquals(CONTEXT_PATH + "/badge.png", contentUrl(layers().get(0)));
		assertEquals(CONTEXT_PATH + "/scrim.png", contentUrl(layers().get(1)));
	}

	/** A layer element contributes the position it takes and a CSS class of its own. */
	public void testLayerElementContributesItsPosition() {
		Map<?, ?> badge = layers().get(0);

		assertEquals(LayerAnchor.TOP_LEFT.getExternalName(), badge.get(ReactOverlayControl.ANCHOR));
		assertEquals("tlBadge", badge.get(ReactOverlayControl.LAYER_CSS_CLASS));
	}

	/** A child that is no layer element covers the base as a whole. */
	public void testOtherChildCoversTheBase() {
		Map<?, ?> scrim = layers().get(1);

		assertEquals(LayerAnchor.FILL.getExternalName(), scrim.get(ReactOverlayControl.ANCHOR));
		assertNull("A child that is no layer brings no CSS class.", scrim.get(ReactOverlayControl.LAYER_CSS_CLASS));
	}

	/** The CSS class of the overlay itself reaches the client. */
	public void testOverlayCssClass() {
		assertEquals("tlHero", _state.get(ReactControl.CSS_CLASS));
	}

	/** A layer without a position of its own covers the base as a whole. */
	public void testLayerDefaultsToCoveringTheBase() throws Exception {
		ReactControl overlay = createOverlay("""
				<view>
					<overlay>
						<image resource="/base.png"/>
						<layer>
							<image resource="/caption.png"/>
						</layer>
					</overlay>
				</view>
				""");

		Map<?, ?> caption = layers(state(overlay)).get(0);

		assertEquals(LayerAnchor.FILL.getExternalName(), caption.get(ReactOverlayControl.ANCHOR));
		assertNull("Without configuration a layer brings no CSS class.",
			caption.get(ReactOverlayControl.LAYER_CSS_CLASS));
	}

	/** An overlay with a base and nothing over it is a base. */
	public void testBaseWithoutLayers() throws Exception {
		ReactControl overlay = createOverlay("""
				<view>
					<overlay>
						<image resource="/base.png"/>
					</overlay>
				</view>
				""");

		assertEquals("Nothing is placed over the base.", List.of(), layers(state(overlay)));
	}

	/** An overlay needs at least the base it stacks content over. */
	public void testOverlayWithoutChildren() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(overlayConfig("""
				<view>
					<overlay/>
				</view>
				"""));

		List<String> errors = log.getErrors();
		assertFalse("An overlay without a base is a configuration error.", errors.isEmpty());
		assertTrue("The failure names the element: " + errors, errors.get(0).contains("overlay"));
	}

	@SuppressWarnings("unchecked")
	private static List<Map<?, ?>> layers(Map<?, ?> state) {
		return (List<Map<?, ?>>) state.get(ReactOverlayControl.LAYERS);
	}

	private List<Map<?, ?>> layers() {
		return layers(_state);
	}

	/** The address the picture of the given layer is loaded from. */
	private static Object contentUrl(Map<?, ?> layer) {
		Map<?, ?> content = (Map<?, ?>) layer.get(ReactOverlayControl.CONTENT);
		assertEquals(IMAGE_MODULE, content.get(MODULE));
		return ((Map<?, ?>) content.get(STATE)).get(ImageSource.URL);
	}

	/** The control of the overlay the given view shows. */
	private static ReactControl createOverlay(String view) throws ConfigurationException {
		DefaultInstantiationContext instantiationContext = new DefaultInstantiationContext(TestOverlayElement.class);
		UIElement element = instantiationContext.getInstance(overlayConfig(view));
		instantiationContext.checkErrors();

		ViewContext context = new DefaultViewContext(new DefaultReactContext(CONTEXT_PATH, "test",
			new SSEUpdateQueue(), new ReactWindowRegistry("test")));

		return (ReactControl) element.createControl(context);
	}

	/** The {@code overlay} configuration the given view shows. */
	private static PolymorphicConfiguration<? extends UIElement> overlayConfig(String view)
			throws ConfigurationException {
		ViewElement.Config config =
			ViewLoader.parseConfig(List.of(CharacterContents.newContent(view, "test-overlay.view.xml")));
		PolymorphicConfiguration<? extends UIElement> content = config.getContent();
		assertTrue("The view shows an overlay, not " + content, content instanceof OverlayElement.Config);
		return content;
	}

	private static Map<?, ?> state(ReactControl overlay) {
		String json = overlay.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (ParseException ex) {
			throw new RuntimeException("Not a state object: " + json, ex);
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view.
	 */
	public static Test suite() throws ModuleException {
		return ServiceTestSetup.createSetup(TestOverlayElement.class, TypeIndex.Module.INSTANCE);
	}

}
