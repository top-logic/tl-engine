

colorChooser = {

	dialogId : null,
	composerId : null,
	previewId : null,
    colorOfElement: null,
    
    initColorComposer: function() {
		colorChooser.makemovable(this.getColorFieldElement(), 192, 192, this.getCircleElement(), 12, 12, true, true, function(left, top) {colorChooser.calculateSV(left, top);});
		colorChooser.makemovable(this.getColorLineElement(), null, 192, this.getHLineElement(), 30, 12, false, true, function(left, top) {colorChooser.calculateHue(left, top);});
		colorChooser.updateDynamicField();
    },
    
    colorCellClick: function(id){
    	var element = document.getElementById(id);
    	if (BAL.DOM.containsClass(element, "noColor")) {
    		return false;
    	}
        colorChooser.colorOfElement = new colorChooser.MyColor(element.style.backgroundColor);
        colorChooser.updateRGBHexFields();
        colorChooser.updatePreview();
        colorChooser.sendPreview();
        return false;
    },
    
    updateRGBHexFields: function(){
        this.getRedElement().value = colorChooser.colorOfElement.R;
        this.getGreenElement().value = colorChooser.colorOfElement.G;
        this.getBlueElement().value = colorChooser.colorOfElement.B;
        this.getHexElement().value = colorChooser.colorOfElement.HEX;
    },
    
    getRedElement: function() {
    	return services.ajax.topWindow.document.getElementById(colorChooser.dialogId + "-red");
    },
    
    getGreenElement: function() {
    	return services.ajax.topWindow.document.getElementById(colorChooser.dialogId + "-green");
    },
    
    getBlueElement: function() {
    	return services.ajax.topWindow.document.getElementById(colorChooser.dialogId + "-blue");
    },
    
    getHexElement: function() {
    	return services.ajax.topWindow.document.getElementById(colorChooser.dialogId + "-hex");
    },
    
    getPreviewElement: function() {
    	return services.ajax.topWindow.document.getElementById(colorChooser.previewId);
    },

    
    // Composer
    getCircleElement: function() {
    	return services.ajax.topWindow.document.getElementById(colorChooser.composerId + "-circle");
    },
    
    getHLineElement: function() {
    	return services.ajax.topWindow.document.getElementById(colorChooser.composerId + "-hline");
    },
    
    getColorFieldElement: function() {
    	return services.ajax.topWindow.document.getElementById(colorChooser.composerId + "-colorField");
    },
    
    getColorLineElement: function() {
    	return services.ajax.topWindow.document.getElementById(colorChooser.composerId + "-colorLine");
    },
    
    updatePreview: function(){
        this.getPreviewElement().style.backgroundColor = colorChooser.colorOfElement.HEX;
    },

    sendPreview: function(){
		services.ajax.execute('dispatchControlCommand', {
			controlCommand : 'setColor',
			controlID : colorChooser.previewId,
			value: colorChooser.colorOfElement.HEX
		}, /*useWaitPane*/ false);
    },
    
    updateDynamicField: function(){
    	if (this.getCircleElement() != null) {
    		this.getCircleElement().style.left = this.getColorFieldElement().offsetLeft + colorChooser.colorOfElement.S * 180 + "px";
    		this.getCircleElement().style.top =  this.getColorFieldElement().offsetTop + (1 - colorChooser.colorOfElement.V) * 180 + "px";
    		this.getHLineElement().style.top = this.getColorLineElement().offsetTop + 180 - 0.5 * colorChooser.colorOfElement.H + "px";
    		this.getColorFieldElement().style.backgroundColor = Hsv2RgbHex(colorChooser.colorOfElement.H, 1, 1);
    	}
    },
    
    fieldChanged: function(element){
        if (element == this.getRedElement() || element == this.getGreenElement() || element == this.getBlueElement()) {
            if (parseInt(element.value) >= 0 && parseInt(element.value) <= 255) {
                colorChooser.colorOfElement.setRGB(this.getRedElement().value, this.getGreenElement().value, this.getBlueElement().value);
                this.getHexElement().value = colorChooser.colorOfElement.HEX;
            }
            else {
                alert("Just integers 0 <= x <= 255");
            }
        }
        if (element == this.getHexElement()) {
            var val = element.value.toLowerCase();
            var colorInCorrectForm = val.length == 7 && val.charAt(0) == "#";
            var i = 1;
            while (colorInCorrectForm && i < 7) {
                var currentCharCode = val.charCodeAt(i);
                colorInCorrectForm &= ((97 <= currentCharCode && currentCharCode <= 102) || (47 <= currentCharCode && currentCharCode <= 57));
                i++;
            }
            if (colorInCorrectForm) {
                colorChooser.colorOfElement.setHex(this.getHexElement().value);
                this.getRedElement().value = colorChooser.colorOfElement.R;
                this.getGreenElement().value = colorChooser.colorOfElement.G;
                this.getBlueElement().value = colorChooser.colorOfElement.B;
            }
            else {
                alert("Just a hex-Color");
            }
        }
        colorChooser.updateDynamicField();
        colorChooser.updatePreview();
        colorChooser.sendPreview();
        return false;
    },
    
    makemovable: function(outerElement, widthOuter, heightOuter, innerElement, widthInner, heightInner, horizontal, vertical, callbackFunction){
	    services.ajax.topWindow.jQuery(innerElement).css("position", "absolute").css("margin", "0px");
	        // left mouse button hold?
        var mousedown = false;
	        // mouse in the outer element
        var mouseInOuterElementLeft = true;
        var mouseInOuterElementTop = true;
	        // absolute position of the outer element
        var absOuterPosition = BAL.getAbsoluteElementPosition(outerElement);
	        // position of the outer element relative to its parent
        var relOuterPositionLeft = outerElement.offsetLeft;
        var relOuterPositionTop = outerElement.offsetTop;
	        // difference between the absolute and the relative position of the outer element
        var deltaOuterPositionLeft;
        var deltaOuterPositionTop;
	        // absolute position of the inner element
        var absoluteInnerPositionLeft = absOuterPosition.x;
        var absoluteInnerPositionTop = absOuterPosition.y;
	        // distance from the mouseposition in the inner element to the border of the inner element
        var deltaMousePositionLeft;
        var deltaMousePositionTop;
        services.ajax.topWindow.jQuery(innerElement).css("left", relOuterPositionLeft + "px").css("top", relOuterPositionTop + "px");
        
	    function down(event){
            mousedown = true;
            absOuterPosition = BAL.getAbsoluteElementPosition(outerElement);
            relOuterPositionLeft = outerElement.offsetLeft;
            relOuterPositionTop = outerElement.offsetTop;
            deltaOuterPositionLeft = relOuterPositionLeft - absOuterPosition.x;
            deltaOuterPositionTop = relOuterPositionTop - absOuterPosition.y;
            services.ajax.topWindow.jQuery(services.ajax.topWindow.document).mousemove(move);
            services.ajax.topWindow.jQuery(services.ajax.topWindow.document).mouseup(setMousedownFalse);
            services.ajax.topWindow.jQuery(services.ajax.topWindow.document).mouseout(out);
        }
	        
        function downInner(event){
            down(event);
            absoluteInnerPositionLeft = BAL.getAbsoluteElementPosition(innerElement).x;
            absoluteInnerPositionTop = BAL.getAbsoluteElementPosition(innerElement).y;
            deltaMousePositionLeft = absoluteInnerPositionLeft - event.pageX;
            deltaMousePositionTop = absoluteInnerPositionTop - event.pageY;
            return false;
        }
	        
        function downOuter(event){
            down(event);
            absoluteInnerPositionLeft = event.pageX;
            absoluteInnerPositionTop = event.pageY;
            deltaMousePositionLeft = -widthInner / 2;
            deltaMousePositionTop = -heightInner / 2;
            if (window.event) 
                deltaMousePositionTop += heightInner/2;
            move(event)
            return false;
        }
	        
        function out(event){
            if (event.target == services.ajax.topWindow.document.body) 
                setMousedownFalse();
            return false;
        }
        
        function setMousedownFalse(){
            mousedown = false;
            services.ajax.topWindow.jQuery(services.ajax.topWindow.document).unbind("mousemove", move);
            services.ajax.topWindow.jQuery(services.ajax.topWindow.document).unbind("mouseup", setMousedownFalse);
            services.ajax.topWindow.jQuery(services.ajax.topWindow.document).unbind("mouseout", out);
            
            colorChooser.sendPreview();
            
            return false;
        }
        
        function move(event){
            if (mousedown) {
                var newPosition;
                if (horizontal) {
                    var left = event.pageX;
                    if (left < absOuterPosition.x - deltaMousePositionLeft) {
                        mouseInOuterElementLeft = false;
                        newPosition = absOuterPosition.x;
                    }
                    else {
                        if (left > absOuterPosition.x + widthOuter - widthInner - deltaMousePositionLeft) {
                            mouseInOuterElementLeft = false;
                            newPosition = absOuterPosition.x + widthOuter - widthInner;
                        }
                        else {
                            if (mouseInOuterElementLeft) {
                                newPosition = left + deltaMousePositionLeft;                                
                            }
                            mouseInOuterElementLeft = true;                            
                        }
                    }
                    if(newPosition != undefined)
                    	absoluteInnerPositionLeft = newPosition;
                    services.ajax.topWindow.jQuery(innerElement).css("left", absoluteInnerPositionLeft + deltaOuterPositionLeft + "px");
                }
                if (vertical) {
                    var top = event.pageY;
                    if (top < absOuterPosition.y - deltaMousePositionTop) {
                        mouseInOuterElementTop = false;
                        newPosition = absOuterPosition.y;
                    }
                    else {
	                        if (top > absOuterPosition.y + heightOuter - heightInner - deltaMousePositionTop) {
	                            mouseInOuterElementTop = false;
	                            newPosition = absOuterPosition.y + heightOuter - heightInner;
	                        }
	                        else {
	                           if (mouseInOuterElementTop) {
		                                newPosition = top + deltaMousePositionTop;		                                
	                           }
	                           mouseInOuterElementTop = true;
		                    }
	                }
                    if(newPosition != undefined)
                    	absoluteInnerPositionTop = newPosition;
                    services.ajax.topWindow.jQuery(innerElement).css("top", absoluteInnerPositionTop + deltaOuterPositionTop + "px");
                }
                if(BAL.getAbsoluteElementPosition(innerElement).y == undefined)
                	BAL.getAbsoluteElementPosition(innerElement).y;
                callbackFunction(BAL.getAbsoluteElementPosition(innerElement).x - absOuterPosition.x, BAL.getAbsoluteElementPosition(innerElement).y - absOuterPosition.y);
            }
	        return false;
	    }
	    
        services.ajax.topWindow.jQuery(innerElement).mousedown(downInner);
        services.ajax.topWindow.jQuery(outerElement).mousedown(downOuter);
    },
    
    calculateHue: function(left, top){
        colorChooser.colorOfElement.setHSV((360 / 180) * (180 - top), colorChooser.colorOfElement.S, colorChooser.colorOfElement.V); //180 = height of the color line
        colorChooser.updateRGBHexFields();
        colorChooser.updatePreview();
        this.getColorFieldElement().style.backgroundColor = Hsv2RgbHex(colorChooser.colorOfElement.H, 1, 1);
    },
    
    /** Calculates the s and v-part of the color depending on the position of the circle.
     *  Updates rgb and hex fields and colorOfMyElement.
     */
    calculateSV: function(left, top){
    	var s = left / 180;
    	var v = 1 - top / 180;
    	
    	s = (s < 0) ? 0 : (s > 1) ? 1 : s;
    	v = (v < 0) ? 0 : (v > 1) ? 1 : v;
    	
        colorChooser.colorOfElement.setHSV(colorChooser.colorOfElement.H, s, v); //1^st 180 = width of the color field; 2^nd 180 = height of the color field
        colorChooser.updateRGBHexFields();
        colorChooser.updatePreview();
    },
    
            //The argument initialColor is an optional argument of the form 
            //"rgb ( xxx , yyy , zzz )" or "# xx yy zz". Blanks are irrelevant.
    
    MyColor: function(initialColor){
	    this.setHSV = function(h, s, v){
	        this.H = h;
	        this.S = s;
	        this.V = v;
	        while (this.H >= 360) 
	            this.H -= 360;
	        var RGB = Hsv2RgbInteger(this.H, this.S, this.V);
	        this.R = RGB[0];
	        this.G = RGB[1];
	        this.B = RGB[2];
	        this.HEX = RgbInteger2RgbHex(this.R, this.G, this.B).toLowerCase();
	    }
	    this.setRGB = function(r, g, b){
	        this.R = r;
	        this.G = g;
	        this.B = b;
	        var HSV = RgbInteger2Hsv(r, g, b);
	        this.H = HSV[0];
	        this.S = HSV[1];
	        this.V = HSV[2];
	        while (this.H >= 360) 
	            this.H -= 360;
	        this.HEX = RgbInteger2RgbHex(this.R, this.G, this.B).toLowerCase();
	    }
	    this.setHex = function(hex){
	        this.HEX = hex.toLowerCase();
	        var RGB = RgbHex2RgbInteger(hex);
	        this.R = RGB[0];
	        this.G = RGB[1];
	        this.B = RGB[2];
	        var HSV = RgbInteger2Hsv(this.R, this.G, this.B);
	        this.H = HSV[0];
	        this.S = HSV[1];
	        this.V = HSV[2];
	        while (this.H >= 360) 
	            this.H -= 360;
	    }
	    if (initialColor) {
	        /* removes all blanks*/
	        var initial = initialColor.toLowerCase().replace(/ /, "");
	        if (initial.charAt(0) == "#") 
	            this.setHex(initial)
	        else 
	            if (initial.substring(0, 4) == "rgb(") {
	                this.R = parseInt(initial.substring(4, initial.indexOf(",")));
	                initial = initial.substring(initial.indexOf(",") + 1);
	                this.G = parseInt(initial.substring(0, initial.indexOf(",")));
	                this.B = parseInt(initial.substring(initial.indexOf(",") + 1, initial.indexOf(")")));
	                this.setRGB(this.R, this.G, this.B);
	            }
	            else 
	                this.setHex("#000000");
	    }
    }
}

