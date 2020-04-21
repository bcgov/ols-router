 pipeline {
    agent any
    options {
    buildDiscarder(logRotator(numToKeepStr: '3', artifactNumToKeepStr: '1'))
    }
    parameters {
    string(defaultValue: 'https://github.com/bcgov/ols-router.git', description: 'Source Code Repo URL', name: 'gitRepo')
    string(defaultValue: 'dev', description: 'Git Branch or Tag Name', name: 'gitBranch')
    choice(name: 'pn', choices: ['ols-router-admin', 'ols-router-web'], description: 'Product Name to build, used by docker package') 
    string(defaultValue: '2.0.0-SNAPSHOT', description: 'Version Tag will be used by Arctifactory', name: 'mvnTag', trim: false)
    string(defaultValue: 'clean package -Pk8s -pl ${pn} -am', description: 'default maven life cycle goal', name: 'mvnGoal', trim: false)
    }
    stages {
        stage ('code checkout') {
            steps {
                git branch: '${gitBranch}', url: '${gitRepo}'
            }
        }
      
        stage('build && SonarQube analysis') {
        environment {
        scannerHome = tool 'appqa'
        }    
            steps {
                withSonarQubeEnv('SonarQube') {
                    withMaven(maven:'m3') {
                        sh 'mvn clean package sonar:sonar -Dsonar.java.source=11'
                    }
                }
            }
        }
       /* stage("Quality Gate") {
            steps {
             timeout(time: 1, unit: 'MINUTES') {
                   waitForQualityGate abortPipeline: false
             }
            }
        } */

       stage ('create docker sidecar image') {
            steps {
                script {
                  def ocDir = tool "oc3.11"
                  withEnv(["PATH+OC=${ocDir}"]) {    
                  openshift.withCluster() {
                    def models = openshift.process( "-f", "https://raw.githubusercontent.com/bcgov/ols-router/tools/ols-router.bc.yaml", "-p", "PROJ_NAME=${pn}", "SITE_REPO=${gitRepo}", "REPO_BRANCH=${gitBranch}", "MVN_GOAL=${mvnGoal}" )
                    openshift.delete( models )
                    def created = openshift.create( models )
                    def bc = openshift.selector( 'bc', [build: '${pn}'] )
                    def statusv = openshift.raw( 'status', '-v' )
                    echo "Cluster status: ${statusv.out}"
                    def buildSelector = bc.startBuild()
                    buildSelector.logs('-f')
                    def result = buildSelector.logs('-f')
                    def logsString = result.actions[0].out
                    def logsErr = result.actions[0].err
                    echo "The logs operation require ${result.actions.size()} oc interactions"
                    echo "Logs executed: ${result.actions[0].cmd}"
            }
          }
        }
      }
    }        
  }
}
