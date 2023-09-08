
var processToKill;

/*
 * Parameter:
 * elementID: the ID of the HTML-element, whose firstChild is a text node which shall be updated
 *            with the actual time left; may be null
 * finishedTime: the time when the countdown is finished (time in milliseconds)
 */
function countdownTimer(elementID, finishedTime) {
    var timeLeft = finishedTime - new Date().getTime();
    if (timeLeft < 0) timeLeft = 0;
    if (elementID != null && elementID != "") {
        if(!document.getElementById(elementID)) return;
        document.getElementById(elementID).firstChild.data = formatTime(timeLeft);
    }
    if (timeLeft >= 1000) {
        timeLeft -= 1000;
        processToKill = window.setTimeout("countdownTimer('" + elementID + "', " + finishedTime + ")", 1000);
    }
    else {
        processToKill = window.setTimeout("finishTimer()", 1000);
    }
}

function finishTimer() {
    document.location.reload();
}

function initTimer(elementID, currentTime, finishedTime) {
    // necessary to synchronize server time with client time
    var delay = new Date().getTime() - currentTime;
    if (processToKill != null) {
        window.clearTimeout(processToKill);
    }
    countdownTimer(elementID, finishedTime + delay);
}

function formatTime(timeLeft) {
    var min = Math.floor( timeLeft / (60 * 1000) );
    if (min >= 60) {
        var h = Math.floor(min / 60);
        min = Math.floor(min - h * 60);
        return "in " + h + ":" + (min > 9 ? min : "0" + min) + " h";
    }
    else {
        var sec = Math.floor( (timeLeft - min * 60 * 1000) / 1000 );
        return "in " + min + ":" + (sec > 9 ? sec : "0" + sec) + " min";
    }
}