/* ********** inserting the content of the file ColorTranslator.js ****************************/

/* *****************************************************************************************************

  filename: ColorTranslator.js
  version:  1.3
  last modified:    20.07.2007
  author:   Axel Schneider
  mail:          mail@axelschneider.info
  www:      http://axelschneider.info

  summary:       This script handles colors in the color-modes rgb, hsv and hsl. The most important
        advantage of this script to adjust saturation and lightness of a given color in
                 rgb-mode dynamically.

  history:  16.07.2007 initial version.
        18.07.2007 added hsl-mode
                 19.07.2007 added complementary-functionality for hsv-mode
                 20.07.2007 added color-circle-function

  usage:         All functions of this script are public. So you may use them in the way you need them.

  functions:     HSV
                 RgbHex2Hsv(v)
        RgbInteger2Hsv(r, g, b)
        Hsv2RgbHex(h, s, v)
        Hsv2RgbInteger(h, s, v)
        Hsv2Complementary(h, s, v)
        ChangeHsvColorRelative(sFactor, vFactor, strHexRgbColor)
        ChangeHsvColorAbsolute(sValue, vValue, strHexRgbColor)
        ChangeHsvColorComplementary(strHexRgbColor)
                 GetHsvColorCircle(hWidth, strHexRgbColor)

                 HSL
                 RgbHex2Hsl(v)
        RgbInteger2Hsl(r, g, b)
        Hsl2RgbHex(h, s, l)
        Hsl2RgbInteger(h, s, l)
        ChangeHslColorRelative(sFactor, lFactor, strHexRgbColor)
        ChangeHslColorAbsolute(sValue, lValue, strHexRgbColor)

                 BASICS
                 RgbHex2RgbInteger(v)
        RgbInteger2RgbHex(r, g, b)
        Hex2Integer(hex)
        Integer2Hex(integer)

  example:  If you want to make a color more saturated and less bright you could do this:
        var strRgbHexColor = "#1C6496";
                 var valueSat = 1.1;
                 var valueLight = 0.9;
                 var strRgbHexColorAdjusted = ChangeHslColorAbsolute(valueSat,valueLight,strRgbHexColor);

  algroithms:   http://www.cs.rit.edu/~ncs/color/t_convert.html
                 http://www5.informatik.tu-muenchen.de/lehre/vorlesungen/graphik/info/csc/COL_26.htm#topic25
        http://en.wikipedia.org/wiki/HSV_color_space#Complementary_colors

        r (rgb) = color red                     | r = [0,255]
        g (rgb) = color green                   | g = [0,255]
        b (rgb) = color blue            | b = [0,255]
        h (hsv|hsl) = hue                       | h = [0,360]   | degree
        s (hsv|hsl) = saturation        | s = [0,1]     | s=1 makes the purest color (no white)
        v (hsv) = (grey-)value              | v = [0,1]     | v=0 is black
                 l (hsl) = lightness            | l = [0,1]

  cool links:   http://www.yafla.com/yaflaColor/ColorRGBHSL.aspx (complete Javascript but without the library)
        http://www.cs.rit.edu/~ncs/color/a_spaces.html (Java-Applet; very cool HSV-Applet)

  known-probs:   The HSL-Mode seems to work not properly.

***************************************************************************************************** */

