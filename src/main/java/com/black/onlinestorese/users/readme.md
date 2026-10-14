<div dir="rtl">

# دستور کار ماژول users

هدف: رساندن ماژول users (‎`User`‎ و ‎`Role`‎ و ‎`Profile`‎) به سطح ماژول ‎`attachments`‎.

**پیش از شروع:** فایل [راهنمای دانشجو](../student-guide.md) را کامل بخوانید. ساختار ماژول، مسیر کامل یک request، کلاس‌هایی که از core ارث می‌برید، ترتیب کار ماژول‌ها و روش تحویل در آنجا آمده است.

- برای هر مرحله، همان مرحله را در فایل ‎`attachments/readme.md`‎ (بخش ۳) بخوانید، با کد خودتان مقایسه کنید و اشکال‌ها را برطرف کنید.
- مراحل را **به همین ترتیب** انجام دهید؛ هر مرحله روی مرحله‌ی قبل بنا می‌شود.
- بعد از هر مرحله پروژه را کامپایل کنید.

> **توجه:** ماژول‌های ‎`organization`‎ و ‎`tickets`‎ و ‎`orders`‎ از entity به نام ‎`User`‎ استفاده می‌کنند.
> پس **مرحله‌ی ۱ این ماژول باید زودتر از بقیه تحویل داده شود**. اگر نام فیلد یا کلاسی را عوض کردید،
> کل پروژه را build کنید و به آن گروه‌ها خبر دهید.

---

> **تحویل اول (لایه‌ی داده):** مراحل ۱ تا ۴.
> پیش از تحویل، کل پروژه کامپایل شود و deploy روی TomEE بدون خطا انجام شود.

## مرحله ۱ — entityها

**اشکال‌های فعلی:**

- ‎`User`‎ لیست ‎`branches`‎ دارد (‎`@ManyToMany(mappedBy = "employees")`‎)؛ همین باعث شده users و organization به هم وابسته باشند.
- ‎`User`‎ فیلد ‎`enabled`‎ ندارد، در حالی که ‎`UserDto`‎ آن را دارد.
- رابطه‌ی ‎`@ManyToMany`‎ به ‎`Role`‎ جدول واسط مشخص (‎`@JoinTable`‎) ندارد.
- ‎`username`‎ در ‎`User`‎ و ‎`roleName`‎ در ‎`Role`‎ یکتا (‎`unique`‎) نیستند.
- در ‎`Profile`‎ رابطه‌ی ‎`@OneToOne`‎ به ‎`User`‎ ستون مشخص ندارد و یکتا نیست.
- import مربوط به ‎`@SuperBuilder`‎ در ‎`Profile`‎ و ‎`Role`‎ با بقیه‌ی پروژه یکی نیست (در ‎`Attachment`‎ از ‎`lombok.experimental.SuperBuilder`‎ استفاده شده است).

**آنچه باید انجام دهید:**

- لیست ‎`branches`‎ را از ‎`User`‎ حذف کنید؛ رابطه‌ی کارمندان شعبه فقط در ماژول organization (‎`Branch.employees`‎) می‌ماند.
- فیلد ‎`boolean enabled`‎ را با مقدار پیش‌فرض ‎`true`‎ به ‎`User`‎ اضافه کنید.
- برای رابطه‌ی ‎`roles`‎ جدول واسط را با ‎`@JoinTable(name = "user_roles")`‎ مشخص کنید.
- ‎`username`‎ و نام role را ‎`unique`‎ کنید. نام فیلد ‎`roleName`‎ را به ‎`name`‎ تغییر دهید.
- رابطه‌ی ‎`Profile.user`‎ را با ‎`@JoinColumn`‎ مشخص کنید: نام ستون ‎`user_id`‎ باشد و هم ‎`unique`‎ و هم اجباری (‎`nullable = false`‎). هر user حداکثر یک profile دارد.
- ستون ‎`password`‎ هرگز متن ساده را نگه نمی‌دارد؛ hash کردن آن در مرحله‌ی ۶ است.

## مرحله ۲ — repository

**اشکال‌های فعلی:**

