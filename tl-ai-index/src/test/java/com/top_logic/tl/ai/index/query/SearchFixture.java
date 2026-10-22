/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.query;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.index.AiIndex;
import com.top_logic.tl.ai.index.AiIndexConfig;
import com.top_logic.tl.ai.index.config.IndexTestModel;
import com.top_logic.tl.ai.index.embed.HashingEmbedder;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * An index over the test model, rights that a test sets object by object, and searches on a thread
 * of their own in a user's interaction - the way a request of that user runs.
 */
public final class SearchFixture {

	private SearchFixture() {
		// Static utility.
	}

	/**
	 * Starts an index with the given configuration in the given directory and waits until it holds
	 * every commit so far.
	 */
	public static AiIndex start(String xml, Path dir) throws IOException {
		AiIndex index = new AiIndex(IndexTestModel.read(AiIndexConfig.class, "index", xml), HashingEmbedder.create(64),
			dir, PersistencyLayer.getKnowledgeBase());
		index.start();
		await(index);
		return index;
	}

	/**
	 * Waits until the index holds every commit so far.
	 */
	public static void await(AiIndex index) {
		long last = PersistencyLayer.getKnowledgeBase().getHistoryManager().getLastRevision();
		if (!index.awaitRevision(last, 60_000)) {
			throw new AssertionError("The index did not catch up with revision " + last + ".");
		}
	}

	/**
	 * Runs the given computation on a new thread in an interaction of the given person.
	 */
	public static <T> T asUser(Person person, Callable<T> computation) {
		AtomicReference<T> result = new AtomicReference<>();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread thread = new Thread(() -> TLContextManager.inPersonContext(person, () -> {
			try {
				result.set(computation.call());
			} catch (Throwable ex) {
				failure.set(ex);
			}
		}));
		thread.start();
		try {
			thread.join(60_000);
		} catch (InterruptedException ex) {
			throw new AssertionError(ex);
		}
		if (failure.get() instanceof RuntimeException ex) {
			throw ex;
		}
		if (failure.get() != null) {
			throw new AssertionError(failure.get());
		}
		return result.get();
	}

	/**
	 * The ids of the hits, in rank order.
	 */
	public static List<String> ids(Hits hits) {
		List<String> result = new ArrayList<>();
		for (Hit hit : hits.hits()) {
			result.add(hit.id());
		}
		return result;
	}

	/**
	 * Rights that grant reading exactly the given objects, and none of the hidden attributes.
	 */
	public static final class Rights implements ModelSecurity {

		private final Set<TLObject> _readable = new HashSet<>();

		private final Set<String> _hiddenAttributes = new HashSet<>();

		/**
		 * Grants reading the given objects.
		 */
		public Rights read(Collection<? extends TLObject> objects) {
			_readable.addAll(objects);
			return this;
		}

		/**
		 * Hides the attribute of the given name on every object.
		 */
		public Rights hide(String attribute) {
			_hiddenAttributes.add(attribute);
			return this;
		}

		@Override
		public boolean canRead(TLObject obj) {
			return _readable.contains(obj);
		}

		@Override
		public boolean canRead(TLObject obj, TLStructuredTypePart attribute) {
			return canRead(obj) && !_hiddenAttributes.contains(attribute.getName());
		}

		@Override
		public boolean canWrite(TLObject obj) {
			return false;
		}

		@Override
		public boolean canWrite(TLObject obj, TLStructuredTypePart attribute) {
			return false;
		}

		@Override
		public boolean canDelete(TLObject obj) {
			return false;
		}

		@Override
		public boolean canCreate(TLClass type, TLObject context) {
			return false;
		}

		@Override
		public boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute, TLClass type) {
			return false;
		}

		@Override
		public boolean canSetInitial(TLObject draft, TLStructuredTypePart attribute) {
			return false;
		}

		@Override
		public boolean canPerform(TLObject obj, BoundCommandGroup operation) {
			return false;
		}

		@Override
		public <T extends TLObject> List<T> filterReadable(Collection<T> objects) {
			List<T> result = new ArrayList<>();
			for (T object : objects) {
				if (canRead(object)) {
					result.add(object);
				}
			}
			return result;
		}

		@Override
		public Set<TLClass> accessibleTypes(BoundCommandGroup operation) {
			return TLModelUtil.getAllGlobalClasses(ModelService.getApplicationModel());
		}
	}

}
