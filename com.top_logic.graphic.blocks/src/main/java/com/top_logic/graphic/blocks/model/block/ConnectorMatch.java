/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.block;

/**
 * Description of tow potentially matching {@link Connector}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ConnectorMatch {

	/**
	 * No matching connectors.
	 */
	public static final ConnectorMatch NONE = new ConnectorMatch() {
		@Override
		public ConnectorMatch best(ConnectorMatch other) {
			return other;
		}
	
		@Override
		public boolean hasMatch() {
			return false;
		}
	
		@Override
		public Connector getActive() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Connector getOther() {
			throw new UnsupportedOperationException();
		}
	};

	/**
	 * Creates a {@link ConnectorMatch}.
	 * 
	 * @param distance
	 *        The distance of the given {@link Connector}'s {@link Connector#getCenter()} points.
	 * @param active
	 *        The first {@link Connector} of the match, see {@link #getActive()}.
	 * @param other
	 *        The second {@link Connector} of the match.
	 */
	public static ConnectorMatch match(double distance, Connector active, Connector other) {
		return new Impl(distance, active, other);
	}

	/**
	 * The best match (either this one, or the other one).
	 */
	public abstract ConnectorMatch best(ConnectorMatch other);

	/**
	 * Whether there is a potential match.
	 */
	public abstract boolean hasMatch();

	/**
	 * The active connector of the match (if {@link #hasMatch()}).
	 * 
	 * @see #match(double, Connector, Connector)
	 */
	public abstract Connector getActive();

	/**
	 * The other connector of the match (if {@link #hasMatch()}).
	 * 
	 * @see #match(double, Connector, Connector)
	 */
	public abstract Connector getOther();

	private static final class Impl extends ConnectorMatch {
		private double _distance;
	
		private Connector _a;
	
		private Connector _b;
	
		public Impl(double distance, Connector a, Connector b) {
			_distance = distance;
			_a = a;
			_b = b;
		}
	
		@Override
		public boolean hasMatch() {
			return true;
		}
	
		@Override
		public ConnectorMatch best(ConnectorMatch other) {
			if (other == NONE) {
				return this;
			}
			if (_distance < ((Impl) other)._distance) {
				return this;
			} else {
				return other;
			}
		}
	
		@Override
		public Connector getActive() {
			return _a;
		}

		@Override
		public Connector getOther() {
			return _b;
		}
	}

}
