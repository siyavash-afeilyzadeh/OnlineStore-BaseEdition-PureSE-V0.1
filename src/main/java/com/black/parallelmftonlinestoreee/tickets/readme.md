<div dir="rtl">

# دستور کار ماژول tickets

هدف: رساندن ماژول tickets (‎`Ticket`‎ و ‎`TicketMessage`‎) به سطح ماژول ‎`attachments`‎.

**پیش از شروع:** فایل [راهنمای دانشجو](../student-guide.md) را کامل بخوانید. ساختار ماژول، مسیر کامل یک request، کلاس‌هایی که از core ارث می‌برید، ترتیب کار ماژول‌ها و روش تحویل در آنجا آمده است.

- برای هر مرحله، همان مرحله را در فایل ‎`attachments/readme.md`‎ (بخش ۳) بخوانید، با کد خودتان مقایسه کنید و اشکال‌ها را برطرف کنید.
- مراحل را **به همین ترتیب** انجام دهید؛ هر مرحله روی مرحله‌ی قبل بنا می‌شود.
- بعد از هر مرحله پروژه را کامپایل کنید.

> **توجه:** این ماژول از entity به نام ‎`User`‎ در ماژول ‎`users`‎ استفاده می‌کند.
> مرحله‌ی ۱ را بعد از مرحله‌ی ۱ ماژول users انجام دهید و entity آن را خودتان تغییر ندهید.

---

> **تحویل اول (لایه‌ی داده):** مراحل ۱ تا ۴.
> پیش از تحویل، کل پروژه کامپایل شود و deploy روی TomEE بدون خطا انجام شود.

## مرحله ۱ — entityها

**اشکال‌های فعلی:**

- annotationهای validation (‎`@NotBlank`‎ و ‎`@Size`‎) روی entityها هستند؛ جای آن‌ها در request است.
- در ‎`TicketMessage`‎ هر دو فیلد ‎`sender`‎ و ‎`receiver`‎ از نوع ‎`@ManyToMany List<User>`‎ هستند؛ هر پیام فقط یک فرستنده دارد.
- ‎`Ticket`‎ لیست ‎`ticketMessage`‎ از نوع ‎`@OneToMany`‎ دارد که لازم نیست.
- ‎`@ManyToOne`‎ به ‎`User`‎ به‌طور پیش‌فرض ‎`EAGER`‎ است.
- enum به نام ‎`TicketStatus`‎ در خود پوشه‌ی ‎`entity`‎ است، نه در ‎`entity/enums`‎.
- نام فیلد ‎`ticketStatus`‎ تکراری است؛ در کلاس ‎`Ticket`‎ کافی است ‎`status`‎ باشد.

**آنچه باید انجام دهید:**

- validation را از entity به request منتقل کنید؛ در entity فقط قواعد ‎`@Column`‎ بماند.
- ‎`sender`‎ را ‎`@ManyToOne`‎ از نوع ‎`LAZY`‎ کنید، با ستون ‎`sender_id`‎ که اجباری باشد؛ ‎`receiver`‎ را حذف کنید.
- لیست ‎`ticketMessage`‎ را حذف کنید؛ پیام‌های یک ticket را با repository پیدا می‌کنید.
- ‎`@ManyToOne`‎ از ‎`Ticket`‎ به ‎`User`‎ را ‎`LAZY`‎ کنید.
- ‎`TicketStatus`‎ را به ‎`entity/enums`‎ منتقل کنید و ‎`ticketStatus`‎ را به ‎`status`‎ تغییر نام دهید.

## مرحله ۲ — repository

**اشکال‌های فعلی:**

- ‎`findByTicket`‎ فقط **یک** پیام برمی‌گرداند (‎`Optional`‎)، در حالی که هر ticket چند پیام دارد.
- ‎`findBySubject`‎ جایی لازم نیست.

**متدهایی که باید داشته باشید** (غیر از متدهای آماده‌ی ‎`BaseRepository`‎):

| کلاس | متد | کار |
|---|---|---|
| ‎`TicketRepository`‎ | ‎`List<Ticket> findByUser(Long userId)`‎ | ticketهای یک user، مرتب بر اساس ‎`id`‎ |
| ‎`TicketMessageRepository`‎ | ‎`List<TicketMessage> findByTicket(Long ticketId)`‎ | پیام‌های یک ticket، مرتب بر اساس ‎`id`‎ |

**نکته:** این دو را با JPQL بنویسید؛ نمونه‌اش ‎`findByOwner`‎ در ‎`AttachmentRepository`‎ است.

