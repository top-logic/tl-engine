services.bpe = {
	BPMNDisplay : {
		init : function(controlId, editMode, selectedId) {
			var controlElement = document.getElementById(controlId);
			var bpmnXML = BAL.DOM.getNonStandardAttribute(controlElement, "data-bpml");
			
			var Modeler = editMode ? BpmnJS : BpmnJS.NavigatedViewer;
			
			var modeler = new Modeler({
				container : '#' + controlId
			});
			
			controlElement.modeler = modeler;

			modeler.importXML(bpmnXML, function(err) {
				if (err) {
					return console.error('Cannot import BPMN diagram', err);
				}

				var canvas = modeler.get('canvas');
				// canvas.zoom('fit-viewport');
				
				var eventBus = modeler.get('eventBus');

				eventBus.on("commandStack.changed", function(evt) {
					services.ajax.executeOrUpdateLazy("setChangedState-" + controlId, "dispatchControlCommand", {
						controlCommand : "setChangedState",
						controlID : controlId,
						changed : true
					});
				});

				eventBus.on("element.click", function(evt) {
				    // evt.element = the model element
				    // evt.gfx = the graphical element

					var element = evt.element;
					if (element.businessObject) {
						element = element.businessObject;
					}
					var elementID = element.id;
					
					services.ajax.execute("dispatchControlCommand", {
						controlCommand : "uiSelection",
						controlID : controlId,
						elementID : elementID
					}, false);
				});
				
				if (selectedId != null){
					services.bpe.BPMNDisplay.selectElement(controlId, selectedId);
				};
			});
		}, 
		
		storeDiagram: function(controlId) {
			var controlElement = document.getElementById(controlId);
			var modeler = controlElement.modeler;
			
			modeler.saveXML({ format: false }, function(err, xml) {
				if (!err) {
					services.ajax.execute("dispatchControlCommand", {
						controlCommand : "handleDiagramData",
						controlID : controlId,
						xml : xml
					}, false);
				}
			});
		},
		
		selectElement: function(controlId, elementId) {
			var controlElement = document.getElementById(controlId);
			var modeler = controlElement.modeler;
			
			var node = modeler.get("elementRegistry").get(elementId);
			modeler.get("selection").select(node);
		}
	}

}
