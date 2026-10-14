# 🛒 Parallel MFT Online Store

> A lightweight enterprise e-commerce backend built with Pure Java SE and explicit object-oriented design.

---

## 🎯 هدف پروژه
پیاده‌سازی یک فروشگاه اینترنتی ماژولار و مقیاس‌پذیر با معماری تمیز (Clean Architecture)، بدون وابستگی به جادوهای فریم‌ورک‌ها و به صورت کاملاً ساختاریافته (Pure Java SE).

---

## 🛠️ زیرساخت و فناوری‌ها
- **زبان برنامه‌نویسی:** Java 17
- **سیستم ساخت:** Apache Maven
- **پایگاه داده:** PostgreSQL (JDBC خام)
- **گروه پروژه (`GroupId`):** `com.black`
- **شناسه اثر (`ArtifactId`):** `ParallelMftOnlineStoreEE`

---

## 📂 ساختار ماژول‌ها و پیکربندی
پروژه به صورت ماژول‌های کاملاً ایزوله و مستقل از هم طراحی شده است:
```text
com.black.parallelmftonlinestoree
├── 📁 attachments/    # مدیریت فایل‌ها و پیوست‌ها
├── 📁 delivery/       # ارسال و لجستیک سفارشات
├── 📁 financials/     # تراکنش‌های مالی و حسابداری
├── 📁 inventories/    # انبارداری و مدیریت موجودی
├── 📁 orders/         # ثبت و پیگیری سفارشات
├── 📁 organization/   # ساختار سازمانی و نقش‌ها
├── 📁 products/       # کاتالوگ محصولات و قیمت‌گذاری
├── 📁 security/       # امنیت و رمزنگاری
├── 📁 tickets/        # تیکت‌های پشتیبانی مشتریان
└── 📁 users/          # مدیریت کاربران و احراز هویت
