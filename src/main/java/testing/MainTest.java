package testing;

public class MainTest {

    public static void main(String[] args) {


    }

    private static <T extends Number> long power(T value, int a){

        long sum = 1;
        for (int i = 0; i < a; i++){
            sum *= value.longValue();
        }

        return sum;
    }
}
