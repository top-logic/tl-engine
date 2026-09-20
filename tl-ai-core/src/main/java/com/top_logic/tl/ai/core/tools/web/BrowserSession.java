/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.Proxy;
import com.microsoft.playwright.options.WaitUntilState;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.BlockSignal;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.CaptchaSignal;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.InteractiveElement;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.PageView;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.ScreenshotCapture;

/**
 * One isolated, interactive browser session: its own {@link Playwright} instance, {@link Browser},
 * {@link BrowserContext} (separate cookies/storage/cache) and a single {@link Page}, all driven
 * from one dedicated thread.
 *
 * <p>
 * <b>Why a dedicated thread and a dedicated {@link Playwright} per session.</b> Playwright for
 * Java is not thread-safe: every object created by a {@code Playwright} instance must be used only
 * from the thread that created it, because the Java client and the driver share a single pipe that
 * the calling thread pumps. Driving two contexts of one shared {@code Playwright} from two threads
 * concurrently corrupts that pipe. To let independent sessions run <em>truly</em> concurrently
 * (the whole point of dropping the former global lock) each session therefore gets its own
 * {@code Playwright}/{@code Browser} confined to its own single-threaded executor. Sessions are
 * capped by {@link BrowserSessionPool}, which bounds total memory. (A denser design - many
 * contexts per browser process behind one dispatch thread each - is a possible later optimization;
 * it trades memory for the added complexity of an actor per process.)
 * </p>
 *
 * <p>
 * Every call is submitted to the session thread with a hard outer deadline so a hung page can
 * never pin its slot indefinitely; on timeout the session is considered broken and torn down by
 * the pool.
 * </p>
 *
 * <p>
 * <b>Security layering.</b> The outermost and authoritative layer is a filtering forward proxy
 * ({@link FilteringProxy}) the browser is pinned to via {@code --proxy-server}: every connection
 * Chromium makes - top-level navigation, redirect hop, sub-resource, IP-literal target - is
 * SSRF-checked and policy-checked there <em>before a packet leaves the machine</em>. Inside the
 * browser the same policy is applied again by a per-context request route, so an off-policy request
 * is refused before it even leaves the process; and a top-level hop reached by a <em>server-side
 * redirect</em> (which Playwright's routing cannot intercept) is recorded by
 * {@link #installRedirectGuard} and, with the final-URL re-validation, fails the operation - which is
 * what turns the proxy's refusal into the tool's own clean policy message. So no off-policy origin is
 * ever contacted, and no off-policy page is ever handed to the agent.
 * </p>
 *
 * <p>
 * Failures are reported in the tool's own vocabulary, never as raw Playwright text: a navigation the
 * policy refused raises {@link BrowserPolicyException}, a page that never loaded raises
 * {@link BrowserNavigationTimeoutException}, and an element that cannot be acted on within the
 * bounded action timeout raises {@link BrowserRefNotActionableException}.
 * </p>
 */
final class BrowserSession {

	/** The domain policy for the top-level origin; SSRF is enforced separately and always. */
	interface DomainPolicy {
		/** Whether a top-level navigation to {@code host} is permitted (blocklist/allowlist/allow-any). */
		boolean isNavigationPermitted(String host);

		/**
		 * Why a file download from {@code host} is refused, or {@code null} when it is permitted.
		 *
		 * <p>
		 * A second axis rather than a second answer to the first question: an origin worth reading is
		 * not automatically an origin worth accepting a file from, so a policy may permit the
		 * navigation and refuse the download. The default permits every download, which is what an
		 * installation that configures no browsing policy has always done.
		 * </p>
		 *
		 * @param host
		 *        The host the download comes from.
		 */
		default String refuseDownload(String host) {
			return null;
		}
	}

	/**
	 * JavaScript run on every capture: tags interactive elements with a {@code data-tl-ref}
	 * attribute (the ref the model acts on) and reports any human-verification challenge. The ref is
	 * generation-tagged - {@code s<gen>e<index>}, where {@code gen} is the passed-in snapshot
	 * generation - so a ref carried over from an earlier snapshot can be detected as stale instead of
	 * silently matching whatever element now occupies that index. Returns
	 * {@code {captcha: string|null, elements: [{ref, role, tag, type, name}]}}.
	 *
	 * <p>
	 * <b>Shadow DOM.</b> Element collection recursively descends into <em>open</em> shadow roots
	 * ({@code element.shadowRoot}), and nested shadow roots within them, so interactive elements
	 * inside web components (cookie-consent banners such as Usercentrics, and much modern
	 * component-based UI) are tagged and become clickable - a plain {@code document.querySelectorAll}
	 * does not cross shadow boundaries, which previously left such elements visible in the ARIA
	 * snapshot yet unaddressable. Once tagged, a {@code [data-tl-ref]} CSS locator resolves them
	 * because Playwright's CSS engine pierces open shadow roots. <b>Closed</b> shadow roots
	 * ({@code element.shadowRoot} is {@code null}) and canvas-rendered widgets expose no DOM to
	 * traverse and remain unreachable by any DOM approach - a known limit, to be addressed by a
	 * future vision-based fallback.
	 * </p>
	 *
	 * <p>
	 * <b>Element visibility.</b> Only <em>effectively visible</em> elements are reported, because
	 * modern UI hides things without removing them from the DOM: a dismissed cookie banner is
	 * typically left in place with the whole container switched to {@code display:none} (or faded to
	 * {@code opacity:0}, or marked {@code aria-hidden="true"}) while its buttons keep their own
	 * {@code display:inline-block}. Testing only the element's own computed style therefore kept
	 * reporting a banner the user had already dismissed as clickable. The test is consequently
	 * <em>ancestor-aware</em>: an element is dropped when the standard, ancestor-aware
	 * {@code Element.checkVisibility()} rejects it (covers {@code display:none} /
	 * {@code visibility:hidden} anywhere up the tree, plus {@code content-visibility} skipping), when
	 * it has no client rects (zero-area, or laid out in a hidden subtree), or when any ancestor - and
	 * for {@code aria-hidden} the element itself - is marked {@code aria-hidden="true"} or faded to
	 * {@code opacity:0}. The ancestor walk crosses open shadow boundaries via the root's {@code host},
	 * so a component's hidden inner content is judged by its real, composed visibility.
	 * </p>
	 *
	 * <p>
	 * {@code opacity:0} is deliberately disqualifying only on an <em>ancestor</em>, never on the
	 * element itself: a fully transparent <em>container</em> is hidden, whereas a fully transparent
	 * <em>control</em> is the ubiquitous "visually hidden but functional" idiom (a native checkbox or
	 * file input behind a styled label) that must stay addressable. Elements are judged as rendered,
	 * so a control that is genuinely visible - including one inside an open shadow root - is
	 * unaffected.
	 * </p>
	 *
	 * <h2>One view of one document</h2>
	 *
	 * <p>
	 * The script also returns the page's <b>URL</b> and its <b>serialized HTML</b>, although
	 * Playwright offers both directly ({@code page.url()}, {@code page.content()}). It does so
	 * because those are separate round trips to the browser, and a document can commit between any
	 * two of them: a click whose handler navigates a beat later would then be answered with the URL
	 * of the page it started on beside the HTML of the page it ended on, and with refs belonging to
	 * one of the two - a page view that is internally inconsistent, and whose {@code URL:} line is
	 * simply a wrong fact. No timeout can prevent that, because it is a race between round trips
	 * rather than a wait that is too short; reading all of it in one synchronous evaluation prevents
	 * it by construction, and leaves {@link #awaitNavigationCommit()}'s bound as the latency question
	 * it is meant to be.
	 * </p>
	 *
	 * <p>
	 * The HTML is assembled as Playwright assembles it, doctype included, so it stays the same string
	 * {@code page.content()} returned. The {@code data-tl-ref} attributes this script sets are part of
	 * it, exactly as before - they are written before the serialization, in the same evaluation.
	 * </p>
	 */
	private static final String CAPTURE_SCRIPT =
		"(gen) => {\n"
			+ "  const captchaSelectors = ['.g-recaptcha','iframe[src*=\"recaptcha\"]',"
			+ "'iframe[src*=\"hcaptcha\"]','.h-captcha','.cf-turnstile','iframe[src*=\"turnstile\"]',"
			+ "'iframe[title*=\"captcha\" i]','#challenge-form','#cf-challenge-running','[data-sitekey]'];\n"
			+ "  let captcha = null;\n"
			+ "  for (const s of captchaSelectors) { if (document.querySelector(s)) { captcha = s; break; } }\n"
			+ "  if (!captcha) {\n"
			+ "    const t = ((document.body && document.body.innerText) || '').toLowerCase();\n"
			+ "    const phrases = [\"i'm not a robot\",'i am not a robot','verify you are human',"
			+ "'are you a robot','checking your browser','please verify','complete the captcha',"
			+ "'unusual traffic'];\n"
			+ "    for (const p of phrases) { if (t.includes(p)) { captcha = 'text-challenge'; break; } }\n"
			+ "  }\n"
			+ "  const sel = 'a[href], button, input:not([type=\"hidden\"]), textarea, select,"
			+ " [role=\"button\"], [role=\"link\"], [role=\"textbox\"], [role=\"checkbox\"],"
			+ " [role=\"radio\"], [role=\"tab\"], [role=\"menuitem\"], [contenteditable=\"\"],"
			+ " [contenteditable=\"true\"]';\n"
			// Collect matches from a root plus every open shadow root nested under it (any depth).
			// querySelectorAll does not cross shadow boundaries, so each element is visited in exactly
			// one root context (no duplicates), and el.shadowRoot is null for closed roots and plain
			// elements alike, so the recursion naturally stops there.
			+ "  const collect = (root, out) => {\n"
			+ "    for (const el of root.querySelectorAll(sel)) out.push(el);\n"
			+ "    for (const el of root.querySelectorAll('*')) {\n"
			+ "      if (el.shadowRoot) collect(el.shadowRoot, out);\n"
			+ "    }\n"
			+ "  };\n"
			+ "  const els = [];\n"
			+ "  collect(document, els);\n"
			// Effectively-hidden test - see the ELEMENT VISIBILITY javadoc paragraph. Ancestor-aware,
			// so an element left in the DOM inside a hidden container is dropped.
			+ "  const hidden = (el) => {\n"
			+ "    if (typeof el.checkVisibility === 'function'\n"
			+ "        && !el.checkVisibility({visibilityProperty: true, contentVisibilityAuto: true})) {\n"
			+ "      return true;\n"
			+ "    }\n"
			+ "    if (el.getClientRects().length === 0) return true;\n"
			+ "    for (let p = el; p; p = p.parentElement || (p.parentNode && p.parentNode.host)) {\n"
			+ "      if (p.nodeType !== 1) continue;\n"
			+ "      if (p.getAttribute('aria-hidden') === 'true') return true;\n"
			+ "      const cs = window.getComputedStyle(p);\n"
			+ "      if (!cs) continue;\n"
			+ "      if (cs.display === 'none' || cs.visibility === 'hidden' || cs.visibility === 'collapse') {\n"
			+ "        return true;\n"
			+ "      }\n"
			// opacity:0 on an ANCESTOR means the whole block is hidden; opacity:0 on the element
			// itself is the standard visually-hidden-but-functional idiom (a native checkbox behind a
			// styled label), so it must stay addressable.
			+ "      if (p !== el && parseFloat(cs.opacity) === 0) return true;\n"
			+ "    }\n"
			+ "    return false;\n"
			+ "  };\n"
			+ "  const out = [];\n"
			+ "  let i = 0;\n"
			+ "  for (const el of els) {\n"
			+ "    if (hidden(el)) continue;\n"
			+ "    if (++i > 400) break;\n"
			+ "    const ref = 's' + gen + 'e' + i;\n"
			+ "    el.setAttribute('data-tl-ref', ref);\n"
			+ "    const tag = el.tagName.toLowerCase();\n"
			+ "    const role = el.getAttribute('role') || tag;\n"
			+ "    let name = el.getAttribute('aria-label') || el.getAttribute('placeholder')"
			+ " || (el.innerText || '').trim() || el.getAttribute('value')"
			+ " || el.getAttribute('name') || el.getAttribute('title') || '';\n"
			+ "    name = name.replace(/\\s+/g, ' ').trim().slice(0, 160);\n"
			+ "    const type = el.getAttribute('type') || '';\n"
			+ "    out.push({ref, role, tag, type, name});\n"
			+ "  }\n"
			// The URL and the serialized document are read HERE, in the same synchronous evaluation
			// that minted the refs, so all three describe one document - see the "single view" note
			// on this constant.
			+ "  const doctype = document.doctype\n"
			+ "    ? new XMLSerializer().serializeToString(document.doctype) : '';\n"
			+ "  const html = doctype + document.documentElement.outerHTML;\n"
			+ "  return {captcha, elements: out, url: document.location.href, html};\n"
			+ "}";

	/**
	 * Debounce window of the post-navigation settle: the page is considered "quiet" once no DOM
	 * mutation has been observed for this many milliseconds. A small internal constant (not a
	 * separate config knob) - it only decides how long a still page waits before it is declared
	 * settled, whereas the operator-tunable hard cap ({@code navigation-settle-millis}) bounds the
	 * worst case for a page that never goes quiet.
	 */
	private static final int DOM_QUIET_DEBOUNCE_MILLIS = 500;

