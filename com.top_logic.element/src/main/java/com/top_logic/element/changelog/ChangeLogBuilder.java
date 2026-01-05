/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.LongID;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.col.LongRangeSet;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.element.changelog.model.Change;
import com.top_logic.element.changelog.model.trans.TransientChangeSet;
import com.top_logic.element.changelog.model.trans.TransientCreation;
import com.top_logic.element.changelog.model.trans.TransientDeletion;
import com.top_logic.element.changelog.model.trans.TransientModification;
import com.top_logic.element.changelog.model.trans.TransientUpdate;
import com.top_logic.element.meta.AssociationStorageDescriptor;
import com.top_logic.element.model.cache.ElementModelCacheService;
import com.top_logic.element.model.cache.ModelTables;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.RevisionType;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.util.model.ModelService;

/**
 * Algorithm to analyze technical changes reported by a {@link KnowledgeBase} an build a model
 * change log.
 */
public class ChangeLogBuilder {

	private final KnowledgeBase _kb;

	private final HistoryManager _hm;

	private final TLModel _model;

	private int _numberEntries;

	private Revision _startRev;

	private Revision _stopRev;

	private Person _author;

	private boolean _includeTechnical;

	private ModelTables _modelTables;

	private Set<String> _excludeModules = Collections.emptySet();

	/**
	 * Creates a {@link ChangeLogBuilder}.
	 */
	public ChangeLogBuilder(KnowledgeBase kb, TLModel model) {
		_kb = kb;
		_model = model;
		_hm = kb.getHistoryManager();

		_startRev = _hm.getRevision(1);
		_stopRev = toRevision(_hm.getLastRevision());
	}

	private Revision toRevision(long commitNumber) {
		return _hm.getRevision(commitNumber);
	}

	/**
	 * The first {@link Revision} to analyze.
	 */
	public Revision getStartRev() {
		return _startRev;
	}

	/**
	 * The last {@link Revision} to analyze.
	 */
	public Revision getStopRev() {
		return _stopRev;
	}

	/**
	 * @see #getStartRev()
	 */
	public ChangeLogBuilder setStartRev(Revision startRev) {
		_startRev = startRev;
		return this;
	}

	/**
	 * @see #getStopRev()
	 */
	public ChangeLogBuilder setStopRev(Revision stopRev) {
		_stopRev = stopRev;
		return this;
	}

	/**
	 * The account for which to produce the change log.
	 * 
	 * <p>
	 * <code>null</code> for a system-wide change log.
	 * </p>
	 */
	public Person getAuthor() {
		return _author;
	}

	/**
	 * @see #getAuthor()
	 */
	public ChangeLogBuilder setAuthor(Person author) {
		_author = author;
		return this;
	}

	/**
	 * Whether to also include change sets that represent a technical change that does not reflect
	 * in the model.
	 */
	public boolean getIncludeTechnical() {
		return _includeTechnical;
	}

	/**
	 * @see #getIncludeTechnical()
	 */
	public ChangeLogBuilder setIncludeTechnical(boolean includeTechnical) {
		_includeTechnical = includeTechnical;
		return this;
	}

	/**
	 * Names of {@link TLModule}s that must not be regarded.
	 */
	public Set<String> getExcludedModules() {
		return _excludeModules;
	}

	/**
	 * @see #getExcludedModules()
	 */
	public ChangeLogBuilder setExcludedModules(Set<String> excluded) {
		_excludeModules = excluded;
		return this;
	}

	/**
	 * Maximal number of entries to display.
	 */
	public int getNumberEntries() {
		return _numberEntries;
	}

	/**
	 * @see #getNumberEntries()
	 */
	public ChangeLogBuilder setNumberEntries(int value) {
		_numberEntries = value;
		return this;
	}

