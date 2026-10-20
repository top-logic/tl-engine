/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.List;

import org.junit.Assume;
import org.junit.runner.RunWith;

import junit.framework.TestCase;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.core.tools.web.BrowserSession.DomainPolicy;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.InteractiveElement;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.PageView;

/**
 * Gated end-to-end tests that only <em>effectively visible</em> elements are reported as interactive,
 * and that genuinely visible ones are never filtered out.
 *
 * <p>
 * The regression: a dismissed cookie banner kept appearing in the interactive-element list. Sites do
 * not remove such a banner from the DOM - they hide the whole container (typically by animating it
 * away and then switching it to {@code display:none}) while its buttons keep their own
 * {@code display:inline-block}. Testing only the element's own computed style therefore still offered
 * the model buttons the user had already dismissed, so it believed a gone banner was still blocking
 * the page. Verified against {@code top-logic.com}'s Borlabs banner, which hides exactly this way:
 * a ~1 s flip-out animation on an ancestor, ending in {@code display:none}, with the "individual
 * settings" sub-panel additionally marked {@code aria-hidden="true"}.
 * </p>
 *
 * <p>
 * Half of these tests exist to guard against <em>over</em>-filtering, which would be the worse
 * failure: an element wrongly dropped is an element the agent cannot reach at all. In particular a
 * control with {@code opacity:0} on <em>itself</em> stays addressable, because that is the ubiquitous
 * "visually hidden but functional" idiom (a native checkbox behind a styled label), whereas
 * {@code opacity:0} on a <em>container</em> means the block is hidden.
 * </p>
 *
 * <p>
 * These launch a real Chromium (gated behind {@link BrowserE2e}) but need <b>no
 * network</b>: fixture HTML is loaded straight into the page, so each hiding mechanism can be
 * exercised in isolation. One test does hit the live {@code top-logic.com} to prove the real banner
 * is gone after it is accepted.
 * </p>
 */
@RunWith(AssumptionAwareRunner.class)
public class TestBrowserElementVisibility extends TestCase {

	/** Allow-any policy: fixtures are injected, not navigated, so the policy is irrelevant. */
	private static final DomainPolicy ALLOW_ANY = host -> true;

	private static final int TIMEOUT_MILLIS = 30_000;

	/** The production-default settle cap. */
	private static final int DEFAULT_SETTLE_MILLIS = 1500;

	/** A banner container switched to {@code display:none}, the steady state of a dismissed banner. */
	private static final String DISPLAY_NONE_CONTAINER_FIXTURE =
		"<!doctype html><html><body>"
			+ "<button id='keep'>Real button</button>"
			+ "<div id='banner' style='display:none'>"
			+ "<button style='display:inline-block'>Accept all</button>"
			+ "<a href='#'>Cookie details</a>"
			+ "</div>"
			+ "</body></html>";

	/** A banner container faded to {@code opacity:0} but still laid out. */
	private static final String TRANSPARENT_CONTAINER_FIXTURE =
		"<!doctype html><html><body>"
			+ "<button id='keep'>Real button</button>"
			+ "<div id='banner' style='opacity:0'>"
			+ "<button>Accept all</button>"
			+ "</div>"
			+ "</body></html>";

	/** A banner container marked {@code aria-hidden="true"} - the site says it is not part of the UI. */
	private static final String ARIA_HIDDEN_CONTAINER_FIXTURE =
		"<!doctype html><html><body>"
			+ "<button id='keep'>Real button</button>"
			+ "<div id='banner' aria-hidden='true'>"
			+ "<button>Accept all</button>"
			+ "</div>"
			+ "</body></html>";

	/** A container hidden with {@code visibility:hidden}, which its children inherit. */
	private static final String VISIBILITY_HIDDEN_CONTAINER_FIXTURE =
		"<!doctype html><html><body>"
			+ "<button id='keep'>Real button</button>"
			+ "<div id='banner' style='visibility:hidden'>"
			+ "<button>Accept all</button>"
			+ "</div>"
			+ "</body></html>";

	/**
	 * The "visually hidden but functional" idiom: a native checkbox made transparent and positioned
	 * behind its styled label. It is genuinely operable and must stay addressable.
	 */
	private static final String TRANSPARENT_CHECKBOX_FIXTURE =
		"<!doctype html><html><body>"
			+ "<label class='styled'>"
			+ "<input type='checkbox' name='consent' style='opacity:0;width:20px;height:20px'>"
			+ "<span>Accept marketing cookies</span>"
			+ "</label>"
			+ "</body></html>";

