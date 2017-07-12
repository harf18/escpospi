
node{
    stage("checkout"){
        git url:'https://github.com/harf18/escpospi.git', branch:'develop'
        echo BRANCH_NAME
    }

    def pom = readMavenPom file:'pom.xml'

    stage("maven"){
        withMaven { sh 'mvn clean verify' }
    }
    
    stage("Archive"){
    	archiveArtifacts artifacts: '**/target/*.jar'
    }

    stage("notify"){
        if  (BRANCH_NAME == 'master') {
        	echo BRANCH_NAME + ' => Deployement PROD'
	    	slackSend channel: '#team-vem', color: 'good', message: 'Nouveau build OK (v. ${pom.getVersion()}) sur la branche Master. Pour deployer l\'artefact en PREPRODUCTION, cliquer ici : http://localhost:8080/job/W3-deploy-PREPROD/build?token=OAKej5o40yY9XqyFiY8b7DEYY6qv5XPd', teamDomain: 'devlescrous', tokenCredentialId : '1'
        } else if (BRANCH_NAME.startsWith('release')){
        	echo BRANCH_NAME + ' => Deployement PREPROD'
    		slackSend channel: '#team-vem', color: 'good', message: 'Nouveau build OK (v. ${pom.getVersion()}) sur la branche ' + BRANCH_NAME + '. Pour deployer l\'artefact en PRODUCTION, cliquer ici : http://localhost:8080/job/W3-deploy-PROD/build?token=OAKej5o40yY9XqyFiY8b7DEYY6qv5XPd', teamDomain: 'devlescrous', tokenCredentialId : '1'        	    
        }
    }
}

def withMaven(def body){
    def java = tool 'oracle-8u131'
    def maven = tool 'maven-3.5.0'

    withEnv(["JAVA_HOME=${java}", "PATH+MAVEN=${maven}/bin"]){
        body.call()
    }
}