/*
  Function to trace some information what functions are doing.
*/
function out(strMsg)
{
  /*var t = document.getElementById("out");
  t.innerHTML = t.innerHTML+strMsg+"<br>";*/
}

/*
  Converts rgb-color to a hsv-color.

  Input:  rgb-color in hex-code (e.g. #123456)

  Output: Array with hsb-color-values.
          h (hsv-color hue; expected range [0, 360])
          s (hsv-color saturation; expected range [0, 1])
          v (hsv-color value/grey; expected range [0, 1])
*/
function RgbHex2Hsv(v)
{
  out("RgbHex2Hsv: input [v="+v+"]");
  var vInt = RgbHex2RgbInteger(v);
  out("RgbHex2Hsv: hex->int [r="+vInt[0]+", g="+vInt[1]+", b="+vInt[2]+"]");

  var hsv = RgbInteger2Hsv(vInt[0],vInt[1],vInt[2]);
  out("RgbHex2Hsv: int->hsv [h="+hsv[0]+", s="+hsv[1]+", v="+hsv[2]+"]");

  return hsv;
}
/*
  Converts rgb-color to a hsv-color.

  Input:     r (rgb-color red; expected range [0, 255])
     g (rgb-color green; expected range [0, 255])
          b (rgb-color blue; expected range [0, 255])

  Output: Array with hsb-color-values.
          h (hsv-color hue; expected range [0, 360])
          s (hsv-color saturation; expected range [0, 1])
          v (hsv-color value/grey; expected range [0, 1])
*/
function RgbInteger2Hsv(r, g, b)
{
  var h, s, v;
  var min, max, delta;

  min = Math.min(Math.min(r,g), b);
  max = Math.max(Math.max(r,g), b);
  v = max/255;              //v

  delta = max-min;
  
  if (delta == 0){
    s = 0;
    h = 0;
    return new Array(h,s,v);
  }

  if(max!=0)
    s = delta/max;          //s
  else
  {
    //r = g = b = 0         //s = 0, v is undefined
    s = 0;
    h = -1;
    return new Array(h, s, v);
  }

  if(r==max)
  {
    h = (g-b)/delta;            //between yellow and magenta
  }
  else if(g==max)
  {
    h = 2+((b-r)/delta);            //between cyan and yellow
  }
  else
  {
    h = 4+((r-g)/delta);            //between magenta and cyan
  }

  h = h*60;             //degree

  if(h<0)
    h = h+360;

  return new Array(h, s, v);
}

