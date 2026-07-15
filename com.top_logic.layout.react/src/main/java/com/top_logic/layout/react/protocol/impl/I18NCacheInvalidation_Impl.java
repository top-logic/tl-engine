package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.I18NCacheInvalidation}.
 */
public class I18NCacheInvalidation_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.I18NCacheInvalidation {

	/**
	 * Creates a {@link I18NCacheInvalidation_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.I18NCacheInvalidation#create()
	 */
	public I18NCacheInvalidation_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.I_18_NCACHE_INVALIDATION;
	}

	@Override
	public String jsonType() {
		return I_18_NCACHE_INVALIDATION__TYPE;
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
