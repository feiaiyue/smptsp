package prob;

import comm.Param;
import comm.Base;
import comm.ProblemIO;

import java.io.File;
import java.util.Arrays;


public class AlgoRunner {


    public void run(String[] args) {
        Param.test = false;
        Param.algoName = "NSGA-II";
        Param.timeLimit = 10;

        readParams(args);
        ProblemIO.makeResultFolders();
        ProblemIO.writeCSV(makeCSVTitle(), true);
        Instance[] instances = readInsts();
        switch (Param.algoName) {
            case "NSGA-II":
                runNSGAII(instances);
                break;
            default:
                System.err.println("No such method");
                break;
        }
    }

    void readParams(String[] args) {
        Param.problemName = "carSequencing";
        Param.dataPath = "./data";
        //保存结果的文件夹
        Param.resultPath = "./result";
        //结果下面跑不同算法的文件夹。代表着algo文件夹在result下面
        Param.algoPath = Param.resultPath + "/" + Param.algoName;
        //因为这里存放的是路径，所以要先是algoPath,并且这个algopath里面包括的algoname是不会出现在名字里的
        Param.csvPath = Param.algoPath + "-" + Param.problemName + "-"
                 + Base.getCurrentTime() + ".csv";
        Param.solPath = Param.algoPath + "/sol";
        Param.instanceSuffix = ".csv";
    }

    boolean belongToTestSet(Instance inst) {
        return true;
    }


    Instance[] readInsts() {
        File[] files = ProblemIO.getDataFiles();
        Instance[] instances = new Instance[files.length];
        for (int i = 0; i < files.length; i++) {
            String[] strings = ProblemIO.read(files[i]);
            Data instData = new Data(files[i].getName(), strings);
            instances[i] = new Instance(instData);
        }
        Arrays.sort(instances);
        return instances;
    }

    String makeCSVTitle() {
        String str = "instName, numOfSolution, numOfCars, feasible, timeLimit, timeCost, obj1, obj2, obj3, obj4";
//        switch (Param.algoName) {
//            case "grasp":
//                str += "ub, lb, maxIter, callmax, nNonimprovemax, t_best, nIter_best, nIter";
//                break;
//        }
        return str;
    }

    void writeResult(String instName, String csv, Solution sol) {
        ProblemIO.writeCSV(csv, false);
//        if (!feasible || !Param.algoName.equals("bnb")) {
//            return;
//        }
        String solPath = Param.algoPath + "/sol/" + Param.problemName + "_sol_" + instName + "_"  + ".csv";
        File solFile = new File(solPath);
        ProblemIO.write(solFile, sol.toString());
    }

    void runNSGAII(Instance[] instances) {
        NSGAII nsgaii = new NSGAII(instances[99]);
        nsgaii.run();
        writeResult(instances[99].instName, nsgaii.makeCsvItem(), nsgaii.sol);
//        for (Instance inst : instances) {
//            if (!belongToTestSet(inst)) {
//                continue;
//            }
//            Base.renewRandom();
//            NSGAII nsgaii = new NSGAII(inst);
//            nsgaii.run(Param.timeLimit);
//
//            writeResult(inst.instName, nsgaii.makeCsvItem(), nsgaii.sol);
//        }
    }
}
