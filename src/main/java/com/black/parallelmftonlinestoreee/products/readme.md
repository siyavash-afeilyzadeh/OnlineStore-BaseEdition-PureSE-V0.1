<div dir="rtl">

# دستور کار ماژول products

هدف: رساندن ماژول products (‎`Product`‎ و ‎`ProductCategory`‎) به سطح ماژول ‎`attachments`‎.

**پیش از شروع:** فایل [راهنمای دانشجو](../student-guide.md) را کامل بخوانید. ساختار ماژول، مسیر کامل یک request، کلاس‌هایی که از core ارث می‌برید، ترتیب کار ماژول‌ها و روش تحویل در آنجا آمده است.

- برای هر مرحله، همان مرحله را در فایل ‎`attachments/readme.md`‎ (بخش ۳) بخوانید، با کد خودتان مقایسه کنید و اشکال‌ها را برطرف کنید.
- مراحل را **به همین ترتیب** انجام دهید؛ هر مرحله روی مرحله‌ی قبل بنا می‌شود.
- بعد از هر مرحله پروژه را کامپایل کنید.

> **توجه:** ماژول‌های ‎`orders`‎ و ‎`inventories`‎ از entity به نام ‎`Product`‎ استفاده می‌کنند.
> پس **مرحله‌ی ۱ این ماژول باید زودتر از بقیه تحویل داده شود**. اگر نام فیلد یا کلاسی را عوض کردید،
> کل پروژه را build کنید و به آن گروه‌ها خبر دهید.

---

> **تحویل اول (لایه‌ی داده):** مراحل ۱ تا ۴.
> پیش از تحویل، کل پروژه کامپایل شود و deploy روی TomEE بدون خطا انجام شود.

## مرحله ۱ — entityها

**اشکال‌های فعلی:**

- فیلد ‎`price`‎ از نوع ‎`BigDecimal`‎ است ولی ‎`@NotBlank`‎، ‎`@Size`‎ و ‎`length`‎ دارد؛ با این وضع هر ذخیره‌ای شکست می‌خورد.
- annotationهای validation (‎`@NotBlank`‎، ‎`@Size`‎ و ...) روی entity هستند؛ جای آن‌ها در request است.
- برای فیلد ‎`Boolean isActive`‎ کتابخانه‌ی Lombok متد ‎`getIsActive()`‎ را می‌سازد؛ این فیلد مقدار ‎`null`‎ هم می‌پذیرد.
- ‎`@ManyToOne`‎ به ‎`ProductCategory`‎ به‌طور پیش‌فرض ‎`EAGER`‎ است و ‎`@JoinColumn`‎ ندارد.
- ‎`ProductCategory`‎ دو لیست ‎`@OneToMany`‎ دارد (‎`products`‎ و ‎`subCategories`‎) که لازم نیستند.
- ‎`@AllArgsConstructor`‎ کنار ‎`@SuperBuilder`‎ لازم نیست.
- نام ستون‌ها یکدست نیست (‎`product_name`‎ و ‎`product_price`‎ در یک کلاس، ‎`title`‎ و ‎`description`‎ در کلاس دیگر).
- importها ‎`*`‎ دارند و ‎`Table`‎ تکراری است.

**آنچه باید انجام دهید:**

- برای ستون ‎`price`‎، ‎`precision`‎ و ‎`scale`‎ تعیین کنید.
- در entity فقط قواعد ‎`@Column`‎ بماند (مثل ‎`Attachment`‎).
- ‎`@ManyToOne`‎ را ‎`LAZY`‎ کنید و نام ستون را با ‎`@JoinColumn`‎ مشخص کنید. هر ‎`Product`‎ **باید** category داشته باشد.
- دو لیست ‎`@OneToMany`‎ را حذف کنید؛ جست‌وجوهای لازم را در مرحله‌ی ۲ می‌سازید.
- relation مربوط به parent category (‎`parentCategory`‎) را نگه دارید؛ category بدون parent، یک category اصلی است.

## مرحله ۲ — repository

**اشکال‌های فعلی:**

- متد ‎`findByName`‎ در ‎`ProductCategoryRepository`‎ دنبال فیلد ‎`name`‎ می‌گردد که در ‎`ProductCategory`‎ وجود ندارد.
- constructor‌ای که ‎`EntityManager`‎ می‌گیرد، آن را به ‎`super`‎ نمی‌دهد.

