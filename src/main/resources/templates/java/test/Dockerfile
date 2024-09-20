FROM eclipse-temurin:21-alpine as jre-build

WORKDIR build

RUN jlink \
     --add-modules ALL-MODULE-PATH \
     --strip-debug \
     --no-man-pages \
     --no-header-files \
     --output jre

FROM gcr.io/distroless/base-debian12:debug

COPY --from=jre-build /build/jre jre
COPY pids-cms-app-1.5.2.jar app.jar

ENV JAVA_HOME /jre
ENV PATH $JAVA_HOME/bin:$PATH

ENTRYPOINT [ "java" ]
CMD [ "-jar", "app.jar" ]