/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.editor;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.basic.col.GCQueue;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.FileSystemCache;
import com.top_logic.basic.io.PathUpdate;

/**
 * {@link FileSystemCache} for tests that publishes an empty {@link PathUpdate} on each
 * {@link #fetchUpdates()} and counts how many of these updates have been consumed.
 * 
 * <p>
 * An update counts as consumed when its {@link PathUpdate#getDeletions()} is requested. This cache
 * does no caching of file system resources at all, see {@link #isCaching()}.
 * </p>
 */
public class CountingFileSystemCache extends FileSystemCache {

	private final GCQueue<PathUpdate> _updates = new GCQueue<>();

	private final AtomicInteger _published = new AtomicInteger();

	private final AtomicInteger _consumed = new AtomicInteger();

	/**
	 * Creates a {@link CountingFileSystemCache} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public CountingFileSystemCache(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public boolean isCaching() {
		return false;
	}

	@Override
	public List<Path> pathOverlays(String resourceName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path resolveFile(String resourceName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<PathUpdate> getUpdates() {
		return _updates.iterator();
	}

	@Override
	public void fetchUpdates() {
		_published.incrementAndGet();
		_updates.add(new CountingUpdate());
	}

	/**
	 * The number of updates published by {@link #fetchUpdates()}.
	 */
	public int getPublished() {
		return _published.get();
	}

	/**
	 * The number of published updates that have been consumed.
	 */
	public int getConsumed() {
		return _consumed.get();
	}

	/**
	 * Resets {@link #getPublished()} and {@link #getConsumed()} to zero.
	 */
	public void resetCounters() {
		_published.set(0);
		_consumed.set(0);
	}

	private class CountingUpdate extends PathUpdate {

		CountingUpdate() {
			super(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
		}

		@Override
		public Collection<Path> getDeletions() {
			_consumed.incrementAndGet();
			return super.getDeletions();
		}
	}

}