**متدهایی که باید داشته باشید** (غیر از متدهای آماده‌ی ‎`BaseRepository`‎):

| کلاس | متد | کار |
|---|---|---|
| ‎`ProductRepository`‎ | ‎`Optional<Product> findByName(String name)`‎ | product با این نام (موجود است؛ نگهش دارید) |
| ‎`ProductRepository`‎ | ‎`List<Product> findByCategory(Long categoryId)`‎ | productهای یک category، مرتب بر اساس ‎`id`‎ |
| ‎`ProductRepository`‎ | ‎`long countByCategory(Long categoryId)`‎ | تعداد productهای یک category |
| ‎`ProductCategoryRepository`‎ | ‎`Optional<ProductCategory> findByTitle(String title)`‎ | category با این عنوان (به‌جای ‎`findByName`‎) |
| ‎`ProductCategoryRepository`‎ | ‎`List<ProductCategory> findSubCategories(Long parentId)`‎ | sub-categoryهای یک category |
| ‎`ProductCategoryRepository`‎ | ‎`long countSubCategories(Long parentId)`‎ | تعداد sub-categoryها |

**نکته‌ها:**

- جست‌وجو روی یک فیلد ساده را با ‎`findOneBy(...)`‎ بسازید.
- بقیه را با JPQL و ‎`entityManager.createQuery(...)`‎ بنویسید؛ نمونه‌اش ‎`findByOwner`‎ در ‎`AttachmentRepository`‎ است.
- ردیف‌های soft delete شده خودکار کنار گذاشته می‌شوند؛ لازم نیست شرط ‎`deleted`‎ را بنویسید.

## مرحله ۳ — DTOها

**اشکال‌های فعلی:**

- ‎`ProductCategoryDto`‎ فیلد ‎`id`‎ ندارد؛ کلاینت نمی‌تواند category را ویرایش یا حذف کند یا به آن ارجاع دهد.
- فیلد ‎`price`‎ در ‎`ProductRequest`‎ همان محدودیت‌های اشتباه entity را دارد.
- ‎`ProductRequest`‎ یک ‎`ProductCategoryDto`‎ کامل دارد؛ request باید فقط **‎`id`‎** شیء مرتبط را بفرستد.
- نام اجزای record با حرف بزرگ شروع شده است (‎`ProductCategoryDto ProductCategoryDto`‎).
- در ‎`ProductCategoryRequest`‎ فیلد ‎`description`‎ اجباری است ولی در entity اختیاری.

**فیلدهای هر record:**

| record | فیلدها |
|---|---|
| ‎`ProductRequest`‎ | ‎`name`‎، ‎`price`‎، ‎`description`‎، ‎`active`‎، ‎`categoryId`‎ |
| ‎`ProductDto`‎ | ‎`id`‎، ‎`name`‎، ‎`price`‎، ‎`description`‎، ‎`active`‎، ‎`categoryId`‎، ‎`categoryTitle`‎ |
| ‎`ProductCategoryRequest`‎ | ‎`title`‎، ‎`description`‎، ‎`active`‎، ‎`parentId`‎ (اختیاری) |
| ‎`ProductCategoryDto`‎ | ‎`id`‎، ‎`title`‎، ‎`description`‎، ‎`active`‎، ‎`parentId`‎ |

**نکته:** برای هر فیلد در request یک validation مناسب بگذارید؛ برای رشته ‎`@NotBlank`‎ و ‎`@Size`‎، برای عدد و ‎`id`‎ از ‎`@NotNull`‎ و ‎`@Positive`‎.

## مرحله ۴ — mapper

**اشکال فعلی:** relationها تبدیل نمی‌شوند؛ category در هر دو جهت بی‌صدا گم می‌شود.

**آنچه باید انجام دهید:**

