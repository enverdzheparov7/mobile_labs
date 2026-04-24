# AnimationApp — Лабораторная работа

## Что реализовано

Приложение демонстрирует 6 видов базовой анимации для `ImageView`:

| Кнопка | Анимация | Файл |
|--------|----------|------|
| BLINK  | Мигание (alpha 0↔1, infinite) | `res/anim/blink_animation.xml` |
| ROTATE | Поворот 0°→360°→0° | `res/anim/rotate_animation.xml` |
| FADE   | Затухание (fade in → fade out) | `res/anim/fade_animation.xml` |
| MOVE   | Перемещение вправо на 75% | `res/anim/move_animation.xml` |
| SLIDE  | Скольжение (scale Y 1→0) | `res/anim/slide_animation.xml` |
| ZOOM   | Масштабирование ×2 | `res/anim/zoom_animation.xml` |
| STOP ANIMATION | Остановка любой анимации | — |

## Структура проекта

```
AnimationApp/
├── app/
│   ├── build.gradle                          ← зависимости + Lottie
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/animationapp/
│       │   └── MainActivity.java             ← обработчики кнопок
│       └── res/
│           ├── anim/
│           │   ├── blink_animation.xml
│           │   ├── fade_animation.xml
│           │   ├── move_animation.xml
│           │   ├── rotate_animation.xml
│           │   ├── slide_animation.xml
│           │   └── zoom_animation.xml
│           ├── drawable/
│           │   └── img.png                   ← ДОБАВИТЬ ВРУЧНУЮ
│           ├── layout/
│           │   └── activity_main.xml
│           └── values/
│               └── strings.xml
├── build.gradle
└── settings.gradle
```

## Как открыть в Android Studio

1. Откройте Android Studio → **Open** → выберите папку `AnimationApp`
2. Добавьте любое изображение в `app/src/main/res/drawable/` с именем **`img.png`**
3. Нажмите **Sync Project with Gradle Files**
4. Запустите на эмуляторе или устройстве (minSdk 21)

## Lottie (бонус)

Зависимость уже добавлена в `build.gradle`:
```groovy
implementation 'com.airbnb.android:lottie:6.4.0'
```
Для использования:
1. Скачайте JSON-анимацию с https://lottiefiles.com/featured
2. Создайте папку `res/raw/` и положите туда файл (например, `lottie.json`)
3. Добавьте в layout `<com.airbnb.lottie.LottieAnimationView>` с атрибутом `app:lottie_rawRes="@raw/lottie"`
