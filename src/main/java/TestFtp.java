import com.jcraft.jsch.ChannelSftp;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import sun.net.ftp.FtpClient;

import java.io.*;
import java.util.*;

public class TestFtp {
    List<String> filePathList = new ArrayList<String>();

    public FTPClient connectFTPServer(String ip, int port, String username, String password)throws Exception{
        FTPClient ftpClient = new FTPClient();
        boolean result = false;
        ftpClient.connect(ip,port);
        //System.out.println(ftpClient.getReplyCode());
        if(ftpClient.isConnected()){
            boolean login = ftpClient.login(username,password);
            if(login){
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
                conf.setServerLanguageCode("zh");
                result = true;
            }
        }
        return result ? ftpClient : null;
    }

    public boolean uploadFile(FTPClient client, FileInputStream fileInputStream
            , String remotePath, String fileName) throws Exception{
        //System.out.println(client.printWorkingDirectory());
        if(!client.changeWorkingDirectory(remotePath.substring(1))){
            boolean flag = makeDirectory(remotePath,client);
        }
        client.changeWorkingDirectory(remotePath.substring(1));
        return client.storeFile(fileName,fileInputStream);
    }

    public FileOutputStream getOutputStream(String localPath,String fileName) throws Exception{
        File file = new File(localPath,fileName);
        if(!file.exists()){
            File folder = new File(file.getParent());
            if(!folder.exists()){
                folder.mkdirs();
            }
            file.createNewFile();
        }
        return new FileOutputStream(file);
    }

    public void downloadFileResume(FTPClient client,String remotePath,String localPath,String fileName)throws Exception{
        FTPFile[] files = client.listFiles(remotePath);
        if(files.length<1){
            System.out.println("file not exist!!!");
        }
        long size = files[0].getSize();
        File file = new File(localPath,fileName);
        file.createNewFile();
        if(file.exists()){
            long length = file.length();
            if(length>=size){
                System.out.println("download file success");
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file,true);
            client.setRestartOffset(1024);
            InputStream inputStream = client.retrieveFileStream(fileName);
            byte[] bytes = new byte[1024];
            long step = size / 100;
            long process = length / step;
            int c;
            while ((c=inputStream.read(bytes))!=-1){
                fileOutputStream.write(bytes,0,c);
                length+=c;
                long nowProcess = length / step;
                if(nowProcess>process){
                    process = nowProcess;
                    if(process%10==0){
                        System.out.println("下载进度："+process);
                    }
                }
                inputStream.close();
                fileOutputStream.close();
                boolean isFinish = client.completePendingCommand();
                if(isFinish){
                    System.out.println("下载完成");
                }else{
                    System.out.println("下载失败");
                }
            }
        }
    }

    public boolean makeDirectory(String path,FTPClient client)throws Exception{
        String dirs = path.substring(1, path.length());
        String[] dirArr = dirs.split(File.separator);
        String base = "";
        for (String dir : dirArr) {
            base += dir;
            if (!client.changeWorkingDirectory(base)) {
                client.makeDirectory(base);
            }
            base+=File.separator;
        }
        return true;
    }

    private boolean _downloadFile(FTPClient client, String fileName, FileOutputStream fileOutputStream, String remotePath) throws Exception{
        if(client.changeWorkingDirectory(remotePath)){
            //client.setRestartOffset(1024);
            client.remoteRetrieve(fileName);
            FileInputStream fileInputStream = new FileInputStream(new File(""));
            fileInputStream.skip(12311L);
            return client.retrieveFile(fileName,fileOutputStream);

        }else{
            return false;
        }
    }

    public boolean downloadFile(FTPClient client,String fileName,String localPath,String remotePath)throws Exception{
        if(client.changeWorkingDirectory(remotePath)){
            File file = new File(remotePath,fileName);
            if(file.isDirectory()){
                localPath = makeLocalDirectory(localPath,file.getName());
                File[] files = file.listFiles();
                for (int i = 0;i<files.length;i++){
                    if(files[i].isDirectory()){
                        makeLocalDirectory(localPath,files[i].getName());
                        downloadFile(client,files[i].getName(),localPath,files[i].getParent());
                    }else if(files[i].isFile()){
                        FileOutputStream outputStream = getOutputStream(localPath,files[i].getName());
                        _downloadFile(client,files[i].getName(),outputStream,files[i].getParent());
                    }
                }
            }else  if (file.isFile()){
                FileOutputStream outputStream = getOutputStream(localPath,fileName);
                _downloadFile(client,fileName,outputStream,remotePath);
            }else {return false;}
            return true;
        }
        return false;
    }

