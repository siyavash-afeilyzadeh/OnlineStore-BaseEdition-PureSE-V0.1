<div dir="rtl">

# دستور کار ماژول financials

هدف: رساندن ماژول financials به سطح ماژول ‎`attachments`‎. این ماژول سه بخش دارد:

| بخش | entityها | کار |
|---|---|---|
| بانک | ‎`Bank`‎ و ‎`BankAccount`‎ و ‎`Transaction`‎ | حساب‌های بانکی و واریز و برداشت از آن‌ها |
| صندوق | ‎`Cashier`‎ | صندوق‌دارها |
| پرداخت | ‎`Payment`‎ و ‎`CashPayment`‎ و ‎`CardPayment`‎ و ‎`ChequePayment`‎ | پرداخت سفارش‌ها به سه روش |

**پیش از شروع:** فایل [راهنمای دانشجو](../student-guide.md) را کامل بخوانید. ساختار ماژول، مسیر کامل یک request، کلاس‌هایی که از core ارث می‌برید، ترتیب کار ماژول‌ها و روش تحویل در آنجا آمده است.

- برای هر مرحله، همان مرحله را در فایل ‎`attachments/readme.md`‎ (بخش ۳) بخوانید، با کد خودتان مقایسه کنید و اشکال‌ها را برطرف کنید.
- مراحل را **به همین ترتیب** انجام دهید؛ هر مرحله روی مرحله‌ی قبل بنا می‌شود.
- بعد از هر مرحله پروژه را کامپایل کنید.
- این ماژول بزرگ است؛ هر مرحله را اول برای بخش بانک، بعد صندوق و در آخر پرداخت انجام دهید.

> **توجه:** این ماژول به هیچ ماژول دیگری وابسته نیست. پرداخت فقط **شماره‌ی** سفارش (‎`orderId`‎) را نگه می‌دارد
> و فعلاً رابطه‌ای با entity به نام ‎`Order`‎ ندارد.

---

> **تحویل اول (لایه‌ی داده):** مراحل ۱ تا ۴.
> پیش از تحویل، کل پروژه کامپایل شود و deploy روی TomEE بدون خطا انجام شود.

## مرحله ۱ — entityها

**اشکال‌های فعلی (همه‌ی بخش‌ها):**

- مبلغ‌ها (‎`amount`‎ و ‎`balance`‎) از نوع ‎`double`‎ هستند؛ برای پول، ‎`double`‎ دقیق نیست.
- روی ستون‌های عددی و تاریخ ‎`length`‎ گذاشته شده است؛ ‎`length`‎ فقط برای رشته معنا دارد.
- ‎`Bank`‎ و ‎`BankAccount`‎ و ‎`Cashier`‎ لیست‌های ‎`@OneToMany`‎ دارند که لازم نیستند.
- ‎`bankCode`‎ و ‎`accountNumber`‎ و ‎`employeeCode`‎ یکتا نیستند.
- enumها در خود پوشه‌ی ‎`entity`‎ هستند و نام ‎`paymentStatus`‎ با حرف کوچک شروع شده است.
- ‎`Cashier`‎ با ‎`@Entity(name = "CashierEntity")`‎ نام دیگری گرفته است.

**اشکال‌های بخش پرداخت:**

- ‎`CardPayment`‎ و ‎`CashPayment`‎ و ‎`ChequePayment`‎ هر کدام ‎`@SQLDelete`‎ و ‎`@SQLRestriction`‎ خودشان را دارند. در ارث‌بری ‎`JOINED`‎ ستون ‎`deleted`‎ فقط در جدول ‎`payments`‎ است.
- ‎`CardPayment`‎ کد ‎`ccv2Number`‎ را ذخیره می‌کند؛ این کد **هرگز** نباید ذخیره شود. ماه و سال انقضا هم از نوع ‎`LocalDateTime`‎ هستند.
- فیلدهای ‎`count`‎ و ‎`paymentDate`‎ در ‎`CashPayment`‎ معنای روشنی ندارند.
- ‎`ChequePayment`‎ تاریخ چک و وضعیت ندارد، در حالی که DTOهای آن این دو را دارند. نام بانک هم فقط یک رشته است.
- ‎`Payment`‎ وضعیت، نوع و شماره‌ی سفارش ندارد.
- ‎`ChequeStatus`‎ همان کار ‎`paymentStatus`‎ را تکرار می‌کند، و ‎`BANK_TRANSFER`‎ در ‎`PaymentType`‎ هیچ کلاسی ندارد.

