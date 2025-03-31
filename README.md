# netty api server

- 자바 네트워크 소녀 Netty 의 api-server 예제를 기본으로 수정 및 구현 연습
  - spring 6
  - spring-data-redis
  - jpa hibernate
  - h2
- 실행 시 redis-server 가 기본 설정으로 실행 되어 있어야 함.

# 수정 사항
- spring 의존성 제거
- 패키지 재구성
- 코드 리팩토링

# 테스트 요청
curl -X GET http://localhost:8080/users -H "email: alex@mail.com"
curl -X POST http://localhost:8080/tokens -d "userId=1&password=passworda"
curl -X GET http://localhost:8080/tokens -H "token: token:alex@mail.com"
curl -X DELETE http://localhost:8080/tokens -H "token: token:alex@mail.com"
