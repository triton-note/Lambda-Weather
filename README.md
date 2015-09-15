# Lambda-Weather
AWS Lambda to get information of past and forecast of weather

## TEST Usage
`curl -X POST https://api.fathens.org/natural_conditions/weather -H 'x-api-key: ${key}' -H 'Content-type: application/json' -d '{"bucketName": "triton-note-test","date": 1441938497761, "latitude": 37.971751, "longitude": 23.726720}'`

※1 date = Fri Sep 11 11:28:17 JST 2015
※2 lat,lng = Athens
