package com.top_logic.react.flow.data;

/**
 * One rendering option of an {@link LOD} box.
 */
public interface LODVariant extends Widget, com.top_logic.react.flow.operations.LODVariantOperations {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.LODVariant} instance.
	 */
	static com.top_logic.react.flow.data.LODVariant create() {
		return new com.top_logic.react.flow.data.impl.LODVariant_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.LODVariant} type in JSON format. */
	String LODVARIANT__TYPE = "LODVariant";

	/** @see #getContent() */
	String CONTENT__PROP = "content";

	/** @see #getMinZoom() */
	String MIN_ZOOM__PROP = "minZoom";

	/** @see #getMinWidth() */
	String MIN_WIDTH__PROP = "minWidth";

	/** @see #getMinHeight() */
	String MIN_HEIGHT__PROP = "minHeight";

	/**
	 * The content rendered when this variant is selected.
	 */
	com.top_logic.react.flow.data.Box getContent();

	/**
	 * @see #getContent()
	 */
	com.top_logic.react.flow.data.LODVariant setContent(com.top_logic.react.flow.data.Box value);

	/**
	 * Checks, whether {@link #getContent()} has a value.
	 */
	boolean hasContent();

	/**
	 * Optional extra gate: minimum render zoom factor required for this variant.
	 * A value of {@code 0} disables the gate.
	 */
	double getMinZoom();

	/**
	 * @see #getMinZoom()
	 */
	com.top_logic.react.flow.data.LODVariant setMinZoom(double value);

	/**
	 * Optional extra lower bound on the available width, independent of intrinsic width.
	 * A value of {@code 0} disables the gate.
	 */
	double getMinWidth();

	/**
	 * @see #getMinWidth()
	 */
	com.top_logic.react.flow.data.LODVariant setMinWidth(double value);

	/**
	 * Optional analogue to {@link #getMinWidth()} for the vertical axis.
	 */
	double getMinHeight();

	/**
	 * @see #getMinHeight()
	 */
	com.top_logic.react.flow.data.LODVariant setMinHeight(double value);

	@Override
	com.top_logic.react.flow.data.LODVariant setCssClass(String value);

	@Override
	com.top_logic.react.flow.data.LODVariant setUserObject(java.lang.Object value);

	@Override
	com.top_logic.react.flow.data.LODVariant setClientId(String value);

	@Override
	com.top_logic.react.flow.data.LODVariant setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.LODVariant readLODVariant(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.LODVariant) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert LODVARIANT__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.LODVariant_Impl result = new com.top_logic.react.flow.data.impl.LODVariant_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default LODVariant self() {
		return this;
	}

}
