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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.EnumDefaultValue;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
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
 * {@link BrowserSession} - a dedicated <code>Playwright/Browser/BrowserContext</code> with separate
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
 * is safe-by-default: an empty allowlist with allow-any off refuses every navigation.
 * </p>
 *
 * <p>
 * <b>Above all of this sits the browsing governance of {@link BrowsingPolicy}</b>, configured on
 * {@code WebToolsService} and resolved per security role: which origins <em>this user's</em> agent
 * may reach, and whether a page there may hand it a file. It composes with the lists above - both
 * have to permit an origin - and an application that configures none is bound by nothing extra.
 * </p>
 *
 * <p>
 * <b>Where the policy is enforced.</b> The authoritative layer is a {@link FilteringProxy} on the
 * loopback interface that <em>every</em> browser session is pinned to: every connection Chromium
 * makes - top-level navigation, redirect hop, sub-resource, IP-literal target - is checked there
 * before a packet leaves the machine, and the destination is DNS-pinned so a rebinding host cannot
 * slip through. The allowlist, blocklist and allow-any flag are <em>service</em> configuration,
 * identical for every session, so nothing about that half of the decision varies per session; the
 * browsing policy does vary, per role, which is why there is one proxy per <em>policy in use</em>
 * rather than one for the whole service. Inside each browser the same policy is applied again by a
 * per-context request route, plus a redirect guard and a final-URL re-validation, which is what turns
 * a refusal into the tool's clean policy message.
 * </p>
 */
