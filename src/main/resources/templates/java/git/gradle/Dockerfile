FROM alpine/git:2.45.2 as src

WORKDIR /data

ARG HASH
ARG URL

ENV HASH=${HASH}
RUN git clone $URL src


FROM gradle:8.9.0-jdk21-alpine as builder

ARG JAR_PATH
ARG BASE_DIRECTORY="."
ARG BUILD_ARGS=""

WORKDIR /data

COPY --from=src /data/src src

WORKDIR /data/src/$BASE_DIRECTORY

RUN gradle $BUILD_ARGS clean build
RUN mv $JAR_PATH /data/app.jar


FROM eclipse-temurin:21-alpine as jrebuilder

WORKDIR build

RUN jlink \
     --add-modules ALL-MODULE-PATH \
     --strip-debug \
     --no-man-pages \
     --no-header-files \
     --output jre


FROM alpine:3.20.2

COPY --from=jrebuilder /build/jre jre
COPY --from=builder  /data/app.jar app.jar

ENV JAVA_ARGS ""
ENV JAR_ARGS ""

ENV JAVA_HOME /jre
ENV PATH $JAVA_HOME/bin:$PATH

CMD java $JAVA_ARGS -jar $JAR_ARGS app.jar