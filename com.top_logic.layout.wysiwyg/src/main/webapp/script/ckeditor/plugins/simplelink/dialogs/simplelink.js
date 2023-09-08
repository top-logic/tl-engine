CKEDITOR.dialog.add("simplelinkDialog", function(editor) {
	return {
		title: "Link",
		minWidth: 300,
		minHeight: 100,
		contents:[{
			id: "SimpleLink",
			label: "SimpleLink",
			elements:[{
				id: "url",
				type: "text",
				label: "URL",
				validate: CKEDITOR.dialog.validate.notEmpty( "URL field cannot be empty." ),
		        setup: function( element ) {
		        	var href = element.getAttribute("href");
		        	var isExternalURL = /^(http|https):\/\//;
		        	if(href) {
		        			if(!isExternalURL.test(href)) {
		        				href = "http://" + href;
		        			}
			            this.setValue(href);
			        }
		        },
		        commit: function(element) {
		        	var href = this.getValue();
		        	var isExternalURL = /^(http|https):\/\//;
		        	if(href) {
		        			if(!isExternalURL.test(href)) {
		        				href = "http://" + href;
		        			}
			            element.setAttribute("href", href);
			            if(!element.getText()) {
		        				element.setText(this.getValue());
		        			}
			        }        	
		        }				
			},
			{
				id: "name",
				type: "text",
				label: "Name",
				validate: CKEDITOR.dialog.validate.notEmpty( "Name field cannot be empty." ),
		        setup: function( element ) {
		            this.setValue( element.getText() );
		        },
		        commit: function(element) {
		        	var currentValue = this.getValue();
		        	if(currentValue !== "" && currentValue !== null) {
			        	element.setText(currentValue);
			        }
		        }	
			}]
		}],
		onShow: function() {
			var selection = editor.getSelection();
			var selector = selection.getStartElement()
			var element;
			
			if(selector) {
				 element = selector.getAscendant( 'a', true );
			}
			
			if ( !element || element.getName() != 'a' ) {
				element = editor.document.createElement( 'a' );
				element.setAttribute("target","_blank");
				if(selection) {
					element.setText(selection.getSelectedText());
				}
                this.insertMode = true;
			}
			else {
				this.insertMode = false;
			}
			
			this.element = element;

			this.setupContent(this.element);
		},
		onOk: function() {
			var dialog = this;
			var anchorElement = this.element;
			
			this.commitContent(this.element);

			if(this.insertMode) {
				editor.insertElement(this.element);
			}
		}
	};
});