public class BrowserToolsService extends ConfiguredManagedClass<BrowserToolsService.Config<?>>
		implements BrowserSession.DomainPolicy, FilteringProxy.RequestPolicy {

	/**
	 * Whether Chromium's OS-level sandbox is used for the browser sessions.
	 *
	 * @see Config#getBrowserSandbox()
	 */
	public enum SandboxMode {

		/**
		 * The default: the sandbox is used everywhere except inside a container, where the default
		 * runtime profile prevents Chromium from setting one up.
		 */
		AUTO,

		/**
		 * The sandbox is always used, containers included. Chromium refuses to start where it cannot
		 * set one up.
		 */
		ENABLED,

		/**
		 * The sandbox is never used, whatever the environment looks like. This weakens the isolation
		 * of every browsed page.
		 */
		DISABLED;
	}

	/**
	 * {@link EnumDefaultValue} providing {@link SandboxMode#AUTO} as the default.
	 */
	public static final class AutoSandboxDefault extends EnumDefaultValue {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return SandboxMode.AUTO;
		}
	}

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
		 * Configuration property name for the bounded element-action timeout (seconds).
		 *
		 * @see #getActionTimeoutSeconds()
		 */
		String ACTION_TIMEOUT_SECONDS = "action-timeout-seconds";

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
		 * Configuration property name for the allowlist-governs-every-request flag.
		 *
		 * @see #isApplyAllowlistToAllRequests()
		 */
		String APPLY_ALLOWLIST_TO_ALL_REQUESTS = "apply-allowlist-to-all-requests";

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
		 * Configuration property name for the cookie-pools master toggle.
		 *
		 * @see #isCookiePoolsEnabled()
		 */
		String COOKIE_POOLS_ENABLED = "cookie-pools-enabled";

		/**
		 * Configuration property name for the Chromium sandbox mode.
		 *
		 * @see #getBrowserSandbox()
		 */
		String BROWSER_SANDBOX = "browser-sandbox";

		/**
		 * Timeout for a <b>page load</b> ({@code browserNavigate}, {@code browserBack}, and a
		 * {@code browserClick} that started a navigation), in seconds.
		 *
		 * <p>
		 * This bounds waiting for the document: how long the browser may spend reaching
		 * {@code domcontentloaded} and then the {@code load} event on a slow site. It does
		 * <em>not</em> bound the settle that follows a load - that is capped much more tightly by
		 * {@link #getNavigationSettleMillis()}, because a page that never falls quiet must not cost a
		 * page load per operation. Acting on an <em>element</em> is bounded separately and much more
		 * tightly again by {@link #getActionTimeoutSeconds()}.
		 * </p>
		 *
		 * @see #NAVIGATION_TIMEOUT_SECONDS
		 */
		@Name(NAVIGATION_TIMEOUT_SECONDS)
		@IntDefault(30)
		int getNavigationTimeoutSeconds();

		/**
		 * Timeout for acting on a single <b>element</b> - {@code browserClick} / {@code browserType},
		 * and reading the accessibility snapshot - in seconds.
		 *
		 * <p>
		 * Deliberately much shorter than {@link #getNavigationTimeoutSeconds() the page-load timeout},
		 * because the two wait for entirely different things. A click waits for the element to become
		 * <em>actionable</em> (attached, visible, stable, unobstructed, enabled); if the page has
		 * re-rendered that element away - the classic search box whose suggestion list replaces the
		 * control after the query is typed - no amount of further waiting can make the wait succeed, and
		 * a page-load-sized budget just turns a dead click into a half-minute stall followed by an
		 * opaque timeout. Bounding it to a few seconds lets the tool answer promptly with an actionable
		 * "re-snapshot and use a fresh ref" instruction (see {@link BrowserRefNotActionableException})
		 * while still absorbing the ordinary case of an element that needs a moment to settle.
		 * </p>
		 *
		 * <p>
		 * Raise it for a deliberately slow or heavily animated application; it is clamped to at most the
		 * page-load timeout, since waiting longer for one element than for a whole page load is never
		 * useful.
		 * </p>
		 *
		 * @see #ACTION_TIMEOUT_SECONDS
		 */
		@Name(ACTION_TIMEOUT_SECONDS)
		@IntDefault(8)
		int getActionTimeoutSeconds();

		/**
		 * Hard cap, in milliseconds, of the post-load <b>settle</b>: after a page-loading operation
		 * ({@code browserNavigate}, {@code browserBack}, or a {@code browserClick} that navigated) has
		 * seen the page load, and before the page is captured, the browser waits for the page to calm
		 * down - and each of the three things it waits for is bounded by this one cap, separately.
		 *
		 * <p>
		 * <b>Commit.</b> Only for a {@code browserClick}, and only where the page navigated itself
		 * from a script rather than through the link that was clicked: the browser waits for the new
		 * document to actually replace the old one before it settles anything, because until then the
		 * page's address and its load state are still the old document's. A navigation that is begun
		 * and never lands - a {@code 204}, a download, a handler that changes its mind - gives up
		 * after this cap.
		 * </p>
		 *
		 * <p>
		 * <b>Network-quiet.</b> A brief chance for client-side rendering that only starts after the
		 * load event: the wait ends as soon as the page has issued no request for Playwright's idle
		 * window, or when this cap elapses. It is deliberately <em>not</em> bounded by
		 * {@link #getNavigationTimeoutSeconds() the page-load timeout}: a page that polls, streams or
		 * sends beacons - most SSO login pages, most single-page applications - never goes network-idle
		 * at all, so a page-load-sized budget here bought nothing and cost that whole budget on every
		 * operation.
		 * </p>
		 *
		 * <p>
		 * <b>DOM-quiet.</b> The browser then waits until the DOM goes quiet (no mutation for a short
		 * internal debounce window, ~500 ms) and any in-flight, finite CSS animation has finished, or
		 * until this cap elapses. This closes the common gap where a page injects a cookie-consent
		 * banner, lazy widget or other content a beat <em>after</em> navigation returns: without the
		 * settle the snapshot is taken too early and misses it, forcing a manual re-snapshot. A still
		 * page goes quiet almost immediately and is delayed by at most the debounce window, so fast
		 * pages are not meaningfully slowed; a page that never stops mutating (constant
		 * animation/polling) is bounded by this cap.
		 * </p>
		 *
		 * <p>
		 * The three stages do not add up on any ordinary path: a click that does not navigate runs the
		 * DOM-quiet stage alone, and an ordinary link or form-submit click has already committed by the
		 * time the commit stage is reached. The cap is always applied well inside the per-operation
		 * deadline, so it can never cause a hang. Set to {@code 0} to disable the settle entirely and
		 * capture as soon as the page has loaded; the page that is then captured is still internally
		 * consistent - address, content and element references are read in one go - it is simply
		 * captured earlier. The settle does <em>not</em> apply to {@code browserSnapshot} (explicit re-capture),
		 * to {@code browserType} (same-page edit), or to a {@code browserClick} that started no
		 * navigation - that one gets the DOM-quiet wait alone, because there is no page load to wait
		 * for.
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
		 * Whether the {@link #getAllowedDomains() allowlist} governs <b>every</b> connection the
		 * browser makes, rather than only the top-level document origin.
		 *
		 * <p>
		 * The allowlist has always been a <em>top-level-origin</em> control: an allowlisted page may
		 * pull in sub-resources (fonts, scripts, images from a CDN) from anywhere that is neither
		 * SSRF-blocked nor blocklisted, exactly as an ordinary browser would. {@code false} (the
		 * default) preserves that: the filtering proxy enforces SSRF and the blocklist for every
		 * connection, and the allowlist keeps being enforced for the top-level origin only.
		 * </p>
		 *
		 * <p>
		 * Set to {@code true} for a locked-down installation whose intent is "this browser may talk to
		 * these domains and nothing else": the proxy then refuses <em>any</em> connection to a host
		 * that is not on the allowlist, sub-resources included. That is strictly safer - it also stops
		 * an allowlisted page from beaconing to a third party - but a page whose assets live on other
		 * domains will render without them, so it is an explicit operator decision rather than the
		 * default. It has no effect when {@link #isAllowAnyDomain() allow-any} is enabled (there is no
		 * allowlist to apply), and it can never relax anything: SSRF and the blocklist apply to every
		 * connection in both settings.
		 * </p>
		 *
		 * @see #APPLY_ALLOWLIST_TO_ALL_REQUESTS
		 */
		@Name(APPLY_ALLOWLIST_TO_ALL_REQUESTS)
		@BooleanDefault(false)
		boolean isApplyAllowlistToAllRequests();

		/**
		 * Whether the browser should disguise its automation fingerprint so bot-protection walls are
		 * less likely to serve it an access-denied / challenge page (off by default).
		 *
		 * <p>
		 * When {@code false} (the default) the browser behaves as an out-of-the-box headless Chromium:
		 * no user-agent override, no extra launch argument, no fingerprint init script. When
		 * {@code true}, each session's context is made to look like an ordinary desktop Chrome: the
		 * {@code --disable-blink-features=AutomationControlled} launch switch is added, a realistic
		 * desktop Chrome user-agent (and matching {@code Accept-Language}) is set, and a small init
		 * script masks the usual automation tells ({@code navigator.webdriver}, empty
		 * {@code navigator.plugins}, {@code navigator.languages}, missing {@code window.chrome}).
		 * </p>
		 *
		 * <p>
		 * The <b>full</b> Chromium build (Playwright channel "chromium", new headless - still
		 * windowless) that evasion also depends on - the light {@code chrome-headless-shell} is itself
		 * a fingerprint some bot walls suppress content for, even when the UA and
		 * {@code navigator.webdriver} are masked - is launched for every session regardless of this
		 * setting, because it is what allows the UDP egress of {@code WebRTC} to be turned off. See
		 * {@code BrowserSession.buildLaunchOptions}, including the deployment prerequisite that
		 * carries.
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
		 * service fails fast at startup rather than hanging on a launch that can never succeed.
		 * </p>
		 *
		 * @see #HEADLESS
		 * @see #isLocalDisplayAvailable()
		 */
		@Name(HEADLESS)
		@BooleanDefault(true)
		boolean getHeadless();

		/**
		 * Master switch for cookie pools: whether the {@code browserSaveCookies} /
		 * {@code browserUseCookies} verbs are operative.
		 *
		 * <p>
		 * {@code true} (the default) enables persistent, named, encrypted login pools: a session can
		 * save its current login state into a named pool and a later session can start seeded from one
		 * (see {@code CookiePoolService}). When {@code false}, both verbs refuse with a clear message
		 * and no cookie state is captured or seeded - use this to switch the whole feature off for an
		 * installation regardless of what the model tries. Turning it off does not affect ordinary
		 * navigation or the per-session isolated context.
		 * </p>
		 *
		 * @see #COOKIE_POOLS_ENABLED
		 */
		@Name(COOKIE_POOLS_ENABLED)
		@BooleanDefault(true)
		boolean isCookiePoolsEnabled();

		/**
		 * Whether the browser sessions run inside Chromium's own OS-level sandbox.
		 *
		 * <p>
		 * The sandbox is the boundary between a renderer executing <em>attacker-controlled page
		 * JavaScript</em> and the host the application runs on: without it, a renderer exploit is
		 * simply code running with the application's own privileges, inside the application's process
		 * tree. It is therefore on wherever it can be, and it is not a performance knob.
		 * </p>
		 *
		 * <p>
		 * {@link SandboxMode#AUTO} (the default) keeps it on and gives it up only inside a container,
		 * recognised by the marker files and cgroup layout a container runtime leaves behind. That is
		 * the one environment where it cannot simply stay on: the default Docker/Podman seccomp
		 * profile denies the {@code clone}/{@code unshare} calls Chromium's namespace sandbox needs,
		 * so a sandboxed launch fails outright there. The right answer for a container is a runtime
		 * profile that permits those calls (tracked as the deploy-image ticket) - until then, the
		 * sandbox is dropped in containers and only in containers.
		 * </p>
		 *
		 * <p>
		 * {@link SandboxMode#ENABLED} and {@link SandboxMode#DISABLED} are the escape hatches:
		 * {@code ENABLED} keeps the sandbox even in a container (correct once the image carries a
		 * seccomp profile that allows it; the browser will fail to start if it does not), and
		 * {@code DISABLED} gives it up unconditionally, which weakens the isolation of every browsed
		 * page and should only ever be a temporary diagnostic.
		 * </p>
		 *
		 * @see #BROWSER_SANDBOX
		 */
		@Name(BROWSER_SANDBOX)
		@ComplexDefault(AutoSandboxDefault.class)
		SandboxMode getBrowserSandbox();
	}

	private static volatile BrowserToolsService _instance;

	private volatile BrowserSessionPool<BrowserSession> _pool;

	/**
	 * The filtering forward proxies browser sessions are pinned to, one per browsing policy.
	 *
	 * <p>
	 * Keyed by {@link BrowsingPolicy#name()} rather than one per session, because everything a proxy
	 * decides - SSRF, the administrator allowlist/blocklist/allow-any configuration, and the browsing
	 * policy of the acting role - is the same for every session running under the same policy. An
	 * application that configures no browsing policy therefore has exactly one, as it always had.
	 * </p>
	 *
	 * <p>
	 * Created lazily and closed only at shutdown. The number of entries is bounded by the number of
	 * distinct role combinations among the roles the browsing policy names, which is configuration -
	 * a session cannot invent a policy - so there is nothing here for a run to grow without limit.
	 * </p>
	 */
	private final Map<String, FilteringProxy> _proxies = new ConcurrentHashMap<>();

	/** Whether the proxies may still be created; {@code false} from {@link #shutDown()} onwards. */
	private volatile boolean _running;

	/**
	 * The <b>decrypted</b> {@code storageState} to seed into the next session created for a key, set
	 * by {@link #seedSession(Object, String)} when a cookie pool is selected.
	 *
	 * <p>
	 * This is transient plumbing, not the durable store: the persistent, encrypted truth lives in the
	 * {@code CookiePool} entities managed by {@code CookiePoolService}. The plaintext is held here
	 * only to convey a chosen pool into the pool's lazily-created {@link BrowserSession} (whose
	 * factory sees only the session key), and it is dropped as soon as a context has been seeded from
	 * it - with a TTL and a size cap as a backstop for an entry nobody consumes. See
	 * {@link CookieSeedStore} for why the lifetime is bounded so aggressively.
	 * </p>
	 *
	 * <p>
	 * <b>Behavioural consequence.</b> The seed no longer survives an idle/LRU
	 * eviction-and-recreate: a conversation whose session is evicted comes back anonymous and has to
	 * select its pool again. That is the price of not holding a logged-in site's live session cookies
	 * in memory for the whole conversation.
	 * </p>
	 */
	private final CookieSeedStore _seedByKey = new CookieSeedStore();

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
		int actionTimeoutMillis = getActionTimeoutSeconds() * 1000;
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
		// Resolved once at startup rather than per launch: the environment does not change while the
		// service runs, and an operator has to be able to read the effective decision off the log.
		boolean sandboxDisabled = isSandboxDisabled();
		_running = true;
		// The unrestricted proxy is started eagerly, before any session can exist: an unproxied
		// browser would not be bound by the policy for redirects and IP-literal targets, and the
		// application that configures no browsing policy must see exactly the setup it always had.
		String proxyServer = proxyFor(BrowsingPolicy.unrestricted()).serverUrl();
		_pool = new BrowserSessionPool<>(
			getMaxConcurrentSessions(),
			getSessionIdleTimeoutSeconds() * 1000L,
			getAcquireTimeoutSeconds() * 1000L,
			System::nanoTime,
			key -> {
				// Resolved on the calling thread, which is the one that carries the acting account:
				// the pool creates a session inside the tool call that needed it.
				BrowsingPolicy browsing = WebToolsService.currentPolicy();
				GovernedPolicy policy = new GovernedPolicy(this, this, browsing);
				BrowserSession session = BrowserSession.open(key, navigationTimeoutMillis,
					actionTimeoutMillis, navigationSettleMillis, policy, _seedByKey.lookup(key), headed,
					evadeBotDetection, proxyFor(browsing).serverUrl(), sandboxDisabled);
				// The context now holds the cookies, so the decrypted copy has served its purpose.
				// Dropped only after a SUCCESSFUL launch: a failed one must stay retryable.
				_seedByKey.discard(key);
				return session;
			},
			BrowserSession::close);
		// State the EFFECTIVE limits, not the configured ones: they are clamped, and an operator
		// investigating "the cap is not being honoured" needs to be able to rule out a configuration
		// or restart-propagation artifact from the log alone.
		Logger.info("Browser tool started: at most " + getMaxConcurrentSessions()
			+ " concurrent live browser session(s), page-load timeout " + getNavigationTimeoutSeconds()
			+ " s, action timeout " + getActionTimeoutSeconds() + " s, settle cap "
			+ getNavigationSettleMillis() + " ms (bounds each of the commit, network-quiet and "
			+ "DOM-quiet waits after a page load; a click that did not navigate waits only for "
			+ "DOM-quiet), acquire timeout " + getAcquireTimeoutSeconds()
			+ " s, idle timeout " + getSessionIdleTimeoutSeconds() + " s. Every browser connection is "
			+ "filtered through a loopback proxy; a session under no browsing policy uses " + proxyServer
			+ (isApplyAllowlistToAllRequests() ? " (allowlist governs every request)." : ".")
			+ (sandboxDisabled
				? " WARNING: Chromium runs WITHOUT its OS-level sandbox (browser-sandbox="
					+ getConfig().getBrowserSandbox() + "), so page JavaScript is not isolated from the "
					+ "application host by the operating system."
				: " Chromium runs inside its OS-level sandbox."),
			BrowserToolsService.class);
		_instance = this;
	}

	@Override
	protected void shutDown() {
		_instance = null;
		_seedByKey.clear();
		BrowserSessionPool<BrowserSession> pool = _pool;
		_pool = null;
		if (pool != null) {
			pool.close();
		}
		// Close the proxies only after every session is gone, so no live browser is left pointing at a
		// listener that has stopped answering.
		_running = false;
		List<FilteringProxy> proxies = new ArrayList<>(_proxies.values());
		_proxies.clear();
		for (FilteringProxy proxy : proxies) {
			proxy.close();
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
	 * Whether cookie pools are enabled for this installation; {@code true} by default.
	 *
	 * @see Config#isCookiePoolsEnabled()
	 */
	public boolean isCookiePoolsEnabled() {
		return getConfig().isCookiePoolsEnabled();
	}

	/**
	 * Whether the sessions must be launched <b>without</b> Chromium's OS-level sandbox, resolving
	 * {@link Config#getBrowserSandbox()} against the detected environment.
	 *
	 * <p>
	 * Package-visible and static so the decision can be asserted for both branches in a unit test
	 * without launching a browser.
	 * </p>
	 *
	 * @param mode
	 *        The configured sandbox mode; {@code null} is read as {@link SandboxMode#AUTO}.
	 * @return {@code true} when {@code --no-sandbox} has to be passed at launch.
	 */
	static boolean isSandboxDisabled(SandboxMode mode) {
		if (mode == SandboxMode.ENABLED) {
			return false;
		}
		if (mode == SandboxMode.DISABLED) {
			return true;
		}
		// AUTO: give the sandbox up only where it provably cannot be set up.
		return ContainerEnvironment.isContainer();
	}

	/**
	 * Whether this installation launches its browsers without Chromium's OS-level sandbox.
	 *
	 * @see Config#getBrowserSandbox()
	 */
	public boolean isSandboxDisabled() {
		return isSandboxDisabled(getConfig().getBrowserSandbox());
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
	 * Page-load timeout in seconds (clamped to a safe minimum).
	 */
	public int getNavigationTimeoutSeconds() {
		return Math.max(1, getConfig().getNavigationTimeoutSeconds());
	}

	/**
	 * Bounded element-action timeout in seconds (clamped to a safe minimum, and to at most the
	 * page-load timeout).
	 *
	 * @see Config#getActionTimeoutSeconds()
	 */
	public int getActionTimeoutSeconds() {
		return Math.min(Math.max(1, getConfig().getActionTimeoutSeconds()), getNavigationTimeoutSeconds());
	}

	/**
	 * Post-load settle hard cap in milliseconds - it bounds each of the three waits that can follow an
	 * operation, separately: the commit wait a navigating click runs, the network-quiet stage and the
	 * DOM-quiet stage - or {@code 0} when the settle is disabled.
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
	 * Whether the allowlist governs every connection rather than only the top-level origin.
	 *
	 * @see Config#isApplyAllowlistToAllRequests()
	 */
	public boolean isApplyAllowlistToAllRequests() {
		return getConfig().isApplyAllowlistToAllRequests();
	}

	/**
	 * The domain-policy verdict the {@link FilteringProxy} applies to <b>every</b> outbound connection
	 * of every session.
	 *
	 * <p>
	 * The blocklist applies unconditionally here - a host the administrator has ruled out must not be
	 * contacted for a sub-resource either, and refusing more than the top-level check does can only
	 * ever tighten the policy. The allowlist is consulted only when
	 * {@link #isApplyAllowlistToAllRequests()} is set, because it is defined as a top-level-origin
	 * control (see that flag) and the proxy cannot tell a main document from a sub-resource: applying
	 * it to everything unasked would silently change the documented semantics. SSRF is not decided
	 * here at all - the proxy enforces it unconditionally through {@link UrlSafety}, so no
	 * configuration reachable from this method can relax it.
	 * </p>
	 *
	 * @param host
	 *        The destination host of the connection.
	 * @return The refusal reason, or {@code null} when the domain policy permits the connection.
	 */
	@Override
	public String refuseRequest(String host) {
		if (isBlockedDomain(host)) {
			return "the domain '" + host + "' is on the browser blocklist configured by the administrator.";
		}
		if (isApplyAllowlistToAllRequests() && !isAllowAnyDomain() && !isAllowedDomain(host)) {
			return "the domain '" + host + "' is not on the browser allowlist configured by the "
				+ "administrator.";
		}
		return null;
	}

	/**
	 * The filtering proxy browser sessions running under {@code browsing} are pinned to, started on
	 * first use.
	 *
	 * @param browsing
	 *        The browsing policy of the session; {@link BrowsingPolicy#unrestricted()} when the
	 *        application configures none.
	 * @return The proxy; never {@code null}.
	 */
	FilteringProxy proxyFor(BrowsingPolicy browsing) {
		return _proxies.computeIfAbsent(browsing.name(), name -> {
			if (!_running) {
				// Only reachable from a session creation racing shutdown; starting a listener now
				// would leak it past the service.
				throw new IllegalStateException("The browser tool service is shutting down.");
			}
			// Sized from the session cap: the proxy serves the browsers this service starts and
			// nothing else, so their number is what bounds the connections - and the threads - it
			// needs.
			FilteringProxy proxy =
				FilteringProxy.start(new GovernedPolicy(this, this, browsing), getMaxConcurrentSessions());
			Logger.info("Browser filtering proxy for browsing policy '" + name + "' listening on "
				+ proxy.serverUrl() + "; it permits " + browsing.describeAllowed() + ".",
				BrowserToolsService.class);
			return proxy;
		});
	}

	/**
	 * How many filtering proxies are currently running, one per browsing policy in use. Package-visible
	 * for testing.
	 */
	int proxyCount() {
		return _proxies.size();
	}

	/**
	 * The service's own domain lists and one {@link BrowsingPolicy}, as one decision.
	 *
	 * <p>
	 * Both have to permit an origin, and neither can relax the SSRF check, which the proxy applies
	 * independently. The service's lists speak first because they are the older, installation-wide
	 * statement; which of the two answers first cannot change the outcome, since both only ever
	 * refuse.
	 * </p>
	 *
	 * @param navigations
	 *        The installation-wide answer for a top-level navigation.
	 * @param requests
	 *        The installation-wide answer for one outbound connection. The same service object as
	 *        {@code navigations} in production; two properties because the two questions are two
	 *        interfaces, which is what lets a test state one without the other.
	 * @param browsing
	 *        The policy of the role this session runs under.
	 */
	record GovernedPolicy(BrowserSession.DomainPolicy navigations, FilteringProxy.RequestPolicy requests,
			BrowsingPolicy browsing) implements BrowserSession.DomainPolicy, FilteringProxy.RequestPolicy {

		@Override
		public boolean isNavigationPermitted(String host) {
			return navigations.isNavigationPermitted(host) && browsing.refuseAccess(host) == null;
		}

		@Override
		public String refuseRequest(String host) {
			String refusal = requests.refuseRequest(host);
			return refusal != null ? refusal : browsing.refuseAccess(host);
		}

		@Override
		public String refuseDownload(String host) {
			return browsing.refuseDownload(host);
		}
	}

	/**
	 * Whether {@code host} matches one of the {@code domains} entries (equal host or sub-domain).
	 * An empty or {@code null} list matches nothing, and a {@code null} host matches nothing.
	 * Package-visible for testing.
	 *
	 * <p>
	 * Both sides go through {@link UrlSafety#normalizeHost(String)}, so the comparison is
	 * case-insensitive and the fully qualified spelling with a trailing dot
	 * ({@code blocked.example.com.}) is matched as the host it is rather than slipping past the
	 * blocklist. That normalisation is shared with the {@link FilteringProxy} and with
	 * {@link UrlSafety} itself precisely so the layers cannot disagree about what a host is.
	 * </p>
	 */
	static boolean matchesDomainList(String host, List<String> domains) {
		String normalized = UrlSafety.normalizeHost(host);
		if (normalized == null || normalized.isEmpty()) {
			return false;
		}
		for (String entry : domains) {
			String domain = UrlSafety.normalizeHost(entry);
			if (domain == null || domain.isEmpty()) {
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
	 * Captures the current login state ({@code storageState}: cookies + origin storage) of the
	 * session for {@code sessionKey} as a JSON string, creating the session if needed.
	 *
	 * <p>
	 * This is the raw, unencrypted capture; {@code CookiePoolService} encrypts it before it is
	 * persisted into a named pool. Used by the {@code browserSaveCookies} verb.
	 * </p>
	 *
	 * @param sessionKey
	 *        The stable session key.
	 * @return The {@code storageState} JSON of the session's context.
	 */
	public String captureStorageState(Object sessionKey) {
		return runOnSession(sessionKey, BrowserSession::captureStorageState);
	}

	/**
	 * Selects a cookie pool for a session: the (decrypted) {@code storageState} JSON is remembered as
	 * the seed for the session key, and any live session for that key is discarded so it is recreated
	 * with a fresh context carrying the pool's cookies/login.
	 *
	 * <p>
	 * The decrypted state is held only until the next session for the key has been created from it
	 * (see {@link #_seedByKey} and {@link CookieSeedStore}). Used by the {@code browserUseCookies}
	 * verb; the next {@code browserNavigate} opens the seeded context.
	 * </p>
	 *
	 * @param sessionKey
	 *        The stable session key to seed.
	 * @param storageStateJson
	 *        The decrypted {@code storageState} JSON to seed; must not be {@code null}/blank.
	 */
	public void seedSession(Object sessionKey, String storageStateJson) {
		if (storageStateJson == null || storageStateJson.isBlank()) {
			throw new IllegalArgumentException("storageStateJson must not be blank");
		}
		_seedByKey.install(sessionKey, storageStateJson);
		BrowserSessionPool<BrowserSession> pool = _pool;
		if (pool != null) {
			// Discard any live (unseeded, or differently-seeded) session so the next call recreates
			// the context from the just-installed seed.
			pool.invalidate(sessionKey);
		}
	}

	/**
	 * The decrypted {@code storageState} currently parked for {@code sessionKey}, or {@code null}
	 * when none is - which is what must be true again once a session has been seeded from it.
	 * Package-visible for testing.
	 */
	String seedForTest(Object sessionKey) {
		return _seedByKey.lookup(sessionKey);
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
	 * Test-only: evaluates {@code js} on the current page of the session for {@code sessionKey} (on
	 * that session's confined thread) and returns the result. Lets a gated E2E test manipulate or
	 * probe a live page - covering an element, reading computed styles - through the same pooled
	 * session the tool verbs use. Package-visible for testing.
	 */
	Object evaluateForTest(Object sessionKey, String js) {
		return runOnSession(sessionKey, session -> session.evaluateForTest(js));
	}

	/**
	 * The filtering forward proxy of a session under no browsing policy, or {@code null} when the
	 * service is not started. Package-visible so a test can assert what the proxy refused.
	 */
	FilteringProxy proxyForTest() {
		return _proxies.get(BrowsingPolicy.unrestricted().name());
	}

	/**
	 * The number of currently registered (reusable) browser sessions. Package-visible for testing.
	 */
	int liveSessionCountForTest() {
		BrowserSessionPool<BrowserSession> pool = _pool;
		return pool == null ? 0 : pool.liveSessionCount();
	}

	/**
	 * The number of browser contexts that currently <em>exist</em> - what
	 * {@link Config#getMaxConcurrentSessions()} bounds: registered sessions plus any still being torn
	 * down. Package-visible for testing.
	 */
	int liveResourceCountForTest() {
		BrowserSessionPool<BrowserSession> pool = _pool;
		return pool == null ? 0 : pool.liveResourceCount();
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
