package calculator;

import calculator.Calculator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CalculatorTest {
    static int count = 1;
    private Calculator cal;

    @Before
    public void setup() {
        System.out.println(count++);
        cal = new Calculator();
        System.out.println("before");
    }

    @Test
    public void add() {
        assertEquals(9, cal.add(6,3));
    }

    @Test
    public void subtract() {
        assertEquals(1, cal.subtract(6,5));
    }

    @After
    public void teardown() {
        System.out.println("teardown");
    }
}
