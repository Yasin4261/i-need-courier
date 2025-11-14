# Proje Veritabanı Şeması: Kurye Yönetim Sistemi (KYS)

Bu doküman, Kurye Yönetim Sistemi (KYS) projesinin PostgreSQL veritabanı şemasını, tablo yapılarını ve aralarındaki ilişkileri teknik detaylarıyla açıklamaktadır.

## 1. Veritabanı Mimarisi (Entity-Relationship)

Veritabanı, dört ana varlık (entity) etrafında kurulmuştur: **Kuryeler**, **İşletmeler**, **Teslimatlar** ve **Yardımcı Yapılar** (**Vardiyalar**, **Derecelendirmeler**).

### Temel İlişkiler

| Kaynak Tablo | İlişki Türü | Hedef Tablo | Açıklama |
| :--- | :--- | :--- | :--- |
| **`couriers`** | **Öz-Referanslı (1:Çok)** | **`couriers`** | Kaptanların diğer kuryeleri yönettiği hiyerarşiyi kurar (`supervisor_id`). |
| **`couriers`** | **Bire Çok (1:Çok)** | **`shifts`** | Bir kuryenin birden fazla vardiyası olabilir. |
| **`shift_templates`** | **Referans** | **`shifts`** | Vardiya şablonlarından vardiya oluşturulur (doğrudan FK yok, mantıksal ilişki). |
| **`couriers`** | **Bire Çok (1:Çok)** | **`deliveries`** | Bir kuryeye birden fazla paket atanabilir. |
| **`businesses`** | **Bire Çok (1:Çok)** | **`deliveries`** | Bir işletme birden fazla teslimat oluşturabilir. |
| **`deliveries`** | **Çoka Bir (Çok:1)** | **`routes`** | Bir teslimat tek bir rotanın parçası olabilir. |
| **`couriers`** | **Bire Çok (1:Çok)** | **`ratings`** | Bir kurye birden fazla puanlama yapabilir. |

---

## 2. Tablo Tanımları ve Teknik Detaylar

Aşağıda, her tablonun kritik alanları, veri tipleri ve kısıtlamaları (constraints) listelenmiştir.

### 2.1. `couriers` (Kuryeler)

Kuryelerin temel bilgileri, çalışma modeli ve hiyerarşik pozisyonlarını tutar.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
| :--- | :--- | :--- | :--- |
| **`courier_id`** | `SERIAL` | **PRIMARY KEY** | Benzersiz Kurye Kimliği. |
| `full_name` | `VARCHAR(100)` | `NOT NULL` | Kuryenin Adı Soyadı. |
| `phone_number` | `VARCHAR(20)` | **UNIQUE**, `NOT NULL` | İletişim Numarası. |
| `working_model` | `VARCHAR(50)` | `NOT NULL` | Çalışma şekli (örn: 'Shift', 'Joker'). |
| `status` | `VARCHAR(20)` | `NOT NULL` | Anlık durum (örn: 'Available', 'Offline'). |
| **`on_duty_since`** | `TIMESTAMP WITH TIME ZONE` | - | Sıra tabanlı atama için son vardiya başlangıç saati. **(Sıralama Anahtarı)** |
| **`supervisor_id`** | `INT` | **FOREIGN KEY** self-ref. | Bağlı olduğu Takım Kaptanı'nın `courier_id`'si. (`ON DELETE SET NULL`) |

### 2.2. `shifts` (Vardiyalar)

Kuryelerin planlanmış çalışma saatleri, rolleri ve gerçek check-in/check-out zamanlarını tutar. **Vardiya Yönetim Sistemi**

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
| :--- | :--- | :--- | :--- |
| **`shift_id`** | `BIGSERIAL` | **PRIMARY KEY** | Benzersiz Vardiya Kimliği. |
| **`courier_id`** | `BIGINT` | **FOREIGN KEY** (`couriers`), `NOT NULL`, `ON DELETE CASCADE` | Vardiyayı yapacak kurye. |
| `start_time` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Planlanan vardiya başlangıç zamanı. |
| `end_time` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Planlanan vardiya bitiş zamanı. |
| **`shift_role`** | `ENUM(shift_role)` | `NOT NULL`, DEFAULT `'COURIER'` | O vardiyadaki rol: **'COURIER'** (Normal kurye) veya **'CAPTAIN'** (Takım kaptanı). |
| **`status`** | `ENUM(shift_status)` | `NOT NULL`, DEFAULT `'RESERVED'` | Vardiya durumu: `RESERVED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED`, `NO_SHOW`. |
| **`check_in_time`** | `TIMESTAMP WITH TIME ZONE` | - | Gerçek giriş zamanı (kurye check-in yaptığında doldurulur). |
| **`check_out_time`** | `TIMESTAMP WITH TIME ZONE` | - | Gerçek çıkış zamanı (kurye check-out yaptığında doldurulur). |
| `notes` | `TEXT` | - | Vardiya notları (check-in/out notları dahil). |
| `created_at` | `TIMESTAMP` | DEFAULT `CURRENT_TIMESTAMP` | Kayıt oluşturulma zamanı. |
| `updated_at` | `TIMESTAMP` | DEFAULT `CURRENT_TIMESTAMP` | Son güncellenme zamanı. |

