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
        
        stage('Build') {
            steps {
                gradle {
                    sh './gradlew clean compile --no-daemon --priority=low'
                }
            }
        }
        stage('Unit-Tests') {
            steps {
                gradle {
                    sh './gradlew -Dtest.ignoreFailures=true test --no-daemon --priority=low'
                    junit '**/build/test-results/test/*.xml'
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