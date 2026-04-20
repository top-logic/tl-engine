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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.context.TLSessionContext;
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
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.element.changelog.model.Change;
import com.top_logic.element.changelog.model.trans.TransientChangeSet;
import com.top_logic.element.model.cache.ElementModelCacheService;
import com.top_logic.element.model.cache.ModelTables;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.RevisionType;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
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

	private ChangeFilter _filter;

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
	 * Optional {@link ChangeFilter} restricting which {@link Change}s are included in the result.
	 *
	 * <p>
	 * {@code null} means no filtering beyond the builder's own options.
	 * </p>
	 */
	public ChangeFilter getFilter() {
		return _filter;
	}

	/**
	 * @see #getFilter()
	 */
	public ChangeLogBuilder setFilter(ChangeFilter filter) {
		_filter = filter;
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

		Revision effectiveStartRev = _filter == null ? _startRev : _filter.adjustStartRev(_startRev);

		List<LongRange> revisionRanges = getRevisionRanges(effectiveStartRev);
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

				ChangeSetAnalyzer analyzer =
					new ChangeSetAnalyzer(_kb, _modelTables, _excludeModules, changeSet).setFilter(_filter);
				if ((!_includeTechnical || _filter != null) && !analyzer.hasChanges()) {
					// When a filter is active, a change set that has no changes passing the filter
					// must not be reported, regardless of the _includeTechnical setting.
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

	private List<LongRange> getRevisionRanges(Revision effectiveStartRev) {
		long startRev = effectiveStartRev.getCommitNumber();
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
	 * Lookup the account that was the author of the given {@link Revision}, or <code>null</code>
	 * for a technical transaction.
	 */
	private Person resolveAuthor(Revision revision) {
		String authorSpec = revision.getAuthor();
		Person author;
		if (authorSpec.startsWith(SessionContext.PERSON_ID_PREFIX)) {
			com.top_logic.knowledge.objects.KnowledgeItem authorItem = _kb.resolveObjectKey(
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

	boolean isExcludedModule(TLModule module) {
		return _excludeModules.contains(module.getName());
	}

}