**اشکال‌های ‎`Transaction`‎:**

- ‎`transactionTimeStamp`‎ همان کار ‎`createdAt`‎ در ‎`BaseEntity`‎ را تکرار می‌کند.
- طول ‎`description`‎ فقط 20 است.
- ‎`@OneToOne`‎ به ‎`Payment`‎ و مقدار ‎`PAYMENT`‎ در ‎`TransactionType`‎ فعلاً لازم نیستند.

**آنچه باید انجام دهید:**

- همه‌ی مبلغ‌ها را ‎`BigDecimal`‎ با ‎`precision`‎ و ‎`scale`‎ کنید و ‎`length`‎ را از ستون‌های غیررشته‌ای بردارید.
- لیست‌های ‎`@OneToMany`‎ را حذف کنید و ستون‌های کد و شماره را ‎`unique`‎ کنید.
- enumها را به ‎`entity/enums`‎ منتقل کنید؛ ‎`paymentStatus`‎ را ‎`PaymentStatus`‎ بنامید و ‎`ChequeStatus`‎ را حذف کنید.
- ‎`@Entity(name = "CashierEntity")`‎ را به ‎`@Entity`‎ ساده برگردانید.
- ‎`@SQLDelete`‎ و ‎`@SQLRestriction`‎ فقط روی ‎`Payment`‎ بمانند.
- فیلدهای ‎`Payment`‎: ‎`amount`‎ و ‎`status`‎ (از نوع ‎`PaymentStatus`‎) و ‎`type`‎ (از نوع ‎`PaymentType`‎) و ‎`orderId`‎ (از نوع ‎`Long`‎ و اجباری).
- ‎`BANK_TRANSFER`‎ را از ‎`PaymentType`‎ حذف کنید.
- فیلدهای زیرکلاس‌ها:
  - ‎`CashPayment`‎: یک ‎`@ManyToOne`‎ از نوع ‎`LAZY`‎ به ‎`Cashier`‎ (صندوق‌داری که پول را گرفته).
  - ‎`CardPayment`‎: ‎`cardLastDigits`‎ (فقط چهار رقم آخر کارت) و ‎`trackingCode`‎.
  - ‎`ChequePayment`‎: ‎`chequeNumber`‎ و ‎`dueDate`‎ از نوع ‎`LocalDate`‎، و یک ‎`@ManyToOne`‎ از نوع ‎`LAZY`‎ به ‎`Bank`‎.
- در ‎`Transaction`‎: ‎`transactionTimeStamp`‎ و ‎`payment`‎ را حذف کنید، ‎`bankAccount`‎ اجباری و ‎`cashier`‎ اختیاری باشد، و ‎`PAYMENT`‎ را از ‎`TransactionType`‎ بردارید.
- همه‌ی ‎`@ManyToOne`‎ها ‎`LAZY`‎ باشند و ‎`@JoinColumn`‎ داشته باشند.

## مرحله ۲ — repository

**اشکال‌های فعلی:**

- ‎`findByBankAccountName`‎ در ‎`BankAccountRepository`‎ دنبال فیلد ‎`bankName`‎ می‌گردد که در ‎`BankAccount`‎ وجود ندارد.
- نام ‎`findOneByBankName`‎ با بقیه‌ی پروژه یکی نیست.

