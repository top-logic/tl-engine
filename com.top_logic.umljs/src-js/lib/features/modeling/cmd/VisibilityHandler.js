import inherits from 'inherits';

import { isShape, isLabel, isConnection } from '../../../util/ModelUtil';

VisibilityHandler.$inject = [
  'eventBus',
  'elementRegistry',
  'selection',
  'graphicsFactory',
  'layouter'
];

export default function VisibilityHandler(eventBus, elementRegistry, selection, graphicsFactory, layouter) {
  this._eventBus = eventBus;
  this._graphicsFactory = graphicsFactory;
  this._selection = selection;
  this._layouter = layouter;
  this._elementRegistry = elementRegistry;
};

VisibilityHandler.prototype.execute = function(context) {
  var element = context.element;
  var isVisible = context.visibility;

  this.visibilityContext = {
    elements: [],
    visibility: isVisible
  };
  
  if(isVisible) {
    this.showHiddenParents(element);
  }

  this.setVisibility(element, isVisible);
  
  if(isConnection(element)) {
    if(isVisible) {
      this._setVisibility(element.source, true);
      this.updateGraphics(element.source, true);
      this._setVisibility(element.target, true);
      this.updateGraphics(element.target, true);
    }
  }
  
  this.visibilityContext.elements.forEach(element => {
    if(element.hidden) {
      this._selection.deselect(element);
    }
  });
  
  this._eventBus.fire('elements.visibility', this.visibilityContext);
};

VisibilityHandler.prototype.showHiddenParents = function(element) {
    var parent = element.parent;
    
    if(!parent.isVisible) {
      if(isConnection(parent)) {
        this._setVisibility( parent.source, true);
        this.updateGraphics(parent.source, true);
        this._setVisibility(parent.target, true);
        this.updateGraphics(parent.target, true);
      }
      
      this._setVisibility( parent, true);
      this.updateGraphics(parent, true);
    }
}

VisibilityHandler.prototype.setVisibility = function(element, isVisible) {
  this._setVisibility(element, isVisible);
  
  element.hidden = !isVisible && !this._layouter.showHiddenElements;
  
  if(isShape(element)) {
    if(isLabel(element)) {
      this.setLabelVisibility(element, isVisible);
    } else {
      this.setShapeVisibility(element, isVisible);
    }
  } else {
    this.setConnectionVisibility(element, isVisible);
  }
};

VisibilityHandler.prototype._setVisibility = function(element, isVisible) {
  if(element.isVisible !== isVisible) {
    if(!this.visibilityContext.elements.includes(element)) {
      this.visibilityContext.elements.push(element);
    }
    
    element.isVisible = isVisible;
  }
};

VisibilityHandler.prototype.setLabelVisibility = function(label, visibility) {
  this.setChildrenVisibility(label, visibility);
  
  this.updateGraphics(label, false);
};

VisibilityHandler.prototype.setConnectionVisibility = function(connection, isVisible) {
  this.setChildrenVisibility(connection, isVisible);

  this.updateGraphics(connection, false);
};

VisibilityHandler.prototype.setShapeVisibility = function(shape, visibility) {
  this.setAttachedEdgesVisibility(shape, visibility);
  this.setChildrenVisibility(shape, visibility);

  this.updateGraphics(shape, false);
};

VisibilityHandler.prototype.setAttachedEdgesVisibility = function(shape, visibility) {
  this.setIncomingAttachedEdgesVisibility(shape, visibility);
  this.setOutgoingAttachedEdgesVisibility(shape, visibility);
};

VisibilityHandler.prototype.setIncomingAttachedEdgesVisibility = function(shape, isVisible) {
  var handler = this;

  if('incoming' in shape) {
    shape.incoming.forEach(function(edge) {
      if(isVisible) {
        if(edge.source.isVisible) {
          handler.setVisibility(edge, isVisible);
        }
      } else {
        handler.setVisibility(edge, isVisible);
      }
    });
  }
};

VisibilityHandler.prototype.setOutgoingAttachedEdgesVisibility = function(shape, isVisible) {
  var handler = this;

  if('outgoing' in shape) {
    shape.outgoing.forEach(function(edge) {
      if(isVisible) {
        if(edge.target.isVisible) {
          handler.setVisibility(edge, isVisible);
        }
      } else {
        handler.setVisibility(edge, isVisible);
      }
    });
  }
};

VisibilityHandler.prototype.setChildrenVisibility = function(element, visibility) {
  var handler = this;

  if('children' in element) {
    element.children.forEach(function(child) {
      handler.setVisibility(child, visibility);
    });
  }
};

VisibilityHandler.prototype.updateGraphics = function(element, updateChildren) {
  if(isConnection(element)) {
    this.updateGfx(element, 'connection');
  } else {
    this.updateGfx(element, 'shape');
  }
  
  if(updateChildren) {
    this.updateGraphicChildren(element);
  }
};

VisibilityHandler.prototype.updateGraphicChildren = function(element) {
  var handler = this;
  
  if('children' in element) {
	element.children.forEach(function(child) {
	  handler.updateGraphics(child, false);
	});
  }
};

VisibilityHandler.prototype.updateGfx = function(element, type) {
  var gfx = this._elementRegistry.getGraphics(element, false);

  this._graphicsFactory.update(type, element, gfx);
};