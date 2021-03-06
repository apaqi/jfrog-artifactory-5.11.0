package org.artifactory.storage.db.random;

import com.beust.jcommander.internal.Lists;

import java.util.List;

/**
 * @author gidis
 */
public class RandomHelpper {
    private static String[] elements = {"a","b","c","d","e","f","g","h","i","j","k","l","m","o","p","q","r","s","t","v","w","x","y","z"};

    public static String resolveRandomPathName() {
        String randomPath= resolveRandomPath();
        String randomName= resolveRandomFileName();
        return randomPath+randomName;
    }

    private static String resolveRandomFileName() {
        return resolveRandomName(3,10) + "." + resolveRandomName(3,3);
    }

    public static String resolveRandomPath() {
        String finalPath="";
        int numberOfPathElements = random(4,6);
        for (int i = 0; i < numberOfPathElements; i++) {
            finalPath += resolveRandomName(1,10)+ "/";
        }
        return finalPath;
    }


    public static List<String> resolveHierarchyRandomPath(int numberOfChildren, int dept, int numberOfRoots) {
        List<String> result = Lists.newArrayList();
        for (int i = 0; i < numberOfRoots; i++) {
            result.addAll(resolveHierarchyRandomPath(numberOfChildren, dept - 1, resolveRandomName()));
        }
        return result;
    }

    public static List<String> resolveHierarchyRandomPath(int numberOfChildren, int dept,String currentPath) {
        List<String> children = Lists.newArrayList();
        if(dept==0){
            return Lists.newArrayList(currentPath);
        }else {
            for (int i = 0; i < numberOfChildren; i++) {
                children.addAll(resolveHierarchyRandomPath(numberOfChildren, dept - 1,
                        currentPath + "/" + resolveRandomName(1, 10)));
            }
        }
        return children;
    }

    public static String resolveRandomName() {
        return resolveRandomName(5,10);
    }

    public static String resolveRandomName(int minChars, int maxChars) {
        int numberOfCharacters = random(minChars,maxChars);
        return resolveRandomName(numberOfCharacters);
    }

    public static String resolveRandomName(int numberOfCharacters) {

        String finalName="";
        for (int i = 0; i < numberOfCharacters; i++) {
            int charPosition = random(24);
            String element = elements[charPosition];
            finalName+=element;
        }
        return finalName;
    }

    public static int random(double maxNumber) {
        return (int)(Math.random()* maxNumber);
    }

    public static int random(int minNumber, int maxNumber) {
        return minNumber + (int)(Math.random()* (maxNumber-minNumber));
    }
}
