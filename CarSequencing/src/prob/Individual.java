package prob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//定义一个个体/染色体。
public class Individual{
    List<Car> cars; // TODO: 2023/5/7 Car尽量用基本数据类型替代对象
    //这个染色体属于第几rank,越小的是1；
    int rank;
    double[] objectives;
    //拥挤度的计算
    double crowdingDistance;
    public int dominatedByCount; // 被支配次数
    public List<Individual> dominatingIndividuals; // 支配个体
    // TODO: 2023/5/7 一般不会把dominated individuals放在individual内，内容太多，冗余


    public int isDominate(Individual other) {

        boolean flag1 = true;
        boolean flag2 = false;
//        this dominates other
        for (int i = 0; i < objectives.length; i++) {
//            for each i : this.fitness[i] <= other.fitness[i] 存在一个不满足 直接false break
            if (this.objectives[i] > other.objectives[i]) {
                flag1 = false;
                break;
            }
//            there exists i : this.fitness[i] < other.fitness[i]
            if (this.objectives[i] < other.objectives[i]) {
                flag2 = true;
                break;
            }
        }
        if (flag1 && flag2) {
            return 1;
        }
        flag1 = true;
        flag2 = false;
//        other dominates this
        for (int i = 0; i < objectives.length; i++) {
//            for each i : other.fitness[i] <= fitness[i] 存在一个不满足 直接false break
            if (other.objectives[i] > this.objectives[i]) {
                flag1 = false;
                break;
            }
//            there exists i : other.fitness[i] < this.fitness[i]
            if (other.objectives[i] < this.objectives[i]) {
                flag2 = true;
                break;
            }
        }
        if (flag1 && flag2) {
            return -1;
//            当两个Individual的rank相同时
        } else {
            return 0;
        }



    }
//    生成的个体不满足不超过五个颜色连续的硬约束，直接删除
    public boolean isFeasible() {
        int colorCommonNum = 1;
        List<Car> cars1 = cars;
        for (int i = 0; i < cars1.size(); i++) {
            if ((cars1.get(i).roofColor.equals(cars1.get(i).bodyColor)) &&
                    (cars1.get(i).bodyColor.equals(cars1.get(i + 1).roofColor)) &&
                    (cars1.get(i + 1).roofColor.equals(cars1.get(i + 1).bodyColor))) {
                colorCommonNum++;
            }
            if (colorCommonNum > 5) {
                return false;
            }
        }
        return true;
    }
    public void initialize() {
        Collections.shuffle(cars);
    }
//    todo：
//    适应度函数：四个目标值
    public void evaluate() {
        List<Car> cars1 = cars;
//        先把颜色进行处理
//        目标一：车型切换次数，越少越好。
        objectives[0] = 0;
//        放第i个切换位置（i-1）车辆的carNo
        List<Integer> k1 = new ArrayList<>();
//        第i个切换位置的设备的切换耗时
        List<Integer> t11 = new ArrayList<>();
//        第i个切换位置前的连续车辆数量
        List<Integer> m1 = new ArrayList<>();
        int mTemp = 0;
        for (int i = 1; i < cars1.size(); i++) {
            if ((cars1.get(i).type.equals(cars1.get(i - 1).type))) {
                mTemp++;
            } else {
                k1.add(cars1.get(i).carNo);
                m1.add(mTemp);
                t11.add(Math.max(0, 30 * 60 - 80 * mTemp));
                mTemp = 0;
            }
        }
        int typeChangeNum = k1.size();
        objectives[0] = typeChangeNum;
//        时间一：焊装车间总时间
        int t1 = 0;
        t1 = 80 * cars1.size();
        for (int i = 0; i < t11.size(); i++) {
            t1 += t11.get(i);
        }

        //        目标二：不仅仅是喷头的切换次数。
        objectives[1] = 0;
        int colorCommonTemp = 0;

//        第i组连续喷涂车辆组车辆数
//        这一部分可能不对，但是还是要在想象
        List<Integer> k2 = new ArrayList<>();
        for (int i = 0; i < cars1.size() - 1; i++) {
            if ((cars1.get(i).roofColor.equals(cars1.get(i).bodyColor))) {
                colorCommonTemp++;
                if (!cars1.get(i).bodyColor.equals(cars1.get(i + 1).roofColor)) {
                    k2.add(colorCommonTemp);
                    colorCommonTemp = 0;
                } else {
                    continue;
                }
            } else {
                k2.add(colorCommonTemp);
                colorCommonTemp = 0;
            }
        }
        for (int i = 0; i < k2.size(); i++) {
            objectives[1] += Math.sqrt(5.0 - k2.get(i));
        }
//        时间二：涂装车间的总时间
        int t2 = 0;
        int colorChangeNum = 0;
        for (int i = 0; i < cars1.size() - 1; i++) {
            if (!(cars1.get(i).roofColor.equals(cars1.get(i).bodyColor)) ||
                    !(cars1.get(i).bodyColor.equals(cars1.get(i + 1).roofColor))) {
                colorChangeNum++;
            }
        }
        t2 += (40 + 40) * cars1.size() + 80 * colorChangeNum;


        //        目标三：四驱连放数
        objectives[2] = 0;
//        不符合要求的四驱连放组合 越好越好，但是越长肯定越不好，所以衡量的时候，也要考虑到长度，而不仅仅是出现频率
        List<Integer> k3 = new ArrayList<>();
        int fourWheelContinuousNum = 0;
        for (int i = 0; i < cars1.size(); i++) {
            if (cars1.get(i).transmission.equals("两驱")) {
                if (fourWheelContinuousNum != 2 && fourWheelContinuousNum != 3) {
                    k3.add(fourWheelContinuousNum);
                }
                fourWheelContinuousNum = 0;
            } else {
                fourWheelContinuousNum++;
            }
        }
        for (int i = 0; i < k3.size(); i++) {
            objectives[2] += k3.get(i);
        }
        objectives[2] = objectives[2] / cars1.size();
//        时间三：总装车间总时间；
        int t3 = 80 * cars1.size();

//        目标四：所有的时间总和
        int t = t1 + t2 + t3;
        objectives[3] = t;
    }

////        目标一：车型的不同以及计算整个焊装时间
//        for (int i = 1; i < cars1.size(); i++) {
//            t1 += 80;
//            sameEquipmentUseTime += 80;
////            目标一：车型的不同
//            if (!(cars1.get(i).type.equals(cars1.get(i - 1).type))) {
//                typeChangeNum++;
//                if (sameEquipmentUseTime < 30 * 60) {
//                    t1 += 30 * 60;
//                } else {
//                    t1 += 80;
//                }
////                不管怎么样，只要车型变化，同一设备使用时间都必须清零。
//                sameEquipmentUseTime = 0;
//            } else {
//                sameEquipmentUseTime += 80;
//                t1 += 80;
//            }
//        }
////            目标二：颜色的不同
////            这里不用考虑连续五辆的问题，因为这个的话，是不满足约束的，是不正确的
//        for (int i = 0; i < cars1.size(); i++) {
//            if (!(cars1.get(i).roofColor.equals(cars1.get(i).bodyColor)) ||
//                    !(cars1.get(i).bodyColor.equals(cars1.get(i + 1).roofColor))) {
//                colorChangeNum++;
//            }
//        }
//        t2 += (40 + 40) * cars1.size() + 80 * colorChangeNum;
//
////        目标三：两个四驱连放的次数
//        int j = 0;
//        for (int i = j; i < cars1.size(); i++) {
//            if ((cars1.get(i).transmission.equals("四驱")) &&
//                    (cars1.get(i + 1).transmission.equals("四驱")) &&
//                    (cars1.get(i + 2).transmission.equals("两驱"))) {
//                twoFourWheelContinuousNum++;
//                j = i + 3;
//            }
//        }
//        //            目标三：三个四驱连放的次数
//        int k = 0;
//        for (int i = k; i < cars1.size(); i++) {
//            if ((cars1.get(i).transmission.equals("四驱")) &&
//                    (cars1.get(i + 1).transmission.equals("四驱")) &&
//                    (cars1.get(i + 2).transmission.equals("四驱")) &&
//                    (cars1.get(i + 3).transmission.equals("两驱"))) {
//                threeFourWheelContinuousNum++;
//                k = i + 4;
//            }
//        }
//        t3 += 80 * cars1.size();
//        objectives[0] = typeChangeNum;
//        objectives[1] = colorChangeNum;
//        objectives[2] = threeFourWheelContinuousNum + twoFourWheelContinuousNum;
//        objectives[3] = t1 + t2 +t3;
//
//    }

    public Individual(Instance instance) {
        cars = instance.cars;
    }
    public Individual() {
        this.cars = new ArrayList<>();
        this.dominatedByCount = 0;
        this.dominatingIndividuals = new ArrayList<>();
        this.objectives = new double[]{0, 0, 0, 0};
    }
    public Individual(List<Car> cars) {
        this.cars = cars;
    }


    public int getRank() {
        return rank;
    }

    public double getCrowdingDistance() {
        return crowdingDistance;
    }

    boolean isSizeFeasible(Instance inst) {
        if (cars.size() == inst.cars.size())
            return true;
        else {
            System.err.println("size is infeasible");
            return false;
        }
    }

    boolean contains(Car car) {
        return cars.contains(car);
    }

    @Override
    public String toString() {
        return Arrays.toString(objectives) + "|" + cars.size() + ", " + cars.toString();
    }
}
