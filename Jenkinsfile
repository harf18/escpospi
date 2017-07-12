node{
    stage("checkout"){
        git url:'https://github.com/harf18/escpospi.git', branch:'develop'
        echo BRANCH_NAME
    }
    
    stage("maven"){
        withMaven { sh 'mvn clean verify' }
    }
    
    stage("Archive"){
    	archiveArtifacts artifacts: '**/target/*.jar'
    }

    stage("notify"){
    	slackSend channel: '#team-vem', color: 'good', message: 'Deploy : http://localhost:8080/job/W3-deploy-PREPROD/build?token=OAKej5o40yY9XqyFiY8b7DEYY6qv5XPd', teamDomain: 'devlescrous', tokenCredentialId : '1'
    }
}

def withMaven(def body){
    def java = tool 'oracle-8u131'
    def maven = tool 'maven-3.5.0'

    withEnv(["JAVA_HOME=${java}", "PATH+MAVEN=${maven}/bin"]){
        body.call()
    }
}


