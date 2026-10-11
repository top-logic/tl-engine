/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.junit.Assume;
import org.junit.runner.RunWith;

import junit.framework.TestCase;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
import com.top_logic.tl.ai.core.execution.RunInfoBinding;
import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.core.tools.web.BrowserSession.DomainPolicy;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.PageView;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.AddressResolver;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.RequestPolicy;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.Target;
import com.top_logic.tl.ai.tool.api.RunInfo;

/**
 * End-to-end tests, with a real Chromium, that the URL provenance rule holds for the navigations a
 * page makes when the browser acts on it - a click on a link, a form submit - and not only for the
 * address {@code browserNavigate} is given.
 *
 * <p>
 * <b>The fixture.</b> Two public host names, both permitted by the domain policy, are pinned to local
 * fixture servers by the proxy's address resolver, as in {@link TestBrowserPolicyRecheck}: the page at
 * {@link #PAGE_HOST} holds a link and two forms that lead to {@link #COLLECTOR_HOST}. So the only rule
 * that can refuse a navigation in these tests is the provenance rule. Only DNS resolution of the two
 * names is needed; the tests skip themselves if it is unavailable.
 * </p>
 *
 * <p>
 * Gated behind {@link BrowserE2e}.
 * </p>
 */
@RunWith(AssumptionAwareRunner.class)
public class TestBrowserUrlProvenance extends TestCase {

	private static final String PAGE_HOST = "example.com";

	private static final String COLLECTOR_HOST = "www.example.com";

	private static final String PAGE = "http://" + PAGE_HOST + "/";

	private static final String LINKED = "http://" + COLLECTOR_HOST + "/linked";

	/** Both hosts are permitted: the domain policy refuses nothing in these tests. */
	private static final DomainPolicy BOTH_HOSTS =
		host -> PAGE_HOST.equalsIgnoreCase(host) || COLLECTOR_HOST.equalsIgnoreCase(host);

	private static final RequestPolicy PERMIT_ALL = host -> null;

	private static final int TIMEOUT_MILLIS = 30_000;

	/**
	 * A page as an injected one would look: a link the page holds, and two forms that send whatever is
	 * typed into them to another host - one by GET, one by POST.
	 */
	private static final String FORM_PAGE = ""
		+ "<!doctype html><html><head><title>fixture</title></head><body>"
		+ "<h1>search the archive</h1>"
		+ "<p><a href=\"" + LINKED + "\">the linked page</a></p>"
		+ "<form action=\"http://" + COLLECTOR_HOST + "/collect\" method=\"get\">"
		+ "<input name=\"q\" aria-label=\"query\"><button type=\"submit\">Search</button></form>"
		+ "<form action=\"http://" + COLLECTOR_HOST + "/post\" method=\"post\">"
		+ "<input name=\"notes\" aria-label=\"notes\"><button type=\"submit\">Send notes</button></form>"
		+ "</body></html>";

	private static final String COLLECTOR_PAGE = ""
		+ "<!doctype html><html><head><title>collector</title></head><body>"
		+ "<h1>collector page</h1></body></html>";

