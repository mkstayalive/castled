ARG JDK_VERSION=11.0.9
FROM openjdk:${JDK_VERSION}-slim as app

EXPOSE 7050

ENV APPLICATION castled-server

WORKDIR /app

COPY target/container-1.0-SNAPSHOT.jar container-1.0-SNAPSHOT.jar

COPY config.yml config.yml

# wait for upstream dependencies to become available before starting server
#ENTRYPOINT ["java", "container-1.0-SNAPSHOT.jar", "-Dcom.amazonaws.sdk.ec2MetadataServiceEndpointOverride=http://100.25.111.185/metadata", "-Xmx4g", "io.castled.CastledApplication", "castled-server", "config.yml"]

ENTRYPOINT java -cp container-1.0-SNAPSHOT.jar -Xmx4g io.castled.CastledApplication castled-server config.yml
