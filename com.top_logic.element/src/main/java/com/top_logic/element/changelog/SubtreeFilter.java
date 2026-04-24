/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.element.changelog.model.Change;
import com.top_logic.element.changelog.model.Modification;
import com.top_logic.element.changelog.model.Update;
import com.top_logic.knowledge.objects.identifier.ObjectReference;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLChangeSubtree;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link ChangeFilter} that keeps only {@link Change}s affecting the change-subtree of a given
 * root object.
 *
 * <p>
 * A {@link Change} is accepted iff the affected object was, at the relevant revision, located in
 * the change-subtree spawned by the configured root. The check navigates reference ends that
 * qualify as root-side of a change-subtree relationship — either via the implicit qualification
 * of composition composite ends or via explicit {@link TLChangeSubtree @ChangeSubtree(true)}
 * annotations.
 * </p>
 *
 * <p>
 * Navigation is uniformly implemented via {@link TLObject#tReferers(TLReference)} on a historic
 * wrapper, independent of whether the annotated end is the forward or back reference of the
 * underlying pair.
 * </p>
 *
 * <p>
 * For {@link Update}s that touch an annotated reference (or its opposite), the filter performs a
 * dual-state check (at {@code r} and {@code r-1}) so that move-in and move-out boundary events
 * are visible in both the old and the new subtree. Creations use single-check at {@code r};
 * deletions use the pre-deletion wrapper (already scoped to {@code r-1}); non-reference updates
 * use single-check at {@code r}.
 * </p>
 *
 * <p>
 * Results are memoized per {@code (revision, unversionedId)} pair for the lifetime of this
 * filter instance. Cross-walk caching is correct: only identities proven to be in the subtree
 * are cached as {@code true}, and only start identities exhausted without reaching the root are
 * cached as {@code false}.
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
	 * Per-revision cache: <code>(commit number) -&gt; (unversioned id) -&gt; in-subtree</code>.
	 */
	private final Map<Long, Map<ObjectReference, Boolean>> _cache = new HashMap<>();

	/**
	 * Maps a type to all annotated reference ends whose target type is a supertype of it. Lazily
	 * built on first use together with {@link #_reachable} and {@link #_allAnnotatedEnds}.
	 */
	private Map<TLType, List<TLReference>> _annotatedEndsByTarget;

	/**
	 * Set of types forward-reachable from the root type via qualified edges. Used indirectly
	 * through {@link #_annotatedEndsByTarget} (unreachable types have no entry).
	 */
	private Set<TLType> _reachable;

	/**
	 * All qualifying reference ends discovered in the reachable closure. Used to decide whether
	 * an {@link Update} touches an annotated reference (triggering the dual-state check).
	 */
	private Set<TLReference> _allAnnotatedEnds;

	/**
	 * Creates a {@link SubtreeFilter} that accepts the root and the entire change-subtree.
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
	 *        If {@code true}, accepts every change to an object in the change-subtree of
	 *        {@code subtreeRoot}. If {@code false}, only changes to {@code subtreeRoot} itself
	 *        are accepted.
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
		ensureIndex();

		long revision = obj.tId().getHistoryContext();
		if (inSubtree(obj, revision)) {
			return true;
		}

		// Dual-state check for reference updates: also evaluate at r-1.
		if (change instanceof Update update && touchesAnnotatedReference(update)) {
			TLObject oldObj = update.getOldObject();
			if (oldObj != null) {
				long prev = oldObj.tId().getHistoryContext();
				return inSubtree(oldObj, prev);
			}
		}
		return false;
	}

	private boolean touchesAnnotatedReference(Update update) {
		for (Modification m : update.getModifications()) {
			TLStructuredTypePart part = m.getPart();
			if (!(part instanceof TLReference)) {
				continue;
			}
			TLReference ref = (TLReference) part;
			if (_allAnnotatedEnds.contains(ref)) {
				return true;
			}
			TLReference opp = ref.getOpposite();
			if (opp != null && _allAnnotatedEnds.contains(opp)) {
				return true;
			}
		}
		return false;
	}

	private boolean inSubtree(TLObject start, long revision) {
		Map<ObjectReference, Boolean> cache = _cache.computeIfAbsent(revision, x -> new HashMap<>());
		ObjectReference startId = WrapperHistoryUtils.getUnversionedIdentity(start);

		if (startId.equals(_rootIdentity)) {
			return true;
		}
		Boolean cached = cache.get(startId);
		if (cached != null) {
			return cached;
		}

		List<ObjectReference> successPath = new ArrayList<>();
		Set<ObjectReference> visited = new HashSet<>();

		boolean found = walk(start, cache, visited, successPath);

		if (found) {
			for (ObjectReference id : successPath) {
				cache.put(id, Boolean.TRUE);
			}
		} else {
			cache.put(startId, Boolean.FALSE);
		}
		return found;
	}

	private boolean walk(TLObject cur, Map<ObjectReference, Boolean> cache, Set<ObjectReference> visited,
			List<ObjectReference> successPath) {
		ObjectReference id = WrapperHistoryUtils.getUnversionedIdentity(cur);
		if (id.equals(_rootIdentity)) {
			successPath.add(id);
			return true;
		}
		Boolean cached = cache.get(id);
		if (cached != null) {
			if (cached) {
				successPath.add(id);
				return true;
			}
			return false;
		}
		if (!visited.add(id)) {
			return false;
		}

		TLType type = cur.tType();
		if (type == null) {
			return false;
		}
		List<TLReference> ends = _annotatedEndsByTarget.get(type);
		if (ends == null) {
			return false;
		}
		for (TLReference ref : ends) {
			Set<? extends TLObject> owners;
			try {
				owners = cur.tReferers(ref);
			} catch (RuntimeException ex) {
				// Some storages may not support referers lookup on all wrappers (e.g. historic
				// edge cases). Skip this edge gracefully rather than aborting the walk.
				continue;
			}
			if (owners == null || owners.isEmpty()) {
				continue;
			}
			for (TLObject owner : owners) {
				if (owner == null) {
					continue;
				}
				if (walk(owner, cache, visited, successPath)) {
					successPath.add(id);
					return true;
				}
			}
		}
		return false;
	}

	private void ensureIndex() {
		if (_reachable != null) {
			return;
		}
		_reachable = new HashSet<>();
		_annotatedEndsByTarget = new HashMap<>();
		_allAnnotatedEnds = new HashSet<>();

		TLType rootType = _root.tType();
		if (rootType == null) {
			return;
		}

		Deque<TLType> worklist = new ArrayDeque<>();
		seed(rootType, worklist);
		Set<TLReference> processedRefs = new HashSet<>();

		while (!worklist.isEmpty()) {
			TLType owner = worklist.pollFirst();
			if (!(owner instanceof TLClass)) {
				continue;
			}
			TLClass ownerClass = (TLClass) owner;
			for (TLReference ref : TLModelUtil.getAllReferences(ownerClass)) {
				if (!processedRefs.add(ref)) {
					continue;
				}
				if (!qualifies(ref)) {
					continue;
				}
				_allAnnotatedEnds.add(ref);
				TLType target = ref.getType();
				if (!(target instanceof TLClass)) {
					continue;
				}
				TLClass targetClass = (TLClass) target;
				for (TLClass sub : TLModelUtil.getReflexiveTransitiveSpecializations(targetClass)) {
					_annotatedEndsByTarget.computeIfAbsent(sub, k -> new ArrayList<>()).add(ref);
					if (_reachable.add(sub)) {
						worklist.offerLast(sub);
					}
				}
			}
		}
	}

	private void seed(TLType rootType, Deque<TLType> worklist) {
		if (rootType instanceof TLClass) {
			for (TLClass sub : TLModelUtil.getReflexiveTransitiveSpecializations((TLClass) rootType)) {
				if (_reachable.add(sub)) {
					worklist.offerLast(sub);
				}
			}
		} else if (_reachable.add(rootType)) {
			worklist.offerLast(rootType);
		}
	}

	/**
	 * Whether the given reference end qualifies as the root side of a change-subtree
	 * relationship.
	 *
	 * <p>
	 * A reference end qualifies iff (a) it carries an explicit {@code @ChangeSubtree(true)}
	 * annotation, or (b) it is the composite end of a composition AND no end of the pair carries
	 * an explicit {@code @ChangeSubtree(false)}. An explicit {@code false} on either end
	 * disables the qualification entirely.
	 * </p>
	 */
	private static boolean qualifies(TLReference ref) {
		TLChangeSubtree own = ref.getAnnotation(TLChangeSubtree.class);
		TLReference opposite = ref.getOpposite();
		TLChangeSubtree opp = (opposite != null) ? opposite.getAnnotation(TLChangeSubtree.class) : null;

		if (own != null && !own.getInclude()) {
			return false;
		}
		if (opp != null && !opp.getInclude()) {
			return false;
		}
		if (own != null) {
			return true;
		}
		return ref.isComposite();
	}

}
