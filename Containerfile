FROM eclipse-temurin:21-alpine as jrebuilder

WORKDIR build

RUN jlink \
     --add-modules ALL-MODULE-PATH \
     --strip-debug \
     --no-man-pages \
     --no-header-files \
     --output jre

FROM alpine:3.20.2

ARG BASE_JAR_DIR="."
ARG JAR_PATH="$BASE_JAR_DIR/build/libs/hosting-0.0.1-SNAPSHOT.jar"

COPY --from=jrebuilder /build/jre jre
COPY $JAR_PATH app.jar

ENV JAVA_HOME /jre
ENV PATH $JAVA_HOME/bin:$PATH

CMD java $JAVA_ARGS -jar $JAR_ARGS app.jar