**متدهایی که باید داشته باشید** (غیر از متدهای آماده‌ی ‎`BaseRepository`‎):

| کلاس | متد | کار |
|---|---|---|
| ‎`BankRepository`‎ | ‎`Optional<Bank> findByCode(String code)`‎ | بانک با این کد (به‌جای ‎`findOneByBankName`‎) |
| ‎`BankAccountRepository`‎ | ‎`Optional<BankAccount> findByAccountNumber(String accountNumber)`‎ | حساب با این شماره (به‌جای ‎`findByBankAccountName`‎) |
| ‎`BankAccountRepository`‎ | ‎`List<BankAccount> findByBank(Long bankId)`‎ | حساب‌های یک بانک |
| ‎`BankAccountRepository`‎ | ‎`long countByBank(Long bankId)`‎ | تعداد حساب‌های یک بانک |
| ‎`CashierRepository`‎ | ‎`Optional<Cashier> findByEmployeeCode(String employeeCode)`‎ | صندوق‌دار با این کد |
| ‎`TransactionRepository`‎ | ‎`List<Transaction> findByBankAccount(Long bankAccountId)`‎ | transactionهای یک حساب، مرتب بر اساس ‎`id`‎ |
| ‎`PaymentRepository`‎ | ‎`List<Payment> findByOrder(Long orderId)`‎ | پرداخت‌های یک سفارش، از هر سه نوع |

**نکته:** جست‌وجو روی یک فیلد ساده را با ‎`findOneBy(...)`‎ و بقیه را با JPQL بنویسید. repositoryهای سه نوع پرداخت متد اضافه لازم ندارند.

## مرحله ۳ — DTOها

**اشکال‌های فعلی:**

- DTOهای پرداخت کلاس‌های Lombok هستند، نه record.
- ‎`BankDto`‎ فیلد ‎`id`‎ ندارد.
- روی ‎`double`‎ از ‎`@NotBlank`‎ استفاده شده و در ‎`CashierRequest`‎ روی یک ‎`String`‎ از ‎`@Valid`‎ استفاده شده است.
- ‎`balance`‎ در ‎`BankAccountRequest`‎ آمده است؛ موجودی فقط با transaction تغییر می‌کند.
- بعضی DTOها فیلدهایی دارند که در entity نیست، مثل ‎`cashPayment`‎ و ‎`chequeStatus`‎.

**فیلدهای هر record:**

| record | فیلدها |
|---|---|
| ‎`BankRequest`‎ | ‎`name`‎، ‎`code`‎ |
| ‎`BankDto`‎ | ‎`id`‎، ‎`name`‎، ‎`code`‎ |
| ‎`BankAccountRequest`‎ | ‎`bankId`‎، ‎`accountNumber`‎، ‎`ownerName`‎ |
| ‎`BankAccountDto`‎ | ‎`id`‎، ‎`bankId`‎، ‎`bankName`‎، ‎`accountNumber`‎، ‎`ownerName`‎، ‎`balance`‎ |
| ‎`CashierRequest`‎ | ‎`name`‎، ‎`employeeCode`‎ |
| ‎`CashierDto`‎ | ‎`id`‎، ‎`name`‎، ‎`employeeCode`‎ |
| ‎`TransactionRequest`‎ | ‎`type`‎، ‎`amount`‎، ‎`description`‎، ‎`cashierId`‎ (اختیاری) |
| ‎`TransactionDto`‎ | ‎`id`‎، ‎`bankAccountId`‎، ‎`type`‎، ‎`amount`‎، ‎`description`‎، ‎`cashierId`‎، ‎`createdAt`‎ |
| ‎`CashPaymentRequest`‎ | ‎`orderId`‎، ‎`amount`‎، ‎`cashierId`‎ |
| ‎`CardPaymentRequest`‎ | ‎`orderId`‎، ‎`amount`‎، ‎`cardLastDigits`‎، ‎`trackingCode`‎ |
| ‎`ChequePaymentRequest`‎ | ‎`orderId`‎، ‎`amount`‎، ‎`bankId`‎، ‎`chequeNumber`‎، ‎`dueDate`‎ |
| ‎`PaymentDto`‎ | ‎`id`‎، ‎`orderId`‎، ‎`type`‎، ‎`amount`‎، ‎`status`‎، ‎`createdAt`‎ |

