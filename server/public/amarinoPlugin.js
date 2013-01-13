var cordova = window.cordova || window.Cordova;

// Define Constructor
var amarinoPlugin = function(deviceInfo, cbSuccess, cbError) {
	// Set member variables
	this.connectionId = deviceInfo.connectionId;
	console.log("amarinoPlugin" + deviceInfo.connectionId);
	var self = this;
	
	exec = cordova.require('cordova/exec');
	exec(
		function() {
			console.log("success");
			self.connectBT(cbSuccess, cbError);
		},
		function() {
			console.log("error");
		}, "amarinoPlugin", "setConnectionId", [this.connectionId]
	);
}

// Define member functions
amarinoPlugin.prototype.connectBT = function(onSuccess, onError) {
	exec = cordova.require('cordova/exec');
	exec(onSuccess, onError, "amarinoPlugin", "connectBT", []);
}

amarinoPlugin.prototype.controlLED = function(onSuccess, onError) {
	exec = cordova.require('cordova/exec');
	exec(onSuccess, onError, "amarinoPlugin", "controlLED", []);
}

amarinoPlugin.prototype.buttonEvent = function(onSuccess, onError) {
	exec = cordova.require('cordova/exec');
	exec(onSuccess, onError, "amarinoPlugin", "buttonEvent", []);
}