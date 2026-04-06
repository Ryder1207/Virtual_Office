FROM eclipse-temurin:21-jdk-alpine
# 設定工作目錄
WORKDIR /app
# 將所有檔案複製進去
COPY . .
# 2. 賦予執行權限並打包
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# 3. 正確的啟動指令 (明確指定檔名，注意是橫線 -)
CMD ["java", "-jar", "target/virtual-office-0.0.1-SNAPSHOT.jar"]