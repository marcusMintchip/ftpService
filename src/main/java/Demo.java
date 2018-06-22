import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Demo {

    public static void getTree(File file){
        File[] files = file.listFiles();
        List list = Arrays.asList(files);
        Iterator it = list.iterator();
        while(it.hasNext()){
            File itfile = new File(it.next().toString());
            if(itfile.isDirectory()){
                getTree(itfile);
            }else{
                System.out.println(it.next().toString());
            }
        }
    }

    public static void getAllFile(File file){
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(int i = 0;i<files.length;i++){
                getAllFile(files[i]);
            }
        }else if (file.isFile()){
            System.out.println(file.getPath());
        }
    }

    public static void main(String[] args) throws Exception{
        /*File file = new File("/home/wangchenxu/下载/demo/convert/java.txt");
        if(!file.exists()){
            File folder = new File(file.getParent());
            if(!folder.exists()){
                folder.mkdirs();
            }
            file.createNewFile();
        }*/
        /*String localPath = "/home/marcus";
        String remotePath = "/remote/wangchenxu";
        String localdir = localPath+"/remote/wangchenxu/download/java.txt".split(remotePath)[1];
        System.out.println(localdir);*/
        File file = new File("/home/wangchenxu/project");
        getAllFile(file);
    }
}
