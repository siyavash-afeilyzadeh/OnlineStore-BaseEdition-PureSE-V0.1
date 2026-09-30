<div dir="rtl">

# دستور کار ماژول inventories

هدف: رساندن ماژول inventories (‎`Inventory`‎ و ‎`InventoryItem`‎ و ‎`InventoryTransaction`‎) به سطح ماژول ‎`attachments`‎.

- ‎`Inventory`‎ یک انبار است.
- ‎`InventoryItem`‎ موجودی یک product در یک انبار است.
- ‎`InventoryTransaction`‎ هر ورود یا خروج کالا را ثبت می‌کند؛ موجودی **فقط** با transaction تغییر می‌کند.

**پیش از شروع:** فایل [راهنمای دانشجو](../student-guide.md) را کامل بخوانید. ساختار ماژول، مسیر کامل یک request، کلاس‌هایی که از core ارث می‌برید، ترتیب کار ماژول‌ها و روش تحویل در آنجا آمده است.

- برای هر مرحله، همان مرحله را در فایل ‎`attachments/readme.md`‎ (بخش ۳) بخوانید، با کد خودتان مقایسه کنید و اشکال‌ها را برطرف کنید.
- مراحل را **به همین ترتیب** انجام دهید؛ هر مرحله روی مرحله‌ی قبل بنا می‌شود.
- بعد از هر مرحله پروژه را کامپایل کنید.

> **توجه:** این ماژول از entity به نام ‎`Product`‎ در ماژول ‎`products`‎ استفاده می‌کند.
> مرحله‌ی ۱ را بعد از مرحله‌ی ۱ ماژول products انجام دهید و entity آن را خودتان تغییر ندهید.

---

> **تحویل اول (لایه‌ی داده):** مراحل ۱ تا ۴.
> پیش از تحویل، کل پروژه کامپایل شود و deploy روی TomEE بدون خطا انجام شود.

## مرحله ۱ — entityها

**اشکال‌های فعلی:**

- ‎`InventoryTransaction`‎ فیلد ‎`createdAt`‎ را دوباره تعریف کرده است؛ این فیلد در ‎`BaseEntity`‎ وجود دارد.
- ‎`InventoryTransaction`‎ یک ‎`@OneToOne`‎ اجباری به ‎`Order`‎ دارد؛ یعنی هر transaction باید سفارش داشته باشد و هر سفارش فقط یک transaction.
- ‎`referenceId`‎ اجباری است، در حالی که transaction دستی (‎`MANUAL`‎) مرجعی ندارد.
- ‎`InventoryTransaction`‎ هم به ‎`Inventory`‎ وصل است و هم به ‎`Product`‎، به‌جای اینکه به خود ‎`InventoryItem`‎ وصل باشد.
- در ‎`InventoryItem`‎ رابطه با ‎`Product`‎ از نوع ‎`@OneToOne`‎ است؛ یک product می‌تواند در چند انبار باشد.
- ‎`expirationDate`‎ از نوع ‎`LocalDateTime`‎ و اجباری است، در حالی که همه‌ی کالاها تاریخ انقضا ندارند.
- طول ستون‌ها با ‎`@Size`‎ یکی نیست (‎`batchNumber`‎ و ‎`location`‎ و ‎`reason`‎).
- annotationهای validation روی entityها هستند؛ جای آن‌ها در request است.

**آنچه باید انجام دهید:**

