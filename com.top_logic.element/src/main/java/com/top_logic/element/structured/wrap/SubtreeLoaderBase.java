/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.wrap;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.TreeView;
import com.top_logic.element.meta.kbbased.MetaElementPreload;
import com.top_logic.knowledge.service.db2.FlexAttributeFetch;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.export.PreloadOperation;
import com.top_logic.model.export.Preloader;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.CompatibilityService;

/**
 * Utility to preload structures defined by a {@link TreeView} with all relevant information such as
 * types, flex data, and association caches.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SubtreeLoaderBase<N extends TLObject> implements Closeable {

	/**
	 * Marker to set that the complete subtree of a root node should be loaded.
	 * 
	 * @see #loadTree(TLObject, int)
	 */
	public static final int INFINITE_DEPTH = Integer.MAX_VALUE;

	private final ArrayList<N> _loadedNodes = new ArrayList<>();

	private final PreloadContext _innerContext;

	private final TreeView<N> _tree;

	/**
	 * Creates a new {@link SubtreeLoader}. The loaded items are kept until {@link #close()} is
	 * called.
	 */
	public SubtreeLoaderBase(TreeView<N> tree) {
		this(tree, new PreloadContext());
	}

	/**
	 * Creates a new {@link SubtreeLoader}.
	 * 
	 * @param tree
	 *        The definition of the tree to load.
	 * @param context
	 *        The inner {@link PreloadContext} to keep items.
	 */
	public SubtreeLoaderBase(TreeView<N> tree, PreloadContext context) {
		_tree = tree;
		_innerContext = context;
	}

	@Override
	public void close() {
		_innerContext.close();
		clearLoadedNodes();
	}

	/**
	 * Releases the nodes that were loaded by this {@link SubtreeLoader}.
	 * 
	 * <p>
	 * <b>Note:</b> as the nodes are also kept by the inner {@link PreloadContext}. The items are
	 * not actually released until {@link #close()} is called. This method is just useful if
	 * {@link SubtreeLoader} is created with an external {@link PreloadContext}.
	 * </p>
	 */
	public void clearLoadedNodes() {
		_loadedNodes.clear();
	}

	/**
	 * Returns all nodes that were loaded by this {@link SubtreeLoader}.
	 */
	public List<N> getLoadedNodes() {
		return _loadedNodes;
	}

	/**
	 * Loads the subtree starting with the given node.
	 * 
	 * @param root
	 *        Root node of the subtree to load.
	 * 
	 * @see SubtreeLoader#INFINITE_DEPTH
	 */
	public void loadTree(N root) {
		loadTree(root, INFINITE_DEPTH);
	}

	/**
	 * Loads the subtree starting with the given node with given depth.
	 * 
	 * @param root
	 *        Root node of the subtree to load.
	 * @param depth
	 *        Depth of the tree to load. <code>0</code> means no child loading, <code>1</code> means
	 *        loading of the root and the direct children, <code>2</code> means loading of the given
	 *        root, the children and the grandchildren, and so on.
	 */
	public void loadTree(N root, int depth) {
		switch (depth) {
			case 0:
				add(root);
				break;
			case 1:
				add(root);
				addAll(children(root));
				break;
			default:
				if (depth < 0) {
					throw new IllegalArgumentException("Depth must not be negative: " + depth);
				}
				Collection<? extends N> directChildren = children(root);
				List<N> parents = new ArrayList<>(directChildren);
				add(root);
				addAll(directChildren);
				List<N> nextParents = new ArrayList<>(directChildren.size());
				int i = 1;
				while (i < depth && !parents.isEmpty()) {
					StructuredElementWrapper.preloadChildren(_innerContext, parents);
					for (N parent : parents) {
						nextParents.addAll(children(parent));
					}

					List<N> tmp = parents;
					addAll(nextParents);
					tmp.clear();

					parents = nextParents;
					i++;
					nextParents = tmp;
				}
		}
	}

	private Collection<? extends N> children(N parent) {
		return CollectionUtil.toList(_tree.getChildIterator(parent));
	}

	private void addAll(Collection<? extends N> items) {
		_loadedNodes.addAll(items);
		for (Object item : items) {
			_innerContext.keepObject(item);
		}
	}

	private void add(N item) {
		_loadedNodes.add(item);
		_innerContext.keepObject(item);
	}

	/**
	 * Load the types and attributes, flex data and caches of the formerly loaded subtree.
	 */
	public void loadValues() {
		MetaElementPreload.INSTANCE.prepare(_innerContext, _loadedNodes);
		FlexAttributeFetch.INSTANCE.prepare(_innerContext, _loadedNodes);

		Partition<TLClass, TLObject> typePartition = new Partition<>();
		for (TLObject node : _loadedNodes) {
			typePartition.add((TLClass) node.tType(), node);
		}

		FlexAttributeFetch.INSTANCE.prepare(_innerContext, allAttributes(typePartition.keySet()));

		doAttributePreload(typePartition);
	}

	private void doAttributePreload(Partition<TLClass, TLObject> typePartition) {
		for (Entry<TLClass, List<TLObject>> entry : typePartition.entrySet()) {
			List<TLObject> uniformNodes = entry.getValue();
			if (uniformNodes.size() < 5) {
				continue;
			}

			Preloader preloader = createPreloader(entry.getKey());
			preloader.prepare(_innerContext, uniformNodes);
		}
	}

	private static Collection<TLStructuredTypePart> allAttributes(Set<TLClass> classes) {
		return fetchLocalAttributes(reflexiveTransitiveSuperTypes(classes));
	}

	private static List<TLStructuredTypePart> fetchLocalAttributes(Set<TLClass> types) {
		ArrayList<TLStructuredTypePart> result = new ArrayList<>();
		for (TLClass type : types) {
			result.addAll(type.getLocalParts());
		}
		return result;
	}

	private static Set<TLClass> reflexiveTransitiveSuperTypes(Collection<TLClass> classes) {
		Set<TLClass> result = new HashSet<>();
		TLModelUtil.addReflexiveTransitiveGeneralizations(result, classes);
		return result;
	}

	private static Preloader createPreloader(TLClass type) {
		Preloader preloader = new Preloader() {
			@Override
			public void addPreload(PreloadOperation operation) {
				if (operation == FlexAttributeFetch.INSTANCE) {
					// Done for all independent of type.
					return;
				}
				super.addPreload(operation);
			}
		};
		for (TLStructuredTypePart part : type.getAllClassParts()) {
			addPreload(preloader, part);
		}
		return preloader;
	}

	private static void addPreload(Preloader preloader, TLStructuredTypePart part) {
		CompatibilityService.getInstance().preloadContribution(part).contribute(preloader);
	}

	/**
	 * Utility to partition objects.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static final class Partition<K, V> extends HashMap<K, List<V>> {

		public Partition() {
		}

		public void add(K key, V value) {
			List<V> equivalents = get(key);
			if (equivalents == null) {
				equivalents = new ArrayList<>();
				put(key, equivalents);
			}
			equivalents.add(value);
		}

	}

}
