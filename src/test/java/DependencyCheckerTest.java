import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class DependencyCheckerTest {

    private Map<String, List<String>> expectedOutput = new HashMap<>();

    @Before
    public void setExpectedOutput() {
        List<String> a = new ArrayList<>();
        a.add("B");
        a.add("C");
        a.add("E");
        a.add("F");
        a.add("G");
        a.add("H");
        expectedOutput.put("A", a);
        List<String> b = new ArrayList<>();
        b.add("C");
        b.add("E");
        b.add("F");
        b.add("G");
        b.add("H");
        expectedOutput.put("B", b);
        List<String> c = new ArrayList<>();
        c.add("G");
        expectedOutput.put("C", c);
        List<String> d = new ArrayList<>();
        d.add("A");
        d.add("B");
        d.add("C");
        d.add("E");
        d.add("F");
        d.add("G");
        d.add("H");
        expectedOutput.put("D", d);
        List<String> e = new ArrayList<>();
        e.add("F");
        e.add("H");
        expectedOutput.put("E", e);
        List<String> f = new ArrayList<>();
        f.add("H");
        expectedOutput.put("F", f);
    }

    @Test
    public void calculateTransientDependencies() throws IOException {
        String input = FileReaderUtility.readFile("input.txt");
        DependencyChecker dependencyChecker = new DependencyChecker(input);
        Map<String, List<String>> actualOutput = dependencyChecker.getTransientDependencies();
        assertEquals(expectedOutput.keySet(), actualOutput.keySet());
        actualOutput.forEach((key, dependency) -> assertTrue(expectedOutput.get(key).containsAll(dependency)));
    }
}