**نکته‌ها:**

- خروجی هر سه نوع پرداخت همان ‎`PaymentDto`‎ است؛ ‎`CashPaymentDto`‎ و ‎`CardPaymentDto`‎ و ‎`ChequePaymentDto`‎ را حذف کنید.
- برای مبلغ‌ها از ‎`@NotNull`‎ و ‎`@Positive`‎ استفاده کنید و برای ‎`cardLastDigits`‎ از ‎`@Pattern`‎ با چهار رقم.

## مرحله ۴ — mapper

**اشکال‌های فعلی:**

- ‎`ChequePaymentMapper`‎ به‌جای request، خود entity به نام ‎`ChequePayment`‎ را به‌عنوان ورودی گرفته است.
- relationها تبدیل نمی‌شوند.

**آنچه باید انجام دهید:**

- در ‎`BankAccountMapper`‎ خروجی ‎`bankId`‎ و ‎`bankName`‎ پر شوند؛ در ورودی ‎`bank`‎ و ‎`balance`‎ تبدیل نشوند.
- در ‎`TransactionMapper`‎ خروجی ‎`bankAccountId`‎ و ‎`cashierId`‎ پر شوند؛ در ورودی ‎`bankAccount`‎ و ‎`cashier`‎ تبدیل نشوند.
- سه mapper پرداخت (‎`CashPaymentMapper`‎ و ‎`CardPaymentMapper`‎ و ‎`ChequePaymentMapper`‎) هر کدام ‎`PaymentDto`‎ را خروجی می‌دهند؛ در ورودی ‎`status`‎ و ‎`type`‎ و relationها تبدیل نشوند.
- ‎`PaymentMapper`‎ فقط برای خروجی است؛ از ‎`BaseMapper`‎ ارث نمی‌برد و فقط ‎`toDto`‎ و ‎`toDtoList`‎ را دارد.

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
| ‎`DuplicateBankCodeException`‎ | بانک دیگری با همین کد وجود دارد |
| ‎`DuplicateAccountNumberException`‎ | حساب دیگری با همین شماره وجود دارد |
| ‎`DuplicateEmployeeCodeException`‎ | صندوق‌دار دیگری با همین کد وجود دارد |
| ‎`BankNotFoundException`‎ | ‎`bankId`‎ ارسال‌شده وجود ندارد |
| ‎`CashierNotFoundException`‎ | ‎`cashierId`‎ ارسال‌شده وجود ندارد |
| ‎`BankInUseException`‎ | بانکی که هنوز حساب دارد، حذف می‌شود |
| ‎`InsufficientBalanceException`‎ | برداشت بیشتر از موجودی حساب است |
| ‎`PaymentNotEditableException`‎ | پرداختی که ‎`PENDING`‎ نیست ویرایش یا حذف می‌شود |
| ‎`InvalidPaymentStatusException`‎ | چکی که ‎`PENDING`‎ نیست وصول یا برگشت می‌خورد |

همه‌ی این‌ها خودکار به جواب 409 تبدیل می‌شوند.

## مرحله ۶ — service

**اشکال‌های فعلی:**

- نوع‌های ‎`BankAccountService`‎ برعکس است (‎`BaseService<BankAccountDto, BankAccount>`‎) و در نتیجه entity برمی‌گرداند.
- کلاس‌های ‎`Impl`‎ سه نوع پرداخت و ‎`PaymentServiceImpl`‎ روی کلاس ‎`@ApplicationScoped`‎ ندارند؛ پس CDI bean نیستند و inject نمی‌شوند.
- همه‌ی متدها مقدار ‎`null`‎ یا خالی برمی‌گردانند.