## مرحله ۳ — DTOها

**اشکال‌های فعلی:**

- DTOها خود entityها را در خود دارند: ‎`User`‎ و ‎`Ticket`‎ و ‎`List<TicketMessage>`‎.
- ‎`TicketRequest`‎ وضعیت و لیست پیام‌ها را می‌گیرد؛ وضعیت را service تعیین می‌کند و پیام‌ها جدا فرستاده می‌شوند.
- روی فیلدهای DTO از ‎`@Enumerated`‎ استفاده شده است؛ این annotation فقط مخصوص entity است.

**فیلدهای هر record:**

| record | فیلدها |
|---|---|
| ‎`TicketRequest`‎ | ‎`subject`‎، ‎`userId`‎ |
| ‎`TicketDto`‎ | ‎`id`‎، ‎`subject`‎، ‎`userId`‎، ‎`username`‎، ‎`status`‎، ‎`createdAt`‎ |
| ‎`TicketMessageRequest`‎ | ‎`senderId`‎، ‎`content`‎ |
| ‎`TicketMessageDto`‎ | ‎`id`‎، ‎`ticketId`‎، ‎`senderId`‎، ‎`senderUsername`‎، ‎`content`‎، ‎`createdAt`‎ |

## مرحله ۴ — mapper

**اشکال فعلی:** relationها تبدیل نمی‌شوند.

**آنچه باید انجام دهید:**

- در ‎`TicketMapper`‎ خروجی ‎`userId`‎ و ‎`username`‎ از user پر شوند؛ در ورودی ‎`user`‎ و ‎`status`‎ تبدیل نشوند.
- در ‎`TicketMessageMapper`‎ خروجی ‎`ticketId`‎ و ‎`senderId`‎ و ‎`senderUsername`‎ پر شوند؛ در ورودی ‎`ticket`‎ و ‎`sender`‎ تبدیل نشوند.

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
| ‎`UserNotFoundException`‎ | ‎`userId`‎ یا ‎`senderId`‎ ارسال‌شده وجود ندارد |
| ‎`TicketClosedException`‎ | ticket بسته‌شده ویرایش یا دوباره بسته می‌شود، یا به آن پیام فرستاده می‌شود |

هر دو خودکار به جواب 409 تبدیل می‌شوند.

## مرحله ۶ — service

**اشکال‌های فعلی:**

- متد ‎`create`‎ در هر دو کلاس ‎`Impl`‎ بدون ‎`@Transactional`‎ است.
- متد ‎`findAll(page, size)`‎ مقدار ‎`null`‎ برمی‌گرداند و به‌جای آن یک متد اضافه به نام ‎`findPage`‎ در interface ساخته شده است.
- پیام‌ها هم مثل یک entity مستقل CRUD کامل دارند، در حالی که پیام فرستاده‌شده نباید ویرایش یا حذف شود.

**متدهایی که باید بسازید:**

| interface | متد | کار |
|---|---|---|
| ‎`TicketService`‎ | شش متد ‎`BaseService`‎ | CRUD و paging برای ticket |
| ‎`TicketService`‎ | ‎`List<TicketDto> findByUser(Long userId)`‎ | ticketهای یک user |
| ‎`TicketService`‎ | ‎`Optional<TicketDto> close(Long id)`‎ | وضعیت را ‎`CLOSED`‎ می‌کند |
| ‎`TicketMessageService`‎ | ‎`List<TicketMessageDto> findByTicket(Long ticketId)`‎ | پیام‌های یک ticket |
| ‎`TicketMessageService`‎ | ‎`Optional<TicketMessageDto> addMessage(Long ticketId, TicketMessageRequest request)`‎ | پیام جدید؛ اگر ticket نبود خالی برمی‌گردد |

‎`TicketMessageService`‎ دیگر از ‎`BaseService`‎ ارث نمی‌برد؛ فقط همین دو متد را دارد. متد ‎`findPage`‎ را از هر دو interface حذف کنید.

**متدهای کمکی ‎`private`‎:**

| کلاس | متد کمکی | کار |
|---|---|---|
| هر دو کلاس ‎`Impl`‎ | ‎`loadUser(userId)`‎ | user را از ‎`UserRepository`‎ بخواند؛ اگر نبود: ‎`UserNotFoundException`‎ |
| هر دو کلاس ‎`Impl`‎ | ‎`checkNotClosed(ticket)`‎ | اگر ticket بسته بود: ‎`TicketClosedException`‎ |

**کجا صدا زده شوند:**

