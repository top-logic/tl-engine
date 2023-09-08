CKEDITOR.plugins.setLang( 'imageUploader', 'de', {
	success: (name) => {return `Das Bild ${name} wurde erfolgreich hochgeladen`},
	error: (name, error) => {return `Das Bild ${name} konnte nicht hochgeladen werden: ${error}`},
	fetchFail: (url, error) => {return `Die URL ${url} konnte nicht geladen werden: ${error}`},
	wrongExtension: (formats) => {return `Das Bild hat ein falsches Dateiformat. Erlaubte Formate: ${formats}`}
} );
