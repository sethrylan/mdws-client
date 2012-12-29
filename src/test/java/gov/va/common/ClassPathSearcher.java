package gov.va.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClassPathSearcher {

    private static Log log = LogFactory.getLog(ClassPathSearcher.class);

    public static Map<String, InputStream> findFilesInClassPath(String fileNamePattern) {
        Map<String, InputStream> result = new TreeMap<String, InputStream>();
        String classPath = System.getProperty("java.class.path");
        String[] pathElements = classPath.split(System.getProperty("path.separator"));
        for (String element : pathElements) {
            log.debug(element);
            try {
                File newFile = new File(element);
                if (newFile.isDirectory()) {
                    result.putAll(findResourceInDirectory(newFile,fileNamePattern));
                } else {
                    result.putAll(findResourceInFile(newFile, fileNamePattern));
                }
            } catch (IOException e) {
                log.error("Exception:", e);
            }
        }
        return result;
    }

    private static Map<String, InputStream> findResourceInFile(File resourceFile,String fileNamePattern) throws IOException {
        Map<String, InputStream> result = new TreeMap<String, InputStream>();
        if (resourceFile.canRead() && resourceFile.getAbsolutePath().endsWith(".jar")) {
            log.debug("jar file found: " + resourceFile.getAbsolutePath());
            JarFile jarFile = new JarFile(resourceFile);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry singleEntry = entries.nextElement();
                log.debug("jar entry: " + singleEntry.getName());
                if (singleEntry.getName().matches(fileNamePattern)) {
                    result.put(jarFile.getName() + "/" + singleEntry.getName(), jarFile.getInputStream(singleEntry));
                }
            }
        }
        return result;
    }

    private static Map<String, InputStream> findResourceInDirectory(File directory,String fileNamePattern) throws IOException {
        Map<String, InputStream> result = new TreeMap<String, InputStream>();
        File[] files = directory.listFiles();
        for (File currentFile : files) {
            log.debug("current file name: " + currentFile.getAbsolutePath());
            if (currentFile.getAbsolutePath().matches(fileNamePattern)) {
                result.put(currentFile.getAbsolutePath(), new FileInputStream(currentFile));
            } else if (currentFile.isDirectory()) {
                result.putAll(findResourceInDirectory(currentFile,fileNamePattern));
            } else {
                result.putAll(findResourceInFile(currentFile, fileNamePattern));
            }
        }
        return result;
    }
    
    
    public static List<String> getResourcesFromClassPathDirectory(final String directory,final Pattern pattern){
        final ArrayList<String> retval = new ArrayList<String>();
        Enumeration<URL> en = null;
        try {
            en = ClassPathSearcher.class.getClassLoader().getResources(directory);
        } catch (IOException ex) {
            Logger.getLogger(ClassPathSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (en.hasMoreElements()) {
            URL url = en.nextElement();
            File root = null;
            try {
                root = new File(url.toURI());
            } catch (URISyntaxException ex) {
                Logger.getLogger(ClassPathSearcher.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            File[] files = root.listFiles();
            for(File f: files) {
                System.out.println(f.isFile() + " " + f.getAbsolutePath());
                if(f.isDirectory()){
                    retval.addAll(getResourcesFromClassPathDirectory(directory + "/" + f.getName(), pattern));
                } else {
                    try{
                        final String fileName = f.getCanonicalPath();
                        final boolean accept = pattern.matcher(fileName).matches();
                        if(accept){
                            retval.add(fileName);
                        }
                    } catch(final IOException e){
                        throw new Error(e);
                    }
                }

            }
        }
        return retval;
    }
}