| متد service | متدهای کمکی و کار |
|---|---|
| ‎`create`‎ در ‎`TicketServiceImpl`‎ | ‎`loadUser`‎؛ وضعیت ‎`OPEN`‎ |
| ‎`update`‎ و ‎`close`‎ در ‎`TicketServiceImpl`‎ | ‎`checkNotClosed`‎ |
| ‎`addMessage`‎ در ‎`TicketMessageServiceImpl`‎ | ‎`checkNotClosed`‎ و ‎`loadUser`‎؛ اگر فرستنده صاحب ticket نبود، وضعیت ticket به ‎`IN_PROGRESS`‎ برود |

**نکته:** ‎`UserRepository`‎ را از ماژول users با ‎`@Inject`‎ بگیرید؛ entity به نام ‎`User`‎ را تغییر ندهید.

## مرحله ۷ — REST API

**اشکال‌های فعلی:**

- نام متد paging در ‎`TicketApi`‎ برابر ‎`getAttachmentsPage`‎ است (کپی از ماژول دیگر) و ‎`findPage`‎ را صدا می‌زند.
- مسیر ‎`/ticket_messages`‎ جدا از ticket است؛ پیام‌ها باید زیر مسیر ticket خودشان باشند.
- نام متد ‎`creatTicketMessage`‎ غلط املایی دارد.

**یادآوری:** در API نه ‎`try/catch`‎ بنویسید و نه ‎`ApiResponse`‎ بسازید؛ فقط DTO را برگردانید و exception را throw کنید (بخش ۶ [راهنمای دانشجو](../student-guide.md)).

**endpointهایی که در پایان باید کار کنند:**

| HTTP | مسیر | جواب |
|---|---|---|
| ‎`GET`‎ | ‎`/tickets`‎ | همه‌ی ticketها |
| ‎`GET`‎ | ‎`/tickets?userId=1`‎ | ticketهای یک user |
| ‎`GET`‎ | ‎`/tickets/page?page=0&size=10`‎ | یک صفحه |
| ‎`GET`‎ | ‎`/tickets/{id}`‎ | یک ticket؛ اگر نبود 404 |
| ‎`POST`‎ | ‎`/tickets`‎ | 201 همراه ticket جدید |
| ‎`PUT`‎ | ‎`/tickets/{id}`‎ | 200 همراه ticket ویرایش‌شده؛ اگر بسته باشد 409 |
| ‎`DELETE`‎ | ‎`/tickets/{id}`‎ | 204 |
| ‎`POST`‎ | ‎`/tickets/{id}/close`‎ | 200 همراه ticket بسته‌شده |
| ‎`GET`‎ | ‎`/tickets/{id}/messages`‎ | پیام‌های ticket؛ اگر ticket نبود 404 |
| ‎`POST`‎ | ‎`/tickets/{id}/messages`‎ | 201 همراه پیام جدید؛ اگر ticket بسته باشد 409 |

**نکته:** مسیر کلاس ‎`TicketMessageApi`‎ را ‎`@Path("/tickets/{ticketId}/messages")`‎ بگذارید.

## مرحله ۸ — پاک‌سازی

- متدهای ‎`PUT`‎ و ‎`DELETE`‎ پیام‌ها و متد ‎`findPage`‎ را حذف کنید.
- کلاس‌های ‎`TicketServlet`‎ و ‎`TicketMessageServlet`‎ خالی هستند؛ یا کاملشان کنید یا حذفشان کنید.

## مرحله ۹ — چه وقت کار تمام است؟

1. کل پروژه بدون خطا کامپایل می‌شود.
2. پروژه deploy شده و همه‌ی endpointهای جدول مرحله‌ی ۷ روی TomEE یک بار امتحان شده‌اند.
3. این خطاها امتحان شده‌اند: یک خطای validation با جواب 400، یک ‎`id`‎ ناموجود با جواب 404، و هر دو قاعده‌ی business با جواب 409.
4. وقتی user دیگری به ticket جواب می‌دهد، وضعیت آن ‎`IN_PROGRESS`‎ می‌شود.
5. همه‌ی موارد چک‌لیست بخش ۹ فایل ‎`attachments/readme.md`‎ به‌جز تست‌ها انجام شده‌اند.
6. هیچ‌کدام از موارد جدول Common mistakes در بخش ۱۰ فایل ‎`attachments/readme.md`‎ در کد شما باقی نمانده است.

تست‌نویسی بعداً در یک دستور کار جدا اضافه می‌شود.

</div>
