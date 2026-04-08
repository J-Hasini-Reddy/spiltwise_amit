@Library("ci@master") _

MavenDockerPublish(
   'enableCodeSonarSastScan': true,
   'enableCodeSonarSastGating': false,
   'tenant': 'omega',
   'jdkVersion': '17',
   'codeSonarScanCommand': "mvn sonar:sonar -Dsonar.projectKey=zea_opc_b05_splitwise_on_creditline_userservice_amit -Dsonar.projectName=\"zea_opc_b05_splitwise_on_creditline_userservice_amit\" -Dsonar.branch.name=${params.gitBranch ?: 'master'}",
   'mavenBuildCommand': 'mvn clean package',
   'mavenDeployCommand': 'mvn clean deploy',
   'disableImagePublish': false,
   'publishHelmChart': true,
   'publishDdlArtifacts': true
)