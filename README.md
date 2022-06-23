# Sample Google Cloud Function using Java and Gradle

## Running Locally
```shell
./gradlew runFunction
```

```shell
curl -v -X "POST" "http://localhost:8080" \
     -H "Accept: application/json" \
     -H "Content-Type: application/json" \
     -d $'{
  "name": "Again"
}'

```


## Deploying the function
```shell
gcloud beta functions deploy java-http-function \
--gen2 \
--runtime java17 \
--trigger-http \
--entry-point functions.HelloHttp \
--source ./build/libs/ \
--allow-unauthenticated
```