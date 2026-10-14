<div dir="rtl">

# دستور کار ماژول orders

هدف: رساندن ماژول orders (‎`Order`‎ و ‎`OrderItem`‎) به سطح ماژول ‎`attachments`‎.

**پیش از شروع:** فایل [راهنمای دانشجو](../student-guide.md) را کامل بخوانید. ساختار ماژول، مسیر کامل یک request، کلاس‌هایی که از core ارث می‌برید، ترتیب کار ماژول‌ها و روش تحویل در آنجا آمده است.

- برای هر مرحله، همان مرحله را در فایل ‎`attachments/readme.md`‎ (بخش ۳) بخوانید، با کد خودتان مقایسه کنید و اشکال‌ها را برطرف کنید.
- مراحل را **به همین ترتیب** انجام دهید؛ هر مرحله روی مرحله‌ی قبل بنا می‌شود.
- بعد از هر مرحله پروژه را کامپایل کنید.

> **توجه:** این ماژول از entity به نام ‎`Product`‎ در ماژول ‎`products`‎ و entity به نام ‎`User`‎ در ماژول ‎`users`‎ استفاده می‌کند.
> مرحله‌ی ۱ را بعد از مرحله‌ی ۱ آن دو ماژول انجام دهید و entityهای آن‌ها را خودتان تغییر ندهید.

---

> **تحویل اول (لایه‌ی داده):** مراحل ۱ تا ۴.
> پیش از تحویل، کل پروژه کامپایل شود و deploy روی TomEE بدون خطا انجام شود.

## مرحله ۱ — entityها

**اشکال‌های فعلی:**

- رابطه‌ی سفارش و قلم‌ها دو بار تعریف شده است: ‎`Order`‎ یک ‎`@OneToMany`‎ با ‎`@JoinColumn(name = "items")`‎ دارد و ‎`OrderItem`‎ هم یک ‎`@ManyToOne`‎ به ‎`Order`‎.
- ‎`client`‎ با ‎`@OneToOne`‎ به ‎`User`‎ وصل شده است؛ در این حالت هر user فقط یک سفارش می‌تواند داشته باشد.
- در ‎`OrderItem`‎ رابطه با ‎`Product`‎ از نوع ‎`@OneToOne`‎ است و ستون آن ‎`product_name`‎ نام گرفته است.
- مبلغ ‎`total`‎ از نوع ‎`Long`‎ است و قیمت هر قلم در زمان سفارش ذخیره نمی‌شود.
- فیلد ‎`date`‎ همان کار ‎`createdAt`‎ در ‎`BaseEntity`‎ را تکرار می‌کند.
- مقدارهای enumهای ‎`ShippingMethod`‎ و ‎`ShippingStatus`‎ فقط ‎`A`‎ و ‎`B`‎ و ‎`C`‎ هستند، و معنای ‎`OrderType`‎ روشن نیست.
- نام کلاس ‎`OrderItem`‎ جمع است و فیلد ‎`paymentMethod`‎ کامنت شده است.

**آنچه باید انجام دهید:**

- نام کلاس را به ‎`OrderItem`‎ تغییر دهید.
- در ‎`Order`‎ لیست قلم‌ها را ‎`items`‎ بنامید و آن را ‎`@OneToMany`‎ کنید با سه تنظیم: ‎`mappedBy = "order"`‎ و ‎`cascade = CascadeType.ALL`‎ و ‎`orphanRemoval = true`‎.
  برخلاف ماژول‌های دیگر، اینجا لیست لازم است: قلم‌ها جزئی از سفارش هستند و همراه آن ذخیره و حذف می‌شوند.
