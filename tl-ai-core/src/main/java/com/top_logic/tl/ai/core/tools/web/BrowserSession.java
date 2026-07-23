/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
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
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.BlockSignal;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.CaptchaSignal;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.InteractiveElement;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.PageView;

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
 * the pool. SSRF and the domain policy are enforced by a per-context request route, so every
 * request the context issues (main document, redirects, sub-resources) stays guarded.
 * </p>
 */
final class BrowserSession {

	/** The domain policy for the top-level origin; SSRF is enforced separately and always. */
	interface DomainPolicy {
		/** Whether a top-level navigation to {@code host} is permitted (blocklist/allowlist/allow-any). */
		boolean isNavigationPermitted(String host);
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
			+ "  const out = [];\n"
			+ "  let i = 0;\n"
			+ "  for (const el of els) {\n"
			+ "    const style = window.getComputedStyle(el);\n"
			+ "    if (!style || style.display === 'none' || style.visibility === 'hidden') continue;\n"
			+ "    const rect = el.getBoundingClientRect();\n"
			+ "    if (rect.width === 0 && rect.height === 0) continue;\n"
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
			+ "  return {captcha, elements: out};\n"
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
	 * JavaScript run right after the network/load wait and before {@link #CAPTURE_SCRIPT}, to give
	 * late-injected content (cookie-consent banners, lazy widgets) time to appear in the DOM before
	 * the snapshot. It resolves when the DOM has been <em>quiet</em> - no {@code childList}/subtree
	 * mutation for {@link #DOM_QUIET_DEBOUNCE_MILLIS} - or when the hard cap (passed in as
	 * milliseconds) elapses, whichever comes first. A {@link MutationObserver} on {@code document}
	 * drives the quiet detection; a single {@code setTimeout} enforces the cap so a page that never
	 * stops mutating (constant animation/polling) still resolves promptly. A still page resolves
	 * after just the debounce window, so fast pages are barely slowed. The cap is passed as a string
	 * because Playwright's Java argument serializer rejects {@code Long} (mirrors {@link #CAPTURE_SCRIPT}).
	 */
	private static final String DOM_SETTLE_SCRIPT =
		"(capStr) => new Promise(resolve => {\n"
			+ "  const cap = parseInt(capStr, 10);\n"
			+ "  const quiet = " + DOM_QUIET_DEBOUNCE_MILLIS + ";\n"
			+ "  let quietTimer = null;\n"
			+ "  const finish = () => {\n"
			+ "    observer.disconnect();\n"
			+ "    if (quietTimer) clearTimeout(quietTimer);\n"
			+ "    clearTimeout(capTimer);\n"
			+ "    resolve();\n"
			+ "  };\n"
			+ "  const observer = new MutationObserver(() => {\n"
			+ "    if (quietTimer) clearTimeout(quietTimer);\n"
			+ "    quietTimer = setTimeout(finish, quiet);\n"
			+ "  });\n"
			+ "  const capTimer = setTimeout(finish, cap);\n"
			+ "  observer.observe(document, {childList: true, subtree: true});\n"
			// Start the debounce immediately: a page that never mutates resolves after one quiet
			// window rather than waiting out the whole cap.
			+ "  quietTimer = setTimeout(finish, quiet);\n"
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

	/** Hard cap (ms) of the post-navigation DOM-quiet settle; {@code 0} disables the settle. */
	private final int _navigationSettleMillis;

	private final DomainPolicy _policy;

	/** When {@code true} the browser is launched headed (a real window on the app host's desktop). */
	private final boolean _headed;

	/** When {@code true} the context is disguised as ordinary desktop Chrome (operator opt-in). */
	private final boolean _evadeBotDetection;

	private final ExecutorService _executor;

	private volatile Playwright _playwright;

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

	private BrowserSession(Object key, int navigationTimeoutMillis, int navigationSettleMillis,
			DomainPolicy policy, boolean headed, boolean evadeBotDetection) {
		_key = key;
		_navigationTimeoutMillis = navigationTimeoutMillis;
		_navigationSettleMillis = navigationSettleMillis;
		_policy = policy;
		_headed = headed;
		_evadeBotDetection = evadeBotDetection;
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
		return open(key, navigationTimeoutMillis, 1500, policy, false, false);
	}

	/**
	 * Opens a new isolated session.
	 *
	 * @param key
	 *        The session key (for diagnostics).
	 * @param navigationTimeoutMillis
	 *        The per-operation Playwright timeout.
	 * @param navigationSettleMillis
	 *        Hard cap (ms) of the post-navigation DOM-quiet settle run before a page-load capture
	 *        (see {@link #settle()}); {@code 0} disables the settle (immediate capture). Always
	 *        bounded well inside {@link #navigationDeadlineMillis()}.
	 * @param policy
	 *        The top-level-origin domain policy.
	 * @param headed
	 *        When {@code true}, launch Chromium headed (a visible window on the app host's desktop)
	 *        instead of headless (see {@code BrowserToolsService.Config#getHeadless()}); it requires
	 *        a local display.
	 * @param evadeBotDetection
	 *        When {@code true}, disguise the context as ordinary desktop Chrome (see
	 *        {@code BrowserToolsService.Config#isEvadeBotDetection()}): launch the <b>full</b>
	 *        Chromium build (channel "chromium", new headless) instead of the detectable
	 *        {@code chrome-headless-shell} (see {@link #buildLaunchOptions(boolean, boolean)}), add
	 *        the {@code --disable-blink-features=AutomationControlled} launch switch, set a realistic
	 *        Chrome user-agent/locale, and install a fingerprint-masking init script. When
	 *        {@code false} the behaviour is byte-for-byte the plain headless-shell default.
	 * @return A started session.
	 */
	static BrowserSession open(Object key, int navigationTimeoutMillis, int navigationSettleMillis,
			DomainPolicy policy, boolean headed, boolean evadeBotDetection) {
		BrowserSession session = new BrowserSession(key, navigationTimeoutMillis, navigationSettleMillis,
			policy, headed, evadeBotDetection);
		try {
			session.startOnThread();
		} catch (RuntimeException ex) {
			session.close();
			throw ex;
		}
		return session;
	}

	/**
	 * Builds the Chromium {@link BrowserType.LaunchOptions} from a session's launch flags. Extracted
	 * as a package-visible, side-effect-free static seam so the launch decision (which build,
	 * headless-or-not, which args) can be asserted in a unit test without actually launching a
	 * browser.
	 *
	 * <p>
	 * <b>Build selection.</b> With bot-detection evasion <em>off</em> the options are byte-for-byte
	 * the previous default: {@code --disable-dev-shm-usage --no-sandbox}, {@code headless} = not
	 * headed, and <em>no channel</em>, so Playwright launches its light, stripped
	 * {@code chrome-headless-shell} build. With evasion <em>on</em> the options additionally select
	 * the <b>full</b> Chromium distribution via {@link BrowserType.LaunchOptions#setChannel(String)
	 * channel "chromium"} - the {@code chromium-<rev>/chrome-linux/chrome} binary
	 * {@code playwright install} places alongside the shell. The headless shell is itself a
	 * detectable fingerprint that some bot walls / consent managers suppress content for even when
	 * the user-agent and {@code navigator.webdriver} are already masked; the full build is not
	 * detected the same way, so it is what makes the rest of the evasion (UA, locale, stealth
	 * init-script, {@code --disable-blink-features=AutomationControlled}) actually effective. The
	 * launch stays windowless: for the non-headed case {@code headless} remains {@code true}
	 * (channel "chromium" + headless = new headless, no visible window); the channel setting is
	 * orthogonal to {@code headless} and composes cleanly with the headed launch toggle
	 * ({@code headed=true} launches the same full build with a visible window).
	 * </p>
	 *
	 * <p>
	 * <b>Deployment prerequisite.</b> Channel "chromium" requires the full Chromium build to be
	 * present in the Playwright browser cache. {@code playwright install chromium} installs it
	 * (alongside the headless shell), but a slimmed deploy image that ships <em>only</em>
	 * {@code chrome-headless-shell} will fail to launch when evasion is enabled. Ship the full build
	 * wherever {@code evade-bot-detection} may be turned on.
	 * </p>
	 *
	 * @param headed
	 *        Whether to launch a visible window (headed launch toggle) instead of headless.
	 * @param evadeBotDetection
	 *        Whether bot-detection evasion is enabled (selects the full Chromium build).
	 * @return The launch options; never {@code null}.
	 */
	static BrowserType.LaunchOptions buildLaunchOptions(boolean headed, boolean evadeBotDetection) {
		// /dev/shm is only 64 MB in the default Docker sandbox; without this Chromium crashes
		// under memory pressure. Harmless on a normal host.
		List<String> launchArgs = new ArrayList<>(List.of("--disable-dev-shm-usage", "--no-sandbox"));
		if (evadeBotDetection) {
			// Drop the AutomationControlled blink feature (which sets navigator.webdriver and is a
			// cheap bot tell). Added only in evasion mode so the default arg set is unchanged.
			launchArgs.add("--disable-blink-features=AutomationControlled");
		}
		BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
			.setHeadless(!headed)
			.setArgs(launchArgs);
		if (evadeBotDetection) {
			// Select the FULL Chromium build (new headless) instead of the detectable
			// chrome-headless-shell. DEPLOYMENT PREREQUISITE: the full build must be installed
			// (`playwright install chromium`); an image shipping only chrome-headless-shell will fail
			// to launch here. Only set in evasion mode, so the default (off) path is unchanged.
			options.setChannel("chromium");
		}
		return options;
	}

	private void startOnThread() {
		callOnThread(() -> {
			_playwright = Playwright.create();
			BrowserType chromium = _playwright.chromium();
			_browser = chromium.launch(buildLaunchOptions(_headed, _evadeBotDetection));
			_context = _browser.newContext(newContextOptions());
			_context.setDefaultTimeout(_navigationTimeoutMillis);
			_context.setDefaultNavigationTimeout(_navigationTimeoutMillis);
			installSecurityRoute(_context);
			if (_evadeBotDetection) {
				// Mask the automation fingerprint before any page script runs (applies to every
				// document in the context, including the initial about:blank page created below).
				_context.addInitScript(STEALTH_INIT_SCRIPT);
			}
			_page = _context.newPage();
			Logger.info("Opened isolated " + (_headed ? "headed" : "headless") + " browser session " + _key
				+ (_evadeBotDetection ? " (bot-detection evasion on)." : "."),
				BrowserSession.class);
			return null;
		}, startupDeadlineMillis());
	}

	/**
	 * The context options for this session, or {@code null} when none are needed so the launch stays
	 * byte-for-byte the plain {@code newContext()} default. Evasion mode overrides the user-agent and
	 * locale.
	 */
	private Browser.NewContextOptions newContextOptions() {
		Browser.NewContextOptions options = null;
		if (_evadeBotDetection) {
			options = new Browser.NewContextOptions();
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
			Response response = _page.navigate(url, new Page.NavigateOptions()
				.setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
				.setTimeout(_navigationTimeoutMillis));
			settle();
			String finalUrl = response != null ? response.url() : _page.url();
			verifyFinalUrl(finalUrl);
			int status = response != null ? response.status() : -1;
			Map<String, String> headers = response != null ? response.headers() : null;
			// An explicit navigation always lands on a fresh document: start a new page generation so
			// refs from the previous page are detected as stale.
			_pageGeneration++;
			return capture(finalUrl, status, headers);
		}, navigationDeadlineMillis());
	}