**متدهایی که باید بسازید:**

| interface | متد | کار |
|---|---|---|
| ‎`BankService`‎ و ‎`CashierService`‎ | شش متد ‎`BaseService`‎ | CRUD و paging |
| ‎`BankAccountService`‎ | ‎`extends BaseService<BankAccountRequest, BankAccountDto>`‎ | CRUD و paging برای حساب |
| ‎`BankAccountService`‎ | ‎`List<BankAccountDto> findByBank(Long bankId)`‎ | حساب‌های یک بانک |
| ‎`TransactionService`‎ | ‎`List<TransactionDto> findByBankAccount(Long bankAccountId)`‎ | transactionهای یک حساب |
| ‎`TransactionService`‎ | ‎`Optional<TransactionDto> register(Long bankAccountId, TransactionRequest request)`‎ | واریز یا برداشت؛ اگر حساب نبود خالی برمی‌گردد |
| ‎`CashPaymentService`‎ و ‎`CardPaymentService`‎ و ‎`ChequePaymentService`‎ | ‎`extends BaseService<XxxPaymentRequest, PaymentDto>`‎ | ساخت و خواندن هر نوع پرداخت |
| ‎`ChequePaymentService`‎ | ‎`Optional<PaymentDto> clear(Long id)`‎ | وصول چک: وضعیت ‎`COMPLETED`‎ |
| ‎`ChequePaymentService`‎ | ‎`Optional<PaymentDto> bounce(Long id)`‎ | برگشت چک: وضعیت ‎`FAILED`‎ |
| ‎`PaymentService`‎ | ‎`findAll`‎ و ‎`findById`‎ و ‎`findByOrder(Long orderId)`‎ | فقط خواندن همه‌ی پرداخت‌ها |

‎`TransactionService`‎ و ‎`PaymentService`‎ از ‎`BaseService`‎ ارث نمی‌برند: transaction ثبت‌شده عوض نمی‌شود و پرداخت از مسیر نوع خودش ساخته می‌شود.

**متدهای کمکی ‎`private`‎:**

| کلاس | متد کمکی | کار |
|---|---|---|
| ‎`BankServiceImpl`‎ | ‎`checkCodeIsFree(request, currentId)`‎ | کد تکراری: ‎`DuplicateBankCodeException`‎ |
| ‎`BankServiceImpl`‎ | ‎`checkNotInUse(id)`‎ | اگر حساب داشت: ‎`BankInUseException`‎ |
| ‎`BankAccountServiceImpl`‎ | ‎`checkAccountNumberIsFree(request, currentId)`‎ | شماره‌ی تکراری: ‎`DuplicateAccountNumberException`‎ |
| ‎`BankAccountServiceImpl`‎ | ‎`loadBank(bankId)`‎ | اگر نبود: ‎`BankNotFoundException`‎ |
| ‎`CashierServiceImpl`‎ | ‎`checkEmployeeCodeIsFree(request, currentId)`‎ | کد تکراری: ‎`DuplicateEmployeeCodeException`‎ |
| ‎`TransactionServiceImpl`‎ | ‎`loadCashier(cashierId)`‎ | اگر ‎`cashierId`‎ آمده و وجود نداشت: ‎`CashierNotFoundException`‎ |
| ‎`TransactionServiceImpl`‎ | ‎`applyToBalance(account, request)`‎ | ‎`DEPOSIT`‎ اضافه و ‎`WITHDRAW`‎ کم می‌کند؛ اگر موجودی کافی نبود: ‎`InsufficientBalanceException`‎ |
| هر سه کلاس ‎`Impl`‎ پرداخت | ‎`checkEditable(payment)`‎ | اگر ‎`PENDING`‎ نبود: ‎`PaymentNotEditableException`‎ |

