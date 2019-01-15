pipeline {
    agent any
    parameters {
    string(defaultValue: 'https://github.com/bcgov/ols-router.git', description: 'Source Code Repo URL', name: 'gitRepo')
    string(defaultValue: 'dev', description: 'Git Branch or Tag Name', name: 'gitBranch')
    string(defaultValue: '', description: 'Version Tag will be used by Arctifactory', name: 'mvnTag', trim: false)
    string(defaultValue: 'clean install -Pk8s -Dmaven.test.skip=true', description: 'default maven life cycle goal', name: 'mvnGoal', trim: false)
    }
    stages {
        stage ('code checkout') {
            steps {
                git branch: '${gitBranch}', url: "${gitRepo}"
            }
        }

/* comment out sonar block untill jdk11 support by sonar        
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
end of sonar block */
        
        stage ('Artifactory configuration') {
            steps {
                rtServer (
                    id: "prod"
                )

                rtMavenDeployer (
                    id: "MAVEN_DEPLOYER",
                    serverId: "prod",
                    releaseRepo: "libs-release-local",
                    snapshotRepo: "libs-snapshot-local"
                )

                rtMavenResolver (
                    id: "MAVEN_RESOLVER",
                    serverId: "prod",
                    releaseRepo: "libs-release",
                    snapshotRepo: "libs-snapshot"
                )
            }
        }

        stage ('Exec Maven') {
            steps {
                rtMavenRun (
                    tool: "m3",
                    pom: 'pom.xml',
                    goals: '${mvnGoal}',
                    deployerId: "MAVEN_DEPLOYER",
                    resolverId: "MAVEN_RESOLVER"
                )
            }
        }

        stage ('Publish build info') {
            steps {
                rtPublishBuildInfo (
                    serverId: "prod"
                )
            }
        }
    }
}