	/** Captures the current page without navigating. */
	PageView snapshot() {
		return callOnThread(() -> capture(_page.url(), -1, null), navigationDeadlineMillis());
	}

	/** Clicks the element previously assigned {@code ref}, then captures the resulting page. */
	PageView click(String ref) {
		return callOnThread(() -> {
			requireCurrentRef(ref);
			String urlBefore = _page.url();
			Locator locator = _page.locator(refSelector(ref));
			locator.click(new Locator.ClickOptions().setTimeout(_navigationTimeoutMillis));
			settle();
			String finalUrl = _page.url();
			verifyFinalUrl(finalUrl);
			if (!finalUrl.equals(urlBefore)) {
				// The click navigated to a different page: refs from the old page are now stale.
				_pageGeneration++;
			}
			return capture(finalUrl, -1, null);
		}, navigationDeadlineMillis());
	}

	/** Types {@code text} into the element previously assigned {@code ref}, then captures. */
	PageView type(String ref, String text) {
		return callOnThread(() -> {
			requireCurrentRef(ref);
			Locator locator = _page.locator(refSelector(ref));
			locator.fill(text == null ? "" : text, new Locator.FillOptions().setTimeout(_navigationTimeoutMillis));
			return capture(_page.url(), -1, null);
		}, navigationDeadlineMillis());
	}

