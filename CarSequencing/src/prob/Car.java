package prob;

//定义car的六个信息
public class Car {
    //编号
    public int carNo;
    //车的类型 A/B
    public String type; // TODO: 2023/5/7 可以用int代替String的地方，建议用int；int能够索引，能够比较，String比较有限
    //车身颜色
    public String bodyColor;
    //车顶颜色，如果车顶颜色为无对比色，则颜色和车身颜色相同
    //先喷车顶，再喷车身
    public String roofColor;
    //物料编号
    public String materialNo;
    //发动机
    public String engine;
    //变速器
    public String transmission;

    public Car(int carNo, String type, String bodyColor, String roofColor, String materialNo, String engine, String transmission) {
        this.carNo = carNo;
        this.type = type;
        this.bodyColor = bodyColor;
        this.roofColor = roofColor;
        this.materialNo = materialNo;
        this.engine = engine;
        this.transmission = transmission;
    }

    @Override
    public int hashCode() {
        return carNo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Car) {
            Car o = (Car) obj;
            return this.carNo == o.carNo;
        }
        return false;
    }

    @Override
    public String toString() {
        String str = carNo + ", " + type + ", " + bodyColor + ", " + roofColor + ", " + engine;
        return str;
    }
}
