package calculator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringCalculatorTest {
    StringCalculator cal;

    @Before
    public void init() {
        cal = new StringCalculator();
    }

    @Test
    public void add_null() {
        assertEquals(0,cal.add(null));
        assertEquals(0,cal.add(""));
    }

    @Test
    public void add_one() throws Exception {
        assertEquals(1,cal.add("1"));
    }

    @Test
    public void add_쉼표() {
        assertEquals(3,cal.add("1,2"));
        assertEquals(3,cal.add("1,1,1"));

    }

    @Test
    public void add_쉼표s() {
        assertEquals(3,cal.add("1,1:1"));
        assertEquals(3,cal.add("1:1,1"));
    }

    @Test
    public void add_기호들() {
        assertEquals(3,cal.add("//,\n1,1,1"));
    }

    @Test(expected = RuntimeException.class)
    public void add_음수() {
        cal.add("-1,1,2");
    }
}
