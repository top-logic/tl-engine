/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.layer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * Container for segments, specific {@link LayoutEdge}, see {@link LayoutEdge#isSegment()}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class SegmentContainer {
	private List<LayoutEdge> _segments;

	private SegmentContainer _root;

	private SegmentContainer _parent;

	@Override
	public String toString() {
		return "SegmentContainer [_segments=" + _segments + ", _root=" + _root + ", _parent=" + _parent + "]";
	}

	/**
	 * Creates an empty container.
	 */
	public SegmentContainer() {
		this(Collections.emptyList());
	}

	/**
	 * Creates a copy for the given container.
	 */
	public SegmentContainer(SegmentContainer container) {
		this(container.getSegments());
	}

	/**
	 * Creates a container containing the given segments.
	 */
	public SegmentContainer(List<LayoutEdge> segments) {
		this(segments, null, null);
	}

	/**
	 * Creates a container containing the given segments.
	 */
	public SegmentContainer(List<LayoutEdge> segments, SegmentContainer root, SegmentContainer parent) {
		_segments = new LinkedList<>(segments);
		_root = root;
		_parent = parent;
	}

	/**
	 * Add segment at the end of this container.
	 * 
	 * @param segment
	 *        Segment to be added.
	 */
	public void add(LayoutEdge segment) {
		_segments.add(segment);
	}

	/**
	 * Adds the given {@code segments}.
	 */
	public void addAll(List<LayoutEdge> segments) {
		_segments.addAll(segments);
	}

	/**
	 * Join with the given {@link SegmentContainer}. Merge the given segment sets.
	 */
	public void join(SegmentContainer container) {
		addAll(container.getSegments());
	}

	/**
	 * Split container at the given segment into two new containers. All segments less than the
	 * given one are stored in the first container and those who are greater than the given segment
	 * are stored in the second part. The given segment is neither in the first or second part of
	 * the returned pair.
	 * 
	 * @param segment
	 *        Segment where the {@link SegmentContainer} is splitted.
	 * @return Pair containing in the first part segments strict less and in the second part strict
	 *         greater than the given segment.
	 */
	public Pair<SegmentContainer, SegmentContainer> splitAt(LayoutEdge segment) {
		int segmentPosition = _segments.indexOf(segment);

		if (segmentPosition != -1) {
			SegmentContainer firstSegmentContainer = getSubSegmentContainer(0, segmentPosition);
			SegmentContainer secondSegmentContainer = getSubSegmentContainer(segmentPosition + 1, _segments.size());

			return new Pair<>(firstSegmentContainer, secondSegmentContainer);
		} else {
			return null;
		}
	}

	/**
	 * Split container at the given position.
	 * 
	 * @param position
	 *        Split position.
	 * @return The first part contains the first position segments and the second part contains the
	 *         remainder.
	 */
	public Pair<SegmentContainer, SegmentContainer> splitAt(int position) {
		if (position >= size() || position < 0) {
			return null;
		} else {
			SegmentContainer firstSegmentContainer = getSubSegmentContainer(0, position);
			SegmentContainer secondSegmentContainer = getSubSegmentContainer(position, _segments.size());

			return new Pair<>(firstSegmentContainer, secondSegmentContainer);
		}
	}

	private SegmentContainer getSubSegmentContainer(int startIndex, int endIndex) {
		if (startIndex == endIndex) {
			return new SegmentContainer();
		} else if (startIndex < 0 || endIndex > size() || startIndex > endIndex) {
			return null;
		} else {
			List<LayoutEdge> subList = _segments.subList(startIndex, endIndex);

			if (_root != null) {
				return new SegmentContainer(new LinkedList<>(subList), _root, this);
			} else {
				return new SegmentContainer(new LinkedList<>(subList), this, this);
			}
		}
	}

	/**
	 * All segments in this {@link SegmentContainer}.
	 */
	public List<LayoutEdge> getSegments() {
		return _segments;
	}

	/**
	 * @param segments
	 *        Segments to replace the old.
	 */
	public void setSegments(List<LayoutEdge> segments) {
		_segments = new LinkedList<>(segments);
	}

	/**
	 * First segment in this container if the container is not empty, otherwise null.
	 */
	public LayoutEdge getFirstSegment() {
		if (size() > 1) {
			return _segments.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Last segment in this container if the container is not empty, otherwise null.
	 */
	public LayoutEdge getLastSegment() {
		if (size() > 1) {
			return _segments.get(size() - 1);
		} else {
			return null;
		}
	}

	/**
	 * True if the container contains this segment, otherwise false.
	 */
	public boolean contains(LayoutEdge segment) {
		return _segments.contains(segment);
	}

	/**
	 * Size of this {@link SegmentContainer}.
	 */
	public int size() {
		return _segments.size();
	}

	/**
	 * True if the container contains no segments, otherwise false.
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Parent {@link SegmentContainer} where this one is splitted from.
	 */
	public SegmentContainer getRoot() {
		return _root;
	}

	/**
	 * @see #getRoot()
	 */
	public void setRoot(SegmentContainer root) {
		_root = root;
	}

	/**
	 * {@link LayoutNode} source for each segment.
	 */
	public Collection<LayoutNode> getSegmentSources() {
		List<LayoutNode> segmentSources = new LinkedList<>();

		for (LayoutEdge segment : _segments) {
			segmentSources.add(segment.source());
		}

		return segmentSources;
	}
}
