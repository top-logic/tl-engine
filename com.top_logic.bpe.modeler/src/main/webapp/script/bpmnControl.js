services.bpe = {
	BPMNDisplay : {
		init : function(controlId, editMode, selectedId) {
			var controlElement = document.getElementById(controlId);
			var bpmnXML = BAL.DOM.getNonStandardAttribute(controlElement, "data-bpml");
			
			var modeler;
			if (editMode) {
				modeler = new BpmnJS({
					container : '#' + controlId
				});
			} else {
				// The viewer of bpmn-js tracks the selection but draws no outline for it. The outline
				// module is taken from the modules of the modeler, the bundle exports no other access.
				var outlineModules = BpmnJS.prototype._modules.filter(function(module) {
					return module.outlineProvider != null;
				});
				modeler = new BpmnJS.NavigatedViewer({
					container : '#' + controlId,
					additionalModules : outlineModules
				});
			}
			
			controlElement.modeler = modeler;

			modeler.importXML(bpmnXML).then(function() {
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

				// Low priority: runs after the selection behavior of diagram-js, which toggles a selected
				// element off when it is clicked again. The application keeps the clicked element
				// selected, so the canvas does the same.
				eventBus.on("element.click", 500, function(evt) {
				    // evt.element = the model element
				    // evt.gfx = the graphical element

					var selection = modeler.get("selection");
					if (evt.element !== canvas.getRootElement() && !selection.isSelected(evt.element)) {
						selection.select(evt.element);
					}

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
			}).catch(function(err) {
				console.error('Cannot import BPMN diagram', err);
			});
		}, 
		
		storeDiagram: function(controlId) {
			var controlElement = document.getElementById(controlId);
			var modeler = controlElement.modeler;
			
			modeler.saveXML({ format: false }).then(function(result) {
				services.ajax.execute("dispatchControlCommand", {
					controlCommand : "handleDiagramData",
					controlID : controlId,
					xml : result.xml
				}, false);
			}).catch(function(err) {
				console.error('Cannot export BPMN diagram', err);
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
