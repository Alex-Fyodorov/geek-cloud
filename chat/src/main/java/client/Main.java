package client;

public class Main {
    public static void main(String[] args) {
        System.out.println(calc("круг 100.111"));
    }

    private static String calc(String input) {
        String[] strArr = input.split(" ");
        double side = 0d;
        if (strArr.length < 2) side = 1d;
        else side = Double.parseDouble(strArr[1]);
        double sq = 0d;
        double per = 0d;
        if (strArr[0].equals("круг")) {
            sq = 3.1415d * side * side;
            per = 2 * 3.1415 * side;

        } else {
            sq = side * side;
            per = side * 4;
        }
        String sqStr = String.format("%.2f", sq);
        String perStr = String.format("%.2f", per);
        return String.format("%s %s", sqStr, perStr);
    }


}
