module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires jmh.core;
    requires jdk.unsupported;

    opens benchmarks to jmh.core;
    opens org.example to javafx.fxml;
    exports org.example;
    opens controllers to javafx.fxml;
    exports utils;
    opens utils to org.junit.platform.commons;
    exports benchmarks;
    exports benchmarks.jmh_generated;
    opens benchmarks.jmh_generated to jmh.core;


}
