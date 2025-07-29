# java_project_space
## Compilar
javac -d bin src/main/java/br/edu/espacos/auth/*.java src/main/java/br/edu/espacos/client/*.java src/main/java/br/edu/espacos/model/*.java src/main/java/br/edu/espacos/server/*.java src/main/java/br/edu/espacos/storage/*.java src/main/java/br/edu/espacos/view/*.java src/main/java/br/edu/espacos/App.java

## Server

java -cp bin br.edu.espacos.server.EspacosServer

## cliente


java -cp bin br.edu.espacos.App