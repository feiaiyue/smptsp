package prob;

import java.util.ArrayList;
import java.util.List;

//Data代表原始数据，基本不处理，只读入
public class Data {
//    算例名字
    public String instName;
//    序列的长度，数据的长度
    public int N;
    List<Car> cars;
////    汽车编号
//    public int[] carNo;
////    汽车类型
//    public String[] type;
////    车身颜色
//    public String[] bodyColor;
////    车顶颜色
//    public String[] roofColor;
////    材料编号
//    public String[] materialNo;
////    发动机
//    public String[] engine;
////    变速器
//    public String[] transmission;

//    已知文件名，和文件内容，文件内容以String的形式进行存储
    public Data(String fileName, String[] text) {
        cars = new ArrayList<>(text.length );
        String[] s;
        this.instName = fileName.split("\\.")[0];
        for (int line = 1; line < text.length; line++) {
            s = text[line].trim().split(",");
            int carNo = Integer.parseInt(s[0]);
            String type = s[1];
            String bodyColor = s[2];
            String roofColor = s[3];

            if ("无对比颜色".equals(roofColor)) {
                roofColor = bodyColor;
            }

            String materialNo = s[4];
            String engine = s[5];
            String transmission = s[6];
            Car car = new Car(carNo, type, bodyColor, roofColor, materialNo, engine, transmission);
            cars.add(car);
        }
    }
}
