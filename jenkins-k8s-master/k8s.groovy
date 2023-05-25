podTemplate(
  volumes: [
    secretVolume(secretName: 'gcp-sa', mountPath: '/var/jenkins_home/gcpsa/'),
    secretVolume(secretName: 'gcp-ssh', mountPath: '/var/jenkins_home/gcpssh/'),
    secretVolume(secretName: 'gcp-kh', mountPath: '/var/jenkins_home/gcpkh/')
  ],
  containers: [
    
    containerTemplate(
        name: 'kaniko', 
        image: 'gcr.io/kaniko-project/executor:debug', 
        command: 'sleep', 
        args: '30d',
        volumeMounts: [
            hostPathVolume(hostPath: '/var/jenkins_home/gcpsa', mountPath: '/var/jenkins_home/gcpsa')
        ]
        ),        
    containerTemplate(
        name: 'git', 
        image: 'alpine/git', 
        command: 'sleep', 
        args: '30d',
        volumeMounts: [
            hostPathVolume(hostPath: '/var/jenkins_home/gcpssh', mountPath: '/var/jenkins_home/gcpssh'),
            hostPathVolume(hostPath: '/var/jenkins_home/gcpkh', mountPath: '/var/jenkins_home/gcpkh')
        ]
        ),
        containerTemplate(
        name: 'gcloud', 
        image: 'google/cloud-sdk:latest', 
        command: 'sleep', 
        args: '30d',
        volumeMounts: [
            hostPathVolume(hostPath: '/var/jenkins_home/gcpsa', mountPath: '/var/jenkins_home/gcpsa')
        ]
        )


  ]) {

    node(POD_LABEL) {
        stage('Git clone') {
            container('git') {
                stage('Clone a @source.developers.google.com:2022/p/gcp101730-pulanowskisandbox/r/jenkins-nodejs-app project') {
                    sh '''
                    ls -l /var/jenkins_home/gcpssh
                    ls -l /var/jenkins_home/gcpkh
                    mkdir /root/.ssh
                    cp /var/jenkins_home/gcpssh/gcp_ssh /root/.ssh/gcp_ssh
                    cp /var/jenkins_home/gcpkh/known_hosts /root/.ssh/known_hosts
                    chmod 600 /root/.ssh/gcp_ssh
                    echo "IdentityFile /root/.ssh/gcp_ssh" >> /etc/ssh/ssh_config

                    ###echo "slave clonning repository from Source Repositories to Jenkins Workspace"
                    git clone ssh://pulan@softserveinc.com@source.developers.google.com:2022/p/gcp101730-pulanowskisandbox/r/jenkins-k8s-nodejsapp .
                    '''
                }
            }
        }

           stage('Build and push image') {
                    container('kaniko') {
                        sh '''
                            ls -l /var/jenkins_home/gcpsa
                            mkdir /kaniko/.gcp
                            cp /var/jenkins_home/gcpsa/sa-jenkins.json /kaniko/.gcp/sa-jenkins.json
                            export GOOGLE_APPLICATION_CREDENTIALS=/kaniko/.gcp/sa-jenkins.json
                            ###kaniko building image and passing to Artifact Repository
                            /kaniko/executor\
                              --context `pwd` \
                              --destination 'gcr.io/gcp101730-pulanowskisandbox/jenkins-k8s-nodejsapp'
                        '''                  
                    
                    }
            }

           stage('Build and push image') {
                    container('gcloud') {
                        sh '''
                            ls -l /var/jenkins_home/gcpsa
                            mkdir /root/.gcp
                            cp /var/jenkins_home/gcpsa/sa-jenkins.json /root/.gcp/sa-jenkins.json
                            export GOOGLE_APPLICATION_CREDENTIALS=/root/.gcp/sa-jenkins.json
                            gcloud auth activate-service-account sa-jenkins@gcp101730-pulanowskisandbox.iam.gserviceaccount.com --key-file=/root/.gcp/sa-jenkins.json
                            gcloud run deploy nodejsapp \
                                --image=gcr.io/gcp101730-pulanowskisandbox/jenkins-k8s-nodejsapp:latest \
                                --project gcp101730-pulanowskisandbox \
                                --region us-central1 \
                                --allow-unauthenticated
                '''                  
            
                        }
            }

    }
}
