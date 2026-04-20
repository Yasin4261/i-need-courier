# Copilot Instructions

## Test Yazım Kuralları

### SUT (System Under Test) İsimlendirmesi
- Test sınıflarında test edilen sınıfın instance'ı **her zaman `underTest`** olarak isimlendirilmelidir.
- `@InjectMocks` veya doğrudan oluşturulan SUT nesnesi için bu kural geçerlidir.

```java
// ✅ Doğru
@InjectMocks
private OrderAssignmentService underTest;

// ❌ Yanlış
@InjectMocks
private OrderAssignmentService orderAssignmentService;

// ❌ Yanlış
@InjectMocks
private OrderAssignmentService service;
```

