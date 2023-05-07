package prob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//还不知道怎么存储
public class Solution {
    //    本质是一个序列；
    public int numOfSolution;
    public int numOfCars;
    public List<List<Car>> cars;
    public double[][] objectives;

    public Solution(Instance instance) {
        this.numOfCars = instance.cars.size();
    }

    public void setSol(List<Individual> Population) {
        this.numOfSolution = Population.size();
        this.numOfCars = Population.get(0).cars.size();
        this.cars = new ArrayList<>();
        this.objectives = new double[numOfSolution][numOfCars + 4];
//        转化为数组对吗
        for (int i = 0; i < numOfSolution; i++) {
            this.cars.set(i, Population.get(i).cars);
        }
        for (int i = 0; i < numOfSolution; i++) {
            for (int j = 0; j < numOfCars; j++) {
                objectives[i][j] = cars.get(numOfSolution).get(numOfCars).carNo;

            }
            objectives[i][numOfCars] = Population.get(i).objectives[0];
            objectives[i][numOfCars + 1] = Population.get(i).objectives[1];
            objectives[i][numOfCars + 2] = Population.get(i).objectives[2];
            objectives[i][numOfCars + 3] = Population.get(i).objectives[3];
        }
    }

    public boolean isFeasible(Solution solution) {
        for (int i = 0; i < solution.numOfSolution; i++) {
            Individual individual = new Individual(solution.cars.get(i));
            if (!individual.isFeasible()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < numOfSolution; i++) {
            System.out.println(Arrays.toString(objectives[i]));
        }
        return str;
    }
}
