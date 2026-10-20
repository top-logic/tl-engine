/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.sync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.tl.ai.index.config.IndexedTypes;
import com.top_logic.tl.ai.index.embed.Embedder;
import com.top_logic.tl.ai.index.embed.EmbeddingException;
import com.top_logic.tl.ai.index.lucene.Generations;
import com.top_logic.tl.ai.index.lucene.IndexGeneration;
import com.top_logic.tl.ai.index.text.Chunker;

/**
 * Keeps the index in step with the knowledge base, by pulling the history.
 *
 * <h2>One code path</h2>
 *
 * <p>
 * A commit only wakes the worker; the {@link UpdateListener} does nothing else, so it can neither
 * fail nor slow down a commit. The worker reads every change after its watermark from the
 * database ({@link ChangeCollector}), re-indexes the changed objects in batches and commits the new
 * watermark with the Lucene commit that holds their chunks. A crash, a restart, a long downtime and
 * normal work are the same loop. Replaying a range is safe, because an object's chunks are replaced
 * as a whole.
 * </p>
 *
 * <h2>Generations</h2>
 *
 * <p>
 * The configuration (types, chunking, embedding model) has a fingerprint. When the newest built
 * generation was built with another one, a new generation is built beside it: first every indexed
 * object, from a key file and in batches ({@link BuildKeys}), then the history since the build
 * started. The old generation serves searches - with words only when its vectors come from another
 * model - until the new one has caught up, then the new one serves and the old one is deleted. A
 * build reuses the old generation's vectors of unchanged texts, so a change of the analysis or of a
 * keyword embeds nothing.
 * </p>
 *
 * <h2>Threads</h2>
 *
 * <p>
 * One worker thread per index, of low priority, in a system interaction that moves to the newest
 * revision before each step. Searches run on the callers' threads against the serving generation,
 * under a read lock, so a switch never closes a generation a search is reading.
 * </p>
 *
 * <h2>Cluster</h2>
 *
 * <p>
 * Each node keeps its own index and reads the shared history itself, so it sees the commits of all
 * nodes, with the lag of the node's refetch. Nothing is shared and nothing is locked.
 * </p>
 */
public final class IndexSync {

	/** Commit data of a generation being built: the revision the build started at. */
	static final String BUILD_REVISION = "buildRevision";

	/** Commit data of a generation being built: the offset in the key file reached so far. */
	static final String BUILD_OFFSET = "buildOffset";

	/** Commit data of a generation being built: how many objects it has to index. */
	static final String BUILD_TOTAL = "buildTotal";

	/** Commit data of a generation being built: how many objects are done. */
	static final String BUILD_DONE = "buildDone";

	/**
	 * Commit data of a built generation: some of its chunks were written without the vectors of its
	 * model, because another embedder or none was in use. It serves, but the next start with its
	 * model builds a new generation, which embeds only these chunks.
	 */
	static final String VECTORS_MISSING = "vectorsMissing";

	/** How long the worker sleeps when nothing woke it. */
	private static final long IDLE_MILLIS = 5_000;

	/** The longest pause after repeated failures. */
	private static final long MAX_BACKOFF_MILLIS = 10 * 60_000;

	/**
	 * What the sync works with.
	 *
	 * @param kb
	 *        The knowledge base to follow.
	 * @param root
	 *        The directory of the generations.
	 * @param types
	 *        The indexed types.
	 * @param chunker
	 *        How texts are cut.
	 * @param maxTextChars
	 *        The longest text indexed of one attribute.
	 * @param embedder
	 *        The embedder, or <code>null</code> for words only.
	 * @param embedderUnavailable
	 *        Whether an embedder is configured but could not be created: then the newest built
	 *        generation serves and catches up with words only, instead of a new build that deletes
	 *        its vectors.
	 * @param fingerprint
	 *        The fingerprint of the configuration.
	 * @param batchSize
	 *        Objects per batch.
	 * @param revisionSlice
	 *        Revisions of the history read at once.
	 * @param buildMillis
	 *        How long one step of a build works before the worker commits and looks at the history.
	 */
	public record Settings(KnowledgeBase kb, Path root, IndexedTypes types, Chunker chunker, int maxTextChars,
			Embedder embedder, boolean embedderUnavailable, String fingerprint, int batchSize, int revisionSlice,
			long buildMillis) {
		// A plain carrier.
	}

	/**
	 * A generation and its number.
	 */
	private record Gen(int number, IndexGeneration index) {
		Map<String, String> data() {
			return index.commitData();
		}

