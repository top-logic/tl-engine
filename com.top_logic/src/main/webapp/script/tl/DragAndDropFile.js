function DragAndDropFile() {}

DragAndDropFile = {

	init: function (controlId, url, uploadPossible, maxUploadSize, uploadAllowed, uploadNotAllowed, loadingText, progressbarText) {
		var isUploadPossible;
		if (uploadPossible == 'true') {
			isUploadPossible = true;
		} else {
			isUploadPossible = false;
		}
		var controller = document.getElementById(controlId);
		var dropFrame = document.getElementById(controlId + '-dropFrame');

		controller.classList.add('dropControl');
		controller.addEventListener('dragover', function () {
			DragAndDropFile.dragover(event, controlId, isUploadPossible, uploadAllowed, uploadNotAllowed);
		});
		dropFrame.addEventListener('dragleave', function () {
			DragAndDropFile.dragleave(event, controlId);
		});
		dropFrame.addEventListener('drop', function () {
			DragAndDropFile.drop(event, controlId, url, isUploadPossible, maxUploadSize, loadingText, progressbarText);
		});

	},

	dragover: function (event, controlId, isUploadPossible, uploadAllowed, uploadNotAllowed) {
		event.preventDefault();
		var dropFrame = document.getElementById(controlId + '-dropFrame');
		var element = document.getElementById(controlId + '-dropArea');
		var dropText = document.getElementById(controlId + '-dropText');

		dropFrame.classList.add('dropFrame');
		element.classList.add('dragover');
		if (isUploadPossible) {
			element.classList.add('allowed');
			dropText.innerHTML = uploadAllowed;
		} else {
			dropText.innerHTML = uploadNotAllowed;
		}

	},

	dragleave: function (event, controlId) {
		event.preventDefault();
		var dropFrame = document.getElementById(controlId + '-dropFrame');
		var element = document.getElementById(controlId + '-dropArea');
		var dropText = document.getElementById(controlId + '-dropText');

		element.classList.remove('dragover');
		element.classList.remove('allowed');
		dropFrame.classList.remove('dropFrame');
		dropText.innerHTML = '';

	},

	drop: function (event, controlId, url, isUploadPossible, maxUploadSize, loadingText, progressbarText) {
		event.preventDefault();
		var dropFrame = document.getElementById(controlId + '-dropFrame');
		var element = document.getElementById(controlId + '-dropArea');
		var dropText = document.getElementById(controlId + '-dropText');
		var progressDialog = document.getElementById(controlId + '-dndProgressDialog');
		var progressSpan = document.getElementById(controlId + '-dndProgress');
		var controller = document.getElementById(controlId);

		element.classList.remove('dragover');
		dropFrame.classList.remove('dropFrame');
		dropText.innerHTML = '';

		if (isUploadPossible) {
			DragAndDropFile.processItems(controlId, url, maxUploadSize, loadingText, progressbarText);
		}
	},
	
	processItems: function (controlId, url, maxUploadSize, loadingText, progressbarText) {
		var formData = new FormData();
		var size = 0;
		try {
			// If Browser supports directory drops
			var droppedItems = event.dataTransfer.items;
			DragAndDropFile.getEntriesOfAllItems(droppedItems).then(function (result) {
				if (!Array.isArray(result)) {
					result = [result];
				}

				for (var i = 0; i < result.length; i++) {
					size += result[i].size;
					if (DragAndDropFile.maxUploadSizeExceeded(controlId, maxUploadSize, size)) {
						services.form.sendDroppedFiles(controlId, 'dropFile', 'maxSizeExceeded:' + maxUploadSize);
						return;
					}
					formData.append('file', result[i], result[i].name);
				}
				DragAndDropFile.sendFormData(controlId, url, formData, loadingText, progressbarText);
			});

		} catch (error) {
			for (var i = 0; i < event.dataTransfer.files.length; i++) {
				file = event.dataTransfer.files[i];
				size += file.size;
				if (DragAndDropFile.maxUploadSizeExceeded(controlId, maxUploadSize, size)) {
					services.form.sendDroppedFiles(controlId, 'dropFile', 'maxSizeExceeded:' + maxUploadSize);
					return;
				}
				formData.append('file', file, file.name);
			}
			if (event.dataTransfer.files.length == 0) {
				services.form.sendDroppedFiles(controlId, 'dropFile', 'folderDropNotSupported');
			} else {
				DragAndDropFile.sendFormData(controlId, url, formData, loadingText, progressbarText);
			}
		}

	},
	
	maxUploadSizeExceeded: function (controlId, maxUploadSize, uploadSize) {
		if(maxUploadSize != 0 && uploadSize > maxUploadSize){
			return true;
		} else {
			return false;		
		}
	},

	// Collects all files of all dropped items into one array.
	getEntriesOfAllItems: function (items) {
		if (!Array.isArray(items)) {
			items = Array.prototype.slice.call(items);
		}
		return new Promise(function (resolve, reject) {
			let promises = items.map(function (item) {
					return DragAndDropFile.getEntriesOfItem(item)
				});

			return Promise.all(promises).then(function (entries) {
				var files = [];
				for (var i = 0; i < entries.length; i++) {	
					var element = entries[i];
					if (Array.isArray(element)) {
						files = files.concat(element);
					} else {
						files.push(element);
					}
				}
				resolve(files);
			});
		});
	},

	// Collects all files of one item into one array.
	getEntriesOfItem: function (item) {
		var item = item.webkitGetAsEntry();
		return new Promise(function (resolve, reject) {
			if (item) {
				DragAndDropFile.getFiles(item).then(function (result) {
					var files;
					if (!Array.isArray(result)) {
						result = [result];
					}
					var configuredFiles = [];
					let filePromises = result.map(function (r) {
							return DragAndDropFile.createFile(r)
						});

					return Promise.all(filePromises).then(function (fileSets) {
						for (var i = 0; i < fileSets.length; i++) {
							var name = result[i].fullPath.replace(/\//g, ":")
								var newFile = new Blob([fileSets[i]], {
									type: fileSets.type
								});
							newFile.name = name;
							newFile.lastModifiedDate = fileSets[i].lastModifiedDate;
							configuredFiles.push(newFile);
							resolve(configuredFiles);
						}
						resolve(fileSets);
					});

				});
			}
		});
	},

	// Checks if an entry is a file or a directory.
	// If it's a file it returns the file.
	// If it's a directory the function recalls itself on every entry of the directory
	getFiles: function (node) {
		// https://wicg.github.io/entries-api/#api-entry
		if (node.isDirectory) {
			// process directories async
			return new Promise(function (resolve, reject) {
				DragAndDropFile.readDirectoryEntries(node).then(function (entries) {
					let dirPromises = entries.map(function (dir) {
							return DragAndDropFile.getFiles(dir)
						});

					return Promise.all(dirPromises).then(function (fileSets) {
						var files = [];
						for (var i = 0; i < fileSets.length; i++) {
							var element = fileSets[i];
							if (Array.isArray(element)) {
								files = files.concat(element);
							} else {
								files.push(element);
							}
						}
						resolve(files);
					});
				});
			});
		} else {
			// directly resolve files
			return Promise.resolve(node);
		}
	},

	// Collects all entries of an directory into an array.
	readDirectoryEntries: function (rootEntry) {
		// https://wicg.github.io/entries-api/#dir-reader
		return new Promise(function (resolve, reject) {
			var dirReader = rootEntry.createReader();
			var entries = [];

			// Keep calling readEntries() until no more results are returned.
			var readEntries = function () {
				dirReader.readEntries(function (results) {
					if (!results.length) {
						resolve(entries);
					} else {
						entries = entries.concat(results);
						readEntries();
					}
				});
			};
			readEntries();
		});
	},

	// Converts a FileEnty into a File.
	createFile: function (fileEntry) {
		try {
			return new Promise(function (resolve, reject) {
				fileEntry.file(resolve, reject)
			});
		} catch (err) {
			console.log(err);
		}
	},

	// Sends a FormData containing an array of all dropped files to the server.
	sendFormData: function (controlId, url, formData, loadingText, progressbarText) {
		var dropFrame = document.getElementById(controlId + '-dropFrame');
		var element = document.getElementById(controlId + '-dropArea');
		var dropText = document.getElementById(controlId + '-dropText');
		var progressDialog = document.getElementById(controlId + '-dndProgressDialog');
		var progressSpan = document.getElementById(controlId + '-dndProgress');
		var controller = document.getElementById(controlId);
		var xhttp = new XMLHttpRequest();
		var ev = event;
		xhttp.open('POST', url);

		xhttp.upload.onloadstart = function (event) {
			controller.progressTimeout = setTimeout(function () {
					DragAndDropFile.tooManyFiles(event, controlId, progressbarText)
				}, 200);
		}
		xhttp.upload.onprogress = function (event) {
			if (progressDialog.style.display == 'block' && event.lengthComputable) {
				DragAndDropFile.resizeProgressWidth(event, controlId);
			}
		}
		xhttp.upload.onloadend = function (event) {
			clearTimeout(controller.progressTimeout);
			if (progressDialog.style.display == 'block') {
				progressSpan.style.width = '100%';
				setTimeout(function () {
					DragAndDropFile.showLoadingAnimation(controlId, loadingText);
				}, 400);
			} else {
				var parent = controller.parentNode;
				parent.loadTimeout = setTimeout(function () {
						DragAndDropFile.openDialog(controlId);
						DragAndDropFile.showLoadingAnimation(controlId, loadingText);
					}, 200);

			}
		}
		xhttp.send(formData);
		xhttp.onreadystatechange = function () {
			if (xhttp.readyState === 4 && xhttp.status === 200) {
				services.form.sendDroppedFiles(controlId, 'dropFile');
			}
		};
	},

	tooManyFiles: function (event, controlId, progressbarText) {
		if (event.total / 2 > event.loaded) {
			DragAndDropFile.openDialog(controlId);
			DragAndDropFile.showProgressBar(controlId, progressbarText)
		}
	},

	openDialog: function (controlId) {
		DragAndDropFile.createGrayBackground();
		var progressDialog = document.getElementById(controlId + '-dndProgressDialog');
		progressDialog.style.display = 'block';
	},

	showLoadingAnimation: function (controlId, loadingText) {
		var progressBar = document.getElementById(controlId + '-dndProgressBarFrame');
		var loader = document.getElementById(controlId + '-dndLoader');
		var uploadProgressText = document.getElementById(controlId + '-dndUploadProgressText');
		progressBar.style.display = 'none';
		loader.style.display = 'block';
		uploadProgressText.innerHTML = loadingText;

	},

	showProgressBar: function (controlId, progressbarText) {
		var progressBar = document.getElementById(controlId + '-dndProgressBarFrame');
		var loader = document.getElementById(controlId + '-dndLoader');
		var progressSpan = document.getElementById(controlId + '-dndProgress');
		var uploadProgressText = document.getElementById(controlId + '-dndUploadProgressText');
		progressBar.style.display = 'block';
		loader.style.display = 'none';
		progressSpan.style.width = '0%';
		uploadProgressText.innerHTML = progressbarText;
	},

	resizeProgressWidth: function (event, controlId) {
		var progressSpan = document.getElementById(controlId + '-dndProgress');
		var loaded = 100 / event.total * event.loaded;
		var calcString = loaded + '%';
		progressSpan.style.width = calcString;
	},

	createGrayBackground: function () {
		var grayBackground = document.createElement('div');
		grayBackground.classList.add('dlgBackground');
		grayBackground.id = 'grayBackground';
		document.body.appendChild(grayBackground);
		services.ajax.showWaitPane();
	},

	hideProgressDialog: function (controlId) {
		services.ajax.hideWaitPane();
		var background = document.getElementById('grayBackground');
		if (background != null) {
			document.body.removeChild(background);
		}
		var controller = document.getElementById(controlId);
		if(controller == null){
			return;
		}
		var parent = controller.parentNode;
		clearTimeout(parent.loadTimeout);
		var progressDialog = document.getElementById(controlId + '-dndProgressDialog');
		if (progressDialog != null) {
			progressDialog.style.display = 'none';
		}
	}
}
