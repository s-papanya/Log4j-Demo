# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app
# คัดลอกไฟล์ pom ก่อน เพื่อ cache dependency
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# คัดลอกซอร์สแล้ว build fat-jar (มี main manifest)
COPY src ./src
RUN mvn -q -DskipTests clean package

# ---------- Runtime stage (Java 8) ----------
FROM eclipse-temurin:8-jre
WORKDIR /app

# คัดลอก jar จากสเตจ build
COPY --from=build /app/target/log4shell-web-0.0.1-SNAPSHOT.jar app.jar

# พอร์ตเว็บ (เปลี่ยนได้ผ่าน env SERVER_PORT)
ENV SERVER_PORT=8080

# (สำคัญสำหรับ PoC) เปิดอนุญาตให้ JNDI โหลดคลาสจาก URL
ENV JAVA_OPTS="-Dcom.sun.jndi.ldap.object.trustURLCodebase=true"

EXPOSE ${SERVER_PORT}
CMD ["sh", "-lc", "java $JAVA_OPTS -jar /app/app.jar --server.port=$SERVER_PORT"]