- ‎`client`‎ را به ‎`customer`‎ تغییر دهید: ‎`@ManyToOne`‎ از نوع ‎`LAZY`‎، با ستون ‎`customer_id`‎ که اجباری باشد.
- رابطه‌ی ‎`OrderItem`‎ با ‎`Product`‎ را ‎`@ManyToOne`‎ از نوع ‎`LAZY`‎ با ستون ‎`product_id`‎ کنید.
- در ‎`OrderItem`‎ فیلد ‎`quantity`‎ از نوع ‎`Integer`‎ و اجباری باشد، و فیلد ‎`unitPrice`‎ از نوع ‎`BigDecimal`‎ اضافه شود (قیمت محصول در لحظه‌ی سفارش).
- ‎`total`‎ را ‎`BigDecimal`‎ کنید و ‎`date`‎ و فیلد کامنت‌شده را حذف کنید.
- مقدارهای enumها:
  - ‎`ShippingMethod`‎: ‎`POST`‎ و ‎`COURIER`‎ و ‎`PICKUP`‎
  - ‎`ShippingStatus`‎: ‎`PENDING`‎ و ‎`SHIPPED`‎ و ‎`DELIVERED`‎
  - به‌جای ‎`OrderType`‎ یک enum به نام ‎`OrderStatus`‎ با ‎`NEW`‎ و ‎`CONFIRMED`‎ و ‎`CANCELLED`‎ بسازید.

## مرحله ۲ — repository

**اشکال فعلی:** ‎`OrderRepository`‎ هیچ متد جست‌وجویی ندارد.

**متدهایی که باید داشته باشید** (غیر از متدهای آماده‌ی ‎`BaseRepository`‎):

| کلاس | متد | کار |
|---|---|---|
| ‎`OrderRepository`‎ | ‎`List<Order> findByCustomer(Long customerId)`‎ | سفارش‌های یک مشتری، مرتب بر اساس ‎`id`‎ |
| ‎`OrderRepository`‎ | ‎`List<Order> findByStatus(OrderStatus status)`‎ | سفارش‌ها با یک وضعیت |

**نکته:** برای ‎`OrderItem`‎ repository لازم نیست؛ با ‎`cascade`‎ همراه سفارش ذخیره و حذف می‌شود.

## مرحله ۳ — DTOها

**اشکال‌های فعلی:**

- ‎`OrderRequest`‎ و ‎`OrderItemRequest`‎ و ‎`OrderDto`‎ خالی هستند.
- ‎`OrderItemDto`‎ یک class است، نه record، و خود entityهای ‎`Order`‎ و ‎`Product`‎ را در خود دارد.

**فیلدهای هر record:**

| record | فیلدها |
|---|---|
| ‎`OrderItemRequest`‎ | ‎`productId`‎، ‎`quantity`‎ |
| ‎`OrderRequest`‎ | ‎`customerId`‎، ‎`shippingMethod`‎، ‎`items`‎ |
| ‎`OrderItemDto`‎ | ‎`id`‎، ‎`productId`‎، ‎`productName`‎، ‎`quantity`‎، ‎`unitPrice`‎ |
| ‎`OrderDto`‎ | ‎`id`‎، ‎`customerId`‎، ‎`customerUsername`‎، ‎`status`‎، ‎`shippingMethod`‎، ‎`shippingStatus`‎، ‎`total`‎، ‎`createdAt`‎، ‎`items`‎ |

**نکته:** روی ‎`items`‎ در ‎`OrderRequest`‎ هم ‎`@NotEmpty`‎ بگذارید و هم ‎`@Valid`‎، تا تک‌تک قلم‌ها هم بررسی شوند.

## مرحله ۴ — mapper

**اشکال فعلی:** فقط ‎`OrderMapper`‎ وجود دارد و هیچ relation‌ای را تبدیل نمی‌کند.

**آنچه باید انجام دهید:**

- ‎`OrderItemMapper`‎ را بسازید: در خروجی ‎`productId`‎ و ‎`productName`‎ از product پر شوند؛ در ورودی ‎`product`‎ و ‎`order`‎ و ‎`unitPrice`‎ تبدیل نشوند.
- در ‎`@Mapper`‎ کلاس ‎`OrderMapper`‎، علاوه بر ‎`config`‎ تنظیم ‎`uses = OrderItemMapper.class`‎ را هم بگذارید تا قلم‌ها را هم تبدیل کند.
- در خروجی ‎`OrderMapper`‎، فیلدهای ‎`customerId`‎ و ‎`customerUsername`‎ از customer پر شوند.
- در ورودی ‎`OrderMapper`‎، فیلدهای ‎`customer`‎ و ‎`items`‎ و ‎`status`‎ و ‎`shippingStatus`‎ و ‎`total`‎ تبدیل **نشوند**؛ service آن‌ها را پر می‌کند.

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
| ‎`CustomerNotFoundException`‎ | ‎`customerId`‎ ارسال‌شده وجود ندارد |
| ‎`ProductNotFoundException`‎ | یکی از ‎`productId`‎ها وجود ندارد |
| ‎`InactiveProductException`‎ | یکی از productها فعال نیست |
| ‎`DuplicateOrderItemException`‎ | یک product در یک سفارش دو بار آمده است |
| ‎`OrderNotEditableException`‎ | سفارشی که دیگر ‎`NEW`‎ نیست ویرایش، حذف، تأیید یا لغو می‌شود |
| ‎`InvalidOrderStatusException`‎ | ارسال یا تحویل در وضعیت نادرست (جدول مرحله‌ی ۶) |

