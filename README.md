# Lambda-Weather
AWS Lambda to get information of past and forecast of weather

## TEST Usage
`curl -X POST ${host} -H 'x-api-key: ${key}' -H 'Content-type: application/json' -d '{"apiKey": "${key_for_openweather}","date": 1441938497761, "lat": 37.971751, "lng": 23.726720}'`

※1 date = Fri Sep 11 11:28:17 JST 2015
※2 lat,lng = Athens