- ‎`createdAt`‎ را از ‎`InventoryTransaction`‎ حذف کنید.
- فیلد ‎`order`‎ را حذف کنید؛ مرجع transaction فقط با ‎`referenceType`‎ و ‎`referenceId`‎ مشخص می‌شود (همان ایده‌ی owner در ‎`attachments`‎). ‎`referenceId`‎ اختیاری باشد.
- به‌جای ‎`inventory`‎ و ‎`product`‎، در ‎`InventoryTransaction`‎ یک ‎`@ManyToOne`‎ از نوع ‎`LAZY`‎ به ‎`InventoryItem`‎ با ستون ‎`item_id`‎ بگذارید.
- در ‎`InventoryItem`‎ رابطه با ‎`Product`‎ را ‎`@ManyToOne`‎ از نوع ‎`LAZY`‎ کنید، و ‎`@ManyToOne`‎ به ‎`Inventory`‎ را هم ‎`LAZY`‎ کنید.
- هر product در هر انبار فقط یک ردیف دارد: روی ‎`@Table`‎ یک ‎`uniqueConstraints`‎ برای ستون‌های inventory و product بگذارید.
- ‎`expirationDate`‎ را ‎`LocalDate`‎ و اختیاری کنید.
- طول ستون‌ها را با request یکی کنید و validation را به request منتقل کنید.

## مرحله ۲ — repository

**اشکال فعلی:** هیچ repository متد جست‌وجویی ندارد.

**متدهایی که باید داشته باشید** (غیر از متدهای آماده‌ی ‎`BaseRepository`‎):

| کلاس | متد | کار |
|---|---|---|
| ‎`InventoryRepository`‎ | ‎`Optional<Inventory> findByName(String name)`‎ | انبار با این نام |
| ‎`InventoryItemRepository`‎ | ‎`List<InventoryItem> findByInventory(Long inventoryId)`‎ | موجودی‌های یک انبار |
| ‎`InventoryItemRepository`‎ | ‎`Optional<InventoryItem> findByInventoryAndProduct(Long inventoryId, Long productId)`‎ | موجودی یک product در یک انبار |
| ‎`InventoryItemRepository`‎ | ‎`List<InventoryItem> findLowStock()`‎ | ردیف‌هایی که ‎`quantity`‎ آن‌ها کمتر از ‎`minimumQuantity`‎ است |
| ‎`InventoryItemRepository`‎ | ‎`long countByInventory(Long inventoryId)`‎ | تعداد ردیف‌های یک انبار |
| ‎`InventoryTransactionRepository`‎ | ‎`List<InventoryTransaction> findByItem(Long itemId)`‎ | transactionهای یک ردیف، مرتب بر اساس ‎`id`‎ |

**نکته:** جست‌وجو روی یک فیلد ساده را با ‎`findOneBy(...)`‎ و بقیه را با JPQL بنویسید؛ نمونه‌اش ‎`findByOwner`‎ در ‎`AttachmentRepository`‎ است.

## مرحله ۳ — DTOها

**اشکال‌های فعلی:**

- ‎`InventoryItemRequest`‎ مقدار ‎`quantity`‎ را می‌گیرد؛ موجودی نباید مستقیم تنظیم شود. شناسه‌ی انبار و product را هم نمی‌گیرد.
- DTOهای transaction از نوع قدیمی ‎`Date`‎ و از ‎`@Enumerated`‎ استفاده می‌کنند، و request مقدار ‎`createdAt`‎ را می‌گیرد.
- ‎`InventoryTransactionDto`‎ annotationهای validation دارد؛ dto خروجی validation لازم ندارد.

**فیلدهای هر record:**

| record | فیلدها |
|---|---|
| ‎`InventoryRequest`‎ | ‎`name`‎، ‎`location`‎ |
| ‎`InventoryDto`‎ | ‎`id`‎، ‎`name`‎، ‎`location`‎ |
| ‎`InventoryItemRequest`‎ | ‎`inventoryId`‎، ‎`productId`‎، ‎`minimumQuantity`‎، ‎`batchNumber`‎، ‎`expirationDate`‎ |
| ‎`InventoryItemDto`‎ | ‎`id`‎، ‎`inventoryId`‎، ‎`productId`‎، ‎`productName`‎، ‎`quantity`‎، ‎`minimumQuantity`‎، ‎`batchNumber`‎، ‎`expirationDate`‎ |
| ‎`InventoryTransactionRequest`‎ | ‎`type`‎، ‎`quantity`‎، ‎`referenceType`‎، ‎`referenceId`‎، ‎`reason`‎ |
| ‎`InventoryTransactionDto`‎ | ‎`id`‎، ‎`itemId`‎، ‎`type`‎، ‎`quantity`‎، ‎`referenceType`‎، ‎`referenceId`‎، ‎`reason`‎، ‎`createdAt`‎ |