	/** A visible button inside an OPEN shadow root, plus a hidden sibling inside the same root. */
	private static final String SHADOW_MIXED_FIXTURE =
		"<!doctype html><html><body>"
			+ "<div id='host'></div>"
			+ "<script>"
			+ "  const root = document.getElementById('host').attachShadow({mode:'open'});"
			+ "  root.innerHTML = '<div id=\"live\"><button>Shadow visible</button></div>"
			+ "<div id=\"gone\" style=\"display:none\"><button>Shadow hidden</button></div>';"
			+ "</script>"
			+ "</body></html>";

	/**
	 * The Borlabs shape: a banner that starts fading out on load and ends fully transparent
	 * ({@code animation-fill-mode: forwards}), mutating no DOM while it does so.
	 */
	private static final String FADING_BANNER_FIXTURE =
		"<!doctype html><html><head><style>"
			+ "@keyframes tl-fade-out { from { opacity: 1; } to { opacity: 0; } }"
			+ "#banner { animation: tl-fade-out 600ms forwards; }"
			+ "</style></head><body>"
			+ "<button id='keep'>Real button</button>"
			+ "<div id='banner'><button>Accept all</button></div>"
			+ "</body></html>";

	/** A container switched to {@code display:none} keeps its buttons out of the element list. */
	public void testDisplayNoneContainerDropsItsButtons() {
		assertBannerDropped(DISPLAY_NONE_CONTAINER_FIXTURE, "display:none on the container");
	}

	/** A container faded to {@code opacity:0} keeps its buttons out of the element list. */
	public void testTransparentContainerDropsItsButtons() {
		assertBannerDropped(TRANSPARENT_CONTAINER_FIXTURE, "opacity:0 on the container");
	}

	/** An {@code aria-hidden="true"} container keeps its buttons out of the element list. */
	public void testAriaHiddenContainerDropsItsButtons() {
		assertBannerDropped(ARIA_HIDDEN_CONTAINER_FIXTURE, "aria-hidden on the container");
	}

	/** A {@code visibility:hidden} container keeps its buttons out of the element list. */
	public void testVisibilityHiddenContainerDropsItsButtons() {
		assertBannerDropped(VISIBILITY_HIDDEN_CONTAINER_FIXTURE, "visibility:hidden on the container");
	}

