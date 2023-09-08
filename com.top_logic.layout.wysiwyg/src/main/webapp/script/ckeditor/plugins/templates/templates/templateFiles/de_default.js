// Register a templates definition set named "default".
CKEDITOR.addTemplates( 'de_default', {
        // The name of sub folder which hold the shortcut preview images of the
        // templates.
        imagesPath: CKEDITOR.getUrl( CKEDITOR.plugins.getPath( 'templates' ) + 'templates/images/' ),

        // The templates definitions.
        templates: [ {
                title: 'Titel und Text',
                image: 'titleAndText.gif',
                description: 'Ein Titel mit einem Text',
                htmlFile: CKEDITOR.getUrl( 'plugins/templates/templates/html/de/titleAndText.html')
        },
        {
                title: 'Bild mit Untertitel',
                image: 'imageAndSubtitle.gif',
                description: 'Ein Bild mit einer Bildunterschrift. Als Bild wird ein Platzhalter eingefügt, der durch ein beliebiges Bild ausgetauscht werden kann.',
                htmlFile: CKEDITOR.getUrl( 'plugins/templates/templates/html/de/imageAndSubtitle.html')
        }]
});