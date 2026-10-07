FROM tomcat:10.1-jdk17

RUN rm -rf /usr/local/tomcat/webapps/ROOT

RUN mkdir -p /tmp/classes

COPY src/main/webapp /tmp/webapp

COPY src/main/java /tmp/java

RUN javac -cp "/usr/local/tomcat/lib/*" \
    -d /tmp/classes \
    $(find /tmp/java -name "*.java")

RUN mkdir -p /usr/local/tomcat/webapps/ROOT/WEB-INF/classes

RUN cp -r /tmp/webapp/* /usr/local/tomcat/webapps/ROOT/

RUN cp -r /tmp/classes/* /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/

EXPOSE 8080

CMD ["catalina.sh", "run"]