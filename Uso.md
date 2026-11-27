# Proyecto Planilla Fácil: Guía
---
Requisitos
* **Java JDK 26** Y **MAVEN** en IntelliJ IDEA
* **Docker** para tener el contenedor de la base de datos PostgreSQL
* **Node.js vs 20 LTS o superior y npm**

---

Encendido del sistema
### Base de datos
1. Abrir Docker e inciar con "docker compose up -d" en la terminal del
IDE y en la raíz del proyecto

### Servidor 
1. Esperar a que Maven descargue las dependencias
2. Abrir la carpeta raiz "PlanillaFacil"
3. Ubicar la clase "ServidorPlanilla.java" en web.
4. compruebe que esté activo escribiendo en su navegador http://localhost:8080/api/salud

### Frontend 
1. Abra la teminal dentro de "frontend"
2. Instale las dependencias con "npm install"
3. Iniciar el servidor con "npm run dev".
