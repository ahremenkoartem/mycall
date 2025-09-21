Запускайте приложение командой из терминала:

mvn clean javafx:run
Если хотите запускать из IDE, добавьте VM options вручную (Run → Edit Configurations):

--module-path "C:\Users\ahrem\.m2\repository\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2-win.jar;C:\Users\ahrem\.m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar;C:\Users\ahrem\.m2\repository\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar;C:\Users\ahrem\.m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar" --add-modules javafx.controls,javafx.fxml,javafx.graphics  
Убедитесь, что версия JDK для проекта и запуска - 21.

для поддержки сборки через jpackage-maven-plugin
нужен WIX https://github.com/wixtoolset/wix3/releases
Шаги установки WiX Toolset
Перейдите по ссылке на GitHub релизов WiX: https://github.com/wixtoolset/wix3/releases
Скачайте последний файл инсталлятора (например, wix314.exe)
Запустите установку WiX Toolset и следуйте инструкциям
Добавьте путь к установленным утилитам WiX (обычно C:\Program Files (x86)\WiX Toolset v3.14\bin) в переменную среды PATH Windows
Проверьте доступность утилит candle.exe и light.exe из командной строки
Через графический интерфейс Windows
Откройте «Пуск» и найдите «Этот компьютер», нажмите правой кнопкой и выберите «Свойства».
Нажмите слева «Дополнительные параметры системы».
В открывшемся окне нажмите кнопку «Переменные среды».
В разделе «Системные переменные» найдите переменную Path и нажмите «Изменить».
В открывшемся окне нажмите «Создать» и добавьте путь:
C:\Program Files (x86)\WiX Toolset v3.14\bin
Нажмите OK во всех открытых окнах, чтобы сохранить изменения.
Перезапустите терминал или IDE, чтобы переменная PATH обновилась.

иконки - формат ico,  и это не сработает если просто jpg формат менять , надо спец обработка, использую https://greenfishsoftware.org/gfie.php  