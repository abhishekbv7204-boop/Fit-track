# Gym Management System

This is a simple college-project Gym Management System built with:

- Frontend: HTML, CSS, JavaScript
- Backend: Java HTTP Server + JDBC
- Database: MySQL

## Folder Structure

```text
gym-new
├── backend
│   └── src/com/gym
│       ├── Main.java
│       ├── dao
│       ├── models
│       ├── server
│       └── utils
├── database
│   └── gym_db.sql
├── frontend
│   ├── index.html
│   ├── style.css
│   └── app.js
└── lib
    └── mysql-connector-java-8.0.30.jar
```

## Database Setup

1. Start MySQL.
2. Open MySQL Workbench, phpMyAdmin, or command line.
3. Run the SQL file:

```text
database/gym_db.sql
```

Default login:

```text
Username: admin
Password: admin123
```

## VS Code Setup

1. Open this `gym-new` folder in VS Code.
2. Install the "Extension Pack for Java" if needed.
3. Make sure the MySQL connector jar is inside the `lib` folder.
4. Open `backend/src/com/gym/Main.java`.
5. Click Run.
6. Open:

```text
http://localhost:5000
```

If you run from the terminal, use this from the project folder:

```text
javac -cp "lib\mysql-connector-java-8.0.30.jar" -d backend\bin (Get-ChildItem -Recurse backend\src -Filter *.java).FullName
java -cp "backend\bin;lib\mysql-connector-java-8.0.30.jar" com.gym.Main
```

If you are already inside `backend/src`, use:

```text
javac -cp "..\..\lib\mysql-connector-java-8.0.30.jar" (Get-ChildItem -Recurse com -Filter *.java).FullName
java -cp ".;..\..\lib\mysql-connector-java-8.0.30.jar" com.gym.Main
```

The server defaults to port `5000`. You can still pass a different port as an argument if needed.

## MySQL Settings

The backend reads these environment variables if present:

```text
GYM_DB_URL
GYM_DB_USER
GYM_DB_PASSWORD
```

If they are not present, it uses:

```text
jdbc:mysql://localhost:3306/gym_db
root
password: ---
```

You can change these values in:

```text
backend/src/com/gym/utils/DBConnection.java
```

When rows are added, edited, or deleted through the frontend, the backend updates MySQL and rewrites:

```text
database/gym_db.sql
```
