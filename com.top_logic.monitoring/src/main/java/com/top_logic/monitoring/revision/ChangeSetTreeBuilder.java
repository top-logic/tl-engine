/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.revision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.CloseableIteratorProxy;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilteredCloseableIterator;
import com.top_logic.basic.col.InlineMap;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.ChangeSetReaderIterator;
import com.top_logic.knowledge.event.EmptyChangeSetFilter;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemEventVisitor;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.ReaderConfig;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLCollator;
import com.top_logic.util.model.ModelService;

/**
 * {@link DefaultTreeTableBuilder} building a tree containing {@link ChangeSet}, {@link ItemEvent},
 * and {@link ChangeEntry} from a given {@link ChangeSetTreeRoot}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeSetTreeBuilder extends DefaultTreeTableBuilder {

	/**
	 * Property used to attach the {@link KnowledgeBase} to the created {@link TLTreeNode}. This is
	 * necessary to ensure that the same {@link KnowledgeBase} is used as in the root object.
	 */
	private static final Property<KnowledgeBase> KB_PROPERTY = TypedAnnotatable.property(KnowledgeBase.class, "kb");

	/** Singleton {@link ChangeSetTreeBuilder} instance. */
	public static final ChangeSetTreeBuilder INSTANCE = new ChangeSetTreeBuilder();

	/**
	 * Fetches the {@link ObjectKey} of the type of the touched object. It may be <code>null</code>
	 * in case the object has not type.
	 * 
	 * <p>
	 * The returned key (if not <code>null</code>) has the correct revision to resolve.
	 * </p>
	 * 
	 */
	private static ItemEventVisitor<ObjectKey, Void> T_TYPE_FINDER = new ItemEventVisitor<>() {

		@Override
		public ObjectKey visitCreateObject(ObjectCreation event, Void arg) {
			ObjectKey key = (ObjectKey) event.getValues().get(PersistentObject.TYPE_REF);
			return toLookupKey(key, event.getRevision());
		}

		@Override
		public ObjectKey visitDelete(ItemDeletion event, Void arg) {
			ObjectKey key = (ObjectKey) event.getOldValue(PersistentObject.TYPE_REF);
			return toLookupKey(key, event.getRevision() - 1);
		}

		@Override
		public ObjectKey visitUpdate(ItemUpdate event, Void arg) {
			ObjectKey key = (ObjectKey) event.getValues().remove(PersistentObject.TYPE_REF);
			return toLookupKey(key, event.getRevision());
		}

		private ObjectKey toLookupKey(ObjectKey key, long revision) {
			if (key == null) {
				return null;
			}
			return KBUtils.ensureHistoryContext(key, revision);
		}

	};

	/**
	 * Creates a new {@link ChangeSetTreeBuilder}.
	 */
	protected ChangeSetTreeBuilder() {
		// no instance variables, because type is singleton.
	}


	@Override
	public DefaultTreeTableNode createNode(AbstractMutableTLTreeModel<DefaultTreeTableNode> model,
			DefaultTreeTableNode parent, Object userObject) {
		DefaultTreeTableNode child = super.createNode(model, parent, userObject);
		if (userObject instanceof ChangeSetTreeRoot) {
			child.set(KB_PROPERTY, ((ChangeSetTreeRoot) userObject)._kb);
			return child;
		}
		if (parent != null) {
			KnowledgeBase kb = parent.get(KB_PROPERTY);
			child.set(KB_PROPERTY, kb);
			return child;
		}
		return child;
	}

	@Override
	public List<DefaultTreeTableNode> createChildList(DefaultTreeTableNode node) {
		List<DefaultTreeTableNode> result = super.createChildList(node);
		AbstractMutableTLTreeModel<DefaultTreeTableNode> model = node.getModel();
		Object bo = node.getBusinessObject();
		if (bo instanceof ChangeSetTreeRoot) {
			ChangeSetTreeRoot csTreeRoot = (ChangeSetTreeRoot) bo;
			try (CloseableIterator<ChangeSet> reader = csTreeRoot.fetchChanges()) {
				while (reader.hasNext()) {
					result.add(createNode(model, node, reader.next()));
				}
			}
			Collections.reverse(result);
		} else if (bo instanceof ChangeSet) {
			ChangeSet cs = (ChangeSet) bo;
			for (ItemChange evt : cs.getCreations()) {
				result.add(createNode(model, node, evt));
			}
			for (ItemChange evt : cs.getUpdates()) {
				result.add(createNode(model, node, evt));
			}
			for (ItemChange evt : cs.getDeletions()) {
				result.add(createNode(model, node, evt));
			}
		} else if (bo instanceof ItemChange) {
			KnowledgeBase kb = node.get(KB_PROPERTY);
			if (kb == null) {
				throw new IllegalArgumentException("Node " + node + " was not created by " + ChangeSetTreeBuilder.class);
			}
			ItemChange change = (ItemChange) bo;
			Map<String, Object> values = change.getValues();
			ObjectKey tTypeKey = change.visitItemEvent(T_TYPE_FINDER, null);
			TLStructuredType type;
			if (tTypeKey != null) {
				KnowledgeItem typeItem = lookupInHistoryContext(kb, tTypeKey, tTypeKey.getHistoryContext());
				TLType typeWrapper = (TLType) typeItem.getWrapper();
				if (typeWrapper instanceof TLStructuredType) {
					type = (TLStructuredType) typeWrapper;
				} else {
					type = null;
				}
			} else {
				type = null;
			}
			String[] attributes = values.keySet().toArray(ArrayUtil.EMPTY_STRING_ARRAY);
			Arrays.sort(attributes, newCollator());
			for (String attribute : attributes) {
				ChangeEntry changeEntry = createChangeEntry(kb, change, type, attribute);
				result.add(createNode(model, node, changeEntry));
			}
		}
		return result;
	}

	private Comparator<? super String> newCollator() {
		return new TLCollator();
	}

	private ChangeEntry createChangeEntry(KnowledgeBase kb, ItemChange change, TLStructuredType type,
			String attribute) {
		ResKey attributeI18N = null;
		if (type != null) {
			TLStructuredTypePart part = type.getPart(attribute);
			if (part != null) {
				attributeI18N = TLModelI18N.getI18NKey(part);
			}
		}
		Object oldValue, newValue;
		if (change instanceof ItemDeletion) {
			oldValue = resolve(kb, change.getOldValue(attribute), change.getRevision() - 1);
			newValue = null;
		} else if (change instanceof ObjectCreation) {
			oldValue = null;
			newValue = resolve(kb, change.getValues().get(attribute), change.getRevision());
		} else {
			oldValue = resolve(kb, change.getOldValue(attribute), change.getRevision() - 1);
			newValue = resolve(kb, change.getValues().get(attribute), change.getRevision());
		}
		return new ChangeEntry(attribute, attributeI18N, oldValue, newValue);
	}

	private Object resolve(KnowledgeBase kb, Object value, long lookupRevision) {
		if (value instanceof ObjectKey) {
			ObjectKey key = (ObjectKey) value;
			return lookupInHistoryContext(kb, key, lookupRevision);
		}
		if (value instanceof List) {
			List<?> listValue = (List<?>) value;
			List<Object> result = null;
			for (int i = 0, size = listValue.size(); i < size; i++) {
				Object orig = listValue.get(i);
				Object resolved = resolve(kb, orig, lookupRevision);
				if (result != null) {
					// result is not the value itself, i.e. some value before has changed
					result.add(resolved);
					continue;
				}
				if (resolved != orig) {
					// First changed object
					result = new ArrayList<>(size);
					// Add previously not changed objects
					result.addAll(listValue.subList(0, i));
					result.add(resolved);
				}
			}
			if (result != null) {
				return result;
			} else {
				return listValue;
			}
		}
		if (value instanceof Iterable) {
			ArrayList<Object> resolvedResult = new ArrayList<>();
			boolean changed = false;
			for (Object entry : (Iterable<?>) value) {
				Object resolved = resolve(kb, entry, lookupRevision);
				resolvedResult.add(resolved);
				if (resolved != entry) {
					changed = true;
				}
			}
			if (changed) {
				return resolvedResult;
			} else {
				return value;
			}
		}
		return value;
	}

	static KnowledgeItem lookupInHistoryContext(KnowledgeBase kb, ObjectKey key, long lookupRevision) {
		if (HistoryUtils.isCurrent(key)) {
			key = KBUtils.ensureHistoryContext(key, lookupRevision);
		}
		return kb.resolveObjectKey(key);
	}

	/**
	 * Root object for a {@link ChangeSet} tree.
	 *
	 * <p>
	 * This root object defines the range of revisions to display in the tree.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ChangeSetTreeRoot {

		private final class HideSystemObjects extends CloseableIteratorProxy<ChangeSet> {

			HideSystemObjects(CloseableIterator<? extends ChangeSet> impl) {
				super(impl);
			}

			@Override
			public ChangeSet next() {
				ChangeSet next = super.next();
				removeSystemObjects(next);
				return next;
			}

			private void removeSystemObjects(ChangeSet next) {
				removeSystemObjects(next.getCreations());
				removeSystemObjects(next.getUpdates());
				removeSystemObjects(next.getDeletions());
			}

			private <T extends ItemChange> void removeSystemObjects(List<T> events) {
				for (Iterator<T> it = events.iterator(); it.hasNext();) {
					if (!it.next().getValues().containsKey(PersistentObject.TYPE_REF)) {
						it.remove();
					}
				}
			}
		}

		private final class InlineAssociations extends CloseableIteratorProxy<ChangeSet> {

			InlineAssociations(CloseableIterator<? extends ChangeSet> impl) {
				super(impl);
			}

			@Override
			public ChangeSet next() {
				ChangeSet next = super.next();
				inlineAssociations(next);
				return next;
			}

			private void inlineAssociations(ChangeSet next) {
				InlineMap<ObjectBranchId, ItemUpdate> newEventsById = InlineMap.empty();
				newEventsById = inlineAssociations(newEventsById, next.getDeletions(), false);
				newEventsById = inlineAssociations(newEventsById, next.getCreations(), true);
				next.mergeAll(newEventsById.toMap().values());
			}

			private InlineMap<ObjectBranchId, ItemUpdate> inlineAssociations(
					InlineMap<ObjectBranchId, ItemUpdate> newEventsById,
					List<? extends ItemChange> createOrDeletes, boolean creation) {
				for (Iterator<? extends ItemChange> it = createOrDeletes.iterator(); it.hasNext();) {
					ItemChange next = it.next();
					MetaObject changedType = next.getObjectId().getObjectType();
					if (!isHasWrapperTable(changedType)) {
						continue;
					}
					newEventsById = createUpdateFromChange(newEventsById, next, creation);
				}
				return newEventsById;
			}

			private InlineMap<ObjectBranchId, ItemUpdate> createUpdateFromChange(
					InlineMap<ObjectBranchId, ItemUpdate> newEventsById, ItemChange next, boolean creation) {
				Map<String, Object> values;
				if (creation) {
					values = next.getValues();
				} else {
					// In deletions getValues() contain the values before the deletion
					values = next.getValues();
				}
				ObjectKey dest = (ObjectKey) values.get(DBKnowledgeAssociation.REFERENCE_DEST_NAME);
				ObjectKey sourceKey = (ObjectKey) values.get(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
				ObjectKey attrKey = (ObjectKey) values.get(ApplicationObjectUtil.META_ATTRIBUTE_ATTR);
				long revision = next.getRevision();
				ObjectBranchId evtId = ObjectBranchId.toObjectBranchId(sourceKey);
				long lookupRevision;
				if (creation) {
					lookupRevision = revision;
				} else {
					// The attribute may be deleted in the revision.
					lookupRevision = revision - 1;
				}
				KnowledgeItem attribute = ChangeSetTreeBuilder.lookupInHistoryContext(_kb, attrKey, lookupRevision);
				TLStructuredTypePart part = (TLStructuredTypePart) attribute.getWrapper();
				String attributeName = part.getName();
				boolean multipleRef = part.isMultiple();
				ItemUpdate update = newEventsById.getValue(evtId);
				if (update == null) {
					update = new ItemUpdate(lookupRevision, evtId, true);
					KnowledgeItem source = ChangeSetTreeBuilder.lookupInHistoryContext(_kb, sourceKey, lookupRevision);
					ObjectKey currentTypeKey = KBUtils.ensureHistoryContext(typeKey(source), Revision.CURRENT_REV);
					update.setValue(PersistentObject.TYPE_REF, currentTypeKey, currentTypeKey, false);
					Object newValue;
					if (multipleRef) {
						newValue = CollectionFactory.set(dest);
					} else {
						newValue = dest;
					}
					if (creation) {
						update.setValue(attributeName, null, newValue);
					} else {
						update.setValue(attributeName, newValue, null);
					}
					newEventsById = newEventsById.putValue(evtId, update);
				} else {
					if (!multipleRef) {
						if (creation) {
							update.setValue(attributeName, null, dest);
						} else {
							update.setValue(attributeName, dest, null);
						}
					} else {
						Map<String, Object> values2;
						if (creation) {
							values2 = update.getValues();
						} else {
							values2 = update.getOldValues();
						}
						Set<ObjectKey> otherValuesForAttribute = (Set<ObjectKey>) values2.get(attributeName);
						if (otherValuesForAttribute == null) {
							// No value for this attribute
							if (creation) {
								update.setValue(attributeName, null, CollectionFactory.set(dest));
							} else {
								update.setValue(attributeName, CollectionFactory.set(dest), null);
							}
						} else {
							otherValuesForAttribute.add(dest);
						}
					}
				}
				return newEventsById;
			}

			private ObjectKey typeKey(KnowledgeItem item) {
				try {
					TLObject wrapper = item.getWrapper();
					TLStructuredType type = wrapper.tType();
					if (type == null) {
						// Type no longer exists, use top-most type.
						type = TLModelUtil.tlObjectType(ModelService.getApplicationModel());
					}
					return type.tId();
				} catch (NullPointerException ex) {
					// Trap for problem reported in Ticket #23017.
					throw (NullPointerException) new NullPointerException("No type in '" + item + "'.").initCause(ex);
				}
			}

			private boolean isHasWrapperTable(MetaObject changedType) {
				if (ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION_BASE.equals(changedType.getName())) {
					return true;
				}
				if (MetaObjectUtils.isClass(changedType)) {
					MOClass superclass = ((MOClass) changedType).getSuperclass();
					if (superclass != null) {
						return isHasWrapperTable(superclass);
					}
				}
				return false;
			}
		}

		private final Revision _startRev;

		private final Revision _stopRev;

		final KnowledgeBase _kb;

		private final Filter<? super ChangeSet> _filter;

		final boolean _showSystemObjects;

		/**
		 * Creates a new {@link ChangeSetTreeRoot}.
		 * 
		 * @param kb
		 *        {@link KnowledgeBase} to fetch {@link ChangeSet} from,
		 * @param startRev
		 *        First revision to display in the {@link ChangeSet} tree.
		 * @param stopRev
		 *        Last revision to display in the {@link ChangeSet} tree.
		 * @param showSystemObjects
		 *        Whether system objects (i.e. objects without business {@link TLObject#tType()})
		 *        must be displayed.
		 * @param filter
		 *        Only {@link ChangeSet} matching this filter are contained in the displayed
		 *        changes.
		 */
		public ChangeSetTreeRoot(KnowledgeBase kb, Revision startRev, Revision stopRev, boolean showSystemObjects,
				Filter<? super ChangeSet> filter) {
			_kb = kb;
			_startRev = startRev;
			_stopRev = stopRev;
			_showSystemObjects = showSystemObjects;
			_filter = filter;
		}

		/**
		 * The changes described by this {@link ChangeSetTreeRoot}.
		 */
		CloseableIterator<ChangeSet> fetchChanges() {
			ReaderConfig readerConfig = ReaderConfigBuilder.createConfig(_startRev, _stopRev);
			ChangeSetReader csReader = _kb.getChangeSetReader(readerConfig);
			CloseableIterator<ChangeSet> csIterator = new ChangeSetReaderIterator(csReader);
			csIterator = new FilteredCloseableIterator<>(csIterator, _filter);
			csIterator = new InlineAssociations(csIterator);
			if (!_showSystemObjects) {
				csIterator = new HideSystemObjects(csIterator);
			}
			csIterator = new FilteredCloseableIterator<>(csIterator, FilterFactory.not(EmptyChangeSetFilter.INSTANCE));
			return csIterator;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_filter == null) ? 0 : _filter.hashCode());
			result = prime * result + ((_kb == null) ? 0 : _kb.hashCode());
			result = prime * result + (_showSystemObjects ? 1231 : 1237);
			result = prime * result + ((_startRev == null) ? 0 : _startRev.hashCode());
			result = prime * result + ((_stopRev == null) ? 0 : _stopRev.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChangeSetTreeRoot other = (ChangeSetTreeRoot) obj;
			if (_filter == null) {
				if (other._filter != null)
					return false;
			} else if (!_filter.equals(other._filter))
				return false;
			if (_kb == null) {
				if (other._kb != null)
					return false;
			} else if (!_kb.equals(other._kb))
				return false;
			if (_showSystemObjects != other._showSystemObjects)
				return false;
			if (_startRev == null) {
				if (other._startRev != null)
					return false;
			} else if (!_startRev.equals(other._startRev))
				return false;
			if (_stopRev == null) {
				if (other._stopRev != null)
					return false;
			} else if (!_stopRev.equals(other._stopRev))
				return false;
			return true;
		}

	}

	/**
	 * Representation of a single attribute value change in the change set tree.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ChangeEntry {

		private final String _attribute;

		private final ResKey _attributeI18N;

		private final Object _oldValue;

		private final Object _newValue;

		/**
		 * Creates a new {@link ChangeEntry}.
		 * 
		 * @param attribute
		 *        See {@link #getAttribute()}.
		 * @param attributeI18N
		 *        See {@link #getAttributeI18N()}.
		 * @param oldValue
		 *        See {@link #getOldValue()}.
		 * @param newValue
		 *        See {@link #getNewValue()}.
		 */
		public ChangeEntry(String attribute, ResKey attributeI18N, Object oldValue, Object newValue) {
			_attribute = attribute;
			_attributeI18N = attributeI18N;
			_oldValue = oldValue;
			_newValue = newValue;
		}

		/**
		 * The {@link ResKey} to use for translation of the attribute name.
		 * 
		 * @return I18N key to translate attribute. May be <code>null</code>.
		 */
		public ResKey getAttributeI18N() {
			return _attributeI18N;
		}

		/**
		 * Name of the changed attribute.
		 */
		public String getAttribute() {
			return _attribute;
		}

		/**
		 * Value of the {@link ChangeEntry} after the change.
		 */
		public Object getNewValue() {
			return _newValue;
		}

		/**
		 * Value of the {@link ChangeEntry} before the change.
		 */
		public Object getOldValue() {
			return _oldValue;
		}

	}



}

