import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.*;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.*;

public class AnimalFoodOptimization {

    static class FoodItem {
        String name;

        double minPrice;
        double maxPrice;
        double quality;

        FoodItem(String name, double minPrice, double maxPrice, double quality) {
            this.name = name;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.quality = quality;
        }


        public String getName() {
            return this.name;
        }



        public double getMinPrice() {
            return this.minPrice;
        }

        public double getMaxPrice() {
            return this.maxPrice;
        }

        public double getQuality() {
            return this.quality;
        }

    }


    static class FoodGroup {
        List<FoodItem> foods;
        double amountRequiredPerAnimal;

        FoodGroup(double amountRequiredPerAnimal) {
            this.foods = new ArrayList<>();
            this.amountRequiredPerAnimal = amountRequiredPerAnimal;
        }

        void addFood(FoodItem food) {
            this.foods.add(food);
        }

        public FoodItem getFoodItemByNameAndQuality(List<FoodItem> foodItems, String name, double quality) {
            for (FoodItem item : foodItems) {
                if (item.getName().equals(name) && item.getQuality() == quality) {
                    return item;
                }
            }
            return null; // Return null if no matching FoodItem is found
        }
    }

    static class AnimalRequirement {
        String name;
        int count;
        List<FoodGroup> foodGroups;

        AnimalRequirement(String name, int count) {
            this.name = name;
            this.count = count;
            this.foodGroups = new ArrayList<>();
        }

        void addFoodGroup(FoodGroup group) {
            this.foodGroups.add(group);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Reading the table data
        List<FoodItem> foodItems = new ArrayList<>();
        System.out.println("Enter the food items in the format: name minPrice maxPrice quality (separated by spaces)");
        System.out.println("Example: Acelga 465.7 551.4 0.5");
        String line = scanner.nextLine();
        String[] parts = line.split("\\s+");
        int n = parts.length / 4; // Calculate the number of food items
        for (int i = 0; i < parts.length; i += 4) {
            String name = parts[i];
            double minPrice = Double.parseDouble(parts[i + 1]);
            double maxPrice = Double.parseDouble(parts[i + 2]);
            double quality = Double.parseDouble(parts[i + 3]);
            foodItems.add(new FoodItem(name, minPrice, maxPrice, quality));
        }

        // Reading animal requirements and their interchangeable food groups
        System.out.println("Enter the number of animals:");
        int a = scanner.nextInt();
        List<AnimalRequirement> animalRequirements = new ArrayList<>();

        for (int i = 0; i < a; i++) {
            System.out.println("Enter the name of animal " + (i + 1) + ":");
            String animal = scanner.next();
            System.out.println("Enter the amount of this animal:");
            int count = scanner.nextInt();
            AnimalRequirement requirement = new AnimalRequirement(animal, count);

            System.out.println("Enter the number of food groups for " + animal + ":");
            int g = scanner.nextInt();
            for (int j = 0; j < g; j++) {
                System.out.println("Enter the amount of food required per animal for group " + (j + 1) + ":");
                double amountRequiredPerAnimal = scanner.nextDouble();
                FoodGroup group = new FoodGroup(amountRequiredPerAnimal);
                System.out.println("Enter the number of foods in group " + (j + 1) + " for " + animal + ":");
                int fg = scanner.nextInt();
                for (int k = 0; k < fg; k++) {
                    System.out.println("Enter name and quality for food item " + (k + 1) + " in group " + (j + 1) + ":");
                    String name = scanner.next();
                    double quality = scanner.nextDouble();
                    FoodItem item = group.getFoodItemByNameAndQuality(foodItems, name, quality);
                    group.addFood(item);
                }
                requirement.addFoodGroup(group);
            }
            animalRequirements.add(requirement);
        }

        // Setting constraints
        System.out.println("Enter minimum quality:");
        double minQuality = scanner.nextDouble();
        System.out.println("Enter budget:");
        double budget = scanner.nextDouble();

        if (budget == 0) {
            // Calculate the total cost for minimum and maximum quality
            double minTotalCost = 0;
            double maxTotalCost = 0;
            for (AnimalRequirement requirement : animalRequirements) {
                int animalCount = requirement.count;
                for (FoodGroup group : requirement.foodGroups) {
                    double minCost = Double.MAX_VALUE;
                    double maxCost = Double.MIN_VALUE;
                    for (FoodItem food : group.foods) {
                        double foodMinCost = food.minPrice * group.amountRequiredPerAnimal * animalCount;
                        double foodMaxCost = food.maxPrice * group.amountRequiredPerAnimal * animalCount;
                        if (foodMinCost < minCost) {
                            minCost = foodMinCost;
                        }
                        if (foodMaxCost > maxCost) {
                            maxCost = foodMaxCost;
                        }
                    }
                    minTotalCost += minCost;
                    maxTotalCost += maxCost;
                }
            }
            System.out.println("Total cost for minimum quality: " + minTotalCost);
            System.out.println("Total cost for maximum quality: " + maxTotalCost);
            return;
        }

        // Formulating the linear programming problem
        double[] coefficients = new double[n];
        for (int i = 0; i < n; i++) {
            coefficients[i] = foodItems.get(i).quality;
        }

        LinearObjectiveFunction objectiveFunction = new LinearObjectiveFunction(coefficients, 0);

        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // Budget constraint
        double[] priceCoefficients = new double[n];
        for (int i = 0; i < n; i++) {
            priceCoefficients[i] = (foodItems.get(i).minPrice + foodItems.get(i).maxPrice) / 2;
        }
        constraints.add(new LinearConstraint(priceCoefficients, Relationship.LEQ, budget));

        // Minimum quality constraint
        double[] qualityCoefficients = new double[n];
        for (int i = 0; i < n; i++) {
            qualityCoefficients[i] = foodItems.get(i).quality;
        }
        constraints.add(new LinearConstraint(qualityCoefficients, Relationship.GEQ, minQuality));

        // Varied diet constraint: Ensure each animal's diet includes at least one food item from each of its food groups
        Map<String, Integer> foodIndexMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            foodIndexMap.put(foodItems.get(i).name, i);
        }

        for (AnimalRequirement requirement : animalRequirements) {
            int animalCount = requirement.count;
            for (FoodGroup group : requirement.foodGroups) {
                double[] groupCoefficients = new double[n];
                boolean validFoodFound = false;
                for (FoodItem food : group.foods) {
                    if (foodIndexMap.containsKey(food.name)) {
                        groupCoefficients[foodIndexMap.get(food.name)] = group.amountRequiredPerAnimal;
                        validFoodFound = true;
                    }
                }
                if (validFoodFound) {
                    constraints.add(new LinearConstraint(groupCoefficients, Relationship.GEQ, group.amountRequiredPerAnimal * animalCount)); // Ensure required units per animal
                }
            }
        }

        // Solve the problem
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(objectiveFunction, new LinearConstraintSet(constraints), GoalType.MAXIMIZE, new NonNegativeConstraint(true));

        // Output the results
        if (solution != null) {
            System.out.println("Optimal solution found:");
            RealVector solutionVector = new ArrayRealVector(solution.getPoint());
            for (int i = 0; i < n; i++) {
                System.out.printf("Food item %s: %f units%n", foodItems.get(i).name, solutionVector.getEntry(i));
            }
        } else {
            System.out.println("No solution found.");
        }

        scanner.close();
    }
}