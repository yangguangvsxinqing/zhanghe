package com.android.systemui;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import android.util.Log;


public class FileOpreateUtils {  
      
  
    public static void copyfile(File fromFile, File toFile,Boolean rewrite ){  
          
        if(!fromFile.exists()){  
            return;  
        }  
          
        if(!fromFile.isFile()){  
            return;  
        }  
        if(!fromFile.canRead()){  
            return;  
        }  
        if(!toFile.getParentFile().exists()){  
            toFile.getParentFile().mkdirs();  
        }  
        if(toFile.exists() && rewrite){  
            toFile.delete();  
        }  
          
          
        try {  
            FileInputStream fosfrom = new FileInputStream(fromFile);  
            FileOutputStream fosto = new FileOutputStream(toFile);  
              
            byte[] bt = new byte[2*1024*1024];  
            int c;  
            while((c=fosfrom.read(bt)) > 0){  
                fosto.write(bt,0,c);  
            }  

            fosfrom.close();  
            fosto.close();  
              
              
        } catch (FileNotFoundException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
          
    }  
  
}
