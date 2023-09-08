// Register a templates definition set named "function_templates".
CKEDITOR.addTemplates( 'de_function_templates', {
	// The name of sub folder which hold the shortcut preview images of the
	// templates.
	imagesPath: CKEDITOR.getUrl( CKEDITOR.plugins.getPath( 'templates' ) + 'templates/images/' ),

	// The templates definitions.
	templates: [{
		title: 'Funktion',
		image: 'titleAndTextMulti.gif',
		description: 'Eine Vorlage zur Beschreibung einer Funktionen. Enthalten sind die Punkte: Syntax, Beschreibung, Parameter, Rückgabewert und Beispiele.',
		htmlFile: CKEDITOR.getUrl( 'plugins/templates/templates/html/de/functionComplete.html' )
	},
	{
		title: 'Funktion - Syntax und Beschreibung',
		image: 'titleCodeAndText.gif',
		description: 'Eine Vorlage zur Angabe der Syntax einer Funktion und der Beschreibung was sie tut.',
		htmlFile: CKEDITOR.getUrl( 'plugins/templates/templates/html/de/functionSyntaxAndDescription.html' )
	},
	{
		title: 'Funktion - Parameter',
		image: 'titleAndTable.gif',
		description: 'Eine Vorlage zur Angabe der Parameter einer Funktion. Die Parameter werden in einer Tabelle angegeben.',
		htmlFile: CKEDITOR.getUrl( 'plugins/templates/templates/html/de/functionParameters.html' )
	},
	{
		title: 'Funktion - Rückgabewert',
		image: 'titleSubtitleAndText.gif',
		description: 'Eine Vorlage zur Angabe des Rückgabewerts einer Funktion inklusive des Typen.',
		htmlFile: CKEDITOR.getUrl( 'plugins/templates/templates/html/de/functionReturnValue.html' )
	},
	{
		title: 'Funktion - Beispiele',
		image: 'titleAndCode.gif',
		description: 'Eine Vorlage für Codebeispiele einer Funktion.',
		htmlFile: CKEDITOR.getUrl( 'plugins/templates/templates/html/de/functionExamples.html' )
	}]
});