	/**
	 * Retrieves the change sets.
	 */
	public Collection<com.top_logic.element.changelog.model.ChangeSet> build() {
		// all log messages. Sorted in descending order
		List<com.top_logic.element.changelog.model.ChangeSet> log = new ArrayList<>();

		if (_excludeModules.isEmpty() && _model == ModelService.getApplicationModel()) {
			_modelTables = ElementModelCacheService.getModelTables();
		} else {
			_modelTables = new ModelTables(_model, this::isExcludedModule);
		}

		Map<Long, com.top_logic.element.changelog.model.ChangeSet> revertedBy = new HashMap<>();

		List<LongRange> revisionRanges = getRevisionRanges();
		processRevisions:
		for (int i = revisionRanges.size() - 1; i >= 0; i--) {
			LongRange range = revisionRanges.get(i);

			long start = range.getStartValue();
			long stop = range.getEndValue();

			if (limitEntryCount()) {
				while (true) {
					/* Fetch a little bit more revisions than required because there may be
					 * additional empty or technical changes, which are not reported. */
					long maxFetchEntries = (long) ((_numberEntries - log.size()) * 1.5);

					long chunkStart = Long.max(start, stop - maxFetchEntries);
					readDescending(log, revertedBy, chunkStart, stop);

					int remaining = _numberEntries - log.size();
					if (remaining == 0) {
						// log list has correct size
						break processRevisions;
					}
					if (remaining < 0) {
						// log list is to long, skip oldest entries
						log.subList(_numberEntries, log.size()).clear();
						break processRevisions;
					}

					if (chunkStart == start) {
						break;
					}
					stop = chunkStart - 1;
				}
			} else {
				readDescending(log, revertedBy, start, stop);
			}

		}

		// Return entries ascending
		Collections.reverse(log);
		return log;
	}

	private void readDescending(List<com.top_logic.element.changelog.model.ChangeSet> log,
			Map<Long, com.top_logic.element.changelog.model.ChangeSet> revertedBy, long start,
			long stop) {

		List<com.top_logic.element.changelog.model.ChangeSet> toDelete = new ArrayList<>();

		List<com.top_logic.element.changelog.model.ChangeSet> logsInRange =
			readChangesDescending(start, stop);
		
		for (Iterator<com.top_logic.element.changelog.model.ChangeSet> it = logsInRange.iterator(); it
			.hasNext();) {
			com.top_logic.element.changelog.model.ChangeSet cs1 = it.next();
			long commitNumber = cs1.getRevision().getCommitNumber();
		
			com.top_logic.element.changelog.model.ChangeSet undoCS = revertedBy.remove(commitNumber);
			if (undoCS != null) {
				// Connect CS with its undo CS.
				cs1.setRevertedBy(undoCS);
				// Display message "Reverted: ..."
				cs1.setMessage(I18NConstants.REVERTED__MSG.fill(cs1.getMessage()));
				// Undo CS is not displayed
				toDelete.add(undoCS);
				continue;
			}
		
			if (cs1.isRevert()) {
				long revertedRevision = cs1.origRevision();
				if (revertedRevision != -1) {
					// store cs for later connection with the undone CS.
					revertedBy.put(revertedRevision, cs1);
				}
			}
		}
		
		log.addAll(logsInRange);

		Comparator<com.top_logic.element.changelog.model.ChangeSet> revisionOrder = Comparator
			.comparing(com.top_logic.element.changelog.model.ChangeSet::getRevision);
		// Ensure ascending revision order, log is sorted descending
		toDelete.sort(revisionOrder);

		Comparator<com.top_logic.element.changelog.model.ChangeSet> reversedRevisionOrder = revisionOrder.reversed();
		List<com.top_logic.element.changelog.model.ChangeSet> searchList = log;
		for (com.top_logic.element.changelog.model.ChangeSet cs : toDelete) {
			int idx = Collections.binarySearch(searchList, cs, reversedRevisionOrder);
			if (idx < 0) {
				assert false : "toDelete is a sublist of log.";
			} else {
				searchList.remove(idx);
				// All later CS in toDelete have larger commit number, i.e. before idx in log list
				searchList = searchList.subList(0, idx);
			}
		}
	}

