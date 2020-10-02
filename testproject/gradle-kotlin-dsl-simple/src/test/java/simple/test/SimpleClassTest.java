package simple.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import simple.test.SimpleClass;

import static org.junit.jupiter.api.Assertions.*;

class SimpleClassTest {

    @Test
    void multiply() {
        SimpleClass simpleClass = new SimpleClass();
        Assertions.assertEquals(simpleClass.multiply(2), 4);
    }
}