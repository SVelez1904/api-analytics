# api-analytics



[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen)](https://spring.io/projects/spring-boot)
[![Apache Kafka - Consumer](https://img.shields.io/badge/Kafka--Consumer-29092-black?logo=apachekafka)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-4169E1?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Container-blue?logo=docker)](https://www.docker.com/)

Este microservicio (`api-analytics`) se encarga de consumir de forma asíncrona todos los eventos de asignación y actualización generados por `api-proyectos`. Su propósito es procesar métricas en tiempo real, auditorías de rendimiento de usuarios y generar reportes analíticos sin impactar el rendimiento de la API transaccional.

---

##  Responsabilidades en la Arquitectura

1. **Consumidor Reactivo:** Escucha activamente los mensajes del broker de Kafka del tópico `proyecto-asignaciones`.
2. **Procesamiento Analítico:** Consolida la información histórica de qué usuarios participan en qué proyectos para medir métricas de carga de trabajo.
3. **Persistencia Relacional** Realiza reportes de forma eficiente y segura a la base de datos SQL.

---

##  Configuración del Entorno Docker (`application.properties`)

Este servicio opera principalmente como **Consumer**, por lo que requiere la configuración del `group-id` para coordinar la lectura de particiones dentro de Docker:

```properties
server.port=8082

# Configuración del Consumidor de Kafka (Interno Docker)
spring.kafka.bootstrap-servers=kafka:29092
spring.kafka.consumer.group-id=analytics-group
spring.kafka.consumer.auto-offset-reset=earliest

# Deserializadores (Transforman el JSON de la red de vuelta a Objetos Java)
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer


spring.kafka.consumer.properties.spring.json.trusted.packages=com.innovatech.api_proyectos.model,com.innovatech.api_analytics.dto
