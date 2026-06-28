 pipeline {
     agent any

     environment {
         DOCKER_REGISTRY = 'registry.battery.com'
         NEXUS_URL = 'https://nexus.battery.com/repository/maven-public/'
     }

     parameters {
         choice(name: 'BUILD_MODE', choices: ['saas-prod', 'onpremise-prod', 'dev'], description: '构建模式')
         string(name: 'ENTERPRISE', defaultValue: '', description: '企业编码(私有化): trina/tuobang')
         booleanParam(name: 'ENABLE_OBFUSCATE', defaultValue: true, description: '启用代码混淆')
         booleanParam(name: 'DEPLOY', defaultValue: false, description: '部署到目标环境')
     }

     stages {
         stage('Checkout') {
             steps { checkout scm }
         }

         stage('Build Platform Modules') {
             steps {
                 sh 'mvn clean install -pl platform/common-core,platform/common-security,platform/common-mybatis,platform/common-mq -DskipTests'
             }
         }

         stage('Build Services') {
             parallel {
                 stage('Gateway') { steps { sh 'mvn clean package -pl services/gateway-service -DskipTests' } }
                 stage('Auth') { steps { sh 'mvn clean package -pl services/auth-service -DskipTests' } }
                 stage('Battery') { steps { sh 'mvn clean package -pl services/battery-service -DskipTests' } }
                 stage('Tenant') { steps { sh 'mvn clean package -pl services/tenant-service -DskipTests' } }
                 stage('Integration') { steps { sh 'mvn clean package -pl services/integration-service -DskipTests' } }
             }
         }

         stage('Obfuscate (Production)') {
             when { expression { params.ENABLE_OBFUSCATE && params.BUILD_MODE != 'dev' } }
             steps {
                 sh '''
                     for service in battery-service auth-service; do
                         echo "Obfuscating: $service"
                         # ProGuard/CandyGuard 混淆命令
                     done
                 '''
             }
         }

         stage('Docker Build & Push') {
             steps {
                 sh '''
                     for service in gateway-service auth-service battery-service tenant-service integration-service; do
                         docker build -t ${DOCKER_REGISTRY}/battery/${service}:${BUILD_NUMBER} services/$service/
                         docker push ${DOCKER_REGISTRY}/battery/${service}:${BUILD_NUMBER}
                     done
                 '''
             }
         }

         stage('Frontend Build') {
             steps {
                 script {
                     if (params.BUILD_MODE == 'onpremise-prod' && params.ENTERPRISE) {
                         sh "cd frontend && npm run build:${params.ENTERPRISE}"
                     } else if (params.BUILD_MODE == 'saas-prod') {
                         sh 'cd frontend && npm run build:saas'
                     } else {
                         sh 'cd frontend && npm run build'
                     }
                 }
             }
         }

         stage('Deploy') {
             when { expression { params.DEPLOY } }
             steps {
                 script {
                     if (params.BUILD_MODE == 'saas-prod') {
                         sh "helm upgrade --install battery-passport ./helm-charts --namespace battery-prod -f helm-charts/values/values-saas.yaml --set image.tag=${BUILD_NUMBER}"
                     } else if (params.ENTERPRISE) {
                         sh "helm upgrade --install battery-passport ./helm-charts --namespace battery-${params.ENTERPRISE} -f helm-charts/values/values-${params.ENTERPRISE}.yaml --set image.tag=${BUILD_NUMBER}"
                     }
                 }
             }
         }
     }

     post {
         success { emailext subject: "构建成功: ${BUILD_NUMBER}", body: "电池护照构建完成" }
         failure { emailext subject: "构建失败: ${BUILD_NUMBER}", body: "请检查构建日志" }
     }
 }
