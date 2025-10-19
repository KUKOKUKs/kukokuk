
# 이미지를 만들 때 실행
# Java 17이 포함된 경량 이미지 사용 (빌드와 실행 모두 가능)
FROM openjdk:17-jdk-alpine AS build

# 2️⃣ 컨테이너 내부 작업 디렉토리 설정
WORKDIR /app

# 3️⃣ Gradle wrapper 및 빌드 관련 파일 복사
#    - 의존성 캐시를 위해 build.gradle, settings.gradle, gradlew 등을 먼저 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# 4️⃣ 의존성 미리 다운로드 (캐시를 활용해서 빌드 속도 향상)
RUN ./gradlew dependencies --no-daemon

# 5️⃣ 실제 프로젝트 소스 코드 복사
COPY . .

#  gradlew 실행 권한 부여 (권한 문제 해결)
RUN chmod +x gradlew
# 6️⃣ Gradle로 프로젝트 빌드 (테스트는 생략) - jar 파일 생성
RUN ./gradlew clean bootJar --no-daemon -x test

# 이미지를 부팅할 때 실행
# 7️⃣ 실행 전용 이미지(Stage 분리: build와 run을 분리해서 용량 줄이기)
FROM openjdk:17-jdk-alpine

# 8️⃣ 컨테이너 내부 실행 경로 지정
WORKDIR /app

# 9️⃣ 위에서 빌드된 jar 파일을 실행 이미지로 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 🔟 컨테이너가 외부로 노출할 포트
EXPOSE 8080

# 11️⃣ 컨테이너 실행 시 jar 파일을 실행하는 명령어
ENTRYPOINT ["java","-jar","/app/app.jar"]