		long watermark() {
			String value = data().get(IndexGeneration.WATERMARK);
			return value == null ? -1 : Long.parseLong(value);
		}

		boolean built() {
			return data().containsKey(IndexGeneration.WATERMARK);
		}
	}

	/**
	 * The serving generation of a search, as long as the search holds it.
	 *
	 * @param index
	 *        The generation.
	 * @param vectors
	 *        Whether its vectors come from the configured embedder, so a query vector can be compared
	 *        with them.
	 * @param watermark
	 *        The revision it has reached.
	 */
	public record Serving(IndexGeneration index, boolean vectors, long watermark) {
		// A plain carrier.
	}

	/**
	 * A search on the serving generation.
	 */
	@FunctionalInterface
	public interface ServingFunction<T> {

		/**
		 * Searches; <code>serving</code> is <code>null</code> when no generation is built yet.
		 */
		T apply(Serving serving) throws IOException;
	}

	private final Settings _settings;

	private final Generations _generations;

	private final ChangeCollector _collector;

	private final ObjectIndexer _indexer;

	private final ObjectIndexer _wordsIndexer;

	private final String _model;

	private final ReentrantReadWriteLock _switch = new ReentrantReadWriteLock();

	private final Object _wake = new Object();

	private final Object _progress = new Object();

	private final UpdateListener _listener = this::notifyUpdate;

	private volatile Gen _serving;

	private volatile Gen _building;

	private volatile boolean _running;

	private volatile boolean _woken;

	private volatile long _head = -1;

	private volatile String _lastError;

	/** Guarded by {@link #_wake}: the worker has ended. */
	private boolean _ended;

	/** Guarded by {@link #_wake}: the stop gave up waiting, so the worker closes the generations. */
	private boolean _closeAtEnd;

	private Thread _thread;

	private Runnable _beforeCommit = () -> {
		// No hook by default.
	};

	private int _largestBatch;

	/**
	 * Creates an {@link IndexSync}; {@link #start()} opens the generations and starts the worker.
	 */
	public IndexSync(Settings settings) {
		_settings = settings;
		_generations = new Generations(settings.root());
		_collector = new ChangeCollector(settings.kb(), settings.types());
		_indexer = new ObjectIndexer(settings.kb(), settings.types(), settings.chunker(), settings.maxTextChars(),
			settings.embedder());
		_wordsIndexer = new ObjectIndexer(settings.kb(), settings.types(), settings.chunker(),
			settings.maxTextChars(), null);
		_model = settings.embedder() == null ? "" : settings.embedder().fingerprint();
	}

	/**
	 * Opens the generations, registers for commits and starts the worker.
	 */
	public void start() throws IOException {
		try {
			openGenerations();
		} catch (IOException | RuntimeException ex) {
			closeGenerations();
			throw ex;
		}
		_running = true;
		_settings.kb().addUpdateListener(_listener);
		_thread = new Thread(this::work, "tl-ai-index " + _settings.root().getFileName());
		_thread.setDaemon(true);
		_thread.setPriority(Thread.MIN_PRIORITY);
		_thread.start();
	}

