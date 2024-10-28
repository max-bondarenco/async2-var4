import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {
    private static final double LOWER_BOUND = 0.5;
    private static final double UPPER_BOUND = 99.5;
    private static final int LIST_LENGTH = 100000;
    private static final int NUM_THREADS = 10;
    private static final int CHUNK_SIZE = LIST_LENGTH / NUM_THREADS;

    public static void main(String[] args) {
        CopyOnWriteArraySet<Double> numbers = getNumbers();
        long startTime = System.currentTimeMillis();
        List<Double> result = processNumbers(numbers);
        long endTime = System.currentTimeMillis();

        System.out.println("Processing finished in " + (endTime-startTime) + "ms");
//        System.out.println("Input values: " + numbers.stream().map(num -> Math.floor(num*100)/100).toList());
//        System.out.println("Results:      " + result.stream().map(num -> Math.floor(num*100)/100).toList());
    }

    private static CopyOnWriteArraySet<Double> getNumbers() {
        Scanner sc = new Scanner(System.in);
        double userLowerBound, userUpperBound;

        while(true) {
            try {
                System.out.printf("Enter lower bound (Floating point number not less than %f)): ", LOWER_BOUND);
                userLowerBound = Double.parseDouble(sc.nextLine());
                if(userLowerBound < LOWER_BOUND) throw new RuntimeException("Your input is out of accepted bounds");
                break;
            } catch (NumberFormatException e) {
                System.out.println("Your input is not a floating point number");
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }
        }

        while(true) {
            try {
                System.out.printf("Enter upper bound (Floating point number between %f and %f)): ", userLowerBound, UPPER_BOUND);
                userUpperBound = Double.parseDouble(sc.nextLine());
                if(userUpperBound > UPPER_BOUND) throw new RuntimeException("Your input is out of accepted bounds");
                if(userUpperBound <= userLowerBound) throw new RuntimeException("Upper bound must be greater than lower bound");
                break;
            } catch (NumberFormatException e) {
                System.out.println("Your input is not a floating point number");
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }
        }

        sc.close();

        return generateRandomList(userLowerBound, userUpperBound);
    }

    private static CopyOnWriteArraySet<Double> generateRandomList(double lowerBound, double upperBound) {
        CopyOnWriteArraySet<Double> result = new CopyOnWriteArraySet<>();
        while(result.size() < LIST_LENGTH) {
            result.add(lowerBound + (upperBound-lowerBound) * Math.random());
        }
        return result;
    }

    private static List<Double> processNumbers (CopyOnWriteArraySet<Double> numbers) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        List<Future<List<Double>>> futures = new ArrayList<>();

        for(int i=0; i<NUM_THREADS; i++) {
            futures.add(executor.submit(new ArraySquaredTask(numbers.stream().skip(i*CHUNK_SIZE).limit(CHUNK_SIZE).toList())));
        }

        outer: while(true) {
            for(Future<List<Double>> future: futures) {
                if(future.isCancelled()) {
                    System.out.println("One of the processes got cancelled. Can not finish task.");
                    return new ArrayList<>();
                }
                if(!future.isDone()) continue outer;
            }
            break;
        }

        executor.close();

        return futures.stream()
                .map(Future::resultNow).flatMap(List::stream)
                .toList();
    }
}