/*
  Converts rgb-color to a hsl-color.

  Input:  rgb-color in hex-code (e.g. #123456)

  Output: Array with hsl-color-values.
          h (hsl-color hue; expected range [0, 360])
          s (hsl-color saturation; expected range [0, 1])
          l (hsl-color lightness; expected range [0, 1])
*/
function RgbHex2Hsl(v)
{
  var vInt = RgbHex2RgbInteger(v);

  return RgbInteger2Hsl(vInt[0],vInt[1],vInt[2]);
}

/*
  Converts rgb-color to a hsl-color.

  Input:     r (rgb-color red; expected range [0, 255])
     g (rgb-color green; expected range [0, 255])
          b (rgb-color blue; expected range [0, 255])

  Output: Array with hsl-color-values.
          h (hsl-color hue; expected range [0, 360])
          s (hsl-color saturation; expected range [0, 1])
          l (hsl-color lightness; expected range [0, 1])
*/
function RgbInteger2Hsl(r, g, b)
{
  var h, s, l;
  var min, max, delta;

  min = Math.min(Math.min(r,g), b);
  max = Math.max(Math.max(r,g), b);

  l = ((max+min)/2)/255;                //l

  if(l<=0.5)                //s
    s = (max-min)/(max+min);
  else
    s = (max-min)/(2-max-min);

  delta = max-min;

  if(r==max)
  {
    h = (g-b)/delta;            //between yellow and magenta
  }
  else if(g==max)
  {
    h = 2+((b-r)/delta);            //between cyan and yellow
  }
  else
  {
    h = 4+((r-g)/delta);            //between magenta and cyan
  }

  h = h*60;             //degree

  if(h<0)
    h = h+360;

  return new Array(h, s, l);
}