- در خروجی: ‎`categoryId`‎ و ‎`categoryTitle`‎ از category محصول، و ‎`parentId`‎ از parent category پر شوند.
- در ورودی: ‎`categoryId`‎ و ‎`parentId`‎ به entity تبدیل **نشوند** (‎`ignore`‎)؛ service آن‌ها را از repository می‌خواند.
- روش نوشتن خطوط ‎`@Mapping`‎ را در مرحله‌ی ۴ فایل ‎`attachments/readme.md`‎ ببینید.
- بعد از deploy، در جواب API ببینید ‎`categoryId`‎ و ‎`categoryTitle`‎ پر شده باشند.

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
| ‎`DuplicateProductNameException`‎ | product دیگری با همین نام وجود دارد |
| ‎`DuplicateCategoryTitleException`‎ | category دیگری با همین عنوان وجود دارد |
| ‎`CategoryNotFoundException`‎ | ‎`categoryId`‎ یا ‎`parentId`‎ ارسال‌شده وجود ندارد |
| ‎`InvalidParentCategoryException`‎ | یک category به‌عنوان parent خودش انتخاب شده است |
| ‎`CategoryInUseException`‎ | category‌ای که هنوز product یا sub-category دارد، حذف می‌شود |

همه‌ی این‌ها خودکار به جواب 409 تبدیل می‌شوند؛ کار دیگری لازم نیست.

## مرحله ۶ — service

**اشکال‌های فعلی:**

- متد ‎`findAll(page, size)`‎ مقدار ‎`null`‎ برمی‌گرداند و کد کامنت‌شده دارد.
- هیچ business rule‌ای بررسی نمی‌شود.

**متدهایی که باید به interface اضافه کنید** (غیر از شش متد ‎`BaseService`‎):

| interface | متد | کار |
|---|---|---|
| ‎`ProductService`‎ | ‎`List<ProductDto> findByCategory(Long categoryId)`‎ | productهای یک category |
| ‎`ProductCategoryService`‎ | ‎`List<ProductCategoryDto> findSubCategories(Long parentId)`‎ | sub-categoryهای یک category |

**متدهای کمکی ‎`private`‎ در کلاس‌های ‎`Impl`‎** (مثل ‎`checkFileNameIsFree`‎ در ‎`AttachmentServiceImpl`‎):

| کلاس | متد کمکی | کار |
|---|---|---|
| ‎`ProductServiceImpl`‎ | ‎`checkNameIsFree(request, currentId)`‎ | نام تکراری: ‎`DuplicateProductNameException`‎ (در update، خود همان product حساب نشود) |
| ‎`ProductServiceImpl`‎ | ‎`loadCategory(categoryId)`‎ | category را بخواند؛ اگر نبود: ‎`CategoryNotFoundException`‎ |
| ‎`ProductCategoryServiceImpl`‎ | ‎`checkTitleIsFree(request, currentId)`‎ | عنوان تکراری: ‎`DuplicateCategoryTitleException`‎ |
| ‎`ProductCategoryServiceImpl`‎ | ‎`loadParent(parentId, currentId)`‎ | parent را بخواند؛ اگر نبود: ‎`CategoryNotFoundException`‎؛ اگر خودش بود: ‎`InvalidParentCategoryException`‎ |
| ‎`ProductCategoryServiceImpl`‎ | ‎`checkNotInUse(id)`‎ | اگر product یا sub-category داشت: ‎`CategoryInUseException`‎ |

**کجا صدا زده شوند:**

| متد service | متدهای کمکی |
|---|---|
| ‎`create`‎ و ‎`update`‎ در ‎`ProductServiceImpl`‎ | ‎`checkNameIsFree`‎ و ‎`loadCategory`‎؛ سپس category را روی entity قرار دهید |
| ‎`create`‎ و ‎`update`‎ در ‎`ProductCategoryServiceImpl`‎ | ‎`checkTitleIsFree`‎ و ‎`loadParent`‎ |
| ‎`deleteById`‎ در ‎`ProductCategoryServiceImpl`‎ | ‎`checkNotInUse`‎ پیش از حذف |

**نکته‌ها:**

- ‎`findAll(page, size)`‎ را با ‎`findPage`‎ در repository پیاده کنید و کد کامنت‌شده را پاک کنید.
- ‎`ProductCategoryRepository`‎ را در ‎`ProductServiceImpl`‎، و ‎`ProductRepository`‎ را در ‎`ProductCategoryServiceImpl`‎ با ‎`@Inject`‎ بگیرید.
- ‎`@Transactional`‎ فقط روی متدهایی باشد که چیزی را تغییر می‌دهند.

## مرحله ۷ — REST API

**اشکال‌های فعلی:**

