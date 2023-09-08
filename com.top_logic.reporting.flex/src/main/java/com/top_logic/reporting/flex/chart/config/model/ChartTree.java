/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.jfree.data.general.Dataset;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.col.InlineMap;
import com.top_logic.basic.col.LazyTypedAnnotatableMixin;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperNameComparator;
import com.top_logic.layout.tree.model.AbstractTreeModel;
import com.top_logic.model.TLObject;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder.StandardLabelProvider;
import com.top_logic.reporting.flex.chart.config.partition.Criterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.MergeableCriterion;

/**
 * This is the universal model for charts. Unlike a {@link Dataset} this model contains
 * context-information like the objects that lead to the resulting chart.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class ChartTree extends AbstractTreeModel<ChartNode> implements LazyTypedAnnotatableMixin {

	/**
	 * Marker interface for keys that can be used in a {@link ChartTree} to identify
	 * {@link ChartNode}s.
	 */
	public interface DataKey {
		// marker interface
	}

	/**
	 * Base-implementation for {@link DataKey}
	 */
	public static abstract class AbstractDataKey<F, S> implements DataKey, Comparable<AbstractDataKey<F, S>> {

		private final Pair<F, S> _pair;

		/**
		 * Create a new {@link AbstractDataKey} for the given Keys.
		 * 
		 * @param key1
		 *        first aspect of the key, in JFreeChart this is usually the row-key.
		 * @param key2
		 *        second aspect of the key, in JFreeChart this is usually the column-key.
		 */
		public AbstractDataKey(F key1, S key2) {
			_pair = new Pair<>(key1, key2);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof AbstractDataKey)) {
				return false;
			}
			return _pair.equals(((AbstractDataKey<?, ?>) obj)._pair);
		}

		@Override
		public int hashCode() {
			return _pair.hashCode();
		}

		@Override
		public String toString() {
			return toString(_pair.getFirst()) + " - " + toString(_pair.getSecond());
		}

		private String toString(Object obj) {
			if (obj instanceof UniqueName && ((UniqueName) obj).getKey() instanceof Pair) {
				return new StandardLabelProvider().getLabel(((UniqueName) obj).getKey());
			}
			return String.valueOf(obj);
		}

		@Override
		public int compareTo(AbstractDataKey<F, S> o) {
			return _pair.compareTo(o._pair);
		}

		/**
		 * first aspect of the key, in JFreeChart this is usually the row-key.
		 */
		protected F getFirst() {
			return _pair.getFirst();
		}

		/**
		 * second aspect of the key, in JFreeChart this is usually the column-key.
		 */
		protected S getSecond() {
			return _pair.getSecond();
		}

	}

	@Inspectable
	private ChartNode _root;

	private final List<Integer> _childSize;

	private final List<Criterion> _criteria;

	private HashMap<String, ChartNode> _nodeMap;

	@Inspectable
	private InlineMap<Property<?>, Object> _properties = InlineMap.empty();

	private BidiMap<DataKey, String> _nodeKeyMap;

	/**
	 * Creates a new {@link ChartTree} with the given node as root.
	 * 
	 * @param root
	 *        the root-node in the new tree
	 */
	public ChartTree(ChartNode root) {
		_root = root;
		_childSize = new ArrayList<>();
		_criteria = new ArrayList<>();
		_nodeMap = new HashMap<>();
		_nodeKeyMap = new BidiHashMap<>();
	}

	@Override
	public Object getBusinessObject(ChartNode node) {
		return node.getObjects();
	}

	@Override
	public ChartNode getRoot() {
		return _root;
	}

	String registerNewNode(ChartNode chartNode) {
		String key = String.valueOf(_nodeMap.size());
		_nodeMap.put(key, chartNode);
		return key;
	}

	/**
	 * Registers an ID for a given {@link DataKey}
	 */
	public void registerDataKey(DataKey dataKey, String id) {
		_nodeKeyMap.put(dataKey, id);
	}

	private String getNodeID(DataKey dataKey) {
		return _nodeKeyMap.get(dataKey);
	}

	/**
	 * @param nodeID
	 *        the ID to get a DataKey for
	 * @return the DataKey for the given ID, null if no such node is registered
	 */
	public DataKey getDataKey(String nodeID) {
		return _nodeKeyMap.getKey(nodeID);
	}

	/**
	 * @param id
	 *        the ID to get a node for
	 * @return the node with the given ID, null if no such node is registered
	 */
	public ChartNode getNode(String id) {
		return _nodeMap.get(id);
	}

	/**
	 * @param key
	 *        the DataKey to get a node for
	 * @return the node identified by the given DataKey, null if no such node is registered
	 */
	public ChartNode getNode(DataKey key) {
		return _nodeMap.get(getNodeID(key));
	}

	@Override
	public List<? extends ChartNode> getChildren(ChartNode parent) {
		return parent.getChildren();
	}

	@Override
	public boolean childrenInitialized(ChartNode parent) {
		return true;
	}

	@Override
	public void resetChildren(ChartNode parent) {
		// Ignore, not lazily initialized.
	}

	@Override
	public boolean isLeaf(ChartNode node) {
		return node.isLeaf();
	}

	@Override
	public ChartNode getParent(ChartNode node) {
		return node.getParent();
	}

	/**
	 * the objects addressed by the keys starting at the root-object using
	 *         {@link ChartNode#getChildByKey(Comparable)}
	 */
	public Collection<?> getObjects(Comparable<?>... path) {
		ChartNode node = getRoot();
		for (Comparable<?> key : path) {
			ChartNode child = node.getChildByKey(key);
			if (child == null) {
				return Collections.EMPTY_LIST;
			}
			node = child;
		}
		return node.getObjects();
	}

	/**
	 * the depth of the tree
	 */
	public int getDepth() {
		return _childSize.size();
	}

	void setChildSize(int level, int size) {
		while (_childSize.size() <= level) {
			_childSize.add(0);
		}
		int newSize = Math.max(_childSize.get(level), size);
		_childSize.set(level, newSize);
	}

	/**
	 * the {@link Criterion} for the requested level
	 */
	public Criterion getCriterion(int level) {
		int index = level - 1;
		if (index < 0 || index >= _criteria.size()) {
			return null;
		}
		return _criteria.get(index);
	}

	/**
	 * the index of the first {@link Criterion} which is instance of given class, may be
	 *         <code>null</code>
	 */
	public int getCriterionIndex(Class<? extends Criterion> clazz) {
		for (int i = 0; i < _criteria.size(); i++) {
			if (clazz.isInstance(_criteria.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * the index of the first {@link Criterion} which is instance of given class and has the
	 *         given detail annotated, may be <code>null</code>
	 */
	public int getCriterionIndex(Class<? extends Criterion> clazz, Object detail) {
		for (int i = 0; i < _criteria.size(); i++) {
			Criterion criteria = _criteria.get(i);
			if (clazz.isInstance(criteria)) {
				if (criteria.getDetails().contains(detail)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * see {@link #getCriterion(int)}
	 */
	public void setCriterion(int level, Criterion criterion) {
		while (_criteria.size() <= level) {
			_criteria.add(null);
		}
		criterion = merge(criterion, _criteria.get(level));
		_criteria.set(level, criterion);
	}

	private Criterion merge(Criterion newCrit, Criterion currentCrit) {
		if (currentCrit == null) {
			return newCrit;
		}
		if (currentCrit instanceof MergeableCriterion) {
			return ((MergeableCriterion) currentCrit).merge(newCrit);
		}
		return currentCrit;
	}

	/**
	 * the objects of the root-node (see {@link ChartNode#getObjects()}
	 */
	public List<Object> getRootObjects() {
		return new ArrayList<>(getRoot().getObjects());
	}

	/**
	 * the objects of the leaf-node (see {@link ChartNode#isLeaf()},
	 *         {@link ChartNode#getObjects()})
	 */
	public List<Object> getLeafObjects() {
		Set<Object> result = new LinkedHashSet<>();
		collectObjects(getRoot(), result);
		return new ArrayList<>(result);
	}

	private void collectObjects(ChartNode node, Set<Object> result) {
		if (node.isLeaf()) {
			List<?> objects = node.getObjects();
			sortNamed((List) objects);
			result.addAll(objects);
		} else {
			for (ChartNode child : node.getChildren()) {
				collectObjects(child, result);
			}
		}
	}

	private void sortNamed(List<? extends TLObject> objects) {
		try {
			if (!objects.isEmpty()) {
				Object first = CollectionUtil.getFirst(objects);
				if (first instanceof Wrapper) {
					Collections.sort(objects, WrapperNameComparator.getInstance());
				}
			}
		} catch (Exception ex) {
			// do nothing
		}
	}

	@Override
	public InlineMap<Property<?>, Object> internalAccessLazyPropertiesStore() {
		return _properties;
	}

	@Override
	public void internalUpdateLazyPropertiesStore(InlineMap<Property<?>, Object> newProperties) {
		_properties = newProperties;
	}

	@Override
	public boolean isFinite() {
		return false;
	}

}