## مرحله ۴ — mapper

**اشکال فعلی:** relationها تبدیل نمی‌شوند.

**آنچه باید انجام دهید:**

- در ‎`InventoryItemMapper`‎ خروجی ‎`inventoryId`‎ و ‎`productId`‎ و ‎`productName`‎ پر شوند؛ در ورودی ‎`inventory`‎ و ‎`product`‎ و ‎`quantity`‎ تبدیل نشوند.
- در ‎`InventoryTransactionMapper`‎ خروجی ‎`itemId`‎ پر شود؛ در ورودی ‎`item`‎ تبدیل نشود.

---

> **تحویل دوم (منطق و API):** مراحل ۵ تا ۹.
> پیش از تحویل، همه‌ی موارد مرحله‌ی ۹ انجام شده باشد.

## مرحله ۵ — business ruleها و exceptionها

**اشکال فعلی:** پوشه‌ی ‎`exception`‎ وجود ندارد و هیچ business rule‌ای بررسی نمی‌شود.

**کلاس‌هایی که باید بسازید:**

- همه‌ی این کلاس‌ها از ‎`BusinessException`‎ ارث می‌برند.
- هر کدام فقط یک constructor دارد که پیام خطا را می‌سازد.
- نمونه‌ی آماده: کلاس ‎`DuplicateFileNameException`‎ در ماژول ‎`attachments`‎.

| کلاس | چه وقت throw می‌شود |
|---|---|
| ‎`DuplicateInventoryNameException`‎ | انبار دیگری با همین نام وجود دارد |
| ‎`InventoryNotFoundException`‎ | ‎`inventoryId`‎ ارسال‌شده وجود ندارد |
| ‎`ProductNotFoundException`‎ | ‎`productId`‎ ارسال‌شده وجود ندارد |
| ‎`DuplicateInventoryItemException`‎ | این product از قبل در این انبار ردیف دارد |
| ‎`InventoryNotEmptyException`‎ | انباری که هنوز ردیف موجودی دارد، حذف می‌شود |
| ‎`StockNotEmptyException`‎ | ردیفی که موجودی آن صفر نیست، حذف می‌شود |
| ‎`InvalidQuantityException`‎ | ‎`quantity`‎ برای نوع ‎`IN`‎ یا ‎`OUT`‎ یا ‎`RETURN`‎ مثبت نیست، یا برای ‎`ADJUSTMENT`‎ صفر است |
| ‎`InsufficientStockException`‎ | بعد از transaction، موجودی منفی می‌شود |

همه‌ی این‌ها خودکار به جواب 409 تبدیل می‌شوند.

## مرحله ۶ — service

**اشکال‌های فعلی:**

- ‎`InventoryItemServiceImpl`‎ و ‎`InventoryTransactionServiceImpl`‎ کلاس‌های خالی هستند و interface خود را پیاده نمی‌کنند.
- همه‌ی متدهای ‎`InventoryServiceImpl`‎ مقدار ‎`null`‎ یا خالی برمی‌گردانند.
- یک متد اضافه به نام ‎`findPage`‎ در interfaceها هست؛ کار آن را ‎`findAll(page, size)`‎ در ‎`BaseService`‎ انجام می‌دهد.

**متدهایی که باید بسازید:**

