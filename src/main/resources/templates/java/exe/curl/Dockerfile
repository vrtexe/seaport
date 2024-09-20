FROM curlimages/curl:8.8.0 as downloader

ARG HASH
ARG URL

WORKDIR /download

ENV HASH=${HASH}
RUN curl $URL > app.jar


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
COPY --from=downloader  /download/app.jar .

ENV JAVA_ARGS ""
ENV JAR_ARGS ""

ENV JAVA_HOME /jre
ENV PATH $JAVA_HOME/bin:$PATH

CMD java $JAVA_ARGS -jar $JAR_ARGS app.jar
