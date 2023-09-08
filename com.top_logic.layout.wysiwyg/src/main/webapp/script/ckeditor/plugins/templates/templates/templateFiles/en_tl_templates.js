// Register a templates definition set named "function_templates".
CKEDITOR.addTemplates( 'en_function_templates', {
	// The name of sub folder which hold the shortcut preview images of the
	// templates.
	imagesPath: CKEDITOR.getUrl( CKEDITOR.plugins.getPath( 'templates' ) + 'templates/images/' ),

	// The templates definitions.
	templates: [{
		title: 'Function',
		image: 'titleAndTextMulti.gif',
		description: 'A template for describing a function. Included are the points: syntax, description, parameters, return value and examples.',
		htmlFile: CKEDITOR.getUrl( 'plugins/templates/templates/html/en/functionComplete.html' )
	},
	{
		title: 'Function - Syntax and description',
		image: 'titleCodeAndText.gif',
		description: 'A template for specifying the syntax of a function and what it does.',
		htmlFile: CKEDITOR.getUrl( 'plugins/templates/templates/html/en/functionSyntaxAndDescription.html' )
	},
	{
		title: 'Function - Parameter',
		image: 'titleAndTable.gif',
		description: 'A template for specifying the parameters of a function. The parameters are specified in a table.',
		htmlFile: CKEDITOR.getUrl( 'plugins/templates/templates/html/en/functionParameters.html' )
	},
	{
		title: 'Function - Return value',
		image: 'titleSubtitleAndText.gif',
		description: 'A template for specifying the return value of a function, including the type.',
		htmlFile: CKEDITOR.getUrl( 'plugins/templates/templates/html/en/functionReturnValue.html' )
	},
	{
		title: 'Function - Examples',
		image: 'titleAndCode.gif',
		description: 'A template for code examples of a function.',
		htmlFile: CKEDITOR.getUrl( 'plugins/templates/templates/html/en/functionExamples.html' )
	}]
});
