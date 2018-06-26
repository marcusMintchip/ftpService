import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.List;

public class SplitFile {
    public static void main(String[] args) throws Exception{
        String fileName = "mongo_3.2.4.tar";
        String path="/home/wangchenxu/testUpload";
        SplitFile split = new SplitFile();
        //split.cutFile(fileName,path);
        split.mergeFile(fileName,path);
    }

    public void cutFile(String fileName,String path)throws Exception{
        File file = new File(path,fileName);
        long lon = file.length()/10L + 1L;
        RandomAccessFile rFileRead = new RandomAccessFile(file,"r");
        byte[] bytes = new byte[1024];
        int len = -1;
        for(int i = 0; i < 10; i++){
            String tempFileName = path+File.separator
                    +fileName.substring(0,fileName.lastIndexOf('.'))+"("+(i+1)+")"
                    +fileName.substring(fileName.lastIndexOf("."));
            File tempFile = new File(tempFileName);
            tempFile.createNewFile();
            RandomAccessFile rFileWrite = new RandomAccessFile(tempFile,"rw");
            while ((len = rFileRead.read(bytes))!=-1){
                rFileWrite.write(bytes,0,len);
                if(rFileWrite.length()>lon){
                    break;
                }
            }
            rFileWrite.close();
        }
        rFileRead.close();
    }

    public void mergeFile(String fileName,String path)throws Exception{
        File file = new File(path,fileName);
        RandomAccessFile targetFile = new RandomAccessFile(file,"rw");
        for(int i = 0;i<10;i++){
            String tempFileName = path+File.separator
                    +fileName.substring(0,fileName.lastIndexOf('.'))+"("+(i+1)+")"
                    +fileName.substring(fileName.lastIndexOf("."));
            File tempFile = new File(tempFileName);
            RandomAccessFile rFileRead = new RandomAccessFile(tempFile,"r");
            byte[] bytes = new byte[(int)rFileRead.length()/10+1];
            int len = -1;
            while ((len=rFileRead.read(bytes))!=-1){
                targetFile.write(bytes,0,len);
            }
            rFileRead.close();
            tempFile.delete();
        }
        targetFile.close();
    }
}