    public void getAllFile(File file){
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(int i = 0;i<files.length;i++){
                getAllFile(files[i]);
            }
        }else if (file.isFile()){
            System.out.println(file.getPath());
        }
    }

    /*public boolean downloadDirectory(FTPClient client,String fileName,String localPath,String remotePath)throws Exception{
        filePathList = getFilePaths(client,remotePath,fileName);
        for (String path:filePathList) {
            String localFilePath = localPath+path.split(remotePath)[1];
            FileOutputStream outputStream = getOutputStream(localFilePath);
            downloadFile(client,path.substring(path.lastIndexOf("/")+1)
                    ,outputStream,path.substring(0,path.lastIndexOf("/")));
        }
        return true;
    }*/

    public String makeLocalDirectory(String localPath,String fileName)throws Exception{
        File folder = new File(localPath+File.separator+fileName);
        if (!folder.exists()){
            folder.mkdir();
            return folder.getPath();
        }
        return folder.getPath();
    }

    public File createFile(String localPath,String fileName) throws Exception{
       File file = new File(localPath,fileName);
        if(!file.exists()){
            File folder = new File(file.getParent());
            if(!folder.exists()){
                folder.mkdirs();
            }
            file.createNewFile();
            return file;
        }
        return null;
    }

    public boolean deleteFile(FTPClient client,String fileName,String remotePath) throws Exception{
        if(client.changeWorkingDirectory(remotePath.substring(1))){
            return client.deleteFile(fileName);
        }else{
            return false;
        }
    }

    public boolean moveFile(FTPClient client, String fromPath,String toPath,String fileName) throws Exception{
        if(client.changeWorkingDirectory(fromPath)){
            File file = new File(fromPath,fileName);
            if(file.isDirectory()){
                //client.rename(fileName,toPath+File.separator+fileName);
            }
            return client.rename(fileName,toPath+File.separator+fileName);
        }else {
            return false;
        }
    }

    /*public boolean moveFileFTP(FTPClient client,String fromPath,String toPath,String fileName)throws Exception{
        if(client.changeWorkingDirectory(fromPath)){
            client
        }
    }*/

    public List<String> getFilePaths(FTPClient client,String remotePath,String fileName) throws Exception{
        String[] names = client.listNames(remotePath+File.separator+fileName);

        for (int i=0;i<names.length;i++){
            if(!names[i].contains(".")){
                getFilePaths(client,names[i].substring(0,names[i].lastIndexOf("/")),names[i].substring(names[i].lastIndexOf("/")+1));
            }else {
                filePathList.add(names[i]);
            }
        }
        return filePathList;
    }


    public static void main(String[] args) throws Exception{
        TestFtp test = new TestFtp();
        FTPClient client = test.connectFTPServer("localhost",21,"wangchenxu","wangchenxu.");
        //File file = new File("/home/wangchenxu/下载/Spring Integration in Action.pdf");
        //test upload file
       /* boolean flag = test.uploadFile(client,test.convertFile(file),"/remote/temp","test.pdf");
        System.out.println("upload finish:"+flag);*/
        //test download file
        //test.downloadFile(client,"test.pdf","/home/wangchenxu/文档","/remote/temp");
        //test.deleteFile(client,"test.pdf","/remote/temp");
        //test.downloadDirectory(client,"project","/home/wangchenxu/音乐","/home/wangchenxu");
        //只能移动到平级目录下的文件夹内
        //System.out.println(client.getStatus("/home/wangchenxu"));
        //System.out.println(client.structureMount("/home/wangchenxu"));
         /*boolean bool = test.moveFile(client,"/home/wangchenxu/remote","/home/wangchenxu","temp");
        System.out.println(bool);*/
        //FileOutputStream fileOutputStream = test.convertFile("/home/wangchenxu/convertFile","package-lock.json");
       // test.downloadFile(client,"progit.pdf","/home/wangchenxu/testdown","/home/wangchenxu/文档");
        /*FileOutputStream outputStream = test.getOutputStream("/home/wangchenxu","progit.pdf");
        boolean bool = test._downloadFile(client,"progit.pdf",outputStream,"/home/wangchenxu/remote");
        System.out.println(bool);*/
        test.downloadFileResume(client,"/home/wangchenxu/remote","/home/wangchenxu","progit.pdf");
    }
}