	/**
	 * Over-filtering guard: a transparent-but-functional native checkbox stays addressable, because
	 * {@code opacity:0} on the control <em>itself</em> is a styling idiom, not a hidden element.
	 */
	public void testTransparentCheckboxStaysAddressable() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = open(0);
		try {
			PageView view = session.loadHtmlForTest(TRANSPARENT_CHECKBOX_FIXTURE);
			// The capture derives the accessible name from the input's `name` attribute here.
			String ref = refWithName(view.elements(), "consent");
			assertNotNull("a visually-hidden-but-functional checkbox is still addressable "
				+ "(opacity:0 on the element itself is a styling idiom): " + names(view.elements()), ref);

			PageView afterClick = session.activateRefForTest(ref);
			assertTrue("and it is actually operable through its ref",
				Boolean.TRUE.equals(session.evaluateForTest(
					"() => document.querySelector('input[name=consent]').checked")));
			assertNotNull("the page is still captured after the click", afterClick);
		} finally {
			session.close();
		}
	}

	/**
	 * Over-filtering guard across shadow boundaries: a visible element in an open shadow root is kept,
	 * while a hidden one in the same root is dropped - so the composed visibility is what counts, not
	 * merely being inside a component.
	 */
	public void testShadowRootJudgedByComposedVisibility() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = open(0);
		try {
			PageView view = session.loadHtmlForTest(SHADOW_MIXED_FIXTURE);
			assertNotNull("a VISIBLE button in an open shadow root is still captured: "
				+ names(view.elements()), refWithName(view.elements(), "Shadow visible"));
			assertNull("a HIDDEN button in the same open shadow root is dropped: " + names(view.elements()),
				refWithName(view.elements(), "Shadow hidden"));
		} finally {
			session.close();
		}
	}

	/**
	 * With the settle on, a banner that is animating away is no longer captured: the settle waits out
	 * the in-flight, finite animation, and the finished animation leaves the container transparent, so
	 * the visibility test drops its buttons. The unrelated button on the page is unaffected.
	 */
	public void testFadingBannerGoneAfterSettle() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = open(DEFAULT_SETTLE_MILLIS);
		try {
			PageView view = session.loadHtmlWithSettleForTest(FADING_BANNER_FIXTURE);
			assertNull("a banner that finished animating away is not offered as clickable: "
				+ names(view.elements()), refWithName(view.elements(), "Accept all"));
			assertNotNull("the rest of the page is untouched", refWithName(view.elements(), "Real button"));
		} finally {
			session.close();
		}
	}

	/**
	 * The contrast that shows what the settle contributes: with the settle disabled, the very same page
	 * is captured mid-animation, while the banner is still fully opaque and laid out - so it IS
	 * reported. This is the state the tool used to hand the model even with the settle on, because a
	 * CSS animation mutates no DOM and the DOM-quiet wait alone resolves straight through it.
	 */
	public void testFadingBannerStillPresentWithoutSettle() {
		BrowserE2e.assumeEnabled();
		BrowserSession session = open(0);
		try {
			PageView view = session.loadHtmlWithSettleForTest(FADING_BANNER_FIXTURE);
			assertNotNull("captured mid-animation, the banner is genuinely still visible: "
				+ names(view.elements()), refWithName(view.elements(), "Accept all"));
		} finally {
			session.close();
		}
	}

	/**
	 * The real-world case, against the live site: {@code top-logic.com}'s Borlabs consent banner is
	 * offered while it is up, and is gone from the interactive elements once it has been accepted -
	 * both in the click's own result and in a subsequent re-read. Skips itself if the site does not
	 * present the banner (already consented, or the plugin changed).
	 */
	public void testLiveConsentBannerGoneAfterAcceptEndToEnd() {
		BrowserE2e.assumeEnabled();
		BrowserToolsService service = newService("top-logic.com");
		service.startUp();
		try {
			Object sessionKey = new Object();
			PageView landing = service.navigate(sessionKey, "https://www.top-logic.com/");
			String acceptRef = refWhereNameContains(landing.elements(), "akzeptieren");
			// No consent banner on this visit - nothing to dismiss, so nothing to assert. Skip rather
			// than pass on an empty check.
			Assume.assumeTrue("no consent banner on this visit - nothing to dismiss", acceptRef != null);

			PageView afterAccept = service.click(sessionKey, acceptRef);
			assertNull("the accepted banner is gone from the click's own element list: "
				+ names(afterAccept.elements()),
				refWhereNameContains(afterAccept.elements(), "akzeptieren"));

			PageView reread = service.snapshot(sessionKey);
			assertNull("and stays gone on a re-read: " + names(reread.elements()),
				refWhereNameContains(reread.elements(), "akzeptieren"));
			assertFalse("the rest of the page is still captured", reread.elements().isEmpty());
		} finally {
			service.shutDown();
		}
	}

	/**
	 * Loads {@code fixture} and asserts the banner's controls are dropped while the ordinary button
	 * next to them survives.
	 */
	private static void assertBannerDropped(String fixture, String mechanism) {
		BrowserE2e.assumeEnabled();
		BrowserSession session = open(0);
		try {
			PageView view = session.loadHtmlForTest(fixture);
			assertNull("a hidden banner's button is not offered as clickable (" + mechanism + "): "
				+ names(view.elements()), refWithName(view.elements(), "Accept all"));
			assertNotNull("the visible button beside it is kept (" + mechanism + "): "
				+ names(view.elements()), refWithName(view.elements(), "Real button"));
		} finally {
			session.close();
		}
	}

	/** Opens a fixture session with the given settle cap (ms); {@code 0} disables the settle. */
	private static BrowserSession open(int settleMillis) {
		return BrowserSession.open("visibility-" + settleMillis, TIMEOUT_MILLIS,
			BrowserSession.DEFAULT_ACTION_TIMEOUT_MILLIS, settleMillis, ALLOW_ANY, null, false, false,
			null);
	}

	private static String refWithName(List<InteractiveElement> elements, String name) {
		for (InteractiveElement element : elements) {
			if (name.equals(element.name())) {
				return element.ref();
			}
		}
		return null;
	}

	private static String refWhereNameContains(List<InteractiveElement> elements, String fragment) {
		for (InteractiveElement element : elements) {
			if (element.name() != null && element.name().toLowerCase().contains(fragment.toLowerCase())) {
				return element.ref();
			}
		}
		return null;
	}

	/** The captured accessible names, for a failure message that shows what WAS captured. */
	private static String names(List<InteractiveElement> elements) {
		StringBuilder out = new StringBuilder("captured ").append(elements.size()).append(" elements ");
		for (InteractiveElement element : elements) {
			out.append('[').append(element.ref()).append(' ').append(element.name()).append(']');
		}
		return out.toString();
	}

	/** A service allowing only {@code allowedDomain}, with production defaults otherwise. */
	private static BrowserToolsService newService(String allowedDomain) {
		BrowserToolsService.Config<?> config =
			TypedConfiguration.newConfigItem(BrowserToolsService.Config.class);
		config.update(config.descriptor().getProperty(BrowserToolsService.Config.ALLOWED_DOMAINS),
			List.of(allowedDomain));
		return new BrowserToolsService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}
}
