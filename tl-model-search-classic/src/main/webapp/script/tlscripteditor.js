services.tlscript = {

	/**
	 * Enables a custom live auto completion, i.e. opens a popup with suggestions,
	 * if the line prefix matches the given regular expression.
	 */
	enableCustomEditorAutoComplete: function(editorID, regex) {
		var editor = ace.edit(editorID);
		
		/**
		 * Register a callback which is invoked after every single character 
		 * is entered in the editors input field.
		 */
		editor.commands.on('afterExec', function(event) {
			if(!(event.command.name === "backspace" || event.command.name === "insertstring")) {
				return;
			}
			
			var prefix = getLinePrefix(event.editor);
			
			if(regex.test(prefix)) {
				// Executes an internal auto completion.
				event.editor.execCommand('startAutocomplete');
			} 
		});
		
		// Disable standard live auto completion after every single character by default.
		editor.setOptions({
			enableLiveAutocompletion: false
		});
		
		/**
		 * Returns the line up to the current cursor position.
		 */
		function getLinePrefix(editor) {
			const pos = editor.getCursorPosition();
			const line = editor.session.getLine(pos.row);
			
			return line.slice(0, pos.column);
		};
		
	}	

}