	private List<com.top_logic.element.changelog.model.ChangeSet> readChangesDescending(
			long start, long stop) {
		List<com.top_logic.element.changelog.model.ChangeSet> out = new ArrayList<>();
		readChanges(out, start, stop);
		// entries are filled in ascending order to output
		Collections.reverse(out);
		return out;
	}

	private void readChanges(List<com.top_logic.element.changelog.model.ChangeSet> out, long start, long stop) {

		TLID authorIdFilter = _author == null ? null : _author.tIdLocal();
		Branch branch = _hm.getTrunk();

		/* Note: Use the revision before the actual "start" commit number as start revision, because
		 * the diff reader creates events to come from given startRev to given stopRev, i.e. no
		 * event for the given startRev is created! */
		Revision startRev = toRevision(Math.max(start - 1, 1));
		Revision stopRev = toRevision(stop);

		try (ChangeSetReader reader = _kb.getDiffReader(startRev, branch, stopRev, branch, true)) {
			while (true) {
				ChangeSet changeSet = reader.read();
				if (changeSet == null) {
					break;
				}

				Revision revision = _hm.getRevision(changeSet.getRevision());
				Person author = resolveAuthor(revision);
				if (authorIdFilter != null && (author == null || !authorIdFilter.equals(author.tIdLocal()))) {
					// Filter foreign changes.
					continue;
				}

				ChangeSetAnalyzer analyzer = new ChangeSetAnalyzer(changeSet);
				if (!_includeTechnical && !analyzer.hasChanges()) {
					continue;
				}

				TransientChangeSet entry =
					new TransientChangeSet(analyzer::applyChanges, TransientChangeSet.CHANGES_ATTR);
				entry.setDate(new Date(revision.getDate()));
				entry.setRevision(revision);
				entry.setParentRev(_hm.getRevision(changeSet.getRevision() - 1));
				entry.setMessage(revision.getLog());
				entry.setAuthor(author);


				out.add(entry);
			}
		}
	}

	private List<LongRange> getRevisionRanges() {
		long startRev = _startRev.getCommitNumber();
		long stopRev = _stopRev.getCommitNumber();
		if (_author == null) {
			return LongRangeSet.range(startRev, stopRev);
		}

		MOClass revisionType = BasicTypes.getRevisionType(_kb);
		MOAttribute revAttribute = revisionType.getAttribute(BasicTypes.REVISION_REV_ATTRIBUTE);
		String revDBName = revAttribute.getDbMapping()[0].getDBName();
		MOAttribute authorAttribute = revisionType.getAttribute(RevisionType.AUTHOR_ATTRIBUTE_NAME);
		String authorDBName = authorAttribute.getDbMapping()[0].getDBName();

		SQLSelect select = selectDistinct(
			columns(columnDef(column(revDBName), revDBName)),
			table(revisionType.getDBMapping().getDBName()),
			and(
				le(parameter(DBType.LONG, "startRev"), column(revDBName)),
				ge(parameter(DBType.LONG, "stopRev"), column(revDBName)),
				eqSQL(parameter(DBType.STRING, "author"), column(authorDBName))),
			orders(order(false, column(revDBName))));
		try {
			ConnectionPool pool = KBUtils.getConnectionPool(_kb);
			CompiledStatement sql = query(
				parameters(
					parameterDef(DBType.LONG, "startRev"),
					parameterDef(DBType.LONG, "stopRev"),
					parameterDef(DBType.STRING, "author")),
				select)
					.toSql(pool.getSQLDialect());
			PooledConnection con = pool.borrowReadConnection();
			try {
				ResultSet res = sql.executeQuery(con, startRev, stopRev, TLSessionContext.contextId(_author));
				List<LongRange> result = LongRangeSet.EMPTY_SET;
				while (res.next()) {
					long rev = res.getLong(revDBName);
					result = LongRangeSet.union(result, LongRangeSet.range(rev, rev));
				}
				return result;
			} finally {
				pool.releaseReadConnection(con);
			}
		} catch (SQLException ex) {
			Logger.error("Error determining start revision based on max entry count.", ex, ChangeLogBuilder.class);
			return LongRangeSet.range(startRev, stopRev);
		}
	}

