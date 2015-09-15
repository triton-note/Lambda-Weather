var async = require('async');

var log = function() {
	var args = Array.prototype.map.call(arguments, function(value) {
		if (typeof value === 'string') {
			return value;
		} else {
			return JSON.stringify(value, null, 2)
		}
	});
	console.log.apply(this, args);
}

exports.handler = function(event, context) {
    log('Received event:', event);

    async.waterfall(
    		[
    		 ],
    		 function(err, result) {
    			if (err) {
    				context.faile(err);
    			} else {
    				log("Result: ", result);
    				context.succeed(result);
    			}
    		});
}