همه‌ی این‌ها خودکار به جواب 409 تبدیل می‌شوند.

## مرحله ۶ — service

**اشکال‌های فعلی:**

- همه‌ی متدهای ‎`OrderServiceImpl`‎ مقدار ‎`null`‎ یا خالی برمی‌گردانند و هیچ repository یا mapper‌ای inject نشده است.
- ‎`OrderItemService`‎ لازم نیست؛ قلم‌ها فقط از طریق سفارش مدیریت می‌شوند.

**متدهایی که باید به ‎`OrderService`‎ اضافه کنید** (غیر از شش متد ‎`BaseService`‎):

| متد | کار |
|---|---|
| ‎`List<OrderDto> findByCustomer(Long customerId)`‎ | سفارش‌های یک مشتری |
| ‎`Optional<OrderDto> confirm(Long id)`‎ | وضعیت ‎`NEW`‎ را به ‎`CONFIRMED`‎ می‌برد |
| ‎`Optional<OrderDto> cancel(Long id)`‎ | وضعیت ‎`NEW`‎ را به ‎`CANCELLED`‎ می‌برد |
| ‎`Optional<OrderDto> ship(Long id)`‎ | فقط وقتی سفارش ‎`CONFIRMED`‎ و ارسال ‎`PENDING`‎ است؛ ارسال را ‎`SHIPPED`‎ می‌کند |
| ‎`Optional<OrderDto> deliver(Long id)`‎ | فقط وقتی ارسال ‎`SHIPPED`‎ است؛ آن را ‎`DELIVERED`‎ می‌کند |

**متدهای کمکی ‎`private`‎ در ‎`OrderServiceImpl`‎:**

| متد کمکی | کار |
|---|---|
| ‎`loadCustomer(customerId)`‎ | user را از ‎`UserRepository`‎ بخواند؛ اگر نبود: ‎`CustomerNotFoundException`‎ |
| ‎`buildItems(order, itemRequests)`‎ | productها را از ‎`ProductRepository`‎ بخواند، ناموجود و غیرفعال و تکراری را رد کند، و ‎`unitPrice`‎ را از قیمت product پر کند |
| ‎`calculateTotal(order)`‎ | جمع ‎`unitPrice × quantity`‎ همه‌ی قلم‌ها |
| ‎`checkEditable(order)`‎ | اگر وضعیت ‎`NEW`‎ نبود: ‎`OrderNotEditableException`‎ |

**کجا صدا زده شوند:**

| متد service | متدهای کمکی و کار |
|---|---|
| ‎`create`‎ | ‎`loadCustomer`‎ و ‎`buildItems`‎ و ‎`calculateTotal`‎؛ وضعیت ‎`NEW`‎ و ارسال ‎`PENDING`‎ |
| ‎`update`‎ | ‎`checkEditable`‎؛ خالی کردن قلم‌های قبلی و دوباره ‎`buildItems`‎ و ‎`calculateTotal`‎ |
| ‎`deleteById`‎ و ‎`confirm`‎ و ‎`cancel`‎ | ‎`checkEditable`‎ |
| ‎`ship`‎ و ‎`deliver`‎ | اگر وضعیت درست نبود: ‎`InvalidOrderStatusException`‎ |

**نکته‌ها:**

