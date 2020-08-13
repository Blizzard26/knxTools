pipeline {
    agent any
    
    parameters {
        booleanParam(defaultValue: false, description: 'Build Release', name: 'RELEASE')
        string(defaultValue: "", description: 'Version', name: 'RELEASE_VERSION')
    }
    
    options {
      buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '2', numToKeepStr: '5'))
      disableConcurrentBuilds()
    }
    
    tools {
        jdk "jdk8"
        gradle "gradle"
    }
    
    stages {
        
        stage('Build') {
            steps {
                script {
                    sh 'gradle clean classes --no-daemon --priority=low'
                }
            }
        }
        stage('Unit-Tests') {
            steps {
                script {
                    sh 'gradle -Dtest.ignoreFailures=true test --no-daemon --priority=low'
                }
                junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
            }
        }
        
        stage('Release') {
            when { expression { return params.RELEASE } }
            steps {
                script {
                    sh 'gradle assembleDist --no-daemon --priority=low'
                }
                archiveArtifacts artifacts: 'build/distributions/*.zip', fingerprint: true
                archiveArtifacts artifacts: 'build/distributions/*.tar', fingerprint: true
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