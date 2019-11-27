import java.util.*;

public class DependencyChecker {
    private Map<String, List<String>> dependencies;

    public DependencyChecker(String input) {
        List<String> lines = Arrays.asList(input.split("\n"));
        dependencies = new HashMap<>();
        lines.forEach(line -> {
            List<String> tokens = Arrays.asList(line.split(" "));
            String firstToken = tokens.get(0);

            List<String> dependencyList = new ArrayList<>(tokens.subList(1, tokens.size()));
            dependencies.put(firstToken, dependencyList);
        });
    }

    public Map<String, List<String>> getTransientDependencies() {
        Map<String, List<String>> transientDependenciesMap = new HashMap<>();
        dependencies.forEach((key, dependencyList) -> transientDependenciesMap.put(key, getTransientDependencies(dependencies, dependencyList, new ArrayList<>())));
        return transientDependenciesMap;
    }

    private static List<String> getTransientDependencies(Map<String, List<String>> dependencies, List<String> inputDependencies, List<String> visited) {
        if (inputDependencies.size() == 0) {
            return Collections.emptyList();
        }
        List<String> newDependencyList = new ArrayList<>(inputDependencies);
        inputDependencies.forEach(input -> {
            visited.add(input);
            Optional<List<String>> optional = Optional.ofNullable(dependencies.get(input));
            optional.ifPresent(listToken -> listToken.forEach(element -> {
                    if (!newDependencyList.contains(element)) {
                        newDependencyList.add(element);
                    }
                })
            );
        });

        List<String> unvisitedDecencies = new ArrayList<>(newDependencyList);
        unvisitedDecencies.removeAll(visited);
        List<String> nestedDependencies = getTransientDependencies(dependencies, unvisitedDecencies, visited);
        nestedDependencies.forEach(element -> {
            if (!newDependencyList.contains(element)) {
                newDependencyList.add(element);
            }
        });
        return newDependencyList;
    }
}
