# Chronicle 프로젝트 어노테이션 완벽 가이드

> **어노테이션(Annotation)이란?**
> `@` 기호로 시작하는 메타데이터. 코드에 추가 정보를 제공하거나, 컴파일/런타임 시 특정 동작을 수행하게 합니다.
> 쉽게 말해, "이 클래스/메서드/필드는 이런 역할이야"라고 알려주는 **라벨** 같은 것입니다.

---

## 목차
1. [Lombok 어노테이션](#1-lombok-어노테이션)
2. [JPA/Persistence 어노테이션](#2-jpapersistence-어노테이션)
3. [Spring Core 어노테이션](#3-spring-core-어노테이션)
4. [Spring Data JPA 어노테이션](#4-spring-data-jpa-어노테이션)
5. [Spring Transaction 어노테이션](#5-spring-transaction-어노테이션)
6. [Validation 어노테이션](#6-validation-어노테이션)
7. [Spring Web 어노테이션](#7-spring-web-어노테이션)
8. [테스트 어노테이션](#8-테스트-어노테이션)

---

## 1. Lombok 어노테이션

> **Lombok이란?**
> Java의 반복적인 코드(보일러플레이트)를 어노테이션 하나로 자동 생성해주는 라이브러리입니다.
> 컴파일 시점에 어노테이션을 읽어서 실제 코드를 생성합니다.

---

### @Getter / @Setter

**역할**: 필드의 Getter/Setter 메서드를 자동 생성

#### 사용하지 않았을 때 (직접 작성)
```java
public class User {
    private String name;
    private int age;

    // Getter 직접 작성 (필드마다 작성해야 함)
    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    // Setter 직접 작성 (필드마다 작성해야 함)
    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
```

#### @Getter, @Setter 사용 시
```java
@Getter
@Setter
public class User {
    private String name;
    private int age;
}
// 끝! 위의 모든 getter/setter가 자동 생성됨
```

#### 내부적으로 생성되는 코드
```java
// 컴파일 시 Lombok이 자동으로 아래 코드를 추가합니다
public class User {
    private String name;
    private int age;

    // @Getter가 생성한 코드
    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    // @Setter가 생성한 코드
    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
```

#### 특정 필드만 적용
```java
public class User {
    @Getter                    // name 필드에만 Getter 생성
    private String name;

    @Getter @Setter            // age 필드에는 둘 다 생성
    private int age;

    private String password;   // 아무것도 생성 안 됨
}
```

---

### @NoArgsConstructor

**역할**: 파라미터가 없는 기본 생성자를 자동 생성

#### 사용하지 않았을 때
```java
public class User {
    private String name;
    private int age;

    // 기본 생성자 직접 작성
    public User() {
    }
}
```

#### @NoArgsConstructor 사용 시
```java
@NoArgsConstructor
public class User {
    private String name;
    private int age;
}
// User user = new User(); 가능!
```

#### 내부적으로 생성되는 코드
```java
public class User {
    private String name;
    private int age;

    // @NoArgsConstructor가 생성한 코드
    public User() {
    }
}
```

#### access 옵션
```java
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    private String name;
}

// 내부적으로 생성되는 코드
public class User {
    private String name;

    // PROTECTED로 지정했으므로 protected 생성자 생성
    protected User() {
    }
}
```

**왜 PROTECTED를 사용할까?**
- JPA는 엔티티를 조회할 때 프록시 객체를 만들어야 해서 기본 생성자가 필요함
- 하지만 외부에서 `new User()`로 빈 객체를 만드는 것은 막고 싶음
- `protected`면 같은 패키지나 상속받은 클래스(JPA 프록시)만 접근 가능

---

### @AllArgsConstructor

**역할**: 모든 필드를 파라미터로 받는 생성자를 자동 생성

#### 사용하지 않았을 때
```java
public class User {
    private String name;
    private int age;
    private String email;

    // 모든 필드를 받는 생성자 직접 작성
    public User(String name, int age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
    }
}
```

#### @AllArgsConstructor 사용 시
```java
@AllArgsConstructor
public class User {
    private String name;
    private int age;
    private String email;
}
// User user = new User("홍길동", 25, "hong@email.com"); 가능!
```

#### 내부적으로 생성되는 코드
```java
public class User {
    private String name;
    private int age;
    private String email;

    // @AllArgsConstructor가 생성한 코드
    // 필드 선언 순서대로 파라미터가 됨
    public User(String name, int age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
    }
}
```

---

### @RequiredArgsConstructor

**역할**: `final` 필드와 `@NonNull` 필드만 파라미터로 받는 생성자를 자동 생성

#### 사용하지 않았을 때
```java
public class UserService {
    private final UserRepository userRepository;  // final = 반드시 초기화 필요
    private final EmailService emailService;      // final = 반드시 초기화 필요

    // 생성자 직접 작성 (Spring이 여기에 의존성 주입)
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public void createUser(String name) {
        // userRepository 사용...
    }
}
```

#### @RequiredArgsConstructor 사용 시
```java
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    // 생성자가 자동 생성됨! Spring이 자동으로 의존성 주입

    public void createUser(String name) {
        // userRepository 사용...
    }
}
```

#### 내부적으로 생성되는 코드
```java
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    // @RequiredArgsConstructor가 생성한 코드
    // final 필드들만 파라미터로 받음
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
}
```

**왜 @Autowired 대신 이걸 쓸까?**
```java
// 옛날 방식 - 필드 주입 (권장하지 않음)
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;  // 테스트하기 어려움
}

// 현재 권장 방식 - 생성자 주입
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;  // 테스트 시 Mock 주입 쉬움
}
```

---

### @Builder

**역할**: Builder 패턴 코드를 자동 생성. 객체를 유연하게 생성할 수 있게 해줌

#### Builder 패턴이란?
```java
// 일반 생성자의 문제점
User user = new User("홍길동", 25, "hong@email.com", "010-1234-5678", "서울시");
// 뭐가 뭔지 모르겠음... 25가 나이인지? 어떤 순서인지?

// Builder 패턴 사용
User user = User.builder()
    .name("홍길동")
    .age(25)
    .email("hong@email.com")
    .phone("010-1234-5678")
    .address("서울시")
    .build();
// 명확하게 어떤 필드에 뭘 넣는지 알 수 있음!
```

#### 사용하지 않았을 때 (Builder 패턴 직접 구현)
```java
public class User {
    private String name;
    private int age;
    private String email;

    // private 생성자
    private User(Builder builder) {
        this.name = builder.name;
        this.age = builder.age;
        this.email = builder.email;
    }

    // Builder 클래스 직접 작성 (매우 길다...)
    public static class Builder {
        private String name;
        private int age;
        private String email;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
```

#### @Builder 사용 시
```java
@Builder
public class User {
    private String name;
    private int age;
    private String email;
}
// 위의 모든 Builder 코드가 자동 생성됨!
```

#### 사용 예시
```java
// 모든 필드 설정
User user1 = User.builder()
    .name("홍길동")
    .age(25)
    .email("hong@email.com")
    .build();

// 일부 필드만 설정 (나머지는 null 또는 기본값)
User user2 = User.builder()
    .name("김철수")
    .build();
```

#### 생성자에 @Builder 적용 (권장)
```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    private Long id;
    private String name;
    private int age;

    @Builder  // 생성자에 적용하면 id 제외 가능
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

// id 없이 생성 가능
User user = User.builder()
    .name("홍길동")
    .age(25)
    .build();
```

---

### @Builder.Default

**역할**: Builder 사용 시 기본값 설정

#### 문제 상황
```java
@Builder
public class Task {
    private String title;
    private Boolean completed;  // Builder로 생성하면 null이 됨!
}

Task task = Task.builder()
    .title("공부하기")
    .build();  // completed가 null!
```

#### @Builder.Default 사용
```java
@Builder
public class Task {
    private String title;

    @Builder.Default
    private Boolean completed = false;  // 기본값 false
}

Task task = Task.builder()
    .title("공부하기")
    .build();  // completed가 false!
```

---

### @Data (주의: Entity에서는 사용 금지!)

**역할**: @Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor 한번에

```java
@Data
public class UserDto {
    private String name;
    private int age;
}

// 위 코드는 아래와 동일
@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class UserDto {
    private String name;
    private int age;
}
```

**왜 Entity에서 사용하면 안 될까?**
- `@Setter`: 엔티티는 함부로 값을 변경하면 안 됨
- `@ToString`: 연관관계가 있으면 무한루프 발생 가능
- `@EqualsAndHashCode`: 연관관계 필드 포함 시 문제 발생

---

## 2. JPA/Persistence 어노테이션

> **JPA(Java Persistence API)란?**
> 자바 객체를 데이터베이스 테이블에 자동으로 저장/조회해주는 기술입니다.
> 어노테이션으로 "이 클래스는 이 테이블이야", "이 필드는 이 컬럼이야"라고 알려줍니다.

---

### @Entity

**역할**: "이 클래스는 데이터베이스 테이블과 매핑되는 엔티티입니다"라고 선언

```java
@Entity  // JPA야, 이 클래스는 DB 테이블이야!
public class User {
    @Id
    private Long id;
    private String name;
}
```

**동작 원리**
```
Java 객체                         DB 테이블
┌──────────────┐                 ┌──────────────┐
│ @Entity      │                 │ USER 테이블   │
│ class User   │  ←── JPA ───→   ├──────────────┤
│   id         │                 │ ID (PK)      │
│   name       │                 │ NAME         │
└──────────────┘                 └──────────────┘
```

---

### @Table

**역할**: 엔티티와 매핑할 테이블의 상세 정보 설정

```java
@Entity
@Table(name = "daily_notes")  // 테이블명을 daily_notes로 지정
public class DailyNote {
    // ...
}
```

#### 인덱스와 유니크 제약조건 설정
```java
@Entity
@Table(
    name = "daily_notes",
    // 인덱스 설정: 검색 속도 향상
    indexes = @Index(name = "idx_user_date", columnList = "user_id, date"),
    // 유니크 제약조건: user_id + date 조합은 유일해야 함
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"})
)
public class DailyNote {
    private Long userId;
    private LocalDate date;
}
```

**위 설정이 만드는 SQL**
```sql
CREATE TABLE daily_notes (
    -- 컬럼들...
);

-- 인덱스 생성
CREATE INDEX idx_user_date ON daily_notes (user_id, date);

-- 유니크 제약조건
ALTER TABLE daily_notes ADD CONSTRAINT ... UNIQUE (user_id, date);
```

---

### @Id

**역할**: 엔티티의 기본키(Primary Key) 필드 지정

```java
@Entity
public class User {
    @Id  // 이 필드가 기본키입니다
    private Long id;

    private String name;
}
```

**기본키란?**
- 테이블에서 각 행(row)을 유일하게 식별하는 컬럼
- 중복 불가, NULL 불가

---

### @GeneratedValue

**역할**: 기본키 값을 자동으로 생성하는 전략 지정

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // DB가 자동으로 1, 2, 3, ... 부여
}
```

**전략 종류**

| 전략 | 설명 | 사용 DB |
|------|------|---------|
| `IDENTITY` | DB의 AUTO_INCREMENT 사용 | MySQL, PostgreSQL |
| `SEQUENCE` | DB 시퀀스 사용 | Oracle, PostgreSQL |
| `TABLE` | 키 생성 전용 테이블 사용 | 모든 DB |
| `AUTO` | DB에 맞게 자동 선택 | 모든 DB |

```java
// MySQL/PostgreSQL에서 주로 사용
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

// 위 설정으로 생성되는 SQL
// CREATE TABLE user (
//     id BIGINT AUTO_INCREMENT PRIMARY KEY,
//     ...
// );
```

---

### @Column

**역할**: 필드와 데이터베이스 컬럼 매핑 정보 설정

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name")  // 컬럼명을 user_name으로 지정
    private String name;

    @Column(nullable = false)  // NOT NULL 제약조건
    private String email;

    @Column(length = 500)  // VARCHAR(500)
    private String description;

    @Column(columnDefinition = "TEXT")  // 컬럼 타입 직접 지정
    private String content;

    @Column(updatable = false)  // UPDATE 시 이 컬럼은 변경 불가
    private LocalDateTime createdAt;

    @Column(unique = true)  // 유니크 제약조건
    private String username;
}
```

**주요 속성 정리**

| 속성 | 설명 | 기본값 |
|------|------|--------|
| `name` | 컬럼명 | 필드명 |
| `nullable` | NULL 허용 여부 | true |
| `length` | 문자열 길이 (VARCHAR) | 255 |
| `columnDefinition` | 컬럼 타입 직접 지정 | - |
| `updatable` | UPDATE 가능 여부 | true |
| `insertable` | INSERT 가능 여부 | true |
| `unique` | 유니크 제약조건 | false |

**생성되는 SQL 예시**
```sql
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(255),           -- @Column(name = "user_name")
    email VARCHAR(255) NOT NULL,      -- @Column(nullable = false)
    description VARCHAR(500),          -- @Column(length = 500)
    content TEXT,                      -- @Column(columnDefinition = "TEXT")
    created_at TIMESTAMP,              -- @Column(updatable = false)
    username VARCHAR(255) UNIQUE       -- @Column(unique = true)
);
```

---

### @MappedSuperclass

**역할**: 여러 엔티티의 공통 필드를 부모 클래스로 분리

#### 문제 상황: 모든 엔티티에 생성일/수정일이 필요함
```java
@Entity
public class User {
    @Id
    private Long id;
    private String name;
    private LocalDateTime createdAt;   // 중복!
    private LocalDateTime updatedAt;   // 중복!
}

@Entity
public class Post {
    @Id
    private Long id;
    private String title;
    private LocalDateTime createdAt;   // 중복!
    private LocalDateTime updatedAt;   // 중복!
}
```

#### @MappedSuperclass로 해결
```java
@MappedSuperclass  // 이 클래스는 테이블이 아니야, 공통 필드 모음이야
@Getter
public abstract class BaseEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

@Entity
public class User extends BaseEntity {  // BaseEntity 상속
    @Id
    private Long id;
    private String name;
    // createdAt, updatedAt은 상속받음!
}

@Entity
public class Post extends BaseEntity {  // BaseEntity 상속
    @Id
    private Long id;
    private String title;
    // createdAt, updatedAt은 상속받음!
}
```

**테이블 구조**
```
USER 테이블                         POST 테이블
┌─────────────────────┐            ┌─────────────────────┐
│ id                  │            │ id                  │
│ name                │            │ title               │
│ created_at (상속)   │            │ created_at (상속)   │
│ updated_at (상속)   │            │ updated_at (상속)   │
└─────────────────────┘            └─────────────────────┘

BaseEntity 테이블은 생성되지 않음!
```

---

### @EntityListeners

**역할**: 엔티티의 생명주기 이벤트(생성, 수정 등)를 감지하는 리스너 등록

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)  // 생성/수정 시간 자동 기록 리스너
public abstract class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**동작 원리**
```
1. User 엔티티 저장 (save)
     ↓
2. AuditingEntityListener가 감지
     ↓
3. @CreatedDate 필드에 현재 시간 자동 설정
     ↓
4. DB에 저장

1. User 엔티티 수정 (update)
     ↓
2. AuditingEntityListener가 감지
     ↓
3. @LastModifiedDate 필드에 현재 시간 자동 설정
     ↓
4. DB에 저장
```

---

### @Enumerated

**역할**: Java Enum 타입을 DB에 저장하는 방식 지정

```java
public enum Role {
    USER,    // 0번
    ADMIN    // 1번
}

@Entity
public class User {
    @Id
    private Long id;

    // 방법 1: 숫자로 저장 (기본값, 비권장)
    @Enumerated(EnumType.ORDINAL)
    private Role role;  // DB에 0 또는 1로 저장

    // 방법 2: 문자열로 저장 (권장!)
    @Enumerated(EnumType.STRING)
    private Role role;  // DB에 "USER" 또는 "ADMIN"으로 저장
}
```

**왜 STRING을 권장할까?**
```java
// ORDINAL의 위험성
public enum Role {
    USER,     // 0
    ADMIN     // 1
}

// 나중에 GUEST를 추가하면?
public enum Role {
    GUEST,    // 0 (새로 추가)
    USER,     // 1 (원래 0이었음!)
    ADMIN     // 2 (원래 1이었음!)
}
// 기존 데이터가 다 꼬임!

// STRING이면 안전
// DB에 "USER", "ADMIN"으로 저장되어 있어서 순서 변경해도 OK
```

---

### 연관관계 어노테이션

#### @ManyToOne (다대일)
```java
// 여러 개의 Task가 하나의 DailyNote에 속함
@Entity
public class Task {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // 지연 로딩 권장
    @JoinColumn(name = "daily_note_id")  // 외래키 컬럼명
    private DailyNote dailyNote;
}
```

#### @OneToMany (일대다)
```java
@Entity
public class DailyNote {
    @Id
    private Long id;

    @OneToMany(mappedBy = "dailyNote")  // Task의 dailyNote 필드가 주인
    private List<Task> tasks = new ArrayList<>();
}
```

**관계 그림**
```
DailyNote (1) ────────< Task (N)
   │                      │
   │ id = 1               │ id = 1, daily_note_id = 1
   │                      │ id = 2, daily_note_id = 1
   │                      │ id = 3, daily_note_id = 1
```

#### @OneToOne (일대일)
```java
@Entity
public class User {
    @Id
    private Long id;

    @OneToOne(mappedBy = "user")
    private SlackIntegration slackIntegration;
}

@Entity
public class SlackIntegration {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
```

#### FetchType 설명
```java
// EAGER: 즉시 로딩 - 연관 엔티티를 항상 함께 조회
@ManyToOne(fetch = FetchType.EAGER)  // 기본값이지만 비권장

// LAZY: 지연 로딩 - 실제 사용할 때 조회 (권장!)
@ManyToOne(fetch = FetchType.LAZY)

// 예시
DailyNote note = repository.findById(1L);  // DailyNote만 조회
// LAZY면 tasks는 아직 조회 안 함
List<Task> tasks = note.getTasks();  // 이 시점에 tasks 조회!
```

---

## 3. Spring Core 어노테이션

> **Spring Framework란?**
> Java 애플리케이션 개발을 편하게 해주는 프레임워크입니다.
> 객체 생성과 의존성 관리를 자동으로 해줍니다.

---

### @SpringBootApplication

**역할**: Spring Boot 애플리케이션의 시작점. 3개 어노테이션의 조합

```java
@SpringBootApplication  // 이것 하나로 모든 설정 완료!
public class KanvaApplication {
    public static void main(String[] args) {
        SpringApplication.run(KanvaApplication.class, args);
    }
}
```

**내부적으로 포함된 어노테이션**
```java
// @SpringBootApplication은 아래 3개를 합친 것
@Configuration           // 이 클래스는 설정 클래스야
@EnableAutoConfiguration // 필요한 설정을 자동으로 해줘
@ComponentScan          // 이 패키지부터 컴포넌트를 찾아서 등록해줘
public class KanvaApplication { }
```

---

### @Configuration

**역할**: 이 클래스가 Spring 설정 클래스임을 선언

```java
@Configuration  // 이 클래스는 설정 파일 역할을 해
public class JpaConfig {

    @Bean  // 이 메서드가 반환하는 객체를 Spring이 관리해줘
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
```

---

### @Component / @Service / @Repository / @Controller

**역할**: 클래스를 Spring Bean으로 등록

```java
@Component      // 일반적인 컴포넌트
public class MyComponent { }

@Service        // 비즈니스 로직 담당 (Service 계층)
public class UserService { }

@Repository     // 데이터 접근 담당 (Repository 계층)
public class UserRepository { }

@Controller     // 웹 요청 처리 (Controller 계층)
public class UserController { }
```

**계층 구조**
```
┌─────────────────────────────────────────────────────────┐
│                    @Controller                          │
│                 (웹 요청 받음)                           │
│                       ↓                                 │
├─────────────────────────────────────────────────────────┤
│                     @Service                            │
│                 (비즈니스 로직)                          │
│                       ↓                                 │
├─────────────────────────────────────────────────────────┤
│                   @Repository                           │
│                 (DB 접근)                                │
└─────────────────────────────────────────────────────────┘
```

**Spring Bean이란?**
- Spring이 관리하는 객체
- Spring 컨테이너가 객체 생성, 소멸을 관리
- 필요한 곳에 자동으로 주입(DI)

---

### @Autowired vs @RequiredArgsConstructor

**의존성 주입(DI)이란?**
```java
// DI 없이: 직접 객체 생성
public class UserService {
    private UserRepository userRepository = new UserRepository();  // 직접 생성
}

// DI 사용: Spring이 알아서 주입
public class UserService {
    private final UserRepository userRepository;  // Spring이 주입해줌
}
```

**방법 1: 필드 주입 (비권장)**
```java
@Service
public class UserService {
    @Autowired  // Spring이 여기에 주입
    private UserRepository userRepository;

    // 문제점: 테스트 시 Mock 주입이 어려움
}
```

**방법 2: 생성자 주입 (권장)**
```java
@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired  // 생성자가 하나면 생략 가능
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// Lombok으로 간단하게!
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    // 생성자 자동 생성, Spring이 자동 주입
}
```

---

## 4. Spring Data JPA 어노테이션

---

### @EnableJpaAuditing

**역할**: JPA Auditing 기능 활성화 (생성일/수정일 자동 기록)

```java
@Configuration
@EnableJpaAuditing  // 이거 없으면 @CreatedDate, @LastModifiedDate 작동 안 함!
public class JpaConfig {
}
```

---

### @CreatedDate / @LastModifiedDate

**역할**: 엔티티 생성/수정 시간을 자동으로 기록

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {

    @CreatedDate  // INSERT 시 현재 시간 자동 설정
    @Column(updatable = false)  // 수정 불가
    private LocalDateTime createdAt;

    @LastModifiedDate  // UPDATE 시 현재 시간 자동 갱신
    private LocalDateTime updatedAt;
}
```

**동작 예시**
```java
// 1. 새 엔티티 저장
User user = User.builder().name("홍길동").build();
userRepository.save(user);
// createdAt = 2025-01-18T10:00:00
// updatedAt = 2025-01-18T10:00:00

// 2. 엔티티 수정
user.updateName("김철수");
userRepository.save(user);
// createdAt = 2025-01-18T10:00:00 (변경 없음!)
// updatedAt = 2025-01-18T11:30:00 (자동 갱신!)
```

---

### @Query

**역할**: 직접 JPQL 또는 SQL 쿼리 작성

```java
public interface DailyNoteRepository extends JpaRepository<DailyNote, Long> {

    // 메서드 이름으로 쿼리 자동 생성 (간단한 경우)
    List<DailyNote> findByUserId(Long userId);

    // 복잡한 쿼리는 직접 작성
    @Query("""
        SELECT d FROM DailyNote d
        WHERE d.userId = :userId
        AND d.date BETWEEN :startDate AND :endDate
        ORDER BY d.date DESC
        """)
    List<DailyNote> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // 네이티브 SQL 사용 (특수한 경우)
    @Query(value = "SELECT * FROM daily_notes WHERE user_id = ?1", nativeQuery = true)
    List<DailyNote> findByUserIdNative(Long userId);
}
```

---

### @Param

**역할**: @Query에서 사용하는 파라미터 바인딩

```java
// :userId는 @Param("userId")와 매핑
@Query("SELECT u FROM User u WHERE u.id = :userId")
User findByUserId(@Param("userId") Long userId);

// ?1, ?2는 순서대로 매핑 (비권장, 순서 바뀌면 버그)
@Query("SELECT u FROM User u WHERE u.name = ?1 AND u.age = ?2")
User findByNameAndAge(String name, int age);
```

---

## 5. Spring Transaction 어노테이션

---

### @Transactional

**역할**: 메서드/클래스에 트랜잭션 적용

**트랜잭션이란?**
```
트랜잭션 = 여러 작업을 하나로 묶음
- 모두 성공하면 저장 (commit)
- 하나라도 실패하면 전체 취소 (rollback)

예) 계좌이체
1. A계좌에서 10000원 출금
2. B계좌에 10000원 입금
→ 둘 다 성공해야 저장, 하나라도 실패하면 둘 다 취소!
```

#### 기본 사용법
```java
@Service
public class UserService {

    @Transactional  // 이 메서드는 하나의 트랜잭션으로 실행
    public void transfer(Long fromId, Long toId, int amount) {
        accountRepository.withdraw(fromId, amount);  // 출금
        accountRepository.deposit(toId, amount);     // 입금
        // 둘 다 성공해야 commit, 예외 발생 시 둘 다 rollback
    }
}
```

#### 클래스 레벨 적용
```java
@Service
@Transactional(readOnly = true)  // 모든 메서드에 기본 적용 (읽기 전용)
public class UserService {

    // readOnly = true 적용됨
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    @Transactional  // readOnly = false로 오버라이드
    public User save(User user) {
        return userRepository.save(user);
    }
}
```

#### 주요 속성

| 속성 | 설명 | 기본값 |
|------|------|--------|
| `readOnly` | 읽기 전용 (성능 최적화) | false |
| `timeout` | 타임아웃(초) | -1 (제한없음) |
| `rollbackFor` | 롤백 대상 예외 | RuntimeException |
| `propagation` | 전파 옵션 | REQUIRED |

#### readOnly = true의 효과
```java
@Transactional(readOnly = true)
public List<User> findAll() {
    return userRepository.findAll();
}

// readOnly = true 장점:
// 1. DB 최적화: 일부 DB는 읽기 전용 힌트로 성능 향상
// 2. JPA 최적화: 더티체킹(변경감지) 스킵 → 메모리/CPU 절약
// 3. 실수 방지: 읽기 메서드에서 실수로 save해도 반영 안 됨
```

#### 전파 옵션 (Propagation)
```java
@Transactional(propagation = Propagation.REQUIRED)  // 기본값
public void method1() {
    // 기존 트랜잭션 있으면 참여, 없으면 새로 생성
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void method2() {
    // 항상 새 트랜잭션 생성 (기존 트랜잭션 일시 중단)
}
```

**전파 옵션 그림**
```
REQUIRED (기본값):
┌─────────────────────────────────────┐
│ 트랜잭션 A                           │
│   method1() 호출                     │
│      │                              │
│      └→ method2() 호출 (같은 트랜잭션) │
└─────────────────────────────────────┘

REQUIRES_NEW:
┌─────────────────────┐
│ 트랜잭션 A           │
│   method1() 호출    │
│      │              │
│      │ ┌───────────────────┐
│      └→│ 트랜잭션 B (새로 생성)│
│        │ method2() 호출     │
│        └───────────────────┘
└─────────────────────┘
```

---

## 6. Validation 어노테이션

> **Validation이란?**
> 사용자 입력값이 올바른지 검증하는 것입니다.
> 예: 이메일 형식 확인, 필수값 확인, 길이 제한 등

---

### @Valid

**역할**: 객체의 유효성 검증을 실행하도록 트리거

```java
@RestController
public class UserController {

    @PostMapping("/users")
    public ResponseEntity<User> createUser(
        @Valid @RequestBody UserRequest request  // 여기서 검증 실행!
    ) {
        // 검증 실패 시 이 코드까지 오지 않음
        return ResponseEntity.ok(userService.create(request));
    }
}
```

---

### Bean Validation 어노테이션

```java
public class UserRequest {

    @NotNull(message = "이름은 필수입니다")
    private String name;  // null 불가

    @NotBlank(message = "이메일은 필수입니다")
    private String email;  // null, "", "   " 모두 불가

    @NotEmpty(message = "취미 목록은 필수입니다")
    private List<String> hobbies;  // null, 빈 리스트 불가

    @Size(min = 2, max = 20, message = "이름은 2~20자입니다")
    private String nickname;  // 길이 제한

    @Min(value = 0, message = "나이는 0 이상이어야 합니다")
    @Max(value = 150, message = "나이는 150 이하여야 합니다")
    private Integer age;  // 숫자 범위

    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;  // 이메일 형식

    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식: 010-0000-0000")
    private String phone;  // 정규식 패턴

    @Past(message = "생년월일은 과거 날짜여야 합니다")
    private LocalDate birthDate;  // 과거 날짜만

    @Future(message = "예약일은 미래 날짜여야 합니다")
    private LocalDate reservationDate;  // 미래 날짜만
}
```

**@NotNull vs @NotBlank vs @NotEmpty 비교**

| 값 | @NotNull | @NotEmpty | @NotBlank |
|----|----------|-----------|-----------|
| null | X | X | X |
| "" (빈 문자열) | O | X | X |
| "   " (공백만) | O | O | X |
| "abc" | O | O | O |

```java
// 추천 사용법
@NotNull    // 숫자, 날짜, 객체 등에 사용
private Long userId;

@NotBlank   // 문자열에 사용 (가장 엄격)
private String name;

@NotEmpty   // 컬렉션에 사용
private List<String> items;
```

---

## 7. Spring Web 어노테이션

---

### @RestController

**역할**: REST API를 제공하는 컨트롤러. 모든 메서드가 JSON 응답

```java
@RestController  // @Controller + @ResponseBody
public class UserController {

    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.findAll();  // List<User>가 자동으로 JSON 변환
    }
}
```

**@Controller vs @RestController**
```java
// @Controller: HTML 뷰를 반환 (전통적인 웹 애플리케이션)
@Controller
public class ViewController {
    @GetMapping("/home")
    public String home() {
        return "home";  // home.html 뷰 반환
    }
}

// @RestController: JSON 데이터 반환 (REST API)
@RestController
public class ApiController {
    @GetMapping("/api/users")
    public List<User> getUsers() {
        return users;  // JSON으로 변환되어 반환
    }
}
```

---

### @RequestMapping

**역할**: URL 경로 매핑

```java
@RestController
@RequestMapping("/api/users")  // 모든 메서드의 기본 경로
public class UserController {

    @GetMapping          // GET /api/users
    public List<User> getAll() { }

    @GetMapping("/{id}") // GET /api/users/1
    public User getById() { }

    @PostMapping         // POST /api/users
    public User create() { }
}
```

---

### HTTP 메서드 매핑 어노테이션

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping              // 조회 (Read)
    public List<User> findAll() { }

    @GetMapping("/{id}")     // 단건 조회
    public User findById(@PathVariable Long id) { }

    @PostMapping             // 생성 (Create)
    public User create(@RequestBody UserRequest request) { }

    @PutMapping("/{id}")     // 전체 수정 (Update)
    public User update(@PathVariable Long id, @RequestBody UserRequest request) { }

    @PatchMapping("/{id}")   // 일부 수정
    public User patch(@PathVariable Long id, @RequestBody Map<String, Object> updates) { }

    @DeleteMapping("/{id}")  // 삭제 (Delete)
    public void delete(@PathVariable Long id) { }
}
```

**HTTP 메서드 정리**

| 메서드 | 용도 | 어노테이션 |
|--------|------|-----------|
| GET | 조회 | @GetMapping |
| POST | 생성 | @PostMapping |
| PUT | 전체 수정 | @PutMapping |
| PATCH | 일부 수정 | @PatchMapping |
| DELETE | 삭제 | @DeleteMapping |

---

### @PathVariable

**역할**: URL 경로의 변수 추출

```java
// URL: /api/users/123
@GetMapping("/{id}")
public User getById(@PathVariable Long id) {  // id = 123
    return userService.findById(id);
}

// URL: /api/users/123/posts/456
@GetMapping("/{userId}/posts/{postId}")
public Post getPost(
    @PathVariable Long userId,    // userId = 123
    @PathVariable Long postId     // postId = 456
) {
    return postService.findByUserIdAndPostId(userId, postId);
}
```

---

### @RequestParam

**역할**: 쿼리 파라미터 추출

```java
// URL: /api/users?name=홍길동&age=25
@GetMapping
public List<User> search(
    @RequestParam String name,                    // 필수 (없으면 에러)
    @RequestParam(required = false) Integer age,  // 선택
    @RequestParam(defaultValue = "0") int page    // 기본값
) {
    return userService.search(name, age, page);
}

// URL: /api/daily-notes?date=2025-01-18
@GetMapping
public DailyNote getByDate(@RequestParam LocalDate date) {
    return dailyNoteService.findByDate(date);
}
```

---

### @RequestBody

**역할**: HTTP 요청 본문(Body)의 JSON을 객체로 변환

```java
@PostMapping
public User create(@RequestBody UserRequest request) {
    return userService.create(request);
}
```

**HTTP 요청 예시**
```http
POST /api/users
Content-Type: application/json

{
    "name": "홍길동",
    "email": "hong@email.com",
    "age": 25
}
```

**변환 과정**
```
HTTP 요청 Body (JSON)          @RequestBody           Java 객체
┌─────────────────────┐                          ┌─────────────────────┐
│ {                   │                          │ UserRequest         │
│   "name": "홍길동",  │  ──── 자동 변환 ────→    │   name = "홍길동"    │
│   "email": "...",   │                          │   email = "..."     │
│   "age": 25         │                          │   age = 25          │
│ }                   │                          │                     │
└─────────────────────┘                          └─────────────────────┘
```

---

### @ResponseEntity

**역할**: HTTP 응답을 세밀하게 제어 (상태 코드, 헤더, 본문)

```java
@RestController
public class UserController {

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        User user = userService.findById(id);

        if (user == null) {
            return ResponseEntity.notFound().build();  // 404 Not Found
        }

        return ResponseEntity.ok(user);  // 200 OK + body
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody UserRequest request) {
        User created = userService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)  // 201 Created
            .header("Location", "/api/users/" + created.getId())
            .body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();  // 204 No Content
    }
}
```

**자주 사용하는 응답 코드**

| 코드 | 의미 | 메서드 |
|------|------|--------|
| 200 | 성공 | `ResponseEntity.ok()` |
| 201 | 생성됨 | `ResponseEntity.status(CREATED)` |
| 204 | 성공, 내용 없음 | `ResponseEntity.noContent()` |
| 400 | 잘못된 요청 | `ResponseEntity.badRequest()` |
| 404 | 찾을 수 없음 | `ResponseEntity.notFound()` |

---

## 8. 테스트 어노테이션

> **테스트 코드란?**
> 작성한 코드가 올바르게 동작하는지 자동으로 검증하는 코드입니다.
> 수동으로 테스트하지 않아도 버튼 하나로 전체 검증 가능!

---

### JUnit 5 기본 어노테이션

#### @Test
```java
// 실제 프로젝트 코드: DailyNoteTest.java
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DailyNoteTest {

    @Test  // 이 메서드는 테스트 메서드입니다
    void createWithBuilder() {
        // given: 준비
        Long userId = 1L;
        LocalDate date = LocalDate.of(2025, 1, 18);
        String content = "오늘의 노트 내용";

        // when: 실행
        DailyNote dailyNote = DailyNote.builder()
                .userId(userId)
                .date(date)
                .content(content)
                .build();

        // then: 검증
        assertThat(dailyNote.getUserId()).isEqualTo(userId);
        assertThat(dailyNote.getDate()).isEqualTo(date);
        assertThat(dailyNote.getContent()).isEqualTo(content);
    }
}
```

#### @DisplayName
```java
// 실제 프로젝트 코드: DailyNoteTest.java
class DailyNoteTest {

    @Test
    @DisplayName("Builder로 DailyNote를 생성할 수 있다")  // 테스트 결과에 이 이름으로 표시
    void createWithBuilder() {
        // ...
    }

    @Test
    @DisplayName("content가 null인 빈 DailyNote를 생성할 수 있다")
    void createEmptyDailyNote() {
        // ...
    }
}
```

**테스트 결과 표시**
```
DailyNote 엔티티 테스트
  DailyNote 생성
    ✓ Builder로 DailyNote를 생성할 수 있다 (15ms)
    ✓ content가 null인 빈 DailyNote를 생성할 수 있다 (8ms)
```

#### @BeforeEach / @AfterEach
```java
// 실제 프로젝트 코드: DailyNoteRepositoryTest.java
@DataJpaTest
class DailyNoteRepositoryTest {

    @Autowired
    private DailyNoteRepository dailyNoteRepository;

    @Autowired
    private TestEntityManager em;

    private Long userId;
    private LocalDate today;

    @BeforeEach  // 각 테스트 실행 전에 실행
    void setUp() {
        userId = 1L;
        today = LocalDate.of(2025, 1, 18);
    }

    // @AfterEach는 @DataJpaTest가 자동으로 롤백하므로 불필요

    @Test
    void test1() {
        // setUp() 실행됨 → userId=1L, today=2025-01-18 설정됨
        // 테스트 실행
        // @DataJpaTest가 자동 롤백
    }

    @Test
    void test2() {
        // setUp() 실행됨 (다시!) → 새로운 테스트 데이터로 시작
        // 테스트 실행
        // @DataJpaTest가 자동 롤백
    }
}
```

**실행 순서**
```
test1 실행:  setUp() → test1() → (자동 롤백)
test2 실행:  setUp() → test2() → (자동 롤백)
```

#### @BeforeAll / @AfterAll
```java
class DatabaseTest {

    @BeforeAll  // 모든 테스트 전에 딱 1번 실행
    static void initDatabase() {
        // DB 연결, 테이블 생성 등
    }

    @AfterAll  // 모든 테스트 후에 딱 1번 실행
    static void closeDatabase() {
        // DB 연결 해제
    }
}
```

**실행 순서**
```
initDatabase() (1번)
  └→ test1()
  └→ test2()
  └→ test3()
closeDatabase() (1번)
```

#### @Nested
```java
// 실제 프로젝트 코드: DailyNoteServiceTest.java
@ExtendWith(MockitoExtension.class)
@DisplayName("DailyNoteService 테스트")
class DailyNoteServiceTest {

    @Nested
    @DisplayName("getOrCreateDailyNote 메서드")
    class GetOrCreateDailyNote {

        @Test
        @DisplayName("기존 DailyNote가 있으면 조회하여 반환한다")
        void getExistingDailyNote() { }

        @Test
        @DisplayName("기존 DailyNote가 없으면 새로 생성하여 반환한다")
        void createNewDailyNote() { }
    }

    @Nested
    @DisplayName("deleteDailyNote 메서드")
    class DeleteDailyNote {

        @Test
        @DisplayName("기존 DailyNote가 있으면 삭제한다")
        void deleteExistingDailyNote() { }

        @Test
        @DisplayName("DailyNote가 없으면 아무 동작도 하지 않는다")
        void deleteNonExistingDailyNote() { }
    }
}
```

**테스트 결과 (계층 구조로 표시)**
```
DailyNoteService 테스트
  ├─ getOrCreateDailyNote 메서드
  │    ├─ ✓ 기존 DailyNote가 있으면 조회하여 반환한다
  │    └─ ✓ 기존 DailyNote가 없으면 새로 생성하여 반환한다
  └─ deleteDailyNote 메서드
       ├─ ✓ 기존 DailyNote가 있으면 삭제한다
       └─ ✓ DailyNote가 없으면 아무 동작도 하지 않는다
```

#### @ParameterizedTest
```java
class ValidationTest {

    // 여러 입력값으로 같은 테스트 반복
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})  // 3번 실행됨
    @DisplayName("빈 문자열은 유효하지 않다")
    void emptyString_isInvalid(String input) {
        assertThat(validator.isValid(input)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})  // 5번 실행됨
    @DisplayName("1~5는 유효한 범위이다")
    void validRange(int number) {
        assertThat(number).isBetween(1, 5);
    }
}
```

#### @CsvSource
```java
@ParameterizedTest
@CsvSource({
    "1, 2, 3",      // 1 + 2 = 3
    "0, 0, 0",      // 0 + 0 = 0
    "-1, 1, 0",     // -1 + 1 = 0
    "100, 200, 300" // 100 + 200 = 300
})
@DisplayName("덧셈 테스트")
void add(int a, int b, int expected) {
    assertThat(calculator.add(a, b)).isEqualTo(expected);
}
```

#### @Disabled
```java
@Test
@Disabled("이슈 #123 해결 전까지 비활성화")
void temporarilyDisabled() {
    // 이 테스트는 실행되지 않음
}
```

---

### Spring Boot Test 어노테이션

#### @SpringBootTest
```java
@SpringBootTest  // 전체 애플리케이션 컨텍스트 로드 (통합 테스트)
class KanvaApplicationTests {

    @Test
    void contextLoads() {
        // 애플리케이션이 정상적으로 로드되는지 확인
    }
}
```

**주의**: 전체 컨텍스트를 로드하므로 느림. 필요할 때만 사용!

#### @WebMvcTest
```java
// 실제 프로젝트 코드: DailyNoteControllerTest.java
@WebMvcTest(DailyNoteController.class)  // Controller만 테스트 (빠름!)
@DisplayName("DailyNoteController 테스트")
class DailyNoteControllerTest {

    @Autowired
    private MockMvc mockMvc;  // 가짜 HTTP 요청 보내기

    @Autowired
    private ObjectMapper objectMapper;  // JSON 변환

    @MockBean  // Service는 Mock으로 대체
    private DailyNoteService dailyNoteService;

    @Test
    @WithMockUser  // 인증된 사용자로 테스트
    @DisplayName("날짜로 DailyNote를 조회한다")
    void getDailyNoteSuccess() throws Exception {
        // given: dailyNoteService.getOrCreateDailyNote() 호출 시 반환값 설정
        DailyNoteDetailResponse response = DailyNoteDetailResponse.builder()
                .id(1L)
                .date(LocalDate.of(2025, 1, 18))
                .content("테스트 내용")
                .build();

        given(dailyNoteService.getOrCreateDailyNote(eq(1L), eq(LocalDate.of(2025, 1, 18))))
                .willReturn(response);

        // when & then: HTTP 요청 보내고 응답 검증
        mockMvc.perform(get("/api/daily-notes")
                        .param("date", "2025-01-18"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.date").value("2025-01-18"))
                .andExpect(jsonPath("$.data.content").value("테스트 내용"))
                .andExpect(jsonPath("$.code").value(200));

        verify(dailyNoteService).getOrCreateDailyNote(1L, LocalDate.of(2025, 1, 18));
    }
}
```

**@WebMvcTest vs @SpringBootTest**
```
@SpringBootTest         @WebMvcTest
┌─────────────────┐    ┌─────────────────┐
│ Controller      │    │ Controller ✓    │
│ Service         │    │ Service (Mock)  │
│ Repository      │    │ Repository X    │
│ Database        │    │ Database X      │
│ 전체 로드 (느림)  │    │ 일부만 로드 (빠름)│
└─────────────────┘    └─────────────────┘
```

#### @DataJpaTest
```java
// 실제 프로젝트 코드: DailyNoteRepositoryTest.java
@DataJpaTest  // JPA Repository만 테스트
@DisplayName("DailyNoteRepository 테스트")
class DailyNoteRepositoryTest {

    @Autowired
    private DailyNoteRepository dailyNoteRepository;

    @Autowired
    private TestEntityManager em;  // 테스트용 EntityManager

    @Test
    @DisplayName("userId와 date로 DailyNote를 조회할 수 있다")
    void findByUserIdAndDate_success() {
        // given
        DailyNote dailyNote = DailyNote.builder()
                .userId(1L)
                .date(LocalDate.of(2025, 1, 18))
                .content("테스트 내용")
                .build();
        em.persist(dailyNote);
        em.flush();
        em.clear();  // 영속성 컨텍스트 초기화 (실제 DB 조회 테스트)

        // when
        Optional<DailyNote> found = dailyNoteRepository
                .findByUserIdAndDate(1L, LocalDate.of(2025, 1, 18));

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
        assertThat(found.get().getContent()).isEqualTo("테스트 내용");
    }
}
```

**@DataJpaTest 특징**
- 인메모리 H2 DB 자동 설정
- @Transactional 자동 적용 (테스트 후 자동 롤백)
- JPA 관련 빈만 로드 (빠름)

#### @MockBean
```java
// 실제 프로젝트 코드: DailyNoteControllerTest.java
@WebMvcTest(DailyNoteController.class)
class DailyNoteControllerTest {

    @MockBean  // Spring 컨텍스트에 Mock 객체 등록
    private DailyNoteService dailyNoteService;

    @Test
    @WithMockUser
    void test() throws Exception {
        // dailyNoteService의 메서드 동작을 가짜로 정의
        given(dailyNoteService.getOrCreateDailyNote(eq(1L), any(LocalDate.class)))
                .willReturn(DailyNoteDetailResponse.builder()
                        .id(1L)
                        .date(LocalDate.of(2025, 1, 18))
                        .content("테스트 내용")
                        .build());

        // Controller가 dailyNoteService.getOrCreateDailyNote()를 호출하면
        // 실제 DB 조회 없이 위에서 정의한 값 반환
    }
}
```

#### @WithMockUser
```java
// 실제 프로젝트 코드: DailyNoteControllerTest.java
@WebMvcTest(DailyNoteController.class)
class DailyNoteControllerTest {

    @Test
    @WithMockUser  // 인증된 가짜 사용자로 테스트
    @DisplayName("날짜로 DailyNote를 조회한다")
    void getDailyNoteSuccess() throws Exception {
        // 인증된 상태로 테스트 진행
        mockMvc.perform(get("/api/daily-notes")
                        .param("date", "2025-01-18"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 401 에러를 반환한다")
    void unauthorizedAccess() throws Exception {
        // @WithMockUser 없으면 인증되지 않은 상태
        mockMvc.perform(get("/api/daily-notes")
                        .param("date", "2025-01-18"))
                .andExpect(status().isUnauthorized());
    }
}
```

#### csrf()
```java
// 실제 프로젝트 코드: DailyNoteControllerTest.java
// POST, PUT, DELETE 요청 시 CSRF 토큰 필요

@Test
@WithMockUser
@DisplayName("DailyNote의 content를 수정한다")
void updateDailyNoteSuccess() throws Exception {
    mockMvc.perform(put("/api/daily-notes")
                    .with(csrf())  // CSRF 토큰 추가
                    .param("date", "2025-01-18")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
}
```

---

### Mockito 어노테이션

> **Mock이란?**
> 가짜 객체. 실제 객체 대신 테스트용 가짜를 사용해서 테스트를 독립적으로 만듦.

#### @ExtendWith(MockitoExtension.class)
```java
// 실제 프로젝트 코드: DailyNoteServiceTest.java
@ExtendWith(MockitoExtension.class)  // Mockito 사용 선언 (필수!)
@DisplayName("DailyNoteService 테스트")
class DailyNoteServiceTest {
    // @Mock, @InjectMocks 사용 가능
}
```

#### @Mock / @InjectMocks
```java
// 실제 프로젝트 코드: DailyNoteServiceTest.java
@ExtendWith(MockitoExtension.class)
@DisplayName("DailyNoteService 테스트")
class DailyNoteServiceTest {

    @Mock  // 가짜 DailyNoteRepository 생성
    private DailyNoteRepository dailyNoteRepository;

    @InjectMocks  // 위의 Mock을 자동 주입
    private DailyNoteServiceImpl dailyNoteService;

    @Test
    @DisplayName("기존 DailyNote가 있으면 조회하여 반환한다")
    void getExistingDailyNote() {
        // given: Mock 동작 정의
        DailyNote existingNote = createDailyNote(1L, 1L, LocalDate.of(2025, 1, 18), "기존 내용");

        given(dailyNoteRepository.findByUserIdAndDate(1L, LocalDate.of(2025, 1, 18)))
                .willReturn(Optional.of(existingNote));

        // when
        DailyNoteDetailResponse response = dailyNoteService.getOrCreateDailyNote(1L, LocalDate.of(2025, 1, 18));

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getContent()).isEqualTo("기존 내용");
        verify(dailyNoteRepository).findByUserIdAndDate(1L, LocalDate.of(2025, 1, 18));
        verify(dailyNoteRepository, never()).save(any());  // save() 호출 안 됨 검증
    }
}
```

**Mock 동작 방식**
```
[실제 코드]
dailyNoteService.getOrCreateDailyNote(1L, date)
    └→ dailyNoteRepository.findByUserIdAndDate(1L, date)  // 실제면 DB 조회
                                                          // Mock이면 정의한 값 반환

[테스트 코드]
given(dailyNoteRepository.findByUserIdAndDate(1L, date))
    .willReturn(Optional.of(existingNote));
    // dailyNoteRepository.findByUserIdAndDate() 호출 시 무조건 이 값 반환
```

#### @Captor
```java
// 실제 프로젝트 코드: DailyNoteServiceTest.java
@ExtendWith(MockitoExtension.class)
class DailyNoteServiceTest {

    @Mock
    private DailyNoteRepository dailyNoteRepository;

    @Captor  // 메서드 호출 시 전달된 인자를 캡처
    private ArgumentCaptor<DailyNote> dailyNoteCaptor;

    @InjectMocks
    private DailyNoteServiceImpl dailyNoteService;

    @Test
    @DisplayName("기존 DailyNote가 없으면 새로 생성하여 반환한다")
    void createNewDailyNote() {
        // given
        given(dailyNoteRepository.findByUserIdAndDate(1L, LocalDate.of(2025, 1, 18)))
                .willReturn(Optional.empty());
        given(dailyNoteRepository.save(any(DailyNote.class)))
                .willReturn(createDailyNote(1L, 1L, LocalDate.of(2025, 1, 18), null));

        // when
        dailyNoteService.getOrCreateDailyNote(1L, LocalDate.of(2025, 1, 18));

        // then: save()에 전달된 DailyNote 객체 캡처
        verify(dailyNoteRepository).save(dailyNoteCaptor.capture());

        DailyNote capturedNote = dailyNoteCaptor.getValue();
        assertThat(capturedNote.getUserId()).isEqualTo(1L);
        assertThat(capturedNote.getDate()).isEqualTo(LocalDate.of(2025, 1, 18));
        assertThat(capturedNote.getContent()).isNull();
    }
}
```

#### verify() 메서드
```java
// 실제 프로젝트 코드: DailyNoteServiceTest.java

// 호출 여부 검증
verify(dailyNoteRepository).findByUserIdAndDate(1L, today);

// 호출되지 않음 검증
verify(dailyNoteRepository, never()).save(any());

// 정확히 1번 호출 검증
verify(dailyNoteRepository, times(1)).delete(existingNote);

// 최소 1번 호출 검증
verify(dailyNoteRepository, atLeastOnce()).findByUserIdAndDate(anyLong(), any());
```

---

### Spring Test 유틸리티

#### ReflectionTestUtils
```java
// 실제 프로젝트 코드: DailyNoteServiceTest.java
// private 필드에 값을 주입할 때 사용

private DailyNote createDailyNote(Long id, Long userId, LocalDate date, String content) {
    DailyNote dailyNote = DailyNote.builder()
            .userId(userId)
            .date(date)
            .content(content)
            .build();
    // id, createdAt, updatedAt은 JPA가 자동 생성하는 필드
    // 테스트에서는 ReflectionTestUtils로 강제 주입
    ReflectionTestUtils.setField(dailyNote, "id", id);
    ReflectionTestUtils.setField(dailyNote, "createdAt", LocalDateTime.now());
    ReflectionTestUtils.setField(dailyNote, "updatedAt", LocalDateTime.now());
    return dailyNote;
}
```

---

### Testcontainers 어노테이션

> **Testcontainers란?**
> 테스트 시 Docker 컨테이너로 실제 DB를 띄워서 테스트.
> H2 인메모리 DB보다 실제 환경에 가까운 테스트 가능.

```java
@SpringBootTest
@Testcontainers  // Testcontainers 활성화
class IntegrationTest {

    @Container  // Docker 컨테이너 정의
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource  // 컨테이너 정보를 Spring 설정에 주입
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void realDatabaseTest() {
        // 실제 PostgreSQL에서 테스트!
    }
}
```

**동작 순서**
```
1. 테스트 시작
2. Docker로 PostgreSQL 컨테이너 실행
3. 테스트 실행 (실제 PostgreSQL 사용)
4. 테스트 종료
5. 컨테이너 자동 정리
```

---

### MockMvc 주요 메서드 (참고)

```java
// 실제 프로젝트 코드에서 사용된 패턴

// GET 요청
mockMvc.perform(get("/api/daily-notes")
                .param("date", "2025-01-18"))
        .andDo(print())  // 요청/응답 출력
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.content").value("테스트 내용"));

// PUT 요청 (CSRF 토큰 필요)
mockMvc.perform(put("/api/daily-notes")
                .with(csrf())
                .param("date", "2025-01-18")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

// DELETE 요청
mockMvc.perform(delete("/api/daily-notes")
                .with(csrf())
                .param("date", "2025-01-18"))
        .andExpect(status().isNoContent());

// 상태 코드 검증
.andExpect(status().isOk())           // 200
.andExpect(status().isCreated())      // 201
.andExpect(status().isNoContent())    // 204
.andExpect(status().isBadRequest())   // 400
.andExpect(status().isUnauthorized()) // 401
.andExpect(status().isNotFound())     // 404

// JSON 응답 검증
.andExpect(jsonPath("$.data.id").value(1))
.andExpect(jsonPath("$.data.content").value("테스트"))
.andExpect(jsonPath("$.data.content").doesNotExist())  // null 검증
.andExpect(jsonPath("$.data").isArray())
.andExpect(jsonPath("$.data.length()").value(2))
```

---

### AssertJ 주요 메서드 (참고)

```java
import static org.assertj.core.api.Assertions.*;

// 기본 비교
assertThat(actual).isEqualTo(expected);        // 같음
assertThat(actual).isNotEqualTo(other);        // 다름
assertThat(actual).isNull();                   // null
assertThat(actual).isNotNull();                // not null

// boolean
assertThat(condition).isTrue();
assertThat(condition).isFalse();

// 숫자
assertThat(number).isGreaterThan(5);           // > 5
assertThat(number).isLessThanOrEqualTo(10);    // <= 10
assertThat(number).isBetween(1, 10);           // 1 <= x <= 10

// 문자열
assertThat(str).contains("hello");             // 포함
assertThat(str).startsWith("Hello");           // 시작
assertThat(str).endsWith("World");             // 끝
assertThat(str).isBlank();                     // 빈 문자열/공백
assertThat(str).isEmpty();                     // 빈 문자열

// Optional
assertThat(optional).isPresent();              // 값 존재
assertThat(optional).isEmpty();                // 값 없음
assertThat(optional.get().getName()).isEqualTo("홍길동");

// 컬렉션
assertThat(list).hasSize(3);                   // 크기
assertThat(list).contains("a", "b");           // 포함
assertThat(list).containsExactly("a", "b");    // 정확히 일치 (순서도)
assertThat(list).containsExactlyInAnyOrder("b", "a");  // 순서 무관
assertThat(list).isEmpty();                    // 비어있음

// 객체 필드 추출 (실제 프로젝트에서 사용)
assertThat(found).extracting("content")
        .containsExactlyInAnyOrder("첫번째 노트", "두번째 노트");

// 날짜
assertThat(found.get(0).getDate()).isEqualTo(LocalDate.of(2025, 1, 20));

// 예외 검증
assertThatThrownBy(() -> service.doSomething())
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("error");

// 예외 없음 검증
assertThatCode(() -> service.doSomething())
    .doesNotThrowAnyException();
```

---

## 요약 표

### 레이어별 주요 어노테이션

| 레이어 | 어노테이션 | 설명 |
|--------|-----------|------|
| **Entity** | `@Entity`, `@Table`, `@Id` | DB 테이블 매핑 |
| **Entity** | `@Column`, `@Enumerated` | 컬럼 설정 |
| **Entity** | `@MappedSuperclass` | 공통 필드 상속 |
| **Repository** | `@Query`, `@Param` | 커스텀 쿼리 |
| **Service** | `@Service`, `@Transactional` | 비즈니스 로직 |
| **Controller** | `@RestController`, `@RequestMapping` | API 엔드포인트 |
| **Controller** | `@GetMapping`, `@PostMapping` | HTTP 메서드 |
| **DTO** | `@Valid`, `@NotNull`, `@NotBlank` | 입력 검증 |

### 테스트 레이어별 어노테이션

| 테스트 종류 | 어노테이션 | 용도 | 실제 사용 파일 |
|------------|-----------|------|---------------|
| 엔티티 테스트 | `@Test`, `@Nested`, `@DisplayName` | 도메인 로직 검증 | `DailyNoteTest.java` |
| Repository 테스트 | `@DataJpaTest`, `@BeforeEach` | JPA 쿼리 검증 | `DailyNoteRepositoryTest.java` |
| Service 테스트 | `@ExtendWith(MockitoExtension.class)` | Mockito 사용 | `DailyNoteServiceTest.java` |
| Service 테스트 | `@Mock`, `@InjectMocks`, `@Captor` | Mock 객체 생성/주입 | `DailyNoteServiceTest.java` |
| Controller 테스트 | `@WebMvcTest`, `@MockBean` | Controller 슬라이스 | `DailyNoteControllerTest.java` |
| Controller 테스트 | `@WithMockUser`, `csrf()` | Security 테스트 | `DailyNoteControllerTest.java` |
| 통합 테스트 | `@SpringBootTest` | 전체 컨텍스트 | `KanvaApplicationTests.java` |
| 통합 테스트 | `@Testcontainers`, `@Container` | Docker DB | (예정) |

### 테스트 파일별 사용 어노테이션

| 파일 | 사용된 어노테이션 |
|------|------------------|
| `DailyNoteTest.java` | `@DisplayName`, `@Nested`, `@Test` |
| `DailyNoteRepositoryTest.java` | `@DataJpaTest`, `@DisplayName`, `@Nested`, `@Test`, `@BeforeEach` |
| `DailyNoteServiceTest.java` | `@ExtendWith`, `@Mock`, `@InjectMocks`, `@Captor`, `@DisplayName`, `@Nested`, `@Test`, `@BeforeEach` |
| `DailyNoteControllerTest.java` | `@WebMvcTest`, `@MockBean`, `@WithMockUser`, `@DisplayName`, `@Nested`, `@Test` |
