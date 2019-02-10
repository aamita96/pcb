package com.sc.fe.analyze.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.sc.fe.analyze.to.FileDetails;
import com.sc.fe.analyze.to.ProjectDetails;

/**
 *
 * @author Hemant
 */
public class CompareUtility {

    private static final String DELIMITER = "~";
    
    private static Set<String> DO_NOT_COMPARE = initDoNotCompare();
  
    //Field names that will not be compared
    private static Set<String> initDoNotCompare() {
		Set<String> set = new HashSet<String>();
    	set.add("version"); set.add("serialVersionUID");
    	set.add("modifiedDate");  set.add("valid"); set.add("createDate");
    	set.add("newProject"); set.add("attachReplace");
		return set;
	}
    
    /**
     * @param newRecord
     * @param oldRecord
     * @return
     */
    public static Map<String, String> fullCompare(ProjectDetails newRecord, ProjectDetails oldRecord) {
    	
    	Map<String, String> differences = new HashMap<String, String>();
//        Map<String, String> regularDifferences = new HashMap<String, String>();
//        Map<String, String> validationDifferences = new HashMap<String, String>();
        if (oldRecord == null ) {
            return new HashMap<String, String>();
        }
        try {
        	differences.putAll( compareObject(newRecord, oldRecord));
        	
        	differences.putAll(compareObjectMaps(newRecord.getTurnTimeQuantity(), oldRecord.getTurnTimeQuantity ()));
            //Comparing the Validation errors with previous validation errors
        	differences.putAll( compareMaps(newRecord.getErrors(), oldRecord.getErrors()));
            //validationDifferences.put("Errors", validationDifferences.remove("tail"));
        	
        	//Compare FileDetails
        	differences.putAll( compareFileDetails( newRecord, oldRecord) );
            

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return differences;
    }
    
    

	/**
     * This method is compare the new Object from the old Object details.
     * @param newFD - the new object details to set
     * @param oldFD - the old object details to set
     * @return the differences after comparing the new object from the old object 
    */
    public static Map<String, String> compareObject(Object newFD, Object oldFD) throws IllegalArgumentException, IllegalAccessException {
        
        //Map: Field name - newValue ~ oldValue
        Map<String, String> differences = new HashMap<String, String>();
        if (newFD == null || oldFD == null) {
            return differences;
        }
        for (Field field : newFD.getClass().getDeclaredFields()) {
        	//Do not compare collections and specific fields
            if (isCollection(field) || DO_NOT_COMPARE.contains( field.getName() ) ) {
                continue;
            }
            // You might want to set modifier to public first (if it is not public yet)
            field.setAccessible(true);
            
            
            Object newVal = field.get(newFD);
            Object oldVal = field.get(oldFD);
            //If both set has value
            if (newVal != null && oldVal != null) {
                if (!Objects.equals(newVal, oldVal)) {
                    differences.put(field.getName(), newVal + DELIMITER + oldVal);
                }
            }
            //Only old set has value
            if( newVal == null && oldVal != null ) {
            	differences.put(field.getName(), "REMOVED" + DELIMITER + oldVal);
            }
            //Only new set has value
            if( newVal != null && oldVal == null ) {
            	differences.put(field.getName(), newVal + DELIMITER + "ADDED");
            }
        }
        return differences;
    }

    /**
     * @param newProject
     * @param oldProject
     * @return
     */
    public static Map<String, String> compareFileDetails(ProjectDetails newProject, ProjectDetails oldProject) {
    	//FileDetail objects are compared if they have same file name
    	Map<String, String> differences = new HashMap<String, String>();
    	Set<String> combinedKeys = new HashSet<String>();
    	Set<String> newFileNameSet = newProject.getAllFileNames();
    	Set<String> oldFileNameSet = oldProject.getAllFileNames();
    	
    	if(newProject != null && newProject.getAllFileNames() != null) {
    		newFileNameSet = newProject.getAllFileNames();
    	}
    	if(oldProject != null && oldProject.getAllFileNames() != null) {
    		oldFileNameSet = oldProject.getAllFileNames();
    	}
    	//Collect all filenames in set for uniqueness
    	combinedKeys.addAll(newFileNameSet);
    	combinedKeys.addAll(oldFileNameSet);
    	
    	//Reading All fileNames 
    	combinedKeys.stream().forEach( fileName -> {
            //Get FileDetail from 2 sets by same filename    
            FileDetails newFD = newProject.getFileDetails(fileName);
            FileDetails oldFD = oldProject.getFileDetails(fileName);
            
            try {
            	//Now compare
                differences.putAll( compare(newFD , oldFD ));
                
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        });

    	return differences;
    }
    
    /**
     *
     * @param newFD - the new FileDetails object details to set
     * @param oldFD - the old FileDetails object details to set
     * @return the differences after comparing the new FileDetails object from the old FileDetails object
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    public static Map<String, String> compare(FileDetails newFD, FileDetails oldFD) throws IllegalArgumentException, IllegalAccessException {
    	Map<String, String> differences = new HashMap<String, String>();
    	
        if (newFD == null) {
        	newFD = new FileDetails();
        }
        if ( oldFD == null) {
        	oldFD = new FileDetails();
        }
        //First compare as simple object
        differences.putAll( compareObject( newFD, oldFD) );
        //Now compare collection attributes for FileDetails 
		differences.putAll( compareMaps(newFD.getAttributes(), oldFD.getAttributes()) );
		newFD.setErrors(differences);
        return differences;
    }
    
    /**
     * @param newMap
     * @param oldMap
     * @return
     */
    public static Map<String, String> compareMaps( Map<String, String> newMap, Map<String, String> oldMap) {
    	Map<String, String> differences = new HashMap<String, String>();
 
    	//Get all keys from both sets
    	Set<String> combinedKeys = new HashSet<String>();
    	if( newMap != null ) {
    		combinedKeys.addAll(newMap.keySet());
    	}
    	if( oldMap != null ) {
    		combinedKeys.addAll(oldMap.keySet());
    	}
        //For each key, compare old and new
        combinedKeys.stream().forEach( key -> {
        	String oldValue = "NA";
        	String newValue = "NA";
        	if( oldMap != null && oldMap.get(key) != null) {
        		oldValue = oldMap.get(key);
        	}
        	if( newMap != null && newMap.get(key) != null) {
        		newValue = newMap.get(key);
        	}
        	
            if (!Objects.equals(oldValue, newValue)) {
                differences.put(key, newValue + DELIMITER + oldValue);
            }
        });
        
    	return differences;
    }
    
    /**
     * @param newMap
     * @param oldMap
     * @return
     */
    public static Map<String, String> compareObjectMaps( Map newMap, Map oldMap) {
    	Map<String, String> differences = new HashMap<String, String>();
 
    	//Get all keys from both sets
    	Set<Object> combinedKeys = new HashSet<Object>();
    	if( newMap != null ) {
    		combinedKeys.addAll(newMap.keySet());
    	}
    	if( oldMap != null ) {
    		combinedKeys.addAll(oldMap.keySet());
    	}
        //For each key, compare old and new
        combinedKeys.stream().forEach( key -> {
        	Object oldValue = "NA";
        	Object newValue = "NA";
        	if( oldMap != null && oldMap.get(key) != null) {
        		oldValue = oldMap.get(key);
        	}
        	if( newMap != null && newMap.get(key) != null) {
        		newValue = newMap.get(key);
        	}
        	
            if (!Objects.equals(oldValue, newValue)) {
                differences.put(String.valueOf(key) , newValue + DELIMITER + oldValue);
            }
        });
        
    	return differences;
    }
    

    /**
     * Check if given field is a collection type of not
     * @param field
     * @return
     */
    private static boolean isCollection(Field field) {
        // TODO Auto-generated method stub
        boolean retVal = false;
        if (Collection.class.isAssignableFrom(field.getType())) {
            return true;
        }
        if (Map.class.isAssignableFrom(field.getType())) {
            return true;
        }
        if (Set.class.isAssignableFrom(field.getType())) {
            return true;
        }
        return retVal;
    }
    
//============================
//    /**
//     * This method is compare the new Object from the old Object of
//     * AdvancedReport Class.
//     *
//     * @param newReport- the new object details to set
//     * @param oldReport- the old object details to set
//     * @return the differences after comparing the new object from the old
//     * object
//     */
//    public static Map<String, String> compare(AdvancedReport newReport, AdvancedReport oldReport) throws IllegalAccessException {
//        Map<String, String> differences = new HashMap<String, String>();
//        if (newReport == null || oldReport == null) {
//            return null;
//        }
////        if (!(newReport.getOdbMatrix() == null || oldReport.getOdbMatrix() == null)) {
////            if (!(newReport.getOdbMatrix().equalsIgnoreCase(oldReport.getOdbMatrix()))) {
////                differences.put("odbMatrix", newReport.getOdbMatrix() + DELIMITER + oldReport.getOdbMatrix());
////            }
////        }
////
////        if (!(newReport.getCustomerInputs() == null || oldReport.getCustomerInputs() == null)) {
////            CustomerInformation newCI = newReport.getCustomerInputs();
////            CustomerInformation oldCI = oldReport.getCustomerInputs();
////            differences.putAll(FileDetailCompareUtility.compareObject(newCI, oldCI));
////        }
//
//        if (!(newReport.getAllFileNames() == null || oldReport.getAllFileNames() == null)) {
//            Set<String> newFileNameSet = newReport.getAllFileNames();
//            Set<String> oldFileNameSet = oldReport.getAllFileNames();
//
//            //Reading All fileNames from new Report object
//            newFileNameSet.stream().forEach(newFileName -> {
//                //Reading All fileNames from old Report object
//                oldFileNameSet.stream().forEach(oldFileName -> {
//
//                    FileDetails newFD = newReport.getFileDetails(newFileName);
//                    FileDetails oldFD = oldReport.getFileDetails(oldFileName);
//
//                    try {
//                        differences.putAll(FileDetailCompareUtility.compareObject(newFD, oldFD));
//                        //To check that FileDetailobject initialize with attributes or not,if YES ,then process                        
//                        if (!(newFD.getAttributes() == null && oldFD.getAttributes() == null)) {
//                            differences.putAll(FileDetailCompareUtility.compare(newFD, oldFD));
//                        }
//                    } catch (IllegalAccessException ex) {
//                        ex.printStackTrace();
//                    }
//                });
//            });
//        }
//        return differences;
//    }

    
}
