# IssueTracker

## Package
```
./gradlew distZip
```

# issuetracker-with-http4k

## http4k 를 사용한 이슈관리 시스템 구현 (스프링 아웃!)
https://www.http4k.org/

1. 로그인, 인증 보완
- [X] 쿠키로 로그인 여부를 컨텍스트에서 가져올 수 있다.
- [X] path 별로 인증필터를 추가한다.
- [X] github api 연동한다.
- [ ] 쿠키가 만료되면 재발행한다. (refresh token 구현)

2. 테이블 설계
- [X] user, issue, label, assignee, comment
- [X] h2 연동 테스트

3. Api 설계
- issue 생성
- issue detail 조회
- issue update api
- issue 목록 조회 with 검색필터
- 페이징 추가
- comment api

4. api 데이터 기반 화면 개발
