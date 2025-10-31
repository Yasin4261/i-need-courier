# Proje Veritabanı Şeması: Kurye Yönetim Sistemi (KYS)

Bu doküman, Kurye Yönetim Sistemi (KYS) projesinin PostgreSQL veritabanı şemasını, tablo yapılarını ve aralarındaki ilişkileri teknik detaylarıyla açıklamaktadır.

## 1. Veritabanı Mimarisi (Entity-Relationship)

Veritabanı, dört ana varlık (entity) etrafında kurulmuştur: **Kuryeler**, **İşletmeler**, **Teslimatlar** ve **Yardımcı Yapılar** (**Vardiyalar**, **Derecelendirmeler**).

### Temel İlişkiler

| Kaynak Tablo | İlişki Türü | Hedef Tablo | Açıklama |
| :--- | :--- | :--- | :--- |
| **`couriers`** | **Öz-Referanslı (1:Çok)** | **`couriers`** | Kaptanların diğer kuryeleri yönettiği hiyerarşiyi kurar (`supervisor_id`). |
| **`couriers`** | **Bire Çok (1:Çok)** | **`shifts`** | Bir kuryenin birden fazla vardiyası olabilir. |
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

Kuryelerin planlanmış çalışma saatleri ve o vardiyadaki rollerini tutar.

| Sütun Adı | Veri Tipi | Kısıtlamalar | Açıklama |
| :--- | :--- | :--- | :--- |
| **`shift_id`** | `SERIAL` | **PRIMARY KEY** | Benzersiz Vardiya Kimliği. |
| **`courier_id`** | `INT` | **FOREIGN KEY** (`couriers`) | Vardiyayı yapacak kurye. |
| `start_time` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Vardiya başlangıcı. |
| `end_time` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL` | Vardiya bitişi. |
| **`shift_role`** | `VARCHAR(50)` | `NOT NULL` | O vardiyadaki rolü (örn: **'Courier'** veya **'Captain'**). |

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

## 3. Atama ve Sıralama Mantığı

Veritabanı, adil atama için **FIFO (First-In, First-Out)** mantığını destekler.

1.  **Sıra Anahtarı:** Kuryenin `couriers` tablosundaki **`on_duty_since`** sütunu kullanılır.
2.  **Kural:** Paket atanması gerektiğinde, sistem **`status = 'Available'`** olan kuryeleri listeler ve **`on_duty_since ASC`** (en eskiden yeniye) sıralaması yaparak en uzun süredir çalışan kuryeyi seçer.
3.  **Vardiya Bitişi:** Kurye vardiyadan çıktığında, `on_duty_since` alanı `NULL` yapılır ve kurye atama sırasından çıkar.
