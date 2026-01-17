# minicommerceAPI

![CI](https://github.com/batuhanmkrk/minicommerce/actions/workflows/ci.yml/badge.svg)
[![codecov](https://codecov.io/gh/batuhanmkrk/minicommerce/branch/main/graph/badge.svg)](https://codecov.io/gh/batuhanmkrk/minicommerce)

Mini bir e-ticaret senaryosu icin gelistirilmis REST API. Projede esas odak, duzgun katman ayrimi (service/repository),
dogru HTTP kodlari, OpenAPI 3 dokumantasyonu ve testlerle (unit + integration + e2e) davranisin dogrulanmasidir.

## Tech Stack

- Java 17, Spring Boot
- Spring Web, Validation
- Spring Data JPA (Hibernate)
- Database: SQLite (file-based)
- OpenAPI 3.0+: springdoc-openapi + Swagger UI
- Tests: JUnit 5, Mockito, Spring Boot Test, MockMvc, TestRestTemplate
- Coverage: JaCoCo
- CI: GitHub Actions

## Setup

Requirements:

- Java 17+
- Gradle 9+

Run tests:

```bash
./gradlew test
```

Run the application:

```bash
./gradlew bootRun
```

Database file (dev):

- `./minicommerce.db`

## Swagger / OpenAPI (dev)

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## API Resources

Base path: `/api`

- Users: `/users`
- Categories: `/categories`
- Products: `/products`
- Orders: `/orders`
- Reviews: `/reviews`

## Ornek istekler (curl)

### Kullanici olustur

```bash
curl -s -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d '{"name":"Ayse Yilmaz","email":"ayse@ornek.com"}'
```

### Kategori olustur

```bash
curl -s -X POST http://localhost:8080/api/categories -H "Content-Type: application/json" -d '{"name":"Giyim"}'
```

### Urun olustur

```bash
curl -s -X POST http://localhost:8080/api/products -H "Content-Type: application/json" -d '{"name":"Mont","sku":"SKU-TR-1","price":999.90,"stock":10,"categoryId":1}'
```

### Urun guncelle (PATCH)

```bash
curl -s -X PATCH http://localhost:8080/api/products/1 -H "Content-Type: application/json" -d '{"price":899.90,"stock":8}'
```

### Siparis olustur

```bash
curl -s -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" -d '{"userId":1,"items":[{"productId":1,"quantity":2}]}'
```

### Siparis durum guncelle (PATCH)

```bash
curl -s -X PATCH http://localhost:8080/api/orders/1 -H "Content-Type: application/json" -d '{"status":"PAID"}'
```

### Yorum olustur

```bash
curl -s -X POST http://localhost:8080/api/reviews -H "Content-Type: application/json" -d '{"userId":1,"productId":1,"rating":5,"comment":"Gayet iyi"}'
```

### Yorum guncelle (PATCH)

```bash
curl -s -X PATCH http://localhost:8080/api/reviews/1 -H "Content-Type: application/json" -d '{"rating":4,"comment":"Hala iyi"}'
```

## Coverage

- JaCoCo report: `target/site/jacoco/index.html`

## Testing

This project includes comprehensive test coverage:

- **Unit Tests**: Service layer tests with Mockito
- **Integration Tests**: Full API endpoint tests with MockMvc
- **E2E Tests**: End-to-end workflow tests (5 scenarios, 9 test methods)

### E2E Test Scenarios

1. **User Registration → Order Placement**: Complete shopping workflow
2. **Product Lifecycle**: CRUD operations and inventory management
3. **Review Workflow**: Purchase, review, update, delete
4. **Catalog Management**: Multi-category organization
5. **Multi-User Shopping**: Concurrent users, orders, and reviews

See [E2E_TEST_DOCUMENTATION.md](E2E_TEST_DOCUMENTATION.md) for detailed information.

## Notes

- Tum endpoint'ler Swagger UI uzerinden goruntulenebilir ve request/response semalari OpenAPI 3 ile dokumante edilir.
- Testler, basarili senaryolarin yaninda validation / not-found / conflict gibi hata durumlarini da kapsar.
