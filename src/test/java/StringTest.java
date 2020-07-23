import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTest {

    public static void main(String[] args) {
        String test = "efwefopwiejfoiwjef <$m> YO dodkwokd dd nnneeee ___ !!! <$n>", test2, test3 = null;

        Tuple[] patterns = {
                new Tuple(Pattern.compile("<$m>"), "HIGHLIGHT"),
                new Tuple(Pattern.compile("<$n>"), "NORMAL"),
                new Tuple(Pattern.compile("<$p>"), "PREFIX")};

        String highlightColor = "HIGHLIGHT";
        String normalColor = "NORMAL";
        long start = System.nanoTime();

        for(int i = 0 ; i < 10000000; i++) {
            test2 = test.replace("<$m>", "" + highlightColor).replace("<$n>", "" + normalColor).replace("<$p>", highlightColor);
        }

        System.out.println((System.nanoTime() - start) + "ns");
        start = System.nanoTime();

        for(int i = 0 ; i < 10000000; i++) {
            test3 = test;
            for(Tuple compiled : patterns) {
                Matcher m = compiled.pattern.matcher(test3);
                test3 = m.replaceAll(compiled.color);
            }
        }

        System.out.println((System.nanoTime() - start) + "ns");
        start = System.nanoTime();
        for(int i = 0; i < 10000000; i++) {
            test3 = test;

            StringBuilder builder = new StringBuilder(test3.length());
            char[] c = test3.toCharArray();
            int j = 2, l = -1;
            for(j = 2; j < c.length - 1; j++) {
                if(c[j - 2] == '<' && c[j - 1] == '$' && c[j + 1] == '>') {
                    char k = c[j];
                    switch(k) {
                        case 'M':
                        case 'm':
                            builder.append(highlightColor);
                            j+=3;
                            continue;
                        case 'N':
                        case 'n':
                            builder.append(normalColor);
                            j+=3;
                            continue;
                    }
                }

                builder.append(c[j - 2]);
            }
            for( ; j < c.length + 2; j++) {
                builder.append(c[j - 2]);
            }

            test3 = builder.toString();
        }
        System.out.println((System.nanoTime() - start) + "ns");


        System.out.println(test3);

    }


    private static class Tuple {
        private final Pattern pattern;
        private final String color;

        public Tuple(Pattern pattern, String color) {
            this.pattern = pattern;
            this.color = color;
        }
    }
}
