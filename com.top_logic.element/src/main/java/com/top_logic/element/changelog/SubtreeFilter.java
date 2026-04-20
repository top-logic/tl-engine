/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.element.changelog.model.Change;
import com.top_logic.knowledge.objects.identifier.ObjectReference;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLObject;

/**
 * {@link ChangeFilter} that keeps only {@link Change}s affecting the composition subtree of a
 * given root object.
 *
 * <p>
 * A {@link Change} is accepted iff the affected object was, at the time of its revision, located
 * in the composition subtree spawned by the configured root. The check uses the historic
 * {@link TLObject} carried by the {@link Change} and walks {@link TLObject#tContainer()} upwards
 * until either the root identity is reached (accepted) or no further container exists (rejected).
 * </p>
 *
 * <p>
 * Because the check is performed against a historic wrapper, the result is automatically scoped to
 * the revision in which the change was committed. Object moves are therefore handled correctly:
 * the move event itself is the last change relevant to the previous container; subsequent
 * modifications of the moved object are no longer considered to affect the previous subtree.
 * </p>
 *
 * <p>
 * Results are memoized per <code>(unversioned object identity, revision)</code>. Container path
 * prefixes are shared between sibling objects, so the amortized cost per change is close to
 * <code>O(1)</code> within a session.
 * </p>
 *
 * <p>
 * Instances are not thread-safe.
 * </p>
 */
public class SubtreeFilter implements ChangeFilter {

	private final TLObject _root;

	private final ObjectReference _rootIdentity;

	private final Revision _rootCreation;

	private final boolean _includeSubtree;

	/**
	 * Per-revision cache: <code>(revision commit number) -&gt; (unversioned id) -&gt; in subtree</code>.
	 *
	 * <p>
	 * The two-level structure allows a fast revision-keyed lookup; entries from different
	 * revisions can never be confused because container assignments are revision-specific.
	 * </p>
	 */
	private final Map<Long, Map<ObjectReference, Boolean>> _cache = new HashMap<>();

	/**
	 * Creates a {@link SubtreeFilter} that accepts the root and the entire composition subtree.
	 *
	 * @param subtreeRoot
	 *        The root object {@code R}. Must not be {@code null}.
	 */
	public SubtreeFilter(TLObject subtreeRoot) {
		this(subtreeRoot, true);
	}

	/**
	 * Creates a {@link SubtreeFilter}.
	 *
	 * @param subtreeRoot
	 *        The root object {@code R}. Must not be {@code null}.
	 * @param includeSubtree
	 *        If {@code true}, accepts every change to an object in the composition subtree of
	 *        {@code subtreeRoot}. If {@code false}, only changes to {@code subtreeRoot} itself are
	 *        accepted; descendants are ignored.
	 */
	public SubtreeFilter(TLObject subtreeRoot, boolean includeSubtree) {
		_root = subtreeRoot;
		_rootIdentity = WrapperHistoryUtils.getUnversionedIdentity(subtreeRoot);
		_rootCreation = WrapperHistoryUtils.getCreateRevision(subtreeRoot);
		_includeSubtree = includeSubtree;
	}

	/**
	 * The root object this filter is configured for.
	 */
	public TLObject getRoot() {
		return _root;
	}

	@Override
	public Revision adjustStartRev(Revision startRev) {
		if (_rootCreation == null) {
			return startRev;
		}
		if (startRev.getCommitNumber() >= _rootCreation.getCommitNumber()) {
			return startRev;
		}
		return _rootCreation;
	}

	@Override
	public boolean accept(Change change) {
		TLObject obj = change.getObject();
		if (obj == null) {
			return false;
		}
		if (!_includeSubtree) {
			return WrapperHistoryUtils.getUnversionedIdentity(obj).equals(_rootIdentity);
		}
		long revision = obj.tId().getHistoryContext();
		return inSubtree(obj, revision);
	}

	private boolean inSubtree(TLObject obj, long revision) {
		Map<ObjectReference, Boolean> revisionCache =
			_cache.computeIfAbsent(revision, x -> new HashMap<>());

		// Walk up the container chain, collecting visited identities to memoize the whole path
		// once the answer is known.
		List<ObjectReference> path = null;
		TLObject current = obj;
		while (current != null) {
			ObjectReference id = WrapperHistoryUtils.getUnversionedIdentity(current);

			Boolean cached = revisionCache.get(id);
			if (cached != null) {
				memoize(revisionCache, path, cached);
				return cached;
			}

			if (id.equals(_rootIdentity)) {
				memoize(revisionCache, path, Boolean.TRUE);
				revisionCache.put(id, Boolean.TRUE);
				return true;
			}

			if (path == null) {
				path = new ArrayList<>();
			}
			path.add(id);

			current = current.tContainer();
		}

		memoize(revisionCache, path, Boolean.FALSE);
		return false;
	}

	private static void memoize(Map<ObjectReference, Boolean> revisionCache, List<ObjectReference> path,
			Boolean result) {
		if (path == null) {
			return;
		}
		for (ObjectReference id : path) {
			revisionCache.put(id, result);
		}
	}

}
