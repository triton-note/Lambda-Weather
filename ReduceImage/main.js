var gm = require('gm').subClass({ imageMagick: true });
var async = require('async');
var yaml = require('js-yaml');
var aws = require('aws-sdk');
var s3 = new aws.S3();

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

var resize = function(data, maxSize, next) {
	gm(data).size(function(err, size) {
		if (err) {
			next(err);
		} else {
			var rate = maxSize / Math.max(size.width, size.height);
			var width = rate * size.width;
			var height = rate * size.height;
			this.resize(width, height).toBuffer("jpg", next);
		}
	});
}

var proc = function(src, next) {
    log(src);
    async.waterfall(
    		[
    		 function(next) {
    			 var params = {Bucket: src.bucket, Key: src.key};
    			 log("Downloading...", params);
    			 s3.getObject(params, next);
    		 },
    		 function(download, next) {
    			 log("Downloaded original image: ", download.ContentLength);
    			 async.waterfall(
    					 [
    					  function(next) {
    						  s3.getObject({
    							  Bucket: src.bucket, 
    							  Key: "unauthorized/settings.yaml"
    						  }, next);
    					  },
    					  function(res, next) {
    						  var text = res.Body.toString();
    						  var settings = yaml.load(text);
    						  log("Settings: ", settings);
    						  next(null, settings.photo.reduced);
    					  },
    					  function(list, next) {
    						  async.map(list, function(red, next) {
    							  resize(download.Body, red.maxSize, function(err, buffer) {
    								  next(err, {name: red.name, data: buffer, contentType: download.ContentType});
    							  });
    						  }, next);
    					  }
    					  ],
    					  next);
    		 },
    		 function(list, next) {
    			 async.map(list, function(obj, next) {
    				 var dstKey = src.prefix + "/reduced/" + obj.name + "/" + src.surfix;
    				 s3.putObject({
    					 Bucket: src.bucket,
    					 Key: dstKey,
    					 Body: obj.data,
    					 ContentType: obj.contentType
    				 }, function(err, data) {
    					 next(err, dstKey);
    				 });
    			 }, next);
    		 }
    		 ],
             next);
}

exports.handler = function(event, context) {
	console.log('Received event:', JSON.stringify(event, null, 2));
	var records = event.Records.map(function(record) {
	    var srcKey = decodeURIComponent(record.s3.object.key.replace(/\+/g, " "));
	    var found = srcKey.match(/^(.+)\/original\/(.+)$/);
	    if (found) {
	    	return {
	    		bucket: record.s3.bucket.name,
	    		key: srcKey,
	    		prefix: found[1],
	    		surfix: found[2]
	    	};
	    } else {
	    	return null;
	    }
	}).filter(function(record) {
		return record;
	});
    async.map(records, proc, function(err, results) {
    	if (err) {
    		log(err);
    		context.fail(err);
    	} else {
    		log("Results: ", results);
    		context.succeed("Results: " + results);
    	}
    });
}