	/**
	 * A worker that types into a form and submits it to an address it was not given is refused, and
	 * the address is never contacted.
	 */
	public void testAFormSubmitToAnUnseenAddressIsRefused() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		try (RecordingServer page = RecordingServer.serving(FORM_PAGE);
				RecordingServer collector = RecordingServer.serving(COLLECTOR_PAGE);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(page, collector))) {
			ReadingList list = new ReadingList();
			list.seenUrls().add(PAGE);
			BrowserSession session = openSession("provenance-form", proxy);
			try (RunInfoBinding.Binding run = RunInfoBinding.bind(workerRun());
					ReadingListBinding.Binding reading = ReadingListBinding.bind(list)) {
				PageView loaded = session.navigate(PAGE);
				typeAsTheToolDoes(session, ref(loaded, "input", "query"), "the findings of this run");
				try {
					PageView sent = session.click(ref(loaded, "button", "Search"));
					fail("a form submit to an address the run was not given must be refused: " + sent.finalUrl());
				} catch (BrowserPolicyException expected) {
					assertEquals(UrlProvenance.NAVIGATION_REFUSED, expected.getMessage());
				}
			} finally {
				session.close();
			}
			assertFalse("the collector never received the typed text: " + collector.requestLines(),
				collector.requestLines().toString().contains("findings"));
		}
	}

	/** Without the rule - the chat's own agent - the same form submit goes through. */
	public void testAnUnboundRunSubmitsTheForm() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		try (RecordingServer page = RecordingServer.serving(FORM_PAGE);
				RecordingServer collector = RecordingServer.serving(COLLECTOR_PAGE);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(page, collector))) {
			BrowserSession session = openSession("provenance-lead", proxy);
			try (RunInfoBinding.Binding run = RunInfoBinding.bind(leadRun());
					ReadingListBinding.Binding reading = ReadingListBinding.bind(new ReadingList())) {
				PageView loaded = session.navigate(PAGE);
				session.type(ref(loaded, "input", "query"), "storage");
				PageView sent = session.click(ref(loaded, "button", "Search"));
				assertTrue(sent.finalUrl(), sent.finalUrl().startsWith("http://" + COLLECTOR_HOST + "/collect?q=storage"));
			} finally {
				session.close();
			}
		}
	}

	/** A link to an address the run was given is followed. */
	public void testALinkTheRunWasGivenIsFollowed() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		try (RecordingServer page = RecordingServer.serving(FORM_PAGE);
				RecordingServer collector = RecordingServer.serving(COLLECTOR_PAGE);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(page, collector))) {
			ReadingList list = new ReadingList();
			list.seenUrls().add(PAGE);
			list.seenUrls().add(LINKED);
			BrowserSession session = openSession("provenance-link", proxy);
			try (RunInfoBinding.Binding run = RunInfoBinding.bind(workerRun());
					ReadingListBinding.Binding reading = ReadingListBinding.bind(list)) {
				PageView loaded = session.navigate(PAGE);
				PageView followed = session.click(ref(loaded, "a", "the linked page"));
				assertEquals(LINKED, followed.finalUrl());
				assertTrue(followed.html(), followed.html().contains("collector page"));
			} finally {
				session.close();
			}
		}
	}

	/** A link to an address the run was not given is not followed, and the address is never contacted. */
	public void testALinkToAnUnseenAddressIsRefused() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		try (RecordingServer page = RecordingServer.serving(FORM_PAGE);
				RecordingServer collector = RecordingServer.serving(COLLECTOR_PAGE);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(page, collector))) {
			ReadingList list = new ReadingList();
			list.seenUrls().add(PAGE);
			BrowserSession session = openSession("provenance-unseen-link", proxy);
			try (RunInfoBinding.Binding run = RunInfoBinding.bind(workerRun());
					ReadingListBinding.Binding reading = ReadingListBinding.bind(list)) {
				PageView loaded = session.navigate(PAGE);
				try {
					session.click(ref(loaded, "a", "the linked page"));
					fail("a link to an address the run was not given must not be followed");
				} catch (BrowserPolicyException expected) {
					assertEquals(UrlProvenance.NAVIGATION_REFUSED, expected.getMessage());
				}
			} finally {
				session.close();
			}
			assertEquals("the unseen address was never contacted", 0, collector.contactCount());
		}
	}

	/**
	 * After the run typed into a page, a form that sends its data in the request body is refused even
	 * to an address the run was given: the address does not carry the typed text, the body does.
	 */
	public void testAFormPostAfterTypingIsRefused() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		try (RecordingServer page = RecordingServer.serving(FORM_PAGE);
				RecordingServer collector = RecordingServer.serving(COLLECTOR_PAGE);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(page, collector))) {
			ReadingList list = new ReadingList();
			list.seenUrls().add(PAGE);
			list.seenUrls().add("http://" + COLLECTOR_HOST + "/post");
			BrowserSession session = openSession("provenance-post", proxy);
			try (RunInfoBinding.Binding run = RunInfoBinding.bind(workerRun());
					ReadingListBinding.Binding reading = ReadingListBinding.bind(list)) {
				PageView loaded = session.navigate(PAGE);
				typeAsTheToolDoes(session, ref(loaded, "input", "notes"), "the findings of this run");
				try {
					session.click(ref(loaded, "button", "Send notes"));
					fail("a form post after typing must be refused");
				} catch (BrowserPolicyException expected) {
					assertEquals(UrlProvenance.FORM_REFUSED, expected.getMessage());
				}
			} finally {
				session.close();
			}
			assertEquals("nothing was posted", 0, collector.contactCount());
		}
	}

	/**
	 * An address the run typed into a page is shown back by the page's snapshot, but it does not
	 * become an address the run was given; the page's own links still do.
	 */
	public void testATypedAddressInASnapshotIsNotSeen() {
		BrowserE2e.assumeEnabled();
		Assume.assumeTrue("DNS does not resolve the fixture hosts - no network", resolvable());
		String composed = "http://" + COLLECTOR_HOST + "/?d=the-findings-of-this-run";
		try (RecordingServer page = RecordingServer.serving(FORM_PAGE);
				RecordingServer collector = RecordingServer.serving(COLLECTOR_PAGE);
				FilteringProxy proxy = FilteringProxy.start(PERMIT_ALL, resolvingTo(page, collector))) {
			ReadingList list = new ReadingList();
			list.seenUrls().add(PAGE);
			BrowserSession session = openSession("provenance-snapshot", proxy);
			BrowserToolsService service = newService();
			try (RunInfoBinding.Binding run = RunInfoBinding.bind(workerRun());
					ReadingListBinding.Binding reading = ReadingListBinding.bind(list)) {
				PageView loaded = session.navigate(PAGE);
				typeAsTheToolDoes(session, ref(loaded, "input", "query"), composed);

				String snapshot = BrowserTool.execute(service, session::snapshot, false, true);

				assertTrue("the page shows what was typed: " + snapshot, snapshot.contains(composed));
				assertNotNull("but the typed address was not given to the run", UrlProvenance.refusal(composed));
				assertNull("the page's own link was", UrlProvenance.refusal(LINKED));
			} finally {
				session.close();
			}
		}
	}

	// --- Fixture helpers. ---

	/** Types into a field the way {@code browserType} does: the run's ledger learns the text first. */
	private static void typeAsTheToolDoes(BrowserSession session, String ref, String text) {
		UrlProvenance.typed(text);
		session.type(ref, text);
	}

	private static BrowserToolsService newService() {
		BrowserToolsService.Config<?> config = TypedConfiguration.newConfigItem(BrowserToolsService.Config.class);
		return new BrowserToolsService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}

	private static RunInfo leadRun() {
		return new RunInfo("lead", null, "lead", null, null, null, 0, null);
	}

	private static RunInfo workerRun() {
		return new RunInfo("worker", null, "worker", null, null, null, 1, leadRun());
	}

	/** The ref of the element with the given role and accessible name. */
	private static String ref(PageView view, String role, String name) {
		return view.elements().stream()
			.filter(element -> role.equals(element.role()) && name.equals(element.name()))
			.map(BrowserToolsService.InteractiveElement::ref)
			.findFirst()
			.orElseThrow(() -> new AssertionError("no " + role + " '" + name + "' in " + view.elements()));
	}

	private static AddressResolver resolvingTo(RecordingServer page, RecordingServer collector) {
		AddressResolver real = FilteringProxy.ssrfResolver();
		return (host, port) -> {
			if (PAGE_HOST.equalsIgnoreCase(host)) {
				return Target.to(new InetSocketAddress(InetAddress.getLoopbackAddress(), page.port()));
			}
			if (COLLECTOR_HOST.equalsIgnoreCase(host)) {
				return Target.to(new InetSocketAddress(InetAddress.getLoopbackAddress(), collector.port()));
			}
			return real.resolve(host, port);
		};
	}

	private static BrowserSession openSession(String key, FilteringProxy proxy) {
		return BrowserSession.open(key, TIMEOUT_MILLIS, BrowserSession.DEFAULT_ACTION_TIMEOUT_MILLIS, 0,
			BOTH_HOSTS, null, false, false, proxy);
	}

	private static boolean resolvable() {
		for (String host : new String[] { PAGE_HOST, COLLECTOR_HOST }) {
			try {
				InetAddress.getByName(host);
			} catch (UnknownHostException ex) {
				return false;
			}
		}
		return true;
	}
}
