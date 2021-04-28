FROM openjdk:16-slim

COPY envisu4-tools.jar envisu4-tools.jar 

ENV PROFILE=prod

ENTRYPOINT [ "/bin/bash", "-c", "java  -Djava.security.egd=file:/dev/./urandom -jar -Dspring.profiles.active=$PROFILE envisu4-tools.jar"]

CMD ["/bin/bash"]