# Moviet

Android-приложение для поиска фильмов через OMDb, просмотра деталей и сохранения избранного. Проект готовится для JedyApps Android Home Assignment.

Сейчас это собираемый каркас: Compose-тема, стартовый экран, переход к экрану-заготовке избранного, Navigation 3, Koin, настройка Room и Retrofit, отдельный Android Library модуль `SDK`. Поиск, Paging-поток, детали, подключение избранного к UI и реклама пока не реализованы.

## Открытие в Android Studio

1. Открыть корневую папку `Moviet` как существующий проект. Локальная копия: `C:\Users\jacks\AndroidStudioProjects\Moviet`.
2. Выбрать встроенный JDK Android Studio для Gradle; требуется JDK 17 или новее.
3. Установить Android SDK Platform 37 и выполнить Gradle Sync. Локальный путь к SDK хранится в игнорируемом `local.properties`.
4. Запустить конфигурацию `app` на устройстве или эмуляторе с API 28+.

Gradle Wrapper включён в репозиторий. Системная установка Gradle не нужна. Если `java` отсутствует в PATH, выставить `JAVA_HOME` на JBR своей Android Studio.

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug --no-daemon
```

На Linux/macOS: `sh ./gradlew :app:assembleDebug :app:lintDebug --no-daemon`.

## Ключ OMDb

Для сборки и открытия каркаса ключ не требуется. Для будущих запросов скопировать `secrets.properties.example` в `secrets.properties` и заполнить `omdb.apiKey`. Также поддерживаются переменная окружения `OMDB_API_KEY` и Gradle property `omdb.apiKey`; приоритет: Gradle property → окружение → файл. Запросы в стартовом экране пока не выполняются.

Локальный файл с ключом исключён из Git. Ключ попадает в APK через BuildConfig, поэтому такой механизм не скрывает его от пользователя APK. Ключ из примера задания в проект не перенесён.

## Структура и документация

- `app` — Android-приложение, пакеты `app`, `core`, `search`, `details`, `favorites`.
- `SDK` — отдельный модуль для будущей рекламы; приложение уже зависит от него.
- [Описание и статус проекта](docs/PROJECT.md) — требования, границы текущей базы и дальнейшая работа.
- [Project conventions](docs/PROJECT_CONVENTIONS.md) — структура, загрузка данных, Compose, Koin, Room и Paging.
- [Версии зависимостей](docs/DEPENDENCIES.md) — проверенные стабильные версии и источники.
- [Skills](docs/SKILLS.md) — состав, происхождение и выбор нужных инструкций.
- [AGENTS.md](AGENTS.md) — начальная точка для агента в новом чате.

Git уже инициализирован. Начальный коммит не создан, staging не выполнен, удалённый репозиторий не настроен. `.gitignore` исключает сборки, IDE-состояние, локальные пути, ключи и подписи; Room schemas, Wrapper и проектные skills остаются видимыми для Git.
