import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

public class TestSftp {
    private static Session sshSession;
    public ChannelSftp connectSFTPServer(String ip, int port, String username, String password)throws Exception{
        JSch jsch = new JSch();
        sshSession = jsch.getSession(username,ip,port);
        sshSession.setPassword(password);
        Properties sshConfig = new Properties();
        sshConfig.put("StrictHostKeyChecking", "no");
        sshConfig.put("userauth.gssapi-with-mic", "no");
        sshSession.setConfig(sshConfig);
        sshSession.connect();
        ChannelSftp sftp = (ChannelSftp)sshSession.openChannel("sftp");
        sftp.connect();
        return sftp;
    }

    public boolean uploadFileSFTP(ChannelSftp sftp,FileInputStream fileInputStream
            ,String fileName,String remotePath)throws Exception{
        if(!dirExist(remotePath,sftp)){
            makeDirectory(remotePath,sftp);
        }
        sftp.cd(remotePath);
        sftp.put(fileInputStream,fileName);
        return true;
    }

    public boolean makeDirectory(String remotePath,ChannelSftp sftp)throws Exception{
        String dirs = remotePath.substring(1);
        String[] dirArr = dirs.split(File.separator);
        String base = "";
        for (String dir : dirArr) {
            base += File.separator + dir;
            if (!dirExist(base, sftp)) {
                sftp.mkdir(base);
            }
        }
        return true;
    }

    public String makeLocalDirectory(String localPath,String fileName)throws Exception{
        File folder = new File(localPath+File.separator+fileName);
        if (!folder.exists()){
            folder.mkdir();
            return folder.getPath();
        }
        return folder.getPath();
    }

    public boolean dirExist(String dirPath,ChannelSftp sftp) throws Exception{
        Vector<?> vector = sftp.ls(dirPath);
        return null != vector;
    }

    public boolean downloadFileSFTP(ChannelSftp sftp,String localPath,String remotePath,String fileName) throws Exception{
        sftp.cd(remotePath);
        File file = new File(localPath+File.separator+fileName);
        if(!file.exists()){
            file.createNewFile();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        sftp.get(fileName,fileOutputStream);
        return true;
    }

    /*public boolean downloadDirectorySFTP(ChannelSftp sftp,String remotePath,String localPath,String fileName) throws Exception{
        File file = new File(remotePath+File.separator+fileName);
        if(file.isFile()){
            sftp.cd(remotePath);
            sftp.lcd(localPath);

            List<String> childDirectories = new LinkedList<>();
            sftp.ls(remotePath, lsEntry -> {
                SftpATTRS attrs = lsEntry.getAttrs();
                if (attrs.isDir()) {
                    childDirectories.add(lsEntry.getLongname());
                }
                else if (attrs.isBlk()) {
                    //Download normal file
                    try {
                        downloadFileSFTP(sftp,remotePath,localPath,fileName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return ChannelSftp.LsEntrySelector.CONTINUE;
            });

            childDirectories.forEach(remote -> {
                //local path 需要添加remote的文件夹的名称
                try {
                    downloadFileSFTP(sftp, remote,localPath+remote.substring(remote.lastIndexOf("/")),file.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return true;
    }*/

    public boolean downloadDirectory(ChannelSftp sftp,String remotePath,String localPath,String fileName)throws Exception{
        sftp.cd(remotePath);
        File file = new File(remotePath,fileName);
        if(file.isDirectory()){
            localPath = makeLocalDirectory(localPath,file.getName());
            File[] files = file.listFiles();
            for (int i = 0; i<files.length; i++){
                if(files[i].isDirectory()){
                    makeLocalDirectory(localPath,files[i].getName());
                    downloadDirectory(sftp,files[i].getParent(),localPath,files[i].getName());
                }else if(files[i].isFile()){
                    downloadFileSFTP(sftp,localPath,files[i].getParent(),files[i].getName());
                }
            }
        }
        return true;
    }

    public boolean deleteFileSFTP(ChannelSftp sftp,String remotePath,String pattern) throws Exception{
        File file = new File(remotePath+File.separator+pattern);
        sftp.cd(remotePath);
        if(file.isDirectory()){
            if(file.list().length>0){
                return false;
            }else{
                sftp.rmdir(pattern);
            }
        }else{
            sftp.rm(pattern);
        }
        return true;
    }

    public static void main(String[] args) throws Exception{
        TestSftp test = new TestSftp();
        ChannelSftp sftp = test.connectSFTPServer("localhost",22,"wangchenxu","wangchenxu.");
        //File file = new File("/home/wangchenxu/package-lock.json");
        //FileInputStream fileInputStream = new FileInputStream(file);
        //上传
        //test.uploadFileSFTP(sftp,fileInputStream,"package-lock.json","/home/wangchenxu/remote");
        //下载
        /*boolean bool = test.downloadFileSFTP(sftp,"/home/wangchenxu","/home/wangchenxu/remote/temp","progit.pdf");
        System.out.println(bool);*/
        //下载文件夹，还有些问题：创建文件夹时会级联创建父级文件夹导致内存泄漏
        test.downloadDirectory(sftp,"/home/wangchenxu/testdown","/home/wangchenxu/模板","project");
        //只能删除空文件夹，文件夹内有内容不能删除
        //test.deleteFileSFTP(sftp,"/home/wangchenxu/模板","remote");
        sftp.disconnect();
        sshSession.disconnect();
    }
}
