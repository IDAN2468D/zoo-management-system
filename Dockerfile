# שלב 1: בניית הפרויקט ויצירת קובץ ה-JAR
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# העתקת קובץ התלויות וקוד המקור
COPY pom.xml .
COPY src ./src

# קומפילציה ואריזה לקובץ JAR (דילוג על טסטים בזמן הבנייה)
RUN mvn clean package -DskipTests

# שלב 2: סביבת הריצה (קלה, מאובטחת ומהירה)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# העתקת ה-JAR שנבנה בשלב הראשון
COPY --from=build /app/target/*.jar app.jar

# חשיפת פורט 9091
EXPOSE 9091

# פקודת ההרצה של השרת
ENTRYPOINT ["java", "-jar", "app.jar"]
