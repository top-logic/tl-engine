/* 
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

// small library to handle div tags for splash images.
var splashBorder = 1;

// timer variable for refresh
var refreshTimer;
var refreshIntervall = 100; // miliseconds, between animation steps (default 100)
var step = 0;

// picture sequence that shows the busy animation
var prefix = "/top-logic/images/animation/hourglass";

function preloadPictures() {
	if (self.document.splashAnimation == null) {
		self.document.splashAnimation = new Array();
		self.document.splashAnimation[0] = new Image(); self.document.splashAnimation[0].src = prefix + "01.png";
		self.document.splashAnimation[1] = new Image(); self.document.splashAnimation[1].src = prefix + "02.png";
		self.document.splashAnimation[2] = new Image(); self.document.splashAnimation[2].src = prefix + "03.png";
		self.document.splashAnimation[3] = new Image(); self.document.splashAnimation[3].src = prefix + "04.png";
		self.document.splashAnimation[4] = new Image(); self.document.splashAnimation[4].src = prefix + "05.png";
		self.document.splashAnimation[5] = new Image(); self.document.splashAnimation[5].src = prefix + "06.png";
		self.document.splashAnimation[6] = new Image(); self.document.splashAnimation[6].src = prefix + "07.png";
		self.document.splashAnimation[7] = new Image(); self.document.splashAnimation[7].src = prefix + "08.png";
		self.document.splashAnimation[8] = new Image(); self.document.splashAnimation[8].src = prefix + "09.png";
		self.document.splashAnimation[9] = new Image(); self.document.splashAnimation[9].src = prefix + "10.png";
		self.document.splashAnimation[10] = new Image(); self.document.splashAnimation[10].src = prefix + "11.png";
		self.document.splashAnimation[11] = new Image(); self.document.splashAnimation[11].src = prefix + "12.png";
		self.document.splashAnimation[12] = new Image(); self.document.splashAnimation[12].src = prefix + "13.png";
		self.document.splashAnimation[13] = new Image(); self.document.splashAnimation[13].src = prefix + "14.png";
		self.document.splashAnimation[14] = new Image(); self.document.splashAnimation[14].src = prefix + "15.png";
	} 
    else {
		// do nothing
	}
}

function showSplash (splashtop, splashleft, splashwidth, splashheight, splashtext, border) {
  //initSplash ();
  //preloadPictures();
  splashImage.style.top = splashtop;
  splashImage.style.left = splashleft;
  splashImage.style.width = splashwidth;
  splashImage.style.height = splashheight;
  document.all.bodyText.innerText = splashtext;
  //splashImage.style.background-color = splashcolor;
  splashImage.style.visibility = 'visible';
  splashImage.style.display = 'block';
  //setHourglasPointer();
  splashImage.style.cursor = 'wait';
  document.all(0).style.cursor = 'wait';
  document.all(1).style.cursor = 'wait';
  // start animation sequence
  setRefreshTimer(refreshIntervall);
  return true;
}

function hideSplash () {
  splashImage.style.visibility = 'hidden';  
  splashImage.style.display = 'block';
  //setNormalPointer();
  document.all(0).style.cursor = 'default';
  document.all(1).style.cursor = 'default';
  // stop splashAnimationTimer
	if(refreshTimer) window.clearTimeout(refreshTimer);
}

function initSplash (){
  preloadPictures();
  document.writeln ("<div id=\"splashImage\" border=\""+splashBorder+"\" style=\"position:absolute; left:0px; top:0px; width:100%; height:100%; z-index:1000; background-color:#ffffff; visibility:hidden; display:block; \">");
  document.writeln (" <center>");
  document.writeln ("  <table id=\"divBorder\" border=\""+splashBorder+"\" width=\"100%\" height=\"100%\" cellspacing=\"0\" cellpadding=\"2\"><tr><td align=\"center\" valign=\"middle\">");
  document.writeln ("   <table id=\"splashTable\" border=\"0\" cellspacing=\"2\" cellpadding=\"2\"><tr>");
  document.writeln ("    <td width=\"10\" valign=\"middle\" align=\"left\"><img src=\"" + self.document.splashAnimation[0].src + "\" name=\"animation\" border=\"0\" \/><\/td>");
  document.writeln ("    <td valign=\"middle\" align=\"left\" nowrap=\"yes\">");
  document.writeln ("     <font color=\"#0036a2\" face=\"Arial, Helvetica, sans-serif\" size=\"2\"><b id=\"bodyText\">Bitte warten</\b><\/font>");
  document.writeln ("    <\/td>");
  document.writeln ("   <\/tr><\/table>");
  document.writeln ("  <\/td><\/tr><\/table>");
  document.writeln (" <\/center>");
  document.writeln ("<\/div>");
}

// Refresh Timer
function setRefreshTimer(interval) {
	if(refreshTimer) window.clearTimeout(refreshTimer);	// stop old interval
	if(interval > 0) {	// only when new interval is set
		refreshTimer = window.setTimeout('nextAnimationStep()', interval);
	}
}

function setHourglasPointer() { 
	if (document.all) {
		for (var i=0;i < document.all.length; i++) 
		document.all(i).style.cursor = 'wait';
		document.splashImage.style.cursor = 'wait';
	}
} 

function setNormalPointer() { 
	if (document.all) {
		for (var i=0;i < document.all.length; i++) 
		document.all(i).style.cursor = null; 
	}
}

// change splash animation to next image in animation array
function nextAnimationStep() {
	document.animation.src=self.document.splashAnimation[step].src;
	step++;
	if (step>=self.document.splashAnimation.length) step=0;
	setRefreshTimer(refreshIntervall);
}

// utilities for windows.

 // Creates a resizable pop-up window with scrollbars at defined.
 function openBareResizableWindowAt (id, url, width, height, top, left) {
    window.open(url,id,"status=no,resizable=yes,scrollbars=auto,toolbar=no,locationbar=no,WIDTH="+width+",HEIGHT="+height+",top="+top+",left="+left);
 }

 // Creates a resizable pop-up window with scrollbars yes.
 function openBareResizableWindowWithScrollbarsAt (id, url, width, height, top, left) {
    window.open(url,id,"status=no,resizable=yes,scrollbars=yes,toolbar=no,locationbar=no,WIDTH="+width+",HEIGHT="+height+",top="+top+",left="+left);
 }
