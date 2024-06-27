FROM azul/zulu-openjdk-alpine:21.0.3 AS build
RUN mkdir /project
COPY . /project
WORKDIR /project
# create fat jar
RUN chmod +x gradlew && ./gradlew assemble && cp build/libs/impl-progress.jar ./
# extrect layered jar file
RUN java -Djarmode=layertools -jar impl-progress.jar extract

FROM azul/zulu-openjdk-alpine:21.0.3-21.34-jre-headless
# install dumb-init
RUN apk add --no-cache dumb-init=1.2.5-r3
RUN mkdir /app
# set work directory
WORKDIR /app
# copy jar from build stage
COPY --from=build /project/spring-boot-loader/ ./
COPY --from=build /project/snapshot-dependencies/ ./
COPY --from=build /project/dependencies/ ./
COPY --from=build /project/application/ ./
