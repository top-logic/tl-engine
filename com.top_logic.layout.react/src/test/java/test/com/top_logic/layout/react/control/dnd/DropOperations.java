/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.dnd;

import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.dnd.AcceptedKinds;
import com.top_logic.layout.react.control.dnd.DropEvent;
import com.top_logic.layout.react.control.dnd.DropLocation;
import com.top_logic.layout.react.control.dnd.DropMode;
import com.top_logic.layout.react.control.dnd.DropRequest;
import com.top_logic.layout.react.control.dnd.DropTarget;
import com.top_logic.layout.react.control.dnd.DropVerdict;

/**
 * A {@link DropTarget} of several {@link Operation}s for tests of a control accepting drops, tried
 * in their order: the first one finding a location for its mode and not refusing it wins.
 */
public final class DropOperations implements DropTarget {

	/** The reason a drop is refused with where no operation names its own. */
	public static final ResKey REFUSAL = ResKey.text("Refused.");

	/**
	 * One operation of a {@link DropOperations} target: a mode and the reason it refuses a location
	 * with ({@code null} to accept); remembers the drops it applied.
	 */
	public static final class Operation {

		private final DropMode _mode;

		private final Function<DropLocation, ResKey> _refusal;

		private final List<DropEvent> _applied = new ArrayList<>();

		/**
		 * Creates an {@link Operation} refusing the locations the given function names a reason
		 * for.
		 */
		public Operation(DropMode mode, Function<DropLocation, ResKey> refusal) {
			_mode = mode;
			_refusal = refusal;
		}

		/**
		 * Creates an {@link Operation} accepting every location of its mode.
		 */
		public Operation(DropMode mode) {
			this(mode, location -> null);
		}

		/**
		 * The drops this operation applied, in the order they were applied.
		 */
		public List<DropEvent> applied() {
			return _applied;
		}

		/**
		 * The location of the drop applied last.
		 */
		public DropLocation lastLocation() {
			assertFalse("A drop must have been applied.", _applied.isEmpty());
			return _applied.get(_applied.size() - 1).location();
		}

	}

	private final AcceptedKinds _accepted;

	private final List<Operation> _operations;

	/**
	 * Creates a {@link DropOperations} target.
	 *
	 * @param accepted
	 *        The kinds of drag the target accepts.
	 * @param operations
	 *        The operations, in the order they are tried.
	 */
	public DropOperations(AcceptedKinds accepted, Operation... operations) {
		_accepted = accepted;
		_operations = List.of(operations);
	}

	@Override
	public AcceptedKinds acceptedKinds() {
		return _accepted;
	}

	@Override
	public Set<DropMode> dropModes() {
		Set<DropMode> result = new LinkedHashSet<>();
		for (Operation operation : _operations) {
			result.add(operation._mode);
		}
		return result;
	}

	@Override
	public DropVerdict check(DropRequest request) {
		ResKey first = null;
		for (Operation operation : _operations) {
			DropLocation location = request.location(operation._mode);
			if (location == null) {
				continue;
			}
			ResKey refusal = operation._refusal.apply(location);
			if (refusal == null) {
				return DropVerdict.accepted(location);
			}
			if (first == null) {
				first = refusal;
			}
		}
		return DropVerdict.refused(first != null ? first : REFUSAL);
	}

	@Override
	public void onDrop(DropEvent event) {
		for (Operation operation : _operations) {
			DropLocation location = DropRequest.of(event).location(operation._mode);
			if (location != null && operation._refusal.apply(location) == null) {
				operation._applied.add(event);
				return;
			}
		}
		fail("A drop nothing accepts must not be applied.");
	}

}
