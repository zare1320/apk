<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# دستیار حرفه‌ای دامپزشکی (Vet Assistant Pro)

**دستیار فوق‌حرفه‌ای و تخصصی دامپزشکی با قابلیت‌های محاسبات دارویی هوشمند، پرونده الکترونیک، تشخیص و تفکیک گونه‌ای و محیط مجزا برای دامپزشکان و صاحبان حیوانات خانگی.**

**A high-fidelity veterinary assistant application featuring intelligent drug calculations, electronic medical records (EMR), differential diagnosis tools, and dedicated environments for both veterinarians and pet owners.**

View your app in AI Studio: https://ai.studio/apps/1f3745bc-add2-45ad-9b32-327110d765d9

---

## 🌟 ویژگی‌های کلیدی (Key Features)

### 👨‍⚕️ بخش دامپزشکان و دانشجویان (Veterinarian & Student Suite)
- **پذیرش هوشمند بیمار (Smart Patient Intake):** ثبت دقیق اطلاعات سگ، گربه و حیوانات اگزوتیک (پرندگان، جوندگان، آبزیان و دوزیستان).
- **دارونامه تخصصی و محاسبات خودکار (Intelligent Drug Manual):** دسترسی به فارماکوپه جامع با قابلیت محاسبه خودکار دوز (Dosage) و حجم (Volume) دارو بر اساس وزن بیمار.
- **تشخیص و درمان (Diagnosis & Treatment):** سیستم تشخیص افتراقی بر اساس علائم بالینی و پروتکل‌های درمانی استاندارد.
- **محاسبه‌گرهای کلینیکال (Clinical Calculators):**
    - مایع‌درمانی (Fluid Therapy)
    - انتقال خون (Blood Transfusion)
    - تخمین کالری مورد نیاز (Calorie Calculator)
    - زمان زایمان (Gestation)
    - سن معادل انسانی (Human Age)
    - تریاژ تروما (Trauma Triage)
- **پرونده الکترونیک (EMR):** مدیریت سوابق مراجعات و تاریخچه کلینیکی.

### 🐕 بخش صاحبان حیوانات خانگی (Pet Owner Suite)
- **داشبورد مراقبت هوشمند:** مشاهده وضعیت عمومی و یادآوری‌ها.
- **مدیریت نسخه‌ها (Prescriptions):** دسترسی به تاریخچه داروهای تجویز شده.
- **تقویم سلامت (Health Calendar):** ثبت نوبت‌های ویزیت و واکسیناسیون.
- **نقشه کلینیک‌ها (Clinic Map):** مکان‌یابی مراکز درمانی.

---

## 🛠 پشته فنی (Technical Stack)

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Database:** [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- **Networking:** [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
- **JSON Parsing:** [Moshi](https://github.com/square/moshi)
- **AI Integration:** [Gemini API](https://ai.google.dev/) (Server-side)
- **Architecture:** MVVM (Model-View-ViewModel) with Repository Pattern
- **Dependency Management:** Gradle Version Catalog (libs.versions.toml)

---

## 🚀 راه اندازی محلی (Local Setup)

**پیش‌نیازها:** [Android Studio Ladybug](https://developer.android.com/studio) یا نسخه‌های جدیدتر.

1. **کلون کردن پروژه:**
   ```bash
   git clone <repository-url>
   ```
2. **تنظیمات API:**
   یک فایل با نام `.env` در ریشه پروژه ایجاد کنید و کلید Gemini خود را در آن قرار دهید:
   ```env
   GEMINI_API_KEY=your_api_key_here
   ```
3. **اجرای پروژه:**
   - پروژه را در Android Studio باز کنید.
   - اجازه دهید Gradle Sync انجام شود.
   - اپلیکیشن را روی یک Emulator یا دستگاه فیزیکی اجرا کنید.

---

## 🧪 تست و پایداری (Testing)

این پروژه شامل تست‌های واحد (Unit Tests) و تست‌های رابط کاربری با استفاده از **Roborazzi** برای اسکرین‌شات تستینگ است.
برای اجرای تست‌ها:
```bash
./gradlew test
```

---

<div align="center">
ساخته شده با ❤️ برای جامعه دامپزشکی
</div>
