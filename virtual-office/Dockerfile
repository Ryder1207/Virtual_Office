FROM eclipse-temurin:21-jdk-alpine
# 設定工作目錄
WORKDIR /app
# 將所有檔案複製進去
COPY . .
# 執行 Maven 打包 (跳過測試以加快速度)
RUN ./mvnw clean package -DskipTests
# 執行程式
CMD ["java", "-jar", "target/virtual_office-0.0.1-SNAPSHOT.jar"]