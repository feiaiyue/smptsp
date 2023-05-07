package prob;

import comm.Base;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NSGAII {
    Instance instance;
    int timeLimit;
//    parameters
//    种群大小
    int populationSize;
    // 最大迭代次数
    int maxGeneration;
    // 交叉概率
    double crossoverProbability;
    // 变异概率
    double mutationProbability;
    int tournamentSize; // 锦标赛选择的个数
    int numObjectives; // 目标个数
    List<Individual> population; // 种群
    List<Individual> offspringPopulation; // 子代种群
    Random rand = new Random(Base.SEED);
//    result
    public double timeCost;
    public boolean feasible;
    Solution sol; // TODO: 2023/5/7 sol没有记录
    long s0;


    public NSGAII(Instance instance) {
        this.instance = instance;
        this.sol = new Solution(instance);
        this.populationSize = 20;
        this.maxGeneration = 10;
        this.crossoverProbability = 0.9;
        this.mutationProbability = 0.5;
        this.tournamentSize = 10;
        this.numObjectives = 4;
        this.population = new ArrayList<>();
        this.offspringPopulation = new ArrayList<>();

    }

    public List<Individual> run() {
        s0 = System.currentTimeMillis();
        this.population = greedySortInitializePopulation();
        evaluatePopulation(population);
        for (int iter = 0; iter < maxGeneration; iter++) {
            List<Individual> offspringPopulation = new ArrayList<>();
            for (int h = 0; h < populationSize; h++) {
                Individual parent1 = tournamentSelection(population, tournamentSize);
//                test:
//                 System.out.println("parent1Size" + parent1.cars.size()); // TODO: 2023/5/7 cars的数量是固定的
                Individual parent2 = tournamentSelection(population, tournamentSize);
                Individual offspring1 = new Individual();
                Individual offspring2 = new Individual();
                crossover(parent1, parent2, offspring1, offspring2); // TODO: 2023/5/7 你忘了crossover probability了
                if (!offspring1.cars.isEmpty()) {
                    mutate(offspring1);
                }
                if (!offspring2.cars.isEmpty()) {
                    mutate(offspring2);
                }

                offspringPopulation.add(offspring1);
                offspringPopulation.add(offspring2);
                offspring1.isSizeFeasible(instance);
                offspring2.isSizeFeasible(instance);
                h += 2;
            }
            evaluatePopulation(offspringPopulation);
            List<Individual> union = new ArrayList<>(population);
            union.addAll(offspringPopulation);

            List<List<Individual>> fronts = fastNonDominatedSort(union);
            population.clear();
            int i = 0; // 层数
            while (population.size() + fronts.get(i).size() <= populationSize) {
                calculateCrowdingDistance(fronts.get(i));
                population.addAll(fronts.get(i));
                i++;
            }
            if (!population.isEmpty()) {
                calculateCrowdingDistance(fronts.get(i));
                Collections.sort(fronts.get(i), Comparator.comparingDouble((Individual individual) -> -individual.crowdingDistance));
                population.addAll(fronts.get(i).subList(0, populationSize - population.size()));
            }
        }
        timeCost = Base.getTimecost(s0);
        feasible = sol.isFeasible(sol);
        System.out.println(instance.instName + ", " + sol.toString());
        return population;

    }

// 分成更多的block，每一个block，都是颜色相同、大小<=5

    public static List<List<Car>> splitCarsSameColor(List<Car> carsSameColor) {
        List<List<Car>> savedCarsSameColor = new ArrayList<>();
        if (carsSameColor.isEmpty()) {
            return savedCarsSameColor;
        }
        List<Car> temp1 = new ArrayList<>();
        for (int i = 0; i < carsSameColor.size() - 1; i++) {
            temp1.add(carsSameColor.get(i));
            if (!carsSameColor.get(i).bodyColor.equals(carsSameColor.get(i + 1).bodyColor)) {
                savedCarsSameColor.add(temp1);
                temp1 = new ArrayList<>();
            }
        }
//        要把最后一个加进去
        temp1.add(carsSameColor.get(carsSameColor.size() - 1));
        savedCarsSameColor.add(temp1);
        List<List<Car>> savedSplitCarsSameColor = new ArrayList<>();
//        拆的更细致，就是五个这种
        for (List<Car>cars : savedCarsSameColor) {
            List<Car> temp = new ArrayList<>();
            for(Car car : cars) {
                temp.add(car);
                if (temp.size() == 5) {
                    savedSplitCarsSameColor.add(temp);
                    temp = new ArrayList<>();
                }
            }
            savedSplitCarsSameColor.add(temp);
        }
        return savedSplitCarsSameColor;
    }
    //返回一个个体，但这里调用完可能只有一个车型的，因为先按照车型分配的。
//    public static Individual emplace(List<List<Car>> splitCarsSameColorCopy, List<Car> carsNotSameColorCopy) {
//        Individual individual = new Individual();
//        List<List<Car>> cars = new ArrayList<>();
//        // 函数结束时最多 5 个颜色相同的在一起
//        for (List<Car> carsSameColor : splitCarsSameColorCopy) {
//            List<Car> temp = new ArrayList<>();
//            for (int i = 0; i < carsSameColor.size(); i++) {
//                temp.add(carsSameColor.get(i));
//                if (temp.size() == 5 || (i == carsSameColor.size() - 1)) {
//                    cars.add(temp);
//                    temp = new ArrayList<>();
//                }
//            }
//        }
//        for (Car car : carsNotSameColorCopy) {
//            List<Car> temp = new ArrayList<>();
//            temp.add(car);
//            cars.add(temp);
//        }
//        long seed = System.currentTimeMillis();
//        Collections.shuffle(cars, rand);
//        for (List<Car> temp : cars) {
//            for (Car car : temp) {
//                individual.cars.add(car);
//            }
//        }
//        return individual;
//    }
    public List<Individual> greedySortInitializePopulation() {
        List<Individual> population = new ArrayList<>();
        // TODO: 2023/5/7 一般不会先生成若干空的，然后填充内容，而是生成一个真的individual就往population内加
        for (int i =0; i < populationSize; i++) {
            population.add(new Individual());
        }
        List<Car> carsAB = instance.cars; // TODO: 2023/5/7 建议不用List，而是指明具体类型（List似乎是个抽象类）
        Collections.sort(carsAB, (Comparator<Car>) (a, b) -> a.type.compareTo(b.type));
        // TODO: 2023/5/7 这个处理不对，不能在instance上进行操作，你可以复制出来再进行操作
        int indexB = -1;
        for (int i = 0; i < carsAB.size(); ++i) {
            if (carsAB.get(i).type.equals("B")) {
                indexB = i;
                break;
            }
        }
        List<Car> carsA = new ArrayList<>(carsAB.subList(0, indexB));
        List<Car> carsB = new ArrayList<>(carsAB.subList(indexB, carsAB.size()));
        List<Car> carsASameColor = new ArrayList<>();
        List<Car> carsBSameColor = new ArrayList<>();
        List<List<Car>> carsANotSameColor = new ArrayList<>();
        List<List<Car>> carsBNotSameColor = new ArrayList<>();
        for (Car car : carsA) {
            List<Car> temp = new ArrayList<>();
            if (!(car.roofColor.equals(car.bodyColor))) {
                temp.add(car);
                carsANotSameColor.add(temp);
            } else {
                carsASameColor.add(car);
            }
        }
        for (Car car : carsB) {
            List<Car> temp = new ArrayList<>();
            if (!(car.roofColor.equals(car.bodyColor))) {
                temp.add(car);
                carsBNotSameColor.add(temp);
            } else {
                carsBSameColor.add(car);
            }
        }
        Collections.sort(carsASameColor, (Comparator<Car>) (a, b) -> a.bodyColor.compareTo(b.bodyColor));
        Collections.sort(carsBSameColor, (Comparator<Car>) (a, b) -> a.bodyColor.compareTo(b.bodyColor));
        List<List<Car>> splitCarsASameColor = splitCarsSameColor(carsASameColor);
        List<List<Car>> splitCarsBSameColor = splitCarsSameColor(carsBSameColor);

        for (Individual individual : population) {
            List<List<Car>> carsANotSameColorCopy = new ArrayList<>(carsANotSameColor);
            List<List<Car>> carsBNotSameColorCopy = new ArrayList<>(carsBNotSameColor);
            List<List<Car>> splitCarsASameColorCopy = new ArrayList<>(splitCarsASameColor);
            List<List<Car>> splitCarsBSameColorCopy = new ArrayList<>(splitCarsBSameColor);
            // long seed = System.currentTimeMillis();
            Collections.shuffle(carsANotSameColorCopy, rand); // TODO: 2023/5/7  SEED 要用固定值，不然结果会变
            Collections.shuffle(carsBNotSameColorCopy, rand);
            Collections.shuffle(splitCarsASameColorCopy,rand);
            Collections.shuffle(splitCarsBSameColorCopy, rand);


            List<List<Car>> combinedCars = Stream.of(carsANotSameColorCopy, carsBNotSameColorCopy,
                            splitCarsASameColorCopy, splitCarsBSameColorCopy)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            // 函数结束时最多 5 个颜色相同的在一起

            // long seed1 = System.currentTimeMillis();
            Collections.shuffle(combinedCars, rand);
            for (List<Car> temp : combinedCars) {
                for (Car car : temp) {
                    individual.cars.add(car);
                }
            }
        }
        return population;
    }


//    public void colorCommonCross(List<Individual> population) {
////    unsigned seed = std::chrono::system_clock::now().time_since_epoch().count();
////    shuffle(population.begin(), population.end(), default_random_engine(seed));
//        for (int i = 0; i < population.size(); i += 2) {
//            for (int j = 0; j != crossTime; ++j) {
//                List<Integer> parent1 = population.get(i).genes;
//                List<Integer> parent2 = population.get(i + 1).genes;
//                // 生成两个要交换位置的随机数 index1, index2
//                int index1 = (int) (Math.random() * parent1.size());
//                // index 落在 [start, end] 区间
//                int start1 = index1, end1 = index1;
//                findStartEnd(parent1, start1, end1);
//
//                int index2 = (int) (Math.random() * parent1.size());
//                int start2 = index2, end2 = index2;
//                findStartEnd(parent2, start2, end2);
//                int len1 = end1 - start1 + 1;
//                int len2 = end2 - start2 + 1;
//                if (len1 > 5) {
//                    // 以 index1 为中心，截取一段
//                    getSubRange(index1, start1, end1);
//                }
//                if (len2 > 5) {
//                    // 以 index2 为中心，截取一段
//                    getSubRange(index2, start2, end2);
//                }
//
//                List<Integer> genes1 = parent1.subList(start1, end1 + 1);
//                HashSet<Integer> genes1Set = new HashSet<>(genes1); // make sure find O(1)
//                List<Integer> genes2 = parent2.subList(start2, end2 + 1);
//                HashSet<Integer> genes2Set = new HashSet<>(genes2); // make sure find O(1)
//                List<Integer> child1 = new ArrayList<>();
//                List<Integer> child2 = new ArrayList<>();
//
//                int insertPos1 = end1;
//                int insertPos2 = end2;
//
//                // 将 genes2 插入 parent1 中
//                for (int k = 0; k < parent1.size(); k++) {
//                    if (!genes2Set.contains(parent1.get(k))) {
//                        // 元素跟 genes2 元素不重复
//                        child1.add(parent1.get(k));
//                    }
//                    if (k == insertPos1) {
//                        // 插入 genes2
//                        child1.addAll(genes2);
//                    }
//                }
//                // 将 genes1 插入 parent2 中
//                for (int k = 0; k < parent2.size(); k++) {
//                    if (!genes1Set.contains(parent2.get(k))) {
//                        // 元素跟 genes2 元素不重复
//                        child2.add(parent2.get(k));
//                    }
//                    if (k == insertPos2) {
//                        // 插入 genes2
//                        child2.addAll(genes1);
//                    }
//                }
//
//                population.get(i).genes = child1;
//                population.get(i + 1).genes = child2;
//            }
//
//            evaluation(population.get(i));
//            evaluation(population.get(i + 1));
//        }
//    }

//    public void findStartEnd(final List<Integer> sequence, int[] start, int[] end) {
//        while (start[0] - 1 >= 0) {
//            carInfo cur = ins.cars.get(sequence.get(start[0]));
//            carInfo prev = ins.cars.get(sequence.get(start[0] - 1));
//            if (prev.checkNextNotContinuesColor(cur)) {
//                // 颜色不连续
//                break;
//            }
//            --start[0];
//        }
//        while (end[0] + 1 <= sequence.size() - 1) {
//            carInfo cur = ins.cars.get(sequence.get(end[0]));
//            carInfo next = ins.cars.get(sequence.get(end[0] + 1));
//            if (cur.checkNextNotContinuesColor(next)) {
//                // 颜色不连续
//                break;
//            }
//            ++end[0];
//        }
//    }


    public static void getSubRange(int index, int[] start, int[] end) {
        // 长度超过 5
        int newStart = index - 2;
        int newEnd = index + 2;
        if (newStart < start[0]) {
            newEnd += (start[0] - newStart);
            newStart = start[0];
        }
        if (newEnd > end[0]) {
            newStart -= (newEnd - end[0]);
            newEnd = end[0];
        }
        start[0] = newStart;
        end[0] = newEnd;
    }



    // 计算种群中每个个体的目标值
    private void evaluatePopulation(List<Individual> population) {
        for (Individual individual : population) {
            individual.evaluate();
            individual.isSizeFeasible(instance);
        }
    }

    // 快速非支配排序，返回很多个前沿面
    private List<List<Individual>> fastNonDominatedSort(List<Individual> population) {
        List<List<Individual>> fronts = new ArrayList<>();
        List<Individual> front = new ArrayList<>();
//        key：Individual支配value：Set<Individual>
        Map<Individual, Set<Individual>> S = new HashMap<>();
//        key:Individual被支配的数量
        Map<Individual, Integer> n = new HashMap<>();
        for (Individual individual : population) {
            S.put(individual, new HashSet<>());
            n.put(individual, 0);
        }
        for (int i = 0; i < population.size(); i++) {
            Individual individual1 = population.get(i);
            for (int j = 0; j < population.size(); j++) {
                if (i == j) {
                    continue;
                }
                Individual individual2 = population.get(j);
                if (individual1.isDominate(individual2) == 1) {
                    S.get(individual1).add(individual2);
                } else if (individual1.isDominate(individual2) == -1) {
                    n.put(individual1, n.get(individual1) + 1);
                }
            }
            if (n.get(individual1) == 0) {
//                无人支配，级别最高，rank=1
                individual1.rank = 1;
                front.add(individual1);
            }
        }
        fronts.add(front);
        int i = 1;
//        计算每一层front的rank
        while (!fronts.get(i - 1).isEmpty()) {
            front = new ArrayList<>();
            for (Individual individual1 : fronts.get(i - 1)) {
                for (Individual individual2 : S.get(individual1)) {
                    n.put(individual2, n.get(individual2) - 1);
                    if (n.get(individual2) == 0) {
                        individual2.rank = i + 1;
                        front.add(individual2);
                    }
                }
            }
            i++;
            fronts.add(front);
        }
        return fronts.subList(0, fronts.size() - 1);
    }

    // 计算拥挤距离
    private void calculateCrowdingDistance(List<Individual> front) {
        int size = front.size();
        if (size == 0) {
            return;
        }
        for (Individual individual : front) {
            individual.crowdingDistance = 0.0;
        }
        for (int i = 0; i < numObjectives; i++) {
            final int index = i;
            front.sort(Comparator.comparingDouble((Individual individual) -> individual.objectives[index]));
            front.get(0).crowdingDistance = Double.POSITIVE_INFINITY;
            front.get(size - 1).crowdingDistance = Double.POSITIVE_INFINITY;
            double fMax = front.get(front.size() - 1).objectives[index];
            double fMin = front.get(0).objectives[index];
            for (int j = 1; j < size - 1; j++) {
                double distance = (front.get(j + 1).objectives[i] - front.get(j - 1).objectives[i]) /
                        (fMax - fMin);
                front.get(j).crowdingDistance += distance;
            }
        }
    }

    // 锦标赛选择
    private Individual tournamentSelection(List<Individual> population, int tournamentSize) {
        List<Individual> candidates = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            candidates.add(population.get(rand.nextInt(population.size())));
        }
//        test
//         System.out.println("candidatesSzie" + candidates.size());
        int index = rand.nextInt(candidates.size()); // TODO: 2023/5/7 tournament 是先随机选一部分，然后从这一部分中取最好的一个；你没有取最好
        // System.out.println("Index" + index);
        Individual temp = candidates.get(index);

        // System.out.println("tempSize" + temp.cars.size());
        if (temp.cars.size() == 0) { // TODO: 2023/5/7 这个不可能会发生
            System.out.println(temp.toString());
        }
        return temp;
//        return candidates.get(rand.nextInt(candidates.size()));
//        return candidates.stream()
//                .max(Comparator.comparingInt(Individual::getRank)
//                        .thenComparingDouble(Individual::getCrowdingDistance))
//                .orElseThrow();
    }


    // 交叉
    private void crossover(Individual parent1, Individual parent2, Individual offspring1, Individual offspring2) {
        // System.out.println("parent2size"+parent2.cars.size());
        int carSize = parent1.cars.size();
        if (rand.nextDouble() < crossoverProbability) {
            int crossoverPoint = rand.nextInt(carSize);
            // for (int i = 0; i < crossoverPoint; i++) {
            //     offspring1.cars.add(parent1.cars.get(i)); // TODO: 2023/5/7 需要注意是浅复制，还是深复制（避免child改变时会影响到parent,反之亦然）
            //     offspring2.cars.add(parent2.cars.get(i));
            //
            // }
            // for (int i = crossoverPoint; i < parent1.cars.size(); i++) {
            //     offspring1.cars.add(parent2.cars.get(i));
            //     offspring2.cars.add(parent1.cars.get(i)); // TODO: 2023/5/7 有bug，存在重复，没有检查已经访问过的cars
            // }

            for (int i = 0; i < crossoverPoint; i++) {
                offspring1.cars.add(parent1.cars.get(i));
                offspring2.cars.add(parent2.cars.get(i));
            }
            for (int h = 0; h < carSize; h++) {
                Car car = parent2.cars.get(h);
                if (!offspring1.contains(car)) {
                    offspring1.cars.add(car);
                }
            }
            for (int h = 0; h < carSize; h++) {
                Car car = parent1.cars.get(h);
                if (!offspring2.contains(car)) {
                    offspring2.cars.add(car);
                }
            }
        } else {
            for (int i = 0; i < carSize; i++) {
                offspring1.cars.add(parent1.cars.get(i));
                offspring2.cars.add(parent2.cars.get(i));
            }
        }
        offspring1.isSizeFeasible(instance);
        offspring2.isSizeFeasible(instance);
    }

//    随机选取一个序列进行倒序
    public void mutate(Individual individual) {
        if (rand.nextDouble() < mutationProbability) {
            if (individual.cars.size() == 0) {
                System.out.println(individual.cars);
            }
            int mutationPoint1 = rand.nextInt(individual.cars.size());
            int mutationPoint2 = rand.nextInt(individual.cars.size());
            int start = Math.min(mutationPoint1, mutationPoint2);
            int end = Math.max(mutationPoint1, mutationPoint2);
            Collections.reverse(individual.cars.subList(start, end));
            individual.isSizeFeasible(instance);
        }
    }

    public String makeCsvItem() {
        String sb = new String();
        for (int i = 0; i < instance.cars.size(); i++) {
            sb = "varaible" + i;
        }
        return sb;
    }
}