/*
  Converts hsv-color to a rgb-color.

  Input:  Array with hsb-color-values.
          h (hsv-color hue; expected range [0, 360])
          s (hsv-color saturation; expected range [0, 1])
          v (hsv-color value/grey; expected range [0, 1])

  Output: rgb-color in hex-code (e.g. #123456)
*/
function Hsv2RgbHex(h, s, v)
{
  out("Hsv2RgbHex: input [h="+h+", s="+s+", v="+v+"]");
  var vInt = Hsv2RgbInteger(h, s, v);
  out("Hsv2RgbHex: hsv->int [r="+vInt[0]+", g="+vInt[1]+", b="+vInt[2]+"]");

  var ret = RgbInteger2RgbHex(vInt[0], vInt[1], vInt[2]);
  out("Hsv2RgbHex: int->hex [hex="+ret+"");

  return ret;

}

/*
  Converts hsv-color to a rgb-color.

  Input:  Array with hsb-color-values.
          h (hsv-color hue; expected range [0, 360])
          s (hsv-color saturation; expected range [0, 1])
          v (hsv-color value/grey; expected range [0, 1])

  Output: r (rgb-color red; expected range [0, 255])
     g (rgb-color green; expected range [0, 255])
          b (rgb-color blue; expected range [0, 255])
*/
function Hsv2RgbInteger(h, s, v)
{
  var i;
  var f, p, q, t;
  var r, g, b;

  if(s==0)
  {
    //achromatic {grey}
    r = g = b = Math.floor(v*255);
    return new Array(r, g, b);
  }

  h = h/60;             //sector 0 to 5
  i = Math.floor(h);
  f = h-i;                  //factorial part of h
  p = v*(1-s);
  q = v*(1-s*f);
  t = v*(1-s*(1-f));

  switch(i)
  {
    case 0:
      r = v;
      g = t;
      b = p;
      break;
    case 1:
      r = q;
      g = v;
      b = p;
      break;
    case 2:
      r = p;
      g = v;
      b = t;
      break;
    case 3:
      r = p;
      g = q;
      b = v;
      break;
    case 4:
      r = t;
      g = p;
      b = v;
      break;
    default:    //case 5
      r = v;
      g = p;
      b = q;
      break;
  }

  //now we have rgb-values which are between 0 to 1 but we want to have value between 0 to 255
  r = Math.floor(r*255);
  g = Math.floor(g*255);
  b = Math.floor(b*255);
  return new Array(r, g, b);
}

