// Trigger blur event before save button is clicked and handled. By default the blur event is delayed by 200ms.
CKEDITOR.focusManager._.blurDelay = 10;

// Allow empty tags.
CKEDITOR.dtd.$removeEmpty['i'] = false;
CKEDITOR.dtd.$removeEmpty['span'] = false;
CKEDITOR.dtd.$removeEmpty['em'] = false;
