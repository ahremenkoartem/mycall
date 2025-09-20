Запускайте приложение командой из терминала:

mvn clean javafx:run
Если хотите запускать из IDE, добавьте VM options вручную (Run → Edit Configurations):

--module-path "C:\Users\ahrem\.m2\repository\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2-win.jar;C:\Users\ahrem\.m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar;C:\Users\ahrem\.m2\repository\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar;C:\Users\ahrem\.m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar" --add-modules javafx.controls,javafx.fxml,javafx.graphics  
Убедитесь, что версия JDK для проекта и запуска - 21.