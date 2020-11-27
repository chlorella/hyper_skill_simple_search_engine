package search;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class SearchStrategyClient {
    SearchStrategy searchStrategy;

    SearchStrategyClient(String type) {
        switch (type) {
            case "ALL":
                searchStrategy = new AllSearchStrategy();
                break;
            case "ANY":
                searchStrategy = new AnySearchStrategy();
                break;
            case "NONE":
                searchStrategy = new NoneSearchStrategy();
                break;
            default:
        }
    }

    List<Integer> search(String search, Map<String, List<Integer>> invertedIndex, Integer size) {
        return searchStrategy.search(search, invertedIndex, size);
    }
}

interface SearchStrategy {
    List<Integer> search(String search, Map<String, List<Integer>> invertedIndex, Integer size);
}

class AllSearchStrategy implements SearchStrategy {

    @Override
    public List<Integer> search(String search, Map<String, List<Integer>> invertedIndex, Integer size) {
        return invertedIndex.getOrDefault(search, new ArrayList<>());
    }
}

class AnySearchStrategy implements SearchStrategy {

    @Override
    public List<Integer> search(String search, Map<String, List<Integer>> invertedIndex, Integer size) {
        Set<Integer> indexSet = new HashSet<>();
        Arrays.stream(search.split(" ")).map(
                word -> invertedIndex.getOrDefault(word, new ArrayList<>())
        ).forEach(indexSet::addAll);

        return new ArrayList<>(indexSet);
    }
}

class NoneSearchStrategy implements SearchStrategy {

    @Override
    public List<Integer> search(String search, Map<String, List<Integer>> invertedIndex, Integer size) {
        Set<Integer> indexSet = new HashSet<>();
        Arrays.stream(search.split(" ")).map(
                word -> invertedIndex.getOrDefault(word.toLowerCase(), new ArrayList<>())
        ).forEach(indexSet::addAll);

        return IntStream.range(0,size).filter(i -> !indexSet.contains(i)).boxed().collect(Collectors.toList());
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner systemInScanner = new Scanner(System.in);
        String[] dataArray = null;
        for (int i = 0; i < args.length; i++) {
            if ("--data".equals(args[i])) {
                dataArray = getDataFromFiles(args[i + 1].replace("--", ""));
            }
        }

        menu(systemInScanner, dataArray);
    }

    public static String[] getDataFromFiles(String fileName) {
        File file = new File(fileName);
        ArrayList<String> list = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                list.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("No file found: " + fileName);
        }
        return list.toArray(String[]::new);
    }

    public static Map<String, List<Integer>> arrayToInvertedIndex(String[] dataArray) {
        Map<String, List<Integer>> invertedIndex = new HashMap<>();

        for (int i = 0; i < dataArray.length; i++) {
            String[] spliced = dataArray[i].split(" ");
            for (String word : spliced) {
                List<Integer> indexList = invertedIndex.getOrDefault(word.toLowerCase(), new ArrayList<>());
                indexList.add(i);
                invertedIndex.put(word.toLowerCase(), indexList);
            }
        }

        return invertedIndex;
    }

    public static void menu(Scanner scanner, String[] dataArray) {
        Map<String, List<Integer>> invertedIndex = arrayToInvertedIndex(dataArray);

        while (true) {
            System.out.println("=== Menu ===\n" +
                    "1. Find a person\n" +
                    "2. Print all people\n" +
                    "0. Exit");

            int option = scanner.nextInt();
            scanner.nextLine();
            switch (option) {
                case 1:
                    searchQueries(scanner, dataArray, invertedIndex);
                    break;
                case 2:
                    printAll(dataArray);
                    break;
                case 0:
                    System.out.println("Bye!");
                    return;
                default:
                    System.out.println("Incorrect option! Try Again.");
            }
        }
    }

    public static void printAll(String[] dataArray) {
        for (String s : dataArray) {
            System.out.println(s);
        }
    }

    public static void searchQueries(Scanner scanner, String[] dataArray, Map<String, List<Integer>> invertedIndex) {
        System.out.println("Select a matching strategy: ALL, ANY, NONE");

        String matchingStrategy = scanner.nextLine();
        SearchStrategyClient strategyClient = new SearchStrategyClient(matchingStrategy);

        System.out.println("Enter data to search record: ");
        String searchStr = scanner.nextLine();

        List<Integer> indexList = strategyClient.search(searchStr, invertedIndex, dataArray.length);

        printSearchResult(indexList, dataArray);
    }

    public static void printSearchResult(List<Integer> indexList, String[] dataArray) {
        if (indexList.isEmpty()) {
            System.out.println("Not Found");
        } else {
            System.out.println(indexList.size() + " persons found:");
            for (Integer i : indexList) {
                System.out.println(dataArray[i]);
            }
        }
    }

}
