<div dir="rtl">

# دستور کار ماژول organization

هدف: رساندن ماژول organization (‎`Organization`‎ و ‎`Branch`‎) به سطح ماژول ‎`attachments`‎.

**پیش از شروع:** فایل [راهنمای دانشجو](../student-guide.md) را کامل بخوانید. ساختار ماژول، مسیر کامل یک request، کلاس‌هایی که از core ارث می‌برید، ترتیب کار ماژول‌ها و روش تحویل در آنجا آمده است.

- برای هر مرحله، همان مرحله را در فایل ‎`attachments/readme.md`‎ (بخش ۳) بخوانید، با کد خودتان مقایسه کنید و اشکال‌ها را برطرف کنید.
- مراحل را **به همین ترتیب** انجام دهید؛ هر مرحله روی مرحله‌ی قبل بنا می‌شود.
- بعد از هر مرحله پروژه را کامپایل کنید.

> **توجه:** این ماژول برای کارمندان شعبه از entity به نام ‎`User`‎ در ماژول ‎`users`‎ استفاده می‌کند.
> مرحله‌ی ۱ را بعد از مرحله‌ی ۱ ماژول users انجام دهید؛ در آنجا لیست ‎`branches`‎ از ‎`User`‎ حذف می‌شود.

---

> **تحویل اول (لایه‌ی داده):** مراحل ۱ تا ۴.
> پیش از تحویل، کل پروژه کامپایل شود و deploy روی TomEE بدون خطا انجام شود.

## مرحله ۱ — entityها

**اشکال‌های فعلی:**

- ‎`@ManyToOne`‎ از ‎`Branch`‎ به ‎`Organization`‎ به‌طور پیش‌فرض ‎`EAGER`‎ است، ‎`@JoinColumn`‎ ندارد و اجباری نیست.
- ‎`Organization`‎ لیست ‎`branches`‎ و متد ‎`addBranch`‎ دارد که لازم نیستند.
- رابطه‌ی ‎`@ManyToMany`‎ به نام ‎`employees`‎ جدول واسط مشخص (‎`@JoinTable`‎) ندارد.
- ‎`registrationNumber`‎ یکتا نیست.
- طول ‎`branchCode`‎ در entity برابر 30 است ولی در request برابر 50.
- متدهای ‎`updateInfo`‎ همان کار ‎`updateEntity`‎ در mapper را تکرار می‌کنند.
- enum به نام ‎`EmployeeRole`‎ هیچ جا استفاده نشده است.

**آنچه باید انجام دهید:**

- ‎`@ManyToOne`‎ را ‎`LAZY`‎ کنید و با ‎`@JoinColumn`‎ مشخص کنید: نام ستون ‎`organization_id`‎ باشد و اجباری (‎`nullable = false`‎).
- لیست ‎`branches`‎ و متد ‎`addBranch`‎ را حذف کنید؛ شعبه‌های یک organization را با repository پیدا می‌کنید.
- برای ‎`employees`‎ جدول واسط را با ‎`@JoinTable(name = "branch_employees")`‎ مشخص کنید.
- ‎`registrationNumber`‎ را ‎`unique`‎ کنید و طول ‎`branchCode`‎ را در entity و request یکی کنید.
- متدهای ‎`updateInfo`‎ را حذف کنید؛ متدهای ‎`activate`‎ و ‎`deactivate`‎ بمانند.
- ‎`EmployeeRole`‎ را حذف کنید؛ roleها در ماژول users هستند.

## مرحله ۲ — repository

**اشکال فعلی:** repositoryها درست ساخته شده‌اند ولی متدهای لازم برای business ruleها را ندارند.

**متدهایی که باید داشته باشید** (غیر از متدهای آماده‌ی ‎`BaseRepository`‎):

| کلاس | متد | کار |
|---|---|---|
| ‎`OrganizationRepository`‎ | ‎`Optional<Organization> findByRegistrationNumber(String registrationNumber)`‎ | موجود است؛ نگهش دارید |
| ‎`BranchRepository`‎ | ‎`List<Branch> findByOrganizationId(Long organizationId)`‎ | موجود است؛ مرتب‌سازی بر اساس ‎`id`‎ را اضافه کنید |
| ‎`BranchRepository`‎ | ‎`long countByOrganizationId(Long organizationId)`‎ | تعداد شعبه‌های یک organization |
| ‎`BranchRepository`‎ | ‎`Optional<Branch> findByBranchCode(String branchCode)`‎ | شعبه با این کد |

