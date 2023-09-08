/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.compare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.top_logic.config.xdiff.model.Node;

/**
 * Algorithm that computes the minimum distance in a weighted edit graph of {@link Node}s.
 * 
 * <p>
 * The computed edit script consists of a number of {@link EditOperation}s and transforms a source
 * node list into a target node list.
 * </p>
 * 
 * @see #compute(List, List)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MinimumEditPath {

	final class Position implements Comparable<Position> {

		final int _x;

		final int _y;
		
		int _distanceFromStart;

		int _minimumTotal;

		Position _predecessor;

		public Position(Position predecessor, int x, int y, int distanceFromStart) {
			_x = x;
			_y = y;
			_distanceFromStart = distanceFromStart;
			
			_minimumTotal = distanceFromStart + getEstimatedCompletionDistance();
			_predecessor = predecessor;
		}

		private int getEstimatedCompletionDistance() {
			int currentDiagonal = getDiagonal();
			
			if (_targetDiagonal > currentDiagonal) {
				// Deletion necessary to reach target diagonal.
				return (_targetDiagonal - currentDiagonal) * _minOldWeight;
			} else {
				// Insertion necessary to reach target diagonal.
				return (currentDiagonal - _targetDiagonal) * _minNewWeight;
			}
		}

		private int getDiagonal() {
			return _x - _y;
		}

		@Override
		public int compareTo(Position other) {
			return _minimumTotal - other._minimumTotal;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (! (obj instanceof Position)) {
				return false;
			}
			
			return equalsNode((Position) obj);
		}

		private boolean equalsNode(Position other) {
			return (_x == other._x) && (_y == other._y);
		}

		@Override
		public int hashCode() {
			return 75967 * _x + _y;
		}

		protected boolean canDelete() {
			return _x < _N;
		}

		protected boolean canInsert() {
			return _y < _M;
		}

		protected Node deleteOption() {
			return _oldNodes.get(_x);
		}

		protected Node insertOption() {
			return _newNodes.get(_y);
		}

		void step(int dx, int dy, int stepCost) {
			int newDistance = _distanceFromStart + stepCost;
			Position next = new Position(this, _x + dx, _y + dy, newDistance);
			
			Position existing = _visited.get(next);
			if (existing != null) {
				if (existing._distanceFromStart > newDistance) {
					_pending.remove(existing);
					
					int oldDistance = existing._distanceFromStart;
					existing._distanceFromStart = newDistance;
					existing._minimumTotal -= oldDistance - newDistance;
					existing._predecessor = this;
					
					_pending.add(existing);
				}
			} else {
				_pending.add(next);
				_visited.put(next, next);
			}
		}

		public List<EditOperation> getOperations() {
			ArrayList<EditOperation> operations = new ArrayList<>();
			pathStep(operations, null);
			return operations;
		}

		private void pathStep(List<EditOperation> path, Position next) {
			if (_predecessor != null) {
				_predecessor.pathStep(path, this);
			}

			if (next != null) {
				boolean isDeletion = next._x > _x;
				boolean isInsertion = next._y > _y;

				EditOperation operation;
				if (isDeletion) {
					if (isInsertion) {
						operation = EditOperation.MATCH;
					} else {
						operation = EditOperation.DELETE;
					}
				} else {
					operation = EditOperation.INSERT;
				}
				path.add(operation);
			}
		}

	}

	List<Node> _oldNodes;

	List<Node> _newNodes;

	int _N;

	int _M;

	int _minOldWeight;

	int _minNewWeight;

	int _targetDiagonal;

	PriorityQueue<Position> _pending;

	Map<Position, Position> _visited;

	List<Node> _diff;

	private final MatchDecision _matchAlgorithm;

	/**
	 * Creates a {@link MinimumEditPath}.
	 * 
	 * @param matchAlgorithm
	 *        The algorithm that decides, whether nodes can be transformed into each other.
	 */
	public MinimumEditPath(MatchDecision matchAlgorithm) {
		_matchAlgorithm = matchAlgorithm;
	}

	private static int minWeight(List<Node> nodes) {
		int result = nodes.get(0).getWeight();
		for (int index = 1, cnt = nodes.size(); index < cnt; index++) {
			result = Math.min(result, nodes.get(index).getWeight());
		}
		return result;
	}

	/**
	 * Performs the {@link MinimumEditPath} computation.
	 * 
	 * @param oldNodes
	 *        The source node list.
	 * @param newNodes
	 *        The target node list.
	 * @return The edit script.
	 */
	public List<EditOperation> compute(List<Node> oldNodes, List<Node> newNodes) {
		setup(oldNodes, newNodes);
		
		Position bestGuess = minimumPath();

		teardown();

		return bestGuess.getOperations();
	}

	private Position minimumPath() {
		Position bestGuess;
		while (true) {
			bestGuess = _pending.poll();
			
			boolean canDelete = bestGuess.canDelete();
			boolean canInsert = bestGuess.canInsert();

			if (canDelete) {
				bestGuess.step(1, 0, bestGuess.deleteOption().getWeight());
			}
			
			if (canInsert) {
				bestGuess.step(0, 1, bestGuess.insertOption().getWeight());
			}
			
			if (canDelete && canInsert) {
				matchStep(bestGuess, bestGuess.deleteOption(), bestGuess.insertOption());
			}
			
			else if ((!canDelete) && (!canInsert)) {
				// Target is reached.
				break;
			}
		}
		return bestGuess;
	}

	private void setup(List<Node> oldNodes, List<Node> newNodes) {
		_oldNodes = oldNodes;
		_newNodes = newNodes;
		
		_minOldWeight = minWeight(oldNodes);
		_minNewWeight = minWeight(newNodes);
		
		_N = oldNodes.size();
		_M = newNodes.size();

		_targetDiagonal = _N - _M;

		_pending = new PriorityQueue<>();
		_visited = new HashMap<>();
		
		Position start = new Position(null, 0, 0, 0);
		_pending.add(start);
		_visited.put(start, start);
	}

	private void teardown() {
		_oldNodes = null;
		_newNodes = null;
		_pending = null;
		_visited = null;
	}

	private void matchStep(Position bestGuess, Node deleteOption, Node insertOption) {
		if (deleteOption.equals(insertOption)) {
			bestGuess.step(1, 1, 0);
		} else if (_matchAlgorithm.canMatch(deleteOption, insertOption)) {
			int matchCost = matchCost(deleteOption, insertOption);
			
			bestGuess.step(1, 1, matchCost);
		} else {
			// Nodes cannot be matched, no diagonal step possible.
		}
	}

	private int matchCost(Node deleteOption, Node insertOption) {
		int deleteWeight = deleteOption.getWeight();
		int insertWeight = insertOption.getWeight();
		int maxWeight = Math.max(deleteWeight, insertWeight);
		int commonWeight = Math.min(deleteWeight, insertWeight);
		int weightDifference = maxWeight - commonWeight;
		int expectedUpdateCost = (commonWeight + 3) / 4;
		return weightDifference + expectedUpdateCost;
	}

}
