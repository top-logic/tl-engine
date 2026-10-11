/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.DataObject;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.ApplicationUrl;
import com.top_logic.tl.ai.core.execution.model.ArchivedPage;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.tool.api.ReadSource;
import com.top_logic.util.model.ModelService;

/**
 * Keeps a copy of every web page an agent run read, so that a citation of it still shows what the
 * page said after it changed or disappeared.
 *
 * <p>
 * A web source is archived when it enters a run's reading list: {@code webFetch} and the browser
 * hand the text they extracted to this service, and the citation of the page carries the address of
 * the copy. A quoted passage is checked against the same text. The copy is stored as a {@code tl.ai.execution:ArchivedPage}, its text as a
 * {@code tl.core:Binary} - the attribute type the engine's blob store will take over, so that moving
 * the archive there is a change of storage, not of this class.
 * </p>
 *
 * <p>
 * <b>Its own transaction, on its own thread.</b> A read tool runs while the turn may hold an open
 * transaction for the writes of an earlier call; a copy written into it would be rolled back with
 * that turn and the citation would point at nothing. The copy is therefore written by this service's
 * own worker in a system interaction, and the tool waits for it.
 * </p>
 *
 * <p>
 * <b>Extended once, not per window.</b> A long document is read in windows, and its copy is to hold
 * every window read. Rewriting the copy per window would store the text read so far again for every
 * window - a document read in <i>k</i> windows would cost <i>k</i> commits and some <i>k</i>²/2
 * windows of bytes. The first window is therefore stored at once, so the citation has an address,
 * and what later windows add is only remembered: the copy is given the longer text when the run
 * ends ({@link #flush(ReadingList)}), when somebody opens it, when a research run is kept with it,
 * at the latest {@link #FLUSH_DELAY} after it was first extended, and when the service shuts down.
 * The copy can therefore lag what the run read while the run is still reading; a process that dies
 * meanwhile keeps the windows written until then.
 * </p>
 *
 * <p>
 * <b>Bounded.</b> At most {@link Config#getMaxChars()} characters of a page are kept, the same page
 * read twice with the same text by the same account is stored once (within the life of the
 * process), and copies older than {@link Config#getRetentionDays()} are deleted once a day - except a
 * copy a kept research run references, which lives as long as the run.
 * </p>
 *
 * @see #archive(String, String, String, Person)
 * @see ArchivedPageServlet
 */
@ServiceDependencies({
	ModelService.Module.class,
	PersistencyLayer.Module.class,
})
public class PageArchive extends ConfiguredManagedClass<PageArchive.Config<?>> {

	/** Path prefix of the route an archived copy is opened at. */
	public static final String URL_PREFIX = "/archived-page/";

	/** What the stored text is. */
	public static final String CONTENT_TYPE = "text/markdown; charset=utf-8";

	/** Length of the random handle in bytes, before base64url encoding. */
	private static final int HANDLE_BYTES = 16;

	/** The longest title the table column holds. */
	private static final int MAX_TITLE = 500;

	/** The longest address the table column holds; a longer one is not archived. */
	private static final int MAX_URL = 2048;

	/** How many recent copies are remembered for de-duplication. */
	private static final int DEDUP_ENTRIES = 1_000;

	/** How long a tool waits for its copy to be written before it reads on without one. */
	private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(20);

	/**
	 * How long what later windows add to a copy may stay unwritten when nothing else writes it - a
	 * run that never ends normally, a tool called outside of one.
	 */
	static final Duration FLUSH_DELAY = Duration.ofMinutes(1);

	/** Configuration of {@link PageArchive}. */
	public interface Config<I extends PageArchive> extends ConfiguredManagedClass.Config<I> {

		/** Configuration property name for {@link #isEnabled()}. */
		String ENABLED = "enabled";

		/** Configuration property name for {@link #getMaxChars()}. */
		String MAX_CHARS = "max-chars";

