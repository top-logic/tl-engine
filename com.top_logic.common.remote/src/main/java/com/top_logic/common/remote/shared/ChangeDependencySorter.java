/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.list;
import static java.util.Collections.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.common.remote.json.Ref;
import com.top_logic.common.remote.update.Change;
import com.top_logic.common.remote.update.Changes;
import com.top_logic.common.remote.update.Create;
import com.top_logic.common.remote.update.Delete;
import com.top_logic.common.remote.update.Update;

/**
 * Sorts the given {@link Changes} to allow them to be executed without further reordering.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ChangeDependencySorter {

	private final Map<String, Create> _createsById;

	private final Map<String, Update> _updatesById;

	private final Collection<Delete> _deletes;

	private final Map<String, Set<String>> _directDependencies;

	/**
	 * Creates a {@link ChangeDependencySorter}.
	 * 
	 * @param changes
	 *        Is not allowed to be null.
	 */
	private ChangeDependencySorter(Changes changes) {
		_createsById = getChangesById(changes.getCreates());
		_updatesById = getChangesById(changes.getUpdates());
		_deletes = changes.getDeletes();
		_directDependencies = calcDirectDependencies(changes);
	}

	private Map<String, Set<String>> calcDirectDependencies(Changes changes) {
		Map<String, Set<String>> directDependenciesById = map();

		for (Update update : changes.getUpdates()) {
			directDependenciesById.put(update.getId(), extractDirectDependencies(update));
		}

		return directDependenciesById;
	}

	/**
	 * Sorts the given {@link Changes} to allow them to be executed without further reordering.
	 * 
	 * @param changes
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static List<Change> sort(Changes changes) {
		return new ChangeDependencySorter(changes).sort();
	}

	private <T extends Change> Map<String, T> getChangesById(Collection<T> changes) {
		Map<String, T> updatesById = map();
		for (T update : changes) {
			updatesById.put(update.getId(), update);
		}
		return updatesById;
	}

	private Set<String> extractDirectDependencies(Update update) {
		Set<String> dependencies = set();
		for (Object value : update.getValues().values()) {
			if (value instanceof Ref) {
				addDependencies((Ref) value, dependencies);
			} else if (value instanceof Collection) {
				addDependencies((Collection<?>) value, dependencies);
			}
		}
		return dependencies;
	}

	private void addDependencies(Collection<?> values, Set<String> dependencies) {
		for (Object innerValue : values) {
			if (innerValue instanceof Ref) {
				addDependencies((Ref) innerValue, dependencies);
			}
		}
	}

	private void addDependencies(Ref ref, Set<String> dependencies) {
		String id = ref.id();

		if (_createsById.containsKey(id)) {
			dependencies.add(id);
		}
	}

	private List<Change> sort() {
		List<Change> result = list();

		result.addAll(getSortedCreationChanges());
		result.addAll(getSortedUpdateChanges());
		result.addAll(_deletes);

		return result;
	}

	private List<Change> getSortedUpdateChanges() {
		return getSortedNotInitializerUpdateIds().stream()
			.map(id -> _updatesById.get(id))
			.filter(updateChange -> updateChange != null)
			.collect(Collectors.toList());
	}

	private List<Change> getSortedCreationChanges() {
		return getSortedCreationIds().stream()
			.flatMap(id -> Arrays.asList(_createsById.get(id), _updatesById.get(id)).stream())
			.filter(change -> change != null)
			.collect(Collectors.toList());
	}

	private List<String> getSortedNotInitializerUpdateIds() {
		return CollectionUtilShared.topsort(this::getDependencies, getNotInitializerUpdateIds(), false);
	}

	private List<String> getSortedCreationIds() {
		return CollectionUtilShared.topsort(this::getDependencies, _createsById.keySet(), false);
	}

	private HashSet<String> getNotInitializerUpdateIds() {
		HashSet<String> updateIds = new HashSet<>(_updatesById.keySet());

		updateIds.removeAll(_createsById.keySet());

		return updateIds;
	}

	private Set<String> getDependencies(String id) {
		return _directDependencies.getOrDefault(id, emptySet());
	}
}
