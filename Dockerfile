FROM azul/zulu-openjdk-alpine:17 AS build
RUN mkdir /project
COPY . /project
WORKDIR /project
# create fat jar
RUN chmod +x gradlew && ./gradlew build -x test
# move the jar file
RUN cd build/libs/ && cp impl-progress.jar /project/
# extrect layered jar file
RUN java -Djarmode=layertools -jar impl-progress.jar extract

FROM azul/zulu-openjdk-alpine:17-jre
RUN mkdir /app
# set work directory
WORKDIR /app
# copy jar from build stage
COPY --from=build /project/dependencies/ ./
COPY --from=build /project/snapshot-dependencies/ ./
COPY --from=build /project/spring-boot-loader/ ./
COPY --from=build /project/application/ ./