		/** Configuration property name for {@link #getRetentionDays()}. */
		String RETENTION_DAYS = "retention-days";

		/**
		 * Whether web pages an agent read are archived at all.
		 *
		 * <p>
		 * Without it a web citation links to the live page only.
		 * </p>
		 *
		 * @see #isEnabled()
		 */
		@Name(ENABLED)
		@BooleanDefault(true)
		boolean isEnabled();

		/**
		 * How many characters of a page's text are kept; the rest is cut off.
		 *
		 * <p>
		 * What is kept of a page is what the run read of it. A long document is read in windows, and
		 * the copy holds every window read, so the default holds a long report read through.
		 * </p>
		 *
		 * @see #getMaxChars()
		 */
		@Name(MAX_CHARS)
		@IntDefault(1_000_000)
		int getMaxChars();

		/**
		 * After how many days an archived copy is deleted; 0 keeps every copy.
		 *
		 * <p>
		 * A citation of a deleted copy still links to the live page. A copy a kept research run
		 * references is kept as long as the run.
		 * </p>
		 *
		 * @see #getRetentionDays()
		 */
		@Name(RETENTION_DAYS)
		@IntDefault(90)
		int getRetentionDays();
	}

	/**
	 * A stored copy, as a citation refers to it.
	 *
	 * @param handle
	 *        The unguessable key the copy is opened by.
	 * @param viewUrl
	 *        Where it can be looked at.
	 * @param fetchedAt
	 *        When the page was read.
	 */
	public record Copy(String handle, String viewUrl, Instant fetchedAt) {
		// Value type only.
	}

	private final SecureRandom _random = new SecureRandom();

