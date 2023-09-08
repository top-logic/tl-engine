CKEDITOR.plugins.add('imageUploader', {
	requires: 'filetools',
	beforeInit: function(editor) {
		if (!!!CKEDITOR.fileTools) {
			console.log("Please add the plugins fileTools and its requirements.")
		}
	},
	lang: [ 'en', 'de'],

	init: function(editor) {
		// add file type filter
		var fileDialog = $('<input type="file" multiple accept="image/*" />'),
			allowed = 'img[alt,!src]{border-style,border-width,float,height,margin,margin-bottom,margin-left,margin-right,margin-top,width}',
			required = 'img[alt,src]';
		var extensions = new Map();
		extensions.set('image/gif', '.gif');
		extensions.set('image/jpeg', '.jpg');
		extensions.set('image/png', '.png');
		extensions.set('image/svg+xml', '.svg');
		extensions.set('image/webp', '.webp');

		
		var parent = editor.element.$.parentNode;
		if (!('upload' in parent.dataset)) {
			return;
		}
		var uploadUrl = window.location.origin + parent.dataset['upload'];

		// Add image through button click.
		fileDialog.on('change', function(e) {
			Array.from(e.target.files).forEach(file => {
				return new Promise(function(resolve, reject) {
					return resolve(uploadImg(editor, extensions, uploadUrl, file));
				}).then(x => {
					editor.fire('change');
				}).catch(function(error) {
					editor.showNotification(editor.lang.imageUploader.error(file.name, evt.sender.message), 'warning', 12000);
				});
			});
			// empty input
			fileDialog[0].value = "";
			
			
		});

		/**
		/ The clipboard plugin creates an info message if the dropped images is not a gif, png or jpg. 
		/ But there are more data types allowed so the message has to be hidden. For unallowed data types 
		/ a custom error message will be created.
		*/
		editor.on( 'notificationShow', function( evt ) {
			if(evt.data.notification.type == 'info' && evt.data.notification.message.includes('This file format is not supported. You can try with one of the supported formats:')) {
				evt.cancel();
			}
		});
		
		// If an image is dropped into an empty editor or at the end of the editor, the droprange will be invalid
		// and leads to an error. So the droprange has to be set manually.
		editor.on('drop', function(evt) {
			var startContainer = evt.data.dropRange.startContainer;
			if(startContainer.type == CKEDITOR.NODE_ELEMENT && startContainer.getName() == "html") {
				var range = editor.createRange();
		        range.moveToElementEditEnd(editor.editable());
				evt.data.dropRange = range;
	        }
		}, null, null, 1);

		// Copy & Paste and Drag & Drop functionality
		editor.on('paste', function(evt) {
			if (editor.isReadOnly) {
				return;
			}

			var doc = new DOMParser().parseFromString(evt.data.dataValue, "text/html");
			var imageTags = doc.querySelectorAll("img");
			var fileCount = evt.data.dataTransfer.getFilesCount();

			// Dropped images or pasted images from system 
			if (fileCount > 0) {
				var image, 
					replaceClass
					files = new Array();
				for (let i = 0, p = Promise.resolve(); i < fileCount; i++) {
					image = evt.data.dataTransfer.getFile(i);
					files.push(image);
					// Only for copied not dropped images
					replaceClass = null;
					if (imageTags.length) {
						replaceClass = replaceImageClass(imageTags[i], i);
						evt.data.dataValue = doc.body.innerHTML;
					}
						
				}
				
				const uploadNextImage = (i) => {
					return new Promise(function(resolve, reject) {
						replaceClass = "replaceImage_" + i;
						image = files[i];
						return resolve(uploadImg(editor, extensions, uploadUrl, image, replaceClass));
					}).then(x => {
						i++;
						if (i < fileCount)
        					uploadNextImage(i);
					}).catch(function(error) {
						editor.showNotification(editor.lang.imageUploader.error(image.name, error.message), 'warning', 12000);
						var replaceImage = editor.document.findOne("." + replaceClass);
						if(replaceImage != null) {
							replaceImage.remove();
						}
						i++;
						if (i < fileCount)
        					uploadNextImage(i);
					});
				}
				uploadNextImage(0);
				
			// Pasted images from websites
			} else {
				if(imageTags.length == 0) {
					return;
				}
				for (let i = 0; i < imageTags.length; i++) {
					if(ignoreImage(imageTags[i])) {
						imageTags[i].remove();
						evt.data.dataValue = doc.body.innerHTML;
						return;
					}
					var src = imageTags[i].getAttribute("src");
					replaceImageClass(imageTags[i], i);
				}
				evt.data.dataValue = doc.body.innerHTML;
				var replaceClass,
					src;
				const uploadNextImage = (i) => {
					return new Promise(function(resolve, reject) {
						replaceClass = "replaceImage_" + i;
						src = imageTags[i].getAttribute("src");
						return resolve(fetchImage(editor, extensions, uploadUrl, src, replaceClass));
					}).then(x => {
						i++;
						if (i < imageTags.length)
        					uploadNextImage(i);
					});
				}
				
				uploadNextImage(0);
			}
		});

		// Add toolbar button for this plugin.
		editor.ui.addButton && editor.ui.addButton('Image', {
			label: 'Insert Image',
			command: 'openDialog',
			toolbar: 'insert'
		});

		// Add ACF rule to allow img tag
		editor.addCommand('openDialog', {
			allowedContent: allowed,
			requiredContent: required,
			contentTransformations: [
				['img{width}: sizeToStyle', 'img[width]: sizeToAttribute'],
				['img{float}: alignmentToStyle', 'img[align]: alignmentToAttribute']
			],
			exec: function(editor) {
				fileDialog.click();
			}
		});
	}
});

