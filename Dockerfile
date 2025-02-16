FROM openjdk:8
LABEL authors="sfuli"
EXPOSE 9000
WORKDIR /app

COPY target/*.jar hexo-image-clean.jar
#ENTRYPOINT ["--spring.config.location=/data/prod.yml"]
ENTRYPOINT ["java","-jar","/app/hexo-image-clean.jar"]