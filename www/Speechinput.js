var exec = require('cordova/exec');
var PLUGIN_NAME = 'Speechinput';

var speechinput = {

	startSpeechInputPrompt : function (params, success, error ) {
		exec(success, error, 'Speechinput', 'startSpeechInputPrompt', [params]);
	}
};

module.exports = speechinput;
