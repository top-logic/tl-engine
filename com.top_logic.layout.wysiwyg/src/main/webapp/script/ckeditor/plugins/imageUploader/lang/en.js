CKEDITOR.plugins.setLang( 'imageUploader', 'en', {
	success: (name) => {return `The image ${name} was successfully uploaded.`},
	error: (name, error) => {return `The image ${name} could not be uploaded: ${error}`},
	fetchFail: (url, error) => {return `The URL ${url} could not be loaded: ${error}`},
	wrongExtension: (formats) => {return `The image has an incorrect file format. Allowed formats: ${formats}`}
} );
