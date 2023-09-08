/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import static com.top_logic.config.xdiff.compare.XDiffSchema.*;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.config.xdiff.compare.XDiff;
import com.top_logic.config.xdiff.model.AbstractNodeVisitor;
import com.top_logic.config.xdiff.model.DocumentBuilder;
import com.top_logic.config.xdiff.model.Element;
import com.top_logic.config.xdiff.model.Fragment;
import com.top_logic.config.xdiff.model.FragmentBase;
import com.top_logic.config.xdiff.model.Node;
import com.top_logic.config.xdiff.model.NodeType;
import com.top_logic.config.xdiff.ms.DocumentPosition.ChildrenTraversal;
import com.top_logic.config.xdiff.ms.NodeAddComputation.AddVisit;

/**
 * Generation of {@link NodeAdd} operations from a {@link XDiff} result.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class NodeAddComputation extends AbstractNodeVisitor<Void, AddVisit> {

	class AddVisit {

		/**
		 * XPath pointing to the parent node referenced in node insertions.
		 */
		private final String _insertParent;

		/**
		 * Buffered {@link NodeAdd} operations that have not yet a {@link NodeAdd#getBeforeXpath()}
		 * assigned.
		 */
		private final List<NodeAdd> _localAdds = new ArrayList<>();

		private final ChildrenTraversal _origTraversal;

		/**
		 * Resulting list of {@link NodeAdd} operations after {@link NodeAdd#getBeforeXpath()} has
		 * been assigned.
		 */
		private final List<MSXDiff> _allAdds;

		private ComplexNodeAdd _pendingAdd;

		public AddVisit(List<MSXDiff> allAdds, String insertParent, ChildrenTraversal origTraversal) {
			_allAdds = allAdds;
			_insertParent = insertParent;
			_origTraversal = origTraversal;
		}

		public String getInsertParent() {
			return _insertParent;
		}

		public void visitInserted(Node node) {
			flushPending(node, true);
			_pendingAdd = new ComplexNodeAdd(_component, getInsertParent(), null, toW3C(node));
		}

		public void visitOrig(Node node) {
			flushPending(node, false);
		}

		public void flush() {
			flushPending(null, false);
			for (int n = _localAdds.size() - 1; n >= 0; n--) {
				NodeAdd add = _localAdds.get(n);

				_allAdds.add(add);
			}
			_localAdds.clear();

			_origTraversal.stop();
		}

		private void flushPending(Node nextSibling, boolean alsoInserted) {
			ComplexNodeAdd pendingAdd = _pendingAdd;
			boolean isPending = pendingAdd != null;
			if (nextSibling != null) {
				if (alsoInserted) {
					if (isPending) {
						_origTraversal.lookAtChild(nextSibling);
					}
				} else {
					_origTraversal.traverseChild(nextSibling);
				}

				if (isPending) {
					addToLocal(_insertPosition.getXPath(), pendingAdd);
				}
			} else {
				if (isPending) {
					addToLocal(null, pendingAdd);
				}
			}
		}

		private void addToLocal(String insertAnchor, ComplexNodeAdd pendingAdd) {
			pendingAdd.setBeforeXpath(insertAnchor);
			_localAdds.add(pendingAdd);
			_pendingAdd = null;
		}

	}

	private static final Void none = null;

	String _component;

	final DocumentPosition _insertPosition = new DocumentPosition();

	private List<MSXDiff> _adds = new ArrayList<>();

	public NodeAddComputation(String component) {
		_component = component;
	}

	public List<MSXDiff> getAdds() {
		return _adds;
	}

	@Override
	public Void visitElement(Element node, AddVisit arg) {
		if (isInsert(node)) {
			for (Node inserted : node.getChildren()) {
				// Text delete and add is realized through node value operations.
				if (inserted.getNodeType() != NodeType.TEXT) {
					arg.visitInserted(inserted);
				}
			}
			return none;
		} else if (isDiff(node)) {
			descend(node, arg);
			return none;
		} else if (isXDiffElement(node)) {
			return none;
		} else {
			Void result = super.visitElement(node, arg);
			return result;
		}
	}

	@Override
	protected Void visitFragmentBase(FragmentBase node, AddVisit arg) {
		Void result = super.visitFragmentBase(node, arg);

		descendNextLevel(node);

		return result;
	}

	@Override
	public Void visitDocument(com.top_logic.config.xdiff.model.Document node, AddVisit arg) {
		descendNextLevel(node);
		return none;
	}

	@Override
	public Void visitFragment(Fragment node, AddVisit arg) {
		descend(node, arg);
		return none;
	}

	private void descendNextLevel(FragmentBase node) {
		ChildrenTraversal childrenTraversal = _insertPosition.childrenTraversal();
		AddVisit childrenVisit = new AddVisit(_adds, _insertPosition.getXPath(), childrenTraversal);
		descend(node, childrenVisit);

		// Flush content potentially inserted at the end of this element contents.
		childrenVisit.flush();
	}

	private void descend(FragmentBase node, AddVisit childrenVisit) {
		for (Node child : node.getChildren()) {
			child.visit(this, childrenVisit);
		}
	}

	@Override
	protected Void visitNode(Node node, AddVisit arg) {
		arg.visitOrig(node);
		return none;
	}

	static org.w3c.dom.Node toW3C(Node inserted) {
		Document document = DOMUtil.newDocument();
		org.w3c.dom.Element fragment = document.createElementNS(null, "fragment");
		DocumentBuilder.convertToDOM(fragment, null, inserted);
		return fragment;
	}

}
