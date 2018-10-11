package calculator;

public class StringCalculator {
    int add(String text) {
        if(text == null || text.isEmpty()) {
            return 0;
        }

        String[] numbers = splits(text);

        return sum(toInts(numbers));
    }

    private String[] splits(String text) {
        if(text.startsWith("//") && text.contains("\n")) {
            int index = text.indexOf("//");
            int endIndex = text.indexOf("\n");
            String findSplit = text.substring((index+2),endIndex);
            String resText = text.substring(endIndex+1);
            return resText.split(findSplit);
        }

        return text.split(",|:");
    }

    private int sum(int[] numbers) {
        int sum = 0;
        for(int n : numbers) {
            sum += isPlus(n);
        }
        return sum;
    }

    private int[] toInts(String[] numbers) {
        int[] result = new int[numbers.length];

        for(int i=0;i<numbers.length;i++) {
            result[i] = Integer.parseInt(numbers[i]);
        }
        return result;
    }

    private int isPlus(int n) {
        if(n<0) {
            throw new RuntimeException();
        }

        return n;
    }
}
