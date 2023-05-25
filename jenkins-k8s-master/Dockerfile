FROM jenkins/jenkins:lts
#skip initial setup
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false 
# Config file variable
COPY base-config.yaml /usr/share/jenkins/base-config.yaml
ENV CASC_JENKINS_CONFIG /usr/share/jenkins

# Another config approach - multifile config etc.
#COPY jobs-config.yaml /usr/share/jenkins/jobs-config.yaml
#COPY k8s-config.yaml /usr/share/jenkins/k8s-config.yaml 

#install plugins
COPY plugins.txt /usr/share/jenkins/plugins.txt
RUN jenkins-plugin-cli -f /usr/share/jenkins/plugins.txt
#copy rest of files
COPY k8s.groovy /usr/share/jenkins/jobs/k8s.groovy
#expose port for tests
EXPOSE 8080
#user back to jenkins
USER jenkins