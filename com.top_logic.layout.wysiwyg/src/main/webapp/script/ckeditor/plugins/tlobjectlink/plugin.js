CKEDITOR.plugins.add('tlobjectlink', {
	icons: 'tlobjectlink',
	init: function(editor) {
		editor.addCommand('tlobjectlink', {
			exec: function(editor) {
				var name = editor.name;
				var id = name.substring(0, name.indexOf("-content"));
				var bookmarks = editor.getSelection().createBookmarks();
				editor.element.setCustomData('bookmarks', bookmarks);
				
				services.ajax.execute('dispatchControlCommand', {controlCommand: 'createTLObjectLink', controlID: id});
			}
		});
		
		// Add shortcut with ALT+SHIFT+T
		editor.setKeystroke( CKEDITOR.ALT + CKEDITOR.SHIFT + 84, 'tlobjectlink' );
		
		editor.ui.addButton('TLObjectLink', {
			label: 'TL Object Link',
			command: 'tlobjectlink',
			toolbar: 'link'
		});
	}
});