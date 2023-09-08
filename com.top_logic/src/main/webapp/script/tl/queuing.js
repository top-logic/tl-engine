/**
 * This queue limits parallel AJAX requests to a defined amount. The number of parallel requests
 * can be specified at creation time of this queue. Requests, which can't be send immediately will
 * be enqueued. They will be executed when they have been moved to the first waiting position and
 * the next free slot becomes available. Thereby the order of execution follows FIFO rule.
 */
 queuing = {
 	// Namespace declaration
 };
 
 queuing.RequestQueue = function(allowedAmountOfParallelRequests) {
 	/* 
 	 * Due to error in ECMAScript specification, which sets the incorrect this-pointer
 	 * in inner functions
 	 */
 	var self = this;
 	
 	this.allowedAmountOfParallelRequests = allowedAmountOfParallelRequests;
 	this.activeRequestsCount = 0;
 	this.waitingQueue = new Array();
 	this._active = true;
 	
 	this.stop = function() {
 		this._active = false;
 	};
 	
 	this.resume = function() {
 		this._active = true;
 		this._processNext();
 	};
 	
 	this.setAllowedParallelRequests = function(size) {
 		this.allowedAmountOfParallelRequests = size;
 	};
 	
 	this._processNext = function() {
 		while (self.isFreeRequestSlotAvailable() && !self.isWaitingQueueEmpty()) {
			var queueObject = self.dequeueRequest();
			self.executeNextRequest(queueObject.requestFunction, queueObject.optionalResponseFunction);
 		}
 	};
 	
 	this.isFreeRequestSlotAvailable = function() {
 		return self.activeRequestsCount < self.allowedAmountOfParallelRequests;
 	};
 	
 	this.isWaitingQueueEmpty = function() {
 		return self.waitingQueue.length == 0;
 	};
 	
 	this.allocateRequestSlot = function() {
 		self.activeRequestsCount++;
 	};
 	
 	this.freeRequestSlot = function() {
 		self.activeRequestsCount--;
 	};
 	
 	this._enqueueRequest = function(requestFunction, optionalResponseFunction) {
 		var queueObject = new Object();
 		queueObject.requestFunction = requestFunction;
 		queueObject.optionalResponseFunction = optionalResponseFunction;
		self.waitingQueue.push(queueObject);
 	};
 	
 	this.dequeueRequest = function() {
 		return self.waitingQueue.shift();
 	};
 	
 	this.executeNextRequest = function(requestFunction, optionalResponseFunction) {
 		self.allocateRequestSlot();
 		requestFunction.call();
 		self.registerStandardResponseAndIncludeOptionalResponseFunction(optionalResponseFunction);
 	};
 	
 	this.registerStandardResponseAndIncludeOptionalResponseFunction = function(optionalResponseFunction) {
 		services.ajax.runLater(function() {
 			self.freeRequestSlot();
 			
 			if(self.existOptionalResponseFunction(optionalResponseFunction)) {
	 			optionalResponseFunction.call();
 			}
 			
 			if(self._active) {
 				self._processNext();
 			}
 		});
 	};
 	
 	this.existOptionalResponseFunction = function(optionalResponseFunction) {
 		return (optionalResponseFunction != undefined) && (optionalResponseFunction != null);
 	};
 }
 
 /**
  * 
  * @param {Function} requestFunction - function, which triggers the ajax request
  * @param {Function} optionalResponseFunction - optional function, which shall be executed,
  * 											 after the server sends the response to the
  * 											 request, triggered by the function, given as
  * 											 first argument 											 this function 
  */
 queuing.RequestQueue.prototype.executeRequest = function(requestFunction, optionalResponseFunction) {
 	if (this._active && this.isFreeRequestSlotAvailable()) {
 		this.executeNextRequest(requestFunction, optionalResponseFunction);
 	} else {
 		this._enqueueRequest(requestFunction, optionalResponseFunction); 		
 	}
 }