node ('master'){
    def server = Artifactory.server 'prod'
    def rtMaven = Artifactory.newMavenBuild()
    def buildInfo
    
    stage ('SCM prepare'){
        deleteDir()
        checkout([$class: 'GitSCM', branches: [[name: '${gitTag}']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'WipeWorkspace']], gitTool: 'Default', submoduleCfg: [], userRemoteConfigs: [[url: '${gitRepo}']]])
        withMaven(jdk: 'jdk', maven: 'm3') {
            sh 'mvn versions:set -DnewVersion="${mvnTag}" -DgenerateBackupPoms=false'
        }
    }

    stage ('Sonar Scan'){
        tool name: 'appqa', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
        withSonarQubeEnv('SonarQube'){      
        withMaven(jdk: 'jdk', maven: 'm3') {
          sh 'mvn clean package -Dmaven.test.skip=true sonar:sonar'
          def props = readProperties file: 'target/sonar/report-task.txt'
          echo "properties=${props}"
          env.sonarServerUrl=props['serverUrl']
          env.SONAR_CE_TASK_URL=props['ceTaskUrl']
          def ceTask
            timeout(time: 1, unit: 'MINUTES') {
              waitUntil {
                sh 'curl -u $sonarToken $SONAR_CE_TASK_URL -o ceTask.json'
                ceTask = readJSON file: 'ceTask.json'
                echo ceTask.toString()
                return "SUCCESS".equals(ceTask["task"]["status"])
                }
             }
           env.qualityGateUrl = env.sonarServerUrl + "/api/qualitygates/project_status?analysisId=" + ceTask["task"]["analysisId"]
           sh 'curl -u $sonarToken $qualityGateUrl -o qualityGate.json'
           def qualitygate = readJSON file: 'qualityGate.json'
           echo qualitygate.toString()
           if ("ERROR".equals(qualitygate["projectStatus"]["status"])) {
              error  "Quality Gate failure"
             }
           echo  "Quality Gate success"
            } 
        }
    }

    stage ('Artifactory configuration'){
        rtMaven.deployer releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local', server: server
        rtMaven.resolver releaseRepo: 'repo', snapshotRepo: 'repo', server: server
        rtMaven.deployer.deployArtifacts = true // Disable artifacts deployment during Maven run
        buildInfo = Artifactory.newBuildInfo()
    }

    stage ('Maven Install'){
        rtMaven.run pom: 'pom.xml', goals: '${mvnGoal}', buildInfo: buildInfo
    }

    stage('Publish build info') {
        server.publishBuildInfo buildInfo
    }
 }
}