**نکته:** جست‌وجو روی یک فیلد ساده را می‌توانید با ‎`findOneBy(...)`‎ کوتاه‌تر بنویسید.

## مرحله ۳ — DTOها

**اشکال‌های فعلی:**

- ‎`OrganizationRequest`‎ و ‎`OrganizationDto`‎ یک ‎`List<BranchDto>`‎ دارند؛ شعبه‌ها جداگانه ساخته و خوانده می‌شوند.
- ‎`status`‎ در requestها آمده است؛ وضعیت فقط با endpointهای activate و deactivate عوض می‌شود.
- ‎`organizationId`‎ در ‎`BranchRequest`‎ هیچ validation ندارد و ‎`email`‎ با ‎`@Email`‎ بررسی نمی‌شود.
- ‎`website`‎ اجباری است، در حالی که در entity اختیاری است.

**فیلدهای هر record:**

| record | فیلدها |
|---|---|
| ‎`OrganizationRequest`‎ | ‎`name`‎، ‎`registrationNumber`‎، ‎`email`‎، ‎`website`‎، ‎`establishmentDate`‎ |
| ‎`OrganizationDto`‎ | ‎`id`‎، ‎`name`‎، ‎`registrationNumber`‎، ‎`email`‎، ‎`website`‎، ‎`establishmentDate`‎، ‎`status`‎ |
| ‎`BranchRequest`‎ | ‎`organizationId`‎، ‎`name`‎، ‎`branchCode`‎، ‎`phoneNumber`‎، ‎`email`‎، ‎`website`‎، ‎`openingDate`‎، ‎`address`‎ |
| ‎`BranchDto`‎ | ‎`id`‎، ‎`organizationId`‎، ‎`organizationName`‎، ‎`name`‎، ‎`branchCode`‎، ‎`phoneNumber`‎، ‎`email`‎، ‎`website`‎، ‎`openingDate`‎، ‎`status`‎، ‎`address`‎ |
| ‎`EmployeeDto`‎ (جدید) | ‎`id`‎، ‎`username`‎ |

## مرحله ۴ — mapper

**اشکال فعلی:** relationها تبدیل نمی‌شوند.

**آنچه باید انجام دهید:**

- در ‎`BranchMapper`‎ خروجی ‎`organizationId`‎ و ‎`organizationName`‎ از organization شعبه پر شوند.
- در ورودی ‎`organization`‎ و ‎`employees`‎ و ‎`status`‎ تبدیل **نشوند** (‎`ignore`‎)؛ service آن‌ها را پر می‌کند.
- در ‎`OrganizationMapper`‎ هم ‎`status`‎ در ورودی تبدیل نشود.
- در ‎`BranchMapper`‎ یک متد برای تبدیل ‎`User`‎ به ‎`EmployeeDto`‎ اضافه کنید.

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
| ‎`DuplicateRegistrationNumberException`‎ | organization دیگری با همین شماره‌ی ثبت وجود دارد |
| ‎`DuplicateBranchCodeException`‎ | شعبه‌ی دیگری با همین کد وجود دارد |
| ‎`OrganizationNotFoundException`‎ | ‎`organizationId`‎ ارسال‌شده وجود ندارد |
| ‎`InactiveOrganizationException`‎ | برای organization‌ای که ‎`ACTIVE`‎ نیست شعبه ساخته می‌شود |
| ‎`OrganizationHasBranchesException`‎ | organization‌ای که هنوز شعبه دارد، حذف می‌شود |
| ‎`EmployeeNotFoundException`‎ | user‌ای که باید به شعبه اضافه شود وجود ندارد |

همه‌ی این‌ها خودکار به جواب 409 تبدیل می‌شوند.

## مرحله ۶ — service

**اشکال‌های فعلی:**

- همه‌ی متدهای هر دو کلاس ‎`Impl`‎ مقدار ‎`null`‎ یا خالی برمی‌گردانند.
- هیچ متدی ‎`@Transactional`‎ ندارد.

**متدهایی که باید به interface اضافه کنید** (غیر از شش متد ‎`BaseService`‎):

