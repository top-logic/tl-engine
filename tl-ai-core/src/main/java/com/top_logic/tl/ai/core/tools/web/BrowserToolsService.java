/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Service owning the pool of isolated, per-session headless Chromium browsers used by
 * {@link BrowserTool}.
 *
 * <p>
 * It holds the configuration (per-navigation timeout, page byte/character caps, the
 * administrator-owned domain allowlist/blocklist/allow-any policy, and the concurrency bounds) and
 * owns the {@link BrowserSessionPool}. Each agent session gets its <b>own</b>
 * {@link BrowserSession} - a dedicated Playwright/Browser/BrowserContext with separate
 * cookies/storage - reused across the many tool calls of a run. Two concurrent sessions can never
 * see each other's cookies, pages or results.
 * </p>
 *
 * <p>
 * <b>Concurrency model (replaces the former single global lock).</b> Sessions run fully
 * concurrently. A {@link BrowserSessionPool} caps the number of live sessions at
 * {@link Config#getMaxConcurrentSessions()} (a memory bound, not a throughput throttle). Starting a
 * new session waits at most {@link Config#getAcquireTimeoutSeconds()} for a free slot, reclaiming
 * the least-recently-used idle session if needed; if none frees up it fails fast with
 * {@link BrowserBusyException} rather than blocking the caller thread indefinitely. Idle sessions
 * are evicted after {@link Config#getSessionIdleTimeoutSeconds()}. Every browser operation runs
 * under a hard outer deadline, so one hung page can never pin a slot.
 * </p>
 *
 * <p>
 * Security is layered and applied in a fixed order of precedence for every navigation and every
 * in-browser request (main document, redirects and sub-resources):
 * </p>
 * <ol>
 * <li><b>SSRF always wins.</b> Any request to a private, loopback, link-local or otherwise
 * internal address is refused through {@link UrlSafety}, unconditionally. No configuration can
 * relax this.</li>
 * <li><b>The blocklist always applies.</b> A top-level origin on the administrator-owned
 * {@link Config#getBlockedDomains() blocklist} is refused, in every mode.</li>
 * <li><b>Then the allowlist - unless allow-any is enabled.</b> By default the top-level origin
 * must also be on the {@link Config#getAllowedDomains() allowlist}. When
 * {@link Config#isAllowAnyDomain() allow-any} is enabled, the public-domain allowlist is
 * skipped (any public domain that is neither SSRF-blocked nor blocklisted is reachable);
 * allow-any relaxes <em>only</em> the allowlist, never SSRF or the blocklist.</li>
 * </ol>
 * <p>
 * The allowlist, blocklist and allow-any flag are policy configuration owned by the
 * administrator - not model-supplied arguments. The default (allowlist-enforced, allow-any off)
 * is safe-by-default: an empty allowlist with allow-any off refuses every navigation. The SSRF and
 * domain policy is enforced by a per-context request route, so it holds for every session
 * independently.
 * </p>
 */
public class BrowserToolsService extends ConfiguredManagedClass<BrowserToolsService.Config<?>>
		implements BrowserSession.DomainPolicy {

	/**
	 * Configuration of {@link BrowserToolsService}.
	 */
	public interface Config<I extends BrowserToolsService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name for the per-navigation timeout (seconds).
		 *
		 * @see #getNavigationTimeoutSeconds()
		 */
		String NAVIGATION_TIMEOUT_SECONDS = "navigation-timeout-seconds";

		/**
		 * Configuration property name for the post-navigation settle hard cap (milliseconds).
		 *
		 * @see #getNavigationSettleMillis()
		 */
		String NAVIGATION_SETTLE_MILLIS = "navigation-settle-millis";

		/**
		 * Configuration property name for the page byte cap.
		 *
		 * @see #getMaxPageBytes()
		 */
		String MAX_PAGE_BYTES = "max-page-bytes";

		/**
		 * Configuration property name for the page character cap.
		 *
		 * @see #getMaxPageChars()
		 */
		String MAX_PAGE_CHARS = "max-page-chars";

		/**
		 * Configuration property name for the domain allowlist.
		 *
		 * @see #getAllowedDomains()
		 */
		String ALLOWED_DOMAINS = "allowed-domains";

		/**
		 * Configuration property name for the domain blocklist.
		 *
		 * @see #getBlockedDomains()
		 */
		String BLOCKED_DOMAINS = "blocked-domains";

		/**
		 * Configuration property name for the allow-any-domains mode.
		 *
		 * @see #isAllowAnyDomain()
		 */
		String ALLOW_ANY_DOMAINS = "allow-any-domains";

		/**
		 * Configuration property name for the bot-detection evasion flag.
		 *
		 * @see #isEvadeBotDetection()
		 */
		String EVADE_BOT_DETECTION = "evade-bot-detection";

		/**
		 * Configuration property name for the maximum number of concurrent sessions.
		 *
		 * @see #getMaxConcurrentSessions()
		 */
		String MAX_CONCURRENT_SESSIONS = "max-concurrent-sessions";

		/**
		 * Configuration property name for the session idle timeout (seconds).
		 *
		 * @see #getSessionIdleTimeoutSeconds()
		 */
		String SESSION_IDLE_TIMEOUT_SECONDS = "session-idle-timeout-seconds";

		/**
		 * Configuration property name for the session acquire timeout (seconds).
		 *
		 * @see #getAcquireTimeoutSeconds()
		 */
		String ACQUIRE_TIMEOUT_SECONDS = "acquire-timeout-seconds";

		/**
		 * Configuration property name for the headless launch toggle.
		 *
		 * @see #getHeadless()
		 */
		String HEADLESS = "headless";

		/**
		 * Per-operation timeout for browser actions (navigate, click, type, snapshot), in seconds.
		 *
		 * @see #NAVIGATION_TIMEOUT_SECONDS
		 */
		@Name(NAVIGATION_TIMEOUT_SECONDS)
		@IntDefault(30)
		int getNavigationTimeoutSeconds();

		/**
		 * Hard cap, in milliseconds, of the post-navigation <b>settle</b>: after a page-loading
		 * operation ({@code browserNavigate} / {@code browserBack}) finishes its network/load wait and
		 * before the page is captured, the browser waits until the DOM goes quiet (no mutation for a
		 * short internal debounce window, ~500 ms) or this cap elapses, whichever comes first.
		 *
		 * <p>
		 * This closes the common gap where a page injects a cookie-consent banner, lazy widget or
		 * other content a beat <em>after</em> navigation returns: without the settle the snapshot is
		 * taken too early and misses it, forcing a manual re-snapshot. A still page goes quiet almost
		 * immediately and is delayed by at most the debounce window, so fast pages are not meaningfully
		 * slowed; a page that never stops mutating (constant animation/polling) is bounded by this cap.
		 * The cap is always applied well inside {@link #getNavigationTimeoutSeconds() the per-operation
		 * deadline}, so it can never cause a hang.
		 * </p>
		 *
		 * <p>
		 * Set to {@code 0} to disable the settle entirely and restore immediate capture. It does
		 * <em>not</em> apply to {@code browserSnapshot} (explicit re-capture) or {@code browserType}
		 * (same-page edit), which stay immediate.
		 * </p>
		 *
		 * @see #NAVIGATION_SETTLE_MILLIS
		 */
		@Name(NAVIGATION_SETTLE_MILLIS)
		@IntDefault(1500)
		int getNavigationSettleMillis();

		/**
		 * Maximum number of HTML bytes read from a page before extraction stops.
		 *
		 * @see #MAX_PAGE_BYTES
		 */
		@Name(MAX_PAGE_BYTES)
		@IntDefault(5_000_000)
		int getMaxPageBytes();

		/**
		 * Maximum number of characters of extracted text a browser tool returns.
		 *
		 * @see #MAX_PAGE_CHARS
		 */
		@Name(MAX_PAGE_CHARS)
		@IntDefault(50_000)
		int getMaxPageChars();

		/**
		 * Administrator-owned allowlist of domains the browser may navigate to.
		 *
		 * <p>
		 * A navigation is permitted only if the target host equals one of these entries or is a
		 * sub-domain of one. If empty, the browser refuses every navigation (fail-closed). This
		 * is policy configuration - it is not the model-supplied {@code allowedDomains} argument
		 * of {@link WebSearchTool}.
		 * </p>
		 *
		 * @see #ALLOWED_DOMAINS
		 */
		@Name(ALLOWED_DOMAINS)
		@Format(CommaSeparatedStrings.class)
		List<String> getAllowedDomains();

		/**
		 * Administrator-owned blocklist of domains the browser must never navigate to.
		 *
		 * <p>
		 * A top-level origin is refused if its host equals one of these entries or is a sub-domain
		 * of one (same matching as {@link #getAllowedDomains()}). The blocklist takes precedence
		 * over the allowlist and applies in every mode, including {@link #isAllowAnyDomain()
		 * allow-any}. It carves exceptions out of a broad allowlist (or out of allow-any). SSRF
		 * protection is enforced independently and cannot be relaxed.
		 * </p>
		 *
		 * @see #BLOCKED_DOMAINS
		 */
		@Name(BLOCKED_DOMAINS)
		@Format(CommaSeparatedStrings.class)
		List<String> getBlockedDomains();

		/**
		 * Whether the public-domain allowlist is disabled (allow-any-domains mode).
		 *
		 * <p>
		 * When {@code false} (the safe default), a navigation is permitted only if the top-level
		 * origin is on the {@link #getAllowedDomains() allowlist}. When {@code true}, the allowlist
		 * is ignored and any public domain is reachable - <em>except</em> that SSRF protection and
		 * the {@link #getBlockedDomains() blocklist} still apply unconditionally. Allow-any relaxes
		 * only the public-domain allowlist, never SSRF or the blocklist.
		 * </p>
		 *
		 * @see #ALLOW_ANY_DOMAINS
		 */
		@Name(ALLOW_ANY_DOMAINS)
		@BooleanDefault(false)
		boolean isAllowAnyDomain();

		/**
		 * Whether the browser should disguise its automation fingerprint so bot-protection walls are
		 * less likely to serve it an access-denied / challenge page (off by default).
		 *
		 * <p>
		 * When {@code false} (the default) the browser behaves exactly as an out-of-the-box headless
		 * Chromium: the light {@code chrome-headless-shell} build, no user-agent override, no extra
		 * launch argument, no fingerprint init script. When {@code true}, each session's context is
		 * made to look like an ordinary desktop Chrome: the <b>full</b> Chromium build (Playwright
		 * channel "chromium", new headless - still windowless) is launched instead of the detectable
		 * headless shell, the {@code --disable-blink-features=AutomationControlled} launch switch is
		 * added, a realistic desktop Chrome user-agent (and matching {@code Accept-Language}) is set,
		 * and a small init script masks the usual automation tells ({@code navigator.webdriver}, empty
		 * {@code navigator.plugins}, {@code navigator.languages}, missing {@code window.chrome}). The
		 * build swap matters: the headless shell is itself a fingerprint some bot walls suppress
		 * content for even when the UA and {@code navigator.webdriver} are masked.
		 * </p>
		 *
		 * <p>
		 * <b>Deployment prerequisite.</b> With this enabled the full Chromium build must be installed
		 * in the Playwright browser cache ({@code playwright install chromium} installs it alongside
		 * the shell). A slimmed deploy image that ships <em>only</em> {@code chrome-headless-shell}
		 * will fail to launch when this flag is on.
		 * </p>
		 *
		 * <p>
		 * <b>Caveat - authorised use only.</b> Enabling this deliberately circumvents a site's
		 * automated-access protection and may violate that site's Terms of Service. It is an operator
		 * opt-in for authorised targets (for example a logged-in site the operator owns or has
		 * permission to automate), off by default, and must not be enabled to access sites where doing
		 * so is not permitted. It does not touch SSRF, the blocklist or the allowlist, which stay
		 * enforced unchanged.
		 * </p>
		 *
		 * @see #EVADE_BOT_DETECTION
		 */
		@Name(EVADE_BOT_DETECTION)
		@BooleanDefault(false)
		boolean isEvadeBotDetection();

		/**
		 * Maximum number of live browser sessions (isolated contexts) allowed at once.
		 *
		 * <p>
		 * This bounds memory: each live session is its own Chromium process (roughly 200-400 MB
		 * incl. its Playwright driver). Requests beyond the cap reclaim an idle session by LRU or,
		 * if none is idle, fail fast with a retryable "browser busy" error. The default of 4 keeps
		 * peak browser memory around 1 GB; raise it in line with available RAM.
		 * </p>
		 *
		 * @see #MAX_CONCURRENT_SESSIONS
		 */
		@Name(MAX_CONCURRENT_SESSIONS)
		@IntDefault(4)
		int getMaxConcurrentSessions();

		/**
		 * How long a session may stay idle (no tool call) before it is closed and its resources
		 * released, in seconds.
		 *
		 * @see #SESSION_IDLE_TIMEOUT_SECONDS
		 */
		@Name(SESSION_IDLE_TIMEOUT_SECONDS)
		@IntDefault(300)
		int getSessionIdleTimeoutSeconds();

		/**
		 * How long a tool call waits for a free session slot before failing fast with a "browser
		 * busy, retry" error, in seconds.
		 *
		 * <p>
		 * Kept short on purpose: a bounded wait cannot exhaust the servlet thread pool the way the
		 * former unbounded lock could.
		 * </p>
		 *
		 * @see #ACQUIRE_TIMEOUT_SECONDS
		 */
		@Name(ACQUIRE_TIMEOUT_SECONDS)
		@IntDefault(10)
		int getAcquireTimeoutSeconds();

		/**
		 * Whether browser sessions are launched headless (no visible window).
		 *
		 * <p>
		 * {@code true} (the default) is the production-safe path: every session runs a windowless
		 * Chromium, so nothing appears on the app host and no local display is required. Set to
		 * {@code false} to launch a <b>headed</b> (visible) Chromium window on the app host's desktop
		 * - a dev/local convenience that requires a local display and must not be used in production
		 * or on remote/headless hosts. When headed is requested on a host with no display, the
		 * service fails fast at startup (see {@link #isLocalDisplayAvailable()}) rather than hanging
		 * on a launch that can never succeed.
		 * </p>
		 *
		 * @see #HEADLESS
		 */
		@Name(HEADLESS)
		@BooleanDefault(true)
		boolean getHeadless();
	}

	private static volatile BrowserToolsService _instance;

	private volatile BrowserSessionPool<BrowserSession> _pool;

	/**
	 * Creates a {@link BrowserToolsService} from configuration.
	 */
	public BrowserToolsService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * The running instance, or {@code null} if the service is not started.
	 */
	public static BrowserToolsService getInstance() {
		return _instance;
	}

	@Override
	protected void startUp() {
		super.startUp();
		int navigationTimeoutMillis = getNavigationTimeoutSeconds() * 1000;
		int navigationSettleMillis = getNavigationSettleMillis();
		// A plain launch toggle: headless (the default) is production-safe and windowless; headed
		// launches a visible window and needs a local display. Production stays headless.
		boolean headed = !getHeadless();
		if (headed && !isLocalDisplayAvailable()) {
			// Fail fast rather than let every session hang on a headed launch that can never open a
			// window on a display-less host.
			throw new RuntimeException("The browser tool is configured headed (headless=false) but the app "
				+ "host has no local display for a browser window. Set headless=true for this environment.");
		}
		boolean evadeBotDetection = isEvadeBotDetection();
		_pool = new BrowserSessionPool<>(
			getMaxConcurrentSessions(),
			getSessionIdleTimeoutSeconds() * 1000L,
			getAcquireTimeoutSeconds() * 1000L,
			System::nanoTime,
			key -> BrowserSession.open(key, navigationTimeoutMillis, navigationSettleMillis, this,
				headed, evadeBotDetection),
			BrowserSession::close);
		_instance = this;
	}

	@Override
	protected void shutDown() {
		_instance = null;
		BrowserSessionPool<BrowserSession> pool = _pool;
		_pool = null;
		if (pool != null) {
			pool.close();
		}
		super.shutDown();
	}

	/**
	 * Whether browser sessions are launched headless (no visible window); {@code true} by default.
	 *
	 * @see Config#getHeadless()
	 */
	public boolean getHeadless() {
		return getConfig().getHeadless();
	}

	/**
	 * Whether the app host has a local display a headed browser window could open on.
	 *
	 * <p>
	 * Consulted at startup when {@link #getHeadless() headless} is off, to fail fast (instead of
	 * hanging on a launch that cannot succeed) when there is no display. Package-visible and
	 * overridable so the display guard can be exercised in tests without a real display.
	 * </p>
	 */
	boolean isLocalDisplayAvailable() {
		return HeadedDisplay.isAvailable();
	}

	/**
	 * Per-operation timeout in seconds (clamped to a safe minimum).
	 */
	public int getNavigationTimeoutSeconds() {
		return Math.max(1, getConfig().getNavigationTimeoutSeconds());
	}

	/**
	 * Post-navigation settle hard cap in milliseconds, or {@code 0} when the settle is disabled.
	 *
	 * <p>
	 * A negative configured value is treated as {@code 0} (disabled). A positive value is capped at
	 * the per-operation navigation timeout so the settle can never outlast - let alone hang past -
	 * the deadline that bounds the whole operation.
	 * </p>
	 *
	 * @see Config#getNavigationSettleMillis()
	 */
	public int getNavigationSettleMillis() {
		int settle = getConfig().getNavigationSettleMillis();
		if (settle <= 0) {
			return 0;
		}
		return Math.min(settle, getNavigationTimeoutSeconds() * 1000);
	}

	/**
	 * Maximum number of HTML characters extracted per page (clamped to a safe minimum).
	 */
	public int getMaxPageChars() {
		return Math.max(256, getConfig().getMaxPageChars());
	}

	/**
	 * Maximum number of HTML bytes read per page (clamped to a safe minimum).
	 */
	public int getMaxPageBytes() {
		return Math.max(1024, getConfig().getMaxPageBytes());
	}

	/**
	 * Maximum number of concurrent live sessions (clamped to a safe minimum).
	 */
	public int getMaxConcurrentSessions() {
		return Math.max(1, getConfig().getMaxConcurrentSessions());
	}

	/**
	 * Session idle timeout in seconds (clamped to a safe minimum).
	 */
	public int getSessionIdleTimeoutSeconds() {
		return Math.max(5, getConfig().getSessionIdleTimeoutSeconds());
	}

	/**
	 * Session acquire timeout in seconds (clamped to a safe minimum).
	 */
	public int getAcquireTimeoutSeconds() {
		return Math.max(1, getConfig().getAcquireTimeoutSeconds());
	}

	/**
	 * Whether the given host is on the administrator-owned allowlist (equal host or sub-domain).
	 *
	 * <p>
	 * This is the raw allowlist membership test; it does not consider the blocklist or
	 * {@link Config#isAllowAnyDomain() allow-any} mode. Use {@link #isNavigationPermitted(String)}
	 * for the full domain policy decision.
	 * </p>
	 *
	 * @param host
	 *        The host to check (may be {@code null}).
	 * @return {@code true} if the host is on the allowlist.
	 */
	public boolean isAllowedDomain(String host) {
		return matchesDomainList(host, getConfig().getAllowedDomains());
	}

	/**
	 * Whether the given host is on the administrator-owned blocklist (equal host or sub-domain).
	 *
	 * @param host
	 *        The host to check (may be {@code null}).
	 * @return {@code true} if the host is on the blocklist.
	 */
	public boolean isBlockedDomain(String host) {
		return matchesDomainList(host, getConfig().getBlockedDomains());
	}

	/**
	 * Whether allow-any-domains mode is enabled (the public-domain allowlist is disabled).
	 *
	 * @see Config#isAllowAnyDomain()
	 */
	public boolean isAllowAnyDomain() {
		return getConfig().isAllowAnyDomain();
	}

	/**
	 * Whether bot-detection evasion is enabled for new sessions.
	 *
	 * @see Config#isEvadeBotDetection()
	 */
	public boolean isEvadeBotDetection() {
		return getConfig().isEvadeBotDetection();
	}

	/**
	 * Whether a top-level navigation to {@code host} is permitted by the domain policy.
	 *
	 * <p>
	 * Applies the blocklist and allowlist/allow-any precedence: a blocklisted host is always
	 * refused; otherwise the host must be on the allowlist unless {@link #isAllowAnyDomain()
	 * allow-any} is enabled. This does <em>not</em> perform the SSRF check - callers must run
	 * {@link UrlSafety#validateUrl(URI)} independently, and it is always enforced regardless of
	 * this decision.
	 * </p>
	 *
	 * @param host
	 *        The top-level document host to check (may be {@code null}).
	 * @return {@code true} if the domain policy permits navigating to {@code host}.
	 */
	@Override
	public boolean isNavigationPermitted(String host) {
		if (isBlockedDomain(host)) {
			return false;
		}
		if (isAllowAnyDomain()) {
			return true;
		}
		return isAllowedDomain(host);
	}

	/**
	 * Whether {@code host} matches one of the {@code domains} entries (equal host or sub-domain,
	 * case-insensitive). An empty or {@code null} list matches nothing, and a {@code null} host
	 * matches nothing. Package-visible for testing.
	 */
	static boolean matchesDomainList(String host, List<String> domains) {
		if (host == null || host.isEmpty()) {
			return false;
		}
		String normalized = host.toLowerCase(Locale.ROOT);
		for (String entry : domains) {
			String domain = entry.trim().toLowerCase(Locale.ROOT);
			if (domain.isEmpty()) {
				continue;
			}
			if (normalized.equals(domain) || normalized.endsWith("." + domain)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A single interactive element on the current page, addressable by its {@code ref}.
	 *
	 * @param ref
	 *        The generation-tagged reference (e.g. {@code s1e3}, meaning snapshot 1, element 3) the
	 *        model passes to click/type. Valid only for the snapshot that produced it; the next
	 *        snapshot or navigation mints fresh refs and a carried-over ref is rejected.
	 * @param role
	 *        The ARIA role or tag name (e.g. {@code button}, {@code textbox}, {@code a}).
	 * @param tag
	 *        The HTML tag name.
	 * @param type
	 *        The {@code type} attribute for inputs (e.g. {@code text}, {@code password}), else "".
	 * @param name
	 *        The accessible name/label/placeholder/text of the element (truncated).
	 */
	public record InteractiveElement(String ref, String role, String tag, String type, String name) {
		// Value type only.
	}

	/**
	 * Whether the current page presents a human-verification challenge the tool cannot solve.
	 *
	 * @param detected
	 *        {@code true} if a CAPTCHA / verification challenge was detected.
	 * @param kind
	 *        A short signal identifying what was matched (selector or {@code text-challenge}), or
	 *        {@code null} when none was detected.
	 */
	public record CaptchaSignal(boolean detected, String kind) {
		// Value type only.
	}

	/**
	 * Whether the current page is a bot-protection / access-denied "block page" rather than the
	 * genuine target content.
	 *
	 * <p>
	 * Reported (always on, never evasive) so the model can tell a page it was <em>blocked</em> from
	 * an empty page it genuinely reached - see {@link BotWallDetector}. Detection only: nothing
	 * retries or works around the block.
	 * </p>
	 *
	 * @param detected
	 *        {@code true} if the page looks like a bot-protection block.
	 * @param status
	 *        The HTTP status that accompanied the block, or {@code -1} if unknown (e.g. a re-snapshot).
	 * @param reason
	 *        A short human-readable reason for the classification, or {@code null} when none detected.
	 */
	public record BlockSignal(boolean detected, int status, String reason) {
		// Value type only.
	}

	/**
	 * A capture of the current page after a navigation or interaction: the rendered HTML, the
	 * accessibility snapshot, the addressable interactive elements, and any CAPTCHA / block signal.
	 *
	 * @param finalUrl
	 *        The URL after any redirects the browser followed.
	 * @param status
	 *        The HTTP status of the last main-document response, or {@code -1} if unknown.
	 * @param html
	 *        The fully rendered {@code document} HTML (post-JavaScript).
	 * @param ariaSnapshot
	 *        The ARIA/accessibility-tree snapshot of the page body, as YAML-ish text.
	 * @param elements
	 *        The interactive elements the model may act on, each with a stable {@code ref}.
	 * @param captcha
	 *        Whether a human-verification challenge is present.
	 * @param block
	 *        Whether the page is a bot-protection / access-denied block page.
	 * @param navigated
	 *        Whether this capture moved to a <em>different</em> page than the one the session was on
	 *        before the operation (an explicit navigate/back, or a click that changed the URL). Drives
	 *        the terse "Navigation: occurred / none" line; always {@code false} for a same-page
	 *        re-capture (snapshot / type).
	 */
	public record PageView(String finalUrl, int status, String html, String ariaSnapshot,
			List<InteractiveElement> elements, CaptchaSignal captcha, BlockSignal block, boolean navigated) {
		// Value type only.
	}

	/**
	 * A captured screenshot: the URL it was taken from and the PNG bytes.
	 *
	 * @param url
	 *        The URL of the page at capture time.
	 * @param png
	 *        The PNG image bytes.
	 */
	public record ScreenshotCapture(String url, byte[] png) {
		// Value type only.
	}

	/**
	 * Navigates the session's page to the given (already SSRF- and policy-validated) URL and
	 * captures the resulting page.
	 *
	 * @param sessionKey
	 *        The stable session key identifying which isolated context to use.
	 * @param url
	 *        The absolute http(s) URL to navigate to.
	 * @return The captured page.
	 */
	public PageView navigate(Object sessionKey, String url) {
		return run(sessionKey, session -> session.navigate(url));
	}

	/**
	 * Captures the session's current page without navigating.
	 *
	 * @param sessionKey
	 *        The stable session key.
	 * @return The captured page.
	 */
	public PageView snapshot(Object sessionKey) {
		return run(sessionKey, BrowserSession::snapshot);
	}

	/**
	 * Captures a PNG screenshot of the session's current page.
	 *
	 * @param sessionKey
	 *        The stable session key.
	 * @param fullPage
	 *        {@code false} for the viewport only, {@code true} for the full scrolling page.
	 * @return The captured screenshot (source URL and PNG bytes).
	 */
	public ScreenshotCapture screenshot(Object sessionKey, boolean fullPage) {
		return runOnSession(sessionKey, session -> session.screenshot(fullPage));
	}

	/**
	 * Clicks the element with the given ref on the session's current page, then captures the page.
	 *
	 * @param sessionKey
	 *        The stable session key.
	 * @param ref
	 *        A ref from the most recent snapshot of this session.
	 * @return The captured page after the click.
	 */
	public PageView click(Object sessionKey, String ref) {
		return run(sessionKey, session -> session.click(ref));
	}

	/**
	 * Types text into the element with the given ref on the session's current page, then captures.
	 *
	 * @param sessionKey
	 *        The stable session key.
	 * @param ref
	 *        A ref from the most recent snapshot of this session.
	 * @param text
	 *        The text to enter (replaces any current value).
	 * @return The captured page after typing.
	 */
	public PageView type(Object sessionKey, String ref, String text) {
		return run(sessionKey, session -> session.type(ref, text));
	}

	/**
	 * Navigates back in the session's history, then captures the page.
	 *
	 * @param sessionKey
	 *        The stable session key.
	 * @return The captured page after going back.
	 */
	public PageView back(Object sessionKey) {
		return run(sessionKey, BrowserSession::back);
	}

	/**
	 * Discards the live session for a key (forcing a recreate on next use), for testing eviction
	 * behaviour deterministically.
	 */
	void invalidateSessionForTest(Object sessionKey) {
		BrowserSessionPool<BrowserSession> pool = _pool;
		if (pool != null) {
			pool.invalidate(sessionKey);
		}
	}

	/**
	 * The number of currently live browser sessions. Package-visible for testing.
	 */
	int liveSessionCountForTest() {
		BrowserSessionPool<BrowserSession> pool = _pool;
		return pool == null ? 0 : pool.liveSessionCount();
	}

	private PageView run(Object sessionKey, Function<BrowserSession, PageView> op) {
		return runOnSession(sessionKey, op);
	}

	private <R> R runOnSession(Object sessionKey, Function<BrowserSession, R> op) {
		BrowserSessionPool<BrowserSession> pool = _pool;
		if (pool == null) {
			throw new IllegalStateException("The browser tool service is not started.");
		}
		try {
			return pool.run(sessionKey, op);
		} catch (BrowserSessionBrokenException ex) {
			// The page hung; tear the session down so its slot is freed and it is not reused.
			pool.invalidate(sessionKey);
			throw ex;
		}
	}

	/**
	 * {@link TypedRuntimeModule} for {@link BrowserToolsService}.
	 */
	public static final class Module extends TypedRuntimeModule<BrowserToolsService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<BrowserToolsService> getImplementation() {
			return BrowserToolsService.class;
		}
	}
}
