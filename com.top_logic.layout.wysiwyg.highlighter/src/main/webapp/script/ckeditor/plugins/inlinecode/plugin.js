CKEDITOR.plugins.add( 'inlinecode', {
  icons: 'inlinecode',
  init: function( editor ) {
    var style = new CKEDITOR.style( { element: 'code', attributes: { 'class': 'hljs inlineCode' }  } );

    // Listen to contextual style activation.
    editor.attachStyleStateChange( style, function (state) {
      !editor.readOnly && editor.getCommand( 'wrapCode').setState(state);
    } );
    // Add shortcut with CTRL+SHIFT+C
    editor.setKeystroke( CKEDITOR.CTRL + CKEDITOR.SHIFT + 67, 'wrapCode' );

    // Create the command.
    editor.addCommand( 'wrapCode', new CKEDITOR.styleCommand( style ) );

    // Register the button, if the button plugin is enabled.
    if ( editor.ui.addButton ) {
      editor.ui.addButton( 'InlineCode', {
        label: 'Inline Code',
        command: 'wrapCode',
        toolbar: 'insert'
      } );
    }
  }
});