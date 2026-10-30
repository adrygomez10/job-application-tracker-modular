FROM maven:3.9.12-eclipse-temurin-25 AS dependencies
WORKDIR /workspace
COPY pom.xml .
FROM dependencies AS browser
RUN mvn -B -ntp -N org.codehaus.mojo:exec-maven-plugin:3.6.3:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.classpathScope=test -Dexec.args="install --with-deps chromium"
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
FROM browser AS tests
COPY . .
ENTRYPOINT ["bash", "scripts/container-test.sh"]
FROM dependencies AS quality
COPY . .
ENTRYPOINT ["bash", "scripts/container-quality.sh"]
FROM dependencies AS build
COPY . .
RUN mvn -B -ntp package -DskipTests -DskipITs=true -Djacoco.skip=true
FROM eclipse-temurin:25-jre-noble AS runtime
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* && groupadd --system --gid 10001 tracker && useradd --system --uid 10001 --gid tracker tracker
WORKDIR /app
COPY --from=build /workspace/tracker-app/target/tracker.jar app.jar
USER tracker
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s CMD curl --fail --silent http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
