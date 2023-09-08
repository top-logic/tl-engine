services.codeeditor = {
	
	init: function(controlID, editorId, theme, mode, readOnly, gutter,  minDisplayedLines, maxDisplayedLines, excludedKeyBindings, value, noClientWarningsExpected) {
		// Must be deferred until the context path initialization script was invoked.
		var contextPath = WebService.prototype.CONTEXT_PATH;
		
		// Set base paths for dynamic loading of features. In a deployed variant,
		// the main ace.js script is included in the packaged top-logic.js script
		// and can no longer determine its own location.
		ace.config.set('basePath', contextPath + '/script/ace');
		ace.config.set('modePath', contextPath + '/script/ace');
		ace.config.set('themePath', contextPath + '/script/ace');
		
		// Remove static initialization code from the code path of the next initialization.
		this.init = this.initInstance;
		
		// Forward call to instance initialization code.
		this.initInstance.apply(this, arguments);
		
		// add module for code (auto) completion.
		ace.require("ace/ext/language_tools");
	},
	
	initInstance: function(controlID, editorId, theme, mode, readOnly, gutter, wrapMode, minDisplayedLines, maxDisplayedLines, excludedKeyBindings, value, noClientWarningsExpected) {
		var controlElement = document.getElementById(controlID);
		var editor = ace.edit(editorId);
		editor.setTheme(theme);
		editor.session.setMode(mode);
		this.setReadOnlyInternal(editor, readOnly);
		this.setShowGutterInternal(editor, gutter);
		editor.setValue(value, -1);
		editor.setOptions({
			enableBasicAutocompletion: true,
			enableSnippets: true,
			enableLiveAutocompletion: true
		});
		
		if(minDisplayedLines !== -1) {
			editor.setOption('minLines', minDisplayedLines);
		}
		
		if(maxDisplayedLines !== -1) {
			editor.setOption('maxLines', maxDisplayedLines);
		}
		
		this.setWrapModeInternal(editor, wrapMode);
		this.removeKeyBindingsInternal(editor, excludedKeyBindings);
		
		controlElement.changed = false;
		
		editor.on("blur", function() {
			if (controlElement.changed) {
				var annotations = editor.getSession().getAnnotations();
				if (annotations == null || annotations.length == 0) {
					services.ajax.execute("dispatchControlCommand", {
						controlCommand : "valueChanged",
						controlID : controlID,
						value : editor.getValue()
					});
				} else {
					services.ajax.execute("dispatchControlCommand", {
						controlCommand : "valueChanged",
						controlID : controlID,
						value : editor.getValue(),
						annotations : annotations
					});
				}
			}
		});	
		editor.on("change", function(delta) {
			controlElement.changed = true;
		});	

		controlElement.editor = editor;
		
		// Prevent resizing before TL component rendering is finished.
		window.removeEventListener("resize", editor.env.onResize);
		
		var closestLayoutResizeElement= BAL.DOM.getClosest(controlElement, "[data-resize]");
		if (closestLayoutResizeElement) {
			function editorResize() {
				if(editor) {
					editor.env.onResize();
				} else {
					LayoutFunctions.removeCustomRenderingFunction(closestLayoutResizeElement, editorResize);
				}
			}
			
			LayoutFunctions.addCustomRenderingFunction(closestLayoutResizeElement, editorResize);
		}
		
		BAL.stopEventKeyPropagation(controlElement, 'Escape');

		function initialAnnotationUpdate() {
			var session = editor.getSession();
			var annotations = session.getAnnotations();
			if (annotations.length > 0 || !noClientWarningsExpected) {
				// There is no need to call the server when no warnings are expected
				// and the editor dispays no warnings.
				services.ajax.execute("dispatchControlCommand", {
					controlCommand : "annotationUpdate",
					controlID : controlID,
					annotations : annotations
				});
			}
			session.off("changeAnnotation", initialAnnotationUpdate);
		};
		editor.getSession().on("changeAnnotation", initialAnnotationUpdate);
	},
	
	removeKeyBindings: function(controlID, value) {
		var controlElement = document.getElementById(controlID);
		var editor = controlElement.editor;
		
		removeKeyBindingsInternal(editor, value);
	},
	
	removeKeyBindingsInternal: function(editor, value) {
		for (key in editor.keyBinding.$defaultHandler.commandKeyBinding) {
		    if (value.indexOf(key) !== -1) {
		    	delete editor.keyBinding.$defaultHandler.commandKeyBinding[key];
		    }
		}
	},
	
	setValue: function(controlID, value) {
		var controlElement = document.getElementById(controlID);
		var editor = controlElement.editor;
		
		editor.setValue(value, -1);
		controlElement.changed = false;
	},
	
	setReadOnly: function(controlID, readOnly) {
		var controlElement = document.getElementById(controlID);
		var editor = controlElement.editor;
		
		this.setReadOnlyInternal(editor, readOnly);
	},
	
	setShowGutter: function(controlID, showGutter) {
		var controlElement = document.getElementById(controlID);
		var editor = controlElement.editor;

		this.setShowGutterInternal(editor, showGutter);
	},
	
	setMaxLines: function(controlID, maxLines) {
		var controlElement = document.getElementById(controlID);
		var editor = controlElement.editor;
		
		editor.setOption('maxLines', maxLines);
	},
	
	setWrapMode: function(controlID, wrapMode) {
		var controlElement = document.getElementById(controlID);
		var editor = controlElement.editor;
		
		setWrapModeInternal(editor, wrapMode);
	},
	
	setWrapModeInternal: function(editor, wrapMode) {
		editor.setOptions({
			wrap: wrapMode,
			indentedSoftWrap: !wrapMode
		});
	},
	
	setReadOnlyInternal: function(editor, readOnly) {
		editor.setOptions({
			readOnly: readOnly,
			highlightActiveLine: !readOnly,
			highlightGutterLine: !readOnly
		});
		
		if(readOnly) {
			editor.renderer.$cursorLayer.element.style.display = "none";
		} else {
			editor.renderer.$cursorLayer.element.style.display = "inline";
		}
	},
	
	setShowGutterInternal: function(editor, showGutter) {
		editor.renderer.setShowGutter(showGutter);
	},
	
	setEditorTheme: function(controlID, theme) {
		var controlElement = document.getElementById(controlID);
		var editor = controlElement.editor;
		
		editor.setTheme(theme);
	},
	
	setLanguageMode: function(controlID, mode) {
		var controlElement = document.getElementById(controlID);
		var editor = controlElement.editor;
		
		editor.session.setMode(mode);
	}
	
}