	/**
	 * How long a {@link #click} waits, after the click itself landed, to find out whether it started
	 * a top-level navigation - the <em>first</em> of the two windows in which
	 * {@link #_navigationStarted} may still be raised by the browser.
	 *
	 * <p>
	 * A click that navigates does so by issuing the main-frame request essentially with the click
	 * itself (a link, a form submit) or from the handler it invoked, so half a second is a generous
	 * window; a slow <em>server</em> does not lengthen it, because the observed event is the request
	 * going out, not the response coming back. It is a small internal constant rather than a config
	 * knob: it does not bound any work, it only decides how long the session listens before it
	 * concludes that the click stayed on the page - and that conclusion is what saves the click from
	 * the page-load-sized waits.
	 * </p>
	 *
	 * <p>
	 * <b>The combined window.</b> A navigation a handler starts <em>later</em> -
	 * {@code setTimeout(() => location.href = …)}, or
	 * {@code fetch('/api/login').then(() => location.href = …)} against a backend that takes about a
	 * second - misses this window and is caught by the second look {@link #settleAfterClick()} takes
	 * after the DOM-quiet wait. The window is therefore this constant plus however long that wait
	 * runs: about a second on a page whose DOM goes still at once, and up to this constant plus the
	 * settle cap (so about two seconds at the default 1500 ms) on a page that keeps mutating - which
	 * is the page class this whole settle exists for. Inside the window the click waits for the new
	 * document to commit ({@link #settleAfterNavigatingClick()}) and then settles it, so what comes
	 * back is the new page, settled. Should that commit be slower than the wait allows, the capture is
	 * still self-consistent rather than torn - {@link #CAPTURE_SCRIPT} reads the URL, the HTML and the
	 * refs in one evaluation - so the worst case is a page view of whichever document was there, never
	 * one document's URL beside another's content.
	 * </p>
	 *
	 * <p>
	 * <b>What falls outside it.</b> A navigation that starts after that is not waited for: the click
	 * reports the page it was on, and the model's next {@code browserSnapshot} shows the new one. That
	 * is a deliberate residual - the alternative is to keep every same-page click waiting for a
	 * navigation that will never come, which is the defect this replaced. What it costs is that the
	 * page generation is not advanced either, so a ref the model kept from before that click is still
	 * accepted although the page has since moved on. That exposure is not particular to a click: a
	 * page that hops on a timer does the same to a {@code browserSnapshot} or a {@code browserType}
	 * that returned a moment earlier, and always has. It is why a ref is documented as valid only
	 * against the most recent page text.
	 * </p>
	 *
	 * <p>
	 * <b>Known limitations of the flag, all cost-only.</b> A {@code target="_blank"} popup's own
	 * main-frame request satisfies the observer's test too ({@link #installNavigationObserver} is
	 * installed on the context, and a popup has no parent frame), so such a click is treated as a
	 * navigating one: it waits out the whole commit wait - this session's own page never commits
	 * anything - and then the DOM-quiet stage, about two seconds at the default cap where a same-page
	 * click would have cost one. And {@link #beginNavigationAttempt()} clears the flag
	 * without draining the transport queue, so a navigation event left over from the previous
	 * operation can be attributed to this one - the same one-message seam
	 * {@link #_offPolicyNavigation} has always had. Both cost a bounded settle and neither can make a
	 * capture wrong.
	 * </p>
	 */
	private static final int CLICK_NAVIGATION_DETECTION_MILLIS = 500;

	/**
	 * JavaScript run right after the network/load wait and before {@link #CAPTURE_SCRIPT}, to give
	 * late-injected content (cookie-consent banners, lazy widgets) time to appear in the DOM before
	 * the snapshot. It resolves when the DOM has been <em>quiet</em> - no {@code childList}/subtree
	 * mutation for {@link #DOM_QUIET_DEBOUNCE_MILLIS} - or when the hard cap (passed in as
	 * milliseconds) elapses, whichever comes first. A Mutation Observer on {@code document} drives
	 * the quiet detection; a single {@code setTimeout} enforces the cap so a page that never stops
	 * mutating (constant animation/polling) still resolves promptly. A still page resolves after
	 * just the debounce window, so fast pages are barely slowed. The cap is passed as a string
	 * because Playwright's Java argument serializer rejects {@code Long} (mirrors
	 * {@link #CAPTURE_SCRIPT}).
	 *
	 * <p>
	 * Once the DOM is quiet it additionally waits out any <em>in-flight, finite</em> CSS
	 * animation/transition, still inside the same cap. A dismissed consent banner is a typical
	 * case: accepting it starts a fade/flip-out that mutates no DOM at all, so the quiet wait alone
	 * resolves while the banner is still fully laid out at a fractional opacity - and it would then
	 * be captured as visible content the user has already dismissed. Only animations whose
	 * iteration count is finite are awaited, so an endlessly repeating spinner or carousel (which
	 * would never finish) does not push every page load out to the cap.
	 * </p>
	 */
	private static final String DOM_SETTLE_SCRIPT =
		"(capStr) => new Promise(resolve => {\n"
			+ "  const cap = parseInt(capStr, 10);\n"
			+ "  const quiet = " + DOM_QUIET_DEBOUNCE_MILLIS + ";\n"
			+ "  const deadline = Date.now() + cap;\n"
			+ "  let quietTimer = null;\n"
			+ "  const finish = () => {\n"
			+ "    observer.disconnect();\n"
			+ "    if (quietTimer) clearTimeout(quietTimer);\n"
			+ "    clearTimeout(capTimer);\n"
			+ "    resolve();\n"
			+ "  };\n"
			// Promise for "no finite animation is still running", ignoring infinitely repeating ones.
			+ "  const animationsSettled = () => {\n"
			+ "    if (typeof document.getAnimations !== 'function') return Promise.resolve();\n"
			+ "    const pending = document.getAnimations()\n"
			+ "      .filter(a => a.playState === 'running')\n"
			+ "      .filter(a => {\n"
			+ "        const t = (a.effect && a.effect.getComputedTiming) ? a.effect.getComputedTiming() : null;\n"
			+ "        return t != null && t.iterations !== Infinity;\n"
			+ "      })\n"
			// A cancelled animation rejects its finished promise; that is a settled animation too.
			+ "      .map(a => a.finished.catch(() => {}));\n"
			+ "    return pending.length === 0 ? Promise.resolve() : Promise.all(pending);\n"
			+ "  };\n"
			+ "  const onQuiet = () => {\n"
			+ "    const remaining = deadline - Date.now();\n"
			+ "    if (remaining <= 0) { finish(); return; }\n"
			+ "    Promise.race([animationsSettled(), new Promise(r => setTimeout(r, remaining))])\n"
			+ "      .then(finish);\n"
			+ "  };\n"
			+ "  const observer = new MutationObserver(() => {\n"
			+ "    if (quietTimer) clearTimeout(quietTimer);\n"
			+ "    quietTimer = setTimeout(onQuiet, quiet);\n"
			+ "  });\n"
			+ "  const capTimer = setTimeout(finish, cap);\n"
			+ "  observer.observe(document, {childList: true, subtree: true});\n"
			// Start the debounce immediately: a page that never mutates resolves after one quiet
			// window rather than waiting out the whole cap.
			+ "  quietTimer = setTimeout(onQuiet, quiet);\n"
			+ "});";

	/**
	 * A realistic, current desktop Chrome user-agent applied only when bot-detection evasion is on,
	 * replacing the default {@code HeadlessChrome/...} token that bot walls key on. Kept a Linux
	 * desktop UA so it stays consistent with the actual (Linux) host and the bundled Chromium's real
	 * engine version (Playwright 1.49.0 ships Chromium 131), avoiding an obvious UA/platform mismatch.
	 */
	private static final String EVASION_USER_AGENT =
		"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
			+ "Chrome/131.0.0.0 Safari/537.36";

	/** Locale applied with {@link #EVASION_USER_AGENT}; sets {@code Accept-Language}/navigator language. */
	private static final String EVASION_LOCALE = "en-US";

	/**
	 * The Chromium switch that takes WebRTC's UDP egress away, closing the last hole the
	 * {@link FilteringProxy} cannot cover.
	 *
	 * <p>
	 * <b>Why this is needed at all.</b> {@code --proxy-server} constrains HTTP and HTTPS, but
	 * <em>WebRTC does not honour an HTTP proxy</em>. A page that constructs an
	 * {@code RTCPeerConnection} makes Chromium emit STUN/UDP datagrams straight from the browser
	 * process to whatever address the page names - past the proxy, so past the SSRF and domain policy
	 * entirely. That is a UDP reach into private/internal address space plus a disclosure of the
	 * host's local interface addresses. A browsing agent never needs WebRTC, so the safe setting is
	 * simply off, unconditionally: there is no configuration toggle, because an option whose only
	 * effect is to reopen an SSRF hole is not worth offering.
	 * </p>
	 *
	 * <p>
	 * <b>Why this exact switch.</b> It was chosen empirically against the pinned browser (Playwright
	 * 1.49.0, revision 1148, Chromium 131.0.6778.33), by pointing a page's ICE gathering at a
	 * loopback UDP listener and counting datagrams - the same measurement
	 * {@code TestBrowserWebRtcEgress} performs. Without it the listener receives STUN traffic and the
	 * page collects host candidates; with it ICE gathering completes with <em>no candidates at all</em>
	 * and the listener receives nothing, while ordinary page loads are unaffected. Three plausible
	 * alternatives were measured and all three left the UDP flowing, so none of them may be
	 * substituted here on the strength of its name:
	 * </p>
	 * <ul>
	 * <li>{@code --force-webrtc-ip-handling-policy=disable_non_proxied_udp} - the Chrome-branded
	 * variant; present in the binary, but no effect on this build, with or without a proxy pinned.</li>
	 * <li>{@code --disable-blink-features=RTCPeerConnection} - not a Blink runtime-feature name, so it
	 * is silently ignored.</li>
	 * <li>{@code --disable-blink-features=WebRTC} and {@code --disable-features=WebRTC} - neither
	 * removes {@code RTCPeerConnection} nor stops the datagrams.</li>
	 * </ul>
	 *
	 * @see #FULL_CHROMIUM_CHANNEL The switch only exists in the full Chromium build, which is why the
	 *      channel is pinned.
	 */
	private static final String DISABLE_NON_PROXIED_UDP_SWITCH =
		"--webrtc-ip-handling-policy=disable_non_proxied_udp";

	/**
	 * The Chromium switch that gives up the OS-level renderer sandbox.
	 *
	 * <p>
	 * Added only where the sandbox provably cannot be set up - see the SANDBOX paragraph of
	 * {@link #buildLaunchOptions(boolean, boolean, String, boolean)} and
	 * {@code BrowserToolsService.Config#getBrowserSandbox()}.
	 * </p>
	 */
	private static final String NO_SANDBOX_SWITCH = "--no-sandbox";

	/**
	 * Playwright channel selecting the <b>full</b> Chromium distribution
	 * ({@code chromium-<rev>/chrome-linux/chrome}) instead of the light, stripped
	 * {@code chrome-headless-shell}.
	 *
	 * <p>
	 * <b>Why it is pinned unconditionally.</b> {@link #DISABLE_NON_PROXIED_UDP_SWITCH} is
	 * <em>compiled out of the headless shell</em>: the switch name does not occur in the
	 * {@code headless_shell} binary at all, and passing it there measurably changes nothing - the
	 * shell keeps emitting STUN/UDP. Since Playwright 1.49 a headless launch with no channel resolves
	 * to exactly that shell, so leaving the channel unset would silently leave the WebRTC hole open on
	 * the default path. Pinning the full build is therefore what makes the switch above load-bearing
	 * rather than decorative.
	 * </p>
	 *
	 * <p>
	 * This subsumes the previous evasion-only use of the channel (the headless shell is itself a
	 * detectable fingerprint), so bot-detection evasion no longer has to select the build for itself.
	 * </p>
	 *
	 * <p>
	 * <b>Deployment prerequisite.</b> The full build must be present in the Playwright browser cache.
	 * Both builds are {@code installByDefault}, so any ordinary {@code playwright install chromium}
	 * (and Playwright's own auto-provisioning) supplies it; only a hand-slimmed image that ships
	 * <em>only</em> {@code chrome-headless-shell} would fail to launch, and such an image must now
	 * install the full build.
	 * </p>
	 */
	private static final String FULL_CHROMIUM_CHANNEL = "chromium";

	/**
	 * Environment variable of the Playwright driver that suppresses the forced
	 * {@code --proxy-bypass-list=<-loopback>} of a proxied launch.
	 *
	 * <p>
	 * Read here only to decide whether the {@link FilteringProxy} really sees every request (see
	 * {@link #_proxyIsAuthoritative}); it is never set by this code. With it set, Chromium keeps its
	 * implicit "localhost and link-local are never proxied" rules, so a loopback or metadata-endpoint
	 * request would bypass the proxy - the one case in which the in-browser route has to keep deciding
	 * sub-resources for itself.
	 * </p>
	 */
	private static final String FORCED_PROXIED_LOOPBACK_OFF_ENV =
		"PLAYWRIGHT_DISABLE_FORCED_CHROMIUM_PROXIED_LOOPBACK";

	/**
	 * The bounded element-action timeout used by the test-facing {@code open} overloads that do not
	 * name one; the service passes its configured value
	 * ({@code BrowserToolsService.Config#getActionTimeoutSeconds()}) explicitly.
	 */
	static final int DEFAULT_ACTION_TIMEOUT_MILLIS = 8_000;

	/**
	 * How long {@link #close()} gives the orderly, API-level teardown on the session thread before it
	 * falls back to destroying the driver process tree.
	 */
	private static final long GRACEFUL_CLOSE_TIMEOUT_MILLIS = 30_000L;

	/**
	 * How long a process gets to exit after {@code SIGTERM} before it is killed outright. Short,
	 * because this path is only reached after the graceful teardown has already failed.
	 */
	private static final long PROCESS_TERMINATION_GRACE_MILLIS = 2_000L;

	/**
	 * The private field of {@code com.microsoft.playwright.impl.PlaywrightImpl} holding the driver
	 * {@link Process}; see {@link #driverProcessOf(Playwright)} for why it is read reflectively.
	 */
	private static final String DRIVER_PROCESS_FIELD = "driverProcess";

	/**
	 * Small, standard "stealth" shim applied (before any page script) only when bot-detection evasion
	 * is on, masking the cheap automation tells a headless Chromium exposes. Deliberately minimal and
	 * dependency-free: it only fixes the giveaways that ordinary desktop Chrome would not present.
	 * Each patch is a no-op unless the tell is actually present, so it never fights a value the
	 * launch flag already corrected.
	 */
	private static final String STEALTH_INIT_SCRIPT =
		"(() => {\n"
			// navigator.webdriver is `true` under automation; ordinary Chrome reports false/undefined.
			// The --disable-blink-features=AutomationControlled launch flag already suppresses it in
			// most builds; this makes the guarantee explicit for pages/builds that still read it.
			+ "  Object.defineProperty(navigator, 'webdriver', { get: () => undefined });\n"
			// Headless Chrome exposes an EMPTY plugin list; ordinary Chrome has a few entries. Bot
			// walls check for length > 0, not the exact contents, so a small non-empty stub suffices.
			+ "  if (navigator.plugins && navigator.plugins.length === 0) {\n"
			+ "    Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] });\n"
			+ "  }\n"
			// Ensure a plausible, non-empty languages list matching the context locale.
			+ "  if (!navigator.languages || navigator.languages.length === 0) {\n"
			+ "    Object.defineProperty(navigator, 'languages', { get: () => ['en-US', 'en'] });\n"
			+ "  }\n"
			// Headless Chrome lacks window.chrome; ordinary Chrome exposes at least window.chrome.runtime.
			+ "  if (!window.chrome) {\n"
			+ "    window.chrome = { runtime: {} };\n"
			+ "  }\n"
			+ "})();";