| interface | متد | کار |
|---|---|---|
| ‎`OrganizationService`‎ | ‎`Optional<OrganizationDto> activate(Long id)`‎ | وضعیت را ‎`ACTIVE`‎ می‌کند |
| ‎`OrganizationService`‎ | ‎`Optional<OrganizationDto> deactivate(Long id)`‎ | وضعیت را ‎`INACTIVE`‎ می‌کند |
| ‎`BranchService`‎ | ‎`List<BranchDto> findByOrganization(Long organizationId)`‎ | شعبه‌های یک organization |
| ‎`BranchService`‎ | ‎`Optional<BranchDto> activate(Long id)`‎ | وضعیت شعبه را ‎`ACTIVE`‎ می‌کند |
| ‎`BranchService`‎ | ‎`Optional<BranchDto> deactivate(Long id)`‎ | وضعیت شعبه را ‎`INACTIVE`‎ می‌کند |
| ‎`BranchService`‎ | ‎`Optional<List<EmployeeDto>> findEmployees(Long branchId)`‎ | کارمندان یک شعبه |
| ‎`BranchService`‎ | ‎`Optional<BranchDto> addEmployee(Long branchId, Long userId)`‎ | اضافه کردن کارمند (اگر از قبل بود، تکرار نشود) |
| ‎`BranchService`‎ | ‎`Optional<BranchDto> removeEmployee(Long branchId, Long userId)`‎ | حذف کارمند از شعبه |

**متدهای کمکی ‎`private`‎:**

| کلاس | متد کمکی | کار |
|---|---|---|
| ‎`OrganizationServiceImpl`‎ | ‎`checkRegistrationNumberIsFree(request, currentId)`‎ | شماره‌ی تکراری: ‎`DuplicateRegistrationNumberException`‎ |
| ‎`OrganizationServiceImpl`‎ | ‎`checkHasNoBranches(id)`‎ | اگر شعبه داشت: ‎`OrganizationHasBranchesException`‎ |
| ‎`BranchServiceImpl`‎ | ‎`checkBranchCodeIsFree(request, currentId)`‎ | کد تکراری: ‎`DuplicateBranchCodeException`‎ |
| ‎`BranchServiceImpl`‎ | ‎`loadActiveOrganization(organizationId)`‎ | اگر نبود: ‎`OrganizationNotFoundException`‎؛ اگر ‎`ACTIVE`‎ نبود: ‎`InactiveOrganizationException`‎ |
| ‎`BranchServiceImpl`‎ | ‎`loadEmployee(userId)`‎ | user را از ‎`UserRepository`‎ بخواند؛ اگر نبود: ‎`EmployeeNotFoundException`‎ |

**کجا صدا زده شوند:**

| متد service | متدهای کمکی |
|---|---|
| ‎`create`‎ و ‎`update`‎ در ‎`OrganizationServiceImpl`‎ | ‎`checkRegistrationNumberIsFree`‎ |
| ‎`deleteById`‎ در ‎`OrganizationServiceImpl`‎ | ‎`checkHasNoBranches`‎ پیش از حذف |
| ‎`create`‎ و ‎`update`‎ در ‎`BranchServiceImpl`‎ | ‎`checkBranchCodeIsFree`‎ و ‎`loadActiveOrganization`‎ |
| ‎`addEmployee`‎ در ‎`BranchServiceImpl`‎ | ‎`loadEmployee`‎ |

**نکته‌ها:**

- ‎`UserRepository`‎ از ماژول users را در ‎`BranchServiceImpl`‎ با ‎`@Inject`‎ بگیرید؛ entity به نام ‎`User`‎ را تغییر ندهید.
- ‎`activate`‎ و ‎`deactivate`‎ از متدهای خود entity استفاده کنند.
- ‎`employees`‎ به‌صورت lazy بارگذاری می‌شود؛ ‎`findEmployees`‎ هم باید ‎`@Transactional`‎ باشد.

## مرحله ۷ — REST API

**اشکال‌های فعلی:**

