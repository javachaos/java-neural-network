module examples {
    exports com.github.javachaos.javaneuralnetwork.examples;

    requires transitive core;
    requires transitive shared;
    requires transitive java.desktop;
    requires org.apache.logging.log4j;
    requires static org.junit.jupiter.api;
}
