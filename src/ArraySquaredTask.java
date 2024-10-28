import java.util.List;
import java.util.concurrent.Callable;

public class ArraySquaredTask implements Callable<List<Double>> {
    private final List<Double> numbers;

    public ArraySquaredTask(List<Double> numbers) {
        this.numbers = numbers;
    }

    @Override
    public List<Double> call() {
        return this.numbers.stream().map(num -> num*num).toList();
    }
}
