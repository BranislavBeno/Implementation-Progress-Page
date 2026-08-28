# syntax=docker/dockerfile:1
FROM azul/zulu-openjdk-alpine:25.0.4.1 AS build
RUN mkdir /project
COPY . /project
WORKDIR /project
# create fat jar
RUN --mount=type=secret,id=codegenome_token \
    chmod +x gradlew && \
    ./gradlew -Pcodegenome.project.token="$(cat /run/secrets/codegenome_token 2>/dev/null || true)" assemble && \
    cp build/libs/impl-progress.jar ./
# extrect layered jar file
RUN java -Djarmode=tools -jar impl-progress.jar extract --layers --launcher --destination extracted

FROM azul/zulu-openjdk-alpine:25.0.4.1-jre-headless
# install dumb-init
RUN apk update
RUN apk add --no-cache --upgrade dumb-init
RUN mkdir /app
# set work directory
WORKDIR /app
# copy jar from build stage
COPY --from=build /project/extracted/spring-boot-loader/ ./
COPY --from=build /project/extracted/snapshot-dependencies/ ./
COPY --from=build /project/extracted/dependencies/ ./
COPY --from=build /project/extracted/application/ ./