	/** Navigates back in this session's history, then captures. */
	PageView back() {
		return callOnThread(() -> {
			Response response = _page.goBack(new Page.GoBackOptions()
				.setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
				.setTimeout(_navigationTimeoutMillis));
			if (response == null) {
				// No history entry to go back to: the page is unchanged, so keep the generation.
				return capture(_page.url(), -1, null);
			}
			settle();
			String finalUrl = response.url();
			verifyFinalUrl(finalUrl);
			// Going back landed on a different page in history: start a new page generation.
			_pageGeneration++;
			return capture(finalUrl, response.status(), response.headers());
		}, navigationDeadlineMillis());
	}

	/**
	 * Settles a freshly loaded page before it is captured. Run only by the page-<em>loading</em>
	 * operations ({@link #navigate}, {@link #back} and a navigating {@link #click}), never by the
	 * explicit re-capture {@link #snapshot} or the same-page edit {@link #type}, which stay immediate.
	 *
	 * <p>
	 * Two stages: first the network/load wait (existing behaviour), then a bounded DOM-quiet wait so
	 * content injected shortly <em>after</em> navigation returns - cookie-consent banners, lazy
	 * widgets - is present in the snapshot rather than requiring the model to re-snapshot manually.
	 * </p>
	 */
	private void settle() {
		waitForNetworkIdle();
		waitForDomQuiet();
	}