- ‎`ProductApi`‎ مسیر ‎`GET /{id}`‎ ندارد.
- در مسیر ‎`/page`‎ مقدار پیش‌فرض ‎`size`‎ صفر است و ‎`@Min(1)`‎ دارد؛ صدا زدن بدون ‎`size`‎ همیشه خطای 400 می‌دهد.
- متد ‎`DELETE`‎ بدنه‌ی request می‌خواهد.
- پیام متد ‎`notFound`‎ کلمه‌ی ‎`Attachment`‎ را دارد (کپی از ماژول دیگر).
- مسیر ‎`/productCategoris`‎ غلط املایی دارد و نام متد paging آن ‎`getProductsPage`‎ است.
- نام متدها با حرف بزرگ شروع شده است (‎`CreateProduct`‎ به‌جای ‎`createProduct`‎).

**یادآوری:** در API نه ‎`try/catch`‎ بنویسید و نه ‎`ApiResponse`‎ بسازید؛ فقط DTO را برگردانید و exception را throw کنید.
core جواب‌ها و خطاها را در قالب یکسان برمی‌گرداند (بخش ۶ [راهنمای دانشجو](../student-guide.md)).

**endpointهایی که در پایان باید کار کنند:**

| HTTP | مسیر | جواب |
|---|---|---|
| ‎`GET`‎ | ‎`/products`‎ | همه‌ی productها |
| ‎`GET`‎ | ‎`/products?categoryId=5`‎ | productهای یک category |
| ‎`GET`‎ | ‎`/products/page?page=0&size=10`‎ | یک صفحه |
| ‎`GET`‎ | ‎`/products/{id}`‎ | یک product؛ اگر نبود 404 |
| ‎`POST`‎ | ‎`/products`‎ | 201 همراه product جدید |
| ‎`PUT`‎ | ‎`/products/{id}`‎ | 200 همراه product ویرایش‌شده |
| ‎`DELETE`‎ | ‎`/products/{id}`‎ | 204 |
| ‎`GET`‎ | ‎`/product-categories`‎ | همه‌ی categoryها |
| ‎`GET`‎ | ‎`/product-categories/page?page=0&size=10`‎ | یک صفحه |
| ‎`GET`‎ | ‎`/product-categories/{id}`‎ | یک category؛ اگر نبود 404 |
| ‎`GET`‎ | ‎`/product-categories/{id}/sub-categories`‎ | sub-categoryهای آن |
| ‎`POST`‎ | ‎`/product-categories`‎ | 201 همراه category جدید |
| ‎`PUT`‎ | ‎`/product-categories/{id}`‎ | 200 همراه category ویرایش‌شده |
| ‎`DELETE`‎ | ‎`/product-categories/{id}`‎ | 204؛ اگر هنوز استفاده شده باشد 409 |

## مرحله ۸ — پاک‌سازی

- کلاس‌های پوشه‌ی ‎`rest/Temp`‎ (یعنی ‎`ProductApi2`‎ و ‎`ProductCategoryApi2`‎) در ‎`@Path`‎ دوباره ‎`/api/v1`‎ را آورده‌اند و کلاس ‎`Impl`‎ را inject می‌کنند.
  وقتی APIهای جدید کار کردند، این پوشه را حذف کنید.
- کلاس‌های ‎`ProductServlet`‎ و ‎`ProductCategoryServlet`‎ خالی هستند؛ یا کاملشان کنید یا حذفشان کنید.

## مرحله ۹ — چه وقت کار تمام است؟

1. کل پروژه بدون خطا کامپایل می‌شود.
2. پروژه deploy شده و همه‌ی endpointهای جدول مرحله‌ی ۷ روی TomEE یک بار امتحان شده‌اند.
3. این خطاها امتحان شده‌اند: یک خطای validation با جواب 400، یک ‎`id`‎ ناموجود با جواب 404، و هر پنج قاعده‌ی business با جواب 409.
4. همه‌ی موارد چک‌لیست بخش ۹ فایل ‎`attachments/readme.md`‎ به‌جز تست‌ها انجام شده‌اند.
5. هیچ‌کدام از موارد جدول Common mistakes در بخش ۱۰ فایل ‎`attachments/readme.md`‎ در کد شما باقی نمانده است.

تست‌نویسی بعداً در یک دستور کار جدا اضافه می‌شود.

</div>