**Kısıtlamalar:**
- Check-in zamanı, planlanan başlangıçtan 30 dakika önceye kadar yapılabilir
- Check-out zamanı, check-in zamanından sonra olmalı
- Vardiya süresi maksimum 24 saat olabilir

### 2.2.1. `shift_templates` (Vardiya Şablonları)

Tekrar eden vardiya zaman dilimlerini tanımlar. Kuryelerin kolayca vardiya rezerve etmesini sağlar.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
| :--- | :--- | :--- | :--- |
| **`template_id`** | `BIGSERIAL` | **PRIMARY KEY** | Benzersiz Şablon Kimliği. |
| `name` | `VARCHAR(100)` | `NOT NULL` | Vardiya şablon adı (örn: "Sabah Vardiyası", "Akşam Vardiyası"). |
| `description` | `TEXT` | - | Vardiya açıklaması. |
| `start_time` | `TIME` | `NOT NULL` | Başlangıç saati (saat:dakika formatında). |
| `end_time` | `TIME` | `NOT NULL` | Bitiş saati (saat:dakika formatında). |
| `default_role` | `ENUM(shift_role)` | DEFAULT `'COURIER'` | Bu vardiya için varsayılan rol. |
| `max_couriers` | `INT` | DEFAULT `10` | Bu vardiyada maksimum kurye sayısı. |
| `is_active` | `BOOLEAN` | DEFAULT `TRUE` | Şablonun aktif olup olmadığı. |
| `created_at` | `TIMESTAMP` | DEFAULT `CURRENT_TIMESTAMP` | Kayıt oluşturulma zamanı. |
| `updated_at` | `TIMESTAMP` | DEFAULT `CURRENT_TIMESTAMP` | Son güncellenme zamanı. |

**Örnek Şablonlar:**
- Sabah Vardiyası: 09:00-17:00
- Akşam Vardiyası: 14:00-22:00
- Gece Vardiyası: 18:00-02:00
- Tam Gün Vardiyası: 08:00-20:00

### 2.3. `businesses` (İşletmeler)

Paket talebi oluşturan işletmelerin bilgileri.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
| :--- | :--- | :--- | :--- |
| **`business_id`** | `SERIAL` | **PRIMARY KEY** | İşletme Kimliği. |
| `name` | `VARCHAR(150)` | `NOT NULL` | İşletmenin ticari adı. |
| `tax_id` | `VARCHAR(20)` | **UNIQUE** | Vergi numarası. |
| `address_line_1` | `VARCHAR(255)` | `NOT NULL` | İşletmenin ana adresi. |

### 2.4. `deliveries` (Teslimatlar)

Sistemdeki her bir paketin/teslimatın detaylarını, durumunu ve atama bilgilerini tutar. **(Merkezi Tablo)**

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
| :--- | :--- | :--- | :--- |
| **`delivery_id`** | `SERIAL` | **PRIMARY KEY** | Benzersiz Teslimat Kimliği. |
| **`business_id`** | `INT` | **FOREIGN KEY** (`businesses`) | Paketi gönderen işletme. (`ON DELETE RESTRICT`) |
| **`courier_id`** | `INT` | **FOREIGN KEY** (`couriers`) | Atanmış kurye. (Atanmadıysa `NULL`) |
| **`route_id`** | `INT` | **FOREIGN KEY** (`routes`) | Paketin parçası olduğu rota. (Opsiyonel) |
| `item_type` | `VARCHAR(50)` | `NOT NULL` | Paket içeriği (örn: 'Yemek', 'Evrak'). |
| `dropoff_address` | `VARCHAR(255)` | `NOT NULL` | Müşteriye teslimat adresi. |
| **`status`** | `VARCHAR(50)` | `NOT NULL` | Anlık durum (örn: 'Pending', 'Assigned', 'Delivered'). |

### 2.5. `routes` (Rotalar)

Teslimatların gruplandığı ve bir kuryeye toplu atama yapılmasını sağlayan yapı.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
| :--- | :--- | :--- | :--- |
| **`route_id`** | `SERIAL` | **PRIMARY KEY** | Benzersiz Rota Kimliği. |
| **`courier_id`** | `INT` | **FOREIGN KEY** (`couriers`) | Rotayı üstlenen kurye. |
| `route_status` | `VARCHAR(50)` | `NOT NULL` | Rotanın durumu (örn: 'Created', 'In Progress'). |

### 2.6. `ratings` (Derecelendirmeler)

Kuryelerin, işletmeleri veya teslimat deneyimlerini puanladığı esnek tablo.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
| :--- | :--- | :--- | :--- |
| **`rating_id`** | `SERIAL` | **PRIMARY KEY** | Puanlama kaydı. |
| **`courier_id`** | `INT` | **FOREIGN KEY** (`couriers`) | Puanlamayı yapan kurye. |
| **`entity_type`** | `VARCHAR(50)` | `NOT NULL` | Puanlanan nesne tipi ('Business' veya 'Delivery'). |
| **`entity_id`** | `INT` | `NOT NULL` | Puanlanan nesnenin ID'si. |
| **`score`** | `INT` | `NOT NULL`, **CHECK (1-5)** | Verilen puan (1'den 5'e kadar). |

