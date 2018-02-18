FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/q4.jar /q4/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/q4/app.jar"]
