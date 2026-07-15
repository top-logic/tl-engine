package com.top_logic.react.flow.data;

/**
 * A container that carries multiple rendering variants and renders the one that best fits
 * the available rendering space.
 *
 * <p>
 * Variants are declared from richest to most compact. During intrinsic sizing, each variant
 * is measured; the first whose intrinsic width fits into the available width (and which
 * satisfies its optional extra gates) is selected. The last variant should be a universal
 * fallback that always fits.
 * </p>
 */
public interface LOD extends com.top_logic.react.flow.data.Box, com.top_logic.react.flow.operations.LODOperations {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.LOD} instance.
	 */
	static com.top_logic.react.flow.data.LOD create() {
		return new com.top_logic.react.flow.data.impl.LOD_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.LOD} type in JSON format. */
	String LOD__TYPE = "LOD";

	/** @see #getVariants() */
	String VARIANTS__PROP = "variants";

	/** @see #getFixedHeight() */
	String FIXED_HEIGHT__PROP = "fixedHeight";

	/** @see #getChosenIndex() */
	String CHOSEN_INDEX__PROP = "chosenIndex";

	/**
	 * Rendering variants from richest to most compact. The last entry serves as fallback.
	 */
	java.util.List<com.top_logic.react.flow.data.LODVariant> getVariants();

	/**
	 * @see #getVariants()
	 */
	com.top_logic.react.flow.data.LOD setVariants(java.util.List<? extends com.top_logic.react.flow.data.LODVariant> value);

	/**
	 * Adds a value to the {@link #getVariants()} list.
	 */
	com.top_logic.react.flow.data.LOD addVariant(com.top_logic.react.flow.data.LODVariant value);

	/**
	 * Removes a value from the {@link #getVariants()} list.
	 */
	void removeVariant(com.top_logic.react.flow.data.LODVariant value);

	/**
	 * If set to a positive value, the LOD reports this height as its intrinsic height
	 * regardless of the chosen variant. Avoids row-height jitter when zooming between
	 * variants of different intrinsic height.
	 */
	double getFixedHeight();

	/**
	 * @see #getFixedHeight()
	 */
	com.top_logic.react.flow.data.LOD setFixedHeight(double value);

	/**
	 * The index of the variant chosen during the last intrinsic-sizing pass.
	 */
	int getChosenIndex();

	/**
	 * @see #getChosenIndex()
	 */
	com.top_logic.react.flow.data.LOD setChosenIndex(int value);

	@Override
	com.top_logic.react.flow.data.LOD setX(double value);

	@Override
	com.top_logic.react.flow.data.LOD setY(double value);

	@Override
	com.top_logic.react.flow.data.LOD setWidth(double value);

	@Override
	com.top_logic.react.flow.data.LOD setHeight(double value);

	@Override
	com.top_logic.react.flow.data.LOD setCssClass(String value);

	@Override
	com.top_logic.react.flow.data.LOD setUserObject(java.lang.Object value);

	@Override
	com.top_logic.react.flow.data.LOD setClientId(String value);

	@Override
	com.top_logic.react.flow.data.LOD setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.LOD readLOD(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.LOD) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert LOD__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.LOD_Impl result = new com.top_logic.react.flow.data.impl.LOD_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default LOD self() {
		return this;
	}

}
