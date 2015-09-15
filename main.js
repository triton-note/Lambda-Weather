var async = require('async');
var yaml = require('yaml-js');
var request = require('superagent');
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

var http_get = function(url, params, next) {
	async.waterfall(
			[
			 function(next) {
				 log("Sending request:", url, params);
				 var qs = Object.keys(params).map(function(key) {
					 return key + "=" + params[key];
				 });
				 var fullUrl = url + "?" + qs.join("&");
				 log("Request:", fullUrl);
				 request.get(fullUrl).end(next);
			 },
			 function(res, next) {
				 log("Response:", res);
				 if (res.ok) {
					 log("JSON:", res.body);
					 next(null, res.body);
				 } else {
					 next(res.text);
				 }
			 }
			 ], next);
}

var build_response = function(settings, info, next) {
	log("Building result from", info);
	if (info.weather.length < 1) {
		next("Empty result");
	} else {
		var result = {
				name: info.weather[0].main,
				iconUrl: settings.iconUrl + "/" + info.weather[0].icon + ".png",
				temperature: Math.round(info.main.temp * 100 - 27315) / 100
		};
		next(null, result);
	}
}

var getCurrent = function(settings, params, next) {
	async.waterfall(
			[
			 function(next) {
				 http_get(settings.url + "/weather", params, next);
			 },
			 function(res, next) {
				 build_response(settings, res, next);
			 }
			 ], next);
}

var getPast = function(settings, params, date, next) {
	async.waterfall(
			[
			 function(next) {
				 params.type = "hour";
				 params.start = Math.round(date / 1000);
				 params.cnt = 1;
				 http_get(settings.url + "/history/city", params, next);
			 },
			 function(res, next) {
				 if (res.list.length < 1) {
					 next("Empty result");
				 } else {
					 build_response(settings, res.list[0], next);
				 }
			 }
			 ], next);
}

exports.handler = function(event, context) {
    log('Received event:', event);
    
    var bucketName = event.bucketName;
    var date = event.date;
    var latitude = event.latitude;
    var longitude = event.longitude;

    async.waterfall(
    		[
    		 function(next) {
    			 s3.getObject({
					 Bucket: bucketName, 
					 Key: "unauthorized/settings.yaml"
				 }, next);
    		 },
			 function(res, next) {
    			 try {
					 var text = res.Body.toString();
					 var settings = yaml.load(text);
					 log("Settings: ", settings);
					 next(null, settings.openweathermap);
    			 } catch (ex) {
    				 next(ex);
    			 }
			 },
    		 function(settings, next) {
				 var params = {
						 APPID: settings.apiKey,
						 lat: latitude,
						 lon: longitude
				 };
    			 var diff = new Date().getTime() - date;
    			 if (diff < 3 * 60 * 60 * 1000) {
    				 getCurrent(settings, params, next);
    			 } else {
    				 getPast(settings, params, date, next);
    			 }
    		 }
    		 ],
    		 function(err, result) {
    			if (err) {
    				context.faile(err);
    			} else {
    				log("Result:", result);
    				context.succeed(result);
    			}
    		});
}
