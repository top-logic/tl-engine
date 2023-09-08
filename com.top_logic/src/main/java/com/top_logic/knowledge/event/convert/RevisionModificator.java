/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ValueModificator;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.NextCommitNumberFuture;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;

/**
 * The class {@link RevisionModificator} is a rewriter which modifies the revisions of the events
 * and attributes of types which represent revisions.
 * 
 * @see BranchModificator
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RevisionModificator extends RewritingEventVisitor {

	/**
	 * Configuration options for {@link RevisionModificator}.
	 */
	public interface Config extends PolymorphicConfiguration<EventRewriter> {

		/**
		 * @see #getMode()
		 */
		String MODE = "mode";
		
		/**
		 * @see #getRevisionAttributes()
		 */
		String REVISION_ATTRIBUTES = "revision-attributes";

		/**
		 * The initial mode of operation.
		 */
		@Name(MODE)
		Modus getMode();

		/**
		 * Attributes that are known to contain revision numbers.
		 * 
		 * <p>
		 * The attributes are indexed by threir type name.
		 * </p>
		 */
		@Name(REVISION_ATTRIBUTES)
		@MapBinding(tag = "type", key = "name", attribute = "attributes", valueFormat = CommaSeparatedStringSet.class)
		Map<String, Set<String>> getRevisionAttributes();

	}

	/**
	 * Different modus in which some {@link RevisionModificator} can be changed
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static enum Modus {

		/**
		 * Guess revision inserts and drops from the stream of events.
		 * 
		 * <p>
		 * Missing revisions in the input stream are assumed to be dropped, while a duplicated
		 * revision in the input stream marks its first occurrence as inserted revision.
		 * </p>
		 */
		auto,

		/**
		 * Replay modus: It is expected that in this modus original {@link KnowledgeEvent}s are
		 * replayed. Each revision number and attribute which represents a revision number is
		 * adopted.
		 */
		replay,

		/**
		 * History faking modus: It is expected that in that modus no original events are given but
		 * only synthesized. Neither revision numbers nor attributes representing revisions are
		 * adopted.
		 */
		fakingHistory,
	}

	private static final String START_REVISION = "startRevision";

	/**
	 * Declaration of revision attributes by type
	 */
	private final RevisionAttributes _revAttributesByType;

	/**
	 * Value holder for the modifications
	 */
	private ValueModificator _modificator = new ValueModificator();

	/**
	 * The revision which is currently processed.
	 */
	private long _expectedRevision;

	/**
	 * The revision that is expected to be created next in the input stream.
	 * 
	 * <p>
	 * This value is only relevant in {@link Modus#auto}.
	 * </p>
	 */
	private long _nextInputRev;

	/**
	 * modification during processing the {@link #_expectedRevision}
	 */
	private long _currentModification = 0L;

	/**
	 * last stored modification
	 */
	private long _lastModification = 0L;

	/**
	 * Whether this modificator is currently in replay modus or in faking history
	 */
	private Modus _modus = Modus.replay;

	/**
	 * Maps the name of a {@link MetaObject} to the sequence of all {@link MOClass#getSuperclass()
	 * super classes}.
	 */
	private Map<String, List<String>> _superTypes = new HashMap<>();

	private Map<String, Set<String>> _cachedRevisionAttributes = new HashMap<>();

	/**
	 * The class {@link RevisionAttributes} maps the name of a type to the set of attributes which
	 * represents revision numbers. If there is no such attribute <code>null</code> may be returned.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static interface RevisionAttributes extends Mapping<String, Set<String>> {

		/**
		 * A {@link RevisionAttributes} which can be used if no type has an attribute which
		 * represents a revision.
		 */
		public static final RevisionAttributes NO_REVISION_ATTRIBUTES = new RevisionAttributes() {

			/**
			 * Constantly returns <code>null</code>
			 */
			@Override
			public Set<String> map(String input) {
				return null;
			}
		};
	}

	/**
	 * Creates a {@link RevisionModificator} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RevisionModificator(InstantiationContext context, Config config) {
		this(revAttributes(config), 1L);
		setModus(config.getMode());
	}

	private static RevisionAttributes revAttributes(Config config) {
		final Map<String, Set<String>> attributesByType = config.getRevisionAttributes();
		return revAttributes(attributesByType);
	}

	/**
	 * Converts a {@link Map} to {@link RevisionAttributes}.
	 * 
	 * @param attributesByType
	 *        Set of attribute names that contain revision numbers indexed by their type.
	 * @return {@link RevisionAttributes} wrapper.
	 */
	public static RevisionAttributes revAttributes(final Map<String, Set<String>> attributesByType) {
		return new RevisionAttributes() {
			@Override
			public Set<String> map(String input) {
				return attributesByType.get(input);
			}
		};
	}

	/**
	 * Creates a {@link RevisionModificator} which expects that the first event
	 * to rewrite has <code>startRevision</code> as {@link KnowledgeEvent#getRevision() revision} .
	 * 
	 * @param revAttributes
	 *        the computation of attributes which represent a revision number
	 * @param startRevision
	 *        the revision the first event to process has.
	 */
	public RevisionModificator(RevisionAttributes revAttributes, long startRevision) {
		this._revAttributesByType = revAttributes;
		this._expectedRevision = startRevision;
	}

	/**
	 * Setup the start revision.
	 * 
	 * @param startRevision
	 *        The first revision that should be produced in the output stream.
	 */
	@Inject
	public void initStartRevision(@Named(START_REVISION) Long startRevision) {
		_expectedRevision = startRevision;
		_nextInputRev = startRevision;
	}

	/**
	 * Initialises this {@link RevisionModificator} with the {@link MORepository} used for
	 * rewriting.
	 */
	@Inject
	public void initMORepository(MORepository repository) {
		repository.getMetaObjects().forEach(mo -> addSuperType(mo));
	}

	private void addSuperType(MetaObject mo) {
		if (_superTypes.containsKey(mo.getName())) {
			return;
		}
		MetaObject superType;
		if (mo instanceof MOClass) {
			superType = ((MOClass) mo).getSuperclass();
		} else {
			superType = null;
		}
		List<String> classes;
		if (superType == null) {
			classes = Collections.emptyList();
		} else {
			addSuperType(superType);
			List<String> superClasses = _superTypes.get(superType.getName());
			classes = new ArrayList<>(superClasses.size() + 1);
			classes.add(superType.getName());
			classes.addAll(superClasses);
		}
		_superTypes.put(mo.getName(), classes);

	}

	/**
	 * the current applied modification
	 */
	public long getCurrentModification() {
		return _currentModification;
	}

	/**
	 * the current applied modification
	 */
	public long getExpectedRevision() {
		return _expectedRevision;
	}

	/**
	 * Current modus of the {@link RevisionModificator}
	 * 
	 * @see Modus#replay
	 * @see Modus#fakingHistory
	 */
	public Modus getModus() {
		return _modus;
	}

	@Override
	public void rewrite(ChangeSet cs, EventWriter out) {
		long processedRevision = cs.getRevision();
		if (ignore(processedRevision)) {
			super.rewrite(cs, out);
			return;
		}
		switch (_modus) {
			case auto: {
				long diff = processedRevision - _nextInputRev;
				if (diff > 0) {
					// There are revisions missing in the input stream, announce them as dropped.
					setModus(Modus.fakingHistory);
					dropRevisions(diff);
					setModus(Modus.auto);
				} else if (diff < 0) {
					// There were revisions inserted into the stream.
					_currentModification -= diff;
					_modificator.clearFrom(processedRevision);
					long lastModification = _modificator.getModificationValue(processedRevision);
					if (lastModification != _currentModification) {
						_modificator.addModification(processedRevision, _currentModification);
					}
				}

				_nextInputRev = processedRevision + 1;
			}
			//$FALL-THROUGH$
			case replay: {
				processedRevision += _currentModification;
				cs.setRevision(processedRevision);
				break;
			}
			case fakingHistory: {
				break;
			}
		}
		if (processedRevision != _expectedRevision) {
			throw new IllegalArgumentException("Expected ChangeSet with revision " + _expectedRevision + ": " + cs);
		}
		super.rewrite(cs, out);
	}

	private static boolean ignore(long processedRevision) {
		return processedRevision < Revision.FIRST_REV || processedRevision == Revision.CURRENT_REV;
	}

	private static boolean ignore(KnowledgeEvent event) {
		return ignore(event.getRevision());
	}

	@Override
	protected Object visitItemChange(ItemChange event, Void arg) {
		if (ignore(event)) {
			return super.visitItemChange(event, arg);
		}
		if (_modus != Modus.fakingHistory) {
			final String type = event.getObjectType().getName();
			final Map<String, Object> values = event.getValues();
			update(type, values);
		}
		return super.visitItemChange(event, arg);
	}

	/**
	 * Updates the values of an object of the given type, i.e. if some attribute represents a
	 * revision number it is updated or if some value represents an object (means the value is an
	 * {@link ObjectKey}) it is also updated.
	 * 
	 * @param type
	 *        the type to determine attributes which represents revision attributes.
	 * @param values
	 *        the values of an object of the given type
	 */
	protected void update(final String type, final Map<String, Object> values) {
		final Collection<String> revAttrNames = revisionAttributes(type);
		if (CollectionUtil.isEmptyOrNull(revAttrNames)) {
			for (Entry<String, Object> entry : values.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof ObjectKey) {
					updateObjectKey(entry, (ObjectKey) value);
				}
			}
		} else {
			for (Entry<String, Object> entry : values.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof ObjectKey) {
					updateObjectKey(entry, (ObjectKey) value);
				} else {
					if (revAttrNames.contains(entry.getKey())) {
						updateRevisionNumber(entry, value);
					}
				}
			}
		}
	}

	private Set<String> revisionAttributes(String type) {
		Set<String> attributes = _cachedRevisionAttributes.get(type);
		if (attributes == null) {
			attributes = computeRevisionAttributes(type);
			_cachedRevisionAttributes.put(type, attributes);
		}
		return attributes;
	}

	private Set<String> computeRevisionAttributes(String type) {
		Set<String> result = _revAttributesByType.map(type);
		List<String> relevantTypes = _superTypes.get(type);
		if (CollectionUtil.isEmptyOrNull(relevantTypes)) {
			return CollectionUtil.nonNull(result);
		}
		boolean resultCopied = false;
		for (String relevantType : relevantTypes) {
			Set<String> attrs = _revAttributesByType.map(relevantType);
			if (CollectionUtil.isEmptyOrNull(attrs)) {
				continue;
			}
			if (CollectionUtil.isEmptyOrNull(result)) {
				result = attrs;
				continue;
			}
			if (!resultCopied) {
				result = new HashSet<>(result);
				resultCopied = true;
			}
			result.addAll(attrs);
		}
		return CollectionUtil.nonNull(result);
	}

	/**
	 * Checks that the given value is a {@link Long}, modifies it and updates the {@link Entry}.
	 * 
	 * @param entry
	 *        the entry to update
	 * @param value
	 *        the value of the entry
	 * 
	 * @throws IllegalArgumentException
	 *         iff the given value can not be casted to {@link Long}
	 * 
	 * @see #modifyRevNumber(long)
	 */
	protected final void updateRevisionNumber(final Entry<String, Object> entry, final Object value) {
		if (value instanceof NextCommitNumberFuture) {
			// NextCommitNumberFuture is later modified to the commit revision.
			return;
		}
		final Long revNumber;
		try {
			revNumber = (Long) value;
		} catch (ClassCastException ex) {
			throw new IllegalArgumentException("Attribute " + entry.getKey()
				+ " has unexpected value type: " + value + " is not a revision number.", ex);
		}
		entry.setValue(modifyRevNumber(revNumber.longValue()));
	}

	/**
	 * Updates the value of the {@link Entry}. This is done by transforming the given
	 * {@link ObjectKey} (if necessary). The {@link ObjectKey#getHistoryContext()} will be adopted.
	 * 
	 * @param entry
	 *        the entry to update
	 * @param value
	 *        the value of the entry
	 * 
	 * @see #modifyRevNumber(long)
	 */
	protected final void updateObjectKey(final Entry<String, Object> entry, final ObjectKey value) {
		final long historyContext = value.getHistoryContext();
		final long modified = modifyRevNumber(historyContext);
		if (modified != historyContext) {
			final long branchContext = value.getBranchContext();
			final MetaObject objectType = value.getObjectType();
			final TLID objectName = value.getObjectName();
			entry.setValue(new DefaultObjectKey(branchContext, modified, objectType, objectName));
		}
	}

	/**
	 * Computes the necessary modification for the given revision number and (if necessary) returns
	 * a modified value. If the resulting value {@link Long#equals(Object) equals} the given value,
	 * the original is returned.
	 * 
	 * @param rev
	 *        the revision to modify
	 * 
	 * @return the modification of the given <code>rev</code> (if necessary).
	 */
	public final long modifyRevNumber(long rev) {
		if (rev == Revision.CURRENT_REV) {
			// don't modify infinity
			return rev;
		}
		long modification = getModification(rev);
		final long modificated = rev + modification;
		if (modificated < 0) {
			throw new IllegalArgumentException("Modificated revision number is negative: REV:" + rev
				+ " Modification:" + modification);
		}
		return modificated;
	}

	/**
	 * Computes the modification this must be done for the given revision number
	 * 
	 * @param revNumber
	 *        the number whose modification must be computed
	 * 
	 * @return the modification which must be added to the given revision number.
	 */
	protected final long getModification(final long revNumber) {
		final long revision = revNumber + _currentModification;
		if (revision == _expectedRevision) {
			return _currentModification;
		}
		if (revision > _expectedRevision) {
			throw new IllegalArgumentException("revision '" + revision + "' points to a future revision. Current: "
				+ _expectedRevision);
		}
		return _modificator.getModificationValue(revNumber);
	}

	@Override
	public Object visitUpdate(ItemUpdate event, Void arg) {
		if (ignore(event)) {
			return super.visitUpdate(event, arg);
		}
		if (_modus != Modus.fakingHistory) {
			final String type = event.getObjectType().getName();
			final Map<String, Object> oldValues = event.getOldValues();
			if (oldValues != null) {
				// old values may be null
				update(type, oldValues);
			}
		}
		return super.visitUpdate(event, arg);
	}

	@Override
	public Object visitBranch(BranchEvent event, Void arg) {
		if (ignore(event)) {
			return super.visitBranch(event, arg);
		}
		if (_modus != Modus.fakingHistory) {
			final long baseRevisionNumber = event.getBaseRevisionNumber();
			final long modification = getModification(baseRevisionNumber);
			if (modification != 0) {
				final long modified = baseRevisionNumber + modification;
				if (modified < 0) {
					throw new IllegalArgumentException("Modificated base revision number is negative: BaseRevision:"
						+ baseRevisionNumber + " Modification:" + modification);
				}
				event.setBaseRevisionNumber(modified);
			}
		}
		return super.visitBranch(event, arg);
	}

	@Override
	public Object visitCommit(CommitEvent event, Void arg) {
		final Object result = super.visitCommit(event, arg);
		if (ignore(event)) {
			return result;
		}
		_expectedRevision++;
		if (_modus == Modus.fakingHistory) {
			_currentModification++;
		}
		return result;
	}

	/**
	 * Changes the modus of the rewriter
	 * 
	 * @param modus
	 *        the new modus. The same as {@link #getModus()} once the method returns.
	 */
	public void setModus(Modus modus) {
		if (this._modus == modus) {
			return;
		}
		this._modus = modus;

		switch (_modus) {
			case auto:
				_nextInputRev = _expectedRevision - _currentModification;
				//$FALL-THROUGH$
			case replay: {
				if (_lastModification < _currentModification) {
					// Inserts have happened.
					_modificator.addModification(_expectedRevision - _currentModification, _currentModification);
					_lastModification = _currentModification;
				} else {
					while (_lastModification > _currentModification) {
						// Drops have happened.
						_lastModification--;

						_modificator.addModification(_expectedRevision - _lastModification - 1, _lastModification);
					}
				}
				break;
			}

			case fakingHistory: {
				break;
			}
		}
	}

	/** 
	 * Service method. Calls {@link #dropRevisions(long)} with <code>1L</code>
	 * 
	 * @see #dropRevisions(long)
	 */
	public final void dropRevision() {
		dropRevisions(1L);
	}

	/**
	 * Drops the number of revisions, i.e. it is expected that the following
	 * <code>numberRevsToDrop</code>-many commits are skipped.
	 * 
	 * @param numberRevsToDrop
	 *        the number of commits to drop
	 */
	public void dropRevisions(long numberRevsToDrop) {
		if (_modus != Modus.fakingHistory) {
			throw new IllegalStateException("Can only drop revisions in faking modus");
		}
		if (numberRevsToDrop <= 0) {
			throw new IllegalArgumentException("Can only drop positive numbers of revisions");
		}
		_currentModification -= numberRevsToDrop;
	}

	@Override
	protected Object visitKnowledgeEvent(KnowledgeEvent event, Void arg) {
		long processedRevision = event.getRevision();
		if (ignore(processedRevision)) {
			return super.visitKnowledgeEvent(event, arg);
		}
		if (_modus != Modus.fakingHistory) {
			processedRevision += _currentModification;
			event.setRevision(processedRevision);
		}
		if (processedRevision != _expectedRevision) {
			throw new IllegalArgumentException("Expected event with revision " + _expectedRevision + ": " + event);
		}

		return super.visitKnowledgeEvent(event, arg);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(RevisionModificator.class.getName());
		builder.append('[');
		builder.append("modus:").append(_modus);
		builder.append(",expectedRevision:").append(_expectedRevision);
		builder.append(",currentModification:").append(_currentModification);
		builder.append(",modifications:").append(_modificator);
		builder.append(']');
		return builder.toString();
	}

	/**
	 * Adopts the given {@link RevisionModificator} such that any event with the given revision
	 * number can be the next event, i.e. the {@link RevisionModificator} is adopted such that it
	 * adopts the next event (if it has the given revision number) to have the correct commit number
	 * for replay.
	 * 
	 * @param modificator
	 *        the {@link RevisionModificator} to adopt
	 * @param kb
	 *        the {@link KnowledgeBase} to write to.
	 * @param nextEvtRev
	 *        the revision number of the next event
	 */
	public static void adoptRevisionModficator(RevisionModificator modificator, KnowledgeBase kb, long nextEvtRev) {
		long expectedNumber = kb.getHistoryManager().getLastRevision() + 1;
		long difference = expectedNumber - nextEvtRev;
		modificator._currentModification = difference;
	}

}