function getFileName(file, extension) {
	var random = "_" + Math.floor(Math.random() * 100000);
	var fileName = file.name;
	if (fileName == null) {
		fileName = "image" + random + extension;
	} else {
		var index = fileName.indexOf(".");
		if (index == -1) {
			fileName += random;
		} else {
			var nameWithoutExtension = fileName.substring(0, index);
			fileName = fileName.replace(nameWithoutExtension, nameWithoutExtension + random);
		}
	}
	return fileName;
};

// Images with classes starting with "cke_" are some special images for the functionality 
// of the editor (e.g. the drag and drop images). They are not relevant for the code itself 
// and therefore should not be uploaded.
function ignoreImage(image) {
	for(var classElement of image.classList){
		if(classElement.startsWith('cke_')) {
			return true;
		}
	}
	return false;
};

// Copied images from websites or the system result in the copied imagetag in the clipboard plus the uploaded version of this image.
// The imagetag in the clipboard has to be replaced by the uploaded image and therefore the replaceclass is used as an 
// identifier for the imagetag to replace.
function replaceImageClass(imageTag, index) {
	var replaceClass = "replaceImage_" + index;
	imageTag.classList.add(replaceClass);
	return replaceClass;
};
		
function fetchImage(editor, extensions, uploadUrl, url, replaceClass) {
	return fetch(url)
		.then(res => {
			if (res.status == 200) {
				return res.blob();
			} else {
				throw Error();
			}
		})
		.then(image => {
			return uploadImg(editor, extensions, uploadUrl, image, replaceClass);
		}).catch(function(error) {
			editor.showNotification(editor.lang.imageUploader.error(url, error.message), 'warning', 12000);
			var replaceImage = editor.document.findOne("." + replaceClass);
			if(replaceImage != null) {
				replaceImage.remove();
			}
		});
}



function uploadImg(editor, extensions, uploadUrl, file, replaceClass) {
	return new Promise(function(resolve, reject) {
		var extension = extensions.get(file.type);
		if (!extension) {
			var extArr = Array.from(extensions.values());
			var extsenionString = extArr.join(', ');
			
			return reject(new Error(editor.lang.imageUploader.wrongExtension(extsenionString)));
		}
		var fileTools = CKEDITOR.fileTools,
			fileName = getFileName(file, extension),
			loader = editor.uploadRepository.create(file, fileName),
			reader = new FileReader(),
			img;
		
	
		// preview image
		reader.onload = function(e) {
			var replaceImg = editor.document.findOne("." + replaceClass);
			if (replaceImg != null) {
				replaceImg.removeClass(replaceClass);
				img = replaceImg;
			} else {
				img = editor.document.createElement('img');
				editor.insertElement(img);
			}
			img.setAttribute('src', e.target.result);
			img.setStyle('opacity', 0.3);
			loader.upload(uploadUrl);
		};
	
		loader.on('uploaded', function(evt) {
			editor.widgets.initOn(img, 'image', {
				src: evt.sender.url
			});
			img.setAttribute('data-cke-saved-src', evt.sender.url);
			img.setAttribute('src', evt.sender.url);
			img.setStyle('opacity', 1);
			editor.showNotification(editor.lang.imageUploader.success(fileName), 'success');
			return resolve();
		});
	
		loader.on('error', function(evt) {
			img.$ && img.$.remove();
			return reject(editor.lang.imageUploader.error(fileName, evt.sender.message));
		});
	
		reader.readAsDataURL(file);
	});

};