/*
  Converts hsl-color to a rgb-color.

  Input:  Array with hsl-color-values.
          h (hsl-color hue; expected range [0, 360])
          s (hsl-color saturation; expected range [0, 1])
          l (hsl-color lightness; expected range [0, 1])

  Output: rgb-color in hex-code (e.g. #123456)
*/
function Hsl2RgbHex(h, s, l)
{
  out("Hsl2RgbHex: input [h="+h+",s="+s+",l="+l+"]");
  var vInt = Hsl2RgbInteger(h, s, l);
  out("Hsl2RgbHex: input [r="+vInt[0]+",g="+vInt[1]+",b="+vInt[2]+"]");

  return RgbInteger2RgbHex(vInt[0], vInt[1], vInt[2]);
}

/*
  Converts hsl-color to a rgb-color.

  Input:  Array with hsl-color-values.
          h (hsl-color hue; expected range [0, 360])
          s (hsl-color saturation; expected range [0, 1])
          l (hsl-color lightness; expected range [0, 1])

  Output: r (rgb-color red; expected range [0, 255])
     g (rgb-color green; expected range [0, 255])
          b (rgb-color blue; expected range [0, 255])
*/
function Hsl2RgbInteger(h, s, l)
{
  out("Hsl2RgbInteger: input [h="+h+", s="+s+", l="+l+"]");
  var m1, m2;

  if(l<=0.5)
    m2 = l*(1+s);
  else
    m2 = l+s-l*s;

  m1 = 2*l-m2;

  if(s==null || String(s)=="NaN" || s==0)
  {
    if(h==null || String(h)=="NaN")
    {
      r=g=b=l;
      return new Array(r,g,b);
    }
    else
      return null;
  }

  r = compute(m1, m2, h+120);
  g = compute(m1, m2, h);
  b = compute(m1, m2, h-120);

  //now we have rgb-values which are between 0 to 1 but we want to have value between 0 to 255
  r = Math.floor(r*255);
  g = Math.floor(g*255);
  b = Math.floor(b*255);
  out("Hsl2RgbInteger: return values [r="+r+"], g="+g+"], b="+b+"]");
  return new Array(r, g, b);

  function compute(n1, n2, hue)
  {
    var value;

    if(hue>360)
      hue = hue-360;
    else if(hue<0)
      hue = hue+360;

    if(hue<60)
      value = n1+(n2-n1)*hue/60;
    else if(hue<180)
      value = n2;
    else if(hue<240)
      value = n1+(n2-n1)*(240-hue)/60;
    else
      value = n1;

    return value;
  }
}

