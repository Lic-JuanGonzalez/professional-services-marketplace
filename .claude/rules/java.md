# Java rules

- Java 21 — use records, sealed classes, pattern matching, text blocks where appropriate
- No raw types — always parameterize generics
- No checked exceptions in service layer — wrap in unchecked `AppException`
- `Optional` for nullable returns — never return null from public methods
- Immutable by default — `final` fields, no setters unless necessary
- No `static` mutable state
- Tests: JUnit 5 + Mockito — `@ExtendWith(MockitoExtension.class)`
- One assertion concept per test — multiple `assertThat` on same result OK
- No `System.out.println` — use SLF4J (`log.info`, `log.debug`, `log.error`)
- Dependency injection via constructor — no field injection (`@Autowired` on field)
- Repository pattern: interfaces in domain, implementations in infrastructure
