/**
 * Compatibility module to allow TL controls use initializers defined in GWT code.
 */
services.gwt = {

	/**
	 * Buffered initializers from TL controls.
	 */
	initializers: {},
	
	/**
	 * Indirection to register a control initializer to delay its execution until the GWT code has
	 * completed loading.
	 * 
	 * <p>
	 * Since TL controls write their initializer call into scripts embedded into the page, their
	 * execution must be delayed until the GWT code has completed loading. Unfortunately, the page's
	 * load event may fire before the GWT code has been initialized. Control initializers are
	 * therefore buffered until the `onLoad` function is triggered by the GWT module entry script.
	 * </p>
	 */
	executeAfterLoad: function(type, win, init) {
		var initializersForType = this.initializers[type];
		if (initializersForType == null) {
			initializersForType = [];
			this.initializers[type] = initializersForType;
		}
		
		// Defer the passed initializer until the corresponding onLoad call happens.
		initializersForType.push(init);
	},
	
	/**
	 * Called, when the GWT script loading has been completed.
	 */
	onLoad: function(type) {
		var initializersForType = this.initializers[type];
		
		// Call all collected initializers for this specific control type.
		if (initializersForType != null) {
			var cnt = initializersForType.length;
			for (var n = 0; n < cnt; n++) {
				var init = initializersForType[n];
				init();
			}
		}
	
		// Install shortcut that now evaluates all initializers directly.
		this.initializers[type] = {
			// Glorious JS hack: Replace the array with an object "implementing" the push 
			// method by simply calling the passed initializer function directly.
			push: function(init) {
				init();
			}
		}
	}

};