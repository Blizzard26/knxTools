pipeline {
    agent any
    
    parameters {
        booleanParam(defaultValue: false, description: 'Build Release', name: 'RELEASE')
        string(defaultValue: "", description: 'Version', name: 'RELEASE_VERSION')
    }
    
    options {
      buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '2', daysToKeepStr: '14', numToKeepStr: '15'))
      disableConcurrentBuilds()
    }
    
    tools {
        jdk "jdk8"
        gradle "gradle"
    }
    
    stages {

        stage('Prepare') {
            steps {
                script {
                    sh 'chmod a+x gradlew'
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    sh './gradle clean compile --no-daemon --priority=low'
                }
            }
        }
        stage('Unit-Tests') {
            steps {
                script {
                    try {
                        sh './gradle -Dtest.ignoreFailures=true test --no-daemon --priority=low'
                    } finally {
                        junit '**/build/test-results/test/*.xml'
                    }
                }
            }
        }
    }
    
    post {
        always {
            recordIssues enabledForFailure: true, ignoreFailedBuilds: false, sourceCodeEncoding: 'UTF-8', 
              tools: [
                java(reportEncoding: 'UTF-8')
              ]  
        }
    }
}