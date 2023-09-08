/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import static com.top_logic.config.xdiff.xpath.Axis.*;
import static com.top_logic.config.xdiff.xpath.XPathFactory.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.config.xdiff.model.Document;
import com.top_logic.config.xdiff.model.Element;
import com.top_logic.config.xdiff.model.Node;
import com.top_logic.config.xdiff.model.NodeType;
import com.top_logic.config.xdiff.model.QName;
import com.top_logic.config.xdiff.xpath.Axis;
import com.top_logic.config.xdiff.xpath.LocationPath;
import com.top_logic.config.xdiff.xpath.Step;

/**
 * Updatable position within a {@link Document} represented by a {@link LocationPath}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DocumentPosition {

	final LocationPath _path;

	/**
	 * Creates a {@link DocumentPosition} that points to the root node.
	 */
	public DocumentPosition() {
		_path = locationPath(true);
	}

	/**
	 * Starts traversing child nodes of the {@link #getCurrentPath()}.
	 */
	public ChildrenTraversal childrenTraversal() {
		return new ChildrenTraversal(_path.size());
	}

	/**
	 * The xpath pointing to the current node.
	 */
	public LocationPath getCurrentPath() {
		return _path;
	}

	/**
	 * Traversal of the children of a node pointed to by the surrounding {@link DocumentPosition}.
	 * 
	 * @see #traverseChild(Node)
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public class ChildrenTraversal implements Cloneable {

		private final Step[] _stepsByType;

		private final Map<QName, Step> _stepsByName;

		private final int _initialSize;

		private int _lastElementPosition;

		private boolean _elementSeen;

		ChildrenTraversal(int initialSize) {
			this(initialSize, new Step[NodeType.values().length], new HashMap<>(), initialSize, false);
		}

		private ChildrenTraversal(int initialSize, Step[] stepsByType, Map<QName, Step> stepsByName,
				int lastElementPosition, boolean elementSeen) {
			_initialSize = initialSize;

			_stepsByType = stepsByType;
			_stepsByName = stepsByName;

			_lastElementPosition = lastElementPosition;
			_elementSeen = elementSeen;
		}

		@Override
		public ChildrenTraversal clone() {
			return new ChildrenTraversal(
				_initialSize, _stepsByType.clone(), cloneSteps(_stepsByName), _lastElementPosition, _elementSeen);
		}

		private Map<QName, Step> cloneSteps(Map<QName, Step> stepsByName) {
			HashMap<QName, Step> result = new HashMap<>(stepsByName);
			for (Entry<QName, Step> entry : result.entrySet()) {
				Step value = entry.getValue();
				Step clonedValue = value.clone();
				entry.setValue(clonedValue);
			}
			return result;
		}

		/**
		 * Moves the {@link DocumentPosition} to the next child of this {@link ChildrenTraversal}.
		 * 
		 * @param child
		 *        The next child of this {@link ChildrenTraversal}.
		 */
		public void traverseChild(Node child) {
			updatePath(true, child);
		}

		/**
		 * Moves the {@link DocumentPosition} to the given child without updating this
		 * {@link ChildrenTraversal}.
		 * 
		 * @param child
		 *        The next child of this {@link ChildrenTraversal}.
		 */
		public void lookAtChild(Node child) {
			updatePath(false, child);
		}

		private void updatePath(boolean updateSelf, Node child) {
			NodeType type = child.getNodeType();
			Step nextStep;
			if (type == NodeType.ELEMENT) {
				QName elementName = ((Element) child).getQName();
				nextStep = _stepsByName.get(elementName);
				if (nextStep == null) {
					nextStep = step(CHILD, nameTest(elementName), 1);
				} else {
					nextStep = incStep(nextStep);
				}
				_path.reset(_initialSize);
				_path.addStep(nextStep);

				if (updateSelf) {
					_stepsByName.put(elementName, nextStep);
					setElementPosition(_path.size());
					Arrays.fill(_stepsByType, null);
				}
			} else {
				int typeIndex = type.ordinal();
				nextStep = _stepsByType[typeIndex];
				if (nextStep == null) {
					Axis axis;
					if (isElementSeen()) {
						axis = FOLLOWING_SIBLING;
					} else {
						axis = CHILD;
					}
					nextStep = step(axis, typeTest(type), 1);
				} else {
					nextStep = incStep(nextStep);
				}
				_path.reset(getLastElementPosition());
				_path.addStep(nextStep);

				if (updateSelf) {
					_stepsByType[typeIndex] = nextStep;
				}
			}
		}

		private void setElementPosition(int position) {
			_lastElementPosition = position;
			_elementSeen = true;
		}

		private boolean isElementSeen() {
			return _elementSeen;
		}

		private int getLastElementPosition() {
			return _lastElementPosition;
		}

		/**
		 * Ascends to the parent.
		 */
		public void stop() {
			_path.reset(_initialSize);
		}
	}

	static Step incStep(Step nextStep) {
		return step(nextStep.getAxis(), nextStep.getNodeTest(), nextStep.getPosition() + 1);
	}

	/**
	 * XPath expression pointing to the current node of this {@link DocumentPosition}.
	 */
	public String getXPath() {
		return _path.toString();
	}

}