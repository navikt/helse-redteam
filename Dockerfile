FROM gcr.io/distroless/java21-debian12:nonroot

ENV TZ="Europe/Oslo"
ENV JAVA_TOOL_OPTIONS='-XX:MaxRAMPercentage=90 -Duser.language=nb'

COPY build/libs/*.jar /app/

WORKDIR /app

CMD ["app.jar"]
