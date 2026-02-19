## 🚀 Guía de Integración: Frontend Angular + Microservicios (JWT)
Hola compañero, el Backend ya está operativo bajo una arquitectura de microservicios protegida. Para que el Frontend pueda comunicarse correctamente, debemos seguir estos estándares de seguridad y conectividad.

### 1. Datos de Conexión (Endpoint Único)
   No debes llamar a cada microservicio por separado. Todo debe pasar por el API Gateway, que actúa como único punto de entrada.

- **Base URL**: ```http://localhost:8080```

- **Rutas de Autenticación**: ```/auth/login``` y ```/auth/register```

- **Rutas de Negocio (Protegidas)**: ```/api/orders/**```, ```/api/shipping/**```, ```/api/notifications/**```

### 2. Flujo de Autenticación con JWT
- **Login**: Haz un POST a ```http://localhost:8080/auth/login``` enviando ```userName``` y ```password```.

- **Almacenamiento**: El backend responderá con una cadena de texto (el Token). Guárdalo en el ```localStorage```.

- **Seguridad**: Para cualquier petición a las rutas /api/**, el Gateway rechazará la entrada si no incluyes el header:
Authorization: Bearer <tu_token_aquí>

### 3. Implementación del Interceptor (CRUCIAL)
   En Angular, no añadas el token manualmente en cada servicio. Crea un **HttpInterceptor** para automatizar el proceso. Este interceptor añade el token a las cabeceras y maneja errores de sesión expirada (401).

**Código del Interceptor** ```auth.interceptor.ts)```:

```
import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

constructor(private router: Router) {}

intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
const token = localStorage.getItem('token');

    // 1. Clonar la petición e inyectar el token si existe
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    // 2. Manejar la respuesta y capturar errores 401 (Unauthorized)
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // Si el token expiró o es inválido, limpiamos y redirigimos al login
          localStorage.removeItem('token');
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      })
    );
}
}
```

### 4. Estructura de Servicios Sugerida
   Para mantener el proyecto escalable, crea un servicio por dominio:

- ```AuthService```: Maneja ```login()```, ```register()``` y el estado de la sesión.

- ```OrderService```: Peticiones a ```/api/orders```.

- ```ShippingService```: Peticiones a ```/api/shipping/calculate?peso=X```.

### 5. Modelos de Datos (DTOs)
   Para el registro de usuario, el objeto JSON debe ser:

```
{
"userName": "ejemplo",
"password": "123",
"email": "correo@test.com",
"role": "USER"
}
```

### 6. Consideración de CORS
   El Gateway ya está configurado para permitir peticiones desde ```http://localhost:4200``` (puerto por defecto de Angular). Si decides usar otro puerto, avísame para actualizar la lista blanca en el Backend.

### 7. 🗄️ Configuración de la Base de Datos (PostgreSQL)
El proyecto utiliza **PostgreSQL** como motor de base de datos. Cada microservicio apunta a una base de datos específica para mantener el aislamiento de datos (Arquitectura de Microservicios).

### a. Scripts de Creación
   Ejecuta los siguientes comandos en tu terminal de PostgreSQL o en tu herramienta de gestión (pgAdmin / DBeaver):

```
-- Crear la base de datos para el microservicio de Autenticación
CREATE DATABASE db_auth;

-- Crear la base de datos para el microservicio de Órdenes
CREATE DATABASE db_orders;

-- Crear la base de datos para el microservicio de Envíos
CREATE DATABASE db_shipping;
```

### b. Configuración de Conexión
   Asegúrate de que el archivo src/main/resources/application.yml de cada microservicio tenga las credenciales correctas. Ejemplo para ms-auth:


```
spring:
datasource:
url: jdbc:postgresql://localhost:5432/db_auth
username: tu_usuario_postgres
password: tu_password_postgres
driver-class-name: org.postgresql.Driver
jpa:
hibernate:
ddl-auto: update # Crea las tablas automáticamente al iniciar
show-sql: true
```
---


## 📨 Configuración de Mensajería (RabbitMQ)
El sistema utiliza **RabbitMQ** para la comunicación asíncrona entre el microservicio de órdenes (``ms-orders``) y el de notificaciones (``ms-notifications``).
### 1. Requisitos de Instalación
Es necesario tener instalado RabbitMQ y Erlang en tu máquina local.
- **Host**: localhost
- **Puerto**: 5672
- **Panel de Administración**: ``http://localhost:15672`` (Usuario/Password por defecto: ``guest``/``guest``)

### 2. Infraestructura de Mensajería
Spring Boot creará automáticamente los componentes necesarios al iniciar los servicios. La configuración lógica es la siguiente:
- **Exchange**: ``correo.exchange`` (Tipo: Direct)
- **Queue**: ``cola.correos``
- **Routing Key**: ``envio.correo``

### 3. Configuración en application.yml
Todos los microservicios que utilicen mensajería deben tener el siguiente bloque de configuración:
```
spring:
rabbitmq:
host: localhost
port: 5672
username: guest
password: guest
```
---

### 🛠️ Guía de Ejecución del Proyecto (Orden Sugerido)
Para que el ecosistema de microservicios arranque sin errores, sigue este orden:
1. Infraestructura Base: Inicia PostgreSQL y RabbitMQ.
2. Service Discovery: Inicia ``eureka-server`` (Puerto 8761).
3. Configuración de Seguridad: Inicia ``ms-auth``.
4. Puerta de Enlace: Inicia ``api-gateway`` (Puerto 8080).
5. Servicios de Negocio: Inicia ``ms-orders``, ``ms-shipping`` y ``ms-notifications``.
---

## 📋 Resumen de Puertos

| Servicio           | Puerto | Descripción                                  |
|--------------------|--------|----------------------------------------------|
| ``eureka-server  ``    | 8761   | Dashboard de Microservicios                  |
| ``api-gateway``        | 8080   | **Punto de entrada único (CORS habilitado)** |
| ``ms-auth ``           | 8089   | Gestión de JWT y Usuarios                    |
| ``ms-orders``          | 8081   | Lógica de pedidos                            |
| ``ms-shipping``        | 8082   | Cálculo de tarifas de envío                  |
| ``ms-notifications ``  | 8083   | Worker de envío de correos                   |

