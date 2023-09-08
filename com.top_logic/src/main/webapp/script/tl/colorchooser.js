//The argument initialColor is an optional argument of the form "rgb ( xxx , yyy , zzz )" or "# xx yy zz". Blanks are irrelevant.

function colorpicker(element , doSomethingWithChosenColor , initialColor){

	var colorpicker;
	var imagepath="../../../themes/default/colorchooser/";
	if (initialColor)
		var colorOfElement = new myColor(initialColor);
	else
		var colorOfElement = new myColor("rgb(0,0,0)");
	
	//my color contains variables for the HSV form, the RBG-form and the HexCode of a color
	
	function myColor(initialColor){
		this.setHSV = function(h,s,v){
			this.H=h;this.S=s;this.V=v;
			while (this.H >= 360) this.H -=360;
			var RGB=Hsv2RgbInteger(this.H,this.S,this.V);
			this.R=RGB[0];this.G=RGB[1];this.B=RGB[2];
			this.HEX = RgbInteger2RgbHex(this.R,this.G,this.B).toLowerCase();
		}
		this.setRGB = function(r,g,b){
			this.R=r;this.G=g;this.B=b;
			var HSV=RgbInteger2Hsv(r,g,b);
			this.H=HSV[0];this.S=HSV[1];this.V=HSV[2];
			while (this.H >= 360) this.H -=360;
			this.HEX = RgbInteger2RgbHex(this.R,this.G,this.B).toLowerCase();
		}
		this.setHex = function(hex){
			this.HEX=hex.toLowerCase();
			var RGB=RgbHex2RgbInteger(hex);
			this.R = RGB[0];this.G = RGB[1]; this.B = RGB[2];
			var HSV=RgbInteger2Hsv(this.R,this.G,this.B);
			this.H=HSV[0];this.S=HSV[1];this.V=HSV[2];
			while (this.H >= 360) this.H -=360;
		}			
		if (initialColor){
			/* removes all blanks*/
			var initial = initialColor.toLowerCase().replace(/ /,"");
			if ( initial.charAt(0) == "#")
				this.setHex(initial)
			else if (initial.substring(0,4) == "rgb("){
				this.R = parseInt(initial.substring(4,initial.indexOf(",")));
				initial = initial.substring(initial.indexOf(",") + 1);
				this.G = parseInt(initial.substring(0,initial.indexOf(",")));
				this.B = parseInt(initial.substring(initial.indexOf(",") + 1 , initial.indexOf(")")));
				this.setRGB(this.R,this.G,this.B);
			}else
				this.setHex("#ffffff");
		}
	}		

	function ColorPicker(){
			//saves the potential existing onclick of the element 
		this._lastOnClick = element.onclick; element.onclick=null;
			//outest div
		this._colorDialog = colorDialog();
		var pos = getPosition(element);
		this._colorDialog.css('position','absolute').css('left',pos.left + 'px').css('top',pos.top + element.offsetHeight+ 'px');
			//simple part
		this._colorTable = simplePart();
		this._colorDialog.find('#selectionCell').append(this._colorTable);
		this._colorTable.find('.colorCell').click(function(){
			colorOfElement = new myColor(this.style.backgroundColor);
			updateRGBHexFields();
			updatePreview();
			return false;
		});
			//advanced part
		this._advancedPart = advancedPart();
		this._advancedPart.hide();
			//control part
		this._controlPart = controlPart();
		this._colorDialog.find('#controlCell').append(this._controlPart);
		this._colorDialog.find('#selectionCell').append(this._advancedPart);
		this._colorDialog.find('#red').change(fieldChanged);
		this._colorDialog.find('#green').change(fieldChanged);
		this._colorDialog.find('#blue').change(fieldChanged);
		this._colorDialog.find('#hex').change(fieldChanged);
		this._colorDialog.find('#standard').click(function(){
			this.style.textDecoration = "none";
			this.style.cursor = "default";
			document.getElementById("userDefined").style.textDecoration = "underline";
			document.getElementById("userDefined").style.cursor = "pointer";
			colorpicker._colorTable.show();
			colorpicker._advancedPart.hide();				
			return false;
		});
		this._colorDialog.find('#userDefined').click(function(){
			this.style.textDecoration = "none";
			this.style.cursor = "default";
			document.getElementById("standard").style.textDecoration = "underline";
			document.getElementById("standard").style.cursor = "pointer";
			colorpicker._colorTable.hide();
			colorpicker._advancedPart.show();
			makemovable( document.getElementById("colorField") , 192 , 192 , document.getElementById("circle") , 12 , 12 ,true , true , calculateSV);
			makemovable( document.getElementById("colorLine") , null , 192 , document.getElementById("hline") , 30 , 12 , false , true , calculateHue);
			updateDynamicField();
			return false;
		});
		this._colorDialog.find('#okButton').click(function(){
			doSomethingWithChosenColor( colorOfElement.HEX );
			exitColorPicker();
			return false;
		});
		this._colorDialog.find('#cancelButton').click(function(){
			exitColorPicker();
			return false;
		});
		this._colorDialog.hide();
	};
	
	function colorDialog(){
		var tag = '<div>' +
				'<table id="colorDialog">'+
					'<tr height="20px">' +
						'<td id="standard" style="text-align:center; vertical-align:top; cursor:default;">standard</td>' +
						'<td id="userDefined" style="text-align:center; vertical-align:top; cursor:pointer; text-decoration:underline;">user defined</td>' +
						'<td rowspan=2 id="controlCell"></td>' +
					'</tr>' +
					'<tr>'+
						'<td colspan=2 id="selectionCell" align="center"></td>'+
					'</tr>'+
				'</table>' + '</div>';
		return $( tag );
	}
	
	function controlPart(){
		var tag =
				'<table cellspacing=0 style="width:100%;">' +
					'<colgroup>' +
						'<col width="40%">' +
						'<col width="60%">' +
					'</colgroup>' +
					'<tr>' +
						'<td>current:</td>' +
						'<td style="background-color:' + colorOfElement.HEX + '"></td>' +
					'</tr><tr>' + 
						'<td>chosen:</td>' +
						'<td id="preview" style="background-color:' + colorOfElement.HEX + '"></td>' +
					'</tr>' +
				'</table>' +
				'<br>' +
				'<table cellspacing=1 style="width:100%; empty-cells:show">' +
					'<tr>' +
						'<td>red:</td>' +
						'<td><input type="text" id="red" size=7 maxlength=3 value ="' + colorOfElement.R + '">' +
					'</tr><tr>' + 
						'<td>green:</td>' +
						'<td><input type="text" id="green" size=7 maxlength=3 value ="' + colorOfElement.G + '">' +
					'</tr><tr>' + 
						'<td>blue:</td>' +
						'<td><input type="text" id="blue" size=7 maxlength=3 value ="' + colorOfElement.B + '">' +
					'</tr><tr>' +
						 '<td></td><td></td>' +
					'</tr><tr>' + 
						'<td>HTML-Code:</td>' +
						'<td><input type="text" id="hex" size=7 maxlength=7 value ="' + colorOfElement.HEX + '">' +
					'</tr>' +
				'</table>' +
				'<input type="button" id="okButton" value="OK" style="width:100%">' +
				'<input type="button" id="cancelButton" value="Cancel" style="width:100%">';
		return $( tag );
	}	

	function simplePart(){
		var tag =
				'<table border=1>' +
					'<tr>' +
						'<td style="background-color:#000000" class="colorCell"></td>' +
						'<td style="background-color:#ffffff" class="colorCell"></td>' +
						'<td style="background-color:#0000ff" class="colorCell"></td>' +
						'<td style="background-color:#ff00ff" class="colorCell"></td>' +
					'</tr><tr>' +
						'<td style="background-color:#808080" class="colorCell"></td>' +
						'<td style="background-color:#008000" class="colorCell"></td>' +
						'<td style="background-color:#00ff00" class="colorCell"></td>' +
						'<td style="background-color:#800000" class="colorCell"></td>' +
					'</tr><tr>' +
						'<td style="background-color:#000080" class="colorCell"></td>' + 
						'<td style="background-color:#808000" class="colorCell"></td>' +
						'<td style="background-color:#800080" class="colorCell"></td>' +
						'<td style="background-color:#ff0000" class="colorCell"></td>' +
					'</tr><tr>' +
						'<td style="background-color:#c0c0c0" class="colorCell"></td>' +
						'<td style="background-color:#008080" class="colorCell"></td>' +
						'<td style="background-color:#ffff00" class="colorCell"></td>' +
						'<td style="background-color:#00ffff" class="colorCell"></td>' +
					'</tr>' +
				'</table>';
		return $( tag );
	}
	
	
	function advancedPart(){
		var tag =		
				//construction of transparentBackground.png in GIMP: Color gradient, color to transparent,(black,normal mode,up),(white,normal mode,left to right),(black,multiplicative mode,up)
			'<img width="192"  height="192" style="position:absolute; left:00px; top:0px;" id="colorField" src="' + imagepath + 'transparentBackground.png">' + 
			'<img width="12" height="12" style="position:absolute; cursor:pointer;" id="circle" src="' + imagepath + 'circle.png"/>' +
				//construction of colorline.png in GIMP. Color gradient HSV from red to red
			'<img width="30" height="192" style="position:absolute; left:195px; top:0px;" id="colorLine" src="' + imagepath + 'colorline.png"/>' + 
			'<img width="30" height="12" style="position:absolute; cursor:pointer" id="hline" src="' + imagepath + 'line.png"/>';
		return  $( '<div id="advancedDiv" style="position:relative; height:192px; width:225px">' + tag + '</div>');
		
	}
	
	/**	This function places the innerElement in front of the outerElement and adds an eventListener to make the innerElement movable in the border of the outerElement.<b> 
	*/
	function makemovable( outerElement , widthOuter , heightOuter , innerElement , widthInner , heightInner , horizontal , vertical , callbackFunction ){
		$(innerElement).css("position","absolute").css("margin","0px");		
			// left mouse button hold?
		var mousedown = false;
			// mouse in the outer element
		var mouseInOuterElementLeft = true; var mouseInOuterElementTop = true;
			// absolute position of the outer element
		var absOuterPosition = getPosition(outerElement);
			// position of the outer element relative to its parent
		var relOuterPositionLeft = outerElement.offsetLeft;
		var relOuterPositionTop = outerElement.offsetTop;
			// difference between the absolute and the relative position of the outer element
		var deltaOuterPositionLeft;	var deltaOuterPositionTop;
			// absolute position of the inner element
		var absoluteInnerPositionLeft = absOuterPosition.left;	var absoluteInnerPositionTop = absOuterPosition.top;
			// distance from the mouseposition in the inner element to the border of the inner element
		var deltaMousePositionLeft;	var deltaMousePositionTop;
		$(innerElement).css("left",relOuterPositionLeft + "px").css("top",relOuterPositionTop + "px");
		function down(event){
			mousedown = true;
			absOuterPosition = getPosition(outerElement);
			relOuterPositionLeft = outerElement.offsetLeft;
			relOuterPositionTop = outerElement.offsetTop;
			deltaOuterPositionLeft = relOuterPositionLeft - absOuterPosition.left;
			deltaOuterPositionTop = relOuterPositionTop - absOuterPosition.top;
			$(document).mousemove( move );
			$(document).mouseup( setMousedownFalse );
			$(document).mouseout( out );
		}
				
		function downInner(event){
			down(event);
			absoluteInnerPositionLeft = getPosition(innerElement).left;
			absoluteInnerPositionTop = getPosition(innerElement).top;
			deltaMousePositionLeft = absoluteInnerPositionLeft - event.pageX;
			deltaMousePositionTop = absoluteInnerPositionTop - event.pageY;
			return false;
		};

		function downOuter(event){
			down(event);
			absoluteInnerPositionLeft = event.pageX;
			absoluteInnerPositionTop = event.pageY;
			deltaMousePositionLeft =  - widthInner/2;
			deltaMousePositionTop = - heightInner/2;
			if (window.event)
				deltaMousePositionTop += heightInner;
			move(event)
			return false;
		}
		
		function out(event){
			if (event.target == document.body)
				setMousedownFalse();
			return false;
		};
		function setMousedownFalse(){
			mousedown = false;
			$(document).unbind("mousemove" , move);
			$(document).unbind("mouseup" , setMousedownFalse);
			$(document).unbind("mouseout" , out);
			return false;
		};
		function move(event){
			if (mousedown){
				var newPosition;
				if (horizontal){
					var left = event.pageX;
					if (left < absOuterPosition.left - deltaMousePositionLeft){
						mouseInOuterElementLeft = false;
						newPosition = absOuterPosition.left;
					}else{
						if (left > absOuterPosition.left + widthOuter - widthInner - deltaMousePositionLeft){
							mouseInOuterElementLeft = false;
							newPosition = absOuterPosition.left + widthOuter - widthInner;
						} else {
							if (mouseInOuterElementLeft)
								newPosition = left + deltaMousePositionLeft;
							mouseInOuterElementLeft = true;
						}
					}
					absoluteInnerPositionLeft = newPosition;
					$(innerElement).css("left", newPosition + deltaOuterPositionLeft + "px");
				}
				if (vertical){
					var top = event.pageY;
					if (top < absOuterPosition.top - deltaMousePositionTop){
						mouseInOuterElementTop = false;
						newPosition = absOuterPosition.top;
					} else {
						if (top > absOuterPosition.top + heightOuter - heightInner - deltaMousePositionTop) {
							mouseInOuterElementTop = false;
							newPosition = absOuterPosition.top + heightOuter - heightInner;
						} else {
							if (mouseInOuterElementTop)
								newPosition = top + deltaMousePositionTop;
							mouseInOuterElementTop = true;
						}
					}
					absoluteInnerPositionTop = newPosition;
					$(innerElement).css("top" , newPosition + deltaOuterPositionTop + "px");
				}
				callbackFunction( getPosition(innerElement).left - absOuterPosition.left , getPosition(innerElement).top - absOuterPosition.top);
			}
			return false;
		};		
		$(innerElement).mousedown(downInner);
		$(outerElement).mousedown(downOuter);
	}
	
	/** Calculates the h-part of the color depending on the position of the slide.
	*	Updates rgb and hex fields and colorOfMyElement.
	*/
	function calculateHue(left , top){
		colorOfElement.setHSV( (360 / 180) * (180 - top) , colorOfElement.S , colorOfElement.V );	//180 = height of the color line
		updateRGBHexFields();
		updatePreview();
		document.getElementById("colorField").style.backgroundColor = Hsv2RgbHex(colorOfElement.H,1,1);
	}
	
	/** Calculates the s and v-part of the color depending on the position of the circle.
	*	Updates rgb and hex fields and colorOfMyElement.
	*/
	function calculateSV(left , top){
		colorOfElement.setHSV( colorOfElement.H , left / 180 , 1 - top / 180 );		//1^st 180 = width of the color field; 2^nd 180 = height of the color field
		updateRGBHexFields();
		updatePreview();
	}
	
	
	/**	Checkes if the value of the fields for red, green, blue, or hex have changed.
	*	In case of changes it sets colorOfElement to the choosen color
	*/
	function fieldChanged(){
		if (this == document.getElementById("red") || this == document.getElementById("green") || this == document.getElementById("blue")){
			if (parseInt(this.value) >= 0 && parseInt(this.value) <=255){
				colorOfElement.setRGB(document.getElementById("red").value , document.getElementById("green").value , document.getElementById("blue").value);
				document.getElementById("hex").value = colorOfElement.HEX;
			} else{
				this.value = 0;
				alert("Just integers 0 <= x <= 255");
			}
		}
		if (this == document.getElementById("hex")){
			var val = this.value.toLowerCase();
			var colorInCorrectForm = val.length == 7 && val.charAt(0)=="#";
			var i = 1;
			while (colorInCorrectForm && i < 7){
				var currentCharCode = val.charCodeAt(i);
				colorInCorrectForm &= ((97 <= currentCharCode && currentCharCode <= 102) || (47 <= currentCharCode && currentCharCode <= 57));
				i++;
			}
			if (colorInCorrectForm){
				colorOfElement.setHex(document.getElementById("hex").value);
				document.getElementById("red").value = colorOfElement.R;
				document.getElementById("green").value = colorOfElement.G;
				document.getElementById("blue").value = colorOfElement.B;
			} else{
				alert("Just a hex-Color");
			}
		}
		updateDynamicField();
		updatePreview();
	}

	/**gives the absolute position of the element
	*/
	function getPosition(elem){
		var pos = { left:0 , top:0};
		do {
    		pos.left += elem.offsetLeft;
    		pos.top += elem.offsetTop;
  		} while (elem = elem.offsetParent);
		return pos;
	}
	
	
	/**sets the Fields for the red, green, blue, and hex-value to the current choice
	*/
	function updateRGBHexFields(){
		document.getElementById("red").value = colorOfElement.R;
		document.getElementById("green").value = colorOfElement.G;
		document.getElementById("blue").value = colorOfElement.B;
		document.getElementById("hex").value = colorOfElement.HEX;
	}
	
	
	/**places the circle and the slide at the position of the current color
	*/
	function updateDynamicField(){
/*		$("#circle").css("left",parseInt($("#colorField").css("left").split("p")[0]) + colorOfElement.S*180 +"px");
		$("#circle").css("top",parseInt($("#colorField").css("top").split("p")[0]) + (1-colorOfElement.V)*180 +"px");
		$("#hline").css("top",parseInt($("#colorLine").css("top").split("p")[0]) + 180-0.5*colorOfElement.H+ "px");
*/		$("#circle").css("left",document.getElementById("colorField").offsetLeft + colorOfElement.S*180 +"px");
		$("#circle").css("top",document.getElementById("colorField").offsetTop + (1-colorOfElement.V)*180 +"px");
		$("#hline").css("top",document.getElementById("colorLine").offsetTop + 180-0.5*colorOfElement.H+ "px");
		document.getElementById("colorField").style.backgroundColor = Hsv2RgbHex(colorOfElement.H,1,1);
	}
	
	//updates the preview field to the current choice
	function updatePreview(){
		document.getElementById("preview").style.backgroundColor = colorOfElement.HEX;
	}
	
	//removes all added listeners and ends the color chooser dialog
	function exitColorPicker(){
		colorpicker._colorDialog.hide( 'slow' , function() {
			$('#colorDialog').remove();
			element.onclick = colorpicker._lastOnClick;
			$(document.body).unbind("mousedown",colorpicker._checkExternalClick);
		});	
	}		
	
	jQuery.extend(ColorPicker.prototype, {
		_checkExternalClick: function(event){
			var target = $(event.target);
			if (target.parents("#colorDialog").length == 0){ //Click out of the dialog
				exitColorPicker();
			}
		}	
	});
		
	colorpicker = new ColorPicker();
	$(document.body).append(colorpicker._colorDialog);
	colorpicker._colorDialog.show( 'slow' , function() {
		$(document.body).bind("mousedown",colorpicker._checkExternalClick);
	});
}
