# Concurrency-in-rest-apis

This application was generated using http://start.vertx.io

## Launching tests

```shell
./gradlew clean test
```

## Packaging application

```shell
./gradlew clean assemble
```

## Running application

```shell
./gradlew clean run
```

This will start the application on port `8888`.

## How to interact with the running application

* Create a wiki page with ID `7051d73e-9e82-44ca-a0d9-eb8af23b9cf6`:
  ```shell
  curl -i -X PUT \
    -H "Content-Type: text/plain" \
    -d 'some content' \
    http://localhost:8888/wikis/7051d73e-9e82-44ca-a0d9-eb8af23b9cf6
  ```
* Retrieve the wiki page with ID `7051d73e-9e82-44ca-a0d9-eb8af23b9cf6`:
  ```shell
  curl -i \
    http://localhost:8888/documents/7051d73e-9e82-44ca-a0d9-eb8af23b9cf6
  ```
  In the response, you will see the ETag header. It will look something like this: ETag: "XXXYYYZZZ". Copy the value of the header.
* Update the wiki page with ID `7051d73e-9e82-44ca-a0d9-eb8af23b9cf6`:
  ```shell
  curl -i -X PUT \
    -H "Content-Type: text/plain" \
    -H "If-Match: \"XXXYYYZZZ\"" \
    -d 'some content' \
    http://localhost:8888/wikis/7051d73e-9e82-44ca-a0d9-eb8af23b9cf6
  ```
  The ETag header is used to ensure that the document has not been modified since the last time it was retrieved. If the document has been modified, the update will fail with a 412 Precondition Failed response.
* Repeat previous command with the same ETag value. This will result in a 412 Precondition Failed response.
* Repeat previous command, this time without ETag at all. This results in 428 Precondition Required response.

## Help

* https://vertx.io/docs/[Vert.x Documentation]
* https://stackoverflow.com/questions/tagged/vert.x?sort=newest&pageSize=15[Vert.x Stack Overflow]
* https://groups.google.com/forum/?fromgroups#!forum/vertx[Vert.x User Group]
* https://discord.gg/6ry7aqPWXy[Vert.x Discord]
* https://gitter.im/eclipse-vertx/vertx-users[Vert.x Gitter]


