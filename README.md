```shell
./gradlew jibBuildTar -Djib.to.image=telemetry-adapter:latest -Djib.outputPaths.tar=build/telemetry-adapter.tar 
```

```shell
docker load --input build/telemetry-adapter.tar
```

```shell
docker run --env-file .env telemetry-adapter:latest
```