**وضعیت پرداخت‌ها:**

| نوع | وضعیت هنگام ساخت | تغییر بعدی |
|---|---|---|
| ‎`CashPayment`‎ و ‎`CardPayment`‎ | ‎`COMPLETED`‎ | ندارد |
| ‎`ChequePayment`‎ | ‎`PENDING`‎ | با ‎`clear`‎ به ‎`COMPLETED`‎ و با ‎`bounce`‎ به ‎`FAILED`‎؛ در غیر ‎`PENDING`‎ خطای ‎`InvalidPaymentStatusException`‎ |

**نکته‌ها:**

- حساب جدید با موجودی صفر ساخته می‌شود. ‎`register`‎ باید ‎`@Transactional`‎ باشد تا ذخیره‌ی transaction و تغییر موجودی با هم انجام شوند.
- مقدار ‎`type`‎ هر پرداخت را service هنگام ساخت پر می‌کند (‎`CASH`‎ یا ‎`CARD`‎ یا ‎`CHEQUE`‎).
- در کلاس‌های دیگر فقط interface را inject کنید، نه کلاس ‎`Impl`‎ را.

## مرحله ۷ — REST API

**اشکال‌های فعلی:**

- بیشتر APIها فقط یک متد ‎`GET`‎ دارند و ‎`BankAccountApi`‎ مقدار ‎`null`‎ برمی‌گرداند.
- ‎`BankAccountApi`‎ و ‎`CardPaymentApi`‎ و ‎`CashPaymentApi`‎ و ‎`ChequePaymentApi`‎ و ‎`PaymentApi`‎ روی کلاس ‎`@RequestScoped`‎ ندارند و چند تا از آن‌ها کلاس ‎`Impl`‎ را inject می‌کنند.
- مسیرها یکدست نیستند: ‎`/bankaccounts`‎ و ‎`/payments/cashs`‎ و ‎`payments/cheques`‎ (بدون ‎`/`‎ اول).

**یادآوری:** در API نه ‎`try/catch`‎ بنویسید و نه ‎`ApiResponse`‎ بسازید؛ فقط DTO را برگردانید و exception را throw کنید (بخش ۶ [راهنمای دانشجو](../student-guide.md)).

**endpointهایی که در پایان باید کار کنند:**

