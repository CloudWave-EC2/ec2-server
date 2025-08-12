pipeline {
  agent any
  environment {
    IMAGE = "hyeyeon763/java"
    TAG   = "${env.BUILD_ID}"
  }
  stages {
    stage('checkout') { steps { checkout scm } }

    stage('push image') {
      steps {
        script {
          docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-creds') {
            def app = docker.build("${IMAGE}:${TAG}")
            app.push()
            sh "docker tag ${IMAGE}:${TAG} ${IMAGE}:latest"
            sh "docker push ${IMAGE}:latest"
          }
        }
      }
    }

    stage('bump manifest') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'gitops-creds', usernameVariable: 'GU', passwordVariable: 'GT')]) {
          sh """
            rm -rf mani
            git clone https://${GU}:${GT}@github.com/CloudWave-EC2/tmp_Manifest_Repo.git mani  
            cd mani/overlays/dev
            sed -i -E "s|(newTag:\\s*).*|\\1${TAG}|" kustomization.yaml
            git config user.email "ci@example.com"
            git config user.name  "jenkins"
            git add .
            git commit -m "ci: bump ${IMAGE} to ${TAG}" || true
            git push origin main
          """
        }
      }
    }
  }
}
