# 📱 AI Video Studio — Android 8.0+ App (Kotlin + GitHub Actions APK Builder)

Yeh complete Android Studio project hai jo **Android 8.0 (API 26) se Android 15 (API 35)** tak har phone par chalta hai.

## 🏗️ APK Kaise Banayein? (2 Aasan Tareeqe)

### Tareeqa 1: GitHub Repository se Automatic APK (Bina Android Studio ke!)
1. GitHub par ek naya repository banayein (maslan `ai-video-studio-android`).
2. Is ZIP ke andar maujood tamam files (`app/`, `gradle/`, `.github/`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`) ko us repository mein upload/push karein.
3. GitHub par **Actions** tab kholein — **`Build Android APK (AI Video Studio)`** workflow khud-ba-khud chal padega!
4. 2 minute baad workflow complete hone par neeche **Artifacts** mein **`AI-Video-Studio-Android8Plus-APK`** par click karke apni installable `.apk` file download kar lein!

### Tareeqa 2: Android Studio se Direct APK
1. Android Studio kholein aur **Open** par click karke is `AIVideoStudio_Android` folder ko select karein.
2. Gradle sync complete hone ke baad top menu mein **Build -> Build Bundle(s) / APK(s) -> Build APK(s)** par click karein.
3. `app/build/outputs/apk/debug/app-debug.apk` taiyar mil jayegi!

---

## 📱 Mobile App Kaise Chalegi?
1. Phone par APK install karein.
2. `colab_backend/colab_mobile_studio_server.ipynb` ko Google Colab (T4 GPU) par chalayein aur wahan milne wala `https://xxxx.trycloudflare.com` URL Android app mein paste kar dein.
3. App ke top bar mein **Google Flow** button bhi diya gaya hai—ek tap se aap apne Gemini Pro login ke saath Google Flow khol kar Veo clips generate kar sakte hain aur wapas Studio par aa kar final MP4 render kar sakte hain!
