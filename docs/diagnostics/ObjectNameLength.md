# Имена объектов метаданных не должны превышать 80 символов (ObjectNameLength)

<Metadata>

## <Params>

<!-- Блоки выше заполняются автоматически, не трогать -->
## Описание диагностики
<!-- Описание диагностики заполняется вручную. Необходимо понятным языком описать смысл и схему работу -->

Имена объектов метаданных не должны превышать 80 символов.

Кроме проблем с использованием этих объектов возникают проблемы с выгрузкой конфигурации в файлы.

## Примеры

ОченьДлинноеИмяСправочникиКотороеВызываетПроблемыВРаботеАТакжеОшибкиВыгрузкиКонфигурации, LooooooooooooooooooooooooooooooooooooooooooooooooooooooooongVeryLongDocumentName

## Источники
<!-- Необходимо указывать ссылки на все источники, из которых почерпнута информация для создания диагностики -->

[Стандарт: Имя, синоним, комментарий](https://its.1c.ru/db/v8std#content:474:hdoc:2.3)

## Сниппеты
<!-- Блоки ниже заполняются автоматически, не трогать -->

### Экранирование кода

```bsl
// BSLLS:ObjectNameLength-off
// BSLLS:ObjectNameLength-on
```

### Параметр конфигурационного файла

```json
"ObjectNameLength": {
    "maxObjectNameLength": 80
}
```