| HTTP | مسیر | جواب |
|---|---|---|
| ‎`GET`‎ | ‎`/banks`‎ | همه‌ی بانک‌ها |
| ‎`GET`‎ | ‎`/banks/page?page=0&size=10`‎ | یک صفحه |
| ‎`GET`‎ | ‎`/banks/{id}`‎ | یک بانک؛ اگر نبود 404 |
| ‎`POST`‎ | ‎`/banks`‎ | 201 همراه بانک جدید |
| ‎`PUT`‎ | ‎`/banks/{id}`‎ | 200 همراه بانک ویرایش‌شده |
| ‎`DELETE`‎ | ‎`/banks/{id}`‎ | 204؛ اگر هنوز حساب داشته باشد 409 |
| ‎`GET`‎ | ‎`/bank-accounts`‎ | همه‌ی حساب‌ها |
| ‎`GET`‎ | ‎`/bank-accounts?bankId=1`‎ | حساب‌های یک بانک |
| ‎`GET`‎ | ‎`/bank-accounts/page?page=0&size=10`‎ | یک صفحه |
| ‎`GET`‎ | ‎`/bank-accounts/{id}`‎ | یک حساب؛ اگر نبود 404 |
| ‎`POST`‎ | ‎`/bank-accounts`‎ | 201 همراه حساب جدید با موجودی صفر |
| ‎`PUT`‎ | ‎`/bank-accounts/{id}`‎ | 200 همراه حساب ویرایش‌شده |
| ‎`DELETE`‎ | ‎`/bank-accounts/{id}`‎ | 204 |
| ‎`GET`‎ | ‎`/bank-accounts/{id}/transactions`‎ | transactionهای آن حساب |
| ‎`POST`‎ | ‎`/bank-accounts/{id}/transactions`‎ | 201 همراه transaction جدید؛ اگر موجودی کافی نباشد 409 |
| ‎`GET`‎ | ‎`/cashiers`‎ | همه‌ی صندوق‌دارها |
| ‎`GET`‎ | ‎`/cashiers/page?page=0&size=10`‎ | یک صفحه |
| ‎`GET`‎ | ‎`/cashiers/{id}`‎ | یک صندوق‌دار؛ اگر نبود 404 |
| ‎`POST`‎ | ‎`/cashiers`‎ | 201 همراه صندوق‌دار جدید |
| ‎`PUT`‎ | ‎`/cashiers/{id}`‎ | 200 همراه صندوق‌دار ویرایش‌شده |
| ‎`DELETE`‎ | ‎`/cashiers/{id}`‎ | 204 |
| ‎`GET`‎ | ‎`/payments`‎ | همه‌ی پرداخت‌ها از هر سه نوع |
| ‎`GET`‎ | ‎`/payments?orderId=1`‎ | پرداخت‌های یک سفارش |
| ‎`GET`‎ | ‎`/payments/{id}`‎ | یک پرداخت؛ اگر نبود 404 |
| ‎`POST`‎ | ‎`/payments/cash`‎ | 201 همراه پرداخت نقدی جدید |
| ‎`POST`‎ | ‎`/payments/card`‎ | 201 همراه پرداخت کارتی جدید |
| ‎`POST`‎ | ‎`/payments/cheque`‎ | 201 همراه چک جدید |
| ‎`PUT`‎ | ‎`/payments/cheque/{id}`‎ | 200 همراه چک ویرایش‌شده؛ اگر ‎`PENDING`‎ نباشد 409 |
| ‎`DELETE`‎ | ‎`/payments/cheque/{id}`‎ | 204؛ اگر ‎`PENDING`‎ نباشد 409 |
| ‎`POST`‎ | ‎`/payments/cheque/{id}/clear`‎ | 200 همراه چک وصول‌شده |
| ‎`POST`‎ | ‎`/payments/cheque/{id}/bounce`‎ | 200 همراه چک برگشتی |

## مرحله ۸ — پاک‌سازی

- ‎`ChequeStatus`‎ و سه کلاس ‎`CashPaymentDto`‎ و ‎`CardPaymentDto`‎ و ‎`ChequePaymentDto`‎ را حذف کنید.
- import و کدهایی را که بعد از این تغییرها استفاده نمی‌شوند پاک کنید.

## مرحله ۹ — چه وقت کار تمام است؟

1. کل پروژه بدون خطا کامپایل می‌شود.
2. پروژه deploy شده و همه‌ی endpointهای جدول مرحله‌ی ۷ روی TomEE یک بار امتحان شده‌اند.
3. این خطاها امتحان شده‌اند: یک خطای validation با جواب 400، یک ‎`id`‎ ناموجود با جواب 404، و هر نُه قاعده‌ی business با جواب 409.
4. بعد از چند واریز و برداشت، موجودی هر حساب با جمع transactionهایش برابر است.
5. در database هیچ ستونی برای کد ‎`CVV2`‎ یا شماره‌ی کامل کارت وجود ندارد.
6. همه‌ی موارد چک‌لیست بخش ۹ فایل ‎`attachments/readme.md`‎ به‌جز تست‌ها انجام شده‌اند.
7. هیچ‌کدام از موارد جدول Common mistakes در بخش ۱۰ فایل ‎`attachments/readme.md`‎ در کد شما باقی نمانده است.

تست‌نویسی بعداً در یک دستور کار جدا اضافه می‌شود.

</div>
