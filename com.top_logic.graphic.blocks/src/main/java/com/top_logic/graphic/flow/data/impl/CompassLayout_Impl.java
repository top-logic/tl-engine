package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.CompassLayout}.
 */
public class CompassLayout_Impl extends com.top_logic.graphic.flow.data.impl.Box_Impl implements com.top_logic.graphic.flow.data.CompassLayout {

	private com.top_logic.graphic.flow.data.Box _north = null;

	private com.top_logic.graphic.flow.data.Box _west = null;

	private com.top_logic.graphic.flow.data.Box _east = null;

	private com.top_logic.graphic.flow.data.Box _south = null;

	private com.top_logic.graphic.flow.data.Box _center = null;

	private double _centerHeight = 0.0d;

	private double _hPadding = 0.0d;

	private double _vPadding = 0.0d;

	/**
	 * Creates a {@link CompassLayout_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.CompassLayout#create()
	 */
	public CompassLayout_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.COMPASS_LAYOUT;
	}

	@Override
	public final com.top_logic.graphic.flow.data.Box getNorth() {
		return _north;
	}

	@Override
	public com.top_logic.graphic.flow.data.CompassLayout setNorth(com.top_logic.graphic.flow.data.Box value) {
		internalSetNorth(value);
		return this;
	}

	/** Internal setter for {@link #getNorth()} without chain call utility. */
	protected final void internalSetNorth(com.top_logic.graphic.flow.data.Box value) {
		com.top_logic.graphic.flow.data.impl.Box_Impl before = (com.top_logic.graphic.flow.data.impl.Box_Impl) _north;
		com.top_logic.graphic.flow.data.impl.Box_Impl after = (com.top_logic.graphic.flow.data.impl.Box_Impl) value;
		if (after != null) {
			com.top_logic.graphic.flow.data.Widget oldContainer = after.getParent();
			if (oldContainer != null && oldContainer != this) {
				throw new IllegalStateException("Object may not be part of two different containers.");
			}
		}
		_listener.beforeSet(this, NORTH__PROP, value);
		if (before != null) {
			before.internalSetParent(null);
		}
		_north = value;
		if (after != null) {
			after.internalSetParent(this);
		}
	}

	@Override
	public final boolean hasNorth() {
		return _north != null;
	}

	@Override
	public final com.top_logic.graphic.flow.data.Box getWest() {
		return _west;
	}

	@Override
	public com.top_logic.graphic.flow.data.CompassLayout setWest(com.top_logic.graphic.flow.data.Box value) {
		internalSetWest(value);
		return this;
	}

	/** Internal setter for {@link #getWest()} without chain call utility. */
	protected final void internalSetWest(com.top_logic.graphic.flow.data.Box value) {
		com.top_logic.graphic.flow.data.impl.Box_Impl before = (com.top_logic.graphic.flow.data.impl.Box_Impl) _west;
		com.top_logic.graphic.flow.data.impl.Box_Impl after = (com.top_logic.graphic.flow.data.impl.Box_Impl) value;
		if (after != null) {
			com.top_logic.graphic.flow.data.Widget oldContainer = after.getParent();
			if (oldContainer != null && oldContainer != this) {
				throw new IllegalStateException("Object may not be part of two different containers.");
			}
		}
		_listener.beforeSet(this, WEST__PROP, value);
		if (before != null) {
			before.internalSetParent(null);
		}
		_west = value;
		if (after != null) {
			after.internalSetParent(this);
		}
	}

	@Override
	public final boolean hasWest() {
		return _west != null;
	}

	@Override
	public final com.top_logic.graphic.flow.data.Box getEast() {
		return _east;
	}

	@Override
	public com.top_logic.graphic.flow.data.CompassLayout setEast(com.top_logic.graphic.flow.data.Box value) {
		internalSetEast(value);
		return this;
	}

	/** Internal setter for {@link #getEast()} without chain call utility. */
	protected final void internalSetEast(com.top_logic.graphic.flow.data.Box value) {
		com.top_logic.graphic.flow.data.impl.Box_Impl before = (com.top_logic.graphic.flow.data.impl.Box_Impl) _east;
		com.top_logic.graphic.flow.data.impl.Box_Impl after = (com.top_logic.graphic.flow.data.impl.Box_Impl) value;
		if (after != null) {
			com.top_logic.graphic.flow.data.Widget oldContainer = after.getParent();
			if (oldContainer != null && oldContainer != this) {
				throw new IllegalStateException("Object may not be part of two different containers.");
			}
		}
		_listener.beforeSet(this, EAST__PROP, value);
		if (before != null) {
			before.internalSetParent(null);
		}
		_east = value;
		if (after != null) {
			after.internalSetParent(this);
		}
	}

	@Override
	public final boolean hasEast() {
		return _east != null;
	}

	@Override
	public final com.top_logic.graphic.flow.data.Box getSouth() {
		return _south;
	}

	@Override
	public com.top_logic.graphic.flow.data.CompassLayout setSouth(com.top_logic.graphic.flow.data.Box value) {
		internalSetSouth(value);
		return this;
	}

	/** Internal setter for {@link #getSouth()} without chain call utility. */
	protected final void internalSetSouth(com.top_logic.graphic.flow.data.Box value) {
		com.top_logic.graphic.flow.data.impl.Box_Impl before = (com.top_logic.graphic.flow.data.impl.Box_Impl) _south;
		com.top_logic.graphic.flow.data.impl.Box_Impl after = (com.top_logic.graphic.flow.data.impl.Box_Impl) value;
		if (after != null) {
			com.top_logic.graphic.flow.data.Widget oldContainer = after.getParent();
			if (oldContainer != null && oldContainer != this) {
				throw new IllegalStateException("Object may not be part of two different containers.");
			}
		}
		_listener.beforeSet(this, SOUTH__PROP, value);
		if (before != null) {
			before.internalSetParent(null);
		}
		_south = value;
		if (after != null) {
			after.internalSetParent(this);
		}
	}

	@Override
	public final boolean hasSouth() {
		return _south != null;
	}

	@Override
	public final com.top_logic.graphic.flow.data.Box getCenter() {
		return _center;
	}

	@Override
	public com.top_logic.graphic.flow.data.CompassLayout setCenter(com.top_logic.graphic.flow.data.Box value) {
		internalSetCenter(value);
		return this;
	}

	/** Internal setter for {@link #getCenter()} without chain call utility. */
	protected final void internalSetCenter(com.top_logic.graphic.flow.data.Box value) {
		com.top_logic.graphic.flow.data.impl.Box_Impl before = (com.top_logic.graphic.flow.data.impl.Box_Impl) _center;
		com.top_logic.graphic.flow.data.impl.Box_Impl after = (com.top_logic.graphic.flow.data.impl.Box_Impl) value;
		if (after != null) {
			com.top_logic.graphic.flow.data.Widget oldContainer = after.getParent();
			if (oldContainer != null && oldContainer != this) {
				throw new IllegalStateException("Object may not be part of two different containers.");
			}
		}
		_listener.beforeSet(this, CENTER__PROP, value);
		if (before != null) {
			before.internalSetParent(null);
		}
		_center = value;
		if (after != null) {
			after.internalSetParent(this);
		}
	}

	@Override
	public final boolean hasCenter() {
		return _center != null;
	}

	@Override
	public final double getCenterHeight() {
		return _centerHeight;
	}

	@Override
	public com.top_logic.graphic.flow.data.CompassLayout setCenterHeight(double value) {
		internalSetCenterHeight(value);
		return this;
	}

	/** Internal setter for {@link #getCenterHeight()} without chain call utility. */
	protected final void internalSetCenterHeight(double value) {
		_listener.beforeSet(this, CENTER_HEIGHT__PROP, value);
		_centerHeight = value;
	}

	@Override
	public final double getHPadding() {
		return _hPadding;
	}

	@Override
	public com.top_logic.graphic.flow.data.CompassLayout setHPadding(double value) {
		internalSetHPadding(value);
		return this;
	}

	/** Internal setter for {@link #getHPadding()} without chain call utility. */
	protected final void internalSetHPadding(double value) {
		_listener.beforeSet(this, H_PADDING__PROP, value);
		_hPadding = value;
	}

	@Override
	public final double getVPadding() {
		return _vPadding;
	}

	@Override
	public com.top_logic.graphic.flow.data.CompassLayout setVPadding(double value) {
		internalSetVPadding(value);
		return this;
	}

	/** Internal setter for {@link #getVPadding()} without chain call utility. */
	protected final void internalSetVPadding(double value) {
		_listener.beforeSet(this, V_PADDING__PROP, value);
		_vPadding = value;
	}

	@Override
	public com.top_logic.graphic.flow.data.CompassLayout setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.CompassLayout setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.CompassLayout setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.CompassLayout setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.CompassLayout setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public String jsonType() {
		return COMPASS_LAYOUT__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			NORTH__PROP, 
			WEST__PROP, 
			EAST__PROP, 
			SOUTH__PROP, 
			CENTER__PROP, 
			CENTER_HEIGHT__PROP, 
			H_PADDING__PROP, 
			V_PADDING__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case NORTH__PROP: return getNorth();
			case WEST__PROP: return getWest();
			case EAST__PROP: return getEast();
			case SOUTH__PROP: return getSouth();
			case CENTER__PROP: return getCenter();
			case CENTER_HEIGHT__PROP: return getCenterHeight();
			case H_PADDING__PROP: return getHPadding();
			case V_PADDING__PROP: return getVPadding();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NORTH__PROP: internalSetNorth((com.top_logic.graphic.flow.data.Box) value); break;
			case WEST__PROP: internalSetWest((com.top_logic.graphic.flow.data.Box) value); break;
			case EAST__PROP: internalSetEast((com.top_logic.graphic.flow.data.Box) value); break;
			case SOUTH__PROP: internalSetSouth((com.top_logic.graphic.flow.data.Box) value); break;
			case CENTER__PROP: internalSetCenter((com.top_logic.graphic.flow.data.Box) value); break;
			case CENTER_HEIGHT__PROP: internalSetCenterHeight((double) value); break;
			case H_PADDING__PROP: internalSetHPadding((double) value); break;
			case V_PADDING__PROP: internalSetVPadding((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		if (hasNorth()) {
			out.name(NORTH__PROP);
			getNorth().writeTo(scope, out);
		}
		if (hasWest()) {
			out.name(WEST__PROP);
			getWest().writeTo(scope, out);
		}
		if (hasEast()) {
			out.name(EAST__PROP);
			getEast().writeTo(scope, out);
		}
		if (hasSouth()) {
			out.name(SOUTH__PROP);
			getSouth().writeTo(scope, out);
		}
		if (hasCenter()) {
			out.name(CENTER__PROP);
			getCenter().writeTo(scope, out);
		}
		out.name(CENTER_HEIGHT__PROP);
		out.value(getCenterHeight());
		out.name(H_PADDING__PROP);
		out.value(getHPadding());
		out.name(V_PADDING__PROP);
		out.value(getVPadding());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case NORTH__PROP: {
				if (hasNorth()) {
					getNorth().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case WEST__PROP: {
				if (hasWest()) {
					getWest().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case EAST__PROP: {
				if (hasEast()) {
					getEast().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case SOUTH__PROP: {
				if (hasSouth()) {
					getSouth().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case CENTER__PROP: {
				if (hasCenter()) {
					getCenter().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case CENTER_HEIGHT__PROP: {
				out.value(getCenterHeight());
				break;
			}
			case H_PADDING__PROP: {
				out.value(getHPadding());
				break;
			}
			case V_PADDING__PROP: {
				out.value(getVPadding());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NORTH__PROP: setNorth(com.top_logic.graphic.flow.data.Box.readBox(scope, in)); break;
			case WEST__PROP: setWest(com.top_logic.graphic.flow.data.Box.readBox(scope, in)); break;
			case EAST__PROP: setEast(com.top_logic.graphic.flow.data.Box.readBox(scope, in)); break;
			case SOUTH__PROP: setSouth(com.top_logic.graphic.flow.data.Box.readBox(scope, in)); break;
			case CENTER__PROP: setCenter(com.top_logic.graphic.flow.data.Box.readBox(scope, in)); break;
			case CENTER_HEIGHT__PROP: setCenterHeight(in.nextDouble()); break;
			case H_PADDING__PROP: setHPadding(in.nextDouble()); break;
			case V_PADDING__PROP: setVPadding(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
