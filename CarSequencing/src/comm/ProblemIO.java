package comm;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class ProblemIO {

    public static void makeResultFolders() {
        File resultFolder = new File(Param.resultPath);
        if (!resultFolder.exists() || !resultFolder.isDirectory()) {
            resultFolder.mkdir();
        }
        File algoFolder = new File(Param.algoPath);
        if (!algoFolder.exists() || !algoFolder.isDirectory()) {
            algoFolder.mkdir();
        }
        File solFolder = new File(Param.algoPath + "/sol");
        if (solFolder.exists() || !solFolder.isDirectory()) {
            solFolder.mkdir();
        }
    }
/**
 * text：文本文件，要写进来的csv文件
 * isTitle:是否是标题
 */
    public static void writeCSV(String text, boolean isTitle) {
        File csvFile = new File(Param.csvPath);
        try {
            if (!csvFile.exists()) {
                csvFile.createNewFile();
                write(csvFile, text);
            } else {
                if (!isTitle) {
                    append(csvFile, text);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


//    这个write会覆盖，直接重写的这种
    public static void write(File file, String text) {
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(text + "\r\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    会在文本内容之后进行追加，和write并不完全一样

    public static void append(File file, String text) {
        try {
            FileWriter fw = new FileWriter(file, true);
//            fw.write(text + "\r\n");
//            macos系统中 用\n当作换行符，windows用\r\n当作换行符
            fw.write(text + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] read(File file) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(System.lineSeparator()).append(s);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString().trim().split(System.lineSeparator());
    }

    public static File[] getDataFiles() {
        File dataFolder = new File(Param.dataPath);
        LinkedList<File> dirList = new LinkedList<>();
        ArrayList<File> fileList = new ArrayList<>();
        dirList.add(dataFolder);
        while (!dirList.isEmpty()) {
            File dir = dirList.remove();
            dirList.addAll(Arrays.asList(dir.listFiles(file -> file.isDirectory())));
            File[] files = dir.listFiles((_dir, name) -> {
                if (!Param.instanceSuffix.equals("") && !name.endsWith(Param.instanceSuffix)) {
                    return false;
                }
                if (!Param.instancePrefix.equals("") && !name.startsWith(Param.instancePrefix)) {
                    return false;
                }
                return true;
            });
            fileList.addAll(Arrays.asList(files));
        }
        return fileList.toArray(new File[fileList.size()]);
    }
}
