package prob;

import java.util.List;

public class Instance implements Comparable<Instance>{
    public String instName;
    public List<Car> cars; // TODO: 2023/5/7 不变的东西用固定数组，需要变化的东西采用动态数据

    public Instance(Data data) {
        this.instName = data.instName;
        this.cars = data.cars;
    }
    @Override
    public int compareTo(Instance other) {
        return this.cars.size() - other.cars.size();
    }
}