	private void waitForNetworkIdle() {
		// Best-effort settle for client-side rendering; many SPAs never reach full idle.
		try {
			_page.waitForLoadState(LoadState.NETWORKIDLE,
				new Page.WaitForLoadStateOptions().setTimeout(_navigationTimeoutMillis));
		} catch (RuntimeException settleTimeout) {
			// Proceed with whatever has rendered so far.
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

	private void verifyFinalUrl(String finalUrl) {
		// Belt-and-suspenders: re-validate the URL the browser actually landed on.
		String ssrf = UrlSafety.validateUrl(URI.create(finalUrl));
		if (ssrf != null) {
			throw new IllegalStateException("Navigation ended at a blocked address: " + ssrf);
		}
		if (!_policy.isNavigationPermitted(URI.create(finalUrl).getHost())) {
			throw new IllegalStateException("Navigation ended at a blocked or off-allowlist origin: " + finalUrl);
		}
	}

	private PageView capture(String finalUrl, int status, Map<String, String> responseHeaders) {
		// Tag every ref minted below with the current page generation (the caller advances it when a
		// navigation moved to a new page); refs from any earlier generation are stale.
		long generation = _pageGeneration;
		CaptchaSignal captcha = new CaptchaSignal(false, null);
		List<InteractiveElement> elements = new ArrayList<>();
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
			}
		} catch (RuntimeException ex) {
			// Extraction of refs/captcha is best-effort; never fail the whole call over it.
			Logger.warn("Browser element extraction failed for " + finalUrl + ".", ex, BrowserSession.class);
		}
		String html = _page.content();
		String ariaSnapshot = _page.locator("body").ariaSnapshot(
			new Locator.AriaSnapshotOptions().setTimeout(_navigationTimeoutMillis));
		// Always-on, non-evasive block-page detection: flag a bot-protection / access-denied page so
		// the agent gets a distinct signal instead of an unexplained empty element list.
		BlockSignal block = BotWallDetector.detect(status, responseHeaders, html);
		return new PageView(finalUrl, status, html, ariaSnapshot, elements, captcha, block);
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
	 * Installs a request route that enforces SSRF and the domain policy for every request the
	 * context issues, including redirects and sub-resources.
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
			if (UrlSafety.validateUrl(uri) != null) {
				// SSRF: block private/loopback/etc. for ALL requests (main doc and sub-resources).
				route.abort();
				return;
			}
			boolean mainFrameNavigation =
				request.isNavigationRequest() && request.frame().parentFrame() == null;
			if (mainFrameNavigation && !_policy.isNavigationPermitted(uri.getHost())) {
				// Blocklist/allowlist govern only the top-level document origin; SSRF (above)
				// governs every request.
				route.abort();
				return;
			}
			route.resume();
		});
	}

	private long startupDeadlineMillis() {
		// Chromium launch can be slow on a cold cache; give it generous headroom over one nav.
		return _navigationTimeoutMillis + 60_000L;
	}

	private long navigationDeadlineMillis() {
		// A navigation does DOMCONTENTLOADED then a NETWORKIDLE settle (each up to the timeout),
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
			return capture(_page.url(), -1, null);
		}, navigationDeadlineMillis());
	}

	/**
	 * Test-only: loads {@code html} via {@code setContent} and then runs the real page-load
	 * {@link #settle()} (network wait + bounded DOM-quiet) before capturing, exactly as
	 * {@link #navigate}/{@link #back} do. Lets gated E2E tests prove that content injected shortly
	 * after load is captured with the settle on, yet absent with it disabled, without a network
	 * fixture or loopback server (which SSRF would block). Package-visible for testing.
	 */
	PageView loadHtmlWithSettleForTest(String html) {
		return callOnThread(() -> {
			_page.setContent(html, new Page.SetContentOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
			settle();
			_pageGeneration++;
			return capture(_page.url(), -1, null);
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
			_page.locator(refSelector(ref)).click(new Locator.ClickOptions().setTimeout(_navigationTimeoutMillis));
			return capture(_page.url(), -1, null);
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

	/** Closes the page, context, browser and Playwright instance, and stops the session thread. */
	void close() {
		try {
			// Give teardown its own bounded task so a wedged page can't hang shutdown forever.
			Future<Void> closed = _executor.submit(() -> {
				closeQuietly(_context);
				closeQuietly(_browser);
				closeQuietly(_playwright);
				return null;
			});
			try {
				closed.get(30_000L, TimeUnit.MILLISECONDS);
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
