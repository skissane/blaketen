pipeline {
    agent any

    stages {
        stage('build') {
            steps {
                sh 'docker/run ./gradlew setupExe'
	    }
        }
    }
}