---

## 3. Vardiya Yönetim Sistemi

Kuryeler vardiya rezerve edebilir, vardiyaya giriş/çıkış yapabilir ve vardiyalarını yönetebilir.

### 3.1. Vardiya İş Akışı

```
1. Vardiya Rezervasyonu (RESERVED)
   ↓ Kurye vardiya şablonundan gelecek bir tarih için rezervasyon yapar
   
2. Check-In (CHECKED_IN)
   ↓ Vardiya zamanı geldiğinde (30 dk önceden) giriş yapar
   ↓ `on_duty_since` alanı doldurulur → Sıra tabanlı atamaya girer
   
3. Aktif Çalışma
   ↓ Kurye paket teslimatları yapar
   
4. Check-Out (CHECKED_OUT)
   ↓ Vardiya bitiminde çıkış yapar
   ↓ `on_duty_since` alanı temizlenir → Sıradan çıkar
```

### 3.2. Vardiya Durumları (shift_status ENUM)

- **`RESERVED`**: Vardiya rezerve edildi, henüz giriş yapılmadı
- **`CHECKED_IN`**: Vardiyaya giriş yapıldı, kurye aktif çalışıyor
- **`CHECKED_OUT`**: Vardiyadan çıkış yapıldı, vardiya tamamlandı
- **`CANCELLED`**: Rezervasyon iptal edildi (başlangıçtan 2 saat öncesine kadar)
- **`NO_SHOW`**: Kurye rezerve ettiği vardiyaya gelmedi/giriş yapmadı

### 3.3. İş Kuralları

**Rezervasyon Kuralları:**
- Geçmiş tarihli vardiya rezerve edilemez
- Aynı zaman diliminde birden fazla vardiya rezerve edilemez (zaman çakışması kontrolü)
- Bir kurye günde birden fazla farklı vardiyaya kayıt olabilir (çakışmayan)

**Check-In Kuralları:**
- Vardiya başlangıcından 30 dakika önceye kadar check-in yapılabilir
- Sadece `RESERVED` durumundaki vardiyalara check-in yapılabilir
- Check-in yaparken konum bilgisi (latitude/longitude) opsiyonel olarak gönderilebilir

**Check-Out Kuralları:**
- Sadece `CHECKED_IN` durumundaki vardiyalardan check-out yapılabilir
- İstediğiniz zaman (erken veya geç) check-out yapılabilir
- Check-out zamanı otomatik olarak kaydedilir

**İptal Kuralları:**
- Sadece `RESERVED` durumundaki vardiyalar iptal edilebilir
- Vardiya başlangıcına 2 saatten az kaldıysa iptal edilemez

## 4. Atama ve Sıralama Mantığı (FIFO)

Veritabanı, adil atama için **FIFO (First-In, First-Out)** mantığını destekler.

### 4.1. Sıra Tabanlı Atama

1.  **Sıra Anahtarı:** Kuryenin `couriers` tablosundaki **`on_duty_since`** sütunu kullanılır.
2.  **Kural:** Paket atanması gerektiğinde, sistem şu kriterleri uygular:
    - **`status = 'ONLINE'`** olan kuryeleri filtrele (aktif çalışanlar)
    - **`on_duty_since IS NOT NULL`** olanları seç (vardiyaya giriş yapmış)
    - **`on_duty_since ASC`** sıralaması yap (en uzun süredir çalışan önce)
    - İlk sıradaki kuryeye paketi ata
3.  **Vardiya Check-In:** Kurye vardiyaya giriş yaptığında:
    - `on_duty_since` alanı `CURRENT_TIMESTAMP` olarak set edilir
    - Kurye durumu `ONLINE` yapılır
    - Kurye paket atama sırasına girer
4.  **Vardiya Check-Out:** Kurye vardiyadan çıktığında:
    - `on_duty_since` alanı `NULL` yapılır
    - Kurye durumu `OFFLINE` yapılır
    - Kurye atama sırasından çıkar

### 4.2. Örnek Senaryo

```sql
-- Aktif kuryeler ve sıraları
SELECT 
    id, 
    name, 
    on_duty_since,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - on_duty_since))/3600 as hours_on_duty
FROM couriers
WHERE status = 'ONLINE' 
  AND on_duty_since IS NOT NULL
ORDER BY on_duty_since ASC;

-- Sonuç:
-- id | name          | on_duty_since           | hours_on_duty
-- 3  | Mehmet Demir  | 2025-11-12 08:00:00+00 | 4.5 (en uzun süredir çalışıyor - ÖNCELİK)
-- 1  | Ahmet Yılmaz  | 2025-11-12 09:30:00+00 | 3.0
-- 5  | Ayşe Kaya     | 2025-11-12 10:15:00+00 | 2.25
```

Yeni paket geldiğinde **Mehmet Demir** öncelikli olarak atanır.