- متد ‎`findByFileName`‎ در ‎`UserRepository`‎ دنبال فیلد ‎`fileName`‎ می‌گردد (کپی از ‎`attachments`‎) که در ‎`User`‎ وجود ندارد.
- برای ‎`Role`‎ و ‎`Profile`‎ هیچ repository وجود ندارد.

**متدهایی که باید داشته باشید** (غیر از متدهای آماده‌ی ‎`BaseRepository`‎):

| کلاس | متد | کار |
|---|---|---|
| ‎`UserRepository`‎ | ‎`Optional<User> findByUsername(String username)`‎ | user با این username (به‌جای ‎`findByFileName`‎) |
| ‎`UserRepository`‎ | ‎`long countByRole(Long roleId)`‎ | تعداد userهایی که این role را دارند |
| ‎`RoleRepository`‎ (جدید) | ‎`Optional<Role> findByName(String name)`‎ | role با این نام |
| ‎`ProfileRepository`‎ (جدید) | ‎`Optional<Profile> findByUser(Long userId)`‎ | profile یک user |

**نکته‌ها:**

- جست‌وجو روی یک فیلد ساده را با ‎`findOneBy(...)`‎ بسازید.
- برای ‎`countByRole`‎ در JPQL از ‎`join`‎ روی ‎`u.roles`‎ استفاده کنید؛ نمونه‌ی JPQL را در ‎`findByOwner`‎ از ‎`AttachmentRepository`‎ ببینید.

## مرحله ۳ — DTOها

**اشکال‌های فعلی:**

- ‎`UserRequest`‎ فیلد ‎`enabled`‎ و roleها را ندارد.
- ‎`UserDto`‎ roleها را نشان نمی‌دهد.
- برای ‎`Role`‎ و ‎`Profile`‎ هیچ DTO وجود ندارد.

**فیلدهای هر record:**

| record | فیلدها |
|---|---|
| ‎`UserRequest`‎ | ‎`username`‎، ‎`password`‎، ‎`enabled`‎، ‎`roleIds`‎ |
| ‎`UserDto`‎ | ‎`id`‎، ‎`username`‎، ‎`enabled`‎، ‎`roles`‎ (نام roleها) |
| ‎`RoleRequest`‎ | ‎`name`‎ |
| ‎`RoleDto`‎ | ‎`id`‎، ‎`name`‎ |
| ‎`ProfileRequest`‎ | ‎`firstName`‎، ‎`lastName`‎، ‎`phoneNumber`‎، ‎`birthDate`‎، ‎`address`‎ |
| ‎`ProfileDto`‎ | ‎`id`‎، ‎`userId`‎، ‎`firstName`‎، ‎`lastName`‎، ‎`phoneNumber`‎، ‎`birthDate`‎، ‎`address`‎ |

**نکته‌ها:**

- ‎`password`‎ در **هیچ** dto خروجی نمی‌آید.
- روی ‎`password`‎ در ‎`UserRequest`‎ حداکثر طول را ۷۲ بگذارید؛ BCrypt بیشتر از ۷۲ بایت اول رمز را در نظر نمی‌گیرد.
- ‎`roleIds`‎ از نوع ‎`Set<Long>`‎ و اختیاری است.
- برای ‎`birthDate`‎ از ‎`@Past`‎ استفاده کنید.

## مرحله ۴ — mapper

**اشکال فعلی:** فقط ‎`UserMapper`‎ وجود دارد و roleها را تبدیل نمی‌کند.

**آنچه باید انجام دهید:**