	/**
	 * Stops the worker after its current batch and closes the generations.
	 *
	 * <p>
	 * Work after the last commit is not committed: it is replayed from the history at the next
	 * start.
	 * </p>
	 */
	public void stop() {
		_running = false;
		_settings.kb().removeUpdateListener(_listener);
		wake();
		if (_thread != null) {
			try {
				_thread.join(120_000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			_thread = null;
			synchronized (_wake) {
				if (!_ended) {
					// Closing the generations under the worker would fail its batch; it closes them itself.
					_closeAtEnd = true;
					Logger.error("The index worker did not stop within two minutes. It closes the index when its"
						+ " current batch ends.", IndexSync.class);
					return;
				}
			}
		}
		closeGenerations();
	}

	private void closeGenerations() {
		_switch.writeLock().lock();
		try {
			close(_building);
			close(_serving);
			_building = null;
			_serving = null;
		} finally {
			_switch.writeLock().unlock();
		}
		synchronized (_progress) {
			_progress.notifyAll();
		}
	}

	private void openGenerations() throws IOException {
		List<Integer> numbers = _generations.numbers();
		Integer matching = null;
		Integer newestBuilt = null;
		Integer unbuiltMatching = null;
		for (int i = numbers.size() - 1; i >= 0; i--) {
			int number = numbers.get(i);
			Map<String, String> data = IndexGeneration.readCommitData(_generations.path(number));
			boolean built = data.containsKey(IndexGeneration.WATERMARK);
			boolean same = _settings.fingerprint().equals(data.get(IndexGeneration.FINGERPRINT))
				&& !data.containsKey(VECTORS_MISSING);
			if (built && same && matching == null) {
				matching = number;
			}
			if (built && newestBuilt == null) {
				newestBuilt = number;
			}
			if (!built && same && unbuiltMatching == null) {
				unbuiltMatching = number;
			}
		}
		Integer serving = matching != null ? matching : newestBuilt;
		Integer building;
		if (_settings.embedderUnavailable() && newestBuilt != null) {
			// Keep everything for the start at which the embedder is back.
			serving = newestBuilt;
			building = null;
		} else {
			building = matching != null ? null
				: unbuiltMatching != null ? unbuiltMatching : Integer.valueOf(_generations.next());
			for (int number : numbers) {
				if (!Integer.valueOf(number).equals(serving) && !Integer.valueOf(number).equals(building)) {
					_generations.delete(number);
				}
			}
		}
		if (serving != null) {
			_serving = new Gen(serving, IndexGeneration.open(_generations.path(serving)));
		}
		if (building != null) {
			_building = new Gen(building, IndexGeneration.open(_generations.path(building)));
			Logger.info("Building index generation " + building + " in " + _settings.root()
				+ (serving == null ? "." : ", generation " + serving + " serves meanwhile."), IndexSync.class);
		}
	}

	/**
	 * Runs a search on the serving generation.
	 */
	public <T> T withServing(ServingFunction<T> search) throws IOException {
		_switch.readLock().lock();
		try {
			Gen serving = _serving;
			if (serving == null) {
				return search.apply(null);
			}
			return search.apply(new Serving(serving.index(), vectors(serving), serving.watermark()));
		} finally {
			_switch.readLock().unlock();
		}
	}

	/**
	 * Whether the serving generation holds vectors of the configured embedder, at the moment of the
	 * call; {@link Serving#vectors()} is the answer for a search.
	 */
	public boolean servesVectors() {
		return vectors(_serving);
	}

	private boolean vectors(Gen gen) {
		return gen != null && _settings.embedder() != null
			&& _model.equals(gen.data().get(IndexGeneration.EMBEDDING_MODEL));
	}

	/**
	 * The revision up to which the serving generation holds every change; -1 before the first build.
	 */
	public long indexedRevision() {
		Gen serving = _serving;
		return serving == null ? -1 : serving.watermark();
	}

	/**
	 * Where the index stands.
	 */
	public IndexStatus status() {
		Gen serving = _serving;
		Gen building = _building;
		long head = Math.max(_head, _settings.kb().getHistoryManager().getLastRevision());
		long indexed = serving == null ? -1 : serving.watermark();
		long total = 0;
		long done = 0;
		if (building != null) {
			Map<String, String> data = building.data();
			total = Long.parseLong(data.getOrDefault(BUILD_TOTAL, "0"));
			done = building.built() ? total : Long.parseLong(data.getOrDefault(BUILD_DONE, "0"));
		}
		return new IndexStatus(serving != null, indexed, head, Math.max(0, head - indexed), building != null, total,
			done, vectors(serving), _lastError);
	}

	/**
	 * Waits until the serving generation holds every change up to the given revision.
	 *
	 * @return Whether it does.
	 */
	public boolean awaitRevision(long revision, long timeoutMillis) {
		long deadline = System.currentTimeMillis() + timeoutMillis;
		wake();
		synchronized (_progress) {
			while (indexedRevision() < revision) {
				long left = deadline - System.currentTimeMillis();
				if (left <= 0 || !_running) {
					return false;
				}
				try {
					_progress.wait(left);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
					return false;
				}
			}
			return true;
		}
	}

	private void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
		try {
			_head = Math.max(_head, event.getCommitNumber());
			wake();
		} catch (Throwable ex) {
			// A listener that throws fails the committer's call although the data is saved.
		}
	}

	private void wake() {
		synchronized (_wake) {
			_woken = true;
			_wake.notifyAll();
		}
	}

	private void work() {
		int failures = 0;
		while (_running) {
			boolean worked;
			try {
				worked = ThreadContextManager.inSystemInteraction(IndexSync.class, this::step);
				failures = 0;
				_lastError = null;
			} catch (Throwable ex) {
				failures++;
				_lastError = ex.getClass().getSimpleName() + ": " + ex.getMessage();
				Logger.error("The index worker failed, it tries again: " + _lastError, ex, IndexSync.class);
				backOff(Math.min(MAX_BACKOFF_MILLIS, 1_000L << Math.min(failures, 20)));
				continue;
			}
			if (!worked) {
				pause(IDLE_MILLIS);
			}
		}
		boolean close;
		synchronized (_wake) {
			_ended = true;
			close = _closeAtEnd;
		}
		if (close) {
			closeGenerations();
		}
	}

	private void pause(long millis) {
		synchronized (_wake) {
			if (!_woken && _running) {
				try {
					_wake.wait(millis);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
			_woken = false;
		}
	}

	/**
	 * Waits the given time after a failure: a commit does not end the wait, only a stop.
	 */
	private void backOff(long millis) {
		long deadline = System.currentTimeMillis() + millis;
		synchronized (_wake) {
			for (long left = millis; left > 0 && _running; left = deadline - System.currentTimeMillis()) {
				try {
					_wake.wait(left);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
					return;
				}
			}
			_woken = false;
		}
	}

	/**
	 * One step of work.
	 *
	 * @return Whether there was something to do.
	 */
	private boolean step() throws IOException, EmbeddingException {
		HistoryManager history = _settings.kb().getHistoryManager();
		history.updateSessionRevision();
		long head = history.getSessionRevision();
		_head = Math.max(_head, head);
		boolean worked = false;
		Gen serving = _serving;
		if (serving != null && serving.watermark() < head && _running) {
			worked = catchUp(serving, null, head);
		}
		Gen building = _building;
		if (building != null && _running) {
			if (!building.built()) {
				build(building, head);
				worked = true;
			} else {
				if (building.watermark() < head) {
					worked |= catchUp(building, reuseFor(building), head);
				}
				// In the same step: under steady commits the next step finds a newer head again.
				if (building.watermark() >= head) {
					switchTo(building);
					worked = true;
				}
			}
		}
		return worked;
	}

	private IndexGeneration reuseFor(Gen building) {
		Gen serving = _serving;
		if (serving == null || building.data().get(IndexGeneration.EMBEDDING_MODEL) == null) {
			return null;
		}
		return building.data().get(IndexGeneration.EMBEDDING_MODEL)
			.equals(serving.data().get(IndexGeneration.EMBEDDING_MODEL)) ? serving.index() : null;
	}

	/**
	 * Indexes the next slice of the history into the given generation.
	 *
	 * @return Whether the generation moved on.
	 */
	private boolean catchUp(Gen gen, IndexGeneration reuse, long head) throws IOException, EmbeddingException {
		long watermark = gen.watermark();
		HistoryManager history = _settings.kb().getHistoryManager();
		if (watermark + 1 < history.getFirstRevision()) {
			// The history before the first revision is gone; only a new build can recover.
			if (gen == _building) {
				gen.index().deleteAll();
				commit(gen, new HashMap<>());
				Logger.warn("The history of the index " + _settings.root() + " was compacted after revision "
					+ watermark + ", generation " + gen.number() + " is built again.", IndexSync.class);
				return true;
			}
			if (_building == null) {
				int number = _generations.next();
				_building = new Gen(number, IndexGeneration.open(_generations.path(number)));
				Logger.warn("The history of the index " + _settings.root() + " was compacted after revision "
					+ watermark + ", building generation " + number + ".", IndexSync.class);
				return true;
			}
			// The serving generation stays where it is until the build switches.
			return false;
		}
		long target = Math.min(head, watermark + _settings.revisionSlice());
		Set<ObjectKey> changed = _collector.changed(watermark, target);
		Map<String, String> data = new HashMap<>(gen.data());
		boolean ownModel = _model.equals(data.get(IndexGeneration.EMBEDDING_MODEL));
		if (!changed.isEmpty() && !ownModel) {
			// The serving generation of another model: its vectors must not be mixed with this one's.
			data.put(VECTORS_MISSING, "true");
		}
		indexAll(gen, ownModel ? _indexer : _wordsIndexer, reuse, new ArrayList<>(changed), head);
		data.put(IndexGeneration.WATERMARK, Long.toString(target));
		commit(gen, data);
		return true;
	}

	private void build(Gen gen, long head) throws IOException, EmbeddingException {
		Map<String, String> data = new HashMap<>(gen.data());
		Path keys = gen.index().path().resolve(BuildKeys.FILE);
		if (!data.containsKey(BUILD_REVISION) || !Files.exists(keys)) {
			gen.index().deleteAll();
			long total = BuildKeys.write(keys, _settings.types());
			data = new HashMap<>();
			data.put(IndexGeneration.FINGERPRINT, _settings.fingerprint());
			data.put(IndexGeneration.EMBEDDING_MODEL, _model);
			data.put(BUILD_REVISION, Long.toString(head));
			data.put(BUILD_OFFSET, "0");
			data.put(BUILD_TOTAL, Long.toString(total));
			data.put(BUILD_DONE, "0");
			commit(gen, data);
			return;
		}
		long offset = Long.parseLong(data.get(BUILD_OFFSET));
		long done = Long.parseLong(data.getOrDefault(BUILD_DONE, "0"));
		long deadline = System.currentTimeMillis() + _settings.buildMillis();
		IndexGeneration reuse = reuseFor(gen);
		boolean end = false;
		while (_running && !end && System.currentTimeMillis() < deadline) {
			BuildKeys.Batch batch = BuildKeys.read(keys, offset, _settings.batchSize());
			List<ObjectKey> objects = new ArrayList<>(batch.ids().size());
			for (String id : batch.ids()) {
				objects.add(ObjectKey.fromStringObjectKey(_settings.kb().getMORepository(), id));
			}
			indexBatch(_indexer, gen.index(), reuse, objects, head);
			offset = batch.next();
			done += batch.ids().size();
			end = batch.end();
		}
		if (end) {
			Map<String, String> built = new HashMap<>();
			built.put(IndexGeneration.FINGERPRINT, data.get(IndexGeneration.FINGERPRINT));
			built.put(IndexGeneration.EMBEDDING_MODEL, data.get(IndexGeneration.EMBEDDING_MODEL));
			built.put(IndexGeneration.WATERMARK, data.get(BUILD_REVISION));
			built.put(BUILD_TOTAL, data.get(BUILD_TOTAL));
			commit(gen, built);
			Files.deleteIfExists(keys);
		} else {
			data.put(BUILD_OFFSET, Long.toString(offset));
			data.put(BUILD_DONE, Long.toString(done));
			commit(gen, data);
		}
	}

	private void indexAll(Gen gen, ObjectIndexer indexer, IndexGeneration reuse, List<ObjectKey> keys,
			long revision) throws IOException, EmbeddingException {
		int size = _settings.batchSize();
		for (int start = 0; start < keys.size(); start += size) {
			indexBatch(indexer, gen.index(), reuse, keys.subList(start, Math.min(keys.size(), start + size)),
				revision);
		}
	}

	private void indexBatch(ObjectIndexer indexer, IndexGeneration target, IndexGeneration reuse,
			List<ObjectKey> keys, long revision) throws IOException, EmbeddingException {
		synchronized (this) {
			_largestBatch = Math.max(_largestBatch, keys.size());
		}
		indexer.index(target, reuse, keys, revision);
	}

	private void commit(Gen gen, Map<String, String> data) throws IOException {
		_beforeCommit.run();
		gen.index().commit(data);
		synchronized (_progress) {
			_progress.notifyAll();
		}
	}

	private void switchTo(Gen building) throws IOException {
		Gen old;
		_switch.writeLock().lock();
		try {
			old = _serving;
			_serving = building;
		} finally {
			_switch.writeLock().unlock();
		}
		Logger.info("Index generation " + building.number() + " in " + _settings.root() + " serves now.",
			IndexSync.class);
		if (old != null) {
			old.index().close();
			_generations.delete(old.number());
		}
		// Only now, so that whoever sees no build any more also sees the old generation gone.
		_building = null;
		synchronized (_progress) {
			_progress.notifyAll();
		}
	}

	private static void close(Gen gen) {
		if (gen == null) {
			return;
		}
		try {
			gen.index().close();
		} catch (IOException ex) {
			Logger.error("Cannot close index generation " + gen.number() + ".", ex, IndexSync.class);
		}
	}

	/**
	 * Test seam: runs before every commit of the worker; a hook that throws makes the worker fail
	 * between writing chunks and committing them, as a crash would.
	 */
	void setBeforeCommit(Runnable hook) {
		_beforeCommit = hook;
	}

	/**
	 * Test seam: the most objects the worker held in one batch so far.
	 */
	synchronized int largestBatch() {
		return _largestBatch;
	}

	/**
	 * Whether a generation is being built.
	 */
	public boolean building() {
		return _building != null;
	}

}
