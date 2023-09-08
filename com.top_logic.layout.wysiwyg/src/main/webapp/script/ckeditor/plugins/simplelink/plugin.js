CKEDITOR.plugins.add('simplelink', {
	icons: 'simplelink',
	init: function(editor) {
		editor.addCommand('simplelink', new CKEDITOR.dialogCommand('simplelinkDialog'));
		
		editor.ui.addButton('SimpleLink', {
			label: 'Link',
			command: 'simplelink',
			toolbar: 'link'
		});
		
		CKEDITOR.dialog.add('simplelinkDialog', this.path + 'dialogs/simplelink.js');
	}
});