	/** Recently stored copies by owner, address and text hash. */
	private final Map<String, Copy> _recent = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Copy> eldest) {
			return size() > DEDUP_ENTRIES;
		}
	};

	/**
	 * The copies this service stored or extended, by handle: those it may extend without looking
	 * them up, because it knows their address and owner.
	 */
	private final Map<String, Known> _known = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Known> eldest) {
			return size() > DEDUP_ENTRIES;
		}
	};

	/** The longer text of a copy that is not written yet, by handle; guarded by {@link #_known}. */
	private final Map<String, Pending> _pending = new LinkedHashMap<>();

	private ScheduledExecutorService _worker;

	/** A stored copy, with what decides whether a run may extend it. */
	private record Known(String url, Person owner, Copy copy) {
		// Pure data.
	}

	/** What a copy is to be given when it is next written. */
	private record Pending(String url, String title, String text, Person owner) {
		// Pure data.
	}

	/**
	 * Creates a {@link PageArchive} from configuration.
	 *
	 * @param context
	 *        For instantiation of dependent configured objects.
	 * @param config
	 *        The configuration.
	 */
	public PageArchive(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		_worker = Executors.newSingleThreadScheduledExecutor(task -> {
			Thread thread = new Thread(task, "tl-ai-page-archive");
			thread.setDaemon(true);
			return thread;
		});
		if (getConfig().getRetentionDays() > 0) {
			_worker.scheduleWithFixedDelay(this::purgeQuietly, 1, 24 * 60, TimeUnit.MINUTES);
		}
	}

	@Override
	protected void shutDown() {
		if (_worker != null) {
			flush(pendingHandles());
			_worker.shutdownNow();
			_worker = null;
		}
		super.shutDown();
	}

	/**
	 * Stores a copy of a page an agent run read.
	 *
	 * @param url
	 *        The address the page was read from.
	 * @param title
	 *        Its title, or empty.
	 * @param text
	 *        What was extracted of it.
	 * @param owner
	 *        The account whose run read it; <code>null</code> for a run without one, whose copy only
	 *        administrators can open.
	 * @return The copy, or <code>null</code> when nothing was stored - archiving is switched off, the
	 *         text is empty, the address too long, or the write failed. A citation then links to
	 *         the live page only; the read itself is never refused for it.
	 */
	public Copy archive(String url, String title, String text, Person owner) {
		return archive(url, title, text, owner, null);
	}

	/**
	 * Stores what a run read of a page, extending the copy it already has of that page.
	 *
	 * <p>
	 * A long document is read in windows, and each window adds to what the run read of it. Storing a
	 * new copy per window would store the beginning of the document once per window; the copy the
	 * run already has is given the longer text instead, under the same handle, so a citation that
	 * points at it shows the whole text read. The copy is extended only when it is a copy of the same
	 * address and belongs to the same account; otherwise a new copy is stored.
	 * </p>
	 *
	 * <p>
	 * A copy this service stored is not written again here: the longer text is remembered and
	 * written once, later (see the class comment). Only a copy it does not know is looked up and
	 * written at once.
	 * </p>
	 *
	 * @param url
	 *        The address the page was read from.
	 * @param title
	 *        Its title, or empty.
	 * @param text
	 *        What the run read of it.
	 * @param owner
	 *        The account whose run read it; <code>null</code> for a run without one.
	 * @param previousHandle
	 *        The handle of the copy the run has of this page, or <code>null</code> for the first read.
	 * @return The copy, or <code>null</code> when nothing was stored.
	 * @see #archive(String, String, String, Person)
	 */
	public Copy archive(String url, String title, String text, Person owner, String previousHandle) {
		if (!getConfig().isEnabled() || _worker == null || url == null || url.isBlank()
			|| url.length() > MAX_URL || text == null || text.isBlank()) {
			return null;
		}
		if (previousHandle != null && !previousHandle.isEmpty()) {
			Copy deferred = defer(previousHandle, url, title, text, owner);
			if (deferred != null) {
				return deferred;
			}
		}
		String kept = kept(text);
		String hash = sha256(kept);
		String key = ownerKey(owner) + "|" + url + "|" + hash;
		synchronized (_recent) {
			Copy known = _recent.get(key);
			if (known != null) {
				return known;
			}
		}
		Copy copy = null;
		if (previousHandle != null && !previousHandle.isEmpty()) {
			copy = write(url, () -> replace(previousHandle, url, title, kept, hash, owner));
		}
		if (copy == null) {
			String handle = newHandle();
			Copy created = new Copy(handle, viewUrl(handle), Instant.now());
			copy = write(url, () -> {
				store(created, url, title, kept, hash, owner);
				return created;
			});
		}
		if (copy != null) {
			synchronized (_recent) {
				forgetTexts(copy.handle());
				_recent.put(key, copy);
			}
			synchronized (_known) {
				_known.put(copy.handle(), new Known(url, owner, copy));
			}
		}
		return copy;
	}

	/**
	 * Forgets which texts the copy under {@code handle} was stored with: it is being given a longer
	 * one, and a run that reads only the shorter one must not be handed a copy that shows more than
	 * it read. Called while holding {@link #_recent}.
	 */
	private void forgetTexts(String handle) {
		_recent.values().removeIf(copy -> copy.handle().equals(handle));
	}

	/** The part of a text a copy keeps. */
	private String kept(String text) {
		return text.length() > getConfig().getMaxChars()
			? text.substring(0, Math.max(0, getConfig().getMaxChars()))
			: text;
	}

	/**
	 * Remembers the longer text of a copy this service knows, to be written later.
	 *
	 * @return The copy, or <code>null</code> when this service does not know it as a copy of
	 *         {@code url} owned by {@code owner}.
	 */
	private Copy defer(String handle, String url, String title, String text, Person owner) {
		boolean first;
		Copy copy;
		synchronized (_known) {
			Known known = _known.get(handle);
			if (known == null || !url.equals(known.url()) || !Objects.equals(owner, known.owner())) {
				return null;
			}
			copy = known.copy();
			first = _pending.put(handle, new Pending(url, title, text, owner)) == null;
		}
		synchronized (_recent) {
			forgetTexts(handle);
		}
		if (first) {
			// The safety net for a copy nothing else flushes; flushing it earlier leaves this with
			// nothing to do.
			try {
				_worker.schedule(() -> writePending(handle), FLUSH_DELAY.toMillis(), TimeUnit.MILLISECONDS);
			} catch (RejectedExecutionException ex) {
				// Shutting down: the shutdown writes what is pending.
			}
		}
		return copy;
	}

	/**
	 * Writes what the run read of its web pages beyond what their copies hold.
	 *
	 * <p>
	 * Called when the run ends, so that the copies its citations point at show every window it read.
	 * Waits for the writes; does nothing where this service is not running.
	 * </p>
	 *
	 * @param list
	 *        The run's reading list; may be <code>null</code>.
	 */
	public static void flush(ReadingList list) {
		if (list == null || !Module.INSTANCE.isActive()) {
			return;
		}
		List<String> handles = new ArrayList<>();
		for (ReadSource source : list.sources()) {
			String handle = handleOf(source.archiveUrl());
			if (!handle.isEmpty()) {
				handles.add(handle);
			}
		}
		getInstance().flush(handles);
	}

	/**
	 * Writes what the copies under the given handles are still to be given, and waits for it.
	 *
	 * @param handles
	 *        The copies; a handle with nothing pending is skipped.
	 * @return Whether anything was written.
	 */
	public boolean flush(Collection<String> handles) {
		List<String> pending = new ArrayList<>();
		synchronized (_known) {
			for (String handle : handles) {
				if (_pending.containsKey(handle)) {
					pending.add(handle);
				}
			}
		}
		ScheduledExecutorService worker = _worker;
		if (pending.isEmpty() || worker == null) {
			return false;
		}
		Future<Boolean> result = worker.submit(() -> {
			boolean written = false;
			for (String handle : pending) {
				written |= writePending(handle);
			}
			return Boolean.valueOf(written);
		});
		try {
			return result.get(WRITE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS).booleanValue();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return false;
		} catch (ExecutionException | TimeoutException ex) {
			Logger.error("Extending the archived copies " + pending + " failed.", ex, PageArchive.class);
			return false;
		}
	}

	private List<String> pendingHandles() {
		synchronized (_known) {
			return new ArrayList<>(_pending.keySet());
		}
	}

	/**
	 * Gives the copy under {@code handle} its pending text, on the worker.
	 *
	 * @return Whether a copy was written.
	 */
	private boolean writePending(String handle) {
		Pending pending;
		synchronized (_known) {
			pending = _pending.remove(handle);
		}
		if (pending == null) {
			return false;
		}
		String kept = kept(pending.text());
		String hash = sha256(kept);
		String key = ownerKey(pending.owner()) + "|" + pending.url() + "|" + hash;
		try {
			Copy copy = ThreadContextManager.inSystemInteraction(PageArchive.class,
				() -> replace(handle, pending.url(), pending.title(), kept, hash, pending.owner()));
			if (copy == null) {
				Logger.info("The archived copy of " + pending.url() + " is gone; what the run read on is not kept.",
					PageArchive.class);
				return false;
			}
			synchronized (_recent) {
				forgetTexts(handle);
				_recent.put(key, copy);
			}
			return true;
		} catch (RuntimeException ex) {
			Logger.error("Extending the archived copy of " + pending.url() + " failed.", ex, PageArchive.class);
			return false;
		}
	}

	/**
	 * Runs {@code write} on this service's worker in a system interaction and waits for it.
	 *
	 * @return What the write answered, or <code>null</code> when it failed or took too long.
	 */
	private Copy write(String url, Computation<Copy> write) {
		Future<Copy> result = _worker.submit(() -> ThreadContextManager.inSystemInteraction(PageArchive.class,
			() -> write.run()));
		try {
			return result.get(WRITE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return null;
		} catch (ExecutionException | TimeoutException ex) {
			Logger.error("Archiving " + url + " failed; its citation links to the live page only.", ex,
				PageArchive.class);
			return null;
		}
	}

	/** A write of the archive, run on its worker. */
	private interface Computation<T> {

		/** Performs the write. */
		T run();
	}

	/**
	 * Gives the copy under {@code handle} the new text, when it is a copy of {@code url} owned by
	 * {@code owner}.
	 *
	 * @return The copy with its handle and first read time, or <code>null</code> when there is no
	 *         such copy.
	 */
	private Copy replace(String handle, String url, String title, String text, String hash, Person owner) {
		Optional<ArchivedPage> found = find(handle);
		if (found.isEmpty()) {
			return null;
		}
		ArchivedPage page = found.get();
		if (!url.equals(page.getUrl()) || !Objects.equals(owner, page.getOwner())) {
			return null;
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(ResKey.text("Extend archived web page " + url))) {
			if (title != null && !title.isEmpty()) {
				page.setTitle(title.length() > MAX_TITLE ? title.substring(0, MAX_TITLE) : title);
			}
			page.setContentHash(hash);
			page.setContent(BinaryDataFactory.createBinaryData(text.getBytes(StandardCharsets.UTF_8),
				CONTENT_TYPE, "page.md"));
			tx.commit();
		}
		Date fetchedAt = page.getFetchedAt();
		return new Copy(handle, viewUrl(handle), fetchedAt == null ? Instant.now() : fetchedAt.toInstant());
	}

	private void store(Copy copy, String url, String title, String text, String hash, Person owner) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(ResKey.text("Archive web page " + url))) {
			ArchivedPage page = TlAiExecutionFactory.getInstance().createArchivedPage();
			page.setHandle(copy.handle());
			page.setUrl(url);
			page.setTitle(title == null ? "" : title.length() > MAX_TITLE ? title.substring(0, MAX_TITLE) : title);
			page.setFetchedAt(Date.from(copy.fetchedAt()));
			page.setContentHash(hash);
			page.setContent(BinaryDataFactory.createBinaryData(text.getBytes(StandardCharsets.UTF_8),
				CONTENT_TYPE, "page.md"));
			if (owner != null) {
				page.setOwner(owner);
			}
			tx.commit();
		}
	}

	/**
	 * The copy stored under {@code handle}, with every window read of it so far.
	 *
	 * <p>
	 * Writes what the copy is still to be given first, so whoever opens it sees all the run read.
	 * The calling thread then reads at the newest revision, to see that write: call it where nothing
	 * the thread has read so far needs to stay as it was - a request that only shows the copy.
	 * </p>
	 *
	 * @param handle
	 *        The handle a {@link #viewUrl(String) view address} names.
	 * @return The copy, or empty for an unknown handle.
	 */
	public Optional<ArchivedPage> findByHandle(String handle) {
		if (handle != null && flush(List.of(handle))) {
			HistoryUtils.updateSessionRevision();
		}
		return find(handle);
	}

	/**
	 * The copy stored under {@code handle}, also where this service is not running.
	 *
	 * @param handle
	 *        The handle a {@link #viewUrl(String) view address} names.
	 * @return The copy, or empty for an unknown handle.
	 * @see #findByHandle(String)
	 */
	public static Optional<ArchivedPage> find(String handle) {
		if (handle == null || handle.isEmpty()) {
			return Optional.empty();
		}
		DataObject found = PersistencyLayer.getKnowledgeBase()
			.getObjectByAttribute(TlAiExecutionFactory.KO_NAME_ARCHIVED_PAGE, ArchivedPage.HANDLE_ATTR, handle);
		if (found instanceof KnowledgeItem item && item.getWrapper() instanceof ArchivedPage page) {
			return Optional.of(page);
		}
		return Optional.empty();
	}

	/** The text of a stored copy, or empty when it has none. */
	public static String text(ArchivedPage page) {
		BinaryDataSource content = page == null ? null : page.getContent();
		if (content == null) {
			return "";
		}
		try (var in = content.toData().getStream()) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (java.io.IOException ex) {
			Logger.error("Cannot read the archived copy of " + page.getUrl() + ".", ex, PageArchive.class);
			return "";
		}
	}

	/**
	 * Whether {@code account} may open the copy: the account whose run read the page, or an
	 * administrator. A copy without an owner is an administrator's alone.
	 */
	public static boolean mayRead(ArchivedPage page, Person account) {
		if (page == null || account == null) {
			return false;
		}
		if (Person.isAdmin(account)) {
			return true;
		}
		return account.equals(page.getOwner());
	}

	/** Where the copy under {@code handle} can be looked at. */
	public static String viewUrl(String handle) {
		return ApplicationUrl.of(URL_PREFIX + handle);
	}

	/**
	 * The handle a {@link #viewUrl(String) view address} names.
	 *
	 * @param viewUrl
	 *        An address of an archived copy, absolute or relative; may be {@code null}.
	 * @return The handle, or the empty string when the address names no archived copy.
	 */
	public static String handleOf(String viewUrl) {
		if (viewUrl == null) {
			return "";
		}
		int start = viewUrl.indexOf(URL_PREFIX);
		if (start < 0) {
			return "";
		}
		String handle = viewUrl.substring(start + URL_PREFIX.length());
		int end = handle.indexOf('?');
		return end < 0 ? handle : handle.substring(0, end);
	}

	/**
	 * Deletes every copy read before {@code cutoff}.
	 *
	 * @return How many copies were deleted.
	 */
	public int purgeOlderThan(Instant cutoff) {
		List<ArchivedPage> expired = new ArrayList<>();
		for (TLObject object : MetaElementUtil.getAllInstancesOf(TlAiExecutionFactory.getArchivedPageType(),
			TLObject.class)) {
			ArchivedPage page = (ArchivedPage) object;
			Date fetchedAt = page.getFetchedAt();
			if (fetchedAt != null && fetchedAt.toInstant().isBefore(cutoff) && !isResearchSource(page)) {
				expired.add(page);
			}
		}
		if (expired.isEmpty()) {
			return 0;
		}
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
			.beginTransaction(ResKey.text("Delete " + expired.size() + " expired archived web pages"))) {
			for (ArchivedPage page : expired) {
				page.tDelete();
			}
			tx.commit();
		}
		synchronized (_recent) {
			_recent.clear();
		}
		synchronized (_known) {
			_known.clear();
		}
		return expired.size();
	}

	/**
	 * Whether a kept research run rests on the copy: it then lives as long as the run, so that the
	 * run's quotes can still be checked against what its sources said.
	 */
	private static boolean isResearchSource(ArchivedPage page) {
		return !page.tReferers(TlAiExecutionFactory.getPageResearchSourceAttr()).isEmpty();
	}

	private void purgeQuietly() {
		try {
			Instant cutoff = Instant.now().minus(Duration.ofDays(getConfig().getRetentionDays()));
			int deleted = ThreadContextManager.inSystemInteraction(PageArchive.class, () -> purgeOlderThan(cutoff));
			if (deleted > 0) {
				Logger.info("Deleted " + deleted + " archived web pages read before " + cutoff + ".",
					PageArchive.class);
			}
		} catch (RuntimeException ex) {
			Logger.error("Purging expired archived web pages failed.", ex, PageArchive.class);
		}
	}

	private String newHandle() {
		byte[] bytes = new byte[HANDLE_BYTES];
		_random.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private static String ownerKey(Person owner) {
		return owner == null ? "" : String.valueOf(owner.tId());
	}

	private static String sha256(String text) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is part of every Java runtime.", ex);
		}
	}

	/** The running instance. */
	public static PageArchive getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/** Module of {@link PageArchive}. */
	public static final class Module extends TypedRuntimeModule<PageArchive> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton
		}

		@Override
		public Class<PageArchive> getImplementation() {
			return PageArchive.class;
		}
	}
}