| interface | متد | کار |
|---|---|---|
| ‎`InventoryService`‎ | شش متد ‎`BaseService`‎ | CRUD و paging برای انبار |
| ‎`InventoryItemService`‎ | شش متد ‎`BaseService`‎ | CRUD و paging برای ردیف موجودی |
| ‎`InventoryItemService`‎ | ‎`List<InventoryItemDto> findByInventory(Long inventoryId)`‎ | موجودی‌های یک انبار |
| ‎`InventoryItemService`‎ | ‎`List<InventoryItemDto> findLowStock()`‎ | ردیف‌هایی که موجودی‌شان کم است |
| ‎`InventoryTransactionService`‎ | ‎`List<InventoryTransactionDto> findByItem(Long itemId)`‎ | transactionهای یک ردیف |
| ‎`InventoryTransactionService`‎ | ‎`Optional<InventoryTransactionDto> register(Long itemId, InventoryTransactionRequest request)`‎ | ثبت transaction و تغییر موجودی؛ اگر ردیف نبود خالی برمی‌گردد |

‎`InventoryTransactionService`‎ دیگر از ‎`BaseService`‎ ارث نمی‌برد؛ transaction ثبت‌شده ویرایش یا حذف نمی‌شود.

**متدهای کمکی ‎`private`‎:**

| کلاس | متد کمکی | کار |
|---|---|---|
| ‎`InventoryServiceImpl`‎ | ‎`checkNameIsFree(request, currentId)`‎ | نام تکراری: ‎`DuplicateInventoryNameException`‎ |
| ‎`InventoryServiceImpl`‎ | ‎`checkIsEmpty(id)`‎ | اگر ردیف موجودی داشت: ‎`InventoryNotEmptyException`‎ |
| ‎`InventoryItemServiceImpl`‎ | ‎`loadInventory(inventoryId)`‎ | اگر نبود: ‎`InventoryNotFoundException`‎ |
| ‎`InventoryItemServiceImpl`‎ | ‎`loadProduct(productId)`‎ | product را از ‎`ProductRepository`‎ بخواند؛ اگر نبود: ‎`ProductNotFoundException`‎ |
| ‎`InventoryItemServiceImpl`‎ | ‎`checkIsUnique(inventoryId, productId, currentId)`‎ | اگر ردیف تکراری بود: ‎`DuplicateInventoryItemException`‎ |
| ‎`InventoryTransactionServiceImpl`‎ | ‎`applyQuantity(item, request)`‎ | ‎`IN`‎ و ‎`RETURN`‎ اضافه می‌کنند، ‎`OUT`‎ کم می‌کند و ‎`ADJUSTMENT`‎ با علامت خودش اعمال می‌شود؛ خطاها: ‎`InvalidQuantityException`‎ و ‎`InsufficientStockException`‎ |

**کجا صدا زده شوند:**

| متد service | متدهای کمکی و کار |
|---|---|
| ‎`create`‎ و ‎`update`‎ در ‎`InventoryServiceImpl`‎ | ‎`checkNameIsFree`‎ |
| ‎`deleteById`‎ در ‎`InventoryServiceImpl`‎ | ‎`checkIsEmpty`‎ پیش از حذف |
| ‎`create`‎ و ‎`update`‎ در ‎`InventoryItemServiceImpl`‎ | ‎`loadInventory`‎ و ‎`loadProduct`‎ و ‎`checkIsUnique`‎؛ ردیف جدید با موجودی صفر ساخته می‌شود |
| ‎`deleteById`‎ در ‎`InventoryItemServiceImpl`‎ | اگر موجودی صفر نبود: ‎`StockNotEmptyException`‎ |
| ‎`register`‎ در ‎`InventoryTransactionServiceImpl`‎ | ‎`applyQuantity`‎؛ سپس ذخیره‌ی transaction |

**نکته‌ها:**

- ‎`register`‎ باید ‎`@Transactional`‎ باشد: ذخیره‌ی transaction و تغییر موجودی یا با هم انجام می‌شوند یا هیچ‌کدام.
- اگر دو نفر همزمان موجودی یک ردیف را تغییر دهند، فیلد ‎`version`‎ در ‎`BaseEntity`‎ جلوی از دست رفتن یکی از تغییرها را می‌گیرد.
- ‎`ProductRepository`‎ را از ماژول products با ‎`@Inject`‎ بگیرید؛ entity به نام ‎`Product`‎ را تغییر ندهید.

## مرحله ۷ — REST API

