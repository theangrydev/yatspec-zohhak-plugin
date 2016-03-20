package io.github.theangrydev.yatspeczohhakplugin;

import com.googlecode.yatspec.junit.ParameterResolverFactory;
import com.googlecode.yatspec.junit.Row;
import com.googlecode.yatspec.junit.SpecRunner;
import com.googlecode.yatspec.junit.Table;
import com.googlecode.zohhak.api.Coercion;
import org.assertj.core.api.WithAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.*;

@RunWith(SpecRunner.class)
public class ZohhakParameterResolverTest implements WithAssertions {

    static {
        ParameterResolverFactory.setParameterResolver(ZohhakParameterResolver.class);
    }

    @Test
    @Table({
        @Row({"1", "1", "2"}),
        @Row({"2", "3", "5"})
    })
    public void multipleParameters(int a, int b, int result) {
        assertThat(a + b).isEqualTo(result);
    }

    @Test
    @Table({
        @Row({"1", "2", "3"})
    })
    public void varargs(int... varargs) {
        assertThat(varargs).containsExactly(1, 2, 3);
    }

    @Test
    @Table({
        @Row("[1,2,3]")
    })
    public void list(List<Integer> list) {
        assertThat(list).containsExactly(1, 2, 3);
    }

    @Test
    @Table({
        @Row("[[1,2],[3,4,5],[]]")
    })
    public void listOfLists(List<List<Integer>> test) {
        assertThat(test).isEqualTo(asList(asList(1, 2), asList(3, 4, 5), emptyList()));
    }

    @Test
    @Table({
        @Row("[1,2,3]")
    })
    public void array(int[] array) {
        assertThat(array).containsExactly(1, 2, 3);
    }

    @Test
    @Table({
        @Row({"[1,2,3]", "[1,2,3]"})
    })
    public void multipleArrays(int[] firstArray, int[] secondArray) {
        assertThat(firstArray).containsExactly(secondArray);
    }

    @Test
    @Table({
        @Row({"[1,2,2]"})
    })
    public void set(Set<Integer> set) {
        assertThat(set).containsExactly(1, 2);
    }

    @Test
    @Table({
        @Row({"{a: {foo: 2}, b: {bar: 3}, c: {}}"})
    })
    public void nestedMap(Map<String, Map<String, Integer>> map) {
        assertThat(map)
                .containsEntry("a", singletonMap("foo", 2))
                .containsEntry("b", singletonMap("bar", 3))
                .containsEntry("c", emptyMap());
    }

    @Test
    @Table({
        @Row({"{a: heart, b: club}"})
    })
    public void mapOfCustomCoercion(Map<String, Suit> map) {
        assertThat(map)
                .containsEntry("a", Suit.HEART)
                .containsEntry("b", Suit.CLUB);
    }

    @Test
    @Table({
        @Row({"1", "2", "1", "2"})
    })
    public void varargsWithOtherParameters(int first, int second, int... rest) {
        assertThat(rest).containsExactly(first, second);
    }

    @Test
    @Table({
        @Row("HEART"),
        @Row("CLUB"),
        @Row("DIAMOND"),
        @Row("SPADE")
    })
    public void enumValue(Suit suit) {
        assertThat(suit).isEqualTo(Suit.valueOf(suit.name()));
    }

    @Test
    @Table({
        @Row({"$100"})
    })
    public void genericParameterTypeCustomConversion(Bill<Integer> bill) {
        assertThat(bill.getAmount()).isEqualTo(100);
    }

    @Test
    @Table({
        @Row("heart"),
        @Row("club"),
        @Row("diamond"),
        @Row("spade")
    })
    public void customCoercion(Suit suit) {
        try {
            assertThat(suit).isEqualTo(parseSuit(suit.symbol()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private enum Suit {
        HEART("heart"),
        CLUB("club"),
        DIAMOND("diamond"),
        SPADE("spade");

        private final String symbol;

        Suit(String symbol) {
            this.symbol = symbol;
        }

        public String symbol() {
            return symbol;
        }

        public static Suit parseSuit(String name) {
            return stream(values())
                    .filter(suit -> suit.symbol().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported name: " + name));
        }
    }

    @Coercion
    public Suit parseSuit(String symbol) {
        return Suit.parseSuit(symbol);
    }

    private static class Bill<T extends Number> {
        private final T amount;

        private Bill(T amount) {
            this.amount = amount;
        }

        T getAmount() {
            return amount;
        }
    }

    @Coercion
    public Bill<Integer> coerceBill(String amount) {
        return new Bill<>(Integer.parseInt(amount.substring(1)));
    }
}