- ‎`ProductRepository`‎ و ‎`UserRepository`‎ را از ماژول‌های خودشان با ‎`@Inject`‎ بگیرید؛ entityهای آن‌ها را تغییر ندهید.
- مبلغ‌ها را همیشه service حساب می‌کند؛ کلاینت ‎`total`‎ یا ‎`unitPrice`‎ نمی‌فرستد.
- قلم‌ها lazy بارگذاری می‌شوند؛ متدهای خواندنی که ‎`OrderDto`‎ می‌سازند هم باید ‎`@Transactional`‎ باشند.

## مرحله ۷ — REST API

**اشکال‌های فعلی:**

- هر دو کلاس ‎`OrderApi`‎ و ‎`OrderItemApi`‎ مسیر ‎`@Path("/api/v1/orders")`‎ دارند: هم prefix تکرار شده و هم دو کلاس روی یک مسیر هستند.
- هیچ‌کدام ‎`@RequestScoped`‎ ندارند و ‎`OrderApi`‎ کلاس ‎`Impl`‎ را inject می‌کند.
- فقط یک متد ‎`GET`‎ وجود دارد.

**یادآوری:** در API نه ‎`try/catch`‎ بنویسید و نه ‎`ApiResponse`‎ بسازید؛ فقط DTO را برگردانید و exception را throw کنید (بخش ۶ [راهنمای دانشجو](../student-guide.md)).

**endpointهایی که در پایان باید کار کنند:**

| HTTP | مسیر | جواب |
|---|---|---|
| ‎`GET`‎ | ‎`/orders`‎ | همه‌ی سفارش‌ها |
| ‎`GET`‎ | ‎`/orders?customerId=1`‎ | سفارش‌های یک مشتری |
| ‎`GET`‎ | ‎`/orders/page?page=0&size=10`‎ | یک صفحه |
| ‎`GET`‎ | ‎`/orders/{id}`‎ | یک سفارش همراه قلم‌ها؛ اگر نبود 404 |
| ‎`POST`‎ | ‎`/orders`‎ | 201 همراه سفارش جدید |
| ‎`PUT`‎ | ‎`/orders/{id}`‎ | 200 همراه سفارش ویرایش‌شده؛ اگر ‎`NEW`‎ نباشد 409 |
| ‎`DELETE`‎ | ‎`/orders/{id}`‎ | 204؛ اگر ‎`NEW`‎ نباشد 409 |
| ‎`POST`‎ | ‎`/orders/{id}/confirm`‎ | 200 همراه سفارش تأییدشده |
| ‎`POST`‎ | ‎`/orders/{id}/cancel`‎ | 200 همراه سفارش لغوشده |
| ‎`POST`‎ | ‎`/orders/{id}/ship`‎ | 200 همراه سفارش ارسال‌شده |
| ‎`POST`‎ | ‎`/orders/{id}/deliver`‎ | 200 همراه سفارش تحویل‌شده |

## مرحله ۸ — پاک‌سازی

- ‎`OrderItemService`‎ و ‎`OrderItemServiceImpl`‎ و ‎`OrderItemApi`‎ را حذف کنید.
- نام پوشه‌ی ‎`sevlet`‎ غلط املایی دارد. کلاس‌های ‎`OrderServlet`‎ و ‎`OrderItemServlet`‎ خالی هستند؛ یا کاملشان کنید یا حذفشان کنید.

## مرحله ۹ — چه وقت کار تمام است؟

1. کل پروژه بدون خطا کامپایل می‌شود.
2. پروژه deploy شده و همه‌ی endpointهای جدول مرحله‌ی ۷ روی TomEE یک بار امتحان شده‌اند.
3. این خطاها امتحان شده‌اند: یک خطای validation با جواب 400، یک ‎`id`‎ ناموجود با جواب 404، و هر شش قاعده‌ی business با جواب 409.
4. ‎`total`‎ یک سفارش با جمع قلم‌هایش برابر است، حتی بعد از ویرایش.
5. همه‌ی موارد چک‌لیست بخش ۹ فایل ‎`attachments/readme.md`‎ به‌جز تست‌ها انجام شده‌اند.
6. هیچ‌کدام از موارد جدول Common mistakes در بخش ۱۰ فایل ‎`attachments/readme.md`‎ در کد شما باقی نمانده است.

تست‌نویسی بعداً در یک دستور کار جدا اضافه می‌شود.

</div>
