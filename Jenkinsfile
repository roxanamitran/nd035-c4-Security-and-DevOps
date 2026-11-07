pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Test') {
            steps { dir('starter_code') { sh 'mvn -B test' } }
        }
        stage('Package JAR') {
            steps { dir('starter_code') { sh 'mvn -B package -DskipTests' } }
        }
        stage('Build image') {
            steps { sh 'docker build -t sareeta-app:latest starter_code' }
        }
        stage('Deploy container') {
            steps {
                sh '''
                  docker rm -f sareeta-app || true
                  docker run -d --name sareeta-app -p 8081:8080 sareeta-app:latest
                '''
            }
        }
    }
}
