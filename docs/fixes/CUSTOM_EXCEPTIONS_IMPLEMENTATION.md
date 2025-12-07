# ✅ CUSTOM EXCEPTION HANDLING İMPLEMENTASYONU
## 🎯 AMAÇ
`BusinessException` yerine **spesifik custom exception'lar** kullanarak:
- ✅ Daha anlamlı HTTP status kodları
- ✅ Daha iyi hata yönetimi
- ✅ Cleaner code
- ✅ Easier debugging
---
## 📝 OLUŞTURULAN CUSTOM EXCEPTION'LAR
### 1. **NoCourierAvailableException**
```java
throw new NoCourierAvailableException("Şu anda aktif kurye yok");
```
- **HTTP Status:** 503 Service Unavailable
- **Kullanım:** Hiç kurye on-duty değilken
- **Anlamı:** Geçici bir durum, daha sonra denenebilir
### 2. **AssignmentNotFoundException**
```java
throw new AssignmentNotFoundException(assignmentId);
```
- **HTTP Status:** 404 Not Found
- **Kullanım:** Assignment ID bulunamadığında
- **Anlamı:** Kayıt yok
### 3. **AssignmentNotOwnedException**
```java
throw new AssignmentNotOwnedException(assignmentId, courierId);
```
- **HTTP Status:** 403 Forbidden
- **Kullanım:** Assignment başka kuryeye ait
- **Anlamı:** Yetkisiz erişim
### 4. **AssignmentExpiredException**
```java
throw new AssignmentExpiredException(assignmentId, "Timeout mesajı");
```
- **HTTP Status:** 410 Gone
- **Kullanım:** Assignment timeout olmuş
- **Anlamı:** Süre dolmuş, artık kullanılamaz
### 5. **InvalidAssignmentStatusException**
```java
throw new InvalidAssignmentStatusException(assignmentId, currentStatus, "Mesaj");
```
- **HTTP Status:** 409 Conflict
- **Kullanım:** Assignment yanlış statüde (örn: zaten ACCEPTED)
- **Anlamı:** İşlem şu anda yapılamaz
---
## 🔄 ÖNCE vs SONRA
### ÖNCE: BusinessException (Tek Exception)
```java
if (assignment == null) {
    throw new BusinessException("Atama bulunamadı");  // 400 Bad Request
}
if (!assignment.getCourierId().equals(courierId)) {
    throw new BusinessException("Bu atama size ait değil");  // 400 Bad Request
}
if (timeout) {
    throw new BusinessException("Timeout oldu");  // 400 Bad Request
}
```
**Problem:** Hepsi 400 (Bad Request), ayırt edilemez!
### SONRA: Custom Exceptions
```java
if (assignment == null) {
    throw new AssignmentNotFoundException(id);  // 404 Not Found ✅
}
if (!assignment.getCourierId().equals(courierId)) {
    throw new AssignmentNotOwnedException(id, courierId);  // 403 Forbidden ✅
}
if (timeout) {
    throw new AssignmentExpiredException(id, "Timeout");  // 410 Gone ✅
}
```
**Avantaj:** Her durum için doğru HTTP status!
---
## 📊 HTTP STATUS CODE MAPPINGS
| Exception | HTTP Status | Kod | Anlamı |
|-----------|-------------|-----|--------|
| `NoCourierAvailableException` | Service Unavailable | 503 | Geçici, tekrar dene |
| `AssignmentNotFoundException` | Not Found | 404 | Kayıt yok |
| `AssignmentNotOwnedException` | Forbidden | 403 | Yetkisiz |
| `AssignmentExpiredException` | Gone | 410 | Süre dolmuş |
| `InvalidAssignmentStatusException` | Conflict | 409 | Durum uyumsuz |
| `BusinessException` (fallback) | Bad Request | 400 | Genel hata |
---
## 🔧 GÜNCELLENEN DOSYALAR
### 1. Yeni Exception Sınıfları (5 dosya)
```
exception/
├── NoCourierAvailableException.java       ✅ Yeni
├── AssignmentNotFoundException.java       ✅ Yeni
├── AssignmentNotOwnedException.java       ✅ Yeni
├── AssignmentExpiredException.java        ✅ Yeni
└── InvalidAssignmentStatusException.java  ✅ Yeni
```
### 2. GlobalExceptionHandler.java
```java
// 5 yeni @ExceptionHandler eklendi
@ExceptionHandler(NoCourierAvailableException.class)
@ExceptionHandler(AssignmentNotFoundException.class)
@ExceptionHandler(AssignmentNotOwnedException.class)
@ExceptionHandler(AssignmentExpiredException.class)
@ExceptionHandler(InvalidAssignmentStatusException.class)
```
### 3. OrderAssignmentService.java
```java
// BusinessException yerine custom exception'lar kullanılıyor
- throw new BusinessException("Atama bulunamadı");
+ throw new AssignmentNotFoundException(assignmentId);
- throw new BusinessException("Bu atama size ait değil");
+ throw new AssignmentNotOwnedException(assignmentId, courierId);
// ... diğer değişiklikler
```
---
## 🎯 FAYDALAR
### 1. **Daha İyi API Tasarımı**
Client'lar HTTP status'e bakarak ne yapacaklarını bilir:
- `404` → "Assignment bulunamadı, başka birini dene"
- `403` → "Bu assignment sana ait değil"
- `410` → "Timeout olmuş, yeni assignment al"
- `503` → "Kurye yok, biraz bekle"
### 2. **Daha İyi Error Handling**
```javascript
// Client-side kod
try {
    await acceptAssignment(id);
} catch (error) {
    if (error.status === 410) {
        // Timeout olmuş, yeni assignment al
        fetchNewAssignments();
    } else if (error.status === 403) {
        // Yetkisiz, hata göster
        showError("Bu atama size ait değil!");
    }
}
```
### 3. **Daha Temiz Kod**
```java
// Exception adından ne olduğu anlaşılıyor
throw new AssignmentExpiredException(id, "Timeout");
// BusinessException'dan çok daha açıklayıcı:
throw new BusinessException("Atama süresi doldu");
```
### 4. **Daha Kolay Debug**
- Log'larda exception adı görünür
- Stack trace'de spesifik exception
- Hangi durumda oluştuğu net
---
## 📈 ÖRNEK SENARYO
### Client Request:
```bash
curl -X POST /api/v1/courier/assignments/123/accept \
  -H "Authorization: Bearer $TOKEN"
```
### Önceki Cevap (Hepsi 400):
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Bu atama size ait değil"
}
```
### Yeni Cevap (Doğru Status):
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Bu atama size ait değil"
}
```
**Client:** "Ah, 403 = Forbidden, demek ki yetkisiz erişim. UI'da uygun mesaj gösterebilirim."
---
## ✅ SONUÇ
| Özellik | Önce | Sonra |
|---------|------|-------|
| **Exception Çeşitliliği** | 1 (BusinessException) | 6 (5 custom + 1 fallback) ✅ |
| **HTTP Status Doğruluğu** | Hep 400 | Duruma göre (403, 404, 410, 503) ✅ |
| **Kod Okunabilirliği** | Düşük | Yüksek ✅ |
| **Client Error Handling** | Zor | Kolay ✅ |
| **Debugging** | Zor | Kolay ✅ |
---
**Status:** ✅ Implemented & Compiled  
**Breaking Change:** ❌ Hayır (API response aynı, sadece status kodları değişti)  
**Best Practice:** ✅ REST API standartlarına uygun