	private boolean limitEntryCount() {
		return _numberEntries > 0;
	}

	/**
	 * Internal state of the analysis of a {@link ChangeSetAnalyzer}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static enum AnalyseState {

		/** Not analysed yet. */
		NO,

		/**
		 * Partially analysed: {@link ChangeSetAnalyzer#hasChanges()} answers with the correct
		 * value.
		 */
		PARTIAL,

		/**
		 * Fully analysed:
		 * {@link ChangeSetAnalyzer#applyChanges(com.top_logic.element.changelog.model.ChangeSet)}
		 * can be applied.
		 */
		FULL;
	}

	private class ChangeSetAnalyzer {

		private final ChangeSet _changeSet;

		/** Mapping from an object to the parts that were updated in {@link #_changeSet}. */
		private final Map<TLObject, Set<TLStructuredTypePart>> _updates = new HashMap<>();

		/**
		 * {@link Change}s to apply to a {@link com.top_logic.element.changelog.model.ChangeSet}.
		 */
		private final List<Change> _changes = new ArrayList<>();

		/**
		 * Adaptions for {@link #_changes} to execute before they are applied to a
		 * {@link com.top_logic.element.changelog.model.ChangeSet}.
		 */
		private final List<Runnable> _adaptions = new ArrayList<>();

		private final Iterator<ObjectCreation> _remainingCreations;

		private final Iterator<ItemUpdate> _remainingUpdates;

		private final Iterator<ItemDeletion> _remainingDeletions;

		private AnalyseState _state = AnalyseState.NO;

		/**
		 * Creates a {@link ChangeSetAnalyzer}.
		 */
		public ChangeSetAnalyzer(ChangeSet changeSet) {
			_changeSet = changeSet;
			_remainingCreations = _changeSet.getCreations().iterator();
			_remainingUpdates = _changeSet.getUpdates().iterator();
			_remainingDeletions = _changeSet.getDeletions().iterator();
		}

		/**
		 * Whether the {@link ChangeSet} contains relevant changes.
		 */
		public boolean hasChanges() {
			switch(_state) {
				case FULL:
					return !_changes.isEmpty();
				case NO:
					analyze(AnalyseState.PARTIAL);
					assert _state != AnalyseState.NO;
					return hasChanges();
				case PARTIAL:
					return !(_changes.isEmpty() && _updates.isEmpty());
			}
			throw new IllegalStateException("No such enum " + _state);
		}

		/**
		 * Applies the {@link Change}s to the given
		 * {@link com.top_logic.element.changelog.model.ChangeSet}.
		 */
		public void applyChanges(com.top_logic.element.changelog.model.ChangeSet out) {
			analyze(AnalyseState.FULL);
			_adaptions.forEach(Runnable::run);
			_changes.forEach(out::addChange);
		}

		private void analyze(AnalyseState completeness) {
			if (_state.ordinal() >= completeness.ordinal()) {
				// already analysed
				return;
			}
			switch (completeness) {
				case FULL:
					analyzeCreations(false);
					analyzeUpdates(false);
					analyzeDeletions(false);

					processUpdates();

					_state = AnalyseState.FULL;
					break;
				case NO:
					// is not called.
					assert false;
					break;
				case PARTIAL:
					boolean creationFound = analyzeCreations(true);
					if (creationFound) {
						_state = AnalyseState.PARTIAL;
						break;
					}
					boolean updatesFound = analyzeUpdates(true);
					if (updatesFound) {
						_state = AnalyseState.PARTIAL;
						break;
					}
					boolean deletionsFound = analyzeDeletions(true);
					if (deletionsFound) {
						_state = AnalyseState.PARTIAL;
						break;
					}
					assert _updates.isEmpty();
					assert _changes.isEmpty();
					assert _adaptions.isEmpty();
					_state = AnalyseState.FULL;
					break;
			}
			assert _state.ordinal() >= completeness.ordinal();
		}

		private void processUpdates() {
			for (Entry<TLObject, Set<TLStructuredTypePart>> entry : _updates.entrySet()) {
				TransientUpdate change = new TransientUpdate();

				TLObject newObject = entry.getKey();
				change.setObject(newObject);

				TLObject oldObject = resolve(inRevision(newObject.tId(), previousRevision())).getWrapper();
				change.setOldObject(oldObject);

				for (TLStructuredTypePart part : entry.getValue()) {
					TransientModification modification = new TransientModification();
					modification.setPart(part);

					// Cast should not be necessary, since a setter of a multiple property should
					// not expect modifyable collections.
					modification.setOldValue((Collection<Object>) CollectionUtil.asList(oldObject.tValue(part)));
					modification.setNewValue((Collection<Object>) CollectionUtil.asList(newObject.tValue(part)));

					change.addModification(modification);
				}

				registerChange(change);
			}
		}

		private long previousRevision() {
			return _changeSet.getRevision() - 1;
		}

		/**
		 * Analyzes creations in the given {@link ChangeSet} and transfers them to the given model
		 * change set.
		 * 
		 * @param stopOnChange
		 *        Whether processing must be stopped when a change was detected.
		 * @return <code>true</code> iff <code>stopOnChange</code> was <code>true</code> and a
		 *         change was detected.
		 */
		private boolean analyzeCreations(boolean stopOnChange) {
			if (_remainingCreations.hasNext()) {
				// All object IDs of objects created in the current change set.
				Set<ObjectKey> createdKeys =
					_changeSet.getCreations()
						.stream()
						.map(ObjectCreation::getOriginalObject)
						.collect(Collectors.toSet());
				do {
					ObjectCreation creation = _remainingCreations.next();
					MetaObject table = creation.getObjectType();
					boolean technicalUpdate = analyzeTechnicalUpdate(_changeSet.getRevision(), table, createdKeys, creation);

					List<TLClass> classes = _modelTables.getClassesForTable(table);
					if (classes.isEmpty()) {
						// A pure technical object.
						if (stopOnChange && technicalUpdate) {
							return true;
						} else {
							continue;
						}
					}

					TLObject object = resolve(creation.getOriginalObject()).getWrapper();
					if (excludedByModule(object) || isPersistentCacheObject(object)) {
						if (stopOnChange && technicalUpdate) {
							return true;
						} else {
							continue;
						}
					}

					// Record a creation.
					TransientCreation change = new TransientCreation();
					change.setObject(object);

					updateImplicit(change, object, createdKeys);

					registerChange(change);
					if (stopOnChange) {
						return true;
					}
				} while (_remainingCreations.hasNext());
			}
			return false;
		}

		/**
		 * Updates {@link Change#getImplicit()}.
		 * 
		 * <p>
		 * Note: Determining the value can be expensive and the value is not needed for determining
		 * {@link #hasChanges()}. So the value is updated lazy when applying the {@link #_changes}.
		 * </p>
		 */
		private void updateImplicit(Change change, TLObject object, Set<ObjectKey> keys) {
			_adaptions.add(() -> {
				TLObject container = object.tContainer();
				change.setImplicit(container != null && keys.contains(container.tId()));
			});
		}

		private boolean isPersistentCacheObject(TLObject object) {
			return TLAnnotations.isPersistentCache(object.tType());
		}

		private boolean isPersistentCacheAttribute(TLStructuredTypePart part) {
			return TLAnnotations.isPersistentCache(part);
		}

		/**
		 * Analyzes updates in the given {@link ChangeSet} and caches them for later.
		 * 
		 * @param stopOnChange
		 *        Whether processing must be stopped when a change was detected.
		 * @return <code>true</code> iff <code>stopOnChange</code> was <code>true</code> and a
		 *         change was detected.
		 * 
		 * @see #processUpdates()
		 */
		private boolean analyzeUpdates(boolean stopOnChange) {
			while (_remainingUpdates.hasNext()) {
				ItemUpdate update = _remainingUpdates.next();
				MetaObject table = update.getObjectType();
				boolean technicalUpdate =
					analyzeTechnicalUpdate(_changeSet.getRevision(), table, Collections.emptySet(), update);
		
				List<TLClass> classes = _modelTables.getClassesForTable(table);
				if (classes.isEmpty()) {
					// A pure technical object.
					if (stopOnChange && technicalUpdate) {
						return true;
					} else {
						continue;
					}
				}
		
				TLObject newObject = resolve(update.getOriginalObject()).getWrapper();
				if (excludedByModule(newObject) || isPersistentCacheObject(newObject)) {
					if (stopOnChange && technicalUpdate) {
						return true;
					} else {
						continue;
					}
				}
		
				// Record an update.
				Set<TLStructuredTypePart> changedParts = enter(newObject);
		
				Map<String, Object> valueUpdates = update.getValues();
				Map<String, Object> oldValues = update.getOldValues();
				TLStructuredType type = newObject.tType();
		
				Map<String, TLStructuredTypePart> partByColumn = _modelTables.lookupColumnBinding(type);
				for (Entry<String, Object> valueUpdate : valueUpdates.entrySet()) {
					String storageAttribute = valueUpdate.getKey();

					Object newValue = valueUpdate.getValue();
					Object oldValue = oldValues.get(storageAttribute);

					if (Utils.equals(newValue, oldValue)) {
						// A value was provided in an update event for technical reasons, without
						// the value being changed.
						continue;
					}

					TLStructuredTypePart part = partByColumn.get(storageAttribute);
					if (part == null) {
						// A change that has no model representation, ignore.
						continue;
					}
					if (isPersistentCacheAttribute(part)) {
						// Value is just a persistent cache, ignore.
						continue;
					}

					changedParts.add(part);
				}
				if (stopOnChange) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Marks the given object as changed and retrieves the set of changed parts.
		 */
		private Set<TLStructuredTypePart> enter(TLObject newObject) {
			return _updates.computeIfAbsent(newObject, x -> new HashSet<>());
		}

		private void registerChange(Change change) {
			_changes.add(change);
		}

		/**
		 * Analyzes deletions in the given {@link ChangeSet} and transfers them to the given model
		 * change set.
		 * 
		 * @param stopOnChange
		 *        Whether processing must be stopped when a change was detected.
		 * @return <code>true</code> iff <code>stopOnChange</code> was <code>true</code> and a
		 *         change was detected.
		 */
		private boolean analyzeDeletions(boolean stopOnChange) {
			if (_remainingDeletions.hasNext()) {
				long previousRev = previousRevision();
				Set<ObjectKey> deletedKeys = _changeSet.getDeletions()
					.stream()
					.map(c -> inRevision(c.getObjectId(), previousRev))
					.collect(Collectors.toSet());
				do {
					ItemDeletion deletion = _remainingDeletions.next();
					MetaObject table = deletion.getObjectType();
					boolean technicalUpdate =
						analyzeTechnicalUpdate(_changeSet.getRevision(), table, deletedKeys, deletion);

					List<TLClass> classes = _modelTables.getClassesForTable(table);
					if (classes.isEmpty()) {
						// A pure technical object.
						if (stopOnChange && technicalUpdate) {
							return true;
						} else {
							continue;
						}
					}

					KnowledgeItem item = resolve(inRevision(deletion.getObjectId(), previousRev));
					TLObject object = item.getWrapper();
					if (excludedByModule(object) || isPersistentCacheObject(object)) {
						if (stopOnChange && technicalUpdate) {
							return true;
						} else {
							continue;
						}
					}

					// Record a deletion.
					TransientDeletion change = new TransientDeletion();
					change.setObject(object);

					updateImplicit(change, object, deletedKeys);

					registerChange(change);
					if (stopOnChange) {
						return true;
					}
				} while (_remainingDeletions.hasNext());
			}
			return false;
		}

		private boolean analyzeTechnicalUpdate(long revision, MetaObject table, Set<ObjectKey> createdDeletedKeys,
				ItemChange change) {
			Map<String, AssociationStorageDescriptor> descriptors = _modelTables.getDescriptorsForTable(table);
			if (descriptors.isEmpty()) {
				// Table is not used to store value of foreign objects.
				return false;
			}

			boolean technicalUpdate = false;
			for (AssociationStorageDescriptor descriptor : descriptors.values()) {
				// A row that stores (part of) an attribute value of some object.
				ObjectKey objId = descriptor.getBaseObjectId(change.getValues());
				if (objId != null) {
					// Note: A table storing values for other objects is not required to do so for
					// every row. An example is the inline collection storage, which may optionally
					// associate value objects with container objects by storing a foreign key value
					// in the table of the value object.

					ObjectKey oldId = inRevision(objId, revision - 1);
					ObjectKey newId = inRevision(objId, revision);
					if (createdDeletedKeys.contains(oldId) || createdDeletedKeys.contains(newId)) {
						// Part of a created or deleted object, no additional change.
						continue;
					}

					TLObject newObject = resolve(newId).getWrapper();
					if (excludedByModule(newObject) || isPersistentCacheObject(newObject)) {
						continue;
					}
					ObjectKey partId = descriptor.getPartId(change.getValues());
					if (partId == null) {
						Logger.error("Unable to determine part id for update of '"
								+ MetaLabelProvider.INSTANCE.getLabel(newObject) + "' in revision '" + revision
								+ "': Changes: " + change.getValues() + ", table: " + table + ", descriptor: "
								+ descriptor,
							ChangeLogBuilder.class);
						continue;
					}
					KnowledgeItem partKI = resolve(partId);
					if (partKI == null) {
						/* Part is deleted in the meanwhile. It is possible to display the
						 * change, but not to revert it. */

						// Assume, part was (in the worst case) deleted together with the change
						// (clearing a reference).
						partKI = resolve(inRevision(partId, revision - 1));
						if (partKI == null) {
							// Part was created together with the change.
							partKI = resolve(inRevision(partId, revision));
						}
					}
					TLStructuredTypePart part = partKI.getWrapper();
					if (isPersistentCacheAttribute(part)) {
						// Value is just a persistent cache, ignore.
						continue;
					}

					enter(newObject).add(part);
					technicalUpdate = true;
				}
			}
			return technicalUpdate;
		}
	}

	private KnowledgeItem resolve(ObjectKey key) {
		return _kb.resolveObjectKey(key);
	}

	private static ObjectKey inRevision(ObjectBranchId objId, long rev) {
		return objId.toObjectKey(rev);
	}

	private static ObjectKey inRevision(ObjectKey objId, long rev) {
		return new DefaultObjectKey(objId.getBranchContext(), rev, objId.getObjectType(), objId.getObjectName());
	}

	/**
	 * Lookup the account that was the author of the given {@link Revision}, or <code>null</code>
	 * for a technical transaction.
	 */
	private Person resolveAuthor(Revision revision) {
		String authorSpec = revision.getAuthor();
		Person author;
		if (authorSpec.startsWith(SessionContext.PERSON_ID_PREFIX)) {
			KnowledgeItem authorItem = _kb.resolveObjectKey(
				new DefaultObjectKey(
					_hm.getTrunk().getBranchId(), revision.getCommitNumber(),
					_kb.getMORepository().getMetaObject(Person.OBJECT_NAME),
					LongID.fromExternalForm(authorSpec.substring(SessionContext.PERSON_ID_PREFIX.length()))));
			author = authorItem == null ? null : authorItem.getWrapper();
		} else {
			author = null;
		}
		return author;
	}

	boolean excludedByModule(TLObject obj) {
		if (_excludeModules.isEmpty()) {
			return false;
		}
		TLModule module = obj.tType().getModule();
		return isExcludedModule(module);
	}

	boolean isExcludedModule(TLModule module) {
		return _excludeModules.contains(module.getName());
	}
	
}
