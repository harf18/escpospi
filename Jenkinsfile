node{
    stage("checkout"){
        git url:'https://github.com/harf18/escpospi.git', branch:'develop'
        echo BRANCH_NAME
    }
    
    stage("maven"){
        withMaven { sh 'mvn clean verify' }
    }
    
    stage("copy"){
    	if  (BRANCH_NAME == 'master') {
        	echo BRANCH_NAME
        	step ([$class: 'CopyArtifact', projectName: 'W3-deploy-PROD', filter: 'target/*.jar']);
        } else if (BRANCH_NAME.startsWith('test')){
        	echo 'YES !' + BRANCH_NAME
        	step ([$class: 'CopyArtifact', projectName: 'W3-deploy-PREPROD', filter: 'target/*.jar']);
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