- در ‎`UserMapper`‎، در ورودی ‎`password`‎ و ‎`roles`‎ تبدیل **نشوند** (‎`ignore`‎)؛ service آن‌ها را پر می‌کند.
- در خروجی، ‎`roles`‎ باید مجموعه‌ی نام roleها باشد. یک متد ‎`default`‎ در mapper بنویسید که یک ‎`Role`‎ را به نامش تبدیل کند؛ MapStruct خودش آن را برای کل مجموعه به کار می‌برد.
- ‎`RoleMapper`‎ و ‎`ProfileMapper`‎ را بسازید. در ‎`ProfileMapper`‎ خروجی ‎`userId`‎ از ‎`user`‎ پر شود و در ورودی ‎`user`‎ تبدیل نشود.

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
| ‎`DuplicateUsernameException`‎ | user دیگری با همین username وجود دارد |
| ‎`DuplicateRoleNameException`‎ | role دیگری با همین نام وجود دارد |
| ‎`RoleNotFoundException`‎ | یکی از ‎`roleIds`‎ ارسال‌شده وجود ندارد |
| ‎`RoleInUseException`‎ | role‌ای که هنوز به user داده شده، حذف می‌شود |

همه‌ی این‌ها خودکار به جواب 409 تبدیل می‌شوند.

## مرحله ۶ — service

**اشکال‌های فعلی:**

- همه‌ی متدهای ‎`UserServiceImpl`‎ مقدار ‎`null`‎ یا خالی برمی‌گردانند.
- برای ‎`Role`‎ هیچ service وجود ندارد و profile هیچ متدی ندارد.

**متدهایی که باید بسازید:**

| interface | متد | کار |
|---|---|---|
| ‎`UserService`‎ | شش متد ‎`BaseService`‎ | CRUD و paging برای user |
| ‎`UserService`‎ | ‎`Optional<ProfileDto> findProfile(Long userId)`‎ | profile یک user |
| ‎`UserService`‎ | ‎`Optional<ProfileDto> saveProfile(Long userId, ProfileRequest request)`‎ | ساخت یا ویرایش profile؛ اگر user نبود خالی برمی‌گردد |
| ‎`RoleService`‎ (جدید) | ‎`extends BaseService<RoleRequest, RoleDto>`‎ | CRUD و paging برای role |

**متدهای کمکی ‎`private`‎:**

| کلاس | متد کمکی | کار |
|---|---|---|
| ‎`UserServiceImpl`‎ | ‎`checkUsernameIsFree(request, currentId)`‎ | username تکراری: ‎`DuplicateUsernameException`‎ |
| ‎`UserServiceImpl`‎ | ‎`loadRoles(roleIds)`‎ | roleها را بخواند؛ اگر یکی نبود: ‎`RoleNotFoundException`‎ |
| ‎`UserServiceImpl`‎ | ‎`hashPassword(password)`‎ | hash رمز عبور با کلاس ‎`BCryptPasswordHash`‎ از core |
| ‎`RoleServiceImpl`‎ | ‎`checkNameIsFree(request, currentId)`‎ | نام تکراری: ‎`DuplicateRoleNameException`‎ |
| ‎`RoleServiceImpl`‎ | ‎`checkNotInUse(id)`‎ | اگر user‌ای این role را داشت: ‎`RoleInUseException`‎ |

**کجا صدا زده شوند:**

| متد service | متدهای کمکی |
|---|---|
| ‎`create`‎ و ‎`update`‎ در ‎`UserServiceImpl`‎ | ‎`checkUsernameIsFree`‎ و ‎`loadRoles`‎ و ‎`hashPassword`‎ |
| ‎`create`‎ و ‎`update`‎ در ‎`RoleServiceImpl`‎ | ‎`checkNameIsFree`‎ |
| ‎`deleteById`‎ در ‎`RoleServiceImpl`‎ | ‎`checkNotInUse`‎ پیش از حذف |

**نکته‌ها:**

- برای hash از کلاس ‎`BCryptPasswordHash`‎ در core استفاده کنید: آن را با ‎`@Inject`‎ بگیرید و متد ‎`generate`‎ را صدا بزنید. کتابخانه‌ی جدیدی لازم نیست. نمونه‌ی کد در بخش ۷ فایل ‎`core/security/readme.md`‎ آمده است.
- ‎`ProfileRepository`‎ و ‎`RoleRepository`‎ را در ‎`UserServiceImpl`‎، و ‎`UserRepository`‎ را در ‎`RoleServiceImpl`‎ با ‎`@Inject`‎ بگیرید.
- roleهای user به‌صورت lazy بارگذاری می‌شوند. متدهای خواندنی که ‎`UserDto`‎ می‌سازند هم باید ‎`@Transactional`‎ باشند، وگرنه ‎`LazyInitializationException`‎ می‌گیرید (بخش ۷ فایل ‎`core/readme.md`‎).