- ‎`OrganizationResource`‎ و ‎`BranchResource`‎ روی کلاس ‎`@RequestScoped`‎ ندارند و کلاس ‎`Impl`‎ را inject می‌کنند.
- بیشتر متدها ‎`null`‎ برمی‌گردانند و بقیه کامنت شده‌اند.
- متدهای ‎`create`‎ و ‎`update`‎ به‌جای request، یک ‎`OrganizationDto`‎ می‌گیرند و ‎`@Valid`‎ ندارند.
- نام کلاس‌ها با بقیه‌ی پروژه یکی نیست؛ آن‌ها را ‎`OrganizationApi`‎ و ‎`BranchApi`‎ بنامید.

**یادآوری:** در API نه ‎`try/catch`‎ بنویسید و نه ‎`ApiResponse`‎ بسازید؛ فقط DTO را برگردانید و exception را throw کنید (بخش ۶ [راهنمای دانشجو](../student-guide.md)).

**endpointهایی که در پایان باید کار کنند:**

| HTTP | مسیر | جواب |
|---|---|---|
| ‎`GET`‎ | ‎`/organizations`‎ | همه‌ی organizationها |
| ‎`GET`‎ | ‎`/organizations/page?page=0&size=10`‎ | یک صفحه |
| ‎`GET`‎ | ‎`/organizations/{id}`‎ | یک organization؛ اگر نبود 404 |
| ‎`GET`‎ | ‎`/organizations/{id}/branches`‎ | شعبه‌های آن |
| ‎`POST`‎ | ‎`/organizations`‎ | 201 همراه organization جدید |
| ‎`PUT`‎ | ‎`/organizations/{id}`‎ | 200 همراه organization ویرایش‌شده |
| ‎`POST`‎ | ‎`/organizations/{id}/activate`‎ | 200 همراه organization فعال‌شده |
| ‎`POST`‎ | ‎`/organizations/{id}/deactivate`‎ | 200 همراه organization غیرفعال‌شده |
| ‎`DELETE`‎ | ‎`/organizations/{id}`‎ | 204؛ اگر هنوز شعبه داشته باشد 409 |
| ‎`GET`‎ | ‎`/branches`‎ | همه‌ی شعبه‌ها |
| ‎`GET`‎ | ‎`/branches?organizationId=1`‎ | شعبه‌های یک organization |
| ‎`GET`‎ | ‎`/branches/page?page=0&size=10`‎ | یک صفحه |
| ‎`GET`‎ | ‎`/branches/{id}`‎ | یک شعبه؛ اگر نبود 404 |
| ‎`POST`‎ | ‎`/branches`‎ | 201 همراه شعبه‌ی جدید |
| ‎`PUT`‎ | ‎`/branches/{id}`‎ | 200 همراه شعبه‌ی ویرایش‌شده |
| ‎`POST`‎ | ‎`/branches/{id}/activate`‎ | 200 همراه شعبه‌ی فعال‌شده |
| ‎`POST`‎ | ‎`/branches/{id}/deactivate`‎ | 200 همراه شعبه‌ی غیرفعال‌شده |
| ‎`DELETE`‎ | ‎`/branches/{id}`‎ | 204 |
| ‎`GET`‎ | ‎`/branches/{id}/employees`‎ | کارمندان شعبه |
| ‎`PUT`‎ | ‎`/branches/{id}/employees/{userId}`‎ | 200 همراه شعبه |
| ‎`DELETE`‎ | ‎`/branches/{id}/employees/{userId}`‎ | 200 همراه شعبه |

## مرحله ۸ — پاک‌سازی

- کدهای کامنت‌شده در کلاس‌های API را پاک کنید.

## مرحله ۹ — چه وقت کار تمام است؟

1. کل پروژه بدون خطا کامپایل می‌شود.
2. پروژه deploy شده و همه‌ی endpointهای جدول مرحله‌ی ۷ روی TomEE یک بار امتحان شده‌اند.
3. این خطاها امتحان شده‌اند: یک خطای validation با جواب 400، یک ‎`id`‎ ناموجود با جواب 404، و هر شش قاعده‌ی business با جواب 409.
4. همه‌ی موارد چک‌لیست بخش ۹ فایل ‎`attachments/readme.md`‎ به‌جز تست‌ها انجام شده‌اند.
5. هیچ‌کدام از موارد جدول Common mistakes در بخش ۱۰ فایل ‎`attachments/readme.md`‎ در کد شما باقی نمانده است.

تست‌نویسی بعداً در یک دستور کار جدا اضافه می‌شود.

</div>
