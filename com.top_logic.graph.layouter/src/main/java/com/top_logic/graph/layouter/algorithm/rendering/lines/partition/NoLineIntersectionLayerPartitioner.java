/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.rendering.lines.partition;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import com.top_logic.graph.layouter.algorithm.rendering.lines.IntersectionStatus;
import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1DContainer;

/**
 * Default partitioner for a one dimensional lines.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class NoLineIntersectionLayerPartitioner implements LayerPartitionAlgorithm<Line1DContainer> {

	/**
	 * Singleton instance for {@link NoLineIntersectionLayerPartitioner}.
	 */
	public static final NoLineIntersectionLayerPartitioner INSTANCE = new NoLineIntersectionLayerPartitioner();

	private NoLineIntersectionLayerPartitioner() {
		// Singleton
	}

	@Override
	public ContainerLayerPartition partition(Collection<Line1DContainer> containers) {
		List<Collection<Line1DContainer>> partition = new LinkedList<>();

		for (Line1DContainer container : containers) {
			insert(container, partition);
		}

		return new ContainerLayerPartition(partition);
	}

	private void insert(Line1DContainer container, List<Collection<Line1DContainer>> partition) {
		int insertIndex = partition.size();

		for (int i = partition.size() - 1; i >= 0; i--) {
			Collection<Line1DContainer> insertedContainers = partition.get(i);

			IntersectionStatus insertStatus = checkIntersection(container, insertedContainers);

			if (insertStatus.equals(IntersectionStatus.PARTIAL)) {
				break;
			} else if (insertStatus.equals(IntersectionStatus.NONE)) {
				insertIndex = i;
			} else if (insertStatus.equals(IntersectionStatus.FULL)) {
				if (isAboveSortable(container, insertedContainers)) {
					break;
				}
			}
		}

		if (insertIndex == partition.size()) {
			partition.add(new LinkedHashSet<>(Arrays.asList(container)));
		} else {
			partition.get(insertIndex).add(container);
		}
	}

	private boolean isAboveSortable(Line1DContainer container, Collection<Line1DContainer> insertedContainers) {
		return insertedContainers.stream()
			.filter(insertedContainer -> container.getPriority() < insertedContainer.getPriority())
			.findAny()
			.isPresent();
	}

	private IntersectionStatus checkIntersection(Line1DContainer container, Collection<Line1DContainer> insertedContainers) {
		IntersectionStatus currentStatus = IntersectionStatus.NONE;

		for (Line1DContainer insertedContainer : insertedContainers) {
			IntersectionStatus insertStatus = getIntersectionStatus(container, insertedContainer);

			if (insertStatus.equals(IntersectionStatus.PARTIAL)) {
				return IntersectionStatus.PARTIAL;
			} else if (insertStatus.equals(IntersectionStatus.FULL)) {
				currentStatus = IntersectionStatus.FULL;
			}
		}

		return currentStatus;
	}

	private IntersectionStatus getIntersectionStatus(Line1DContainer container, Line1DContainer insertedContainer) {
		return container.checkIntersection(insertedContainer);
	}

}