## مرحله ۷ — REST API

**اشکال‌های فعلی:**

- همه‌ی متدها خودشان ‎`Response`‎ می‌سازند؛ کافی است DTO را برگردانید.
- ‎`getUser`‎ یک ‎`Optional`‎ را برمی‌گرداند؛ برای ‎`id`‎ ناموجود باید ‎`NotFoundException`‎ بدهد.
- روی بدنه‌ی request هیچ ‎`@Valid`‎ وجود ندارد.
- ‎`updateUser`‎ جواب 204 همراه بدنه می‌دهد و ‎`deleteUser`‎ هم بدنه برمی‌گرداند.
- مسیر paging وجود ندارد.

**یادآوری:** در API نه ‎`try/catch`‎ بنویسید و نه ‎`ApiResponse`‎ بسازید؛ فقط DTO را برگردانید و exception را throw کنید (بخش ۶ [راهنمای دانشجو](../student-guide.md)).

**endpointهایی که در پایان باید کار کنند:**

| HTTP | مسیر | جواب |
|---|---|---|
| ‎`GET`‎ | ‎`/users`‎ | همه‌ی userها |
| ‎`GET`‎ | ‎`/users/page?page=0&size=10`‎ | یک صفحه |
| ‎`GET`‎ | ‎`/users/{id}`‎ | یک user؛ اگر نبود 404 |
| ‎`POST`‎ | ‎`/users`‎ | 201 همراه user جدید |
| ‎`PUT`‎ | ‎`/users/{id}`‎ | 200 همراه user ویرایش‌شده |
| ‎`DELETE`‎ | ‎`/users/{id}`‎ | 204 |
| ‎`GET`‎ | ‎`/users/{id}/profile`‎ | profile آن user؛ اگر نبود 404 |
| ‎`PUT`‎ | ‎`/users/{id}/profile`‎ | 200 همراه profile ساخته‌شده یا ویرایش‌شده |
| ‎`GET`‎ | ‎`/roles`‎ | همه‌ی roleها |
| ‎`GET`‎ | ‎`/roles/{id}`‎ | یک role؛ اگر نبود 404 |
| ‎`POST`‎ | ‎`/roles`‎ | 201 همراه role جدید |
| ‎`PUT`‎ | ‎`/roles/{id}`‎ | 200 همراه role ویرایش‌شده |
| ‎`DELETE`‎ | ‎`/roles/{id}`‎ | 204؛ اگر هنوز استفاده شده باشد 409 |

## مرحله ۸ — پاک‌سازی

- کلاس ‎`UserServlet`‎ خالی است؛ یا کاملش کنید یا حذفش کنید.

## مرحله ۹ — چه وقت کار تمام است؟

1. کل پروژه بدون خطا کامپایل می‌شود.
2. پروژه deploy شده و همه‌ی endpointهای جدول مرحله‌ی ۷ روی TomEE یک بار امتحان شده‌اند.
3. این خطاها امتحان شده‌اند: یک خطای validation با جواب 400، یک ‎`id`‎ ناموجود با جواب 404، و هر چهار قاعده‌ی business با جواب 409.
4. در هیچ جوابی ‎`password`‎ دیده نمی‌شود و در database هم به‌صورت hash ذخیره شده است. user ساخته‌شده با همان رمز می‌تواند login کند (بخش ۱۰ فایل ‎`core/security/readme.md`‎).
5. همه‌ی موارد چک‌لیست بخش ۹ فایل ‎`attachments/readme.md`‎ به‌جز تست‌ها انجام شده‌اند.
6. هیچ‌کدام از موارد جدول Common mistakes در بخش ۱۰ فایل ‎`attachments/readme.md`‎ در کد شما باقی نمانده است.

تست‌نویسی بعداً در یک دستور کار جدا اضافه می‌شود.

</div>