	private final Object _key;

	private final int _navigationTimeoutMillis;

	/**
	 * Bounded timeout (ms) for an <em>element</em> action - click, type, ARIA snapshot - as opposed to
	 * a page load.
	 *
	 * <p>
	 * Deliberately far shorter than {@link #_navigationTimeoutMillis}: waiting for actionability is
	 * not waiting for the network. When the element a ref points at has been detached, covered or
	 * re-rendered, Playwright keeps retrying actionability until the timeout expires and there is
	 * nothing to gain from spending a full page-load budget on it - the wait can only succeed if the
	 * page settles within a moment. Bounding it is what turns a dead click into a fast, recoverable
	 * {@link BrowserRefNotActionableException} instead of a 30-second stall.
	 * </p>
	 */
	private final int _actionTimeoutMillis;

	/**
	 * Hard cap (ms) of the post-load settle. It bounds each of the <b>three</b> waits that can follow
	 * an operation, separately: the commit wait a navigating click runs
	 * ({@link #awaitNavigationCommit()}), the network-quiet stage ({@link #waitForNetworkQuiet()}) and
	 * the DOM-quiet stage ({@link #waitForDomQuiet()}). {@code 0} disables all three, so a loaded page
	 * is captured immediately.
	 *
	 * <p>
	 * That the <em>network</em> stage is bounded by this cap rather than by
	 * {@link #_navigationTimeoutMillis} is what keeps a page that never goes network-idle from costing
	 * a page-load timeout on every operation; see {@link #waitForNetworkQuiet()}.
	 * </p>
	 *
	 * <p>
	 * The three do not add up on any ordinary path: a click that does not navigate runs only the
	 * DOM-quiet stage, and a navigating click's commit wait is normally already satisfied when it is
	 * reached. They add up only where a navigation is begun and never lands, which
	 * {@link #awaitNavigationCommit()} describes and bounds.
	 * </p>
	 */
	private final int _navigationSettleMillis;

	private final DomainPolicy _policy;

	/** Non-null when the session's context is seeded with a previously captured authenticated state. */
	private final String _initialStorageState;

	/** When {@code true} the browser is launched headed (a real window on the app host's desktop). */
	private final boolean _headed;

	/** When {@code true} the context is disguised as ordinary desktop Chrome (operator opt-in). */
	private final boolean _evadeBotDetection;

	/**
	 * The {@code --proxy-server} value of the filtering forward proxy every connection of this
	 * session must traverse, or {@code null} for an unconstrained browser.
	 *
	 * <p>
	 * {@code null} only occurs in tests that exercise a browser mechanism with no network at all (a
	 * {@code setContent} fixture); {@code BrowserToolsService} always pins its sessions to the
	 * service-wide {@link FilteringProxy}.
	 * </p>
	 */
	private final String _proxyServer;

	/**
	 * Whether the {@link FilteringProxy} is the authoritative SSRF layer for <em>every</em> request this
	 * session's browser makes, so {@link #installSecurityRoute the security route} does not have to
	 * re-decide a sub-resource.
	 *
	 * <p>
	 * True when a proxy is pinned <em>and</em> Chromium's built-in "never proxy loopback" rule is
	 * switched off. Both halves are needed: without a proxy (a fixture session with no network) the route
	 * is the only layer there is, and with the implicit bypass in place a request to
	 * {@code 127.0.0.1} or {@code 169.254.169.254} - the two destinations that matter most - would go
	 * out directly and the proxy would never see it. Playwright passes
	 * {@code --proxy-bypass-list=<-loopback>} with every proxied launch, which subtracts exactly those
	 * implicit rules, unless the {@link #FORCED_PROXIED_LOOPBACK_OFF_ENV} escape hatch of its driver is
	 * set in this process' environment.
	 * </p>
	 */
	private final boolean _proxyIsAuthoritative;

	/**
	 * Whether this session's Chromium is launched without its OS-level sandbox.
	 *
	 * @see BrowserToolsService.Config#getBrowserSandbox()
	 */
	private final boolean _sandboxDisabled;

	private final ExecutorService _executor;

	private volatile Playwright _playwright;

	/**
	 * The Playwright <em>driver</em> process this session launched (a bundled {@code node} running the
	 * Playwright CLI), or {@code null} when it could not be identified.
	 *
	 * <p>
	 * Retained purely so {@link #close()} has a teardown path that does <b>not</b> go through the
	 * session thread. Every Playwright object of this session is confined to that one thread, so when
	 * it wedges - typically blocked in a native read on the driver pipe, which
	 * {@link Future#cancel(boolean)} cannot interrupt - there is no API-level way left to close
	 * anything: a teardown task submitted to the same executor simply queues behind the wedged call
	 * and is dropped by {@code shutdownNow()}. Without this handle the Chromium and driver processes
	 * then outlive the session for the JVM's lifetime, while the pool has already handed the slot to
	 * someone else - so {@code max-concurrent-sessions} stops bounding memory on exactly the path the
	 * broken-session exception exists for. The browser process tree hangs off this driver, so
	 * destroying it (and its descendants) reclaims everything.
	 * </p>
	 */
	private volatile ProcessHandle _driverProcess;

	/** Whether {@link #close(long)} has already run, so a second call is a no-op. */
	private boolean _closed;

	private volatile Browser _browser;

	private volatile BrowserContext _context;

	private volatile Page _page;

	/**
	 * Monotonic page-snapshot generation. It advances whenever the session moves to a <em>different
	 * page</em> - an explicit navigation ({@link #navigate}, {@link #back}) or a {@link #click} that
	 * navigates - because that is exactly when a carried-over ref would resolve to a different element
	 * on the new page. It deliberately does <em>not</em> advance on a re-capture of the same page
	 * ({@link #snapshot}, {@link #type}): filling one field must not invalidate the sibling refs of
	 * the same form, so a "read the form once, fill several fields, submit" flow keeps working.
	 *
	 * <p>
	 * Refs are tagged with the generation that minted them ({@code s<gen>e<index>}), and
	 * {@link #requireCurrentRef(String)} rejects any ref whose generation is not the current one.
	 * Confined to the session thread (only read/written inside operations submitted to
	 * {@link #_executor}), so no synchronization is needed.
	 * </p>
	 */
	private long _pageGeneration;

	/**
	 * The off-policy top-level navigation seen during the operation currently running, or {@code null}
	 * if there was none.
	 *
	 * <p>
	 * Recorded by the security route when it aborts a main-frame navigation, and by the request
	 * listener for a redirect hop the route never sees (see {@link #installSecurityRoute} and
	 * {@link #installRedirectGuard}). Reset at the start of every navigating operation by
	 * {@link #beginNavigationAttempt()} and consumed by {@link #takeOffPolicyNavigation()} on behalf of
	 * {@link #verifyNavigationPolicy}, {@link #verifyCurrentDocumentPolicy} and
	 * {@link #translateNavigationFailure}, so the tool can report the precise policy reason instead of
	 * the raw {@code net::ERR_FAILED} / {@code TimeoutError} the aborted request produces.
	 * </p>
	 *
	 * <p>
	 * Playwright dispatches route and event callbacks on the thread waiting inside the Playwright
	 * call - this session's confined thread - so this is single-threaded in practice; {@code volatile}
	 * documents and guarantees the publication regardless.
	 * </p>
	 */
	private volatile String _offPolicyNavigation;

	/**
	 * Whether the operation currently being observed has started a <b>top-level navigation</b>.
	 *
	 * <p>
	 * This is what tells a {@link #click} apart from a navigating one, and it decides which settle the
	 * click gets: a click that only changed the page in place must not pay the page-load-sized waits
	 * that a page load needs (see {@link #settleAfterClick()}).
	 * </p>
	 *
	 * <p>
	 * Raised from two sources, because neither alone sees every navigation: the main-frame
	 * <em>request</em> going out ({@link #installNavigationObserver}), which fires the moment the
	 * browser commits to fetching a new document and therefore does not depend on how slow the server
	 * is; and the main frame <em>having navigated</em> ({@link #installNavigationCommitObserver}),
	 * which additionally covers a same-document hop - a fragment or a {@code history.pushState} - that
	 * issues no request at all. Comparing URLs is deliberately not one of the sources: a reload or a
	 * form post to the same address navigates without changing the URL, so a URL comparison would
	 * classify a real page load as a same-page click.
	 * </p>
	 *
	 * <p>
	 * Reset at the start of every operation by {@link #beginNavigationAttempt()}, so it can only ever
	 * describe the operation running now. {@code volatile} for the same reason as
	 * {@link #_offPolicyNavigation}: Playwright dispatches the callbacks on the session's confined
	 * thread, so this is single-threaded in practice, and the modifier documents the publication.
	 * </p>
	 */
	private volatile boolean _navigationStarted;

	/**
	 * How many times a <b>top-level navigation has committed</b> - a new document has actually
	 * replaced the old one - in this session's page, counted by
	 * {@link #installNavigationCommitObserver}.
	 *
	 * <p>
	 * {@link #_navigationStarted} says a navigation was <em>begun</em>, which is what a click has to
	 * know and all it can know in time. This says it has <em>landed</em>, which is what everything
	 * after the click needs: until the new document commits, the page's URL is still the old one and
	 * its lifecycle events - {@code load}, {@code networkidle} - are the old document's, so waiting for
	 * them would be satisfied by the page being left rather than by the page being entered. Waiting for
	 * this to move first (see {@link #awaitNavigationCommit()}) is what stops a click from reporting
	 * the old URL beside the new page's HTML.
	 * </p>
	 *
	 * <p>
	 * Monotonic rather than a flag, and compared against the value taken at the start of the operation
	 * ({@link #_navigationsAtAttemptStart}), so an earlier operation's commit cannot be mistaken for
	 * this one's. Confined to the session thread like {@link #_navigationStarted}; {@code volatile}
	 * documents the publication.
	 * </p>
	 */
	private volatile long _mainFrameNavigations;

	/** {@link #_mainFrameNavigations} as it stood when the current operation began. */
	private long _navigationsAtAttemptStart;

