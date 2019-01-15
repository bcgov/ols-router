pipeline {
    agent any
    parameters {
    string(defaultValue: 'https://github.com/bcgov/ols-router.git', description: 'Source Code Repo URL', name: 'gitRepo')
    string(defaultValue: 'dev', description: 'Git Branch or Tag Name', name: 'gitBranch')
    string(defaultValue: 'ols-router', description: 'Project Name', name: 'pn', trim: false)    
    string(defaultValue: '', description: 'Version Tag will be used by Arctifactory', name: 'mvnTag', trim: false)
    string(defaultValue: 'clean install -Pk8s -Dmaven.test.skip=true', description: 'default maven life cycle goal', name: 'mvnGoal', trim: false)
    }
    stages {
        stage ('code checkout') {
            steps {
                git branch: '${gitBranch}', url: "${gitRepo}"
            }
        }
      
        stage('build && SonarQube analysis') {
        environment {
        scannerHome = tool 'appqa'
        }    
            steps {
                withSonarQubeEnv('SonarQube') {
                    withMaven(maven:'m3') {
                        sh 'mvn clean package sonar:sonar'
                    }
                }
            }
        }
        stage("Quality Gate") {
            steps {
                timeout(time: 1, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

       stage ('create build config') {
            steps {
                script {
                  def ocDir = tool "oc3.11"
                  withEnv(["PATH+OC=${ocDir}"]) {    
                  openshift.withCluster() {
                    def models = openshift.process( "-f", "https://raw.githubusercontent.com/bcgov/ols-router/tools/ols-router.bc.yaml", "-p", "PROJ_NAME=${pn}", "SITE_REPO=${gitRepo}", "REPO_BRANCH=${gitBranch}" )
                    openshift.delete( models )
                    def created = openshift.create( models )
                    def bc = openshift.selector( 'bc', [build: 'ols-router'] )
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