**اشکال فعلی:** کلاس ‎`InventoryApi`‎ خالی است.

**یادآوری:** در API نه ‎`try/catch`‎ بنویسید و نه ‎`ApiResponse`‎ بسازید؛ فقط DTO را برگردانید و exception را throw کنید (بخش ۶ [راهنمای دانشجو](../student-guide.md)).

**endpointهایی که در پایان باید کار کنند:**

| HTTP | مسیر | جواب |
|---|---|---|
| ‎`GET`‎ | ‎`/inventories`‎ | همه‌ی انبارها |
| ‎`GET`‎ | ‎`/inventories/page?page=0&size=10`‎ | یک صفحه |
| ‎`GET`‎ | ‎`/inventories/{id}`‎ | یک انبار؛ اگر نبود 404 |
| ‎`GET`‎ | ‎`/inventories/{id}/items`‎ | موجودی‌های آن انبار |
| ‎`POST`‎ | ‎`/inventories`‎ | 201 همراه انبار جدید |
| ‎`PUT`‎ | ‎`/inventories/{id}`‎ | 200 همراه انبار ویرایش‌شده |
| ‎`DELETE`‎ | ‎`/inventories/{id}`‎ | 204؛ اگر خالی نباشد 409 |
| ‎`GET`‎ | ‎`/inventory-items`‎ | همه‌ی ردیف‌های موجودی |
| ‎`GET`‎ | ‎`/inventory-items/low-stock`‎ | ردیف‌هایی که موجودی‌شان کم است |
| ‎`GET`‎ | ‎`/inventory-items/page?page=0&size=10`‎ | یک صفحه |
| ‎`GET`‎ | ‎`/inventory-items/{id}`‎ | یک ردیف؛ اگر نبود 404 |
| ‎`POST`‎ | ‎`/inventory-items`‎ | 201 همراه ردیف جدید با موجودی صفر |
| ‎`PUT`‎ | ‎`/inventory-items/{id}`‎ | 200 همراه ردیف ویرایش‌شده |
| ‎`DELETE`‎ | ‎`/inventory-items/{id}`‎ | 204؛ اگر موجودی صفر نباشد 409 |
| ‎`GET`‎ | ‎`/inventory-items/{id}/transactions`‎ | transactionهای آن ردیف |
| ‎`POST`‎ | ‎`/inventory-items/{id}/transactions`‎ | 201 همراه transaction جدید؛ اگر موجودی کافی نباشد 409 |

**نکته:** برای هر مسیر اصلی یک کلاس API بسازید: ‎`InventoryApi`‎ و ‎`InventoryItemApi`‎ و ‎`InventoryTransactionApi`‎. مسیر کلاس آخر ‎`/inventory-items/{itemId}/transactions`‎ است.

## مرحله ۸ — پاک‌سازی

- متد ‎`findPage`‎ را از interfaceها حذف کنید.
- کلاس ‎`InventoryServlet`‎ خالی است و حتی servlet هم نیست؛ یا کاملش کنید یا حذفش کنید.

## مرحله ۹ — چه وقت کار تمام است؟

1. کل پروژه بدون خطا کامپایل می‌شود.
2. پروژه deploy شده و همه‌ی endpointهای جدول مرحله‌ی ۷ روی TomEE یک بار امتحان شده‌اند.
3. این خطاها امتحان شده‌اند: یک خطای validation با جواب 400، یک ‎`id`‎ ناموجود با جواب 404، و هر هشت قاعده‌ی business با جواب 409.
4. بعد از چند transaction، موجودی هر ردیف با جمع transactionهایش برابر است.
5. همه‌ی موارد چک‌لیست بخش ۹ فایل ‎`attachments/readme.md`‎ به‌جز تست‌ها انجام شده‌اند.
6. هیچ‌کدام از موارد جدول Common mistakes در بخش ۱۰ فایل ‎`attachments/readme.md`‎ در کد شما باقی نمانده است.

تست‌نویسی بعداً در یک دستور کار جدا اضافه می‌شود.

</div>