	private BrowserSession(Object key, int navigationTimeoutMillis, int actionTimeoutMillis,
			int navigationSettleMillis, DomainPolicy policy, String initialStorageState, boolean headed,
			boolean evadeBotDetection, String proxyServer, boolean sandboxDisabled) {
		_key = key;
		_navigationTimeoutMillis = navigationTimeoutMillis;
		_actionTimeoutMillis = actionTimeoutMillis;
		_navigationSettleMillis = navigationSettleMillis;
		_policy = policy;
		_initialStorageState = initialStorageState;
		_headed = headed;
		_evadeBotDetection = evadeBotDetection;
		_proxyServer = proxyServer;
		_proxyIsAuthoritative = proxyServer != null && !proxyServer.isBlank()
			&& System.getenv(FORCED_PROXIED_LOOPBACK_OFF_ENV) == null;
		_sandboxDisabled = sandboxDisabled;
		_executor = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, "browser-session");
			thread.setDaemon(true);
			return thread;
		});
	}

	/**
	 * Opens a new isolated session (launches its Chromium and creates its context/page on the
	 * session thread).
	 *
	 * @param key
	 *        The session key (for diagnostics).
	 * @param navigationTimeoutMillis
	 *        The per-operation Playwright timeout.
	 * @param policy
	 *        The top-level-origin domain policy.
	 * @return A started session.
	 */
	static BrowserSession open(Object key, int navigationTimeoutMillis, DomainPolicy policy) {
		return open(key, navigationTimeoutMillis, DEFAULT_ACTION_TIMEOUT_MILLIS, 1500, policy, null, false,
			false, null);
	}

	/**
	 * The sandbox setting the test-facing {@code open} overloads use: the same
	 * {@link BrowserToolsService.SandboxMode#AUTO auto-detected} answer a default installation gets,
	 * so a session opened without naming one is launched exactly the way production launches it.
	 */
	private static boolean defaultSandboxDisabled() {
		return BrowserToolsService.isSandboxDisabled(BrowserToolsService.SandboxMode.AUTO);
	}

	/**
	 * Opens a new isolated session with the given post-navigation settle cap and the default bounded
	 * action timeout.
	 *
	 * @param key
	 *        The session key (for diagnostics).
	 * @param navigationTimeoutMillis
	 *        The per-page-load Playwright timeout.
	 * @param navigationSettleMillis
	 *        Hard cap (ms) of the post-load settle (network-quiet and DOM-quiet stages); {@code 0}
	 *        disables it.
	 * @param policy
	 *        The top-level-origin domain policy.
	 * @param initialStorageState
	 *        A {@code storageState} JSON to seed into the fresh context, or {@code null}.
	 * @param headed
	 *        Whether to launch a visible window instead of headless.
	 * @param evadeBotDetection
	 *        Whether to disguise the context as ordinary desktop Chrome.
	 * @return A started session.
	 */
	static BrowserSession open(Object key, int navigationTimeoutMillis, int navigationSettleMillis,
			DomainPolicy policy, String initialStorageState, boolean headed, boolean evadeBotDetection) {
		return open(key, navigationTimeoutMillis, DEFAULT_ACTION_TIMEOUT_MILLIS, navigationSettleMillis,
			policy, initialStorageState, headed, evadeBotDetection, null);
	}

	/**
	 * Opens a new isolated session, optionally seeding its context with a previously captured
	 * authenticated {@code storageState} so the session resumes logged in.
	 *
	 * @param key
	 *        The session key (for diagnostics).
	 * @param navigationTimeoutMillis
	 *        The per-page-load Playwright timeout.
	 * @param actionTimeoutMillis
	 *        The bounded timeout for an element action (click/type/ARIA snapshot); see
	 *        {@link #_actionTimeoutMillis}.
	 * @param navigationSettleMillis
	 *        Hard cap (ms) of the post-load settle run before a page-load capture - it bounds both
	 *        the network-quiet stage ({@link #waitForNetworkQuiet()}) and the DOM-quiet stage
	 *        ({@link #waitForDomQuiet()}) of {@link #settleAfterLoad()}; {@code 0} disables the
	 *        settle (capture as soon as the page has loaded). Always bounded well inside
	 *        {@link #navigationDeadlineMillis()}.
	 * @param policy
	 *        The top-level-origin domain policy.
	 * @param initialStorageState
	 *        A {@code storageState} JSON to seed cookies/tokens into the fresh context, or
	 *        {@code null} for a clean anonymous context. This is how a chosen cookie pool starts a
	 *        session already logged in.
	 * @param headed
	 *        When {@code true}, launch Chromium headed (a visible window on the app host's desktop)
	 *        instead of headless (see {@code BrowserToolsService.Config#getHeadless()}); it requires
	 *        a local display.
	 * @param evadeBotDetection
	 *        When {@code true}, disguise the context as ordinary desktop Chrome (see
	 *        {@code BrowserToolsService.Config#isEvadeBotDetection()}): add the
	 *        {@code --disable-blink-features=AutomationControlled} launch switch, set a realistic
	 *        Chrome user-agent/locale, and install a fingerprint-masking init script (see
	 *        {@link #buildLaunchOptions(boolean, boolean, String, boolean)}). The <b>full</b> Chromium
	 *        build that evasion also relies on is selected for every launch now, evasion or not (see
	 *        {@link #FULL_CHROMIUM_CHANNEL}).
	 * @param proxyServer
	 *        The {@code --proxy-server} value of the {@link FilteringProxy} every connection of this
	 *        session must traverse, or {@code null} for an unconstrained browser. Production sessions
	 *        are always pinned (see {@link #_proxyServer}).
	 * @return A started session.
	 */
	static BrowserSession open(Object key, int navigationTimeoutMillis, int actionTimeoutMillis,
			int navigationSettleMillis, DomainPolicy policy, String initialStorageState, boolean headed,
			boolean evadeBotDetection, String proxyServer) {
		return open(key, navigationTimeoutMillis, actionTimeoutMillis, navigationSettleMillis, policy,
			initialStorageState, headed, evadeBotDetection, proxyServer, defaultSandboxDisabled());
	}

	/**
	 * Opens a new isolated session, naming explicitly whether Chromium's OS-level sandbox is given up.
	 *
	 * <p>
	 * Every other parameter is as documented on
	 * {@link #open(Object, int, int, int, DomainPolicy, String, boolean, boolean, String)}, which is
	 * this method with the sandbox setting auto-detected.
	 * </p>
	 *
	 * @param sandboxDisabled
	 *        Whether to launch with {@code --no-sandbox}. Decided by
	 *        {@code BrowserToolsService.isSandboxDisabled(SandboxMode)}, which keeps the sandbox on
	 *        outside a container; see {@code BrowserToolsService.Config#getBrowserSandbox()}.
	 * @return A started session.
	 */
	static BrowserSession open(Object key, int navigationTimeoutMillis, int actionTimeoutMillis,
			int navigationSettleMillis, DomainPolicy policy, String initialStorageState, boolean headed,
			boolean evadeBotDetection, String proxyServer, boolean sandboxDisabled) {
		BrowserSession session = new BrowserSession(key, navigationTimeoutMillis, actionTimeoutMillis,
			navigationSettleMillis, policy, initialStorageState, headed, evadeBotDetection, proxyServer,
			sandboxDisabled);
		try {
			session.startOnThread();
		} catch (RuntimeException ex) {
			session.close();
			throw ex;
		}
		return session;
	}

	/**
	 * Builds the Chromium {@link com.microsoft.playwright.BrowserType.LaunchOptions} from a
	 * session's launch flags. Extracted as a package-visible, side-effect-free static seam so the
	 * launch decision (which build, headless-or-not, which args) can be asserted in a unit test
	 * without actually launching a browser.
	 *
	 * <p>
	 * <b>Build selection.</b> Every launch selects the <b>full</b> Chromium distribution via
	 * {@link com.microsoft.playwright.BrowserType.LaunchOptions#setChannel(String) channel
	 * "chromium"} - see {@link #FULL_CHROMIUM_CHANNEL} for why that is not optional (the WebRTC
	 * switch below does not exist in {@code chrome-headless-shell}) and for the deployment
	 * prerequisite it carries. The launch stays windowless: for the non-headed case
	 * {@code headless} remains {@code true} (channel "chromium" + headless = new headless, no
	 * visible window); the channel setting is orthogonal to {@code headless} and composes cleanly
	 * with the headed launch toggle ({@code headed=true} launches the same full build with a
	 * visible window). Picking the full build also happens to be what bot-detection evasion needs
	 * anyway, since the headless shell is itself a detectable fingerprint that some bot walls /
	 * consent managers suppress content for even when the user-agent and
	 * {@code navigator.webdriver} are already masked; evasion adds the UA, locale, stealth
	 * init-script and {@code --disable-blink-features=AutomationControlled} on top.
	 * </p>
	 *
	 * <p>
	 * <b>Sandbox.</b> {@code --no-sandbox} is added <em>only</em> when the caller says the sandbox
	 * has to be given up. Chromium's OS-level sandbox is the boundary between a renderer running
	 * attacker-controlled page JavaScript and the application host, so passing the switch
	 * unconditionally - which this method used to do, for the sake of one container image - removed
	 * that boundary on every host the application runs on. The decision belongs to
	 * {@code BrowserToolsService.isSandboxDisabled(SandboxMode)}, which keeps the sandbox on unless
	 * this JVM is in a container (or an operator has forced the matter); it is passed in rather
	 * than taken here so this seam stays side-effect-free and both branches can be asserted in a
	 * test.
	 * </p>
	 *
	 * <p>
	 * <b>WebRTC egress.</b> Every launch also carries {@link #DISABLE_NON_PROXIED_UDP_SWITCH},
	 * which stops WebRTC from sending UDP. This is a security control, not a tuning knob: WebRTC
	 * ignores {@code --proxy-server}, so it is the one egress path the {@link FilteringProxy} below
	 * cannot see, and leaving it on would let a page reach private addresses over UDP regardless of
	 * the SSRF policy.
	 * </p>
	 *
	 * <p>
	 * <b>Proxy pinning.</b> When a {@code proxyServer} is given it is set as the launch-level
	 * {@link com.microsoft.playwright.BrowserType.LaunchOptions#setProxy(com.microsoft.playwright.options.Proxy)
	 * proxy}, which Playwright turns into Chromium's {@code --proxy-server} switch, so
	 * <em>every</em> connection the browser process makes goes through the {@link FilteringProxy}.
	 * Launch level rather than per-context because the SSRF and domain policy is service-wide
	 * configuration, and because Chromium's per-context proxy support requires the browser to be
	 * launched with a placeholder proxy anyway - each session has its own browser process, so
	 * launch level is per-session in practice. Playwright additionally passes
	 * {@code --proxy-bypass-list=<-loopback>}, which <em>disables</em> Chromium's built-in "never
	 * proxy localhost" rule; without that a loopback target would evade the proxy entirely, so it
	 * is load-bearing here rather than incidental. The proxy setting is orthogonal to both the
	 * headless toggle and the channel/evasion selection.
	 * </p>
	 *
	 * @param headed
	 *        Whether to launch a visible window (headed launch toggle) instead of headless.
	 * @param evadeBotDetection
	 *        Whether bot-detection evasion is enabled (adds its blink-feature switch; the full
	 *        Chromium build it also relies on is selected either way).
	 * @param proxyServer
	 *        The filtering forward proxy to pin the browser to (e.g.
	 *        {@code http://127.0.0.1:41234}), or {@code null} to leave the browser unproxied.
	 * @param sandboxDisabled
	 *        Whether to add {@code --no-sandbox}; see the paragraph above. Resolved by
	 *        {@code BrowserToolsService.isSandboxDisabled(SandboxMode)}, never decided here.
	 * @return The launch options; never {@code null}.
	 */
	static BrowserType.LaunchOptions buildLaunchOptions(boolean headed, boolean evadeBotDetection,
			String proxyServer, boolean sandboxDisabled) {
		// /dev/shm is only 64 MB in the default Docker sandbox; without this Chromium crashes
		// under memory pressure. Harmless on a normal host.
		List<String> launchArgs = new ArrayList<>(List.of("--disable-dev-shm-usage",
			// Take WebRTC's UDP egress away: it does NOT honour --proxy-server, so without this a
			// page could reach any address over UDP straight past the FilteringProxy. See the
			// constant's comment for the evidence and for why the channel below is load-bearing.
			DISABLE_NON_PROXIED_UDP_SWITCH));
		if (sandboxDisabled) {
			// Hands the renderer the application's own privileges: only added where the sandbox
			// provably cannot be set up (see the SANDBOX paragraph above).
			launchArgs.add(NO_SANDBOX_SWITCH);
		}
		if (evadeBotDetection) {
			// Drop the AutomationControlled blink feature (which sets navigator.webdriver and is a
			// cheap bot tell). Evasion-only: the default path stays an ordinary headless Chromium.
			launchArgs.add("--disable-blink-features=AutomationControlled");
		}
		BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
			.setHeadless(!headed)
			.setArgs(launchArgs)
			// Select the FULL Chromium build rather than chrome-headless-shell. Required
			// unconditionally, because the WebRTC switch above is COMPILED OUT of the shell - see
			// DISABLE_NON_PROXIED_UDP_SWITCH. Orthogonal to `headless`: channel "chromium" +
			// headless is new headless, still windowless.
			.setChannel(FULL_CHROMIUM_CHANNEL);
		if (proxyServer != null && !proxyServer.isBlank()) {
			// Constrain the browser from OUTSIDE the routing API: every connection - including a
			// redirect hop, which a route cannot cancel - has to pass the filtering proxy first.
			options.setProxy(new Proxy(proxyServer));
		}
		return options;
	}

	private void startOnThread() {
		callOnThread(() -> {
			_playwright = Playwright.create();
			// Grab the OS-level handle now, while the session thread is healthy: once it wedges, the
			// only teardown left is destroying this process tree from another thread (see close()).
			_driverProcess = driverProcessOf(_playwright);
			BrowserType chromium = _playwright.chromium();
			_browser = chromium.launch(
				buildLaunchOptions(_headed, _evadeBotDetection, _proxyServer, _sandboxDisabled));
			_context = _browser.newContext(newContextOptions());
			_context.setDefaultTimeout(_navigationTimeoutMillis);
			_context.setDefaultNavigationTimeout(_navigationTimeoutMillis);
			installSecurityRoute(_context);
			installRedirectGuard(_context);
			installProxyRefusalGuard(_context);
			installDownloadGuard(_context);
			installNavigationObserver(_context);
			if (_evadeBotDetection) {
				// Mask the automation fingerprint before any page script runs (applies to every
				// document in the context, including the initial about:blank page created below).
				_context.addInitScript(STEALTH_INIT_SCRIPT);
			}
			_page = _context.newPage();
			installNavigationCommitObserver(_page);
			Logger.info("Opened isolated " + (_headed ? "headed" : "headless") + " browser session " + _key
				+ (_evadeBotDetection ? " (bot-detection evasion on)" : "")
				+ (_proxyServer == null ? " (UNPROXIED - no outbound filtering)"
					: " (filtered through " + _proxyServer + ")")
				+ (_initialStorageState == null ? "." : " (seeded with a saved cookie pool)."),
				BrowserSession.class);
			return null;
		}, startupDeadlineMillis());
	}

	/**
	 * The context options for this session, or {@code null} when none are needed so the launch stays
	 * byte-for-byte the plain {@code newContext()} default. A chosen cookie pool seeds
	 * {@code storageState}; evasion mode additionally overrides the user-agent and locale.
	 */
	private Browser.NewContextOptions newContextOptions() {
		Browser.NewContextOptions options = null;
		if (_initialStorageState != null) {
			options = new Browser.NewContextOptions().setStorageState(_initialStorageState);
		}
		if (_evadeBotDetection) {
			if (options == null) {
				options = new Browser.NewContextOptions();
			}
			// Replace the default HeadlessChrome user-agent with a realistic desktop Chrome UA, and
			// set a matching locale so Accept-Language / navigator.language look ordinary too.
			options.setUserAgent(EVASION_USER_AGENT).setLocale(EVASION_LOCALE);
		}
		return options;
	}

	/**
	 * Navigates this session's page to the given (already pre-validated) URL and captures the page.
	 */
	PageView navigate(String url) {
		return callOnThread(() -> {
			beginNavigationAttempt();
			Response response;
			try {
				response = _page.navigate(url, new Page.NavigateOptions()
					.setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
					.setTimeout(_navigationTimeoutMillis));
			} catch (PlaywrightException ex) {
				throw translateNavigationFailure(url, ex);
			}
			settleAfterLoad();
			int status = response != null ? response.status() : -1;
			Map<String, String> headers = response != null ? response.headers() : null;
			// An explicit navigation always lands on a fresh document: start a new page generation so
			// refs from the previous page are detected as stale.
			_pageGeneration++;
			PageView view = capture(status, headers, true);
			// Judge the document that was actually captured, which is the one about to be handed to the
			// model, rather than a URL read in a separate round trip a moment earlier.
			verifyNavigationPolicy(view.finalUrl());
			return view;
		}, navigationDeadlineMillis());
	}

	/**
	 * Captures the current page without navigating.
	 *
	 * <p>
	 * The policy re-check runs <em>after</em> the capture although its result is then thrown away: a
	 * top-level hop the page started on its own is recorded by a Playwright callback, and the capture's
	 * round trips to the driver are what give that callback the chance to run. Checking first would
	 * systematically miss the hop this verb is here to catch.
	 * </p>
	 */
	PageView snapshot() {
		return callOnThread(() -> {
			PageView view = capture(-1, null, false);
			verifyCurrentDocumentPolicy();
			return view;
		}, navigationDeadlineMillis());
	}

	/**
	 * Captures a PNG screenshot of the current page (the viewport when {@code fullPage} is
	 * {@code false}, the full scrolling page when {@code true}), on this session's confined thread.
	 *
	 * <p>
	 * An image of the page is page content handed to the model exactly as a {@link #snapshot}'s text
	 * is - it is persisted and read back later - so the same policy re-check governs it, and a failing
	 * check discards the capture. It runs <em>after</em> the capture for the reason given on
	 * {@link #snapshot}.
	 * </p>
	 */
	ScreenshotCapture screenshot(boolean fullPage) {
		return callOnThread(() -> {
			byte[] png = _page.screenshot(new Page.ScreenshotOptions().setFullPage(fullPage));
			ScreenshotCapture capture = new ScreenshotCapture(_page.url(), png);
			verifyCurrentDocumentPolicy();
			return capture;
		}, navigationDeadlineMillis());
	}

	/** Clicks the element previously assigned {@code ref}, then captures the resulting page. */
	PageView click(String ref) {
		return callOnThread(() -> {
			requireCurrentRef(ref);
			beginNavigationAttempt();
			String urlBefore = _page.url();
			Locator locator = _page.locator(refSelector(ref));
			try {
				locator.click(new Locator.ClickOptions().setTimeout(_actionTimeoutMillis));
			} catch (PlaywrightException ex) {
				throw translateActionFailure(ref, "clicked", ex);
			}
			settleAfterClick();
			// What the browser reported, not what the URL happens to say: a reload and a form post to
			// the same address navigate without changing the URL, so the comparison alone would leave
			// the page generation behind and keep accepting refs minted on the previous document. The
			// comparison is kept only as a fallback that can never under-report - a changed top-level
			// URL is a navigation whether or not the listeners saw it.
			boolean navigated = _navigationStarted || !_page.url().equals(urlBefore);
			if (navigated) {
				// The click navigated to a different page: refs from the old page are now stale.
				_pageGeneration++;
			}
			PageView view = capture(-1, null, navigated);
			verifyNavigationPolicy(view.finalUrl());
			return view;
		}, navigationDeadlineMillis());
	}

	/** Types {@code text} into the element previously assigned {@code ref}, then captures. */
	PageView type(String ref, String text) {
		return callOnThread(() -> {
			requireCurrentRef(ref);
			// Typing does not navigate by itself, but filling a field can submit a form: start a fresh
			// observation window so a leftover record from the previous operation cannot be
			// mis-attributed to this one.
			beginNavigationAttempt();
			Locator locator = _page.locator(refSelector(ref));
			try {
				locator.fill(text == null ? "" : text,
					new Locator.FillOptions().setTimeout(_actionTimeoutMillis));
			} catch (PlaywrightException ex) {
				throw translateActionFailure(ref, "typed into", ex);
			}
			// Filling a field can submit a form, so the same check the navigating verbs run applies
			// here: an off-policy hop, or a refusal page the proxy served, must not come back as
			// ordinary page content. After the capture, for the reason given on snapshot().
			PageView view = capture(-1, null, false);
			verifyCurrentDocumentPolicy();
			return view;
		}, navigationDeadlineMillis());
	}

	/**
	 * Captures the context's {@code storageState} (cookies + origin local/session storage) as a JSON
	 * string, on this session's confined thread. This is the authenticated login state a cookie pool
	 * persists (encrypted) and later seeds into a fresh session.
	 */
	String captureStorageState() {
		return callOnThread(() -> _context.storageState(), navigationDeadlineMillis());
	}

	/** Navigates back in this session's history, then captures. */
	PageView back() {
		return callOnThread(() -> {
			beginNavigationAttempt();
			Response response;
			try {
				response = _page.goBack(new Page.GoBackOptions()
					.setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
					.setTimeout(_navigationTimeoutMillis));
			} catch (PlaywrightException ex) {
				throw translateNavigationFailure(_page.url(), ex);
			}
			if (response == null) {
				// No history entry to go back to: the page is unchanged, so keep the generation.
				return capture(-1, null, false);
			}
			settleAfterLoad();
			// Going back landed on a different page in history: start a new page generation.
			_pageGeneration++;
			PageView view = capture(response.status(), response.headers(), true);
			verifyNavigationPolicy(view.finalUrl());
			return view;
		}, navigationDeadlineMillis());
	}

	/**
	 * Settles a freshly <b>loaded</b> page before it is captured. Run by the page-<em>loading</em>
	 * operations ({@link #navigate}, {@link #back} and a {@link #click} that started a navigation),
	 * never by the explicit re-capture {@link #snapshot} or the same-page edit {@link #type}, which
	 * stay immediate.
	 *
	 * <p>
	 * Three stages, and only the first may spend the page-load budget:
	 * </p>
	 * <ol>
	 * <li>{@link #waitForLoad()} - the document's own load, bounded by the page-load timeout. This is
	 * the wait the operator sizes with {@code navigation-timeout-seconds}, and a page load is exactly
	 * what it is for.</li>
	 * <li>{@link #waitForNetworkQuiet()} - a best-effort wait for the network to fall silent, for
	 * client-side rendering that only starts after the load event. Bounded by the <em>settle cap</em>,
	 * never by the page-load timeout: a page that polls or beacons (an SSO login page, most SPAs)
	 * never goes network-idle at all, so borrowing the page-load budget here bought nothing and cost
	 * half a minute per operation.</li>
	 * <li>{@link #waitForDomQuiet()} - the bounded DOM-quiet wait, so content injected shortly
	 * <em>after</em> the load - cookie-consent banners, lazy widgets - is present in the snapshot
	 * rather than requiring the model to re-snapshot manually.</li>
	 * </ol>
	 */
	private void settleAfterLoad() {
		waitForLoad();
		waitForNetworkQuiet();
		waitForDomQuiet();
	}

	/**
	 * Settles after a {@link #click}, spending only what the click actually did warrants.
	 *
	 * <p>
	 * A click that started a top-level navigation gets the full page-load settle. A click that stayed
	 * on the page - the overwhelming majority: opening a menu, ticking a box, focusing a field - gets
	 * the bounded DOM-quiet wait and nothing else. There is no page load to wait for, so waiting for
	 * one can only cost time; on a page that never goes network-idle it used to cost the whole
	 * {@code navigation-timeout-seconds} on every single click.
	 * </p>
	 *
	 * <p>
	 * Whether a navigation started is decided from what the browser reported
	 * ({@link #_navigationStarted}) rather than from the URL after the fact: a reload or a form post
	 * to the same address changes no URL, and a click whose navigation is still in flight has not
	 * changed it yet either.
	 * </p>
	 *
	 * <p>
	 * The flag is consulted <b>twice</b>, and the second look is what makes the deferred navigation
	 * safe: a handler that starts the hop itself - {@code setTimeout(() => location.href = …)}, or
	 * the very common {@code fetch(…).then(() => location.href = …)} - can miss
	 * {@link #CLICK_NAVIGATION_DETECTION_MILLIS the first window} and still arrive while the DOM-quiet
	 * wait is running, and the browser dispatches its events into that wait. Capturing then would hand
	 * the model a page view that is <em>torn</em> - the URL of the page the click started on, with the
	 * HTML of the one it ended on, and (because {@link #click} would see no navigation) refs minted on
	 * the new document under the old page generation, so a ref the model still held from before would
	 * be accepted against a different element on a different page. Running the page-load settle
	 * instead lets the new document finish, so URL, content and generation describe the same page.
	 * See {@link #CLICK_NAVIGATION_DETECTION_MILLIS} for what the combined window is and what still
	 * falls outside it.
	 * </p>
	 */
	private void settleAfterClick() {
		if (awaitNavigationStart()) {
			settleAfterNavigatingClick();
			return;
		}
		waitForDomQuiet();
		if (_navigationStarted) {
			// A handler started the navigation while the DOM-quiet wait was running: it is a page
			// load after all, so wait it out rather than capture across it.
			settleAfterNavigatingClick();
		}
	}

	/**
	 * The page-load settle as a <em>click</em> needs it: wait for the new document to commit, then
	 * settle it.
	 *
	 * <p>
	 * The commit wait is what {@link #navigate} and {@link #back} get for free - their Playwright call
	 * returns only once the new document has committed - and what a click does not: a click is known
	 * to have started a navigation while that navigation is still in flight. Settling straight away
	 * would ask the <em>old</em> document's lifecycle whether it had loaded and gone quiet, which it
	 * long since had, so every wait would return at once and the capture would run right across the
	 * commit - the old URL beside the new page's HTML.
	 * </p>
	 */
	private void settleAfterNavigatingClick() {
		if (awaitNavigationCommit()) {
			settleAfterLoad();
		} else {
			// The navigation was begun but no document followed it, so there is no new page to let
			// load and fall quiet - waiting for either would be waiting on the page that is still
			// here, which reached both long ago. Only the DOM-quiet stage still has something to do:
			// whatever the handler did instead of navigating may still be rendering.
			waitForDomQuiet();
		}
	}

	/**
	 * Waits, best-effort, for the top-level navigation this operation started to <b>commit</b> - for
	 * {@link #_mainFrameNavigations} to move past {@link #_navigationsAtAttemptStart}.
	 *
	 * <p>
	 * Bounded by the settle cap rather than by the page-load timeout, and for the same reason the
	 * network-quiet stage is: a navigation that was begun does not always land. A {@code 204}, a
	 * download, a {@code mailto:} link or a handler that changes its mind all start a main-frame
	 * request that no new document ever follows, and spending a page load on each of those would
	 * re-create the toll this whole change removes. On expiry the page is captured as it stands, which
	 * is the behaviour of every other stage here, and the caller skips the stages that only make sense
	 * for a document that did arrive - so a click whose navigation never lands ({@code window.stop()},
	 * a {@code 204}) costs the detection window, this wait and the DOM-quiet stages around it. At the
	 * default cap that is a <em>range</em>, not a figure, because how much of each DOM-quiet stage is
	 * spent depends on when the page next mutates: measured between 2.2 s (an ordinary page) and 5.0 s
	 * (a page that never falls still, and so spends every stage in full), with 3.2 s a typical
	 * restless case. Against the full page-load timeout it used to cost (31.5 s), even the worst of
	 * that range is a sixth.
	 * </p>
	 *
	 * <p>
	 * A no-op when the cap is {@code 0}: the operator has asked for the loaded page to be captured
	 * immediately, and nothing is lost by it - {@link #CAPTURE_SCRIPT} reads the URL, the HTML and the
	 * refs in one go, so a capture that runs across a commit is still internally consistent, it just
	 * describes whichever of the two documents it caught.
	 * </p>
	 *
	 * @return Whether a new document committed.
	 */
	private boolean awaitNavigationCommit() {
		long before = _navigationsAtAttemptStart;
		if (_mainFrameNavigations > before) {
			// Already landed - the ordinary link or form-submit click, where Playwright's own click
			// call did not return until the new document committed.
			return true;
		}
		if (_navigationSettleMillis <= 0) {
			return false;
		}
		try {
			_page.waitForCondition(() -> _mainFrameNavigations > before,
				new Page.WaitForConditionOptions().setTimeout(_navigationSettleMillis));
		} catch (RuntimeException neverCommitted) {
			// The navigation was begun but no document followed; capture what is there.
		}
		return _mainFrameNavigations > before;
	}

	/**
	 * Waits up to {@link #CLICK_NAVIGATION_DETECTION_MILLIS} for the browser to report that the
	 * operation started a top-level navigation.
	 *
	 * <p>
	 * {@link Page#waitForCondition(java.util.function.BooleanSupplier, Page.WaitForConditionOptions)}
	 * rather than a sleep, for two reasons: it returns the instant the flag is raised, so the ordinary
	 * navigating click pays nothing; and it is a Playwright call, which is the only state in which the
	 * session's confined thread dispatches the very browser events that raise the flag - a plain sleep
	 * would sit there deaf and always time out.
	 * </p>
	 *
	 * <p>
	 * The cost of that, for a click that starts no navigation, is the full window spent inside
	 * Playwright's {@code while (!done) connection.processOneMessage()} loop with its 10 ms transport
	 * poll - a busy wait on this session's confined thread, not a parked one. It is bounded and it is
	 * one thread per live session, so it is affordable at the default four concurrent sessions; it is
	 * named here because the alternative reading, "this is a cheap sleep", is wrong.
	 * </p>
	 *
	 * @return Whether a top-level navigation was observed.
	 */
	private boolean awaitNavigationStart() {
		if (_navigationStarted) {
			// Already raised while the click itself was running; no need to wait at all.
			return true;
		}
		try {
			_page.waitForCondition(() -> _navigationStarted,
				new Page.WaitForConditionOptions().setTimeout(CLICK_NAVIGATION_DETECTION_MILLIS));
		} catch (RuntimeException noNavigation) {
			// The window elapsed: the click stayed on the page.
		}
		return _navigationStarted;
	}

	/**
	 * Waits for the current document's {@code load} event within the page-load timeout. Best-effort:
	 * on expiry the page is captured in whatever state it reached, exactly as the network wait it
	 * replaced did.
	 *
	 * <p>
	 * {@code load} rather than {@code domcontentloaded}: the navigating verbs already return at
	 * {@code domcontentloaded} ({@link #navigate}, {@link #back} pass it as their
	 * {@code waitUntil}), so waiting for it again would be a no-op, and the sub-resources whose
	 * arrival {@code load} marks - stylesheets, images, deferred scripts - are what decide whether the
	 * captured page looks finished. It is also the last milestone a page reaches deterministically:
	 * unlike network-idle, {@code load} fires on a polling page too.
	 * </p>
	 */
	private void waitForLoad() {
		try {
			_page.waitForLoadState(LoadState.LOAD,
				new Page.WaitForLoadStateOptions().setTimeout(_navigationTimeoutMillis));
		} catch (RuntimeException loadTimeout) {
			// Proceed with whatever has rendered so far.
		}
	}

	/**
	 * Waits, best-effort, until the page has made no network request for Playwright's idle window, or
	 * until the settle cap {@link #_navigationSettleMillis} elapses - whichever comes first. A no-op
	 * when the cap is {@code 0} (settle disabled).
	 *
	 * <p>
	 * Capped by the settle cap and <b>not</b> by {@link #_navigationTimeoutMillis}: network-idle is a
	 * best-effort signal that a whole class of ordinary pages never produces - anything that polls,
	 * long-polls, streams or beacons, which today means most SSO login pages and most SPAs - so the
	 * wait has to be sized as "a brief chance for client-side rendering to start", not as "a page
	 * load". With the page-load timeout it silently became a fixed half-minute toll on exactly those
	 * pages while adding nothing on the pages that do go idle, which reach idle in well under the cap.
	 * </p>
	 */
	private void waitForNetworkQuiet() {
		if (_navigationSettleMillis <= 0) {
			return;
		}
		try {
			_page.waitForLoadState(LoadState.NETWORKIDLE,
				new Page.WaitForLoadStateOptions().setTimeout(_navigationSettleMillis));
		} catch (RuntimeException notQuiet) {
			// Expected on any page that polls or beacons; proceed with what has rendered so far.
		}
	}

	/**
	 * Waits until the DOM has gone quiet (no mutation for {@link #DOM_QUIET_DEBOUNCE_MILLIS}) or the
	 * configured hard cap {@link #_navigationSettleMillis} elapses, whichever comes first, so
	 * late-injected content is captured. A no-op when the cap is {@code 0} (settle disabled), which
	 * restores the previous immediate-capture behaviour. The in-page promise always resolves within
	 * the cap, and the cap is bounded well inside {@link #navigationDeadlineMillis()}, so this can
	 * never turn a navigation into a hang; best-effort, so any evaluation error is swallowed.
	 */
	private void waitForDomQuiet() {
		if (_navigationSettleMillis <= 0) {
			return;
		}
		try {
			_page.evaluate(DOM_SETTLE_SCRIPT, Integer.toString(_navigationSettleMillis));
		} catch (RuntimeException ex) {
			// Proceed with whatever has rendered so far.
		}
	}

	/**
	 * Starts a fresh policy-observation window for one operation, discarding whatever the previous
	 * one recorded. Must be called before the Playwright call that may navigate, so
	 * {@link #_offPolicyNavigation} and {@link #_navigationStarted} can only ever describe the
	 * navigation being attempted now.
	 */
	private void beginNavigationAttempt() {
		_offPolicyNavigation = null;
		_navigationStarted = false;
		_navigationsAtAttemptStart = _mainFrameNavigations;
	}

	/**
	 * Takes the off-policy hop recorded since the current observation window opened, clearing it so it
	 * is reported exactly once.
	 *
	 * <p>
	 * Clearing on read matters for the verbs that do <em>not</em> open a window of their own
	 * ({@link #snapshot}, {@link #screenshot}, and {@link #type} after its fill): they have to honour a
	 * hop that was recorded after the last navigation returned, which means they must also not re-report
	 * one that has already been reported - a snapshot of a page the session is legitimately on would
	 * otherwise keep failing with the previous operation's refusal.
	 * </p>
	 *
	 * @return The recorded refusal message, or {@code null} when no off-policy hop was seen.
	 */
	private String takeOffPolicyNavigation() {
		String recorded = _offPolicyNavigation;
		_offPolicyNavigation = null;
		return recorded;
	}

	/**
	 * Fails the operation when the document the page is on <em>now</em> is one the policy forbids - the
	 * check {@link #verifyNavigationPolicy} performs for a navigating verb, for the three verbs that do
	 * not navigate themselves ({@link #snapshot}, {@link #screenshot}, {@link #type}).
	 *
	 * <p>
	 * All three still need it: a page can take its own top-level frame somewhere off-policy at any
	 * moment (a timer, a form submit), and the {@link FilteringProxy} answers a refused main-frame
	 * request with a {@code 403} page that commits as the document. Without this check that recorded
	 * hop, or that refusal page, is handed to the model as ordinary page content - exactly what
	 * {@link #installProxyRefusalGuard} exists to prevent on the other verbs.
	 * </p>
	 *
	 * <p>
	 * A document with no http(s) origin is left alone: {@code about:blank} on a session that has not
	 * navigated yet is the normal starting state, a {@code data:}/{@code blob:} document was produced by
	 * a page whose own origin was already judged, and {@code chrome-error://chromewebdata/} is what
	 * Chromium commits after an aborted navigation - which is where a session ends up once a hop has been
	 * refused. None of them carries content fetched from a network origin, so there is nothing for the
	 * domain policy to decide about; the recorded hop above is what fails such an operation.
	 * </p>
	 */
	private void verifyCurrentDocumentPolicy() {
		String offPolicyHop = takeOffPolicyNavigation();
		if (offPolicyHop != null) {
			throw new BrowserPolicyException(offPolicyHop);
		}
		String url = _page.url();
		URI uri;
		try {
			uri = URI.create(url);
		} catch (RuntimeException ex) {
			// Not a URL this process can judge; there is no origin to compare against a policy.
			return;
		}
		String scheme = uri.getScheme();
		if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
			return;
		}
		verifyNavigationPolicy(url);
	}

	/**
	 * Fails the operation when the navigation it performed violated the security policy - either
	 * because the document the browser ended up on is off-policy, or because some intermediate
	 * top-level hop was.
	 *
	 * <p>
	 * Checking the recorded hop as well as the final URL is what stops a redirect chain from
	 * <em>laundering</em> an off-policy origin: {@code allowed.example} → an internal or blocklisted
	 * host → back to {@code allowed.example} would leave a permitted final URL while the browser had
	 * meanwhile been driven somewhere the policy forbids. Both are reported as a
	 * {@link BrowserPolicyException} with the same wording, so the model sees one consistent,
	 * actionable message however the off-policy origin was reached.
	 * </p>
	 *
	 * <p>
	 * This is deliberately stricter than checking the final URL alone: an allowlisted page that tries
	 * to take the top-level frame somewhere the policy forbids now fails the operation, where it
	 * previously returned the (still-permitted) page it was in the middle of leaving. Reporting that is
	 * the honest answer - the site wants to be somewhere the operator has ruled out - and it is the
	 * only way the laundering case above can be caught at all. It cannot misfire in
	 * {@code allow-any-domains} mode, where every public origin is permitted and so nothing is ever
	 * recorded.
	 * </p>
	 *
	 * @param finalUrl
	 *        The URL of the document the navigation ended on.
	 */
	private void verifyNavigationPolicy(String finalUrl) {
		String offPolicyHop = takeOffPolicyNavigation();
		if (offPolicyHop != null) {
			throw new BrowserPolicyException(offPolicyHop);
		}
		// Belt-and-suspenders: re-validate the URL the browser actually landed on.
		String ssrf = UrlSafety.validateUrl(URI.create(finalUrl));
		if (ssrf != null) {
			throw new BrowserPolicyException(blockedAddressMessage(ssrf));
		}
		if (!_policy.isNavigationPermitted(URI.create(finalUrl).getHost())) {
			throw new BrowserPolicyException(blockedOriginMessage(finalUrl));
		}
	}

	/**
	 * Translates a failed page load into the tool's own vocabulary, so no raw Playwright error text
	 * (a {@code net::ERR_FAILED} / {@code TimeoutError} carrying a driver JavaScript stack trace) ever
	 * reaches the model.
	 *
	 * <p>
	 * A load that the security route aborted is reported as the policy block it actually was; that is
	 * the whole point of recording {@link #_offPolicyNavigation}, because an aborted main-frame request
	 * is indistinguishable from a network failure at the Playwright level - and, when the abort hits a
	 * redirect hop, the load can even sit out the full navigation timeout waiting for a document that
	 * will never commit. A genuine timeout becomes a
	 * {@link BrowserNavigationTimeoutException}; anything else keeps its (first-line-only) Playwright
	 * message.
	 * </p>
	 *
	 * @param url
	 *        The URL whose load failed.
	 * @param failure
	 *        The Playwright exception the load raised.
	 * @return The exception to throw instead; never {@code null}.
	 */
	private RuntimeException translateNavigationFailure(String url, PlaywrightException failure) {
		String offPolicyHop = takeOffPolicyNavigation();
		if (offPolicyHop != null) {
			return new BrowserPolicyException(offPolicyHop);
		}
		if (failure instanceof TimeoutError) {
			return new BrowserNavigationTimeoutException(url, _navigationTimeoutMillis);
		}
		return new IllegalStateException("the page at " + url + " could not be loaded: " + terse(failure));
	}

	/**
	 * Translates a failed element action (click/type) into the tool's own vocabulary: a bounded
	 * actionability timeout becomes the recoverable {@link BrowserRefNotActionableException}, while
	 * anything else keeps its (first-line-only) Playwright message.
	 *
	 * @param ref
	 *        The ref that was acted on.
	 * @param action
	 *        The attempted action, phrased for the message (e.g. {@code "clicked"}).
	 * @param failure
	 *        The Playwright exception the action raised.
	 * @return The exception to throw instead; never {@code null}.
	 */
	private RuntimeException translateActionFailure(String ref, String action, PlaywrightException failure) {
		String offPolicyHop = takeOffPolicyNavigation();
		if (offPolicyHop != null) {
			// The action itself worked but triggered a navigation the policy refused.
			return new BrowserPolicyException(offPolicyHop);
		}
		if (failure instanceof TimeoutError) {
			return new BrowserRefNotActionableException(ref, action, _actionTimeoutMillis);
		}
		return new IllegalStateException(
			"the element for ref \"" + ref + "\" could not be " + action + ": " + terse(failure));
	}

	/**
	 * The first line of a Playwright exception's message. The full text is a multi-line dump of the
	 * driver's own JavaScript stack trace, which is noise in a tool result (and, exempted from the
	 * agent loop's truncation, noise that is not even trimmed); the first line carries the actual
	 * cause.
	 */
	private static String terse(PlaywrightException failure) {
		String message = failure.getMessage();
		if (message == null || message.isBlank()) {
			return failure.getClass().getSimpleName();
		}
		for (String line : message.split("\n")) {
			String trimmed = line.strip();
			// The Playwright Java client renders the driver error as "Error {\n message='...'\n ...".
			if (trimmed.startsWith("message='")) {
				return trimmed.substring("message='".length()).strip();
			}
		}
		return message.split("\n")[0].strip();
	}

	/** The message reported when a navigation was refused because of SSRF protection. */
	private static String blockedAddressMessage(String ssrfReason) {
		return "Navigation ended at a blocked address: " + ssrfReason;
	}

	/**
	 * The message reported when the {@link FilteringProxy} refused the navigation outright, so the
	 * request never left the machine.
	 */
	private static String refusedByPolicyMessage(String reason) {
		return "Navigation was refused by the browser security policy before the request left the "
			+ "machine: " + reason + " The page was NOT loaded. Stay on a permitted, public origin, or "
			+ "report that this source is not reachable.";
	}

	/**
	 * The message reported when a navigation was refused by the domain policy. Deliberately names the
	 * policy as a whole rather than one list: {@link DomainPolicy} answers only whether a host is
	 * permitted, so which of the blocklist or the allowlist refused it is not knowable here (unlike in
	 * {@code BrowserTool}'s pre-flight check, which consults both and can say).
	 */
	private static String blockedOriginMessage(String url) {
		String host = null;
		try {
			host = URI.create(url).getHost();
		} catch (RuntimeException ex) {
			// Keep the URL-only wording below.
		}
		StringBuilder message = new StringBuilder();
		message.append("Navigation ended at a blocked or off-allowlist origin: ").append(url);
		if (host != null && !host.isBlank()) {
			message.append(" - the domain '").append(host)
				.append("' is not permitted by the browser domain policy (allowlist/blocklist) "
					+ "configured by the administrator");
		}
		message.append(". The page was NOT loaded. Stay on a permitted origin, or report that this "
			+ "source is not reachable.");
		return message.toString();
	}

	/**
	 * Records {@code url} as the off-policy top-level navigation of the operation currently running,
	 * unless one is already recorded (the first one is the cause; later ones are consequences).
	 */
	private void recordOffPolicyNavigation(URI uri, String ssrfReason) {
		if (_offPolicyNavigation != null) {
			return;
		}
		_offPolicyNavigation =
			ssrfReason != null ? blockedAddressMessage(ssrfReason) : blockedOriginMessage(uri.toString());
	}

	/**
	 * Captures the document the page is showing: its URL, its HTML, its interactive elements and its
	 * accessibility snapshot.
	 *
	 * <p>
	 * The URL is <b>not</b> passed in and is not read with {@code page.url()}: it comes out of
	 * {@link #CAPTURE_SCRIPT} together with the HTML and the refs, so the four cannot describe two
	 * different documents - see that constant's "one view of one document" note for why a timeout
	 * cannot substitute for this. The only part that is still a round trip of its own is the
	 * accessibility snapshot, which Playwright generates and no page script can produce; a document
	 * that commits while it is being read is therefore caught afterwards by re-reading the page once
	 * (below), rather than being handed back half old and half new.
	 * </p>
	 */
	private PageView capture(int status, Map<String, String> responseHeaders, boolean navigated) {
		long documentBefore = _mainFrameNavigations;
		PageView view = captureOnce(status, responseHeaders, navigated);
		if (_mainFrameNavigations != documentBefore) {
			// A navigation committed while this capture was being read, so the accessibility snapshot
			// may belong to a different document than the rest of it. Read the page once more; a
			// second commit inside the few milliseconds a re-read takes is not worth a loop.
			view = captureOnce(status, responseHeaders, navigated);
		}
		return view;
	}

	/** One pass of {@link #capture}; see there for why it is sometimes run twice. */
	private PageView captureOnce(int status, Map<String, String> responseHeaders, boolean navigated) {
		// Tag every ref minted below with the current page generation (the caller advances it when a
		// navigation moved to a new page); refs from any earlier generation are stale.
		long generation = _pageGeneration;
		CaptchaSignal captcha = new CaptchaSignal(false, null);
		List<InteractiveElement> elements = new ArrayList<>();
		// Fallbacks for the case where the evaluation itself fails (a document being replaced right
		// now, a page that forbids script evaluation): the capture stays best-effort, so fall back to
		// Playwright's own reads rather than failing the whole call.
		String finalUrl = null;
		String html = null;
		try {
			// Pass the generation as a String: Playwright's Java arg serializer rejects Long, and JS
			// string concatenation ('s' + gen + 'e' + i) yields the same s<gen>e<index> ref either way.
			Object raw = _page.evaluate(CAPTURE_SCRIPT, Long.toString(generation));
			if (raw instanceof Map<?, ?> map) {
				Object kind = map.get("captcha");
				if (kind instanceof String s && !s.isBlank()) {
					captcha = new CaptchaSignal(true, s);
				}
				Object list = map.get("elements");
				if (list instanceof List<?> items) {
					for (Object item : items) {
						if (item instanceof Map<?, ?> el) {
							elements.add(new InteractiveElement(
								str(el.get("ref")), str(el.get("role")), str(el.get("tag")),
								str(el.get("type")), str(el.get("name"))));
						}
					}
				}
				if (map.get("url") instanceof String url && !url.isBlank()) {
					finalUrl = url;
				}
				if (map.get("html") instanceof String captured) {
					html = captured;
				}
			}
		} catch (RuntimeException ex) {
			// Extraction of refs/captcha is best-effort; never fail the whole call over it.
			Logger.warn("Browser element extraction failed for " + _page.url() + ".", ex,
				BrowserSession.class);
		}
		if (finalUrl == null) {
			finalUrl = _page.url();
		}
		if (html == null) {
			html = _page.content();
		}
		// Bounded by the action timeout, not the page-load timeout: this reads the already-loaded
		// document, so a body that is not there now will not appear by waiting a page load for it.
		String ariaSnapshot = _page.locator("body").ariaSnapshot(
			new Locator.AriaSnapshotOptions().setTimeout(_actionTimeoutMillis));
		// Always-on, non-evasive block-page detection: flag a bot-protection / access-denied page so
		// the agent gets a distinct signal instead of an unexplained empty element list.
		BlockSignal block = BotWallDetector.detect(status, responseHeaders, html);
		return new PageView(finalUrl, status, html, ariaSnapshot, elements, captcha, block, navigated);
	}

	private static String str(Object value) {
		return value == null ? "" : value.toString();
	}

	private static String refSelector(String ref) {
		String safe = ref == null ? "" : ref.replace("\"", "");
		return "[data-tl-ref=\"" + safe + "\"]";
	}

	/**
	 * Rejects a ref that was not minted by the current snapshot generation, before it can be
	 * resolved to an element. This is what turns a carried-over ref from an earlier page into an
	 * explicit, actionable error instead of a silent wrong action (or a long locator timeout).
	 *
	 * @param ref
	 *        The ref the caller passed.
	 * @throws StaleBrowserRefException
	 *         If {@code ref} is not tagged with the current generation.
	 */
	private void requireCurrentRef(String ref) {
		if (refGeneration(ref) != _pageGeneration) {
			throw new StaleBrowserRefException(ref);
		}
	}

	/**
	 * Parses the page generation encoded in a {@code s<gen>e<index>} ref, or {@code -1} if the ref is
	 * {@code null}/blank or not in that form (e.g. a bare {@code eN} ref pre-dating generation
	 * tagging, or arbitrary junk). A {@code -1} never equals a (non-negative) live generation, so such
	 * refs are always rejected. Package-visible for testing.
	 */
	static long refGeneration(String ref) {
		if (ref == null) {
			return -1L;
		}
		String s = ref.trim();
		if (s.length() < 4 || s.charAt(0) != 's') {
			return -1L;
		}
		int e = s.indexOf('e', 1);
		if (e <= 1 || e == s.length() - 1) {
			return -1L;
		}
		String genPart = s.substring(1, e);
		String idxPart = s.substring(e + 1);
		if (!isDigits(genPart) || !isDigits(idxPart)) {
			return -1L;
		}
		try {
			return Long.parseLong(genPart);
		} catch (NumberFormatException ex) {
			return -1L;
		}
	}

	private static boolean isDigits(String value) {
		for (int i = 0; i < value.length(); i++) {
			if (!Character.isDigit(value.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Installs a request route that enforces SSRF and the domain policy for every request the context
	 * issues (main document and sub-resources), and records the reason whenever it refuses a top-level
	 * navigation.
	 *
	 * <p>
	 * Recording matters because {@code route.abort()} is invisible at the Playwright API level: the
	 * pending {@code goto}/{@code click} only learns that "the request failed", and reports it as a
	 * {@code net::ERR_FAILED} or - when nothing else commits a document - by sitting out its whole
	 * timeout. {@link #translateNavigationFailure} turns the recorded reason back into the policy
	 * message the abort actually stood for.
	 * </p>
	 *
	 * <p>
	 * <b>Sub-resources are left to the proxy where the proxy is authoritative</b> (see
	 * {@link #_proxyIsAuthoritative}). {@link UrlSafety#validateUrl} resolves the host, so running it
	 * here costs a <em>blocking DNS lookup on the session's single confined thread</em> for every asset
	 * host of a page - name resolution serialised behind the page load, for a decision the proxy makes
	 * anyway on its own threads, from the same normaliser and the same {@link UrlSafety} rules, before a
	 * packet leaves the machine. Nothing is given up by not repeating it: a sub-resource has no
	 * top-level origin to record and no policy message to carry (a refused one is a missing asset, just
	 * as an aborted one always was), the allowlist never governed sub-resources here in the first place,
	 * and the proxy sees requests a context route cannot even observe (worker fetches). The main-frame
	 * navigation keeps its full in-browser check: that one is what mints the tool's policy message.
	 * </p>
	 */
	private void installSecurityRoute(BrowserContext context) {
		context.route("**/*", route -> {
			Request request = route.request();
			URI uri;
			try {
				uri = URI.create(request.url());
			} catch (IllegalArgumentException ex) {
				route.abort();
				return;
			}
			String scheme = uri.getScheme();
			if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
				// data:/blob:/about: etc. are harmless to rendering; only govern http(s).
				route.resume();
				return;
			}
			boolean mainFrameNavigation = isMainFrameNavigation(request);
			if (!mainFrameNavigation && _proxyIsAuthoritative) {
				// Sub-resource: the proxy has already decided this one, and asking here as well means a
				// blocking DNS lookup on the session's single confined thread for every asset host -
				// serialising name resolution behind the page load for a verdict that changes nothing.
				route.resume();
				return;
			}
			String ssrf = UrlSafety.validateUrl(uri);
			if (ssrf != null) {
				// SSRF: block private/loopback/etc. for ALL requests (main doc and sub-resources).
				if (mainFrameNavigation) {
					recordOffPolicyNavigation(uri, ssrf);
				}
				route.abort();
				return;
			}
			if (mainFrameNavigation && !_policy.isNavigationPermitted(uri.getHost())) {
				// Blocklist/allowlist govern only the top-level document origin; SSRF (above)
				// governs every request.
				recordOffPolicyNavigation(uri, null);
				route.abort();
				return;
			}
			route.resume();
		});
	}

	/**
	 * Installs a request listener that records a top-level navigation to an off-policy origin reached
	 * by a <b>server-side redirect</b>, which {@link #installSecurityRoute the security route} never
	 * sees.
	 *
	 * <p>
	 * <b>Why this is needed.</b> Playwright routes are matched once per request; when a handler calls
	 * {@code route.resume()} and the server answers with a {@code 3xx}, Chromium follows the
	 * {@code Location} itself and the handler is <em>not</em> invoked again for the redirected request
	 * (verified against Playwright 1.49: the request <em>event</em> fires for the hop, with
	 * {@code redirectedFrom} set, while the route does not). A redirect therefore cannot be aborted
	 * from a route at all - the browser has already issued the request by the time it is observable.
	 * This listener closes what <em>can</em> be closed: every off-policy top-level hop is recorded, so
	 * {@link #verifyNavigationPolicy} fails the operation even when the chain ends back on a permitted
	 * origin, and the model is told which origin was refused rather than being handed the page.
	 * </p>
	 *
	 * <p>
	 * <b>What this layer does and does not do.</b> The redirected request is still <em>issued</em> by
	 * the browser before anything in-process can observe it - blocking it there is not expressible with
	 * Playwright's routing API at all. That is why the browser is additionally constrained from
	 * outside: the request goes to the {@link FilteringProxy}, which refuses a forbidden destination
	 * before a packet leaves the machine. This listener is what translates that refusal into the tool's
	 * vocabulary - it records the off-policy hop so {@link #verifyNavigationPolicy} names the origin
	 * that was refused instead of surfacing the proxy's {@code 403} (or the tunnel failure) as a
	 * network error - and it independently fails a chain that ends back on a permitted origin.
	 * </p>
	 */
	private void installRedirectGuard(BrowserContext context) {
		context.onRequest(request -> {
			if (request.redirectedFrom() == null) {
				// Not a redirect hop: the route above already governed (and could abort) this one.
				return;
			}
			if (!isMainFrameNavigation(request)) {
				return;
			}
			URI uri;
			try {
				uri = URI.create(request.url());
			} catch (IllegalArgumentException ex) {
				return;
			}
			String scheme = uri.getScheme();
			if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
				return;
			}
			String ssrf = UrlSafety.validateUrl(uri);
			if (ssrf != null) {
				recordOffPolicyNavigation(uri, ssrf);
				return;
			}
			if (!_policy.isNavigationPermitted(uri.getHost())) {
				recordOffPolicyNavigation(uri, null);
			}
		});
	}

	/**
	 * Installs a response listener that turns a refusal by the {@link FilteringProxy} into the tool's
	 * own policy message.
	 *
	 * <p>
	 * The proxy answers a refused request with a {@code 403}, which is an entirely ordinary response as
	 * far as the browser is concerned: for a <em>top-level</em> request it commits as the document, so
	 * without this the agent would be handed the proxy's own refusal page as if it were the content of
	 * the site it asked for. Recognising the {@link FilteringProxy#REFUSAL_HEADER} marker instead lets
	 * {@link #verifyNavigationPolicy} fail the operation with the reason the proxy gave.
	 * </p>
	 *
	 * <p>
	 * Belt-and-braces rather than the primary path: the proxy only ever refuses a main-frame navigation
	 * that {@link #installSecurityRoute the route} or {@link #installRedirectGuard the redirect guard}
	 * also refuses, so in practice the reason is already recorded by the time this fires. It exists so
	 * that a future divergence between the two layers surfaces as a clean policy error rather than as
	 * proxy HTML masquerading as page content. Only main-frame responses are recorded: a refused
	 * sub-resource is a missing asset, not a failed operation - exactly as an aborted sub-resource
	 * request has always been.
	 * </p>
	 */
	private void installProxyRefusalGuard(BrowserContext context) {
		context.onResponse(response -> {
			String reason = response.headers().get(FilteringProxy.REFUSAL_HEADER);
			if (reason == null || reason.isBlank()) {
				return;
			}
			Request request = response.request();
			if (!isMainFrameNavigation(request)) {
				return;
			}
			if (_offPolicyNavigation == null) {
				_offPolicyNavigation = refusedByPolicyMessage(reason);
			}
		});
	}

	/**
	 * Installs the download gate of the browsing policy: a download the policy refuses is cancelled
	 * and whatever was already written is deleted.
	 *
	 * <p>
	 * Cancelling is the only thing a browser can do about a download it has already been offered -
	 * Chromium starts writing before Playwright hands the event over - so the guarantee is "the file
	 * does not survive the tool call", not "no byte was ever written". That is still the difference
	 * between a policy and a note in the documentation, because nothing in this application can reach
	 * a cancelled download: the temporary file is gone and no verb hands one to the agent.
	 * </p>
	 *
	 * <p>
	 * Nothing is reported back to the model, because nothing asked: no verb of this tool downloads a
	 * file, so a download is something a <em>page</em> started. The refusal is an audit line for the
	 * operator, which is who the policy is written by.
	 * </p>
	 *
	 * <p>
	 * There is deliberately no upload gate beside it. This browser has no upload path at all - no verb
	 * sets input files and no file chooser is answered - so an upload cannot happen, and a setting that
	 * claimed to govern one would be a security control that controls nothing.
	 * </p>
	 */
	private void installDownloadGuard(BrowserContext context) {
		context.onDownload(download -> {
			String url = download.url();
			String host;
			try {
				host = URI.create(url).getHost();
			} catch (RuntimeException ex) {
				// A download URL this process cannot even parse is not one the policy can clear.
				host = null;
			}
			String refusal = _policy.refuseDownload(host);
			if (refusal == null) {
				return;
			}
			try {
				download.cancel();
				download.delete();
			} catch (RuntimeException ex) {
				// The download may have finished before the event arrived; the delete above is the
				// part that matters and a failure of either must not break the page.
				Logger.warn("Could not cancel the off-policy download from " + url + ".", ex,
					BrowserSession.class);
			}
			Logger.info("Browser session " + _key + " refused a download from " + url + ": " + refusal,
				BrowserSession.class);
		});
	}

	/**
	 * Installs a request listener that records that the operation currently running has started a
	 * <b>top-level navigation</b>, so {@link #settleAfterClick()} can tell a navigating click from one
	 * that stayed on the page.
	 *
	 * <p>
	 * The main-frame <em>request</em> is the right signal to key on: the browser issues it as soon as
	 * it commits to fetching a new document, which for a link or a form submit is within the click
	 * itself, so the detection window can stay short no matter how slow the server answers. The
	 * alternative signals are all worse - the URL after the fact misses a reload and a same-address
	 * post, and the {@code framenavigated} event arrives only once the new document commits, which on
	 * a slow server is seconds later.
	 * </p>
	 *
	 * <p>
	 * Purely observational: it decides nothing about policy and aborts nothing. Redirect hops raise the
	 * flag again, which is harmless - it is already raised by then.
	 * </p>
	 */
	private void installNavigationObserver(BrowserContext context) {
		context.onRequest(request -> {
			if (isMainFrameNavigation(request)) {
				_navigationStarted = true;
			}
		});
	}

	/**
	 * Whether {@code request} is a navigation of a <b>top-level</b> frame, as every listener and the
	 * security route here needs to know.
	 *
	 * <p>
	 * Extracted because asking is not as simple as it reads: {@link Request#frame()} <em>throws</em>
	 * for a request whose frame does not exist yet - the {@code target="_blank"} popup, whose
	 * navigation request is issued before the browser has created the frame it belongs to - and an
	 * exception thrown out of a Playwright listener propagates through the dispatch loop and fails
	 * whatever Playwright call this session's thread happens to be inside. The answer in that case is
	 * "yes": a request with no frame of its own is opening a new top-level document, never a
	 * sub-resource of an existing frame, so it is the answer that keeps the security route checking
	 * rather than waving it through.
	 * </p>
	 */
	private static boolean isMainFrameNavigation(Request request) {
		if (!request.isNavigationRequest()) {
			return false;
		}
		try {
			return request.frame().parentFrame() == null;
		} catch (PlaywrightException noFrameYet) {
			// The frame does not exist yet (a popup being opened): a top-level document either way.
			return true;
		}
	}

	/**
	 * Watches the main frame <b>having navigated</b>, which answers two different questions.
	 *
	 * <p>
	 * It completes {@link #installNavigationObserver} for the navigations that issue <b>no request at
	 * all</b>: a same-document hop - a fragment link, or the
	 * {@code history.pushState}/{@code replaceState} that a router-driven application navigates with.
	 * Those still replace what the page shows, so they deserve the page-load settle, and only
	 * {@code framenavigated} reports them.
	 * </p>
	 *
	 * <p>
	 * And it counts commits into {@link #_mainFrameNavigations}, which is what
	 * {@link #awaitNavigationCommit()} waits on.
	 * </p>
	 *
	 * <p>
	 * Installed on the page rather than the context because {@code framenavigated} is a page-level
	 * event; this session owns exactly one page for its lifetime, so one registration covers it.
	 * Sub-frames are ignored: an advert or a widget in an iframe navigating is not this page loading.
	 * </p>
	 */
	private void installNavigationCommitObserver(Page page) {
		page.onFrameNavigated(frame -> {
			if (frame.parentFrame() == null) {
				_navigationStarted = true;
				_mainFrameNavigations++;
			}
		});
	}

	private long startupDeadlineMillis() {
		// Chromium launch can be slow on a cold cache; give it generous headroom over one nav.
		return _navigationTimeoutMillis + 60_000L;
	}

	private long navigationDeadlineMillis() {
		// A navigation does DOMCONTENTLOADED then waits for the load event (each up to the timeout),
		// then the two settle stages (each bounded by the settle cap, itself clamped to the timeout),
		// plus extraction; the outer deadline caps a genuine hang without pre-empting normal work.
		return 2L * _navigationTimeoutMillis + 15_000L;
	}

	private <T> T callOnThread(Callable<T> op, long deadlineMillis) {
		Future<T> future = _executor.submit(op);
		try {
			return future.get(deadlineMillis, TimeUnit.MILLISECONDS);
		} catch (TimeoutException ex) {
			future.cancel(true);
			throw new BrowserSessionBrokenException(
				"Browser operation did not complete within " + deadlineMillis + " ms; session discarded.");
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof RuntimeException re) {
				throw re;
			}
			if (cause instanceof Error err) {
				throw err;
			}
			throw new IllegalStateException(cause != null ? cause.getMessage() : ex.getMessage(), ex);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while awaiting a browser operation.", ex);
		}
	}

	/**
	 * Test-only: loads {@code html} directly into the page (via {@code setContent}, bypassing
	 * navigation and its URL/security checks) and captures it as a fresh page - advancing the page
	 * generation exactly as a real navigation would. Lets gated shadow-DOM tests exercise the real
	 * {@link #CAPTURE_SCRIPT} against a controlled fixture without a network fixture or a loopback
	 * server (which SSRF would block). Package-visible for testing.
	 */
	PageView loadHtmlForTest(String html) {
		return callOnThread(() -> {
			_page.setContent(html, new Page.SetContentOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
			_pageGeneration++;
			return capture(-1, null, true);
		}, navigationDeadlineMillis());
	}

	/**
	 * Test-only: loads {@code html} via {@code setContent} and then runs the real page-load settle
	 * {@link #settleAfterLoad()} (load wait + capped network-quiet + bounded DOM-quiet) before
	 * capturing, exactly as {@link #navigate}/{@link #back} do. Lets gated E2E tests prove that
	 * content injected shortly after load is captured with the settle on, yet absent with it
	 * disabled, without a network fixture or loopback server (which SSRF would block).
	 * Package-visible for testing.
	 */
	PageView loadHtmlWithSettleForTest(String html) {
		return callOnThread(() -> {
			_page.setContent(html, new Page.SetContentOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
			settleAfterLoad();
			_pageGeneration++;
			return capture(-1, null, true);
		}, navigationDeadlineMillis());
	}

	/**
	 * Test-only: evaluates {@code js} on the current page (on the session thread) and returns the
	 * result. Used by gated E2E tests to assert the evasion fingerprint (e.g.
	 * {@code navigator.webdriver}, {@code navigator.userAgent}) without a network fixture.
	 * Package-visible for testing.
	 */
	Object evaluateForTest(String js) {
		return callOnThread(() -> _page.evaluate(js), navigationDeadlineMillis());
	}

	/**
	 * Test-only: clicks the element previously assigned {@code ref} and re-captures, going through
	 * the real generation check ({@link #requireCurrentRef}) and the shadow-piercing
	 * {@code [data-tl-ref]} locator ({@link #refSelector}), but skipping the URL re-validation that a
	 * synthetic {@code setContent} fixture (whose URL stays {@code about:blank}) would otherwise trip.
	 * Package-visible for testing.
	 */
	PageView activateRefForTest(String ref) {
		return callOnThread(() -> {
			requireCurrentRef(ref);
			beginNavigationAttempt();
			try {
				_page.locator(refSelector(ref))
					.click(new Locator.ClickOptions().setTimeout(_actionTimeoutMillis));
			} catch (PlaywrightException ex) {
				throw translateActionFailure(ref, "clicked", ex);
			}
			return capture(-1, null, false);
		}, navigationDeadlineMillis());
	}

	/**
	 * Test-only: the underlying Chromium <em>build's</em> native user-agent, read from a throwaway
	 * context that carries none of this session's evasion overrides, so it reflects the launched
	 * binary rather than the masked UA. Distinguishes the launched build: the stripped
	 * {@code chrome-headless-shell} reports its exact patch version (e.g.
	 * {@code .../131.0.6778.33 ...}), whereas the full Chromium build applies User-Agent Reduction
	 * and freezes the version to {@code <major>.0.0.0}. Package-visible for testing.
	 */
	String rawBuildUserAgentForTest() {
		return callOnThread(() -> {
			BrowserContext probe = _browser.newContext();
			try {
				return String.valueOf(probe.newPage().evaluate("navigator.userAgent"));
			} finally {
				probe.close();
			}
		}, navigationDeadlineMillis());
	}

	/**
	 * Test-only: the OS handle of this session's Playwright driver process, or {@code null} when it
	 * could not be identified. Lets a test assert that the process tree is really gone after a
	 * teardown. Package-visible for testing.
	 */
	ProcessHandle driverProcessForTest() {
		return _driverProcess;
	}

	/**
	 * Test-only: occupies the session thread for {@code millis} in a way that {@link Thread#interrupt()
	 * interruption} cannot end, reproducing the state a session is in after a
	 * {@link BrowserSessionBrokenException} - blocked in a native Playwright pipe read, which the
	 * {@code cancel(true)} in {@link #callOnThread} provably cannot break out of. Returns immediately;
	 * the wedge runs on the session thread. Package-visible for testing.
	 */
	void wedgeSessionThreadForTest(long millis) {
		long until = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(millis);
		_executor.execute(() -> {
			// Deliberately a spin rather than a sleep: a sleep would end on interrupt, and a wedge
			// that interruption can end is not the failure this reproduces.
			while (System.nanoTime() < until) {
				Thread.onSpinWait();
			}
		});
	}

	/**
	 * Closes the page, context, browser and Playwright instance, and stops the session thread -
	 * falling back to destroying the driver process tree when the session thread is unresponsive.
	 *
	 * <p>
	 * Must never return while a Chromium of this session is still running: the pool releases the
	 * session's slot the moment this returns, so a browser that outlives it is a process the
	 * {@code max-concurrent-sessions} cap no longer counts.
	 * </p>
	 */
	void close() {
		close(GRACEFUL_CLOSE_TIMEOUT_MILLIS);
	}

	/**
	 * {@link #close()} with an explicit budget for the graceful, API-level teardown.
	 *
	 * <p>
	 * Package-visible so the broken-session path can be exercised in a test without waiting out the
	 * production budget; production always uses {@link #GRACEFUL_CLOSE_TIMEOUT_MILLIS}.
	 * </p>
	 *
	 * @param gracefulTimeoutMillis
	 *        How long the orderly close on the session thread is given before the process tree is
	 *        destroyed instead.
	 */
	synchronized void close(long gracefulTimeoutMillis) {
		if (_closed) {
			// Idempotent: a second call has nothing left to close, and must not report the already
			// released driver handle as one that could never be identified.
			return;
		}
		_closed = true;
		boolean closedCleanly = false;
		try {
			// Give teardown its own bounded task so a wedged page can't hang shutdown forever.
			Future<Void> closed = _executor.submit(() -> {
				closeQuietly(_context);
				closeQuietly(_browser);
				closeQuietly(_playwright);
				return null;
			});
			try {
				closed.get(gracefulTimeoutMillis, TimeUnit.MILLISECONDS);
				closedCleanly = true;
			} catch (TimeoutException | ExecutionException | InterruptedException ex) {
				if (ex instanceof InterruptedException) {
					Thread.currentThread().interrupt();
				}
				Logger.warn("Browser session " + _key + " did not close cleanly.", ex, BrowserSession.class);
			}
		} catch (RuntimeException ex) {
			Logger.warn("Failed to schedule browser session close for " + _key + ".", ex, BrowserSession.class);
		} finally {
			_executor.shutdownNow();
		}
		// Last resort, and the whole point of retaining the handle: the teardown above runs on the
		// SESSION thread, which is precisely the thread that is wedged whenever a
		// BrowserSessionBrokenException was raised. Its task then never ran, shutdownNow() dropped it,
		// and nothing has closed the browser. Destroy the process tree from HERE - the pool's teardown
		// thread - so no Chromium survives a session the pool has already written off.
		destroyDriverProcessTree(closedCleanly);
	}

	/**
	 * Destroys the driver process and everything below it (the Chromium browser process and its
	 * renderers), unless it is already gone.
	 *
	 * <p>
	 * On the clean path this is a no-op: {@code Playwright.close()} waits for the driver to exit, so
	 * there is nothing left alive. It is only reached for real when the orderly teardown could not
	 * run, which is why surviving processes are logged as an error rather than silently reaped - an
	 * operator seeing this repeatedly is seeing sessions wedge.
	 * </p>
	 *
	 * @param closedCleanly
	 *        Whether the API-level teardown completed; only used to phrase the log.
	 */
	private void destroyDriverProcessTree(boolean closedCleanly) {
		ProcessHandle driver = _driverProcess;
		_driverProcess = null;
		if (driver == null || !driver.isAlive()) {
			if (driver == null && !closedCleanly) {
				Logger.error("Browser session " + _key + " could not be closed and its driver process was "
					+ "never identified, so its Chromium may survive this session. Check for a Playwright "
					+ "version change breaking driver-process detection.", BrowserSession.class);
			}
			return;
		}
		// Snapshot the descendants BEFORE killing anything: once the driver dies its children are
		// reparented and would no longer be reachable from this handle.
		List<ProcessHandle> tree = new ArrayList<>(driver.descendants().toList());
		tree.add(driver);
		Logger.error("Browser session " + _key + " did not shut down through Playwright"
			+ (closedCleanly ? " completely" : " (its session thread is unresponsive)")
			+ "; destroying its driver process and " + (tree.size() - 1)
			+ " descendant process(es) so no browser outlives the session.", BrowserSession.class);
		tree.forEach(ProcessHandle::destroy);
		awaitExit(tree);
		for (ProcessHandle process : tree) {
			if (process.isAlive()) {
				process.destroyForcibly();
			}
		}
	}

	/** Waits at most {@link #PROCESS_TERMINATION_GRACE_MILLIS} for every process to have exited. */
	private static void awaitExit(List<ProcessHandle> processes) {
		CompletableFuture<?>[] exits =
			processes.stream().map(ProcessHandle::onExit).toArray(CompletableFuture[]::new);
		try {
			CompletableFuture.allOf(exits).get(PROCESS_TERMINATION_GRACE_MILLIS, TimeUnit.MILLISECONDS);
		} catch (TimeoutException | ExecutionException ex) {
			// Whatever is still alive is killed forcibly by the caller.
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * The OS handle of the {@code node} driver process behind {@code playwright}.
	 *
	 * <p>
	 * Read reflectively because the Java client exposes no accessor for it: {@code PlaywrightImpl}
	 * keeps the {@link Process} it spawned in a private field and offers only {@code close()}, which
	 * is exactly the call that is unavailable once the owning thread has wedged. There is no
	 * supported alternative - inferring the process from {@code ProcessHandle.current().children()}
	 * would be a guess, and guessing wrong means destroying an unrelated process, which is far worse
	 * than the leak this closes. A failure to read the field is therefore reported and tolerated
	 * (the session still works; only the last-resort teardown is unavailable) rather than fatal.
	 * </p>
	 *
	 * @param playwright
	 *        The freshly created Playwright instance.
	 * @return The driver's process handle, or {@code null} when it could not be determined.
	 */
	private static ProcessHandle driverProcessOf(Playwright playwright) {
		try {
			Field field = playwright.getClass().getDeclaredField(DRIVER_PROCESS_FIELD);
			field.setAccessible(true);
			Object value = field.get(playwright);
			if (value instanceof Process process) {
				return process.toHandle();
			}
			Logger.warn("The Playwright driver process could not be identified: field '"
				+ DRIVER_PROCESS_FIELD + "' does not hold a Process. A wedged browser session cannot be "
				+ "force-terminated.", BrowserSession.class);
		} catch (ReflectiveOperationException | RuntimeException ex) {
			Logger.warn("The Playwright driver process could not be identified. A wedged browser session "
				+ "cannot be force-terminated.", ex, BrowserSession.class);
		}
		return null;
	}

	private static void closeQuietly(AutoCloseable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (Exception ex) {
			Logger.warn("Failed to close a browser resource cleanly.", ex, BrowserSession.class);
		}
	}
}
