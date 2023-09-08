// Register a templates definition set named "default".
CKEDITOR.addTemplates( 'en_default', {
        // The name of sub folder which hold the shortcut preview images of the
        // templates.
        imagesPath: CKEDITOR.getUrl( CKEDITOR.plugins.getPath( 'templates' ) + 'templates/images/' ),

        // The templates definitions.
        templates: [ {
                title: 'Title and Text',
                image: 'titleAndText.gif',
                description: 'A title with a text',
                htmlFile: CKEDITOR.getUrl( 'plugins/templates/templates/html/en/titleAndText.html')
        },
        {
                title: 'Image with subtitle',
                image: 'imageAndSubtitle.gif',
                description: 'An image with a caption. A placeholder can be replaced with any image.',
                htmlFile: CKEDITOR.getUrl( 'plugins/templates/templates/html/en/imageAndSubtitle.html')
        }]
});