/*
  Converts a hex-value to an integer-value.

  Input:  String of rgb-color in hex-code (example: #fff or #ffffff)

  Output: Array which consists of the rgb-color as integer-code (example: 255, 255, 255)
*/
function RgbHex2RgbInteger(v)
{
  var r, g, b;
  v = v.replace(/#/, "");

  if(v.length==3)
  {
    r = Hex2Integer(v.substr(0, 1)+v.substr(0, 1));
    g = Hex2Integer(v.substr(1, 1)+v.substr(1, 1));
    b = Hex2Integer(v.substr(2, 1)+v.substr(2, 1));
  }
  else
  {
    r = Hex2Integer(v.substr(0, 2));
    g = Hex2Integer(v.substr(2, 2));
    b = Hex2Integer(v.substr(4, 2));
  }

  return new Array(r, g, b);
}

/*
  Converts an integer-value to a hex-value .

  Input:  Three integer-values which represents a rgb-color as integer-code (example: 255, 255, 255)

  Output: String of rgb-color in hex-code (example: #ffffff)
*/
function RgbInteger2RgbHex(r, g, b)
{
  return "#"+Integer2Hex(r)+Integer2Hex(g)+Integer2Hex(b);
}

/*
  Converts an integer-value to a hex-value.

  Input:  Hex-value (example: ff)

  Output: Integer-value (example: 255)
*/
function Hex2Integer(hex)
{
  return parseInt(hex, 16);
}

/*
  Converts a hex-value to an integer-value.

  Input:  Integer-value (example: 255)

  Output: Hex-value (example: ff)
*/
function Integer2Hex(integer)
{
   hexValues = new Array("0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F");
   hexDigit1 = Math.floor(integer / 16);
   hexDigit2 = (integer % 16);
   return hexValues[hexDigit1] + hexValues[hexDigit2];
}

/*
  Changes a given hsv-color by adjusting its saturation and its (grey-)value by factors.

  Input:  sFactor (type: float;)
     vFactor (type: float;)
          strHexRgbColor (type: string; rgb-color in hex-code)

  Output: An adjusted rgb-color in hex-code.

  Hint:   If sFactor or vFactor is less than 0 the hsv-color will not be changed.
          If sFactor has a value of null the s-value will not be adjusted.
          If vFactor has a value of null the v-value will not be adjusted.
*/
function ChangeHsvColorRelative(sFactor, vFactor, strHexRgbColor)
{
  out("ChangeHsbColorRelative: input [sFactor="+sFactor+", vFactor="+vFactor+", strHexRgbColor="+strHexRgbColor+"]");

  //the factor should not be less than zero
  if(sFactor!=null && sFactor<0)
    return strHexRgbColor;
  if(vFactor!=null && vFactor<0)
    return strHexRgbColor;

  //get hsv-color
  var arrHsvColor = RgbHex2Hsv(strHexRgbColor);
  //alert(arrHsvColor);
  out("ChangeHsbColorRelative: arrHsvColor [h="+arrHsvColor[0]+", s="+arrHsvColor[1]+", v="+arrHsvColor[2]+"]");

  //if the hue is not defined return the original color
  var h, s, v;
  h = arrHsvColor[0]
  s = arrHsvColor[1];
  v = arrHsvColor[2];
  if(h==null || String(h)=="NaN" || h==-1)
    return strHexRgbColor;

  //now change the saturation
  if(sFactor!=null)
  {
    s = s*sFactor;
    if(s<0) s = 0;
    if(s>1) s = 1;
  }

  //now change the (grey-)value
  if(vFactor!=null)
  {
    v = v*vFactor;
    if(v<0) v = 0;
    if(v>1) v = 1;
  }

  //alert("h="+h+",s="+s+",v="+v+"");
  out("ChangeHsbColorRelative: adjusted hsv [h="+h+", s="+s+", v="+v+"]");

  var ret = Hsv2RgbHex(h,s,v);
  out("ChangeHsbColorRelative: adjusted hex [hex="+ret+"]");

  return ret;
}

/*
  Changes a given hsl-color by adjusting its saturation and its lightness by factors.

  Input:  sFactor (type: float;)
     lFactor (type: float;)
          strHexRgbColor (type: string; rgb-color in hex-code)

  Output: An adjusted rgb-color in hex-code.

  Hint:   If sFactor or lFactor is less than 0 the hsl-color will not be changed.
          If sFactor has a value of null the s-value will not be adjusted.
          If lFactor has a value of null the l-value will not be adjusted.
*/
function ChangeHslColorRelative(sFactor, lFactor, strHexRgbColor)
{
  out("ChangeHslColorRelative: sFactor="+sFactor+", lFactor="+lFactor+", strHexRgbColor="+strHexRgbColor+"");
  //the factors should not be less than zero
  if(sFactor!=null && sFactor<0)
    return strHexRgbColor;
  if(lFactor!=null && lFactor<0)
    return strHexRgbColor;

  //get hsl-color
  var arrHslColor = RgbHex2Hsl(strHexRgbColor);
  out("ChangeHslColorRelative: arrHsvColor [h="+arrHslColor[0]+", s="+arrHslColor[1]+", l="+arrHslColor[2]+"]");

  //if the hue is not defined return the original color
  var h, s, l;
  h = arrHslColor[0]
  s = arrHslColor[1];
  l = arrHslColor[2];
  if(h==null || String(h)=="NaN" || h==-1)
    return strHexRgbColor;

  //now change the saturation
  if(sFactor!=null)
  {
    s = s*sFactor;
    /*if(s<0) s = 0;
    if(s>1) s = 1;*/
  }

  //now change the lightness
  if(lFactor!=null)
  {
    l = l*lFactor;
    /*if(l<0) l = 0;
    if(l>1) l = 1;*/
  }
  out("ChangeHslColorRelative: arrHsvColor adjusted [h="+h+", s="+s+", l="+l+"]");

  var strHexRgbColorChanged = Hsl2RgbHex(h,s,l);
  if(strHexRgbColorChanged==null)
    return strHexRgbColor;

  out("ChangeHslColorRelative: return value='"+strHexRgbColorChanged+"'");
  return strHexRgbColorChanged;
}

/*
  Changes a given hsv-color by setting its saturation and its (grey-)value to a certain values.

  Input:  sValue (type: float;)
     vValue (type: float;)
          strHexRgbColor (type: string; rgb-color in hex-code)

  Output: An newly set rgb-color in hex-code.

  Hint:   If sValue or vValue is not in the range [0,1] the hsv-color will not be changed.
          If sValue has a value of null the s-value will not be set.
          If vValue has a value of null the v-value will not be set.
*/
function ChangeHsvColorAbsolute(sValue, vValue, strHexRgbColor)
{
  //the values should be in the range [0,1]
  if(sValue!=null && (sValue<0 || sValue>1))
    return strHexRgbColor;
  if(vValue!=null  && (vValue<0 || vValue>1))
    return strHexRgbColor;

  //get hsv-color
  var arrHsvColor = RgbHex2Hsv(strHexRgbColor);

  //if the hue is not defined return the original color
  var h, s, v;
  h = arrHsvColor[0]
  s = arrHsvColor[1];
  v = arrHsvColor[2];
  if(h==null || String(h)=="NaN" || h==-1)
    return strHexRgbColor;

  //now change the saturation
  if(sValue!=null)
    s = sValue;

  //now change the (grey-)value
  if(vValue!=null)
    v = vValue;

  var ret = Hsv2RgbHex(h,s,v);

  return ret;
}

/*
  Changes a given hsl-color by setting its saturation and its lightning to a certain values.

  Input:  sValue (type: float;)
     lValue (type: float;)
          strHexRgbColor (type: string; rgb-color in hex-code)

  Output: An newly set rgb-color in hex-code.

  Hint:   If sValue or lValue is not in the range [0,1] the hsl-color will not be changed.
          If sValue has a value of null the s-value will not be set.
          If lValue has a value of null the l-value will not be set.
*/
function ChangeHslColorAbsolute(sValue, lValue, strHexRgbColor)
{
  //the values should be in the range [0,1]
  if(sValue!=null && (sValue<0 || sValue>1))
    return strHexRgbColor;
  if(lValue!=null  && (lValue<0 || lValue>1))
    return strHexRgbColor;

  //get hsl-color
  var arrHslColor = RgbHex2Hsl(strHexRgbColor);

  //if the hue is not defined return the original color
  var h, s, l;
  h = arrHslColor[0]
  s = arrHslColor[1];
  l = arrHslColor[2];
  if(h==null || String(h)=="NaN" || h==-1)
    return strHexRgbColor;

  //now change the saturation
  if(sValue!=null)
    s = sValue;

  //now change the lightness
  if(lValue!=null)
    v = lValue;

  var ret = Hsl2RgbHex(h,s,v);

  return ret;
}

/*
  Gets the complentary color of a certain hsv-color.

  Input:  h (type: int)
     s (type: float;)
     v (type: float;)

  Output: The complementary hsv-color.

  Hint:   If h is null or not in range [0,360] the return-value will be null.
          If s is null or not in range [0,0] the return-value will be null.
          If v is null or not in range [0,0] the return-value will be null.
*/
function Hsv2Complementary(h, s, v)
{
  //check range of hue
  if(h==null || h<0 || h>360)
    return null;

  //check range of saturation
  if(s==null || s<0 || s>1)
    return null;

  //check range of value
  if(v==null || v<0 || v>1)
    return null;

  //now calculat the complementary-color
  var hCompl, sCompl, vComp;
  if(h<180)
    hCompl = h+180;
  else
    hCompl = h-180;
  sCompl = v*s/(v*(s-1)+1);
  vCompl = (v*(s-1)+1);

  return new Array(hCompl, sCompl, vCompl);
}

/*
  Changes a given hsv-color by setting its saturation and its (grey-)value to a certain values.

  Input:  strHexRgbColor (type: string; rgb-color in hex-code)

  Output: The complementary color in rgb-mode.
*/
function ChangeHsvColorComplementary(strHexRgbColor)
{
  out("ChangeHsvColorComplementary: input ["+strHexRgbColor+"]");

  //get hsv-color
  var arrHsvColor = RgbHex2Hsv(strHexRgbColor);

  //if the hue is not defined return the original color
  var h, s, v;
  h = arrHsvColor[0]
  s = arrHsvColor[1];
  v = arrHsvColor[2];
  if(h==null || String(h)=="NaN" || h==-1)
    return strHexRgbColor;

  //now get complementary-color
  var hsvCompl = Hsv2Complementary(h, s, v);

  var ret = Hsv2RgbHex(hsvCompl[0],hsvCompl[1],hsvCompl[2]);
  out("ChangeHsvColorComplementary: input complement ["+ret+"]");

  return ret;
}

/*
  Gives an array of rgb-colors which symbolizes the color-circle of the hsv-mode.

  Input:  hWidth (type: int; steps on the circle)
     strHexRgbColor (type: string; rgb-color in hex-code)

  Output: An Array of rgb-colors in hex-mode.
*/
function GetHsvColorCircle(hWidth, strHexRgbColor)
{
  var arrColorCircle = new Array();
  var strTmpHexColor = strHexRgbColor;

  arrColorCircle.push(strTmpHexColor);

  for(var i=0;i<=360;i+=hWidth)
  {
    //get hsv-color
    var arrHsvColor = RgbHex2Hsv(strTmpHexColor);

    //if the hue is not defined return here -> the circle may not be complete
    var h, s, v;
    h = arrHsvColor[0]
    s = arrHsvColor[1];
    v = arrHsvColor[2];
    if(h==null || String(h)=="NaN" || h==-1)
      return;
    out("GetHsvColorCircle: original hsv [h="+h+", s="+s+", v="+v+"]");

    //adjust hue by hue-width
    h+=hWidth;
    if(h>360)
      h-=360;

    out("GetHsvColorCircle: adjusted hsv [h="+h+", s="+s+", v="+v+"]");
    strTmpHexColor = Hsv2RgbHex(h,s,v);
    out("GetHsvColorCircle: adjusted rgbhex [hex="+strTmpHexColor+"]");

    arrColorCircle.push(strTmpHexColor);
  }

  return arrColorCircle;
}

/* ********************* end ColorTranslator.js **************************/
