# Backend Uygulama ve API Tasarımı: Kurye Yönetim Sistemi (KYS)

Bu doküman, KYS projesinin PostgreSQL veritabanı şemasını temel alan Backend geliştirme yaklaşımını ve API tasarımını (RESTful) tanımlamaktadır.

---

## 1. Veritabanı Uygulama Yaklaşımı

Backend uygulaması, veri bütünlüğünü ve ilişkisel bütünlüğü korumak amacıyla **ORM (Object-Relational Mapping)** kullanmalıdır.

### 1.1. Kritik İş Mantığının Uygulanması

Veritabanındaki kısıtlamalara ek olarak, uygulama katmanında (Servisler/Yöneticiler) yönetilmesi gereken temel iş kuralları:

| İş Kuralı | Uygulama Katmanı Kontrolü |
| :--- | :--- |
| **Sıra Tabanlı Atama (FIFO)** | Yeni bir paket (`status: Pending`) geldiğinde, **`couriers`** tablosunda `on_duty_since ASC`'ye göre sorgulama yapan özel bir **Atama Servisi** tetiklenmeli ve en uygun (en uzun süredir çalışan) kuryeye atama yapılmalıdır. |
| **Vardiya/Rol Kontrolü** | **Kaptan Yetkisi** gerektiren API uç noktalarında (örn: paket yeniden atama), istek yapan kuryenin mevcut aktif vardiyasında (`shifts` tablosu kontrol edilerek) **`shift_role: Captain`** olup olmadığı doğrulanmalıdır. |
| **Puanlama Doğrulaması** | Puanlama API'ında, gelen skorun **1 ile 5 arasında** olduğu ve `entity_type`'ın **'Business'** veya **'Delivery'** ile sınırlandırıldığı kontrol edilmelidir. |

---

## 2. API Tasarımı: RESTful Kaynaklar

Tüm API uç noktaları, sürümleme ve kaynak yönetimi için RESTful standartlarına uymalıdır. Temel URI yapısı: `/api/v1/`

### 2.1. Kurye ve Yönetim Kaynakları (`/couriers`)

| Metot | URI | Açıklama | Yetki Gereksinimi |
| :--- | :--- | :--- | :--- |
| **`GET`** | `/couriers` | Tüm kuryeleri listeler (Admin/Captain, filtreleme ile). | Admin/Captain |
| **`GET`** | `/couriers/{id}` | Belirli bir kuryenin detaylarını getirir. | Self/Admin/Captain |
| **`POST`** | `/couriers` | Yeni kurye kaydı oluşturur. | Admin |
| **`GET`** | `/couriers/team/{captainId}`| Kaptanın yönettiği tüm kuryeleri getirir (`couriers.supervisor_id` alanını kullanır). | Self/Admin |

### 2.2. Teslimat ve Atama Kaynakları (`/deliveries`)

| Metot | URI | Açıklama | Yetki Gereksinimi |
| :--- | :--- | :--- | :--- |
| **POST** | `/deliveries` | Yeni paket talebi oluşturur. | Business |
| **GET** | `/deliveries/pending` | Atanmayı bekleyen paketleri (atama kuyruğunu) getirir. | Scheduler Service |
| **PATCH** | `/deliveries/{id}/assign`| **MANUEL/OTOMATİK ATAMA:** Paketi kuryeye atar. | Admin/Scheduler |
| **PATCH** | `/deliveries/{id}/status`| Paket durumunu günceller (örn: `PickedUp`, `Delivered`). | Courier |
| **GET** | `/deliveries/courier/{courierId}`| Kuryeye atanmış tüm aktif/tamamlanmış paketleri getirir. | Self/Admin/Captain |

### 2.3. Vardiya Yönetimi (`/shifts`)

| Metot | URI | Açıklama | Yetki Gereksinimi |
| :--- | :--- | :--- | :--- |
| **POST** | `/shifts` | Yeni vardiya planı oluşturur. | Admin |
| **PATCH** | `/shifts/{id}/start`| Kuryenin vardiyayı başlattığını kaydeder. **`couriers.on_duty_since`** alanını günceller. | Courier |
| **PATCH** | `/shifts/{id}/end` | Kuryenin vardiyayı bitirdiğini kaydeder. **`couriers.on_duty_since`** alanını `NULL` yapar. | Courier |
| **GET** | `/shifts/active/{courierId}`| Kuryenin o anki aktif vardiyasını ve **`shift_role`**'ünü getirir. | Self/Admin/Captain |

### 2.4. Puanlama Kaynakları (`/ratings`)

| Metot | URI | Açıklama | Yetki Gereksinimi |
| :--- | :--- | :--- | :--- |
| **POST** | `/ratings` | Kurye tarafından yeni bir puanlama kaydı oluşturur (`entity_type` belirtilmeli). | Courier |
| **GET** | `/ratings/average/business/{businessId}`| Bir işletmenin aldığı ortalama puanı hesaplar. | Public/Admin |

---

## 3. Güvenlik ve Yetkilendirme (AuthN/AuthZ)

Tüm API uç noktaları, **JWT (JSON Web Tokens)** kullanılarak kimlik doğrulaması yapmalıdır. Yetkilendirme, Veritabanı rollerine göre **Rol Tabanlı Erişim Kontrolü (RBAC)** ile sağlanır.

| Rol | Yetkilendirme Kaynağı | Yönetim Yetkisi |
| :--- | :--- | :--- |
| **Admin** | Yönetici tablosu/flag. | Tüm verilere tam erişim. |
| **Captain**| Aktif `shift.shift_role = 'Captain'`. | Ekibinin verilerini okuma, paketleri yeniden atama. |
| **Courier**| `couriers` tablosu kaydı. | Kendi verilerini ve atanmış paketlerini yönetme. |
| **Business**| `businesses` tablosu kaydı. | Kendi oluşturduğu paketleri yönetme. |
